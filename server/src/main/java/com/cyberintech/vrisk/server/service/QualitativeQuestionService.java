package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.QualitativeQuestionModelDAO;
import com.cyberintech.vrisk.server.model.data.*;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.audit.items.QuestionAnswerForSystemsDTO;
import com.cyberintech.vrisk.server.model.dto.audit.items.QuestionAnswerForVendorsDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerWeightDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualMetricsViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QuestionWeightDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.QualitativeQuestionEditDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.QualitativeQuestionViewDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.QualitativeQuestionWithAnswersViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Qualitative Questions management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
@Slf4j
public class QualitativeQuestionService {

	public static final String QUESTION_CATEGORY_HEADER = "Category Name";
	public static final String QUESTION_NAME_HEADER = "Question Name";
	public static final String QUESTION_ID_HEADER = "Code";
	public static final String QUESTION_WEIGHT_HEADER = "Question Weight";
	public static final String QUESTION_ANSWER_HEADER = "Answer";
	public static final String QUESTION_ANSWER_WEIGHT_HEADER = "Answer Weight";
	public static final String QUESTION_DESCRIPTION_HEADER = "Question Description";
	public static final String QUESTION_VENDOR_TYPE_HEADER = "Scoring Type";
	public static final String QUESTION_QUAL_METRIC_HEADER = "Metric Domain";
	public static final String QUESTION_QUAL_METRIC_CATEGORY_HEADER = "Metric Domain Category";
	public static final String QUESTION_ORDINAL_HEADER = "Ordinal";
	public static final String QUESTION_ALL_VENDORS_HEADER = "All Vendors";
	public static final String QUESTION_BRANCHING_LOGIC_HEADER = "Branching Logic";
	public static final String QUESTION_IS_INTERNAL_HEADER = "Internal";
	public static final String QUESTION_ALLOW_MULTIPLE_ANSWERS = "Multiple Answers";
	public static final String QUESTION_UPLOAD_DOCUMENT_HEADER = "Upload Answers";
	public static final String QUESTION_WRITE_TEXT_HEADER = "Allows Text For Answers";
	public static final String QUESTION_ALLOW_COMMENT_HEADER = "Allows Comment To Answers";
	public static final String QUESTION_IS_TECHNOLOGY_VENDOR_HEADER = "Tech Vendor";
	public static final String QUESTION_IS_SYSTEM_VENDOR_HEADER = "System Vendor";
	public static final String QUESTION_IS_SERVICE_VENDOR_HEADER = "Service Vendor";
	public static final String QUESTION_USE_COLOR_CODING = "Use Color Coding";
	public static final String QUESTION_GDPR_LINKAGE_HEADER = "GDPR";

	public static final String ANSWER_TYPE_HEADER = "Answer Type";
	public static final String ANSWER_LINK_HEADER = "Link";
	public static final String ANSWER_TEXT_HEADER = "Answer Text";
	public static final String ANSWER_COMMENT_HEADER = "Comment To Answer";

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	@Autowired
	private DocumentsRepository documentsRepository;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private GDPRArticleItemRepository gdprArticleItemRepository;

	@Autowired
	private GDPRArticleToQuestionRepository gdprArticleToQuestionRepository;

	@Autowired
	private GDPRArticleStatusService gdprArticleStatusService;

	@Autowired
	private GDPRSystemArticleStatusService gdprSystemArticleStatusService;

	/*
	@Autowired
	private KeenIOService keenIOService;
	*/

	@Autowired
	private QualMetricsRepository qualMetricsRepository;

	@Autowired
	private QualMetricsService qualMetricsService;

	@Autowired
	private QualitativeQuestionModelDAO qualitativeQuestionModelDAO;

	@Autowired
	private QualitativeQuestionRepository qualitativeQuestionRepository;

	@Autowired
	private QualitativeQuestionAnswerRepository qualitativeQuestionAnswerRepository;

	@Autowired
	private QuestionBranchingLogicRepository questionBranchingLogicRepository;

	@Autowired
	private RiskTypeRepository riskTypeRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private MetricDomainRepository metricDomainRepository;

	@Autowired
	private MetricResultAnswersRepository metricResultAnswersRepository;

	@Autowired
	private QuestionWeightService questionWeightService;

	@Autowired
	private AnswerWeightRepository answerWeightRepository;

	@Autowired
	private AnswerWeightService answerWeightService;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private QuestionAnswersForVendorRepository questionAnswersForVendorRepository;

