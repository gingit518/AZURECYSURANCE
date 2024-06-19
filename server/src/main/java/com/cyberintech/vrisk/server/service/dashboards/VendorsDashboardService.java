package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.dto.dashboards.elements.DashboardGridLayoutDTO;
import com.cyberintech.vrisk.server.model.dto.dashboards.elements.RichDashboardElementDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.service.BusinessUnitService;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.QuantMetricsService;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Vendor Status Dashboard Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-18
 */
@Service
@Slf4j
public class VendorsDashboardService extends DashboardServiceBase {

	public static final Long VENDOR_STATUSES = 45202L;

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private QuestionAnswersForVendorRepository questionAnswersForVendorRepository;

	@Autowired
	private QualitativeQuestionAnswerRepository qualitativeQuestionAnswerRepository;

	@Autowired
	private QualitativeQuestionRepository qualitativeQuestionRepository;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private QuantMetricsService quantMetricsService;

	@Autowired
	private ExposureMetricsDashboardService exposureMetricsDashboardService;

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO buildVendorStatusDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {

		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_VENDOR_STATUS, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$DESCRIPTION), DashboardType.Admin);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.VENDOR_CYBER_RISK_MANAGER(clientMessage);

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<String> roles = Arrays.asList(RoleType.SYSTEM_OWNER.role(), RoleType.VENDOR_OWNER.role());
		// List<Users> ownersList = userRepository.filterUsersByOrganizationAndNameAndRoles(riskModel.getOrganizationId(), "", roles, Arrays.asList(0L), PageRequest.of(0, 1000000));
		List<Organizations> vendorList = organizationRepository.getListForRootOrganization(riskModel.getOrganizationId(), OrganizationType.Vendor);
		Set<Organizations> vendorsSet = vendorList.stream().collect(Collectors.toSet());
		List<AssociateVendors> allAssociateVendorsList = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		Map<Organizations, List<AssociateVendors>> associateVendorMap = allAssociateVendorsList.stream().filter(associateVendors -> associateVendors.getVendor() != null)
			.collect(Collectors.groupingBy(associateVendors -> associateVendors.getVendor()));

		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO(35200L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$ITEM_DESCRIPTION));
		dashboard.getSections().add(section);

		// Create breadcrumbs
		section.setBreadcrumbs(breadcrumbsTop.extend("VENDOR_TYPES", SLCT.DASHBOARDS$VENDOR_STATUS$NAME, "").getBreadcrumbs());


		// Add download button
		DashboardItemDTO downloadButton = buildDownloadButtonDashboardItemDTO(riskModelId, DashboardsConfig.DASHBOARD_VENDOR_STATUS_REPORT, 45701L);
		section.getDashboardItems().add(downloadButton);

		// Initialize Organization Summary Scores
		DashboardDataGridItemDTO dashboardItem2 = new DashboardDataGridItemDTO(VENDOR_STATUSES, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_STATUSES$ITEM_NAME));
		dashboardItem2.getGridHeaders().add(
			Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_STATUSES$PERSON_HEADER), 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_STATUSES$VENDOR_NAME_HEADER), 1L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_EXPOSURES$QUANT_SCORES$SYSTEM_HEADER), 2L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_STATUSES$CLOUD_HEADER), 3L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_STATUSES$BUSINESS_UNIT_HEADER), 4L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_STATUSES$TECHNOLOGY_HEADER), 5L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_STATUSES$SYSTEM_HEADER), 6L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_STATUSES$SERVICE_HEADER), 7L)
			)
		);
		section.getDashboardItems().add(dashboardItem2);
		String yesValue = clientMessage.getMessage(SLCT.DASHBOARD_VALUES$YES);
		String noValue = clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NO);
		int i = 0;
		for (Organizations vendor : vendorsSet) {
			List<DashboardDataItemDTO>  itemsList = new ArrayList<>();

			String ownerName = vendor.getOwner() != null ? vendor.getOwner().getFullName() : "";
			DashboardDataItemDTO ownerCell = sI(ownerName);
			if (vendor.getOwner() != null) {
				ownerCell.applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null));
			}
			itemsList.add(ownerCell);
			itemsList.add(sI(vendor.getName()));
			if (associateVendorMap.containsKey(vendor)) {
				List<AssociateVendors> associateVendor = associateVendorMap.get(vendor);
				if (associateVendor.get(0) != null && associateVendor.get(0).getSystems().size() > 0) {
					itemsList.add(sI(associateVendor.get(0).getSystems().stream().map(Systems::getName).collect(Collectors.joining("; "))));
				} else {
					itemsList.add(sI(""));
				}
			} else {
				itemsList.add(sI(""));
			}
			itemsList.add(sI(Boolean.TRUE.equals(vendor.getIsCloudVendor()) ? yesValue : noValue));
			if (vendor.getOwner() != null && vendor.getOwner().getBusinessUnit() != null) {
				itemsList.add(sI(businessUnitService.getBusinessUnitPath(vendor.getOwner().getBusinessUnit(), true)).applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null)));
			} else {
				itemsList.add(sI(""));
			}

			itemsList.add(sI(Boolean.TRUE.equals(vendor.getIsTechnologyVendor()) ? yesValue : noValue));
			itemsList.add(sI(Boolean.TRUE.equals(vendor.getIsSystemVendor()) ? yesValue : noValue));
			itemsList.add(sI(Boolean.TRUE.equals(vendor.getIsServiceVendor()) ? yesValue : noValue));

			i++;
			dashboardItem2.getGridItems().add(itemsList);
		}

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO buildVendorSelfAssessmentDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {
		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_VENDOR_SELF_ASSESSMENT, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$DESCRIPTION), DashboardType.Admin);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.VENDOR_CYBER_RISK_MANAGER(clientMessage).add("VENDOR_SELF_ASSESSMENT", "DASHBOARDS$VENDOR_SELF_ASSESSMENT$NAME", "/private/dashboards/1001");

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(35301L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$SERVICE_PROVIDER$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$SERVICE_PROVIDER$ITEM_DESCRIPTION));
		dashboard.getSections().add(section1);

		DashboardSectionDTO section2 = new DashboardSectionDTO(35302L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$SYSTEM_TECHNOLOGY_PROVIDER$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$SYSTEM_TECHNOLOGY_PROVIDER$ITEM_DESCRIPTION));
		dashboard.getSections().add(section2);

		DashboardSectionDTO section3 = new DashboardSectionDTO(35303L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$CLOUD_SERVICE_PROVIDER$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$CLOUD_SERVICE_PROVIDER$ITEM_DESCRIPTION));
		dashboard.getSections().add(section3);

		// Create breadcrumbs
		section1.setBreadcrumbs(breadcrumbsTop.extend("VENDOR_SELF_ASSESSMENT_1", SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$SERVICE_PROVIDER$ITEM_NAME, "").getBreadcrumbs());
		section2.setBreadcrumbs(breadcrumbsTop.extend("VENDOR_SELF_ASSESSMENT_2", SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$SYSTEM_TECHNOLOGY_PROVIDER$ITEM_NAME, "").getBreadcrumbs());
		section3.setBreadcrumbs(breadcrumbsTop.extend("VENDOR_SELF_ASSESSMENT_3", SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$CLOUD_SERVICE_PROVIDER$ITEM_NAME, "").getBreadcrumbs());

		List<QualitativeQuestions> allQuestionsList = qualitativeQuestionRepository.getListOfInternalByRiskModelIdAndTypes(riskModelId, Arrays.asList(VendorType.VendorInternal, VendorType.CloudInternal));
		List<QualitativeQuestions> serviceProviderQuestions = allQuestionsList.stream().filter(question -> Boolean.TRUE.equals(question.getIsServiceVendor()) && (VendorType.VendorInternal.equals(question.getVendorType()))).collect(Collectors.toList());
		List<QualitativeQuestions> systemProviderQuestions = allQuestionsList.stream().filter(question -> (Boolean.TRUE.equals(question.getIsSystemVendor()) || Boolean.TRUE.equals(question.getIsTechnologyVendor())) && (VendorType.VendorInternal.equals(question.getVendorType()))).collect(Collectors.toList());
		List<QualitativeQuestions> serviceProviderCloudQuestions = allQuestionsList.stream().filter(question -> Boolean.TRUE.equals(question.getIsServiceVendor()) && (VendorType.CloudInternal.equals(question.getVendorType()))).collect(Collectors.toList());

		List<Long> allQuestionIds = allQuestionsList.stream().map(QualitativeQuestions::getId).collect(Collectors.toList());
		if (allQuestionIds.size() < 1) allQuestionIds.add(0l); // fix for empty questions list
		List<QuestionAnswersForVendor> allAnswers = questionAnswersForVendorRepository.getListByRiskModelAndQuestions(riskModelId, allQuestionIds);

		Map<Long, List<QuestionAnswersForVendor>> allAnswersByQuestion = allAnswers.stream().collect(Collectors.groupingBy(answer -> answer.getQuestion().getId()));

		buildSelfAssessmentDashboard(serviceProviderQuestions, allAnswersByQuestion, section1);
		buildSelfAssessmentDashboard(systemProviderQuestions, allAnswersByQuestion, section2);
		buildSelfAssessmentDashboard(serviceProviderCloudQuestions, allAnswersByQuestion, section3);

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO buildVendorAndSystemsDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {
		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_VENDOR_SELF_ASSESSMENT, clientMessage.getMessage(SLCT.DASHBOARDS$VENDORS_AND_SYSTEMS$NAME), "Vendors and Systems Dashboard", DashboardType.Vendor);

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Double organizationInsuranceLimit = Optional.ofNullable(organizationService.getCurrentOrganizationEntity().getInsuranceLimit()).orElse(Organizations.INSURANCE_LIMIT);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_EXECUTIVE(clientMessage);

		// Create Initial Sections
		// TODO Translate
		DashboardSectionDTO section1 = new DashboardSectionDTO(35701L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDORS_AND_SYSTEMS$VENDORS_AND_SYSTEMS$ITEM_NAME), "Vendors and Systems Dashboard");
		section1.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_VENDORS_AND_SYSTEMS", SLCT.DASHBOARDS$VENDORS_AND_SYSTEMS$NAME, "").getBreadcrumbs());
		dashboard.getSections().add(section1);

		// Add download button
		// DashboardItemDTO downloadButton = buildDownloadButtonDashboardItemDTO(riskModelId, DashboardsConfig.DASHBOARD_EXPOSURE_RISK_REPORT, 45721L);
		DashboardItemDTO downloadButton = buildDownloadButtonDashboardItemDTO(riskModelId, DashboardsConfig.DASHBOARD_VENDOR_AND_SYSTEM_REPORT, 45723L);
		section1.getDashboardItems().add(downloadButton);

		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(35751L, "Vendors and Systems");
		section1.getDashboardItems().add(dashboardItem);

		List<AssociateVendors> allVendorsList = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemExposureMetricsMap = exposureMetricsDashboardService.getSystemsScoringData(riskModelId);
		// Map<Organizations, List<AssociateVendors>> assocVendorsByVendor = allVendorsList.stream().filter(associateVendors -> associateVendors.getVendor() != null && associateVendors.getSystems() != null).collect(Collectors.groupingBy(AssociateVendors::getVendor));

		List<String> headers = new ArrayList<>();
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$VENDOR_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$VENDOR_TYPE_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$SYSTEM_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$BUSINESS_UNIT_HEADER));
		// headers.add("ETL?");
		headers.add("Data Types");
		headers.add("NAIC");
		headers.add("PII");
		headers.add("PCI");
		headers.add("HIPAA");
		headers.add("CCPA");
		headers.add("Total Exposure");
		dashboardItem.addGridHeaders(headers, false);

		for (AssociateVendors associateVendor: allVendorsList) {

			Organizations vendor = associateVendor.getVendor();
			int currentSystem = 0;
			long systemsCount = associateVendor.getSystems().size();
			for (Systems system: associateVendor.getSystems()) {

				// Build Total Exposure
				Double totalExposure = 0d;
				Map<QuantMetrics, ExposureMetricResult> systemMetricDataMap = systemExposureMetricsMap.get(system);
				if (systemMetricDataMap != null) {
					for (Map.Entry<QuantMetrics, ExposureMetricResult> systemMetricEntry : systemMetricDataMap.entrySet()) {
						QuantMetrics quantMetric = systemMetricEntry.getKey();
						ExposureMetricResult exposureMetricResult = systemMetricEntry.getValue();
						double exposure = exposureMetricResult != null && exposureMetricResult.getResult() != null ? exposureMetricResult.getResult() : 0d;
						totalExposure += exposure;
					}
				}

				List<DashboardDataItemDTO> rowItems = new ArrayList<>();
				if (currentSystem == 0) {
					rowItems.add(sI(associateVendor.getVendor().getName()).applyRowspan(systemsCount).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(associateVendor.getVendor(), null)));
					rowItems.add(sI(String.join(", ", buildVendorTypesString(vendor))).applyRowspan(systemsCount));
				}

				rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));
				rowItems.add(sI(businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true)).applyTextAlign("left"));
				// rowItems.add(sI(Boolean.TRUE.equals(system.getIsEtl()) ? "YES" : "NO").applyTextAlign("left"));

				String dataTypes = system.getDataTypeClassifications().stream().map(DataTypeClassification::getName).collect(Collectors.joining(", "));
				Set<Long> itemIds = system.getDataTypeClassifications().stream().mapToLong(DataTypeClassification::getId).boxed().collect(Collectors.toSet());

				// Create Total Exposure
				DashboardDataItemDTO totalExposureCell = $I(totalExposure).applyTextAlign("right").round(0);
				if (totalExposure > organizationInsuranceLimit) {
					totalExposureCell.applyBackgroundColor("#FB5100");
				}

				rowItems.add(sI(dataTypes).applyTextAlign("left"));
				rowItems.add(sI("").applyTextAlign("left"));
				rowItems.add(sI(itemIds.contains(DataTypeDomain.PII.getId()) ? "YES" : "").applyTextAlign("left"));
				rowItems.add(sI(itemIds.contains(DataTypeDomain.CREDIT_CARD.getId()) ? "YES" : "").applyTextAlign("left"));
				rowItems.add(sI(itemIds.contains(DataTypeDomain.HEALTHCARE.getId()) ? "YES" : "").applyTextAlign("left"));
				rowItems.add(sI(itemIds.contains(DataTypeDomain.FINANCIAL.getId()) ? "YES" : "").applyTextAlign("left"));
				rowItems.add(totalExposureCell);

				dashboardItem.getGridItems().add(rowItems);
				currentSystem++;
			}

		}

		return dashboard;
	}

	private List<String> buildVendorTypesString(Organizations vendor) {
		List<String> vendorTypes = new ArrayList<>();
		if (vendor.getIsCloudVendor() != null && vendor.getIsCloudVendor()) {
			vendorTypes.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$CLOUD_VENDOR_TYPE));
		}
		if (vendor.getIsServiceVendor() != null && vendor.getIsServiceVendor()) {
			vendorTypes.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$SERVICE_VENDOR_TYPE));
		}
		if (vendor.getIsTechnologyVendor() != null && vendor.getIsTechnologyVendor()) {
			vendorTypes.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$TECHNOLOGY_VENDOR_TYPE));
		}
		if (vendor.getIsSystemVendor() != null && vendor.getIsSystemVendor()) {
			vendorTypes.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$SYSTEM_VENDOR_TYPE));
		}
		return vendorTypes;
	}

	/**
	 * Build list of values
	 *
	 * @param questions
	 * @param allAnswersByQuestion
	 * @param section
	 */
	public void buildSelfAssessmentDashboard(List<QualitativeQuestions> questions, Map<Long, List<QuestionAnswersForVendor>> allAnswersByQuestion, DashboardSectionDTO section) {

		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(35321l, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$TOTAL_SCORE$ITEM_NAME));
		section.getDashboardItems().add(dashboardItem);
		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$TOTAL_SCORE$QUESTION_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$TOTAL_SCORE$MARK_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_SELF_ASSESSMENT$TOTAL_SCORE$SCORE_HEADER)
		));
		Map<QualitativeQuestions, Map<QualitativeQuestionAnswers, Double>> questionAnswersStats = new HashMap<>();
		for (QualitativeQuestions question : questions) {
			double totalAnswersCount = question.getAnswers().size();

			// Skip Question without answers
			if (totalAnswersCount < 1) {
				continue;
			}

			List<DashboardDataItemDTO>  itemsList = new ArrayList<>();
			itemsList.add(sI(question.getQuestion()));

			// Get questions statistics
			questionAnswersStats.put(question, new HashMap<>());
			for (QualitativeQuestionAnswers answer : question.getAnswers()) {
				questionAnswersStats.get(question).put(answer, 0d);
			}

			Double itemValue = 0d;
			double itemsCount = 0d;
			String itemLetter = "-";
			if (allAnswersByQuestion.containsKey(question.getId())) {
				List<QuestionAnswersForVendor> answers = allAnswersByQuestion.get(question.getId());
				// itemValue = 0d;
				for (QuestionAnswersForVendor answer : answers) {
					if (answer.getAnswer() != null) {
						itemValue += answer.getAnswer().getAnswerWeight() != null ? answer.getAnswer().getAnswerWeight().getValue() : 0d;
						itemsCount++;

						questionAnswersStats.get(question).put(answer.getAnswer(), questionAnswersStats.get(question).get(answer.getAnswer()) + 1);
					}
				}

				double answersCount = answers.size();
				if (answersCount > 0) {
					itemValue = 100 * (itemValue / (answersCount));
				}

			}

			itemsList.add(sI(getLetterByNumber(itemValue)).applyTextAlign("right"));
			itemsList.add(
				sI(itemValue).applyTextAlign("right").applyBackgroundColor(getLetterColourByNumber(itemValue))
					.applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.VENDOR, DashboardDataItemDrilldownDTO.CATEGORY_VENDOR_SELF_ASSESSMENT, question.getId().toString()))
			);

			dashboardItem.getGridItems().add(itemsList);
		}

		// Build Charts List
		DashboardGridLayoutDTO dashboardGridLayout = new DashboardGridLayoutDTO();
		section.getDashboardItems().add(dashboardGridLayout);

		for (Map.Entry<QualitativeQuestions, Map<QualitativeQuestionAnswers, Double>> questionAnswersSet : questionAnswersStats.entrySet()) {
			QualitativeQuestions question = questionAnswersSet.getKey();
			Map<QualitativeQuestionAnswers, Double> questionAnswerStats = questionAnswersSet.getValue();

			// Skipping empty items
			if (questionAnswerStats.size() < 1) continue;

			DashboardChartItemDTO chartItem = getVendorSelfAssessmentDashboardChartItemDTO(questionAnswerStats);
			chartItem.getParameters().put("height", 400L);
			chartItem.getParameters().put("width", 400L);

			// Add items to Dashboard Layout
			RichDashboardElementDTO chartText = new RichDashboardElementDTO(new DashboardItemDTO(1000000L + question.getId(), question.getQuestion(), null, DashboardItemType.Text));
			RichDashboardElementDTO chartElement = new RichDashboardElementDTO(chartItem);
			dashboardGridLayout.addRowItems(chartText, chartElement);

		}

	}

	private DashboardChartItemDTO getVendorSelfAssessmentDashboardChartItemDTO(Map<QualitativeQuestionAnswers, Double> questionAnswerStats) {
		double totalAnswersCount = questionAnswerStats.entrySet().stream().mapToDouble(item -> item.getValue()).sum();
		if (totalAnswersCount == 0) totalAnswersCount = 1d;

		// Initialize Section 1
		DashboardChartItemDTO chartItem = new DashboardChartItemDTO(1l, "", "", DashboardItemType.PieChart);
		chartItem.setXAxis("Scores");
		chartItem.setYAxis("Question");
		chartItem.getParameters().put("isSortable", false);
		// section.getDashboardItems().add(chartItem);

		// Create
		int i = 0;
		for (Map.Entry<QualitativeQuestionAnswers, Double> answerStat : questionAnswerStats.entrySet()) {
			List<DashboardDataItemDTO> chartItems = Arrays.asList(
				sI(answerStat.getKey().getAnswer()).applyBackgroundColor(getColorByNumber(++i))
					.applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.VENDOR, DashboardDataItemDrilldownDTO.CATEGORY_VENDOR_SELF_ASSESSMENT, answerStat.getKey().getQualitativeQuestion().getId().toString())),
				$I(100 * answerStat.getValue() / totalAnswersCount, "%").round(2)
			);
			chartItem.getGridItems().add(chartItems);
		}
		return chartItem;
	}

	/**
	 * Build Vendor drilldown
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	public void buildVendorDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		String category = drilldown.getParams().get(DashboardDataItemDrilldownDTO.PARAM_CATEGORY);
		if (DashboardDataItemDrilldownDTO.CATEGORY_VENDOR_SELF_ASSESSMENT.equals(category)) {
			buildVendorSelfAssessmentDrilldown(drilldown, riskModelId, dashboard);
		}
	}

	/**
	 * Build Vendor drilldown
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	protected void buildVendorSelfAssessmentDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

		String itemIdString = drilldown.getParams().get(DashboardDataItemDrilldownDTO.PARAM_ITEM);
		Long itemId = Long.parseLong(itemIdString);
		// Optional<QualitativeQuestionAnswers> answer = qualitativeQuestionAnswerRepository.findById(itemId);
		// QualitativeQuestions question = answer.get().getQualitativeQuestion();
		QualitativeQuestions question = qualitativeQuestionRepository.findById(itemId).get();

		// Define Header Information
		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR$ITEM_NAME), question.getQuestion()));

		// Build data
		List<QuestionAnswersForVendor> allAnswers = questionAnswersForVendorRepository.getListByRiskModelAndQuestions(riskModelId, Arrays.asList(question.getId()));

		Map<QualitativeQuestionAnswers, Double> questionAnswersStats = new HashMap<>();
		double totalAnswersCount = allAnswers.size() > 0 ? allAnswers.size() : 1d;
		double itemValue = 0d;
		for (QuestionAnswersForVendor currentVendorAnswer : allAnswers) {
			QualitativeQuestionAnswers currentAnswer = currentVendorAnswer.getAnswer();
			if (currentAnswer != null) {
				if (!questionAnswersStats.containsKey(currentAnswer)) questionAnswersStats.put(currentAnswer, 0d);
				itemValue += currentAnswer.getAnswerWeight() != null ? currentAnswer.getAnswerWeight().getValue() : 0d;
				questionAnswersStats.put(currentAnswer, questionAnswersStats.get(currentAnswer) + 1);
			}
		}
		DashboardChartItemDTO chartItem = getVendorSelfAssessmentDashboardChartItemDTO(questionAnswersStats);
		chartItem.getParameters().put("height", 720L);
		chartItem.getParameters().put("width", 720L);
		chartItem.setName(question.getQuestion());
		section1.getDashboardItems().add(chartItem);

		// Total question statistics
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(35401L, clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR$TOTAL_STATISTICS$ITEM_NAME));
		section1.getDashboardItems().add(dashboardItem);
		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR$TOTAL_STATISTICS$ANSWER_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR$TOTAL_STATISTICS$WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR$TOTAL_STATISTICS$COUNT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR$TOTAL_STATISTICS$PERCENT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR$TOTAL_STATISTICS$VALUE_HEADER)
		));
		for (QualitativeQuestionAnswers currentAnswer : question.getAnswers()) {
			double answerWeight = currentAnswer.getAnswerWeight().getValue();
			double answersCount = questionAnswersStats.containsKey(currentAnswer) ? questionAnswersStats.get(currentAnswer) : 0d;
			double percent = 100 * answersCount / totalAnswersCount;

			List<DashboardDataItemDTO>  itemsList = Arrays.asList(
				sI(currentAnswer.getAnswer()),
				sI(answerWeight).applyTextAlign("right"),
				sI(answersCount).applyTextAlign("right"),
				$I(percent, "%").round(2).applyTextAlign("right"),
				sI(answersCount * answerWeight).applyTextAlign("right")
			);

			dashboardItem.getGridItems().add(itemsList);
		}
		double score = 100 * (itemValue / totalAnswersCount);
		String itemLetter = getLetterByNumber(score);
		dashboardItem.getGridItems().add(Arrays.asList(sI("Total:").applyColspan(4l).applyTextAlign("right"), sI(itemValue).applyTextAlign("right")));
		dashboardItem.getGridItems().add(Arrays.asList(sI("Status:").applyColspan(4l).applyTextAlign("right"), sI(itemLetter).applyTextAlign("right")));
		dashboardItem.getGridItems().add(Arrays.asList(sI("Score:").applyColspan(4l).applyTextAlign("right"), sI(score).applyTextAlign("right").applyBackgroundColor(getLetterColourByNumber(score))));

		// Detailed question statistics
		DashboardDataGridItemDTO dashboardItem2 = new DashboardDataGridItemDTO(35401L, clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR$DETAILED_STATISTICS$ITEM_NAME));
		dashboardItem2.getGridHeaders().add(
			Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR$DETAILED_STATISTICS$VENDOR_HEADER), 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR$DETAILED_STATISTICS$ANSWER_HEADER), 1L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR$DETAILED_STATISTICS$WEIGHT_HEADER), 2L)
			)
		);
		for (QuestionAnswersForVendor currentVendorAnswer : allAnswers) {
			dashboardItem2.getGridItems().add(
				Arrays.asList(
					sI(currentVendorAnswer.getVendor().getName()),
					sI(currentVendorAnswer.getAnswer().getAnswer()),
					sI(currentVendorAnswer.getAnswer().getAnswerWeight().getValue().doubleValue()).applyTextAlign("right")
				)
			);
		}
		section1.getDashboardItems().add(dashboardItem2);
	}

	@Deprecated
	public DashboardItemDTO createVendorQuantScoresDashboard(Long riskModelId, List<VendorDataSeries> quantVendorScores, Map<Long, DashboardDataItemDTO> heatChartItemsMap) {
		// Total Quantification Scores
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(2l, "Vendor Exposures");
		boolean isGDPRRegulatoryQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE);

		List<String> headers = new ArrayList<>();
		headers.add("Vendor");
		headers.add("Data Exfiltration");
		headers.add("Business Interruption");
		if (isGDPRRegulatoryQuantDefined) headers.add("GDPR Regulatory Exposure");
		headers.add("Total Exposure");
		int totalItemPosition = isGDPRRegulatoryQuantDefined ? 3 : 2;

		dashboardItem.addGridHeaders(headers, true);
		for (VendorDataSeries dataSeries : quantVendorScores) {
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(dataSeries.getVendor().getName()));
			rowItems.addAll(dataSeries.getItems().stream().map(item -> $I(item, "$").round(0)).collect(Collectors.toList()));
			dashboardItem.getGridItems().add(rowItems);
			applyVendorDashboardQuantsDrilldown(rowItems, dataSeries.getVendor());

			// Apply Vendor Radius based on Quantification data
			DashboardDataItemDTO heatChartItem = heatChartItemsMap.get(dataSeries.getVendor().getId());
			if (heatChartItem != null) {
				Double radiusFactor = (dataSeries.getItems().get(totalItemPosition) > 1000) ? Math.log(dataSeries.getItems().get(totalItemPosition)) : 4d;
				Long radius = Math.round(radiusFactor * radiusFactor * radiusFactor);
				if (radius > 16l) heatChartItem.getParams().put("radius", radius.toString());
				heatChartItem.getParams().put("totalExposure", sRound(dataSeries.getItems().get(totalItemPosition)));

				heatChartItem.setDrilldown(DashboardDataItemDrilldownDTO.ofQuant(dataSeries.getVendor(), null));
			}
		}

		return dashboardItem;
	}

	public DashboardItemDTO createVendorQuantScoresDashboard(RiskModels riskModel, List<Long> dataTypes, Map<Long, DashboardDataItemDTO> heatChartItemsMap) {
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(2l, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$ITEM_NAME));

		List<QuantsDomain> metricsDomains = Arrays.asList(QuantsDomain.DATA_EXFILTRATION, QuantsDomain.BUSINESS_INTERRUPTION, QuantsDomain.GDPR_REGULATORY_EXPOSURE);
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap = exposureMetricsDashboardService.getSystemsScoringData(riskModel.getId(), null, metricsDomains);
		List<AssociateVendors> allVendorsList;
		if (dataTypes != null) {
			allVendorsList = associateVendorRepository.getListForOrganizationAndSystemDataTypes(riskModel.getOrganizationId(), dataTypes);
		} else {
			allVendorsList = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		}

		boolean isGDPRRegulatoryQuantDefined = quantMetricsService.isQuanDefined(riskModel.getId(), QuantsDomain.GDPR_REGULATORY_EXPOSURE);

		List<String> headers = new ArrayList<>();
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$VENDOR_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$VENDOR_TYPE_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$DATA_EXFILTRATION_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$BUSINESS_INTERRUPTION_HEADER));
		if (isGDPRRegulatoryQuantDefined) headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$GDRP_REGULATORY_EXPOSURE_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$TOTAL_EXPOSURE_HEADER));

		dashboardItem.addGridHeaders(headers, true);
		for (AssociateVendors associateVendor: allVendorsList) {
			Organizations vendor = associateVendor.getVendor();
			Double dataExfiltration = 0D;
			Double businessInterruption = 0D;
			Double gdprRegulatoryExposure = 0D;
			Double totalExposure = 0D;

			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(associateVendor.getVendor().getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(associateVendor.getVendor(), null)));

			List<String> vendorTypes = buildVendorTypesString(vendor);
			rowItems.add(sI(String.join(", ", vendorTypes)));

			for (Systems system: associateVendor.getSystems()) {
				Map<QuantMetrics, ExposureMetricResult> vendorMetricDataMap = Optional.ofNullable(systemScoringDataMap.get(system)).orElse(new HashMap<>());
				for (Map.Entry<QuantMetrics, ExposureMetricResult> entry : vendorMetricDataMap.entrySet()) {
					ExposureMetricResult exposureMetricResult = entry.getValue();
					QuantMetrics quantMetric = entry.getKey();
					if (QuantsDomain.DATA_EXFILTRATION.getId().equals(quantMetric.getQuant().getId())) dataExfiltration += exposureMetricResult.getResult();
					if (QuantsDomain.BUSINESS_INTERRUPTION.getId().equals(quantMetric.getQuant().getId())) businessInterruption += exposureMetricResult.getResult();
					if (QuantsDomain.GDPR_REGULATORY_EXPOSURE.getId().equals(quantMetric.getQuant().getId())) gdprRegulatoryExposure += exposureMetricResult.getResult();
				}
			}
			totalExposure += dataExfiltration + businessInterruption + gdprRegulatoryExposure;

			rowItems.add($I(dataExfiltration).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(vendor, QuantsDomain.DATA_EXFILTRATION)));
			rowItems.add($I(businessInterruption).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(vendor, QuantsDomain.BUSINESS_INTERRUPTION)));
			if (isGDPRRegulatoryQuantDefined) {
				rowItems.add($I(gdprRegulatoryExposure).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(vendor, QuantsDomain.GDPR_REGULATORY_EXPOSURE)));
			}
			rowItems.add($I(totalExposure).round(0));

			if (heatChartItemsMap != null) {
				DashboardDataItemDTO heatChartItem = heatChartItemsMap.get(vendor.getId());

				if (heatChartItem != null) {
					Double radiusFactor = (totalExposure > 1000) ? Math.log(totalExposure) : 4d;
					Long radius = Math.round(radiusFactor * radiusFactor * radiusFactor);
					if (radius > 16L) heatChartItem.getParams().put("radius", radius.toString());
					heatChartItem.getParams().put("totalExposure", sRound(totalExposure));
					heatChartItem.setDrilldown(DashboardDataItemDrilldownDTO.ofQuant(vendor, null));
				}
			}

			dashboardItem.getGridItems().add(rowItems);
		}

		return dashboardItem;
	}

	public String getLetterByNumber(double itemValue) {
		String itemLetter = "-";
		if (itemValue > 900) {
			itemLetter = "+A";
		} else if (itemValue > 800) {
			itemLetter = "A";
		} else if (itemValue > 700) {
			itemLetter = "-A";
		} else if (itemValue > 600) {
			itemLetter = "B";
		} else if (itemValue > 500) {
			itemLetter = "-B";
		} else if (itemValue > 400) {
			itemLetter = "C";
		} else if (itemValue > 300) {
			itemLetter = "-C";
		} else if (itemValue > 200) {
			itemLetter = "D";
		} else if (itemValue > 100) {
			itemLetter = "-D";
		} else if (itemValue > 0) {
			itemLetter = "F";
		}

		return itemLetter;
	}

	public String getLetterColourByNumber(double itemValue) {
		String itemLetter = null;

		if (itemValue > 900) {
			itemLetter = "#529985";
		} else if (itemValue > 800) {
			itemLetter = "#689d72";
		} else if (itemValue > 700) {
			itemLetter = "#87a75f";
		} else if (itemValue > 600) {
			itemLetter = "#a3b457";
		} else if (itemValue > 500) {
			itemLetter = "#e2ca49";
		} else if (itemValue > 400) {
			itemLetter = "#e9c54a";
		} else if (itemValue > 300) {
			itemLetter = "#df9a4f";
		} else if (itemValue > 200) {
			itemLetter = "#c26b51";
		} else if (itemValue > 100) {
			itemLetter = "#ff9640";
		} else if (itemValue > 50) {
			itemLetter = "#fb5100";
		} else {
			itemLetter = "#ff0000";
		}

		return itemLetter;
	}

	public String getColorByNumber(int colorNum) {
		String result = "#213c60";
		switch (colorNum) {
			case 0:
				result = "#FF9640";
				break;
			case 1:
				result = "#FFAE40";
				break;
			case 2:
				result = "#6C74AE";
				break;
			case 3:
				result = "#4A4A4A";
				break;
			case 4:
				result = "#337ab7";
				break;
			case 5:
				result = "#FF7400";
				break;
			case 6:
				result = "#FFC000";
				break;
			case 7:
				result = "#FFD040";
				break;
			case 8:
				result = "#FFDD73";
				break;
			case 9:
				result = "#FB5100";
				break;
			case 10:
				result = "#FDBB30";
				break;
		}

		return result;
	}

	/**
	 * Get Dashboard Report
	 *
	 * @return Dashboard
	 */
	public ByteArrayOutputStream buildReport(Long riskModelId) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		try {
			CSVPrinter csvPrinter = getReportCsvPrinter(result);

			RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

			List<Organizations> vendorList = organizationRepository.getListForRootOrganization(riskModel.getOrganizationId(), OrganizationType.Vendor);
			Set<Organizations> vendorsSet = vendorList.stream().collect(Collectors.toSet());

			List<AssociateVendors> allAssociateVendorsList = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
			Map<Organizations, List<AssociateVendors>> associateVendorMap = allAssociateVendorsList.stream().filter(associateVendors -> associateVendors.getVendor() != null)
				.collect(Collectors.groupingBy(associateVendors -> associateVendors.getVendor()));

			for (Organizations vendor : vendorsSet) {

				String businessUnitPath = "";
				if (vendor.getOwner() != null && vendor.getOwner().getBusinessUnit() != null) {
					businessUnitPath = businessUnitService.getBusinessUnitPath(vendor.getOwner().getBusinessUnit(), true);
				}

				// Systems String
				String systemsString = "";
				if (associateVendorMap.containsKey(vendor)) {
					List<AssociateVendors> associateVendor = associateVendorMap.get(vendor);
					if (associateVendor.get(0) != null && associateVendor.get(0).getSystems().size() > 0) {
						systemsString = associateVendor.get(0).getSystems().stream().map(Systems::getName).collect(Collectors.joining("; "));
					}
				}

				csvPrinter.printRecord(
					vendor.getOwner() != null ? vendor.getOwner().getFullName() : "",
					vendor.getOwner() != null ? vendor.getOwner().getEmail() : "",
					vendor.getName(),
					systemsString,
					Boolean.TRUE.equals(vendor.getIsCloudVendor()) ? "YES" : "NO",
					businessUnitPath,
					Boolean.TRUE.equals(vendor.getIsTechnologyVendor()) ? "YES" : "NO",
					Boolean.TRUE.equals(vendor.getIsSystemVendor()) ? "YES" : "NO",
					Boolean.TRUE.equals(vendor.getIsServiceVendor()) ? "YES" : "NO"
				);
			}

			csvPrinter.flush();
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}

		return result;
	}

	private CSVPrinter getReportCsvPrinter(ByteArrayOutputStream result) throws IOException {
		Writer writer = new OutputStreamWriter(result);
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
			"Person",
			"Email",
			"Vendor Name",
			"Systems",
			"Cloud",
			"Business Unit",
			"Is Technology?",
			"Is System?",
			"Is Service?"
		);

		return new CSVPrinter(writer, csvFormat);
	}

	/**
	 * Get Dashboard Report
	 *
	 * @return Dashboard
	 */
	// TODO Translate
	public ByteArrayOutputStream buildVendorAndSystemReport(Long riskModelId) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		try {

			Double organizationInsuranceLimit = Optional.ofNullable(organizationService.getCurrentOrganizationEntity().getInsuranceLimit()).orElse(Organizations.INSURANCE_LIMIT);
			RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

			List<AssociateVendors> allVendorsList = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
			Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemExposureMetricsMap = exposureMetricsDashboardService.getSystemsScoringData(riskModelId);

			// Create Report Headers
			List<String> headers = new ArrayList<>();
			headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$VENDOR_HEADER));
			headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$VENDOR_TYPE_HEADER));
			headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$SYSTEM_HEADER));
			headers.add("System Description");
			headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$BUSINESS_UNIT_HEADER));
			headers.add("System Owner");
			headers.add("Focal Point for the BU");
			headers.add("Digital Asset Classification");
			headers.add("ETL?");
			headers.add("Data Types");
			headers.add("NAIC");
			headers.add("PII");
			headers.add("PCI");
			headers.add("HIPAA");
			headers.add("CCPA");
			headers.add("Total Exposure");
			headers.add("Comments");

			String[] headersArray = new String[headers.size()];
			headers.toArray(headersArray);

			// Create CSV Printer
			Writer writer = new OutputStreamWriter(result);
			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headersArray);
			CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);;

			for (AssociateVendors associateVendor: allVendorsList) {

				Organizations vendor = associateVendor.getVendor();
				int currentSystem = 0;
				long systemsCount = associateVendor.getSystems().size();
				for (Systems system: associateVendor.getSystems()) {

					// Build Total Exposure
					Double totalExposure = 0d;
					Map<QuantMetrics, ExposureMetricResult> systemMetricDataMap = systemExposureMetricsMap.get(system);
					if (systemMetricDataMap != null) {
						for (Map.Entry<QuantMetrics, ExposureMetricResult> systemMetricEntry : systemMetricDataMap.entrySet()) {
							QuantMetrics quantMetric = systemMetricEntry.getKey();
							ExposureMetricResult exposureMetricResult = systemMetricEntry.getValue();
							double exposure = exposureMetricResult != null && exposureMetricResult.getResult() != null ? exposureMetricResult.getResult() : 0d;
							totalExposure += exposure;
						}
					}
					String dataTypes = system.getDataTypeClassifications().stream().map(DataTypeClassification::getName).collect(Collectors.joining(", "));
					Set<Long> itemIds = system.getDataTypeClassifications().stream().mapToLong(DataTypeClassification::getId).boxed().collect(Collectors.toSet());

					List<Object> rowItems = new ArrayList<>();
					if (currentSystem == 0) {
						rowItems.add(associateVendor.getVendor().getName());
						rowItems.add(String.join(", ", buildVendorTypesString(vendor)));
					} else {
						rowItems.add("");
						rowItems.add("");
					}

					rowItems.add(system.getName());
					rowItems.add(system.getDescription());
					rowItems.add(businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true));
					rowItems.add(system.getOwner() != null ? system.getOwner().getEmail() : "");
					rowItems.add(system.getInfosecFocalPerson() != null ? system.getInfosecFocalPerson().getEmail() : "");
					rowItems.add(system.getDataAssetClassification() != null ? system.getDataAssetClassification().getName() : "");
					rowItems.add(Boolean.TRUE.equals(system.getIsEtl()) ? "YES" : "NO");
					rowItems.add(dataTypes);
					rowItems.add("");
					rowItems.add(itemIds.contains(DataTypeDomain.PII.getId()) ? "YES" : "");
					rowItems.add(itemIds.contains(DataTypeDomain.CREDIT_CARD.getId()) ? "YES" : "");
					rowItems.add(itemIds.contains(DataTypeDomain.HEALTHCARE.getId()) ? "YES" : "");
					rowItems.add(itemIds.contains(DataTypeDomain.FINANCIAL.getId()) ? "YES" : "");
					rowItems.add(totalExposure);
					if (totalExposure > organizationInsuranceLimit) {
						rowItems.add(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$EXTRAORDINARY_HIGH_EXPOSURE_RECORD_COUNT));
					} else {
						rowItems.add(""); // Comments
					}

					csvPrinter.printRecord(rowItems);
					currentSystem++;
				}
			}

			csvPrinter.flush();
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}

		return result;
	}

}
