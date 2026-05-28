package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.document.DownloadUrlDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DownloadType;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentTypes;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.repository.jpa.AssessmentTypesRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelRepository;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.service.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Data Export Controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-04-12
 */
@RestController
@RequestMapping(
	value = DataExportController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Data Export Controller"
)
@Tag(name = "Data Export")
public class DataExportController {

	static final String CONTROLLER_URI = "/api/data-export";

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private AssessmentTypesRepository assessmentTypesRepository;

	@Autowired
	private AssessmentTypeService assessmentTypeService;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private ControlSubcategoryService controlSubcategoryService;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private GDPRItemsService gdprItemsService;

	@Autowired
	private ImportDataService importDataService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private ProcessService processService;

	@Autowired
	private QualitativeQuestionService qualitativeQuestionService;

	@Autowired
	private SecurityRequirementService securityRequirementService;

	@Autowired
	private TechnologyService technologyService;

	@Autowired
	private QuantMetricsService quantMetricsService;
	@Autowired
	private RiskModelRepository riskModelRepository;
	@Autowired
	private TechnologyAssetsService technologyAssetsService;

	/**
	 * Obtain download URL for the document
	 *
	 * @return Document Download URL
	 */
	@GetMapping(value = "/get-download-url/{downloadType}", name = "Obtain download URL for the document")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public DownloadUrlDTO getDownloadUrl(
		@PathVariable("downloadType") DownloadType downloadType,
		@RequestParam(name = "riskModelId", required = false) Long riskModelId
	) {
		DownloadUrlDTO result = documentService.buildDownloadUrl(downloadType, riskModelId);

		return result;
	}