	@Autowired
	private QuestionAnswersForSystemRepository questionAnswersForSystemRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private VendorService vendorService;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Get Qualitative Questions List
	 *
	 * @return Qualitative Questions List
	 */
	public List<QualitativeQuestionViewDTO> getList() {
		List<QualitativeQuestions> items = qualitativeQuestionRepository.findAll();

		List<QualitativeQuestionViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, QualitativeQuestionViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Qualitative Questions List
	 *
	 * @return Qualitative Questions List
	 */
	public List<QualitativeQuestionViewDTO> getListByRiskModelId(Long riskModelId) {
		List<QualitativeQuestions> items = qualitativeQuestionRepository.getListByRiskModelId(riskModelId);

		List<QualitativeQuestionViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, QualitativeQuestionViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Qualitative Questions List
	 *
	 * @return Users List
	 */
	public FilteredResponse<QuestionFilter, QualitativeQuestionViewDTO> getListFiltered(Long riskModelId, FilteredRequest<QuestionFilter> filteredRequest) {
		QuestionFilter filter = filteredRequest.getFilter();
		filter.setRiskModelId(riskModelId);

		PagedResult<QualitativeQuestionViewDTO> result = qualitativeQuestionModelDAO.getItemsPageable(filter, filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<QuestionFilter, QualitativeQuestionViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Qualitative Questions List for Vendors
	 *
	 * @param riskModelId
	 * @param vendorFilter
	 * @return
	 */
	public List<QualitativeQuestionWithAnswersViewDTO> getListFilteredForVendorMetric(Long riskModelId, QuestionVendorFilter vendorFilter) {
		Organizations vendor = vendorService.getVendor(vendorFilter.getVendorId());

		/**
		 * Create Advanced Filter for Vendors
		 */
		QuestionVendorAdvancedFilter advancedFilter = new QuestionVendorAdvancedFilter();
		advancedFilter.setVendorId(vendorFilter.getVendorId());
		advancedFilter.setRiskModelId(riskModelId);
		advancedFilter.setIgnoreVendorSelection(vendorFilter.getIgnoreVendorSelection());
		advancedFilter.setQuestionTypes(vendorFilter.getQuestionTypes());
		advancedFilter.setMetricDomain(vendorFilter.getMetricDomain());
		if (userService.isVendorEmployee()) {
			advancedFilter.setIsInternal(true);
			advancedFilter.setIsServiceVendor(vendor.getIsServiceVendor());
			advancedFilter.setIsTechnologyVendor(vendor.getIsTechnologyVendor());
			advancedFilter.setIsSystemVendor(vendor.getIsSystemVendor());
		} else if (Boolean.TRUE.equals(vendorFilter.getIsSelfAssessment())) {
			advancedFilter.setIsInternal(true);
			advancedFilter.setIsServiceVendor(vendor.getIsServiceVendor());
			advancedFilter.setIsTechnologyVendor(vendor.getIsTechnologyVendor());
			advancedFilter.setIsSystemVendor(vendor.getIsSystemVendor());
		}

		return getListFilteredForVendorMetric(riskModelId, advancedFilter);
	}

	public List<QualitativeQuestionWithAnswersViewDTO> getListInternalFilteredForVendorMetric(Long riskModelId, QuestionVendorFilter vendorFilter) {
		Organizations vendor = vendorService.getVendor(vendorFilter.getVendorId());

		/**
		 * Create Advanced Filter for Vendors
		 */
		QuestionVendorAdvancedFilter advancedFilter = new QuestionVendorAdvancedFilter();
		advancedFilter.setVendorId(vendorFilter.getVendorId());
		advancedFilter.setRiskModelId(riskModelId);
		advancedFilter.setIgnoreVendorSelection(vendorFilter.getIgnoreVendorSelection());
		advancedFilter.setQuestionTypes(vendorFilter.getQuestionTypes());
		advancedFilter.setMetricDomain(vendorFilter.getMetricDomain());

		advancedFilter.setIsInternal(true);
		advancedFilter.setIsServiceVendor(vendor.getIsServiceVendor());
		advancedFilter.setIsTechnologyVendor(vendor.getIsTechnologyVendor());
		advancedFilter.setIsSystemVendor(vendor.getIsSystemVendor());

		return getListFilteredForVendorMetric(riskModelId, advancedFilter);
	}

	/**
	 * Get Qualitative Questions List for Vendors
	 *
	 * @param riskModelId
	 * @param advancedFilter
	 * @return
	 */
	public List<QualitativeQuestionWithAnswersViewDTO> getListFilteredForVendorMetric(Long riskModelId, QuestionVendorAdvancedFilter advancedFilter) {

		Pageable pageable = PageRequest.of(0, 2048);
		// Organizations vendor = organizationService.getOrganization(vendorId);

		// MetricDomains metricDomain = metricDomainRepository.findFirstByCodeIgnoreCase(metricDomainCode).get();
		PagedResult<QualitativeQuestionWithAnswersViewDTO> response = qualitativeQuestionModelDAO.getVendorItemsPageable(advancedFilter, pageable, null);
		List<QualitativeQuestionWithAnswersViewDTO> itemsDTOList = response.getItems();
		final Map<Long, QualitativeQuestionWithAnswersViewDTO> questionAnswersMap = itemsDTOList.stream().collect(Collectors.toMap(QualitativeQuestionWithAnswersViewDTO::getId, item -> item));

		List<Long> questionIds = itemsDTOList.stream().map(qualitativeQuestions -> qualitativeQuestions.getId()).collect(Collectors.toList());
		if (questionIds.size() == 0) questionIds.add(0l);
		List<QuestionAnswersForVendor> answers = questionAnswersForVendorRepository.getListByVendorAndQuestions(advancedFilter.getVendorId(), questionIds);
		final Map<Long, List<AnswerViewDTO>> questionAnswers = new HashMap<>();
		answers.stream().forEach(questionAnswersForVendor -> {
			AnswerViewDTO answerView = new AnswerViewDTO(questionAnswersForVendor);
			QualitativeQuestionWithAnswersViewDTO qualitativeQuestionViewVendorDTO = questionAnswersMap.get(questionAnswersForVendor.getQuestion().getId());
			if (qualitativeQuestionViewVendorDTO != null) {
				qualitativeQuestionViewVendorDTO.getSelectedAnswers().add(answerView);
				if (answerView.getDocument() != null) {
					qualitativeQuestionViewVendorDTO.setDocument(answerView.getDocument());

					String downloadUrl = documentService.buildDownloadUrl(answerView.getDocument());
					answerView.getDocument().setDownloadUrl(downloadUrl);
				}
				if (answerView.getAnswerText() != null) {
					qualitativeQuestionViewVendorDTO.setAnswerText(answerView.getAnswerText());
				}
				if (answerView.getAnswerComment() != null) {
					qualitativeQuestionViewVendorDTO.setAnswerComment(answerView.getAnswerComment());
				}
			}
		});

		return itemsDTOList;
	}

	/**
	 * Get Qualitative Questions List for Systems
	 *
	 * @return Users List
	 */
	public List<QualitativeQuestionWithAnswersViewDTO> getListFilteredForSystemMetric(Long riskModelId, Long systemId, String metricDomainCode) {
		return getListFilteredForSystemMetric(riskModelId, systemId, metricDomainCode, Arrays.asList(VendorType.System, VendorType.Both));
	}

	/**
	 * Get Qualitative Questions List for Systems
	 *
	 * @return Users List
	 */
	public List<QualitativeQuestionWithAnswersViewDTO> getListFilteredForSystemMetric(Long riskModelId, Long systemId, String metricDomainCode, List<VendorType> questionTypes) {
		List<QualitativeQuestions> items = null;

		Pageable pageable = PageRequest.of(0, 2048, Sort.by("ordinal"));

		if (StringUtils.isNotEmpty(metricDomainCode)) {
			MetricDomains metricDomain = metricDomainRepository.findFirstByCodeIgnoreCase(metricDomainCode).get();
			items = qualitativeQuestionRepository.getListByRiskModelIdAndSystemAndMetricDomain(riskModelId, systemId, metricDomain.getId(), questionTypes, pageable);
		} else {
			items = qualitativeQuestionRepository.getListByRiskModelIdAndSystem(riskModelId, systemId, questionTypes, pageable);
		}
		List<QualitativeQuestionWithAnswersViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, QualitativeQuestionWithAnswersViewDTO.class);
		final Map<Long, QualitativeQuestionWithAnswersViewDTO> questionAnswersMap = itemsDTOList.stream().collect(Collectors.toMap(QualitativeQuestionWithAnswersViewDTO::getId, item -> item));

		List<Long> questionIds = items.stream().map(qualitativeQuestions -> qualitativeQuestions.getId()).collect(Collectors.toList());
		if (questionIds.size() == 0) questionIds.add(0l);
		List<QuestionAnswersForSystem> answers = questionAnswersForSystemRepository.getListBySystemAndQuestions(systemId, questionIds);
		final Map<Long, List<AnswerViewDTO>> questionAnswers = new HashMap<>();
		answers.stream().forEach(questionAnswersForSystem -> {
			AnswerViewDTO answerView = new AnswerViewDTO(questionAnswersForSystem);
			QualitativeQuestionWithAnswersViewDTO qualitativeQuestionViewSystemDTO = questionAnswersMap.get(questionAnswersForSystem.getQuestion().getId());
			if (qualitativeQuestionViewSystemDTO != null) {
				qualitativeQuestionViewSystemDTO.getSelectedAnswers().add(answerView);
				if (answerView.getDocument() != null) {
					qualitativeQuestionViewSystemDTO.setDocument(answerView.getDocument());

					String downloadUrl = documentService.buildDownloadUrl(answerView.getDocument());
					answerView.getDocument().setDownloadUrl(downloadUrl);
				}
				if (answerView.getAnswerText() != null) {
					qualitativeQuestionViewSystemDTO.setAnswerText(answerView.getAnswerText());
				}
				if (answerView.getAnswerComment() != null) {
					qualitativeQuestionViewSystemDTO.setAnswerComment(answerView.getAnswerComment());
				}
			}
		});

		return itemsDTOList;
	}

	/**
	 * Save Qualitative Questions List for Vendors
	 *
	 * @return Users List
	 */
	public List<QualitativeQuestionWithAnswersViewDTO> saveVendorQuestions(Long riskModelId, Long vendorId, String metricDomainCode, List<QualitativeQuestionWithAnswersViewDTO> questions) {
		return saveVendorQuestions(riskModelId, vendorId, metricDomainCode, questions, VendorType.Vendor);
	}

	/**
	 * Save Qualitative Questions List for Vendors
	 *
	 * @return Users List
	 */
	public List<QualitativeQuestionWithAnswersViewDTO> saveVendorQuestions(Long riskModelId, Long vendorId, String metricDomainCode, List<QualitativeQuestionWithAnswersViewDTO> questions, VendorType vendorType) {

		Users currentUser = userService.getCurrentUserEntity();
		Long organizationId = currentUser.getOrganization().getId();
		Organizations vendor = organizationRepository.findById(vendorId).get();
		List<QualitativeQuestionWithAnswersViewDTO> result = new ArrayList<>();
		for (QualitativeQuestionWithAnswersViewDTO question : questions) {

			QualitativeQuestions questionEntity = qualitativeQuestionRepository.findById(question.getId()).get();

			// Set Current question View
			QualitativeQuestionWithAnswersViewDTO currentQuestionViewDTO = new QualitativeQuestionWithAnswersViewDTO(questionEntity);
			currentQuestionViewDTO.setSelectedAnswers(new ArrayList<>());
			result.add(currentQuestionViewDTO);

			List<QuestionAnswersForVendor> answers = questionAnswersForVendorRepository.getListByVendorAndQuestions(vendorId, Arrays.asList(question.getId()));

			// Find items to remove
			List<AnswerViewDTO> newAnswers = new ArrayList<>();
			List<AnswerViewDTO> existingAnswers = new ArrayList<>();
			DocumentDTO currentDocument = question.getDocument();
			// Map<Long, Long> answersMap = answers.stream().filter(o -> o.getAnswer() != null).collect(Collectors.toMap(o -> o.getAnswer().getId(), o -> o.getId()));
			Map<Long, Long> answersMap = answers.stream().collect(Collectors.toMap(o -> {
				return o.getAnswer() != null ? o.getAnswer().getId() : o.getId() + 1000000000L;
			}, o -> o.getId()));
			Optional.ofNullable(question.getSelectedAnswers()).orElse(new ArrayList<>()).stream().filter(answerViewDTO -> answerViewDTO.getId() != null).forEach(answerViewDTO -> {
				if (answersMap.containsKey(answerViewDTO.getId())) {
					answersMap.remove(answerViewDTO.getId());
					existingAnswers.add(answerViewDTO);
				} else {
					newAnswers.add(answerViewDTO);
				}
			});

			// Add Exception for Document and Text type of the Answers
			if (
				Boolean.TRUE.equals(question.getAllowUploadAsAnswer())
					|| Boolean.TRUE.equals(question.getAllowTextAsAnswer())
					|| Boolean.TRUE.equals(question.getAllowCommentToAnswer())
			) {
				if (StringUtils.isNotEmpty(question.getAnswerText()) || StringUtils.isNotEmpty(question.getAnswerComment()) || question.getDocument() != null) {
					AnswerViewDTO answerViewDTO = new AnswerViewDTO();
					answerViewDTO.setAnswerText(question.getAnswerText());
					answerViewDTO.setAnswerComment(question.getAnswerComment());
					answerViewDTO.setDocument(currentDocument);
					newAnswers.add(answerViewDTO);

					// Set answer comment and Text
					currentQuestionViewDTO.setDocument(currentDocument);
					currentQuestionViewDTO.setAnswerText(question.getAnswerText());
					currentQuestionViewDTO.setAnswerComment(question.getAnswerComment());
				}
			}

			for (Map.Entry<Long, Long> longLongEntry : answersMap.entrySet()) {

				Optional<QuestionAnswersForVendor> answersForVendor = questionAnswersForVendorRepository.findById(longLongEntry.getValue());
				if (answersForVendor.isPresent()) {
					if (currentDocument == null && answersForVendor.get().getDocument() != null) {
						currentDocument = new DocumentDTO(answersForVendor.get().getDocument());
					}
					// Save Audit Log DELETE event
					auditLogService.delete(
						VItemType.QUALITATIVE_QUESTION_ANSWER_FOR_VENDOR,
						answersForVendor.get().getId(),
						new QuestionAnswerForVendorsDTO(answersForVendor.get()),
						AuditLogItemId.of(VItemType.QUALITATIVE_QUESTION, question.getId()), AuditLogItemId.of(VItemType.VENDOR, vendor.getId())
					);

					questionAnswersForVendorRepository.deleteById(longLongEntry.getValue());
				}
			}
			for (AnswerViewDTO answerViewDTO : existingAnswers) {
				currentQuestionViewDTO.getSelectedAnswers().add(answerViewDTO);
			}
			for (AnswerViewDTO answerViewDTO : newAnswers) {
				// Ignore empty answers
				if (answerViewDTO.getAnswer() == null && answerViewDTO.getDocument() == null && answerViewDTO.getAnswerText() == null && answerViewDTO.getAnswerComment() == null) {
					continue;
				}

				QualitativeQuestionAnswers answerEntity = answerViewDTO.getId() != null ? qualitativeQuestionAnswerRepository.findById(answerViewDTO.getId()).get() : null;

				// Init question for Vendor
				QuestionAnswersForVendor questionAnswersForVendor = new QuestionAnswersForVendor();
				questionAnswersForVendor.setQuestion(questionEntity);
				questionAnswersForVendor.setAnswer(answerEntity);
				questionAnswersForVendor.setVendor(vendor);
				questionAnswersForVendor.setCreatedAt(new Date());
				questionAnswersForVendor.setCreatedBy(currentUser);
				questionAnswersForVendor.setUpdatedAt(new Date());
				questionAnswersForVendor.setUpdatedBy(currentUser);
				if (currentDocument != null && currentDocument.getId() != null) {
					questionAnswersForVendor.setDocument(documentsRepository.findByIdAndOrganizationId(currentDocument.getId(), organizationId).orElse(null));
				}
				if (StringUtils.isNotEmpty(question.getAnswerText())) {
					questionAnswersForVendor.setAnswerText(question.getAnswerText());
				}
				if (StringUtils.isNotEmpty(question.getAnswerComment())) {
					questionAnswersForVendor.setAnswerComment(question.getAnswerComment());
				}
				questionAnswersForVendorRepository.save(questionAnswersForVendor);

				// Save Audit Log CREATE event
				auditLogService.create(
					VItemType.QUALITATIVE_QUESTION_ANSWER_FOR_VENDOR,
					questionAnswersForVendor.getId(),
					new QuestionAnswerForVendorsDTO(questionAnswersForVendor),
					AuditLogItemId.of(VItemType.QUALITATIVE_QUESTION, question.getId()), AuditLogItemId.of(VItemType.VENDOR, vendor.getId())
				);

				// Adding Question Answer Event to KeenIO
				// keenIOService.addQuestionAnswerEvent(questionAnswersForVendor);

				// Set Result Answer view
				if (answerEntity != null && answerEntity.getId() != null) currentQuestionViewDTO.getSelectedAnswers().add(new AnswerViewDTO(answerEntity));
			}
		}

		/*
		QuestionVendorAdvancedFilter advancedFilter = new QuestionVendorAdvancedFilter();
		advancedFilter.setRiskModelId(riskModelId);
		advancedFilter.setVendorId(vendorId);
		advancedFilter.setMetricDomain(metricDomainCode);
		advancedFilter.setQuestionTypes(Arrays.asList(vendorType));
		advancedFilter.setIgnoreInternal(true);
		List<QualitativeQuestionWithAnswersViewDTO> itemsDTOList = getListFilteredForVendorMetric(riskModelId, advancedFilter);
		 */

		return result;
	}

	/**
	 * Queue all KeenIO question answers
	 */
	public void queueAllKeenIOQuestionAnswers() {
		Long organizationId = organizationService.getCurrentOrganizationId();

		List<QuestionAnswersForVendor> vendorAnswers = questionAnswersForVendorRepository.getAllByOrganizationId(organizationId);
		List<QuestionAnswersForSystem> systemAnswers = questionAnswersForSystemRepository.getAllByOrganizationId(organizationId);

		int processesItems = 0;
		for (QuestionAnswersForVendor item : vendorAnswers) {
			// keenIOService.addQuestionAnswer4VendorEvent(item.getId());
			processesItems++;
			log.info("Processing item: " + processesItems);
		}
		for (QuestionAnswersForSystem item : systemAnswers) {
			// keenIOService.addQuestionAnswer4SystemEvent(item.getId());
			processesItems++;
			log.info("Processing item: " + processesItems);
		}

		log.info("Processed items: " + processesItems);
	}

	/**
	 * Save Qualitative Questions List for Vendors
	 *
	 * @return Users List
	 */
	public List<QualitativeQuestionWithAnswersViewDTO> saveSystemQuestions(Long riskModelId, Long systemId, String metricDomainCode, List<QualitativeQuestionWithAnswersViewDTO> questions) {

		Users currentUser = userService.getCurrentUserEntity();
		Long organizationId = currentUser.getOrganization().getId();
		Systems system = systemRepository.findById(systemId).get();
		for (QualitativeQuestionWithAnswersViewDTO question : questions) {

			QualitativeQuestions questionEntity = qualitativeQuestionRepository.findById(question.getId()).get();

			List<QuestionAnswersForSystem> answers = questionAnswersForSystemRepository.getListBySystemAndQuestions(systemId, Arrays.asList(question.getId()));

			// Find items to remove
			DocumentDTO currentDocument = question.getDocument();
			List<AnswerViewDTO> newAnswers = new ArrayList<>();
			// Map<Long, Long> answersMap = answers.stream().filter(o -> o.getAnswer() != null).collect(Collectors.toMap(o -> o.getAnswer().getId(), o -> o.getId()));
			Map<Long, Long> answersMap = answers.stream().collect(Collectors.toMap(o -> {
				return o.getAnswer() != null ? o.getAnswer().getId() : o.getId() + 1000000000L;
			}, o -> o.getId()));
			Optional.ofNullable(question.getSelectedAnswers()).orElse(new ArrayList<>()).stream().filter(answerViewDTO -> answerViewDTO.getId() != null).forEach(answerViewDTO -> {
				if (answersMap.containsKey(answerViewDTO.getId())) {
					answersMap.remove(answerViewDTO.getId());
				} else {
					newAnswers.add(answerViewDTO);
				}
			});

			// Add Exception for Document and Text type of the Answers
			if (Boolean.TRUE.equals(question.getAllowUploadAsAnswer()) || Boolean.TRUE.equals(question.getAllowTextAsAnswer()) || Boolean.TRUE.equals(question.getAllowCommentToAnswer())) {
				if (StringUtils.isNotEmpty(question.getAnswerText()) || StringUtils.isNotEmpty(question.getAnswerComment()) || question.getDocument() != null){
					AnswerViewDTO answerViewDTO = new AnswerViewDTO();
					answerViewDTO.setAnswerText(question.getAnswerText());
					answerViewDTO.setAnswerComment(question.getAnswerComment());
					answerViewDTO.setDocument(currentDocument);
					newAnswers.add(answerViewDTO);
				}
			}

			for (Map.Entry<Long, Long> longLongEntry : answersMap.entrySet()) {
				Optional<QuestionAnswersForSystem> answerForSystem = questionAnswersForSystemRepository.findById(longLongEntry.getValue());
				if (answerForSystem.isPresent()) {
					if (currentDocument == null && answerForSystem.get().getDocument() != null) {
						currentDocument = new DocumentDTO(answerForSystem.get().getDocument());
					}

					// Save Audit Log DELETE event
					auditLogService.delete(
						VItemType.QUALITATIVE_QUESTION_ANSWER_FOR_SYSTEM,
						answerForSystem.get().getId(),
						new QuestionAnswerForSystemsDTO(answerForSystem.get()),
						AuditLogItemId.of(VItemType.QUALITATIVE_QUESTION, question.getId()), AuditLogItemId.of(VItemType.SYSTEM, system.getId())
					);

					// Deleting item
					questionAnswersForSystemRepository.delete(answerForSystem.get());
				}
			}

			for (AnswerViewDTO answerViewDTO : newAnswers) {
				// Ignore empty answers
				if (answerViewDTO.getAnswer() == null && answerViewDTO.getDocument() == null && answerViewDTO.getAnswerText() == null && answerViewDTO.getAnswerComment() == null) {
					continue;
				}

				QualitativeQuestionAnswers answerEntity = answerViewDTO.getId() != null ? qualitativeQuestionAnswerRepository.findById(answerViewDTO.getId()).get() : null;

				// Init question for Vendor
				QuestionAnswersForSystem questionAnswersForSystem = new QuestionAnswersForSystem();
				questionAnswersForSystem.setQuestion(questionEntity);
				questionAnswersForSystem.setAnswer(answerEntity);
				questionAnswersForSystem.setSystem(system);
				questionAnswersForSystem.setCreatedAt(new Date());
				questionAnswersForSystem.setCreatedBy(currentUser);
				questionAnswersForSystem.setUpdatedAt(new Date());
				questionAnswersForSystem.setUpdatedBy(currentUser);
				if (currentDocument != null && currentDocument.getId() != null) {
					questionAnswersForSystem.setDocument(documentsRepository.findByIdAndOrganizationId(currentDocument.getId(), organizationId).orElse(null));
				}
				if (StringUtils.isNotEmpty(question.getAnswerText())) {
					questionAnswersForSystem.setAnswerText(question.getAnswerText());
				}
				if (StringUtils.isNotEmpty(question.getAnswerComment())) {
					questionAnswersForSystem.setAnswerComment(question.getAnswerComment());
				}
				questionAnswersForSystemRepository.save(questionAnswersForSystem);

				// Save Audit Log CREATE event
				auditLogService.create(
					VItemType.QUALITATIVE_QUESTION_ANSWER_FOR_SYSTEM,
					questionAnswersForSystem.getId(),
					new QuestionAnswerForSystemsDTO(questionAnswersForSystem),
					AuditLogItemId.of(VItemType.QUALITATIVE_QUESTION, question.getId()), AuditLogItemId.of(VItemType.SYSTEM, system.getId())
				);

				// Adding Question Answer Event to KeenIO
				// keenIOService.addQuestionAnswerEvent(questionAnswersForSystem);
			}
		}

		List<QualitativeQuestionWithAnswersViewDTO> itemsDTOList = getListFilteredForSystemMetric(riskModelId, systemId, metricDomainCode);

		return itemsDTOList;
	}

	/**
	 * Save Qualitative Questions List for System in the GDPR context
	 *
	 * @return Users List
	 */
	public List<QualitativeQuestionWithAnswersViewDTO> saveGDPRSystemQuestions(Long riskModelId, Long systemId, String metricDomainCode, List<QualitativeQuestionWithAnswersViewDTO> questions) {
		List<QualitativeQuestionWithAnswersViewDTO> result = saveSystemQuestions(riskModelId, systemId, metricDomainCode, questions);

		// Recalculate GDPR Compliance for the System
		gdprSystemArticleStatusService.recalculateComplianceStatus(riskModelId, systemId);

		return result;
	}

	/**
	 * Save Qualitative Questions List for Organization in the GDPR context
	 *
	 * @return Users List
	 */
	public List<QualitativeQuestionWithAnswersViewDTO> saveGDPROrganizationQuestions(Long riskModelId, String metricDomainCode, List<QualitativeQuestionWithAnswersViewDTO> questions) {
		Long vendorId = organizationService.getCurrentOrganizationId();
		List<QualitativeQuestionWithAnswersViewDTO> result = saveVendorQuestions(riskModelId, vendorId, metricDomainCode, questions, VendorType.GDPROrganization);

		// Recalculate GDPR Compliance for the Organization
		gdprArticleStatusService.recalculateComplianceStatus(riskModelId);

		return result;
	}

	/**
	 * Get Qualitative Question details
	 *
	 * @return Qualitative Question Details
	 */
	public QualitativeQuestions getQualitativeQuestion(Long itemId) {
		QualitativeQuestions itemDetails;

		try {
			itemDetails = qualitativeQuestionRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Qualitative Question not found in the database [{0}]", itemId));
		}

		// Verify Risk Model and Organization
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		return itemDetails;
	}

	/**
	 * Get Qualitative Question DTO details
	 *
	 * @return Qualitative Question Details
	 */
	public QualitativeQuestionEditDTO getDetails(Long itemId) {

		QualitativeQuestions itemDetails = getQualitativeQuestion(itemId);

		QualitativeQuestionEditDTO result = new QualitativeQuestionEditDTO(itemDetails);
		result.setAnswerWeights(answerWeightService.getList());

		return result;
	}


	/**
	 * Create new Qualitative Question Domain
	 *
	 * @return New Qualitative Question
	 */
	public QualitativeQuestionEditDTO create(QualitativeQuestionEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		RiskModels riskModel = riskModelService.getRiskModel(newItemDTO.getRiskModelId());
		if (newItemDTO.getQualitativeMetric() == null) {
			log.error(String.format("## Failed to create Question because Qual Metric is NULL: %s", newItemDTO.getQuestion()));
		}
		QualMetrics qualMetrics = qualMetricsService.getQualMetric(newItemDTO.getQualitativeMetric().getId());

		if (newItemDTO.getOrdinal() == null) {
			Long maxOrdinal = qualitativeQuestionRepository.getMaxOrdinalByRiskModelId(newItemDTO.getRiskModelId());
			newItemDTO.setOrdinal(maxOrdinal != null ? maxOrdinal + 1 : 1);
		}

//		QualitativeQuestions newItem = newItemDTO.toEntity();
		QualitativeQuestions newItem = new QualitativeQuestions();
		if (newItem.getAnswers() != null) newItem.getAnswers().clear(); // Clear Risk types as it is not usable to get there outside JPA
		newItem.setRiskModelId(riskModel.getId());
		newItem.setQualitativeMetric(qualMetrics);
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyQuestionChanges(newItemDTO, newItem);
		QualitativeQuestions saveResult = qualitativeQuestionRepository.save(newItem);

		// Save answers
		synchronizeAnswers(saveResult, newItemDTO.getAnswers());
		qualitativeQuestionRepository.flush();
		saveResult = qualitativeQuestionRepository.findById(saveResult.getId()).get();

		QualitativeQuestionEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.QUALITATIVE_QUESTION,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, organizationService.getCurrentOrganizationId())
		);

		return result;
	}

	/**
	 * Update Qualitative Question
	 *
	 * @return Updated Qualitative Domains
	 */
	public QualitativeQuestionEditDTO update(QualitativeQuestionEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		QualitativeQuestions existingItem = getQualitativeQuestion(itemDTO.getId());
		QualitativeQuestionEditDTO existingItemDTO = new QualitativeQuestionEditDTO(existingItem);

		// Verify Qualitative Question and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());
		QualMetrics qualMetrics = null;
		if (itemDTO.getQualitativeMetric() != null) {
			qualMetrics = qualMetricsService.getQualMetric(itemDTO.getQualitativeMetric().getId());
		} else {
			log.warn("Qual Metric Not found for item: " + itemDTO.getQuestion());
		}

		// Update item details
		existingItem.setQualitativeMetric(qualMetrics);
		applyQuestionChanges(itemDTO, existingItem);

		// Save answers
		synchronizeAnswers(existingItem, itemDTO.getAnswers());
		// synchronizeAnswers(saveResult, itemDTO.getAnswers());
		// saveResult = qualitativeQuestionRepository.findById(saveResult.getId()).get();

		// Save to the database
		QualitativeQuestions saveResult = qualitativeQuestionRepository.save(existingItem);

		QualitativeQuestionEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.QUALITATIVE_QUESTION,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, organizationService.getCurrentOrganizationId())
		);

