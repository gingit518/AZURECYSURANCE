package com.cyberintech.vrisk.server.service.admin;

import com.cyberintech.vrisk.server.context.ApplicationContextThreadLocal;
import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.dao.OrganizationModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.OrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationDemoDataConfigDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationEditDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model.RiskModelViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.*;
import com.cyberintech.vrisk.server.service.integrations.cysurance.CysuranceIntegrationService;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

/**
 * Organization management Service. Implements basic Organization logic.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-20
 */
@Service
@Slf4j
public class AdminOrganizationService extends OrganizationService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private AssessmentTypeService assessmentTypeService;

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private ControlSubcategoryService controlSubcategoryService;

	@Autowired
	private GDPRItemsService gdprItemsService;

	@Autowired
	private GDPREvidenceDocumentsService gdprEvidenceDocumentsService;

	@Autowired
	private ImportDataService importDataService;

	@Autowired
	private OrganizationModelDAO organizationModelDAO;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private PackagePlansRepository packagePlansRepository;

	@Autowired
	private QualitativeQuestionService qualitativeQuestionService;

	@Autowired
	private QuantMetricsService quantMetricsService;

	@Autowired
	private QuestionAnswersForVendorRepository questionAnswersForVendorRepository;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private SecurityRequirementService securityRequirementService;

	@Autowired
	private SupportedLanguagesRepository supportedLanguagesRepository;

	@Autowired
	private TechnologyRepository technologyRepository;

	@Autowired
	private UserAssignedVendorRepository userAssignedVendorRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private RiskModelConstantRepository riskModelConstantRepository;

	@Autowired
	private AdminUserService adminUserService;

	@Autowired
	private CysuranceIntegrationService cysuranceIntegrationService;

	/**
	 * Get List of Organizations by Type and Filter
	 *
	 * @return Users List
	 */
	public FilteredResponse<OrganizationFilter, OrganizationViewDTO> getListFiltered(FilteredRequest<OrganizationFilter> filteredRequest) {

		FilteredResponse<OrganizationFilter, OrganizationViewDTO> filteredResponse = new FilteredResponse<OrganizationFilter, OrganizationViewDTO>(filteredRequest);

		PagedResult<Organizations> pagedResult = organizationModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());

		List<OrganizationViewDTO> itemsDTOList = DTOBase.fromEntitiesList(pagedResult.getItems(), OrganizationViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(pagedResult.getCount().intValue());

		return filteredResponse;
	}

	/**
	 * Get organization Details to edit
	 *
	 * @param id
	 * @return
	 */
	public OrganizationEditDTO getDetails(Long id) {
		Organizations organization = getOrganization(id);

		OrganizationEditDTO result = new OrganizationEditDTO(organization);

		return result;
	}

	/**
	 * Load Demo Data
	 *
	 * @return Load Status
	 */
	public ImportResultDTO loadDemoData(OrganizationDemoDataConfigDTO organizationDemoDataConfig) {
		ImportResultDTO result = new ImportResultDTO();
		ImportResultDTO localResult;

		InputStream fileStream = null;

		Long organizationId = organizationDemoDataConfig.getOrganizationId();
		// Set Organization Context for the API call
		ApplicationContextThreadLocal.getContext().setOrganizationId(organizationDemoDataConfig.getOrganizationId());

		// Load Demo Users Data
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoUsers())) {
			fileStream = loadDemoFileWithOrganizationUsers(organizationId, "/demo-data/03_ValuRisQ_Users.csv");
			localResult = importDataService.importUsersFromCSVFile(fileStream);
			result.load(localResult);
		}

		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoBusinessUnits())) {
			fileStream = loadDemoFileWithOrganizationUsers(organizationId, "/demo-data/01_ValuRisQ_BusinessUnits.csv");
			localResult = businessUnitService.importFromCSVFile(fileStream);
			result.load(localResult);
		}

		// Load Subsidiaries Data
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoSubsidiaries())) {
			fileStream = loadDemoDataFileStream("/demo-data/02_ValuRisQ_Subsidiaries.csv");
			localResult = importDataService.importOrganizationsFromCSVFile(fileStream, OrganizationType.Subsidiary);
			result.load(localResult);
		}

		// Load Vendors Data
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoVendors())) {
			fileStream = loadDemoFileWithOrganizationUsers(organizationId, "/demo-data/04_ValuRisQ_Vendors.csv");
			localResult = importDataService.importOrganizationsFromCSVFile(fileStream, OrganizationType.Vendor);
			result.load(localResult);
		}

		// Load Systems Data
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoSystems())) {
			fileStream = loadDemoFileWithOrganizationUsers(organizationId, "/demo-data/05_ValuRisQ_SystemRisks.csv");
			localResult = importDataService.importSystemsFromCSVFile(fileStream);
			result.load(localResult);
		}

		// Load Technologies Data
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoTechnologies())) {
			fileStream = loadDemoDataFileStream("/demo-data/09_ValuRisQ_Technologies.csv");
			localResult = importDataService.importTechnologiesFromCSVFile(fileStream);
			result.load(localResult);
		}

		// Load GDPR Items Data
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoGDPRArticles())) {
			fileStream = loadDemoDataFileStream("/demo-data/gdpr_articles_vocabulary.csv");
			localResult = gdprItemsService.importFromCSVFile(fileStream);
			result.load(localResult);
			fileStream = loadDemoDataFileStream("/demo-data/gdpr_evidence_documents_articles_mapping.csv");
			localResult = gdprEvidenceDocumentsService.importFromCSVFile(fileStream);
			result.load(localResult);
		}

		Long riskModelId = organizationDemoDataConfig.getRiskModelId();
		if (riskModelId == null || riskModelId < 1) {
			List<RiskModelViewDTO> riskModels = riskModelService.getList();
			riskModelId = riskModels.get(0).getId();
		}

		// Load Qualitative Questions Data
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsAmplified())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_01_ValuRisQ_ScoringQuestions.AmplifiedOperational.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsConfidentiality())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_02_ValuRisQ_ScoringQuestions.Confidentiality.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsFFIECInherent())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_03_ValuRisQ_ScoringQuestions.FFIEC_Inherent.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsFFIECOrgMaturity())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_04_ValuRisQ_ScoringQuestions.FFIEC_Maturity.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsImpactSystem())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_05_ValuRisQ_ScoringQuestions.Impact.System.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsImpactVendor())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_06_ValuRisQ_ScoringQuestions.Impact.Vendor.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsImpactGDPR())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_07_ValuRisQ_ScoringQuestions.Impact.GDPR.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsIntegrity())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_08_ValuRisQ_ScoringQuestions.Integrity.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsLikelihoodSystem())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_09_ValuRisQ_ScoringQuestions.Likelihood.System.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsLikelihoodVendor())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_10_ValuRisQ_ScoringQuestions.Likelihood.Vendor.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsLikelihoodGDPR())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_11_ValuRisQ_ScoringQuestions.Likelihood.GDPR.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoScoringQuestionsMaturity())) {
			fileStream = loadDemoDataFileStream("/demo-data/08_12_ValuRisQ_ScoringQuestions.Maturity.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}

		// Load Quant Metrics Data
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoQuantMetrics())) {
			fileStream = loadDemoDataFileStream("/demo-data/10_ValuRisQ_QuantMetrics.csv");
			localResult = quantMetricsService.importQuantMetricsFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}

		/*
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoQualitativeQuestionsGDPR())) {
			fileStream = loadDemoDataFileStream("/demo-data/qualitative_questions.GDPR.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoQualitativeQuestionsVendorInternal())) {
			fileStream = loadDemoDataFileStream("/demo-data/qualitative_questions.INTERNAL.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		// Load Cyber Security Maturity questions
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoCyberSecurityMaturity())) {
			fileStream = loadDemoDataFileStream("/demo-data/qualitative_questions_cybersecurity_maturity.csv");
			localResult = qualitativeQuestionService.importFromCSVFile(riskModelId, fileStream);
			result.load(localResult);
		}
		*/

		// Load Security Frameworks
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoFramework_ISO_IEC_27001_2005())) {
			loadFrameworkData(result, "/demo-data/assessments/ISO-IEC_27001-2005.csv", AssessmentTypeService.FRAMEWORK_NAME_ISO_IEC_27001_2005);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoFramework_ISO_IEC_27001_2013())) {
			loadFrameworkData(result, "/demo-data/assessments/ISO-IEC_27001-2013.csv", AssessmentTypeService.FRAMEWORK_NAME_ISO_IEC_27001_2013);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoFramework_NIST_SP800())) {
			loadFrameworkData(result, "/demo-data/assessments/NIST_SP_800-53.csv", AssessmentTypeService.FRAMEWORK_NAME_NIST_SP_800_53);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoFramework_NIST_CSF())) {
			loadFrameworkData(result, "/demo-data/assessments/NIST_CSF.csv", AssessmentTypeService.FRAMEWORK_NAME_NIST_CSF);
		}
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoFramework_PCI_DSS())) {
			loadFrameworkData(result, "/demo-data/assessments/PCI_DSS.csv", AssessmentTypeService.FRAMEWORK_NAME_PCI_DSS);
		}

		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoSecurityRequirements())) {
			fileStream = loadDemoDataFileStream("/demo-data/07_ValuRisQ_SecurityRequirements.csv");
			localResult = securityRequirementService.importFromCSVFile(fileStream);
			result.load(localResult);
		}

		/*
		if (Boolean.TRUE.equals(organizationDemoDataConfig.getLoadDemoSecurityRequirements())) {
			fileStream = loadDemoDataFileStream("/demo-data/assessments/_SecurityRequirements.FRAMEWORKFUL.csv");
			localResult = securityRequirementService.importFromCSVFile(fileStream);
			result.load(localResult);
		}
		*/

		return result;
	}

	@NotNull
	private InputStream loadDemoFileWithOrganizationUsers(Long organizationId, String fileName) {
		InputStream fileStream;
		log.info(String.format("## Loading DEMO file for Organization: %s, %s", organizationId, fileName));
		fileStream = loadDemoDataFileStream(fileName);
		try {
			// String importedText = new String(fileStream.readAllBytes(), StandardCharsets.UTF_8);
			String importedText = IOUtils.toString(fileStream, StandardCharsets.UTF_8.name());
			log.info(String.format("#### File size is: %s", importedText.length()));
			importedText = StringUtils.replace(importedText, "{demouser01}", "demouser_" + organizationId + "+1@risk-q.com");
			importedText = StringUtils.replace(importedText, "{demouser02}", "demouser_" + organizationId + "+2@risk-q.com");
			importedText = StringUtils.replace(importedText, "{demouser03}", "demouser_" + organizationId + "+3@risk-q.com");
			importedText = StringUtils.replace(importedText, "{demouser04}", "demouser_" + organizationId + "+4@risk-q.com");
			log.info(String.format("#### replaced DEMO Users: %s", importedText.length()));

			fileStream = new ByteArrayInputStream(importedText.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return fileStream;
	}

	/**
	 * Load framework data from the drive
	 *
	 * @param result
	 * @param frameworkFilename
	 * @param frameworkName
	 */
	private void loadFrameworkData(ImportResultDTO result, String frameworkFilename, String frameworkName) {
		InputStream fileStream = loadDemoDataFileStream(frameworkFilename);
		AssessmentTypes assessmentDetails = assessmentTypeService.ensureFramework(frameworkName);
		ImportResultDTO localResult = controlSubcategoryService.importFromCSVFile(fileStream, assessmentDetails.getId());
		result.load(localResult);
	}

	/**
	 * Load demo data file content from file
	 *
	 * @param fileName
	 * @return
	 */
	public InputStream loadDemoDataFileStream(String fileName) {
		InputStream in = this.getClass().getResourceAsStream(fileName);

		return in;
	}

	/**
	 * Create new Organization
	 *
	 * @return New Organization
	 */
	public OrganizationEditDTO create(OrganizationEditDTO newItemDTO) {

		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()), ApplicationExceptionCodes.CREATE_IS_NOT_ALLOWED_FOR_ITEM_WITH_EXISTING_ID);
		}

		// Verify organization with such name not exists
		if (organizationRepository.findFirstByNameAndOrganizationTypeAndIdIsNotIn(newItemDTO.getName(), OrganizationType.Organization, Arrays.asList(0l)).isPresent()) {
			throw new ConflictException(MessageFormat.format("Organization with this name already registered in the system [{0}]", newItemDTO.getName()), ApplicationExceptionCodes.ORGANIZATION_WITH_NAME_ALREADY_EXISTS);
		}

