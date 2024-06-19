package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.*;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.GDPRFilter;
import com.cyberintech.vrisk.server.model.dto.gdpr.*;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DataTypeDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GDPR Items management Service. Implements basic CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-24
 */
@Service
@Slf4j
public class GDPRSystemArticleStatusService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private DocumentsRepository documentsRepository;

	@Autowired
	private GDPRArticleItemRepository gdprArticleItemRepository;

	@Autowired
	private GDPRArticleParagraphRepository gdprArticleParagraphRepository;

	@Autowired
	private GDPRArticleToQuestionRepository gdprArticleToQuestionRepository;

	@Autowired
	private GDPRSystemArticleStatusLogModelDAO gdprSystemArticleStatusLogModelDAO;

	@Autowired
	private GDPRSystemArticleStatusModelDAO gdprSystemArticleStatusModelDAO;

	@Autowired
	private GDPRSystemArticleStatusRepository gdprSystemArticleStatusRepository;

	@Autowired
	private GDPRSystemStatusRepository gdprSystemStatusRepository;

	@Autowired
	private GDPRSystemArticleStatusLogRepository gdprSystemArticleStatusLogRepository;

	@Autowired
	private GDPRSystemStatusModelDAO gdprSystemStatusModelDAO;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private QualitativeQuestionRepository qualitativeQuestionRepository;

	@Autowired
	private QuestionAnswersForSystemRepository questionAnswersForSystemRepository;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private UserService userService;

	/**
	 * Get GDPR System Status list filtered
	 *
	 * @return items list
	 */
	public FilteredResponse<GDPRFilter, GDPRSystemStatusDTO> getGDPRSystemStatusListFiltered(FilteredRequest<GDPRFilter> filteredRequest) {

		// Set Data Types limited to PII and Privacy
		filteredRequest.getFilter().setDataTypeClassification(Arrays.asList(DataTypeDomain.PII.getId(), DataTypeDomain.PRIVACY.getId()));

		PagedResult<GDPRSystemStatusDTO> result = gdprSystemStatusModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<GDPRFilter, GDPRSystemStatusDTO> filteredResponse = new FilteredResponse<GDPRFilter, GDPRSystemStatusDTO>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get GDPR System Article Status list filtered
	 *
	 * @return items list
	 */
	public FilteredResponse<GDPRFilter, GDPRSystemArticleStatusDTO> getGDPRSystemArticleStatusListFiltered(FilteredRequest<GDPRFilter> filteredRequest) {

		PagedResult<GDPRSystemArticleStatusDTO> result = gdprSystemArticleStatusModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<GDPRFilter, GDPRSystemArticleStatusDTO> filteredResponse = new FilteredResponse<GDPRFilter, GDPRSystemArticleStatusDTO>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get GDPR System Article Status Log list filtered
	 *
	 * @return items list
	 */
	public FilteredResponse<GDPRFilter, GDPRSystemArticleStatusLogDTO> getGDPRSystemArticleStatusLogListFiltered(FilteredRequest<GDPRFilter> filteredRequest) {

		PagedResult<GDPRSystemArticleStatusLogDTO> result = gdprSystemArticleStatusLogModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<GDPRFilter, GDPRSystemArticleStatusLogDTO> filteredResponse = new FilteredResponse<GDPRFilter, GDPRSystemArticleStatusLogDTO>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Check GDPR System Article Status already exists
	 *
	 * @return article status
	 */
	public GDPRSystemArticleStatusDTO checkArticleStatus(GDPRSystemArticleStatusSearchDTO status) {
		GDPRSystemArticleStatusDTO result;

		Optional<GDPRSystemArticleStatus> articleStatus = findArticleByStatus(status);
		if (articleStatus.isPresent()) {
			result = new GDPRSystemArticleStatusDTO(articleStatus.get());
		} else if (status.getArticle() != null && status.getArticle().getId() != null) {
			Optional<GDPRArticleItem> article = gdprArticleItemRepository.findFirstByIdAndOrganizationId(status.getArticle().getId(), organizationService.getCurrentOrganizationId());
			result = new GDPRSystemArticleStatusDTO((GDPRSystemArticleStatus) null, article.get(), null);
			if (status.getSystem() != null && status.getSystem().getId() != null) {
				result.setSystem(new SystemRefDTO());
				result.getSystem().setId(status.getSystem().getId());
			}
		} else {
			throw new ItemNotFoundException(MessageFormat.format("GDPR Status Item for System [{0}] not found in the database", (status.getSystem() != null ? status.getSystem().getId() : "UNDEFINED")));
		}

		return result;
	}

	/**
	 * Find GDPR System Article Status
	 *
	 * @return article status
	 */
	public Optional<GDPRSystemArticleStatus> findArticleByStatus(GDPRSystemArticleStatusSearchDTO status) {

		Organizations organization = organizationService.getCurrentOrganizationEntity();
		Long systemId = status.getSystem().getId();
		Long articleId = status.getArticle().getId();
		Long paragraphId = status.getParagraph() != null && status.getParagraph().getId() != null ? status.getParagraph().getId() : null;

		Optional<GDPRSystemArticleStatus> result = Optional.empty();

		if (status.getId() != null) {
			result = gdprSystemArticleStatusRepository.findByIdAndOrganizationId(status.getId(), organization.getId());
		} else if (paragraphId == null) {
			result = gdprSystemArticleStatusRepository.getOneByOrganizationAndSystemAndArticle(systemId, articleId, organization.getId());
		} else {
			result = gdprSystemArticleStatusRepository.getOneByOrganizationAndSystemAndArticleParagraph(systemId, articleId, paragraphId, organization.getId());
		}

		return result;
	}

	/**
	 * Recalculate System Compliance Status
	 *
	 * @param systemId
	 * @return
	 */
	public GDPRSystemStatus recalculateComplianceStatus(Long riskModelId, Long systemId) {
		Systems system = systemsService.getSystemForCurrentOrganization(systemId);

		Optional<GDPRSystemStatus> systemStatusOptional = gdprSystemStatusRepository.getOneByOrganizationAndSystem(system.getId(), system.getOrganizationId());

		GDPRSystemStatus systemStatus = null;
		GDPRSystemStatusDTO existingItem = null;
		if (systemStatusOptional.isPresent()) {
			systemStatus = systemStatusOptional.get();
			existingItem = new GDPRSystemStatusDTO(systemStatus);
		} else {
			systemStatus = new GDPRSystemStatus();
			systemStatus.setOrganizationId(system.getOrganizationId());
			systemStatus.setSystem(system);
		}

		List<QualitativeQuestions> questionsList = qualitativeQuestionRepository.getListByRiskModelIdAndType(riskModelId, Arrays.asList(VendorType.GDPRSystem));
		List<Long> questionIds = questionsList.stream().map(QualitativeQuestions::getId).collect(Collectors.toList());
		List<QuestionAnswersForSystem> gdprAnswers = questionAnswersForSystemRepository.getListBySystemAndQuestions(systemId, questionIds);
		Double questionsCount = Double.valueOf(questionsList.size());
		Double questionsAnswered = Double.valueOf(gdprAnswers.size());
		Double totalCompliance = 0d;
		for (QuestionAnswersForSystem answer : gdprAnswers) {
			if (answer.getAnswer() != null && answer.getAnswer().getAnswerWeight() != null) {
				long currentWeight = answer.getAnswer().getAnswerWeight().getValue() != null ? answer.getAnswer().getAnswerWeight().getValue() : 0l;
				totalCompliance += (10 - currentWeight) * 10;
			}
		}

		// Update System Articles Status and Insert Logs
		Map<Long, QualitativeQuestionAnswers> answersMap = gdprAnswers.stream().collect(Collectors.toMap(answer -> answer.getQuestion().getId(), answer -> answer.getAnswer()));
		Set<GDPRArticleToQuestion> articleQuestions = gdprArticleToQuestionRepository.getAllByOrganizationAndQuestions(system.getOrganizationId(), questionIds);
		for (GDPRArticleToQuestion gdprArticleToQuestion : articleQuestions) {
			if (gdprArticleToQuestion.getArticle() != null) {
				boolean isItemUpdated = false;
				Optional<GDPRSystemArticleStatus> systemArticleStatusOpt = Optional.empty();
				if (gdprArticleToQuestion.getParagraphId() == null) {
					systemArticleStatusOpt = gdprSystemArticleStatusRepository.getOneByOrganizationAndSystemAndArticle(systemId, gdprArticleToQuestion.getArticle().getId(), system.getOrganizationId());
				} else {
					systemArticleStatusOpt = gdprSystemArticleStatusRepository.getOneByOrganizationAndSystemAndArticleParagraph(systemId, gdprArticleToQuestion.getArticleId(), gdprArticleToQuestion.getParagraphId(), system.getOrganizationId());
				}
				GDPRSystemArticleStatus systemArticleStatus;
				if (systemArticleStatusOpt.isPresent()) {
					systemArticleStatus = systemArticleStatusOpt.get();
				} else {
					systemArticleStatus = new GDPRSystemArticleStatus();
					systemArticleStatus.setOrganizationId(system.getOrganizationId());
					systemArticleStatus.setSystem(system);
					systemArticleStatus.setArticle(gdprArticleToQuestion.getArticle());
					systemArticleStatus.setParagraph(gdprArticleToQuestion.getParagraph());
					systemArticleStatus.setCreatedAt(new Date());
					isItemUpdated = true;
				}

				QualitativeQuestionAnswers answer = answersMap.get(gdprArticleToQuestion.getQuestionId());
				if (answer != null && answer.getAnswerWeight() != null) {
					long currentWeight = answer.getAnswerWeight().getValue() != null ? answer.getAnswerWeight().getValue() : 0l;
					double compliance = (10 - currentWeight) * 10;
					if ((systemArticleStatus.getCompliance() == null) || !systemArticleStatus.getCompliance().equals(compliance)) {
						isItemUpdated = true;
					}
					systemArticleStatus.setCompliance(compliance);
				} else {
					isItemUpdated = false;
				}

				gdprSystemArticleStatusRepository.save(systemArticleStatus);

				if (isItemUpdated) {
					insertArticleStatusLog(systemArticleStatus);
				}
			}
		}

		Double totalComplianceItems4Div = questionsCount > 0 ? questionsCount : 1d;
		systemStatus.setCompliance(totalCompliance / totalComplianceItems4Div);
		// systemStatus.setFilesNumber(documentNumber);
		systemStatus.setArticlesNumber(questionsCount);
		systemStatus.setArticlesProcessed(questionsAnswered);
		systemStatus = gdprSystemStatusRepository.save(systemStatus);

		GDPRSystemStatusDTO result = new GDPRSystemStatusDTO(systemStatus);
		if (existingItem != null) {
			// Save Audit Log UPDATE event
			auditLogService.update(
				VItemType.GDPR_SYSTEM_STATUS,
				result.getId(),
				existingItem,
				result,
				collectAuditLogItemsForGDPRSystemStatus(result, system.getOrganizationId())
			);
		} else {
			// Save Audit Log CREATE event
			auditLogService.create(
				VItemType.GDPR_SYSTEM_STATUS,
				result.getId(),
				result,
				collectAuditLogItemsForGDPRSystemStatus(result, system.getOrganizationId())
			);
		}

		return systemStatus;
	}

	protected GDPRSystemArticleStatusLog insertArticleStatusLog(GDPRSystemArticleStatus articleStatus) {
		GDPRSystemArticleStatusLog articleStatusLog = new GDPRSystemArticleStatusLog();
		articleStatusLog.setOrganizationId(articleStatus.getOrganizationId());
		articleStatusLog.setCreatedAt(new Date());
		articleStatusLog.setSystem(articleStatus.getSystem());
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
		articleStatusLog = gdprSystemArticleStatusLogRepository.save(articleStatusLog);

		return articleStatusLog;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItemsForGDPRSystemStatus(GDPRSystemStatusDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getSystem() != null) logItems.add(AuditLogItemId.of(VItemType.SYSTEM, existingItemDTO.getSystem().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItemsForGDPRSystemArticleStatus(GDPRSystemArticleStatusDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getOwner() != null) logItems.add(AuditLogItemId.of(VItemType.OWNER_USER, existingItemDTO.getOwner().getId()));
		if (existingItemDTO.getSystem() != null) logItems.add(AuditLogItemId.of(VItemType.SYSTEM, existingItemDTO.getSystem().getId()));
		if (existingItemDTO.getArticle() != null) logItems.add(AuditLogItemId.of(VItemType.GDPR_ARTICLE, existingItemDTO.getArticle().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