		return result;
	}

	/**
	 * Update Branching Logic for Qualitative Question
	 *
	 * @return Updated Qualitative Domains
	 */
	public QualitativeQuestionEditDTO updateBranchingLogicFromString(Long itemId, String branchingLogicString) {

		QualitativeQuestions qualitativeQuestion = getQualitativeQuestion(itemId);
		QualitativeQuestionEditDTO existingItemDTO = new QualitativeQuestionEditDTO(qualitativeQuestion);
		Set<QuestionBranchingLogic> branchingLogic = new HashSet<>();
		if (!branchingLogicString.equalsIgnoreCase("none")) {
			String[] rulesStrings = StringUtils.split(branchingLogicString, "@@");
			long branchingItemIndex = 0;
			for (String rule : rulesStrings) {
				String[] ruleParts = StringUtils.split(rule, "|");
				if (ruleParts.length > 2) {
					String originalQuestion = ruleParts[0].trim();
					String originalQuestionAnswer = ruleParts[1].trim();
					Long ordinal = branchingItemIndex;
					Long operation = 0l;
					try {
						if (ruleParts.length > 2) ordinal = Long.valueOf(ruleParts[2]);
						if (ruleParts.length > 3) operation = Long.valueOf(ruleParts[3]);
					} catch (NumberFormatException e) {}

					Optional<QualitativeQuestions> relatedQuestion = qualitativeQuestionRepository.findFirstByRiskModelIdAndQuestion(qualitativeQuestion.getRiskModelId(), originalQuestion);
					if (relatedQuestion.isPresent()) {
						List<QualitativeQuestionAnswers> relatedAnswer = qualitativeQuestionAnswerRepository.getByQuestionAndAnswer(relatedQuestion.get().getId(), originalQuestionAnswer);

						if (relatedAnswer.size() > 0) {
							QuestionBranchingLogic branchingLogicRule = new QuestionBranchingLogic();
							branchingLogicRule.setQuestion(relatedQuestion.get());
							branchingLogicRule.setAnswer(relatedAnswer.get(0));
							branchingLogicRule.setOrdinal(ordinal);
							branchingLogicRule.setOperation(operation);
							branchingLogicRule = questionBranchingLogicRepository.save(branchingLogicRule);
							branchingLogic.add(branchingLogicRule);
						} else {
							log.warn("## Answer not found for Branching Logic rule: " + originalQuestionAnswer + " [SKIPPING]");
						}
					} else {
						log.warn("## Question not found for Branching Logic rule: " + originalQuestion + " [SKIPPING]");
					}

					branchingItemIndex++;
				}
			}
		}
		qualitativeQuestion.setBranchingLogic(branchingLogic);

		// Save to the database
		QualitativeQuestions saveResult = qualitativeQuestionRepository.save(qualitativeQuestion);
		QualitativeQuestionEditDTO result = new QualitativeQuestionEditDTO(saveResult);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.QUALITATIVE_QUESTION,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, organizationService.getCurrentOrganizationId())
		);

		return result;
	}

	/**
	 * Update GDPR Article Mapping for Qualitative Question
	 *
	 * @param gdprQuestionMap
	 * @param organizationId
	 */
	protected void updateGDPRArticleMapping(Map<Long, String> gdprQuestionMap, Long organizationId) {

		for (Map.Entry<Long, String> mappingEntry : gdprQuestionMap.entrySet()) {
			Long questionId = mappingEntry.getKey();
			String mappingString = mappingEntry.getValue();

			String[] itemsStrings = StringUtils.split(mappingString, ":");
			if (itemsStrings.length > 0) {
				String articleNumberString = itemsStrings[itemsStrings.length - 1];
				String articleNumber = articleNumberString.replaceAll("[a-zA-Z]+", "").trim();
				String paragraphNumber = articleNumberString.replaceAll("\\d+", "").trim();
				Long articleId = null;
				Long paragraphId = null;
				if (StringUtils.isEmpty(articleNumber)) {
					log.warn(MessageFormat.format("## GDPR Article empty for Question Mapping: {0}/{1} [SKIPPING]", articleNumberString, questionId));
					continue;
				}

				Optional<GDPRArticleItem> articleOptional = gdprArticleItemRepository.findFirstByReferenceNumberIgnoreCaseAndOrganizationId(articleNumber, organizationId);
				if (articleOptional.isEmpty()) {
					log.warn(MessageFormat.format("## GDPR Article not found for Question Mapping: {0}/{1} [SKIPPING]", articleNumberString, questionId));
					continue;
				}
				articleId = articleOptional.get().getId();

				Optional<GDPRArticleParagraph> paragraphOptional = Optional.empty();
				if (StringUtils.isNotEmpty(paragraphNumber)) {
					paragraphOptional = articleOptional.get().getParagraphs().stream().filter(paragraph -> paragraphNumber.equalsIgnoreCase(paragraph.getName())).findFirst();
					if (paragraphOptional.isPresent()) {
						paragraphId = paragraphOptional.get().getId();
					}
				}

				GDPRArticleToQuestion articleToQuestion = gdprArticleToQuestionRepository.findByQuestionIdAndArticleIdAndParagraphId(questionId, articleId, paragraphId);
				if (articleToQuestion == null) {
					articleToQuestion = new GDPRArticleToQuestion();
					articleToQuestion.setOrganizationId(organizationId);
					articleToQuestion.setQuestionId(questionId);
					articleToQuestion.setArticleId(articleId);
					articleToQuestion.setParagraphId(paragraphId);

					gdprArticleToQuestionRepository.save(articleToQuestion);
					log.info(MessageFormat.format("## GDPR Article Question Mapping Saved: [{0}, {1}, {2}]", questionId, articleId, paragraphId));
				}
			}
		}

		/*
		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.QUALITATIVE_QUESTION,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, organizationService.getCurrentOrganizationId())
		);
		*/

	}

	/**
	 * Apply question changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyQuestionChanges(QualitativeQuestionEditDTO itemDTO, QualitativeQuestions entity) {
		entity.setQuestion(itemDTO.getQuestion());
		entity.setDescription(itemDTO.getDescription());
		entity.setCategoryName(itemDTO.getCategoryName());
		entity.setVendorType(itemDTO.getVendorType());

		// Set Risk Types
		Optional.ofNullable(itemDTO.getRiskTypes()).ifPresent(riskTypeViewDTOList -> {
			entity.setRiskTypes(new HashSet<>());
			riskTypeViewDTOList.stream().forEach(riskTypeViewDTO -> {
				entity.getRiskTypes().add(riskTypeRepository.findById(riskTypeViewDTO.getId()).get());
			});
		});

		// Set Vendors
		Optional.ofNullable(itemDTO.getVendors()).ifPresent(vendorsList -> {
			entity.setVendors(new HashSet<>());
			vendorsList.stream().forEach(vendorView -> {
				entity.getVendors().add(organizationRepository.findById(vendorView.getId()).get());
			});
		});

		// Set Systems
		Optional.ofNullable(itemDTO.getSystems()).ifPresent(systemList -> {
			entity.setSystems(new HashSet<>());
			systemList.stream().forEach(systemRef -> {
				entity.getSystems().add(systemRepository.findById(systemRef.getId()).get());
			});
		});

		// Set Branching Logic
		Optional.ofNullable(itemDTO.getBranchingLogic()).ifPresent(branchingLogicList -> {
			entity.setBranchingLogic(new HashSet<>());
			branchingLogicList.stream().forEach(branchingLogicView -> {
				QuestionBranchingLogic branchingLogicItem;
				if (branchingLogicView.getId() != null) {
					branchingLogicItem = questionBranchingLogicRepository.findById(branchingLogicView.getId()).get();
				} else {
					branchingLogicItem = new QuestionBranchingLogic();
				}

				if (branchingLogicItem != null) {
					branchingLogicItem.setQuestion(qualitativeQuestionRepository.findById(branchingLogicView.getQuestion().getId()).get());
					branchingLogicItem.setAnswer(qualitativeQuestionAnswerRepository.findById(branchingLogicView.getAnswer().getId()).get());
					branchingLogicItem.setOrdinal(branchingLogicView.getOrdinal());
					branchingLogicItem.setOperation(branchingLogicView.getOperation());
					branchingLogicItem = questionBranchingLogicRepository.save(branchingLogicItem);
					entity.getBranchingLogic().add(branchingLogicItem);
				}
			});
		});

		if (itemDTO.getQuestionWeight() != null && itemDTO.getQuestionWeight().getId() != null) {
			QuestionWeights questionWeights = questionWeightService.getQuestionWeight(itemDTO.getQuestionWeight().getId());
			entity.setQuestionWeight(questionWeights);
		}

		// Synchronize existing properties
		if (StringUtils.isNotEmpty(itemDTO.getCode())) entity.setCode(itemDTO.getCode());
		if (itemDTO.getAllVendorsSelected() != null) entity.setAllVendorsSelected(itemDTO.getAllVendorsSelected());
//		if (itemDTO.getIsInternal() != null) entity.setIsInternal(itemDTO.getIsInternal());
		if (itemDTO.getIsServiceVendor() != null) entity.setIsServiceVendor(itemDTO.getIsServiceVendor());
		if (itemDTO.getIsSystemVendor() != null) entity.setIsSystemVendor(itemDTO.getIsSystemVendor());
		if (itemDTO.getIsTechnologyVendor() != null) entity.setIsTechnologyVendor(itemDTO.getIsTechnologyVendor());
		if (itemDTO.getAllowMultipleAnswers() != null) entity.setAllowMultipleAnswers(itemDTO.getAllowMultipleAnswers());
		if (itemDTO.getAllowTextAsAnswer() != null) entity.setAllowTextAsAnswer(itemDTO.getAllowTextAsAnswer());
		if (itemDTO.getAllowCommentToAnswer() != null) entity.setAllowCommentToAnswer(itemDTO.getAllowCommentToAnswer());
		if (itemDTO.getAllowUploadAsAnswer() != null) entity.setAllowUploadAsAnswer(itemDTO.getAllowUploadAsAnswer());
		if (itemDTO.getUseColorCoding() != null) entity.setUseColorCoding(itemDTO.getUseColorCoding());
		if (itemDTO.getOrdinal() != null) {
			entity.setOrdinal(itemDTO.getOrdinal());
		}

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Synchronize Risk Types for Category Domain
	 *
	 * @param qualitativeQuestions
	 * @param answers
	 */
	private void synchronizeAnswers(QualitativeQuestions qualitativeQuestions, List<AnswerViewDTO> answers) {

		List<AnswerViewDTO> answersCollection = Optional.ofNullable(answers).orElse(new ArrayList<>());

		Map<Long, AnswerViewDTO> riskTypesMap = answersCollection.stream().filter(answer -> answer.getId() != null).collect(Collectors.toMap(AnswerViewDTO::getId, answer -> answer, (oldItem, newItem) -> oldItem));

		// Collect Items to Remove
		List<QualitativeQuestionAnswers> itemsToRemove = new ArrayList<>();
		qualitativeQuestions.getAnswers().stream().forEach(answer -> {
			if (!riskTypesMap.containsKey(answer.getId())) {
				itemsToRemove.add(answer);
			}
		});

		// Physically delete item from DB
		for (QualitativeQuestionAnswers item : itemsToRemove) {
			questionAnswersForVendorRepository.deleteByAnswerId(item.getId());
			questionAnswersForSystemRepository.deleteByAnswerId(item.getId());
			metricResultAnswersRepository.deleteByAnswerId(item.getId());

			// qualitativeQuestionAnswerRepository.delete(item);
			// qualitativeQuestionAnswerRepository.deleteByItemId(item.getId());
		}

		// qualitativeQuestions.getAnswers().clear();
		qualitativeQuestions.getAnswers().removeAll(itemsToRemove);

		// Save Risk Types
		answersCollection.stream().forEach(answerViewDTO -> {
			QualitativeQuestionAnswers answerEntity;
			if (answerViewDTO.getId() != null) {
				answerEntity = qualitativeQuestionAnswerRepository.findById(answerViewDTO.getId()).get();
			} else {
				answerEntity = new QualitativeQuestionAnswers();
				answerEntity.setCreatedAt(new Date());
			}

			AnswerWeight answerWeight = null;

			if (answerViewDTO.getAnswerWeight() != null) {
				answerWeight = answerWeightRepository.findById(answerViewDTO.getAnswerWeight().getId()).orElse(null);
			}

			answerEntity.setQualitativeQuestion(qualitativeQuestions);
			answerEntity.setAnswer(answerViewDTO.getAnswer());
			answerEntity.setAnswerWeight(answerWeight);
			answerEntity.setUpdatedAt(new Date());

			qualitativeQuestionAnswerRepository.save(answerEntity);

			Set<Long> itemIds = new HashSet<>();
			qualitativeQuestions.getAnswers().stream().forEach(answer -> {
				itemIds.add(answer.getId());
			});

			if (!itemIds.contains(answerEntity.getId())) {
				qualitativeQuestions.getAnswers().add(answerEntity);
			}
		});

		// Remove Links to Item
		qualitativeQuestionRepository.save(qualitativeQuestions);
	}

	/**
	 * Deletes Qualitative Question
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		QualitativeQuestions existingItem = getQualitativeQuestion(itemId);
		QualitativeQuestionEditDTO existingItemDTO = new QualitativeQuestionEditDTO(existingItem);

		// Remove dependencies
		// qualitativeQuestionAnswerRepository.deleteByQuestionId(existingItem.getId());
		// questionBranchingLogicRepository.deleteByQuestionId(existingItem.getId());
		metricResultAnswersRepository.deleteByQuestionId(existingItem.getId());
		questionAnswersForSystemRepository.deleteByQuestionId(existingItem.getId());
		questionAnswersForVendorRepository.deleteByQuestionId(existingItem.getId());
		qualitativeQuestionRepository.deleteById(existingItem.getId());
		// qualitativeQuestionRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.QUALITATIVE_QUESTION,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, organizationService.getCurrentOrganizationId())
		);

		return itemId;
	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getDownloadData(Long riskModelId) {
		return getDownloadData(riskModelId, null, null);
	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getDownloadData(Long riskModelId, List<VendorType> scoringType, List<Long> qualitativeMetric) {

		// String templateContent = "Business Unit Name,Business Unit Description,Parent Business Unit";
		ByteArrayInputStream byteArrayInputStream = null;
		List<QualitativeQuestions> items;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = createCsvPrinter(outputStream);

			items = getQualitativeQuestionsListFiltered(riskModelId, scoringType, qualitativeMetric);
			for (QualitativeQuestions qualitativeQuestion : items) {
				List<QualitativeQuestionAnswers> answersList = qualitativeQuestion.getAnswers().stream().collect(Collectors.toList());
				answersList.sort((o1, o2) -> {
					int score1 = o1.getAnswerWeight() != null && o1.getAnswerWeight().getValue() != null ? o1.getAnswerWeight().getValue().intValue() : -1000;
					int score2 = o2.getAnswerWeight() != null && o2.getAnswerWeight().getValue() != null ? o2.getAnswerWeight().getValue().intValue() : -1000;

					return  score1 - score2;
				});

				String codePattern = "Q:{0}:{1}";
				if (VendorType.System.equals(qualitativeQuestion.getVendorType())) codePattern = "QSYS:{0}:{1}";
				if (VendorType.Vendor.equals(qualitativeQuestion.getVendorType())) codePattern = "QVND:{0}:{1}";
				if (VendorType.VendorInternal.equals(qualitativeQuestion.getVendorType())) codePattern = "QVND:{0}:{1}";
				if (VendorType.Cloud.equals(qualitativeQuestion.getVendorType())) codePattern = "QCLD:{0}:{1}";
				if (VendorType.CloudInternal.equals(qualitativeQuestion.getVendorType())) codePattern = "QCLD:{0}:{1}";

				QualitativeQuestionAnswers firstAnswer = answersList.size() > 0 ? answersList.get(0) : null;
				csvPrinter.printRecord(
					StringUtils.isNotEmpty(qualitativeQuestion.getCode()) ? qualitativeQuestion.getCode() : (MessageFormat.format(codePattern, riskModelId, qualitativeQuestion.getId()))
					, qualitativeQuestion.getQuestion()
					, qualitativeQuestion.getQuestionWeight() != null ? qualitativeQuestion.getQuestionWeight().getValue() : ""
					, firstAnswer != null ? firstAnswer.getAnswer() : ""
					, firstAnswer != null && firstAnswer.getAnswerWeight() != null ? firstAnswer.getAnswerWeight().getValue() : ""
					, qualitativeQuestion.getVendorType() != null ? qualitativeQuestion.getVendorType().name() : VendorType.Both
					, qualitativeQuestion.getQualitativeMetric().getMetricDomain() != null ? qualitativeQuestion.getQualitativeMetric().getMetricDomain().getName() : ""
					, qualitativeQuestion.getOrdinal() != null ? qualitativeQuestion.getOrdinal() : ""
					, qualitativeQuestion.getAllVendorsSelected() != null && qualitativeQuestion.getAllVendorsSelected() ? "YES" : "NO"
					, qualitativeQuestion.getAllowMultipleAnswers() != null && qualitativeQuestion.getAllowMultipleAnswers() ? "YES" : "NO"
					, qualitativeQuestion.getDescription()
					, getQuestionBranchingLogicString(qualitativeQuestion)
					, qualitativeQuestion.getCategoryName()
					, qualitativeQuestion.getVendorType() != null && (qualitativeQuestion.getVendorType().equals(VendorType.VendorInternal) || qualitativeQuestion.getVendorType().equals(VendorType.CloudInternal)) ? "YES" : "NO"
					, qualitativeQuestion.getAllowUploadAsAnswer() != null && qualitativeQuestion.getAllowUploadAsAnswer() ? "YES" : "NO"
					, qualitativeQuestion.getAllowTextAsAnswer() != null && qualitativeQuestion.getAllowTextAsAnswer() ? "YES" : "NO"
					, qualitativeQuestion.getIsTechnologyVendor() != null && qualitativeQuestion.getIsTechnologyVendor() ? "YES" : "NO"
					, qualitativeQuestion.getIsSystemVendor() != null && qualitativeQuestion.getIsSystemVendor() ? "YES" : "NO"
					, qualitativeQuestion.getIsServiceVendor() != null && qualitativeQuestion.getIsServiceVendor() ? "YES" : "NO"
					, qualitativeQuestion.getAllowCommentToAnswer() != null && qualitativeQuestion.getAllowCommentToAnswer() ? "YES" : "NO"
					, Boolean.TRUE.equals(qualitativeQuestion.getUseColorCoding()) ? "YES" : "NO"
				);

				// Apply Question answers
				if (answersList.size() > 1) {
					for (int i = 1; i < answersList.size(); i++) {
						QualitativeQuestionAnswers currentAnswer = answersList.get(i);
						csvPrinter.printRecord(
							"",
							"",
							"",
							currentAnswer != null ? currentAnswer.getAnswer() : "",
							currentAnswer != null && currentAnswer.getAnswerWeight() != null ? currentAnswer.getAnswerWeight().getValue() : "",
							"",
							"",
							"",
							"",
							""
						);
					}
				}
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException("Failed to generate CSV Data file");
		}

		return byteArrayInputStream;
	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream generateXLSXReport(Long riskModelId, String qualitativeMetricCode) {

		// String templateContent = "Business Unit Name,Business Unit Description,Parent Business Unit";
		ByteArrayInputStream byteArrayInputStream = null;
		List<QualitativeQuestions> items;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			// CSVPrinter csvPrinter = createCsvPrinter(outputStream);

			List<VendorType> scoringTypes = Arrays.asList(VendorType.Organization);
			List<Long> qualitativeMetric = new ArrayList<>();
			if (!"all".equalsIgnoreCase(qualitativeMetricCode)) {
				List<QualMetrics> qualitativeMetrics = qualMetricsRepository.getOneByRiskModelAndMetricDomainCode(riskModelId, qualitativeMetricCode);
				if (CollectionUtils.isNotEmpty(qualitativeMetrics)) {
					qualitativeMetric = qualitativeMetrics.stream().map(QualMetrics::getId).toList();
				}
			}

			// Get Items Map and Questions
			items = getQualitativeQuestionsListFiltered(riskModelId, scoringTypes, qualitativeMetric);
			List<Long> allQuestionIds = items.stream().map(QualitativeQuestions::getId).toList();
			List<QuestionAnswersForVendor> allVendorQuestionsAnswersList = questionAnswersForVendorRepository.getListByRiskModelAndQuestions(riskModelId, allQuestionIds).stream().toList();
			Map<Long, List<QuestionAnswersForVendor>> questionAnswersMap = allVendorQuestionsAnswersList.stream().collect(Collectors.groupingBy(questionAnswersForVendor -> questionAnswersForVendor.getQuestion().getId()));

			Map<MetricDomains, List<QualitativeQuestions>> metricQuestionsMap = items.stream().collect(Collectors.groupingBy(qualitativeQuestions -> qualitativeQuestions.getQualitativeMetric().getMetricDomain()));
			Map<MetricDomains, Set<String>> questionCategoriesMap = new HashMap<>();
			for (List<QualitativeQuestions> questionsList : metricQuestionsMap.values()) {
				questionsList.sort((o1, o2) -> {
					Long ordinal1 = (o1.getOrdinal() != null ? o1.getOrdinal() : 100000L);
					Long ordinal2 = (o2.getOrdinal() != null ? o2.getOrdinal() : 100000L);

					return ordinal1.compareTo(ordinal2);
				});

				final Map<String, Long> categoryIndexMap = new HashMap<>();
				long currentIndex = 0L;
				for (QualitativeQuestions question : questionsList) {
					String categoryName = question.getCategoryName() != null ? question.getCategoryName() : "";
					if (!categoryIndexMap.containsKey(categoryName)) {
						categoryIndexMap.put(categoryName, currentIndex++);
					}
				}

				questionsList.sort((o1, o2) -> {
					String categoryName1 = (o1.getCategoryName() != null ? o1.getCategoryName() : "");
					String categoryName2 = (o2.getCategoryName() != null ? o2.getCategoryName() : "");

					Long ordinal1 = (o1.getOrdinal() != null ? o1.getOrdinal() : 100000L);
					Long ordinal2 = (o2.getOrdinal() != null ? o2.getOrdinal() : 100000L);

					Long categoryOrder1 = categoryIndexMap.getOrDefault(categoryName1, 0L);
					Long categoryOrder2 = categoryIndexMap.getOrDefault(categoryName2, 0L);

					int categoryCompare = categoryOrder1.compareTo(categoryOrder2);
					return (categoryCompare != 0) ? categoryCompare : ordinal1.compareTo(ordinal2);
				});
			}

			// Create a Workbook
			XSSFWorkbook workbook = new XSSFWorkbook();

			Cell cell;

			Font headerFont = defineWorkbookFont(workbook, "Calibri", 13, true, IndexedColors.BLACK1);
			Font headerCategoryFont = defineWorkbookFont(workbook, "Calibri", 12, true, IndexedColors.WHITE1);
			Font cellFont = defineWorkbookFont(workbook, "Calibri", 11, false, IndexedColors.BLACK1);
			Font cellFontCenter = defineWorkbookFont(workbook, "Calibri", 11, false, IndexedColors.BLACK);
			Font cellFontRight = defineWorkbookFont(workbook, "Calibri", 11, false, IndexedColors.BLACK);

			CellStyle headerCellStyle = defineWorkbookCellStyle(workbook, headerFont, HorizontalAlignment.CENTER, true);
			headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			CellStyle headerCategoryCellStyle = defineWorkbookCellStyle(workbook, headerCategoryFont, HorizontalAlignment.LEFT, true);
			headerCategoryCellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
			headerCategoryCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerCategoryCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			headerCategoryCellStyle.setIndention((short) 1);
			CellStyle bodyCellStyle = defineWorkbookCellStyle(workbook, cellFont, null, false);
			CellStyle bodyCellStyleCenter = defineWorkbookCellStyle(workbook, cellFontCenter, HorizontalAlignment.CENTER, true);
			CellStyle bodyCellStyleRight = defineWorkbookCellStyle(workbook, cellFontRight, HorizontalAlignment.RIGHT, false);

			// CreationHelper helps us create instances of various things like DataFormat,
			// Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way
			CreationHelper createHelper = workbook.getCreationHelper();

			for (Map.Entry<MetricDomains, List<QualitativeQuestions>> metricQuestionsEntry : metricQuestionsMap.entrySet()) {
				MetricDomains metricDomain = metricQuestionsEntry.getKey();
				List<QualitativeQuestions> qualitativeQuestions = metricQuestionsEntry.getValue();

				Set<QualitativeQuestions> categoriesSet = qualitativeQuestions.stream().filter(question -> StringUtils.isNotEmpty(question.getCategoryName())).collect(Collectors.toSet());
				Boolean isCategorised = CollectionUtils.isNotEmpty(categoriesSet);

				// Create a Sheet
				XSSFSheet sheet = workbook.createSheet(metricDomain.getName());
				int currentRow = 0;

				// Set Column Width
				sheet.setColumnWidth(0, 20 * 256);
				sheet.setColumnWidth(1, 80 * 256);
				sheet.setColumnWidth(2, 60 * 256);
				sheet.setColumnWidth(3, 40 * 256);
				sheet.setColumnWidth(4, 20 * 256);
				sheet.setColumnWidth(5, 20 * 256);
				sheet.setColumnWidth(6, 20 * 256);

				// Create a Row
				String cellValue = "Code";
				Row headerRow01 = sheet.createRow(currentRow++);
				headerRow01.setHeight((short) 600);
				// log.info("Height {}", headerRow01.getHeight());

				// Create Header for First Sheet
				cell = buildRowCell(headerRow01, 0, "Code", headerCellStyle);
				cell = buildRowCell(headerRow01, 1, "Question", headerCellStyle);
				cell = buildRowCell(headerRow01, 2, "Answer", headerCellStyle);
				cell = buildRowCell(headerRow01, 3, "Comment", headerCellStyle);

				// Freeze first row
				sheet.createFreezePane(0, 1);

				String currentCategoryName = "";
				for (QualitativeQuestions qualitativeQuestion : qualitativeQuestions) {

					// Category Name Row
					String questionCategoryName = qualitativeQuestion.getCategoryName() != null ? qualitativeQuestion.getCategoryName() : "";
					if (isCategorised && !currentCategoryName.equals(qualitativeQuestion.getCategoryName())) {
						currentCategoryName = questionCategoryName;

						// 656565
						Row categoryRow = sheet.createRow(currentRow);
						categoryRow.setHeight((short) 600);
						cell = buildRowCell(categoryRow, 0, currentCategoryName, headerCategoryCellStyle);
						sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 0, 4));
						currentRow++;
					}

					List<QualitativeQuestionAnswers> answersList = new ArrayList<>(qualitativeQuestion.getAnswers());
					answersList.sort((o1, o2) -> {
						int score1 = o1.getAnswerWeight() != null && o1.getAnswerWeight().getValue() != null ? o1.getAnswerWeight().getValue().intValue() : -1000;
						int score2 = o2.getAnswerWeight() != null && o2.getAnswerWeight().getValue() != null ? o2.getAnswerWeight().getValue().intValue() : -1000;

						return  score1 - score2;
					});
					List<String> answerList = answersList.stream().map(QualitativeQuestionAnswers::getAnswer).toList();

					Boolean isAnswerAsText = false;
					String answerText = "";
					String answerComment = "";
					if (questionAnswersMap.containsKey(qualitativeQuestion.getId())) {
						List<QuestionAnswersForVendor> questionAnswers = questionAnswersMap.get(qualitativeQuestion.getId());
						for (QuestionAnswersForVendor questionAnswersForVendor: questionAnswers) {
							if (questionAnswersForVendor.getAnswer() != null) {
								answerText = questionAnswersForVendor.getAnswer().getAnswer();
							} else if (Boolean.TRUE.equals(qualitativeQuestion.getAllowTextAsAnswer()) && StringUtils.isNotEmpty(questionAnswersForVendor.getAnswerText())) {
								answerText = questionAnswersForVendor.getAnswerText();
								isAnswerAsText = true;
							}

							if (Boolean.TRUE.equals(qualitativeQuestion.getAllowCommentToAnswer()) && StringUtils.isNotEmpty(questionAnswersForVendor.getAnswerComment())) {
								answerComment = questionAnswersForVendor.getAnswerComment();
							}
						}
					}

					Row questionRow = sheet.createRow(currentRow);
					cell = buildRowCell(questionRow, 0, qualitativeQuestion.getCode(), bodyCellStyle);
					cell = buildRowCell(questionRow, 1, qualitativeQuestion.getQuestion(), bodyCellStyle);
					cell = buildRowCell(questionRow, 2, answerText, isAnswerAsText ? bodyCellStyle : bodyCellStyleRight);
					cell = buildRowCell(questionRow, 3, answerComment, bodyCellStyleRight);

					DataValidation dataValidation = null;
					DataValidationConstraint constraint = null;
					DataValidationHelper validationHelper = null;

					if (CollectionUtils.isNotEmpty(answerList)) {
						String[] answers = answerList.toArray(new String[0]);

						validationHelper = new XSSFDataValidationHelper(sheet);
						CellRangeAddressList addressList = new CellRangeAddressList(currentRow, currentRow, 2, 2);
						constraint = validationHelper.createExplicitListConstraint(answers);
						dataValidation = validationHelper.createValidation(constraint, addressList);
						dataValidation.setSuppressDropDownArrow(true);
						sheet.addValidationData(dataValidation);

						if (Boolean.TRUE.equals(qualitativeQuestion.getUseColorCoding())) {
							List<ConditionalFormattingRule> ruleList = new ArrayList<>();
							for (QualitativeQuestionAnswers questionAnswer: answersList) {
								if (questionAnswer.getAnswerWeight() != null) {
									ConditionalFormattingRule rule = sheet.getSheetConditionalFormatting().createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"" + questionAnswer.getAnswer() + "\"");
									FontFormatting fontFmt = rule.createFontFormatting();
									fontFmt.setFontStyle(true, false);

									int scaledWeight = questionAnswer.getAnswerWeight().getValue().intValue();
									if (scaledWeight > 8) {
										// Most
										fontFmt.setFontColorIndex(IndexedColors.DARK_RED.index);
									} else if (scaledWeight > 6) {
										// Significant
										fontFmt.setFontColorIndex(IndexedColors.ORANGE.index);
									} else if (scaledWeight > 4) {
										// moderate
										fontFmt.setFontColorIndex(IndexedColors.YELLOW.index);
									} else if (scaledWeight > 2) {
										// Least
										fontFmt.setFontColorIndex(IndexedColors.LIGHT_GREEN.index);
									} else {
										fontFmt.setFontColorIndex(IndexedColors.GREEN.index);
									}

									ruleList.add(rule);
								}
							}

							if (CollectionUtils.isNotEmpty(ruleList)) {
								ConditionalFormattingRule[] rulesArray = ruleList.toArray(new ConditionalFormattingRule[0]);
								CellRangeAddress cellAddress = new CellRangeAddress(currentRow, currentRow, 2, 2);
								CellRangeAddress[] regions = {cellAddress};
								sheet.getSheetConditionalFormatting().addConditionalFormatting(regions, rulesArray);
							}
						}
					}

					currentRow++;
				}
			}

			/*
			for (QualitativeQuestions qualitativeQuestion : items) {
				List<QualitativeQuestionAnswers> answersList = qualitativeQuestion.getAnswers().stream().collect(Collectors.toList());
				answersList.sort((o1, o2) -> {
					int score1 = o1.getAnswerWeight() != null && o1.getAnswerWeight().getValue() != null ? o1.getAnswerWeight().getValue().intValue() : -1000;
					int score2 = o2.getAnswerWeight() != null && o2.getAnswerWeight().getValue() != null ? o2.getAnswerWeight().getValue().intValue() : -1000;

					return  score1 - score2;
				});

				String codePattern = "Q:{0}:{1}";
				if (VendorType.System.equals(qualitativeQuestion.getVendorType())) codePattern = "QSYS:{0}:{1}";
				if (VendorType.Vendor.equals(qualitativeQuestion.getVendorType())) codePattern = "QVND:{0}:{1}";
				if (VendorType.VendorInternal.equals(qualitativeQuestion.getVendorType())) codePattern = "QVND:{0}:{1}";
				if (VendorType.Cloud.equals(qualitativeQuestion.getVendorType())) codePattern = "QCLD:{0}:{1}";
				if (VendorType.CloudInternal.equals(qualitativeQuestion.getVendorType())) codePattern = "QCLD:{0}:{1}";

				QualitativeQuestionAnswers firstAnswer = answersList.size() > 0 ? answersList.get(0) : null;
				csvPrinter.printRecord(
					StringUtils.isNotEmpty(qualitativeQuestion.getCode()) ? qualitativeQuestion.getCode() : (MessageFormat.format(codePattern, riskModelId, qualitativeQuestion.getId()))
					, qualitativeQuestion.getQuestion()
					, qualitativeQuestion.getQuestionWeight() != null ? qualitativeQuestion.getQuestionWeight().getValue() : ""
					, firstAnswer != null ? firstAnswer.getAnswer() : ""
					, firstAnswer != null && firstAnswer.getAnswerWeight() != null ? firstAnswer.getAnswerWeight().getValue() : ""
					, qualitativeQuestion.getVendorType() != null ? qualitativeQuestion.getVendorType().name() : VendorType.Both
					, qualitativeQuestion.getQualitativeMetric().getMetricDomain() != null ? qualitativeQuestion.getQualitativeMetric().getMetricDomain().getName() : ""
					, qualitativeQuestion.getOrdinal() != null ? qualitativeQuestion.getOrdinal() : ""
					, qualitativeQuestion.getAllVendorsSelected() != null && qualitativeQuestion.getAllVendorsSelected() ? "YES" : "NO"
					, qualitativeQuestion.getDescription()
					, getQuestionBranchingLogicString(qualitativeQuestion)
					, qualitativeQuestion.getCategoryName()
					, qualitativeQuestion.getVendorType() != null && (qualitativeQuestion.getVendorType().equals(VendorType.VendorInternal) || qualitativeQuestion.getVendorType().equals(VendorType.CloudInternal)) ? "YES" : "NO"
					, qualitativeQuestion.getAllowUploadAsAnswer() != null && qualitativeQuestion.getAllowUploadAsAnswer() ? "YES" : "NO"
					, qualitativeQuestion.getAllowTextAsAnswer() != null && qualitativeQuestion.getAllowTextAsAnswer() ? "YES" : "NO"
					, qualitativeQuestion.getIsTechnologyVendor() != null && qualitativeQuestion.getIsTechnologyVendor() ? "YES" : "NO"
					, qualitativeQuestion.getIsSystemVendor() != null && qualitativeQuestion.getIsSystemVendor() ? "YES" : "NO"
					, qualitativeQuestion.getIsServiceVendor() != null && qualitativeQuestion.getIsServiceVendor() ? "YES" : "NO"
					, qualitativeQuestion.getAllowCommentToAnswer() != null && qualitativeQuestion.getAllowCommentToAnswer() ? "YES" : "NO"
					, Boolean.TRUE.equals(qualitativeQuestion.getUseColorCoding()) ? "YES" : "NO"
				);

				// Apply Question answers
				if (answersList.size() > 1) {
					for (int i = 1; i < answersList.size(); i++) {
						QualitativeQuestionAnswers currentAnswer = answersList.get(i);
						csvPrinter.printRecord(
							"",
							"",
							"",
							currentAnswer != null ? currentAnswer.getAnswer() : "",
							currentAnswer != null && currentAnswer.getAnswerWeight() != null ? currentAnswer.getAnswerWeight().getValue() : "",
							"",
							"",
							"",
							"",
							""
						);
					}
				}
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
			*/

			workbook.write(outputStream);

			// Closing the workbook
			workbook.close();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException("Failed to generate CSV Data file");
		}

		return byteArrayInputStream;
	}

	private static Cell buildRowCell(Row row, int index, String cellValue, CellStyle headerCellStyle) {
		Cell cell = row.createCell(index);
		cell.setCellValue(cellValue);
		cell.setCellStyle(headerCellStyle);

		return cell;
	}

	@NotNull
	private static CellStyle defineWorkbookCellStyle(Workbook workbook, Font headerFont, HorizontalAlignment alignment, boolean isWrapped) {
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		if (alignment != null) headerCellStyle.setAlignment(alignment);
		if (isWrapped) headerCellStyle.setWrapText(true);
		// headerCellStyle.setIndention((short) 2);
		return headerCellStyle;
	}

	@NotNull
	private static Font defineWorkbookFont(Workbook workbook, String fontName, Integer fontHeightInPoints, boolean isBold, IndexedColors fontColor) {
		Font headerFont = workbook.createFont();

		if (isBold) headerFont.setBold(true);
		headerFont.setFontName(fontName);
		if (fontHeightInPoints != null) headerFont.setFontHeightInPoints(fontHeightInPoints.shortValue());
		if (fontColor != null) headerFont.setColor(fontColor.getIndex());

		return headerFont;
	}

	private List<QualitativeQuestions> getQualitativeQuestionsListFiltered(Long riskModelId, List<VendorType> scoringType, List<Long> qualitativeMetric) {
		List<QualitativeQuestions> items;
		if (CollectionUtils.isNotEmpty(scoringType) && CollectionUtils.isNotEmpty(qualitativeMetric)) {
			items = qualitativeQuestionRepository.getListByRiskModelIdAndTypeAndMetric(riskModelId, qualitativeMetric, scoringType);
		} else if (CollectionUtils.isNotEmpty(scoringType)) {
			items = qualitativeQuestionRepository.getListByRiskModelIdAndType(riskModelId, scoringType);
		} else if (CollectionUtils.isNotEmpty(qualitativeMetric)) {
			items = qualitativeQuestionRepository.getListByRiskModelIdAndMetric(riskModelId, qualitativeMetric);
		} else {
			items = qualitativeQuestionRepository.getListByRiskModelId(riskModelId);
		}
		return items;
	}

	/**
	 * Get content for Download
	 */
	public ByteArrayInputStream getAnswersDownloadData(Long riskModelId, List<VendorType> scoringType, List<Long> qualitativeMetric) {

		// String templateContent = "Business Unit Name,Business Unit Description,Parent Business Unit";
		ByteArrayInputStream byteArrayInputStream = null;
		List<QualitativeQuestions> items;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			Writer writer = new OutputStreamWriter(outputStream);
			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
				QUESTION_ID_HEADER
				, QUESTION_NAME_HEADER
				, QUESTION_ANSWER_HEADER
				, ANSWER_TYPE_HEADER
				, ANSWER_LINK_HEADER
				, ANSWER_TEXT_HEADER
				, ANSWER_COMMENT_HEADER
			);

			CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

			items = getQualitativeQuestionsListFiltered(riskModelId, scoringType, qualitativeMetric);
			List<Long> questionIds = items.stream().map(QualitativeQuestions::getId).collect(Collectors.toList());

			List<QuestionAnswersForVendor> vendorAnswers = questionAnswersForVendorRepository.getListByRiskModelAndQuestions(riskModelId, questionIds);
			List<QuestionAnswersForSystem> systemAnswers = questionAnswersForSystemRepository.getListByRiskModelAndQuestions(riskModelId, questionIds);

			Map<Long, List<QuestionAnswersForVendor>> vendorAnswersMap = vendorAnswers.stream().collect(Collectors.groupingBy(questionAnswersForVendor -> questionAnswersForVendor.getQuestion().getId()));
			Map<Long, List<QuestionAnswersForSystem>> systemAnswersMap = systemAnswers.stream().collect(Collectors.groupingBy(questionAnswersForSystem -> questionAnswersForSystem.getQuestion().getId()));

			for (QualitativeQuestions qualitativeQuestion : items) {
				// Vendor Answers flow
				if (vendorAnswersMap.containsKey(qualitativeQuestion.getId())) {
					for (QuestionAnswersForVendor answerDetails : vendorAnswersMap.get(qualitativeQuestion.getId())) {
						// Ignore empty records on Export
						if (answerDetails.getAnswer() == null && StringUtils.isEmpty(answerDetails.getAnswerText()) && StringUtils.isEmpty(answerDetails.getAnswerComment())) {
							continue;
						}

						csvPrinter.printRecord(
							(StringUtils.isNotEmpty(qualitativeQuestion.getCode()) ? qualitativeQuestion.getCode() : null)
							, qualitativeQuestion.getQuestion()
							, (answerDetails.getAnswer() != null ? answerDetails.getAnswer().getAnswer() : null)
							, qualitativeQuestion.getVendorType().name()
							, (answerDetails.getVendor() != null ? answerDetails.getVendor().getName() : null)
							, answerDetails.getAnswerText()
							, answerDetails.getAnswerComment()
						);
					}
				}

				// System Answers flow
				if (systemAnswersMap.containsKey(qualitativeQuestion.getId())) {
					for (QuestionAnswersForSystem answerDetails : systemAnswersMap.get(qualitativeQuestion.getId())) {
						csvPrinter.printRecord(
							(StringUtils.isNotEmpty(qualitativeQuestion.getCode()) ? qualitativeQuestion.getCode() : null)
							, qualitativeQuestion.getQuestion()
							, (answerDetails.getAnswer() != null ? answerDetails.getAnswer().getAnswer() : null)
							, qualitativeQuestion.getVendorType().name()
							, (answerDetails.getSystem() != null ? answerDetails.getSystem().getName() : null)
							, answerDetails.getAnswerText()
							, answerDetails.getAnswerComment()
						);
					}
				}
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException("Failed to generate CSV Data file");
		}

		return byteArrayInputStream;
	}


	/**
	 * Queue all KeenIO question answers
	 */
	public ByteArrayInputStream buildQuestionAnswersReport(Long questionId) {
		Long organizationId = organizationService.getCurrentOrganizationId();

		QualitativeQuestions questionDetails = getQualitativeQuestion(questionId);

		List<QuestionAnswersForVendor> vendorAnswers = questionAnswersForVendorRepository.getListByRiskModelAndQuestions(questionDetails.getRiskModelId(), Arrays.asList(questionId));
		List<QuestionAnswersForSystem> systemAnswers = questionAnswersForSystemRepository.getListByRiskModelAndQuestions(questionDetails.getRiskModelId(), Arrays.asList(questionId));

		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			List<String> headersList = new ArrayList<>(Arrays.asList(QUESTION_ID_HEADER, QUESTION_NAME_HEADER, QUESTION_WEIGHT_HEADER, QUESTION_ANSWER_HEADER, QUESTION_ANSWER_WEIGHT_HEADER));
			boolean isSystemAnswer = systemAnswers != null && systemAnswers.size() > 0;
			boolean isVendorAnswer = vendorAnswers != null && vendorAnswers.size() > 0;
			if (isVendorAnswer) {
				headersList.add("Vendor Id");
				headersList.add("Vendor Name");
			}
			if (isSystemAnswer) {
				headersList.add("System Id");
				headersList.add("System Name");
				headersList.add("Associate Vendor");
			}
			String[] headers = headersList.toArray(new String[0]);

			Writer writer = new OutputStreamWriter(outputStream);
			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headers);
			CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

			if (isVendorAnswer) {
				for (QuestionAnswersForVendor answer : vendorAnswers) {
					List<String> answerFieldsList = new ArrayList<>(Arrays.asList(
						answer.getQuestion().getCode()
						, answer.getQuestion().getQuestion()
						, (answer.getQuestion().getQuestionWeight() != null ? answer.getQuestion().getQuestionWeight().getValue().toString() : "")
						, (answer.getAnswer() != null ? answer.getAnswer().getAnswer() : "")
						, (answer.getAnswer() != null && answer.getAnswer().getAnswerWeight() != null ? answer.getAnswer().getAnswerWeight().getValue().toString() : "")
					));

					if (answer.getVendor() != null) {
						answerFieldsList.add(answer.getVendor().getId().toString());
						answerFieldsList.add(answer.getVendor().getName());
					}

					csvPrinter.printRecord(answerFieldsList.toArray(new String[0]));
				}
			}

			if (isSystemAnswer) {

				List<Long> systemIds = systemAnswers.stream().filter(answer -> answer.getSystem() != null).map(answer -> answer.getSystem().getId()).collect(Collectors.toList());
				List<AssociateVendors> associateVendorsList = associateVendorRepository.getAssociateVendorsListForSystemsList(systemIds);
				Map<Long, List<Organizations>> systemVendorsMap = new HashMap<>();
				for (AssociateVendors associateVendor : associateVendorsList) {
					if (associateVendor.getSystems() != null && associateVendor.getVendor() != null) {
						for (Systems system : associateVendor.getSystems()) {
							if (!systemVendorsMap.containsKey(system.getId())) systemVendorsMap.put(system.getId(), new ArrayList<>());

							systemVendorsMap.get(system.getId()).add(associateVendor.getVendor());
						}
					}
				}

				for (QuestionAnswersForSystem answer : systemAnswers) {

					Systems system = answer.getSystem();
					if (answer.getSystem() == null) continue;

					List<String> answerFieldsList = new ArrayList<>(Arrays.asList(
						answer.getQuestion().getId().toString()
						, answer.getQuestion().getQuestion()
						, (answer.getQuestion().getQuestionWeight() != null ? answer.getQuestion().getQuestionWeight().getValue().toString() : "")
						, (answer.getAnswer() != null ? answer.getAnswer().getAnswer() : "")
						, (answer.getAnswer() != null && answer.getAnswer().getAnswerWeight() != null ? answer.getAnswer().getAnswerWeight().getValue().toString() : "")
					));

					if (isVendorAnswer) {
						answerFieldsList.add("");
						answerFieldsList.add("");
					}

					answerFieldsList.add(system.getId().toString());
					answerFieldsList.add(system.getName());

					if (systemVendorsMap.containsKey(system.getId())) {
						List<Organizations> associateVendors = systemVendorsMap.get(system.getId());
						for (int i = 0; i < associateVendors.size(); i++) {

							String[] fieldsArray = answerFieldsList.toArray(new String[0]);
							ArrayList<String> currentAnswerFieldsList = new ArrayList<>(Arrays.asList(fieldsArray));
							currentAnswerFieldsList.add(associateVendors.get(i).getName());
							csvPrinter.printRecord(currentAnswerFieldsList.toArray(new String[0]));
						}
					} else {
						csvPrinter.printRecord(answerFieldsList.toArray(new String[0]));
					}
				}
			}

			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException("Failed to generate CSV Data file");
		}

		return byteArrayInputStream;
	}

	/**
	 * Find question by parameters
	 *
	 * @param riskModelId
	 * @param question
	 * @param code
	 * @return
	 */
	public Optional<QualitativeQuestions> findQuestion(Long riskModelId, String question, String code) {
		Optional<QualitativeQuestions> result = Optional.empty();

		if (StringUtils.isNotEmpty(code)) {
			result = qualitativeQuestionRepository.findFirstByRiskModelIdAndCode(riskModelId, code);
		} else if (StringUtils.isNotEmpty(question)) {
			result = qualitativeQuestionRepository.findFirstByRiskModelIdAndQuestion(riskModelId, question);
		}

		return result;
	}

	/**
	 * Insert business unit data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(Long riskModelId, InputStream fileContentStream) {

		ImportResultDTO result = new ImportResultDTO();
		RiskModels riskModel = riskModelService.getRiskModel(riskModelId);
		Organizations organization = organizationService.getCurrentOrganizationEntity();

		try {

			List<QuestionWeightDTO> questionWeightList = questionWeightService.getList();
			Map<Long, QuestionWeightDTO> questionWeightMap = questionWeightList.stream().collect(Collectors.toMap(QuestionWeightDTO::getValue, questionWeight -> questionWeight));
			List<AnswerWeightDTO> answerWeightList = answerWeightService.getList();
			Map<Long, AnswerWeightDTO> answerWeightMap = answerWeightList.stream().collect(Collectors.toMap(AnswerWeightDTO::getValue, answerWeight -> answerWeight));

			Optional<QualitativeQuestions> lastQuestion = null;
			QualitativeQuestionEditDTO lastQuestionDTO = null;
			List<QualitativeQuestionEditDTO> itemsToSave = new ArrayList<>();

			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			for (CSVRecord csvRecord : csvRecordList) {

				if (!csvRecord.isMapped(QUESTION_NAME_HEADER)) throw new BadRequestException("Missing required column: " + QUESTION_NAME_HEADER);
				if (!csvRecord.isMapped(QUESTION_ANSWER_HEADER)) throw new BadRequestException("Missing required column: " + QUESTION_ANSWER_HEADER);
				if (!csvRecord.isMapped(QUESTION_ANSWER_WEIGHT_HEADER)) throw new BadRequestException("Missing required column: " + QUESTION_ANSWER_WEIGHT_HEADER);
				if (!csvRecord.isMapped(QUESTION_QUAL_METRIC_HEADER)) throw new BadRequestException("Missing required column: " + QUESTION_QUAL_METRIC_HEADER);

				// Accessing values by Header names
				String code = csvRecord.isSet(QUESTION_ID_HEADER) ? csvRecord.get(QUESTION_ID_HEADER).trim() : null;
				String categoryName = csvRecord.isSet(QUESTION_CATEGORY_HEADER) ? csvRecord.get(QUESTION_CATEGORY_HEADER).trim() : null;
				String question = csvRecord.get(QUESTION_NAME_HEADER).trim();
				String answer = csvRecord.get(QUESTION_ANSWER_HEADER).trim();
				String answerWeight = csvRecord.get(QUESTION_ANSWER_WEIGHT_HEADER).trim();

				Long answerWeightLong = null;
				Long questionWeightLong = null;
				Long ordinalLong = null;
				try {
					if (StringUtils.isNotEmpty(answerWeight)) {
						answerWeightLong = Long.parseLong(answerWeight);
					}
				} catch (NumberFormatException e) {
					;
				}

				if (StringUtils.isNotEmpty(question)) {
					lastQuestion = findQuestion(riskModelId, question, code);
					if (!lastQuestion.isEmpty()) {
						lastQuestionDTO = new QualitativeQuestionEditDTO(lastQuestion.get());
						lastQuestionDTO.setAnswers(new ArrayList<>()); // RESET answers list before import
					} else {
						lastQuestionDTO = new QualitativeQuestionEditDTO();
					}

					itemsToSave.add(lastQuestionDTO);

					// Fill Question
					VendorType vendorType = csvRecord.isSet(QUESTION_VENDOR_TYPE_HEADER) ? VendorType.of(csvRecord.get(QUESTION_VENDOR_TYPE_HEADER), VendorType.Both) : VendorType.Both;
					String questionWeight = csvRecord.isSet(QUESTION_WEIGHT_HEADER) ? csvRecord.get(QUESTION_WEIGHT_HEADER).trim() : "";
					String questionDescription = csvRecord.isSet(QUESTION_DESCRIPTION_HEADER) ? csvRecord.get(QUESTION_DESCRIPTION_HEADER) : "";
					String qualMetricDomain = csvRecord.isSet(QUESTION_QUAL_METRIC_HEADER) ? csvRecord.get(QUESTION_QUAL_METRIC_HEADER).trim() : "";
					String qualMetricDomainCategory = csvRecord.isSet(QUESTION_QUAL_METRIC_CATEGORY_HEADER) ? csvRecord.get(QUESTION_QUAL_METRIC_CATEGORY_HEADER).trim() : null;
					String ordinalString = csvRecord.isSet(QUESTION_ORDINAL_HEADER) ? csvRecord.get(QUESTION_ORDINAL_HEADER).trim() : "";
					QualMetricsViewDTO qualMetricView = qualMetricsService.getOrCreateOneByRiskModelAndDomain(riskModelId, organization.getId(), qualMetricDomain, qualMetricDomainCategory);
					if (qualMetricView ==  null) {
						throw new BadRequestException("Qualitative metric domain not found for: [" + qualMetricDomain + "], Question: " + question);
					}
					try {
						if (StringUtils.isNotEmpty(questionWeight)) {
							questionWeightLong = Long.parseLong(questionWeight);
						}
					} catch (NumberFormatException e) {
						;
					}
					try {
						if (StringUtils.isNotEmpty(ordinalString)) {
							ordinalLong = Long.parseLong(ordinalString);
						}
					} catch (NumberFormatException e) {
						;
					}

					// Set defined parameters
					if (StringUtils.isNotEmpty(code)) lastQuestionDTO.setCode(code);
					if (StringUtils.isNotEmpty(categoryName)) lastQuestionDTO.setCategoryName(categoryName);
					lastQuestionDTO.setQuestion(question);
					lastQuestionDTO.setRiskModelId(riskModelId);
					lastQuestionDTO.setVendorType(vendorType);
					if (questionWeightLong != null && questionWeightMap.containsKey(questionWeightLong)) {
						lastQuestionDTO.setQuestionWeight(questionWeightMap.get(questionWeightLong));
					}
					if (ordinalLong != null) {
						lastQuestionDTO.setOrdinal(ordinalLong);
					}
					if (csvRecord.isSet(QUESTION_ALL_VENDORS_HEADER)) {
						lastQuestionDTO.setAllVendorsSelected("yes".equalsIgnoreCase(csvRecord.get(QUESTION_ALL_VENDORS_HEADER)));
					}
					if (StringUtils.isNotEmpty(questionDescription)) {
						lastQuestionDTO.setDescription(questionDescription);
					}
					lastQuestionDTO.setQualitativeMetric(qualMetricView);

					// Set Question Flags
					if (csvRecord.isSet(QUESTION_IS_INTERNAL_HEADER)) {
						// We already manage this by using CloudInternal and VendorInternal types
						// lastQuestionDTO.setIsInternal("yes".equalsIgnoreCase(csvRecord.get(QUESTION_IS_INTERNAL_HEADER)));
					}
					if (csvRecord.isSet(QUESTION_ALLOW_MULTIPLE_ANSWERS)) {
						String allowMultipleAnswers = StringUtils.trim(csvRecord.get(QUESTION_ALLOW_MULTIPLE_ANSWERS));
						if (StringUtils.isNotEmpty(allowMultipleAnswers)) {
							lastQuestionDTO.setAllowMultipleAnswers("yes".equalsIgnoreCase(allowMultipleAnswers));
						}
					}
					if (csvRecord.isSet(QUESTION_UPLOAD_DOCUMENT_HEADER)) {
						lastQuestionDTO.setAllowUploadAsAnswer("yes".equalsIgnoreCase(StringUtils.trim(csvRecord.get(QUESTION_UPLOAD_DOCUMENT_HEADER))));
					}
					if (csvRecord.isSet(QUESTION_WRITE_TEXT_HEADER)) {
						lastQuestionDTO.setAllowTextAsAnswer("yes".equalsIgnoreCase(StringUtils.trim(csvRecord.get(QUESTION_WRITE_TEXT_HEADER))));
					}
					if (csvRecord.isSet(QUESTION_ALLOW_COMMENT_HEADER)) {
						lastQuestionDTO.setAllowCommentToAnswer("yes".equalsIgnoreCase(StringUtils.trim(csvRecord.get(QUESTION_ALLOW_COMMENT_HEADER))));
					}
					if (csvRecord.isSet(QUESTION_IS_TECHNOLOGY_VENDOR_HEADER)) {
						lastQuestionDTO.setIsTechnologyVendor("yes".equalsIgnoreCase(StringUtils.trim(csvRecord.get(QUESTION_IS_TECHNOLOGY_VENDOR_HEADER))));
					}
					if (csvRecord.isSet(QUESTION_IS_SYSTEM_VENDOR_HEADER)) {
						lastQuestionDTO.setIsSystemVendor("yes".equalsIgnoreCase(StringUtils.trim(csvRecord.get(QUESTION_IS_SYSTEM_VENDOR_HEADER))));
					}
					if (csvRecord.isSet(QUESTION_IS_SERVICE_VENDOR_HEADER)) {
						lastQuestionDTO.setIsServiceVendor("yes".equalsIgnoreCase(StringUtils.trim(csvRecord.get(QUESTION_IS_SERVICE_VENDOR_HEADER))));
					}
					if (csvRecord.isSet(QUESTION_USE_COLOR_CODING)) {
						lastQuestionDTO.setUseColorCoding("yes".equalsIgnoreCase(StringUtils.trim(csvRecord.get(QUESTION_USE_COLOR_CODING))));
					}

					// Set Branching Logic
					if (csvRecord.isSet(QUESTION_BRANCHING_LOGIC_HEADER)) {
						String branchingLogicString = StringUtils.trim(csvRecord.get(QUESTION_BRANCHING_LOGIC_HEADER));
						if (StringUtils.isNotEmpty(branchingLogicString)) {
							lastQuestionDTO.setBranchingLogicString(branchingLogicString);
						}
					}

					// Set GDPR Linkages
					if (csvRecord.isSet(QUESTION_GDPR_LINKAGE_HEADER)) {
						lastQuestionDTO.setGdprString(csvRecord.get(QUESTION_GDPR_LINKAGE_HEADER));
					}
				}

				// Process Answer
				if (lastQuestionDTO != null && StringUtils.isNotEmpty(answer)) {
					AnswerViewDTO answerViewDTO;
					List<QualitativeQuestionAnswers> existingAnswer = new ArrayList<>();
					if (lastQuestionDTO.getId() != null) {
						existingAnswer = qualitativeQuestionAnswerRepository.getByQuestionAndAnswer(lastQuestionDTO.getId(), answer);
						// Some Answers May have the same number
						// if (existingAnswer.isEmpty() && answerWeightLong != null) existingAnswer = qualitativeQuestionAnswerRepository.getByQuestionAndAnswerWeight(lastQuestionDTO.getId(), answerWeightLong);
					}

					if (!existingAnswer.isEmpty()) {
						answerViewDTO = new AnswerViewDTO(existingAnswer.get(0));
					} else {
						answerViewDTO = new AnswerViewDTO();
					}

					answerViewDTO.setAnswer(answer);
					if (answerWeightMap.containsKey(answerWeightLong)) {
						answerViewDTO.setAnswerWeight(answerWeightMap.get(answerWeightLong));
					} else {
						answerViewDTO.setAnswerWeight(answerWeightMap.get(0L));
					}

					lastQuestionDTO.getAnswers().add(answerViewDTO);
				}
			}

			// Saving data to the database
			Map<Long, String> branchingLogicMap = new HashMap<>();
			Map<Long, String> gdprQuestionMap = new HashMap<>();
			for (QualitativeQuestionEditDTO qualitativeQuestionEditDTO : itemsToSave) {
				QualitativeQuestionEditDTO savedItem;
				if (qualitativeQuestionEditDTO.getId() != null) {
					savedItem = update(qualitativeQuestionEditDTO);
					result.getUpdated().add(new ItemViewDTO(savedItem.getId(), "QUESTION: " + savedItem.getQuestion()));
				} else {
					savedItem = create(qualitativeQuestionEditDTO);
					result.getCreated().add(new ItemViewDTO(savedItem.getId(), "QUESTION: " + savedItem.getQuestion()));
				}

				// Save Branching Logic position and Formula to update after all items have been saved.
				if (StringUtils.isNotEmpty(qualitativeQuestionEditDTO.getBranchingLogicString())) {
					branchingLogicMap.put(savedItem.getId(), qualitativeQuestionEditDTO.getBranchingLogicString());
				}

				// Save GDPR Question Logic mapping to update after all items have been saved.
				if (StringUtils.isNotEmpty(qualitativeQuestionEditDTO.getGdprString())) {
					gdprQuestionMap.put(savedItem.getId(), qualitativeQuestionEditDTO.getGdprString());
				}

				if (qualitativeQuestionEditDTO.getAnswers() != null && qualitativeQuestionEditDTO.getAnswers().size() > 0) {
					for (AnswerViewDTO answerViewDTO : qualitativeQuestionEditDTO.getAnswers()) {
						if (answerViewDTO.getId() != null) {
							result.getUpdated().add(new ItemViewDTO(answerViewDTO.getId(), "ANSWER: " + answerViewDTO.getAnswer() + ", WEIGHT: " + (answerViewDTO.getAnswerWeight() != null ? answerViewDTO.getAnswerWeight().getValue() : "-")));
						} else {
							result.getCreated().add(new ItemViewDTO(null, "ANSWER: " + answerViewDTO.getAnswer() + ", WEIGHT: " + (answerViewDTO.getAnswerWeight() != null ? answerViewDTO.getAnswerWeight().getValue() : "-")));
						}
					}
				}
			}

			// Updating all Outstanding Branching Logic
			for (Map.Entry<Long, String> entrySet : branchingLogicMap.entrySet()) {
				updateBranchingLogicFromString(entrySet.getKey(), entrySet.getValue());
			}

			// Updating all Outstanding Branching Logic
			if (gdprQuestionMap.size() > 0) {
				updateGDPRArticleMapping(gdprQuestionMap, organization.getId());
			}

			result.setStatus("SUCCESS");

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			log.warn(e.getMessage(), e);
			throw e;
		} catch (Throwable e) {
			log.warn(e.getMessage(), e);
			throw e;
		}

		return result;
	}


	/**
	 * Insert business unit data from CSV file
	 */
	@Transactional
	public ImportResultDTO importAnswersFromCSVFile(Long riskModelId, InputStream fileContentStream) {

		ImportResultDTO result = new ImportResultDTO();
		RiskModels riskModel = riskModelService.getRiskModel(riskModelId);
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		Users currentUser = userService.getCurrentUserEntity();

		try {

			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			for (CSVRecord csvRecord : csvRecordList) {

				if (!csvRecord.isMapped(QUESTION_ID_HEADER)) throw new BadRequestException("Missing required column: " + QUESTION_ID_HEADER);
				if (!csvRecord.isMapped(QUESTION_NAME_HEADER)) throw new BadRequestException("Missing required column: " + QUESTION_NAME_HEADER);
				if (!csvRecord.isMapped(QUESTION_ANSWER_HEADER)) throw new BadRequestException("Missing required column: " + QUESTION_ANSWER_HEADER);
				if (!csvRecord.isMapped(ANSWER_LINK_HEADER)) throw new BadRequestException("Missing required column: " + ANSWER_LINK_HEADER);

				// Accessing values by Header names
				String code = csvRecord.isSet(QUESTION_ID_HEADER) ? csvRecord.get(QUESTION_ID_HEADER).trim() : null;
				String question = csvRecord.get(QUESTION_NAME_HEADER).trim();
				String answer = csvRecord.get(QUESTION_ANSWER_HEADER).trim();
				String answerType = csvRecord.get(ANSWER_TYPE_HEADER).trim();
				String answerLink = csvRecord.get(ANSWER_LINK_HEADER).trim();
				String answerText = csvRecord.isMapped(ANSWER_TEXT_HEADER) ? csvRecord.get(ANSWER_TEXT_HEADER).trim() : null;
				String answerComment = csvRecord.isMapped(ANSWER_COMMENT_HEADER) ? csvRecord.get(ANSWER_COMMENT_HEADER).trim() : null;

				if (StringUtils.isNotEmpty(question)) {
					Optional<QualitativeQuestions> questionDetails = findQuestion(riskModelId, question, code);
					if (questionDetails.isEmpty()) {
						result.getIgnored().add(new ItemViewDTO(question));
						continue;
					}

					List<QualitativeQuestionAnswers> answersList = qualitativeQuestionAnswerRepository.getByQuestionAndAnswer(questionDetails.get().getId(), answer);
					QualitativeQuestionAnswers answerDetails = CollectionUtils.isNotEmpty(answersList) ? answersList.get(0) : null;

					// Fill Question
					VendorType vendorType = VendorType.of(answerType);

					boolean isSystemRecord = false;
					boolean isOrganizationRecord = false;
					switch (vendorType) {
						case System:
						case GDPRSystem:
							isSystemRecord = true;
							break;
						case Organization:
						case GDPROrganization:
							isOrganizationRecord = true;
							break;
					}

					if (isSystemRecord) {
						Optional<Systems> system = systemRepository.getFirstByNameForOrganization(answerLink, organization.getId());
						if (system.isPresent()) {
							List<QuestionAnswersForSystem> systemAnswers = questionAnswersForSystemRepository.getListBySystemAndQuestions(system.get().getId(), Arrays.asList(questionDetails.get().getId()));
							for (QuestionAnswersForSystem systemAnswer : systemAnswers) {
								questionAnswersForSystemRepository.delete(systemAnswer);
							}

							QuestionAnswersForSystem newSystemAnswer = new QuestionAnswersForSystem();
							newSystemAnswer.setQuestion(questionDetails.get());
							newSystemAnswer.setAnswer(answerDetails);
							newSystemAnswer.setSystem(system.get());
							newSystemAnswer.setAnswerText(answerText);
							newSystemAnswer.setAnswerComment(answerComment);
							newSystemAnswer.setCreatedAt(new Date());
							newSystemAnswer.setCreatedBy(currentUser);
							newSystemAnswer.setUpdatedAt(new Date());
							newSystemAnswer.setUpdatedBy(currentUser);
							questionAnswersForSystemRepository.save(newSystemAnswer);

							// Save Audit Log CREATE event
							auditLogService.create(
								VItemType.QUALITATIVE_QUESTION_ANSWER_FOR_SYSTEM,
								newSystemAnswer.getId(),
								new QuestionAnswerForSystemsDTO(newSystemAnswer),
								AuditLogItemId.of(VItemType.QUALITATIVE_QUESTION, questionDetails.get().getId()), AuditLogItemId.of(VItemType.SYSTEM, system.get().getId())
							);

							result.getUpdated().add(new ItemViewDTO<>(String.format("%s - %s, %s", question, answer, answerLink)));
						} else {
							result.getIgnored().add(new ItemViewDTO<>(String.format("%s - %s, %s", question, answer, answerLink)));
						}
					} else {
						Optional<Organizations> vendor = Optional.empty();
						if (isOrganizationRecord) {
							vendor = Optional.of(organization);
						} else {
							vendor = organizationRepository.findFirstByNameAndOrganizationTypeAndRootParent(answerLink, OrganizationType.Vendor, organization);
						}

						if (vendor.isPresent()) {
							List<QuestionAnswersForVendor> vendorAnswers = questionAnswersForVendorRepository.getListByVendorAndQuestions(vendor.get().getId(), Arrays.asList(questionDetails.get().getId()));
							for (QuestionAnswersForVendor vendorAnswer : vendorAnswers) {
								questionAnswersForVendorRepository.delete(vendorAnswer);
							}

							QuestionAnswersForVendor newVendorAnswer = new QuestionAnswersForVendor();
							newVendorAnswer.setQuestion(questionDetails.get());
							newVendorAnswer.setAnswer(answerDetails);
							newVendorAnswer.setVendor(vendor.get());
							newVendorAnswer.setAnswerText(answerText);
							newVendorAnswer.setAnswerComment(answerComment);
							newVendorAnswer.setCreatedAt(new Date());
							newVendorAnswer.setCreatedBy(currentUser);
							newVendorAnswer.setUpdatedAt(new Date());
							newVendorAnswer.setUpdatedBy(currentUser);
							questionAnswersForVendorRepository.save(newVendorAnswer);

							// Save Audit Log CREATE event
							auditLogService.create(
								VItemType.QUALITATIVE_QUESTION_ANSWER_FOR_VENDOR,
								newVendorAnswer.getId(),
								new QuestionAnswerForVendorsDTO(newVendorAnswer),
								AuditLogItemId.of(VItemType.QUALITATIVE_QUESTION, questionDetails.get().getId()), AuditLogItemId.of(VItemType.VENDOR, vendor.get().getId())
							);

							result.getUpdated().add(new ItemViewDTO<>(String.format("%s - %s, %s", question, answer, answerLink)));
						} else {
							result.getIgnored().add(new ItemViewDTO<>(String.format("%s - %s, %s", question, answer, answerLink)));
						}
					}

				}

			}

			result.setStatus("SUCCESS");

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			log.warn(e.getMessage(), e);
			throw e;
		} catch (Throwable e) {
			log.warn(e.getMessage(), e);
			throw e;
		}

		return result;
	}

	/**
	 * Create CSV Printer to build Business Units
	 *
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */
	private CSVPrinter createCsvPrinter(ByteArrayOutputStream outputStream) throws IOException {
		Writer writer = new OutputStreamWriter(outputStream);
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
			QUESTION_ID_HEADER
			, QUESTION_NAME_HEADER
			, QUESTION_WEIGHT_HEADER
			, QUESTION_ANSWER_HEADER
			, QUESTION_ANSWER_WEIGHT_HEADER
			, QUESTION_VENDOR_TYPE_HEADER
			, QUESTION_QUAL_METRIC_HEADER
			, QUESTION_ORDINAL_HEADER
			, QUESTION_ALL_VENDORS_HEADER
			, QUESTION_ALLOW_MULTIPLE_ANSWERS
			, QUESTION_DESCRIPTION_HEADER
			, QUESTION_BRANCHING_LOGIC_HEADER
			, QUESTION_CATEGORY_HEADER
			, QUESTION_IS_INTERNAL_HEADER
			, QUESTION_UPLOAD_DOCUMENT_HEADER
			, QUESTION_WRITE_TEXT_HEADER
			, QUESTION_IS_TECHNOLOGY_VENDOR_HEADER
			, QUESTION_IS_SYSTEM_VENDOR_HEADER
			, QUESTION_IS_SERVICE_VENDOR_HEADER
			, QUESTION_ALLOW_COMMENT_HEADER
			, QUESTION_USE_COLOR_CODING
		);

		return new CSVPrinter(writer, csvFormat);
	}

	/**
	 * Obtain Branching logic String for the question
	 *
	 * @param questions
	 * @return
	 */
	private String getQuestionBranchingLogicString(QualitativeQuestions questions) {
		String result = null;

		if (questions != null && questions.getBranchingLogic() != null && questions.getBranchingLogic().size() > 0) {
			List<String> items = new ArrayList<>();
			for (QuestionBranchingLogic branchingLogic : questions.getBranchingLogic()) {
				if (branchingLogic.getQuestion() == null || branchingLogic.getAnswer() == null) {
					continue;
				}
				String questionInBranching = branchingLogic.getQuestion().getQuestion() + "|" + branchingLogic.getAnswer().getAnswer();
				questionInBranching += "|" + (branchingLogic.getOrdinal() != null ? branchingLogic.getOrdinal() : "0");
				questionInBranching += "|" + (branchingLogic.getOperation() != null ? branchingLogic.getOperation() : "0");

				items.add(questionInBranching);
			}

			if (items.size() > 0) {
				result = StringUtils.join(items, "@@");
			}
		}

		return result;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(QualitativeQuestionEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getQualitativeMetric() != null) logItems.add(AuditLogItemId.of(VItemType.QUALITATIVE_METRIC, existingItemDTO.getQualitativeMetric().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