	/**
	 * Download CSV document with Business Units Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/business-units/export", name = "Get Business Units Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadBusinessUnitsAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = businessUnitService.getDownloadData();

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("BusinessUnits", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV Template
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/business-unit-template/download", name = "Get Business Units CSV template")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).BUSINESS_UNIT_DOWNLOAD_TEMPLATE)")
	public void downloadBusinessUnitCSVTemplate(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = businessUnitService.getDownloadTemplate();

		// Build HTTP Response
		// Response.ResponseBuilder response = Response.ok(byteArrayInputStream.readAllBytes());
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		response.setHeader("Content-Disposition", "attachment; filename=\"BusinessUnitsTemplate.csv\"");
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Qualitative Questions Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/qual-questions/export", name = "Get Qualitative Questions Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadQualitativeQuestionsAsCSV(
		HttpServletResponse response,
		@Parameter(description = "Risk Model", required = true, example = "101") @RequestParam("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Scoring Type") @RequestParam(name = "scoringType", required = false) List<VendorType> scoringType,
		@Parameter(description = "Qualitative Metric") @RequestParam(name = "qualitativeMetric", required = false) List<Long> qualitativeMetric
	) throws IOException {
		// Get data to Download
		ByteArrayInputStream byteArrayInputStream = qualitativeQuestionService.getDownloadData(riskModelId, scoringType, qualitativeMetric);

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("ScoringQuestions", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Qualitative Questions Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/xlsx/qual-questions/organization/report/{metricCode}", name = "Get Qualitative Questions Report in XLSX")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).EXPORT_QUALITATIVE_QUESTION)")
	public void downloadOrganizationQualitativeQuestionsXLSXReport(
		HttpServletResponse response,
		@Parameter(description = "Risk Model", required = true, example = "101") @RequestParam("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Metric Code", required = true, example = "Metric Code") @PathVariable("metricCode") @NotNull @Size(min = 1) String metricCode
	) throws IOException {
		// Get data to Download
		ByteArrayInputStream byteArrayInputStream = qualitativeQuestionService.generateXLSXReport(riskModelId, metricCode);

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("QualitativeQuestions", organization.getName(), metricCode.toUpperCase(), "xlsx");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Qualitative Questions Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/qual-questions/export/{questionType}", name = "Get Qualitative Questions Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).EXPORT_QUALITATIVE_QUESTION)")
	public void downloadQualitativeQuestionsForTypeAsCSV(
		HttpServletResponse response,
		@Parameter(description = "Risk Model", required = true, example = "101") @RequestParam("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Question Type", required = true, example = "System") @PathVariable("questionType") @NotNull @Size(min = 1) VendorType questionType
	) throws IOException {
		// Get data to Download
		ByteArrayInputStream byteArrayInputStream = qualitativeQuestionService.getDownloadData(riskModelId, (questionType != null ? Arrays.asList(questionType) : null), null);

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("QualitativeQuestions", organization.getName(), questionType.name().toUpperCase(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Answers for Scoring Questions
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/qual-questions/answers/export", name = "Get Answers for Scoring Questions in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).EXPORT_QUALITATIVE_QUESTION_ANSWERS)")
	public void downloadAnswersForScoringQuestionsAsCSV(
		HttpServletResponse response,
		@Parameter(description = "Risk Model", required = true, example = "101") @RequestParam("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Scoring Type") @RequestParam(name = "scoringType", required = false) List<VendorType> scoringType,
		@Parameter(description = "Qualitative Metric") @RequestParam(name = "qualitativeMetric", required = false) List<Long> qualitativeMetric
	) throws IOException {
		// Get data to Download
		ByteArrayInputStream byteArrayInputStream = qualitativeQuestionService.getAnswersDownloadData(riskModelId, scoringType, qualitativeMetric);

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("AnswersForScoringQuestions", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Processes Data for Organization.
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/processes/export", name = "Get Processes Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).PROCESS_EXPORT)")
	public void downloadProcessesAsCSV(HttpServletResponse response) throws IOException {
		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		String fileName = applicationProperties.buildExportFileName("Processes", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		processService.exportProcesses(response.getOutputStream());
	}

	/**
	 * Download Processes CSV Template
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/processes/download-template", name = "Get Processes CSV template")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).PROCESS_EXPORT_TEMPLATE)")
	public void downloadProcessesCSVTemplate(HttpServletResponse response) throws IOException {
		// Build HTTP Response
		response.setHeader("Content-Disposition", "attachment; filename=\"ProcessesTemplate.csv\"");
		processService.exportTemplate(response.getOutputStream());
	}

	/**
	 * Download CSV document with Qualitative Questions Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/qual-questions/answers/export/{questionId}", name = "Get Qualitative Question Answers Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).EXPORT_QUALITATIVE_QUESTION)")
	public void downloadQualitativeQuestionAnswersAsCSV(
		HttpServletResponse response,
		@Parameter(example = "101") @PathVariable("questionId") @NotNull @Size(min = 1) Long itemId
	) throws IOException {
		// Get data to Download
		ByteArrayInputStream byteArrayInputStream = qualitativeQuestionService.buildQuestionAnswersReport(itemId);

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("QualitativeQuestionAnswers", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Control Guidelines Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/control-guidlines/export", name = "Get Control Guidelines Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_SUBCATEGORY_EXPORT)")
	public void downloadControlGuidelinesAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		// ByteArrayInputStream byteArrayInputStream = businessUnitService.getDownloadData();

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();

		Optional<AssessmentTypes> assessmentTypeOptional = assessmentTypesRepository.findFirstByNameIgnoreCaseAndOrganizationId(AssessmentTypeService.FRAMEWORK_NAME_NIST_CSF, organization.getId());

		// Build Download Data CSV
		ByteArrayInputStream byteArrayInputStream = controlSubcategoryService.getDownloadData(assessmentTypeOptional.orElse(null));

		// Build HTTP Response
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("ControlGuidelines", organization.getName(), AssessmentTypeService.FRAMEWORK_NAME_NIST_CSF, "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with GDPR Articles Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/gdpr-articles/export", name = "Get GDPR Articles Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).GDPR_ARTICLE_EXPORT)")
	public void downloadGDPRArticlesAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Data CSV
		ByteArrayInputStream byteArrayInputStream = gdprItemsService.getDownloadData();

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("GDPRArticles", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Assessment Frameworks Data
	 * (Control Functions, Control Categories, Control Subcategories)
	 * for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/assessment-frameworks/export/{itemId}", name = "Get Assessment Framework Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_FRAMEWORK_EXPORT)")
	public void downloadAssessmentFrameworkDataAsCSV(
		HttpServletResponse response,
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) throws IOException {
		AssessmentTypes assessmentType = assessmentTypeService.getAssessmentTypeForCurrentOrganization(itemId);
		// Build Download Data CSV
		ByteArrayInputStream byteArrayInputStream = controlSubcategoryService.getDownloadData(assessmentType);

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("AssessmentFrameworks", organization.getName(), assessmentType.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Security Requirements Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/security-requirements/export", name = "Get Security Requirements Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SECURITY_REQUIREMENT_EXPORT)")
	public void downloadSecurityRequirementsAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = securityRequirementService.getDownloadData();

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("SecurityControls", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with System users Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/users/export", name = "Get System Users Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).USER_EXPORT)")
	public void downloadUsersAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = importDataService.getUserDownloadData();

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("Users", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with System Risks Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/systems-risk-data/export", name = "Get System Risks Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_RISK_EXPORT)")
	public void downloadSystemRisksAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = importDataService.getSystemsDownloadData();

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("SystemRisks", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Technologies Data for Organization.
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/technology-assets/export", name = "Get Technologies Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_EXPORT)")
	public void downloadTechnologyAssetsAsCSV(HttpServletResponse response) throws IOException {
		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		String fileName = applicationProperties.buildExportFileName("Technology Assets", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		technologyAssetsService.exportTechnologyAssets(response.getOutputStream());
	}

	/**
	 * Download CSV document with Technologies Data for Organization.
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/technologies/export", name = "Get Technologies Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_EXPORT)")
	public void downloadTechnologiesAsCSV(HttpServletResponse response) throws IOException {
		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		String fileName = applicationProperties.buildExportFileName("Technologies", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		technologyService.exportTechnologies(response.getOutputStream());
	}

	/**
	 * Download Technologies CSV Template
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/technologies/download-template", name = "Get Technologies CSV template")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_EXPORT_TEMPLATE)")
	public void downloadTechnologiesCSVTemplate(HttpServletResponse response) throws IOException {
		// Build HTTP Response
		response.setHeader("Content-Disposition", "attachment; filename=\"TechnologiesTemplate.csv\"");
		technologyService.exportTemplate(response.getOutputStream());
	}

	/**
	 * Download CSV document with Vendors Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/vendors/export", name = "Get Vendors Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VENDOR_EXPORT)")
	public void downloadVendorsAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = importDataService.getOrganizationsDownloadData(OrganizationType.Vendor);

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("Vendors", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Subsidiary Organizations Data for Organization
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/subsidiaries/export", name = "Get Subsidiary Organizations Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SUBSIDIARY_EXPORT)")
	public void downloadSubsidiariesAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = importDataService.getOrganizationsDownloadData(OrganizationType.Subsidiary);

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("Subsidiaries", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download CSV document with Quant Metrics List Data for Risk Model.
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/csv/quant-metrics/export", name = "Get Quant Metrics Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUANT_METRIC_EXPORT)")
	public void downloadQuantMetricsAsCSV(
		HttpServletResponse response,
		@Parameter(description = "Risk Model", required = true, example = "101") @RequestParam("riskModelId") @NotNull @Size(min = 1) Long riskModelId
	) throws IOException {
		// Build HTTP Response
		String riskModel = riskModelRepository.findById(riskModelId).get().getName();
		String fileName = applicationProperties.buildExportFileName("QuantMetrics", riskModel, "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		quantMetricsService.exportQuantMetrics(response.getOutputStream(), riskModelId);
	}

}
