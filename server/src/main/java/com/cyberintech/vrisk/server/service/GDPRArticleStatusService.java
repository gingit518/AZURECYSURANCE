package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.GDPRArticleStatusLogModelDAO;
import com.cyberintech.vrisk.server.model.dao.GDPRArticleStatusModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.GDPRFilter;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleStatusDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleStatusLogDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleStatusSearchDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPROrganizationStatusDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskEditDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GDPR Article Status management Service. Implements basic CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-20
 */
@Service
@Slf4j
public class GDPRArticleStatusService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private DocumentsRepository documentsRepository;

	@Autowired
	private GDPRArticleItemRepository gdprArticleItemRepository;

	@Autowired
	private GDPRArticleParagraphRepository gdprArticleParagraphRepository;

	@Autowired
	private GDPRArticleStatusLogModelDAO gdprArticleStatusLogModelDAO;

	@Autowired
	private GDPRArticleStatusModelDAO gdprArticleStatusModelDAO;

	@Autowired
	private GDPRArticleStatusRepository gdprArticleStatusRepository;

	@Autowired
	private GDPRArticleStatusLogRepository gdprArticleStatusLogRepository;

	@Autowired
	private GDPRArticleToQuestionRepository gdprArticleToQuestionRepository;

	@Autowired
	private GDPROrganizationStatusRepository gdprOrganizationStatusRepository;

	@Autowired
	private QualitativeQuestionRepository qualitativeQuestionRepository;

	@Autowired
	private QuestionAnswersForVendorRepository questionAnswersForVendorRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private UserService userService;

	/**
	 * Get GDPR Organization Article Status list filtered
	 *
	 * @return items list
	 */
	public FilteredResponse<GDPRFilter, GDPRArticleStatusDTO> getGDPRArticleStatusListFiltered(FilteredRequest<GDPRFilter> filteredRequest) {

		PagedResult<GDPRArticleStatusDTO> result = gdprArticleStatusModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<GDPRFilter, GDPRArticleStatusDTO> filteredResponse = new FilteredResponse<GDPRFilter, GDPRArticleStatusDTO>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get GDPR Organization Article Status Log list filtered
	 *
	 * @return items list
	 */
	public FilteredResponse<GDPRFilter, GDPRArticleStatusLogDTO> getGDPRArticleStatusLogListFiltered(FilteredRequest<GDPRFilter> filteredRequest) {

		PagedResult<GDPRArticleStatusLogDTO> result = gdprArticleStatusLogModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<GDPRFilter, GDPRArticleStatusLogDTO> filteredResponse = new FilteredResponse<GDPRFilter, GDPRArticleStatusLogDTO>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Check GDPR Organization Article Status already exists
	 *
	 * @return article status
	 */
	public GDPRArticleStatusDTO checkArticleStatus(GDPRArticleStatusSearchDTO status) {
		GDPRArticleStatusDTO result;

		Optional<GDPRArticleStatus> articleStatus = findArticleByStatus(status);
		if (articleStatus.isPresent()) {
			result = new GDPRArticleStatusDTO(articleStatus.get());
		} else if (status.getArticle() != null && status.getArticle().getId() != null) {
			Optional<GDPRArticleItem> article = gdprArticleItemRepository.findFirstByIdAndOrganizationId(status.getArticle().getId(), organizationService.getCurrentOrganizationId());
			result = new GDPRArticleStatusDTO((GDPRArticleStatus) null, article.get());
		} else {
			throw new ItemNotFoundException("GDPR Status Item for Organization not found in the database");
		}

		return result;
	}

	/**
	 * Check GDPR Organization Article Status already exists
	 *
	 * @return article status
	 */
	public GDPRArticleStatusDTO saveArticleStatus(GDPRArticleStatusDTO status) {
		GDPRArticleStatusDTO result = null;

		Optional<GDPRArticleStatus> articleStatusOpt = gdprArticleStatusRepository.findByIdAndOrganizationId(status.getId(), organizationService.getCurrentOrganizationId());
		if (articleStatusOpt.isPresent()) {
			GDPRArticleStatus articleStatus = articleStatusOpt.get();
			if (StringUtils.isNotEmpty(status.getComments())) {
				articleStatus.setComments(status.getComments());
			}
			if (status.getTasks() != null) {
				articleStatus.setTasks(new HashSet<>());
				for (TaskEditDTO task : status.getTasks()) {
					Tasks taskItem = taskService.getItemForCurrentOrganization(task.getId());
					articleStatus.getTasks().add(taskItem);
				}
			}

			articleStatus = gdprArticleStatusRepository.save(articleStatus);
			result = new GDPRArticleStatusDTO(articleStatus);
		}

		return result;
	}

	/**
	 * Find GDPR Organization Article Status
	 *
	 * @return article status
	 */
	public Optional<GDPRArticleStatus> findArticleByStatus(GDPRArticleStatusSearchDTO status) {

		Organizations organization = organizationService.getCurrentOrganizationEntity();
		Long articleId = status.getArticle().getId();
		Long paragraphId = status.getParagraph() != null && status.getParagraph().getId() != null ? status.getParagraph().getId() : null;

		Optional<GDPRArticleStatus> result = Optional.empty();

		if (status.getId() != null) {
			result = gdprArticleStatusRepository.findByIdAndOrganizationId(status.getId(), organization.getId());
		} else if (paragraphId == null) {
			result = gdprArticleStatusRepository.getOneByOrganizationAndArticle(articleId, organization.getId());
		} else {
			result = gdprArticleStatusRepository.getOneByOrganizationAndArticleParagraph(articleId, paragraphId, organization.getId());
		}

		return result;
	}

	/**
	 * Get GDPR calculation status for Organization in general
	 *
	 * @return
	 */
	public GDPROrganizationStatusDTO getCurrentGDPROrganizationStatus() {
		Long organizationId = organizationService.getCurrentOrganizationId();

		GDPROrganizationStatusDTO result = new GDPROrganizationStatusDTO();

		Optional<GDPROrganizationStatus> organizationStatusOptional = gdprOrganizationStatusRepository.getOneByOrganization(organizationId);
		if (organizationStatusOptional.isPresent()) {
			result = new GDPROrganizationStatusDTO(organizationStatusOptional.get());
		}

		return result;
	}


	/**
	 * Recalculate Compliance Status
	 *
	 * @param riskModelId
	 * @return
	 */
	public void recalculateComplianceStatus(Long riskModelId) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		Optional<GDPROrganizationStatus> organizationStatusOptional = gdprOrganizationStatusRepository.getOneByOrganization(organizationId);

		GDPROrganizationStatus organizationStatus = null;
		GDPROrganizationStatusDTO existingItem = null;
		if (organizationStatusOptional.isPresent()) {
			organizationStatus = organizationStatusOptional.get();
			existingItem = new GDPROrganizationStatusDTO(organizationStatus);
		} else {
			organizationStatus = new GDPROrganizationStatus();
			organizationStatus.setOrganizationId(organizationId);
		}

		List<QualitativeQuestions> questionsList = qualitativeQuestionRepository.getListByRiskModelIdAndType(riskModelId, Arrays.asList(VendorType.GDPROrganization));
		List<Long> questionIds = questionsList.stream().map(QualitativeQuestions::getId).collect(Collectors.toList());
		List<QuestionAnswersForVendor> gdprAnswers = questionAnswersForVendorRepository.getListByVendorAndQuestions(organizationId, questionIds);

		Double questionsCount = Double.valueOf(questionsList.size());
		Double questionsAnswered = Double.valueOf(gdprAnswers.size());
		Double totalCompliance = 0d;
		for (QuestionAnswersForVendor answer : gdprAnswers) {
			if (answer.getAnswer() != null && answer.getAnswer().getAnswerWeight() != null) {
				long currentWeight = answer.getAnswer().getAnswerWeight().getValue() != null ? answer.getAnswer().getAnswerWeight().getValue() : 0l;
				totalCompliance += (10 - currentWeight) * 10;
			}
		}

		// Update Articles Status and Insert Logs
		Map<Long, QualitativeQuestionAnswers> answersMap = gdprAnswers.stream().collect(Collectors.toMap(answer -> answer.getQuestion().getId(), answer -> answer.getAnswer()));
		Set<GDPRArticleToQuestion> articleQuestions = gdprArticleToQuestionRepository.getAllByOrganizationAndQuestions(organizationId, questionIds);
		for (GDPRArticleToQuestion gdprArticleToQuestion : articleQuestions) {
			if (gdprArticleToQuestion.getArticle() != null) {
				boolean isItemUpdated = false;
				Optional<GDPRArticleStatus> articleStatusOpt = Optional.empty();
				if (gdprArticleToQuestion.getParagraphId() == null) {
					articleStatusOpt = gdprArticleStatusRepository.getOneByOrganizationAndArticle(gdprArticleToQuestion.getArticle().getId(), organizationId);
				} else {
					articleStatusOpt = gdprArticleStatusRepository.getOneByOrganizationAndArticleParagraph(gdprArticleToQuestion.getArticleId(), gdprArticleToQuestion.getParagraphId(), organizationId);
				}
				GDPRArticleStatus articleStatus;
				if (articleStatusOpt.isPresent()) {
					articleStatus = articleStatusOpt.get();
				} else {
					articleStatus = new GDPRArticleStatus();
					articleStatus.setOrganizationId(organizationId);
					articleStatus.setArticle(gdprArticleToQuestion.getArticle());
					articleStatus.setParagraph(gdprArticleToQuestion.getParagraph());
					articleStatus.setCreatedAt(new Date());
					isItemUpdated = true;
				}

				QualitativeQuestionAnswers answer = answersMap.get(gdprArticleToQuestion.getQuestionId());
				if (answer != null && answer.getAnswerWeight() != null) {
					long currentWeight = answer.getAnswerWeight().getValue() != null ? answer.getAnswerWeight().getValue() : 0l;
					double compliance = (10 - currentWeight) * 10;
					if ((articleStatus.getCompliance() == null) || !articleStatus.getCompliance().equals(compliance)) {
						isItemUpdated = true;
					}
					articleStatus.setCompliance(compliance);
				} else {
					isItemUpdated = false;
				}

				gdprArticleStatusRepository.save(articleStatus);

				if (isItemUpdated) {
					insertArticleStatusLog(articleStatus);
				}
			}
		}

		// See https://cvrisk.atlassian.net/browse/VRIS-4527 for details
		gdprArticleStatusRepository.deletePhantomStatuses(organizationId);

		Double totalComplianceItems4Div = questionsCount > 0 ? questionsCount : 1d;
		organizationStatus.setCompliance(totalCompliance / totalComplianceItems4Div);
		// organizationStatus.setFilesNumber(documentNumber);
		organizationStatus.setArticlesNumber(questionsCount);
		organizationStatus.setArticlesProcessed(questionsAnswered);
		organizationStatus = gdprOrganizationStatusRepository.save(organizationStatus);

		GDPROrganizationStatusDTO result = new GDPROrganizationStatusDTO(organizationStatus);
		if (existingItem != null) {
			// Save Audit Log UPDATE event
			auditLogService.update(
				VItemType.GDPR_ORGANIZATION_STATUS,
				result.getId(),
				existingItem,
				result,
				collectAuditLogItemsForGDPROrganizationStatus(result, organizationId)
			);
		} else {
			// Save Audit Log CREATE event
			auditLogService.create(
				VItemType.GDPR_ORGANIZATION_STATUS,
				result.getId(),
				result,
				collectAuditLogItemsForGDPROrganizationStatus(result, organizationId)
			);
		}

	}

	protected GDPRArticleStatusLog insertArticleStatusLog(GDPRArticleStatus articleStatus) {
		GDPRArticleStatusLog articleStatusLog = new GDPRArticleStatusLog();
		articleStatusLog.setOrganizationId(articleStatus.getOrganizationId());
		articleStatusLog.setCreatedAt(new Date());
		articleStatusLog.setArticle(articleStatus.getArticle());
		if (articleStatus.getParagraph() != null) {
			articleStatusLog.setParagraph(articleStatus.getParagraph());
		}
		if (articleStatus.getOwner() != null && articleStatus.getOwner().getId() != null) {
			articleStatusLog.setOwner(articleStatus.getOwner());
		}
		if (articleStatus.getDocument() != null && articleStatus.getDocument().getId() != null) {
			articleStatusLog.setDocumentFileType(articleStatus.getDocument().getFileType());
			articleStatusLog.setDocumentFileName(articleStatus.getDocument().getFileName());
			articleStatusLog.setDocumentFileSize(articleStatus.getDocument().getFileSize());
			articleStatusLog.setDocumentUid(articleStatus.getDocument().getDocumentUid());
			articleStatusLog.setRemotePath(articleStatus.getDocument().getRemotePath());
		}

		// Save Common Fields
		articleStatusLog.setCompliance(articleStatus.getCompliance());
		articleStatusLog.setComplianceMetric(articleStatus.getComplianceMetric());
		articleStatusLog.setDueDate(articleStatus.getDueDate());
		articleStatusLog.setDocumentLink(articleStatus.getDocumentLink());
		articleStatusLog.setComments(articleStatus.getComments());
		articleStatusLog = gdprArticleStatusLogRepository.save(articleStatusLog);

		return articleStatusLog;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItemsForGDPROrganizationStatus(GDPROrganizationStatusDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItemsForGDPRArticleStatus(GDPRArticleStatusDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getOwner() != null) logItems.add(AuditLogItemId.of(VItemType.OWNER_USER, existingItemDTO.getOwner().getId()));
		if (existingItemDTO.getArticle() != null) logItems.add(AuditLogItemId.of(VItemType.GDPR_ARTICLE, existingItemDTO.getArticle().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