//		Organizations newItem = newItemDTO.toEntity();
		Organizations newItem = new Organizations();
		newItem.setCreatedAt(new Date());
		newItem.setUpdatedAt(new Date());

		applyEntityChanges(newItemDTO, newItem);

		Organizations saveResult = organizationRepository.save(newItem);

		// Verify Elastio Package Plan setup
		if (newItem.getPackagePlan() != null && PackagePlans.PACKAGE_PLAN_ELASTIO.equals(newItem.getPackagePlan().getId())) {
			verifyElastioPackagePlanSetup(newItem);
		}

		OrganizationEditDTO result = new OrganizationEditDTO(saveResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.ORGANIZATION,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getRootParent() != null ? newItem.getRootParent().getId() : newItem.getId())
		);

		return result;
	}

	/**
	 * Update Organization
	 *
	 * @return New Organization
	 */
	public OrganizationEditDTO update(OrganizationEditDTO itemDTO) {

		OrganizationEditDTO result;

		try {
			// Get Existing item from the database
			Organizations existingItem = organizationRepository.findById(itemDTO.getId()).get();
			OrganizationEditDTO existingItemDTO = new OrganizationEditDTO(existingItem);

			// Verify organization with such name not exists
			if (organizationRepository.findFirstByNameAndOrganizationTypeAndIdIsNotIn(itemDTO.getName(), OrganizationType.Organization, Arrays.asList(itemDTO.getId())).isPresent()) {
				throw new ConflictException(MessageFormat.format("Organization with this name already registered in the system [{0}]", itemDTO.getName()), ApplicationExceptionCodes.ORGANIZATION_WITH_NAME_ALREADY_EXISTS);
			}

			// Update item details
			Organizations updatedItem = existingItem;
			updatedItem.setUpdatedAt(new Date());

			applyEntityChanges(itemDTO, updatedItem);

			// Save to the database
			Organizations saveResult = organizationRepository.save(updatedItem);

			// Verify Elastio Package Plan setup
			if (updatedItem.getPackagePlan() != null && PackagePlans.PACKAGE_PLAN_ELASTIO.equals(updatedItem.getPackagePlan().getId())) {
				verifyElastioPackagePlanSetup(updatedItem);
			}

			result = new OrganizationEditDTO(saveResult);

			// Save Audit Log UPDATE event
			auditLogService.update(
				VItemType.ORGANIZATION,
				saveResult.getId(),
				existingItemDTO,
				result,
				collectAuditLogItems(result, (existingItem.getRootParent() != null ? existingItem.getRootParent().getId() : existingItem.getId()))
			);

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Organization not found in the system [{0}]", itemDTO.getId()), ApplicationExceptionCodes.ORGANIZATION_NOT_EXISTS);
		}

		return result;
	}

	/**
	 * Fill the set of entity relations
	 *
	 * @param itemDTO
	 * @param entity
	 */
	@Override
	protected void applyEntityChanges(OrganizationEditDTO itemDTO, Organizations entity) {
		super.applyEntityChanges(itemDTO, entity);

		if (itemDTO.getLogoutAfterInactivityTimeInMinutes() != null && itemDTO.getLogoutAfterInactivityTimeInMinutes() >= 1) {
			entity.setLogoutAfterInactivityTime(itemDTO.getLogoutAfterInactivityTimeInMinutes() * 60000);
		}

		entity.setOrganizationType(itemDTO.getOrganizationType());
		entity.setZoomInfoId(itemDTO.getZoomInfoId());

		// Apply package Plan
		if (itemDTO.getPackagePlan() != null) {
			if (itemDTO.getPackagePlan().getId() != null) {
				entity.setPackagePlan(packagePlansRepository.findById(itemDTO.getPackagePlan().getId()).get());
			} else {
				entity.setPackagePlan(null);
			}
		}

		Optional.ofNullable(itemDTO.getSupportedLanguages()).ifPresent(supportedLanguageEditDTOList -> {
			entity.setSupportedLanguages(new HashSet<>());
			itemDTO.getSupportedLanguages().stream().forEach(supportedLanguageEditDTO -> {
				entity.getSupportedLanguages().add(supportedLanguagesRepository.findById(supportedLanguageEditDTO.getId()).get());
			});
		});

		if (entity.getSupportedLanguages().size() > 1) {
			entity.setIsMultiLanguage(true);
		} else {
			entity.setIsMultiLanguage(false);
		}

		if (itemDTO.getRootParent() != null) {
			entity.setRootParent(organizationRepository.findById(itemDTO.getRootParent().getId()).orElse(null));
		}

		if (itemDTO.getOrganizationType().equals(OrganizationType.Vendor)) {
			if (itemDTO.getIsCloudVendor() != null) entity.setIsCloudVendor(itemDTO.getIsCloudVendor());
			if (itemDTO.getIsServiceVendor() != null) entity.setIsServiceVendor(itemDTO.getIsServiceVendor());
			if (itemDTO.getIsSystemVendor() != null) entity.setIsSystemVendor(itemDTO.getIsSystemVendor());
			if (itemDTO.getIsTechnologyVendor() != null) entity.setIsTechnologyVendor(itemDTO.getIsTechnologyVendor());

			// Set Technologies
			Optional.ofNullable(itemDTO.getTechnologies()).ifPresent(technologiesRefDTOList -> {
				entity.setTechnologies(new HashSet<>());
				technologiesRefDTOList.stream().forEach(technologyRefDTO -> {
					entity.getTechnologies().add(technologyRepository.findById(technologyRefDTO.getId()).get());
				});
			});
		}

		if (itemDTO.getPackagePlan() != null) {
			if (itemDTO.getIntegrationProperties() != null) {
				if (entity.getIntegrationProperties() == null) {
					entity.setIntegrationProperties(new HashMap<>());
				}
				for (Map.Entry<String, String> integrationProperty : itemDTO.getIntegrationProperties().entrySet()) {
					entity.getIntegrationProperties().put(integrationProperty.getKey(), integrationProperty.getValue());
				}
			}
		}

	}

	protected void verifyElastioPackagePlanSetup(Organizations organization) {
		// Verify Elastio Package Plan Setup
		List<RiskModels> riskModels = riskModelRepository.findAllByOrganizationIdOrderByIdAsc(organization.getId());
		RiskModels elastioRiskModel = null;
		if (CollectionUtils.isEmpty(riskModels)) {
			RiskModels newItem = new RiskModels();
			newItem.setName("Elastio");
			newItem.setDescription("Elastio Risk Model");
			newItem.setOrganizationId(organization.getId());
			newItem.setOrdinal(1L);
			elastioRiskModel = riskModelRepository.save(newItem);
		} else {
			elastioRiskModel = riskModels.stream().filter(item -> item.getName() != null && item.getName().toLowerCase().startsWith("elastio")).findAny().orElse(riskModels.get(0));
		}

		// Verify Elastio variables
		try {
			// Parse CSV file
			InputStream fileStream = loadDemoDataFileStream("/templates/Elastio/risk-model-constants.csv");
			CSVParser csvParser = CSVUtils.createCSVParser(fileStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			log.info(String.format("#### Loaded CSV records: %s", csvRecordList.size()));
			List<RiskModelConstants> constantsToSave = new ArrayList<>();
			for (CSVRecord csvRecord : csvRecordList) {
				// Accessing values by Header names
				String name = Optional.ofNullable(csvRecord.get("name")).orElse("").trim();
				String description = csvRecord.get("description");
				String defaultValueString = csvRecord.get("defaultValue");
				Double defaultValue = 0D;
				if (defaultValueString != null && !defaultValueString.isEmpty()) {
					try {
						defaultValue = Double.parseDouble(defaultValueString);
					} catch (NumberFormatException numberFormatException) {
						log.warn("!! Failed to parse default value for Elastio constant: {}, {}", name, defaultValueString);
					}
				}

				RiskModelConstants riskModelConstant = riskModelConstantRepository.findByNameAndRiskModelId(name, elastioRiskModel.getId()).orElse(null);
				if (riskModelConstant == null) {
					riskModelConstant = new RiskModelConstants();
					riskModelConstant.setName(name);
					riskModelConstant.setValue(defaultValue);
					riskModelConstant.setRiskModelId(elastioRiskModel.getId());
					riskModelConstant.setCreatedAt(new Date());
				}
				if (riskModelConstant.getId() == null || !description.equalsIgnoreCase(riskModelConstant.getDescription())) {
					riskModelConstant.setDescription(description);
					riskModelConstant.setUpdatedAt(new Date());
					constantsToSave.add(riskModelConstant);
				}
			}

			// Save new/modified constants
			if (CollectionUtils.isNotEmpty(constantsToSave)) {
				riskModelConstantRepository.saveAll(constantsToSave);
				riskModelConstantRepository.flush();
			}

		} catch (IOException e) {
			log.error("Failed to import Elastio variables", e);
		}

		// Set Organization Context for the API call
		ApplicationContextThreadLocal.getContext().setOrganizationId(organization.getId());
		UserDetailsImpl currentUser = adminUserService.getCurrentUser();
		Long userOrganizationId = currentUser.getOrganizationId();
		currentUser.applyOrganizationId(organization.getId());

		log.info("## Importing QUANT Metrics for the Organization");
		InputStream fileStream = loadDemoDataFileStream("/templates/Elastio/quant-metrics.csv");
		ImportResultDTO quantImportResult = quantMetricsService.importQuantMetricsFromCSVFile(elastioRiskModel.getId(), fileStream);

		// Restore Active Organization ID for User
		currentUser.applyOrganizationId(userOrganizationId);
	}


	/**
	 * Delete Organization
	 *
	 * @return New Organization
	 */
	@Transactional
	public OrganizationEditDTO deleteOrganizationById(Long itemId) {

		OrganizationEditDTO result;

		try {
			Long currentOrganizationId = getCurrentOrganizationId();

			// Get Existing item from the database
			Organizations organization = organizationRepository.findById(itemId).get();

			if (!organization.getOrganizationType().equals(OrganizationType.Subsidiary) && !organization.getOrganizationType().equals(OrganizationType.Vendor)) {
				throw new BadRequestException(MessageFormat.format("Only Subsidiary or Vendor Organization can be Deleted [{0}]", organization.getName()), ApplicationExceptionCodes.ONLY_SUBSIDIARY_ORGANIZATION_CAN_BE_REMOVED);
			}

			result = new OrganizationEditDTO(organization);
			OrganizationEditDTO existingItemDTO = result;

			// Save to the database
			associateVendorRepository.deleteAllByVendor(organization);
			questionAnswersForVendorRepository.deleteAllByVendor(organization);
			userAssignedVendorRepository.deleteAllByVendor(organization);
			organizationRepository.delete(organization);

			// Save Audit Log DELETE event
			VItemType itemType = organization.getOrganizationType().equals(OrganizationType.Subsidiary) ? VItemType.SUBSIDIARY_ORGANIZATION : VItemType.VENDOR;
			auditLogService.delete(
				itemType,
				existingItemDTO.getId(),
				existingItemDTO,
				collectAuditLogItems(existingItemDTO, organization.getId())
			);

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Organization not found in the system [{0}]", itemId), ApplicationExceptionCodes.ORGANIZATION_NOT_EXISTS);
		}

		return result;
	}

	/**
	 * Get organization Details to edit
	 *
	 * @param id
	 * @return
	 */
	@Transactional
	public OrganizationRefDTO runIntegration(Long id) {
		Organizations organization = getOrganization(id);

		if (organization.getPackagePlan() != null && PackagePlans.PACKAGE_PLAN_CYSURANCE.equals(organization.getPackagePlan().getId())) {
			// Process Cysurance Integration
			return cysuranceIntegrationService.runCysuranceIntegration(organization);
		}

		OrganizationRefDTO result = new OrganizationRefDTO(organization);

		return result;
	}

}
