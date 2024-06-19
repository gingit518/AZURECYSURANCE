package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.*;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleItemDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.*;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleToQuestion;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.GDPRArticleToQuestionRepository;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.QualitativeQuestionService;
import com.cyberintech.vrisk.server.service.SystemsService;
import com.cyberintech.vrisk.server.service.VendorService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Qualitative Questions management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@RestController
@RequestMapping(
	value = QualitativeQuestionController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Qualitative Questions Management Controller"
)
@Tag(name = "Qualitative Questions Management")
public class QualitativeQuestionController {

	static final String CONTROLLER_URI = "/api/risk-model/{riskModelId}/qualitative-questions";

	@Autowired
	private QualitativeQuestionService qualitativeQuestionService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private VendorService vendorService;

	@Autowired
	private GDPRArticleToQuestionRepository gdprArticleToQuestionRepository;

	/**
	 * Get Qualitative Questions List for current Category Domain
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Qualitative Questions List for current Organization and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_QUESTION_READ)")
	public List<QualitativeQuestionViewDTO> getList(@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId) {

		List<QualitativeQuestionViewDTO> result = qualitativeQuestionService.getListByRiskModelId(riskModelId);

		return result;
	}

	/**
	 * Get Qualitative Questions List for current Risk Model
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Qualitative Questions List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_QUESTION_READ)")
	public FilteredResponse<QuestionFilter, QualitativeQuestionViewDTO> getListFiltered(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<QuestionFilter> filteredRequest
	) {

		FilteredResponse<QuestionFilter, QualitativeQuestionViewDTO> result = qualitativeQuestionService.getListFiltered(riskModelId, filteredRequest);

		return result;
	}

	/**
	 * Get Qualitative Questions List for current Risk Model and Vendor
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/vendor-filter", name = "Qualitative Questions List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VENDOR_QUAL_QUESTION)")
	public VendorQuestionsView getListForVendorAndType(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody QuestionVendorFilter vendorFilter
		) {

		List<VendorType> questionTypes = Arrays.asList(VendorType.Vendor, VendorType.Both);
		vendorFilter.setQuestionTypes(questionTypes);
		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.getListFilteredForVendorMetric(riskModelId, vendorFilter);
		Organizations vendor = vendorService.getVendor(vendorFilter.getVendorId());

		VendorQuestionsView result = new VendorQuestionsView(vendor, vendorFilter.getMetricDomain(), questions);

		return result;
	}

	/**
	 * Get Qualitative Questions Internal List for current Risk Model and Vendor
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/internal/vendor-filter", name = "Qualitative Questions Internal List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VENDOR_INTERNAL_QUESTION)")
	public VendorQuestionsView getListInternalForVendorAndType(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody QuestionVendorFilter vendorFilter
		) {

//		List<VendorType> questionTypes = Arrays.asList(VendorType.Vendor, VendorType.Both);
		List<VendorType> questionTypes = Arrays.asList(VendorType.VendorInternal);
		vendorFilter.setQuestionTypes(questionTypes);
		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.getListInternalFilteredForVendorMetric(riskModelId, vendorFilter);
		Organizations vendor = vendorService.getVendor(vendorFilter.getVendorId());

		VendorQuestionsView result = new VendorQuestionsView(vendor, vendorFilter.getMetricDomain(), questions);

		return result;
	}

	/**
	 * Get Qualitative Questions List for current Risk Model and Vendor
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/cloud-filter", name = "Qualitative Questions List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CLOUD_SCORING_QUESTION)")
	public VendorQuestionsView getListForCloudAndType(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody QuestionVendorFilter vendorFilter
		) {

		List<VendorType> questionTypes = Arrays.asList(VendorType.Cloud);
		vendorFilter.setQuestionTypes(questionTypes);
		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.getListFilteredForVendorMetric(riskModelId, vendorFilter);
		Organizations vendor = vendorService.getVendor(vendorFilter.getVendorId());

		VendorQuestionsView result = new VendorQuestionsView(vendor, vendorFilter.getMetricDomain(), questions);

		return result;
	}

	/**
	 * Get Qualitative Questions Internal List for current Risk Model and Vendor
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/internal/cloud-filter", name = "Qualitative Questions Internal List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VENDOR_INTERNAL_QUESTION)")
	public VendorQuestionsView getListInternalForCloudAndType(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody QuestionVendorFilter vendorFilter
		) {

		List<VendorType> questionTypes = Arrays.asList(VendorType.CloudInternal);
		vendorFilter.setQuestionTypes(questionTypes);
		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.getListInternalFilteredForVendorMetric(riskModelId, vendorFilter);
		Organizations vendor = vendorService.getVendor(vendorFilter.getVendorId());

		VendorQuestionsView result = new VendorQuestionsView(vendor, vendorFilter.getMetricDomain(), questions);

		return result;
	}

	/**
	 * Get Qualitative Questions List for current Risk Model and System
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/system-filter", name = "Qualitative Questions List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public SystemQuestionsView getListForSystemAndType(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody QuestionSystemFilter filter
	) {

		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.getListFilteredForSystemMetric(riskModelId, filter.getSystemId(), filter.getMetricDomain());
		Systems system = systemsService.getSystemForCurrentOrganization(filter.getSystemId());

		SystemQuestionsView result = new SystemQuestionsView(system, filter.getMetricDomain(), questions);

		return result;
	}

	/**
	 * Get Qualitative Questions List for current Risk Model and Organization
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/organization-filter", name = "Qualitative Questions List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasOneOfPermissions(T(com.cyberintech.vrisk.api.config.APIAction).ORGANIZATION_RISK_SCORING_READ, T(com.cyberintech.vrisk.api.config.APIAction).CYBERSECURITY_MATURITY_READ)")
	public VendorQuestionsView getListForOrganizationAndType(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody QuestionVendorFilter vendorFilter
	) {

		QuestionVendorAdvancedFilter filter = QuestionVendorAdvancedFilter.of(vendorFilter);
		List<VendorType> questionTypes = Arrays.asList(VendorType.Organization);
		filter.setQuestionTypes(questionTypes);
		filter.setRiskModelId(riskModelId);
		filter.setVendorId(organizationService.getCurrentOrganizationId());
		filter.setIgnoreVendorSelection(true);
		filter.setIgnoreInternal(true);
		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.getListFilteredForVendorMetric(riskModelId, filter);
		VendorQuestionsView result = new VendorQuestionsView((Organizations) null, vendorFilter.getMetricDomain(), questions);

		return result;
	}

	/**
	 * Get Qualitative Questions List for current Risk Model and System
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/gdpr-system-filter", name = "Qualitative Questions List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).GDPR_SYSTEM_COMPLIANCE_SCORING_QUESTION)")
	public GDPRQuestionsView getListOfGDPRForSystemAndType(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody QuestionSystemFilter filter
	) {

		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.getListFilteredForSystemMetric(riskModelId, filter.getSystemId(), filter.getMetricDomain(), Arrays.asList(VendorType.GDPRSystem));

		List<GDPRQualitativeQuestionViewDTO> gdprQuestionList = buildGdprQualitativeQuestionViewDTOList(questions);

		Systems system = systemsService.getSystemForCurrentOrganization(filter.getSystemId());

		GDPRQuestionsView result = new GDPRQuestionsView(system, filter.getMetricDomain(), gdprQuestionList);

		return result;
	}

	/**
	 * Get Qualitative Questions List for current Risk Model and System
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/gdpr-organization-filter", name = "Qualitative Questions List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).GDPR_ORGANIZATION_COMPLIANCE_SCORING_QUESTION)")
	public GDPRQuestionsView getListOfGDPRForOrganizationAndType(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody QuestionVendorFilter filter
	) {

		Long vendorId = organizationService.getCurrentOrganizationId();
		filter.setVendorId(vendorId);
		filter.setQuestionTypes(Arrays.asList(VendorType.GDPROrganization));
		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.getListFilteredForVendorMetric(riskModelId, filter);

		List<GDPRQualitativeQuestionViewDTO> gdprQuestionList = buildGdprQualitativeQuestionViewDTOList(questions);

		GDPRQuestionsView result = new GDPRQuestionsView((Organizations) null, filter.getMetricDomain(), gdprQuestionList);

		return result;
	}

	/**
	 * Save Qualitative Question Answers List for current Risk Model and Vendor
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/vendor-save/{vendorId}/{metricDomain}", name = "Save Qualitative Question Answers List for current Risk Model and Vendor")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public VendorQuestionsView saveVendorAnswers(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Object", required = true) @RequestBody VendorQuestionsView vendorQuestionsView
		) {

		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.saveVendorQuestions(riskModelId, vendorQuestionsView.getVendorId(), vendorQuestionsView.getMetricDomainCode(), vendorQuestionsView.getQuestions());

		VendorQuestionsView result = new VendorQuestionsView(vendorQuestionsView.getVendorId(), vendorQuestionsView.getName(), vendorQuestionsView.getMetricDomainCode(), questions);

		return result;
	}

	/**
	 * Save Qualitative Question Answers List for current Risk Model and Organization
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/organization-save/{metricDomain}", name = "Save Qualitative Question Answers List for current Risk Model and Vendor")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public VendorQuestionsView saveOrganizationAnswers(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Object", required = true) @RequestBody VendorQuestionsView vendorQuestionsView
		) {

		vendorQuestionsView.setVendorId(organizationService.getCurrentOrganizationId());
		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.saveVendorQuestions(riskModelId, vendorQuestionsView.getVendorId(),
			vendorQuestionsView.getMetricDomainCode(), vendorQuestionsView.getQuestions(), VendorType.Organization
		);

		VendorQuestionsView result = new VendorQuestionsView(((Long) null), vendorQuestionsView.getMetricDomainCode(), questions);

		return result;
	}

	/**
	 * Save Qualitative Question Answers List for current Risk Model and System
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/system-save/{systemId}/{metricDomain}", name = "Save Qualitative Question Answers List for current Risk Model and System")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public SystemQuestionsView saveSystemAnswers(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Object", required = true) @RequestBody SystemQuestionsView systemQuestionsView
		) {

		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.saveSystemQuestions(riskModelId, systemQuestionsView.getSystemId(), systemQuestionsView.getMetricDomainCode(), systemQuestionsView.getQuestions());

		SystemQuestionsView result = new SystemQuestionsView(systemQuestionsView.getSystemId(), systemQuestionsView.getMetricDomainCode(), questions);

		return result;
	}

	/**
	 * Save Qualitative Question Answers List for current Risk Model and System in GDPR context
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/gdpr-system-save/{systemId}/{metricDomain}", name = "Save Qualitative Question Answers List for current Risk Model and System in GDPR context")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public GDPRQuestionsView saveGDPRSystemAnswers(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Object", required = true) @RequestBody SystemQuestionsView systemQuestionsView
		) {

		List<QualitativeQuestionWithAnswersViewDTO> saveResult = qualitativeQuestionService.saveGDPRSystemQuestions(riskModelId, systemQuestionsView.getSystemId(), systemQuestionsView.getMetricDomainCode(), systemQuestionsView.getQuestions());

		// Obtain all the questions
		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.getListFilteredForSystemMetric(riskModelId, systemQuestionsView.getSystemId(), systemQuestionsView.getMetricDomainCode(), Arrays.asList(VendorType.GDPRSystem));
		List<GDPRQualitativeQuestionViewDTO> gdprQuestionList = buildGdprQualitativeQuestionViewDTOList(questions);
		GDPRQuestionsView result = new GDPRQuestionsView((Systems) null, systemQuestionsView.getMetricDomainCode(), gdprQuestionList);

		return result;
	}

	/**
	 * Save Qualitative Question Answers List for current Risk Model and System in GDPR context
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/gdpr-organization-save/{metricDomain}", name = "Save Qualitative Question Answers List for current Risk Model and System in GDPR context")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public GDPRQuestionsView saveGDPROrganizationAnswers(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Object", required = true) @RequestBody VendorQuestionsView vendorQuestionsView
		) {

		List<QualitativeQuestionWithAnswersViewDTO> savedQuestions = qualitativeQuestionService.saveGDPROrganizationQuestions(riskModelId, vendorQuestionsView.getMetricDomainCode(), vendorQuestionsView.getQuestions());

		// Reload GDPR Organization Questions
		QuestionVendorFilter filter = new QuestionVendorFilter();
		filter.setVendorId(organizationService.getCurrentOrganizationId());
		filter.setQuestionTypes(Arrays.asList(VendorType.GDPROrganization));
		List<QualitativeQuestionWithAnswersViewDTO> questions = qualitativeQuestionService.getListFilteredForVendorMetric(riskModelId, filter);
		List<GDPRQualitativeQuestionViewDTO> gdprQuestionList = buildGdprQualitativeQuestionViewDTOList(questions);
		GDPRQuestionsView result = new GDPRQuestionsView((Organizations) null, filter.getMetricDomain(), gdprQuestionList);

		return result;
	}

	/**
	 * Queue KeeIO Qualitative Question Answers Analytics
	 *
	 * @return Qualitative Questions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/analytics/queue-all-data", name = "Queue Qualitative Question Answers List for current Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ItemViewDTO queueAnalyticsForQualitativeAnswers() {

		qualitativeQuestionService.queueAllKeenIOQuestionAnswers();

		ItemViewDTO result = new ItemViewDTO(1l, "OK");

		return result;
	}

	/**
	 * Get Qualitative Question details
	 *
	 * @return Qualitative Question Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Qualitative Question details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_QUESTION_READ)")
	public QualitativeQuestionEditDTO getDetails(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		QualitativeQuestionEditDTO itemDTO = qualitativeQuestionService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Qualitative Question
	 *
	 * @return New Qualitative Question
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Qualitative Question", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_QUESTION_CREATE)")
	@Transactional
	public QualitativeQuestionEditDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Qualitative Question Details", required = true) @RequestBody QualitativeQuestionEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);

		QualitativeQuestionEditDTO result = qualitativeQuestionService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Qualitative Question
	 *
	 * @return Updated Qualitative Question
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Qualitative Question", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_QUESTION_UPDATE)")
	@Transactional
	public QualitativeQuestionEditDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "User update Details", required = true) @RequestBody QualitativeQuestionEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		QualitativeQuestionEditDTO result = qualitativeQuestionService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Qualitative Question
	 *
	 * @return ID of removed Qualitative Question
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Qualitative Question", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_QUESTION_DELETE)")
	@Transactional
	public Long delete(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Simple Qualitative Question Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = qualitativeQuestionService.delete(itemDTO.getId());

		return result;
	}

	private List<GDPRQualitativeQuestionViewDTO> buildGdprQualitativeQuestionViewDTOList(List<QualitativeQuestionWithAnswersViewDTO> questions) {
		List<GDPRQualitativeQuestionViewDTO> gdprQuestionList = new ArrayList<>();
		List<Long> questionIdsList = questions.stream().map(resultItem -> resultItem.getId()).collect(Collectors.toList());
		Set<GDPRArticleToQuestion> articleToQuestions = new HashSet<>();
		if (questionIdsList != null && questionIdsList.size() > 0) {
			articleToQuestions = gdprArticleToQuestionRepository.getAllByOrganizationAndQuestions(organizationService.getCurrentOrganizationId(), questionIdsList);
			Map<Long, GDPRArticleToQuestion> articleToQuestionsMap = articleToQuestions.stream().collect(Collectors.toMap(resultItem -> resultItem.getQuestionId(), resultItem -> resultItem));
			for (QualitativeQuestionWithAnswersViewDTO resultItem : questions) {
				GDPRQualitativeQuestionViewDTO item = GDPRQualitativeQuestionViewDTO.of(resultItem);
				gdprQuestionList.add(item);
				if (articleToQuestionsMap.containsKey(resultItem.getId())) {
					GDPRArticleToQuestion gdprArticleToQuestion = articleToQuestionsMap.get(resultItem.getId());
					if (gdprArticleToQuestion.getArticle() != null) item.applyArticle(new GDPRArticleItemDTO(gdprArticleToQuestion.getArticle()));
				}
			}
		}
		return gdprQuestionList;
	}

}
