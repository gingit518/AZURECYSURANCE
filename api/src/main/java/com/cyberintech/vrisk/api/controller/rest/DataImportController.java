package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentTypeEditDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentTypes;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.repository.jpa.AssessmentTypesRepository;
import com.cyberintech.vrisk.server.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Optional;

/**
 * Data Import controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-04-19
 */
@RestController
@RequestMapping(
	value = DataImportController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Data Import Controller"
)
@Tag(name = "Data Import")
public class DataImportController {

	static final String CONTROLLER_URI = "/api/data-import";

	@Autowired
	private AssessmentTypesRepository assessmentTypesRepository;

	@Autowired
	private AssessmentTypeService assessmentTypeService;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private QualitativeQuestionService qualitativeQuestionService;

	@Autowired
	private ImportDataService importDataService;

	@Autowired
	private GDPRItemsService gdprItemsService;

	@Autowired
	private ControlSubcategoryService controlSubcategoryService;

	@Autowired
	private GDPREvidenceDocumentsService gdprEvidenceDocumentsService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private SecurityRequirementService securityRequirementService;

	@Autowired
	private QuantMetricsService quantMetricsService;

	// @Autowired
	// private KeenIOService keenIOService;

	/**
	 * Upload file with Business Units data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/business-units/import", name = "Import Business Units data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_BUSINESS_UNIT)")
	public ImportResultDTO uploadBusinessUnitsAsCSV(@RequestParam("file") MultipartFile file) {
		ImportResultDTO result = businessUnitService.importFromCSVFile(file);

		return result;
	}

	/**
	 * Upload file with Qualitative Questions data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/qual-questions/import", name = "Import Qualitative Questions data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_QUALITATIVE_QUESTION)")
	public ImportResultDTO uploadQualitativeQuestionsAsCSV(
		@RequestParam("file") MultipartFile file,
		@Parameter(description = "Risk Model", required = true, example = "101") @RequestParam("riskModelId") @NotNull @Size(min = 1) Long riskModelId
	) throws IOException {
		ImportResultDTO result = qualitativeQuestionService.importFromCSVFile(riskModelId, file.getInputStream());

		return result;
	}

	/**
	 * Upload file with Answers for Qualitative Questions data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data", value = "/csv/qual-questions/answers/import",
		name = "Import Answers for Qualitative Questions data from CSV template")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_QUALITATIVE_QUESTION_ANSWERS)")
	public ImportResultDTO uploadQualitativeQuestionsAnswersAsCSV(
		@RequestParam("file") MultipartFile file,
		@Parameter(description = "Risk Model", required = true, example = "101") @RequestParam("riskModelId") @NotNull @Size(min = 1) Long riskModelId
	) throws IOException {
		ImportResultDTO result = qualitativeQuestionService.importAnswersFromCSVFile(riskModelId, file.getInputStream());

		return result;
	}

	/**
	 * Upload file with Control Guidelines data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/control-guidlines/import", name = "Import Control Guidelines data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_CONTROL_GUIDELINE)")
	public ImportResultDTO uploadControlGuidelinesAsCSV(@RequestParam("file") MultipartFile file) throws IOException {

		Organizations organization = organizationService.getCurrentOrganizationEntity();
		Optional<AssessmentTypes> assessmentTypeOptional = assessmentTypesRepository.findFirstByNameIgnoreCaseAndOrganizationId(AssessmentTypeService.FRAMEWORK_NAME_NIST_CSF, organization.getId());
		Long itemId;
		if (assessmentTypeOptional.isEmpty()) {
			AssessmentTypeEditDTO item = new AssessmentTypeEditDTO();
			item.setName(AssessmentTypeService.FRAMEWORK_NAME_NIST_CSF);
			AssessmentTypeEditDTO newFramework = assessmentTypeService.create(item);
			itemId = newFramework.getId();
		} else {
			itemId = assessmentTypeOptional.get().getId();
		}

		ImportResultDTO result = controlSubcategoryService.importFromCSVFile(file.getInputStream(), itemId);

		return result;
	}

	/**
	 * Upload file with GDPR Articles data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/gdpr-articles/import", name = "Import GDPR Articles data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_GDPR_ARTICLE)")
	public ImportResultDTO uploadGDPRArticlesAsCSV(@RequestParam("file") MultipartFile file) {
		ImportResultDTO result = gdprItemsService.importFromCSVFile(file);

		return result;
	}

	/**
	 * Upload file with Assessment Framework data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/assessment-frameworks/import/{itemId}", name = "Import Assessment Framework data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_ASSESSMENT_FRAMEWORK)")
	public ImportResultDTO uploadAssessmentFrameworkDataAsCSV(
		@RequestParam("file") MultipartFile file,
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) throws IOException {
		ImportResultDTO result = controlSubcategoryService.importFromCSVFile(file.getInputStream(), itemId);

		return result;
	}

	/**
	 * Upload file with Security Requirements data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/security-requirements/import", name = "Import Assessment Framework data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_SECURITY_REQUIREMENTS)")
	public ImportResultDTO uploadSecurityRequirementsAsCSV(@RequestParam("file") MultipartFile file) throws IOException {
		ImportResultDTO result = securityRequirementService.importFromCSVFile(file.getInputStream());

		return result;
	}

	/**
	 * Import GDPR Evidence Documents Articles mapping data from CSV template
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/gdpr-evidence-articles-mapping/import", name = "Import GDPR Evidence Documents Articles mapping data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_GDPR_EVIDENCE_ARTICLES)")
	public ImportResultDTO uploadGDPREvidenceDocumentsArticlesMappingAsCSV(@RequestParam("file") MultipartFile file) {
		ImportResultDTO result = gdprEvidenceDocumentsService.importFromCSVFile(file);

		return result;
	}

	/**
	 * Upload file with Processes data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/processes/import", name = "Import Processes data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_PROCESS)")
	public ImportResultDTO uploadProcessDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {
		ImportResultDTO result = importDataService.importProcessesFromCSVFile(file.getInputStream());

		return result;
	}

	/**
	 * Upload file with System Users data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/users/import", name = "Import Users data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_USER)")
	public ImportResultDTO uploadSystemUsersDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {
		ImportResultDTO result = importDataService.importUsersFromCSVFile(file.getInputStream());

		return result;
	}

	/**
	 * Upload file with System Risks data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/systems-risk-data/import", name = "Import System Risks data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_SYSTEM_RISK)")
	public ImportResultDTO uploadSystemRisksDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {
		ImportResultDTO result = importDataService.importSystemsFromCSVFile(file.getInputStream());

		return result;
	}

	/**
	 * Upload file with Technologies data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/technologies/import", name = "Import Technologies data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_TECHNOLOGY)")
	public ImportResultDTO uploadTechnologyDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {
		ImportResultDTO result = importDataService.importTechnologiesFromCSVFile(file.getInputStream());

		return result;
	}

	/**
	 * Upload file with Technologies data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/technology-mapping/import", name = "Import Technology Mapping data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_TECHNOLOGY)")
	public ImportResultDTO uploadTechnologyMappingDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {
		ImportResultDTO result = importDataService.importTechnologyCategoryMappingFromCSVFile(file.getInputStream());

		return result;
	}

	/**
	 * Upload file with Technologies data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/technology-assets/import", name = "Import Technology Mapping data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_TECHNOLOGY)")
	public ImportResultDTO uploadTechnologyAssetsDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {
		ImportResultDTO result = importDataService.importTechnologyAssetsFromCSVFile(file.getInputStream());

		return result;
	}

	/**
	 * Upload file with Vendors data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/vendors/import", name = "Import Vendors data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_VENDOR)")
	public ImportResultDTO uploadVendorsDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {
		ImportResultDTO result = importDataService.importOrganizationsFromCSVFile(file.getInputStream(), OrganizationType.Vendor);

		return result;
	}

	/**
	 * Upload file with Subsidiary Organizations data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/subsidiaries/import", name = "Import Subsidiary Organizations data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_SUBSIDIARY)")
	public ImportResultDTO uploadSubsidiariesDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {
		ImportResultDTO result = importDataService.importOrganizationsFromCSVFile(file.getInputStream(), OrganizationType.Subsidiary);

		return result;
	}

	/**
	 * Upload file with Subsidiary Organizations data in CSV format
	 *
	 * @return
	 */
	/*
	@RequestMapping(method = RequestMethod.POST, value = "/csv/keen-collection/import", name = "Import Keen IO data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ImportResultDTO uploadKeenIODataAsCSV(@RequestParam("file") MultipartFile file) {
		ImportResultDTO result = keenIOService.importKeenCollectionFromCSVFile(file);

		return result;
	}
	*/
	/**
	 * Upload file with Quant Metrics data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/csv/quant-metrics/import", name = "Import Qualitative Questions data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).IMPORT_QUANT_METRICS)")
	public ImportResultDTO uploadQuantMetricsAsCSV(
		@RequestParam("file") MultipartFile file,
		@Parameter(description = "Risk Model", required = true, example = "101") @RequestParam("riskModelId") @NotNull @Size(min = 1) Long riskModelId
	) throws IOException {
		ImportResultDTO result = quantMetricsService.importQuantMetricsFromCSVFile(riskModelId, file.getInputStream());

		return result;
	}

}
