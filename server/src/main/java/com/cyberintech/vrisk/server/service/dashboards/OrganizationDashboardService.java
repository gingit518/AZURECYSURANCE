package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dao.GDPRArticleStatusModelDAO;
import com.cyberintech.vrisk.server.model.dao.GDPRSystemArticleStatusModelDAO;
import com.cyberintech.vrisk.server.model.dao.GDPRSystemStatusModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.GDPRFilter;
import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.dto.dashboards.elements.RichDashboardElementDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleStatusDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPROrganizationStatusDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRSystemArticleStatusDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRSystemStatusDTO;
import com.cyberintech.vrisk.server.model.dto.organization.ElastioOrganizationViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.service.*;
import com.cyberintech.vrisk.server.service.integrations.cysurance.CysuranceIntegrationService;
import com.cyberintech.vrisk.server.service.integrations.cysurance.dto.CysuranceQueryResponseDataEntityRating;
import com.cyberintech.vrisk.server.service.integrations.elastio.ElastioOrganizationService;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Question Status Dashboard Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-02
 */
@Service
@Slf4j
public class OrganizationDashboardService extends DashboardServiceBase {
	@Autowired
	private RegulationRepository regulationRepository;

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private ExposureMetricsDashboardService exposureMetricsDashboardService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private QuantMetricsRepository quantMetricsRepository;

	@Autowired
	private QuantMetricsService quantMetricsService;

	@Autowired
	private ScoringQuestionsDashboardService scoringQuestionsDashboardService;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private GDPRSystemStatusModelDAO gdprSystemStatusModelDAO;

	@Autowired
	private GDPRSystemArticleStatusModelDAO gdprSystemArticleStatusModelDAO;

	@Autowired
	private GDPRArticleStatusModelDAO gdprArticleStatusModelDAO;

	@Autowired
	private GDPRArticleStatusService gdprArticleStatusService;

	@Autowired
	private ElastioOrganizationService elastioOrganizationService;

	@Autowired
	@Qualifier("organizationService")
	private OrganizationService organizationService;
	@Autowired
	private CysuranceIntegrationService cysuranceIntegrationService;

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getCyberInsuranceDashboardDetails(Long riskModelId, Long dashboardId) {
		DashboardDTO dashboard = new DashboardDTO(dashboardId, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$DESCRIPTION), DashboardType.Organization);

		boolean isGDPRRegulatoryQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE);
		boolean isPrivacyQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.PRIVACY_EXPOSURE);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop;
		if (DashboardsConfig.DASHBOARD_CYBER_INSURANCE.equals(dashboardId)) {
			breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_EXECUTIVE(clientMessage).add("DASHBOARD_CYBER_INSURANCE", SLCT.DASHBOARDS$CYBER_INSURANCE$NAME, "/private/dashboards/2000");
		} else {
			breadcrumbsTop = DashboardBreadcrumbsHelper.CFO_DASHBOARD(clientMessage).add("DASHBOARD_CYBER_INSURANCE", SLCT.DASHBOARDS$CYBER_INSURANCE$NAME, "/private/dashboards/2004");
		}

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<Systems> allSystemsList = systemRepository.getAllByOrganizationAndNotEtl(riskModel.getOrganizationId());

		// List<QuantsDomain> quantDomains = Arrays.asList(QuantsDomain.DATA_EXFILTRATION, QuantsDomain.BUSINESS_INTERRUPTION_SUBLIMIT, QuantsDomain.RANSOMWARE_SUBLIMIT, QuantsDomain.GDPR_REGULATORY_EXPOSURE);
		List<QuantsDomain> quantDomains = Arrays.asList(
			QuantsDomain.DATA_EXFILTRATION,
			QuantsDomain.BUSINESS_INTERRUPTION_SUBLIMIT,
			QuantsDomain.RANSOMWARE_SUBLIMIT,
			QuantsDomain.GDPR_REGULATORY_EXPOSURE,
			QuantsDomain.PRIVACY_EXPOSURE
		);
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsDataMap = exposureMetricsDashboardService.getSystemsScoringData(riskModelId, null, quantDomains);

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(2001001L, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$ITEM_NAME), null);
		DashboardSectionDTO section2 = new DashboardSectionDTO(2001002L, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$BUSINESS_INTERRUPTION$ITEM_NAME), null);
		DashboardSectionDTO section3 = new DashboardSectionDTO(2001003L, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$CYBER_EXTORTION$ITEM_NAME), null);
		//DashboardSectionDTO section4 = new DashboardSectionDTO(2001004L, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$CYBER_EXTORTION$ITEM_NAME), null);
		//DashboardSectionDTO section5 = new DashboardSectionDTO(2001005L, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$CYBER_EXTORTION$ITEM_NAME), null);

		dashboard.getSections().add(section1);
		dashboard.getSections().add(section2);
		dashboard.getSections().add(section3);
		//dashboard.getSections().add(section4);
		//dashboard.getSections().add(section5);

		// Create breadcrumbs
		section1.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_CYBER_INSURANCE_1", SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$ITEM_NAME, "/private/dashboards/2").getBreadcrumbs());
		section2.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_CYBER_INSURANCE_2", SLCT.DASHBOARDS$CYBER_INSURANCE$BUSINESS_INTERRUPTION$ITEM_NAME, "/private/dashboards/2?section=1").getBreadcrumbs());
		section3.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_CYBER_INSURANCE_3", SLCT.DASHBOARDS$CYBER_INSURANCE$CYBER_EXTORTION$ITEM_NAME, "/private/dashboards/2?section=2").getBreadcrumbs());


		// Initialize Aggregate Limit section
		createOrganizationAggregateLimitDashboardItem(riskModelId, section1, systemsDataMap, allSystemsList);

		// Initialize Organization Business Interruption Sublimit section
		createOrganizationBusinessInterruptionSublimitDashboardItem(riskModelId, section2, systemsDataMap, allSystemsList);

		// Initialize Organization Cyber Extortion Sublimit section
		createOrganizationCyberExtortionSublimitDashboardItem(riskModelId, section3, systemsDataMap, allSystemsList);


		if (isGDPRRegulatoryQuantDefined) {
			// Initialize Organization Privacy Sublimit section
			DashboardSectionDTO section4 = new DashboardSectionDTO(2001004L, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$GDPR_PRIVACY$ITEM_NAME), null);
			dashboard.getSections().add(section4);
			createOrganizationGDPRPrivacySublimitDashboardItem(riskModelId, section4, systemsDataMap, allSystemsList);

			// Create breadcrumbs
			section4.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_CYBER_INSURANCE_4", SLCT.DASHBOARDS$CYBER_INSURANCE$GDPR_PRIVACY$ITEM_NAME, "/private/dashboards/2?section=2").getBreadcrumbs());
		}

		if (isPrivacyQuantDefined) {
			// Initialize Organization Privacy Sublimit section
			DashboardSectionDTO section5 = new DashboardSectionDTO(2001005L, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$PRIVACY$ITEM_NAME), null);
			dashboard.getSections().add(section5);
			createOrganizationPrivacySublimitDashboardItem(riskModelId, section5, systemsDataMap, allSystemsList);

			// Create breadcrumbs
			section5.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_CYBER_INSURANCE_5", SLCT.DASHBOARDS$CYBER_INSURANCE$PRIVACY$ITEM_NAME, "/private/dashboards/2?section=2").getBreadcrumbs());

		}

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getOrganizationDashboardDetails(Long riskModelId) {
		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_ORGANIZATION, clientMessage.getMessage("DASHBOARDS$ORGANIZATION$NAME"), clientMessage.getMessage("DASHBOARDS$ORGANIZATION$DESCRIPTION"), DashboardType.Organization);

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_CISO(clientMessage).add("DASHBOARD_CISO_DIGITAL", SLCT.DASHBOARDS$CISO$DIGITAL_ASSET, "/private/dashboards/2");

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(2000001L, clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_RISK$ITEM_NAME), null);
		DashboardSectionDTO section2 = new DashboardSectionDTO(2000002L, clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_EXPOSURES$ITEM_NAME), null);
		DashboardSectionDTO section3 = new DashboardSectionDTO(2000003L, clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$REGULATIONS_HEADER), null);
		DashboardSectionDTO section4 = new DashboardSectionDTO(2000004L, clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$EXPOSURE_SUMMARY$ITEM_NAME), null);
		dashboard.getSections().add(section1);
		dashboard.getSections().add(section2);
		dashboard.getSections().add(section3);
		dashboard.getSections().add(section4);

		// Create breadcrumbs
		section1.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_EXECUTIVE_CYBER_1", SLCT.DASHBOARDS$ORGANIZATION$CYBER_RISK$ITEM_NAME, "/private/dashboards/2").getBreadcrumbs());
		section2.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_EXECUTIVE_CYBER_2", SLCT.DASHBOARDS$ORGANIZATION$CYBER_EXPOSURES$ITEM_NAME, "/private/dashboards/2?section=1").getBreadcrumbs());
		section3.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_EXECUTIVE_CYBER_3", SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$REGULATIONS_HEADER, "/private/dashboards/2?section=2").getBreadcrumbs());
		section4.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_EXECUTIVE_CYBER_4", SLCT.DASHBOARDS$ORGANIZATION$EXPOSURE_SUMMARY$ITEM_NAME, "/private/dashboards/2?section=3").getBreadcrumbs());


		// Initialize Organization Summary Scores
		DashboardItemDTO dashboardItem1 = scoringQuestionsDashboardService.createSummaryScoresDashboardItem(riskModelId, Arrays.asList(VendorType.System));
		section1.getDashboardItems().add(dashboardItem1);

		// Initialize Organization Quant Scores
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap = exposureMetricsDashboardService.getSystemsScoringData(riskModel.getId());
		List<Systems> allSystemsList = systemRepository.getAllByOrganizationAndNotEtl(riskModel.getOrganizationId());

		DashboardItemDTO dashboardItem2 = createOrganizationQuantScoresDashboardItem(riskModel, systemScoringDataMap, allSystemsList);
		section2.getDashboardItems().add(dashboardItem2);

		DashboardItemDTO dashboardItem3 = createOrganizationRegulationScoresDashboardItem(riskModel, systemScoringDataMap, allSystemsList);
		section3.getDashboardItems().add(dashboardItem3);

		// Build Organization Quants Summary
		DashboardTableItemDTO dashboardItem4 = new DashboardTableItemDTO(23l, clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$EXPOSURE_SUMMARY$CYBER_INSURANCE_SUMMARY$ITEM_NAME));
		dashboardItem4.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$EXPOSURE_SUMMARY$CYBER_INSURANCE_SUMMARY$QUANTIFICATION_METRIC_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$EXPOSURE_SUMMARY$CYBER_INSURANCE_SUMMARY$QUANT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$EXPOSURE_SUMMARY$CYBER_INSURANCE_SUMMARY$QUANT_LEVEL),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$REGULATIONS_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$INDUSTRIES_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$EXPOSURE_SUMMARY$CYBER_INSURANCE_SUMMARY$FORMULA_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$EXPOSURE_SUMMARY$CYBER_INSURANCE_SUMMARY$TOTAL_EXPOSURE_HEADER)
		), true);
		Map<QuantMetrics, ExposureMetricResult> organizationScoringDataMap = exposureMetricsDashboardService.getOrganizationCumulativeScoringData(riskModel, null);
		for (Map.Entry<QuantMetrics, ExposureMetricResult> entry : organizationScoringDataMap.entrySet()) {
			ExposureMetricResult exposureMetricResult = entry.getValue();
			if (exposureMetricResult == null) {
				continue;
			}

			QuantMetrics quantMetric = entry.getKey();
			String regulations = quantMetric.getRegulations().stream().map(Regulations::getAcronym).collect(Collectors.joining(", "));
			String industries = quantMetric.getIndustries().stream().map(Industries::getName).collect(Collectors.joining("; "));

			DashboardDataItemDrilldownDTO drilldown = DashboardDataItemDrilldownDTO.ofOrganizationQuants(quantMetric, null);

			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(exposureMetricResult.getMetricName()).applyDrilldown(drilldown));
			rowItems.add(sI(quantMetric.getQuant() != null ? quantMetric.getQuant().getName() : ""));
			rowItems.add(sI(quantMetric.getQuantMetricLevel() != null ? quantMetric.getQuantMetricLevel().name() : ""));
			rowItems.add(sI(regulations));
			rowItems.add(sI(industries));
			rowItems.add(sI(exposureMetricResult.getFormulaBuilder().getFormulaString()));
			rowItems.add($I(exposureMetricResult.getResult(), exposureMetricResult.getMeasurementUnit("$")).round(0).applyDrilldown(drilldown));
			dashboardItem4.getGridItems().add(rowItems);
		}
		section4.getDashboardItems().add(dashboardItem4);

		return dashboard;
	}

	public DashboardDTO getOrganizationSystemGDPRDashboard(Long riskModelId) {
		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_ORGANIZATION_SYSTEM_GDPR, clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$DESCRIPTION), DashboardType.None);

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

		List<GDPRSystemStatusDTO> gdprSystemStatusDTOList = gdprSystemStatusModelDAO.getItemsForOrganization(riskModel.getOrganizationId());
		GDPRFilter filter = new GDPRFilter();
		filter.setIsOrganizationLevel(true);
		PagedResult<GDPRArticleStatusDTO> gdprOrganizationArticleStatusPagedResult = gdprArticleStatusModelDAO.getItemsPageable(filter);
		List<GDPRArticleStatusDTO> gdprOrganizationArticleStatusDTOList = gdprOrganizationArticleStatusPagedResult.getItems();
		GDPROrganizationStatusDTO gdprOrganizationStatus = gdprArticleStatusService.getCurrentGDPROrganizationStatus();

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(1270001L, clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$SYSTEM_STATUS$ITEM_NAME), null);
		dashboard.getSections().add(section1);
		DashboardSectionDTO section2 = new DashboardSectionDTO(1270002L, clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$ITEM_NAME), null);
		dashboard.getSections().add(section2);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD(clientMessage)
			.add("DASHBOARDS$ORGANIZATION_SYSTEM_GDPR", SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$NAME, "/private/dashboards/127");
		section1.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_ORGANIZATION_SYSTEM_GDPR_1", "DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$SYSTEM_STATUS$ITEM_NAME", "").getBreadcrumbs());
		section2.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_ORGANIZATION_SYSTEM_GDPR_2", "DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$ITEM_NAME", "").getBreadcrumbs());


		// Fill System GDPR Status section with data
		// Initialize System GDPR Article Statuses Summary
		DashboardDataGridItemDTO dashboardItem11 = new DashboardDataGridItemDTO(12700011L, "");
		section1.getDashboardItems().add(dashboardItem11);
		dashboardItem11.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$SYSTEM_STATUS$SUMMARY$SYSTEM_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$SYSTEM_STATUS$SUMMARY$STATUS_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$SYSTEM_STATUS$SUMMARY$BUSINESS_UNIT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$SYSTEM_STATUS$SUMMARY$ASSET_CLASS_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$SYSTEM_STATUS$SUMMARY$OWNER_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$SYSTEM_STATUS$SUMMARY$QUESTIONS_APPLIED_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$SYSTEM_STATUS$SUMMARY$QUESTIONS_ANSWERED_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$SYSTEM_STATUS$SUMMARY$COMPLIANCE_HEADER)
		), true);

		for (GDPRSystemStatusDTO gdprSystemStatus: gdprSystemStatusDTOList) {

			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			boolean isSystemPresent = gdprSystemStatus.getSystem() != null;
			String systemName = "";
			String systemStatusName = "";
			String systemBusinessUnitName = "";
			String systemAssetClassName = "";
			String systemOwnerName = "";
			String articlesNumber = "";
			String articlesProcessed = "";
			String compliance = "";

			DashboardDataItemDTO systemNameCell;
			DashboardDataItemDTO systemStatusNameCell;
			DashboardDataItemDTO systemBusinessUnitNameCell;
			DashboardDataItemDTO systemAssetClassNameCell;
			DashboardDataItemDTO systemOwnerNameCell;
			DashboardDataItemDTO articlesNumberCell;
			DashboardDataItemDTO articlesProcessedCell;
			DashboardDataItemDTO complianceCell;

			if (isSystemPresent) {
				Systems system = systemRepository.findById(gdprSystemStatus.getSystem().getId()).get();

				systemName = StringUtils.isNotEmpty(system.getName()) ? system.getName() : "";
				systemNameCell = sI(systemName).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, DashboardDataItemDrilldownDTO.SYSTEM_GDPR_STATUS, null));
			 	systemStatusName = system.getSystemStatus() != null && StringUtils.isNotEmpty(system.getSystemStatus().name()) ? system.getSystemStatus().name() : "";
				systemStatusNameCell = sI(systemStatusName).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, DashboardDataItemDrilldownDTO.SYSTEM_GDPR_STATUS, null));
			 	systemBusinessUnitName = system.getBusinessUnit() != null && StringUtils.isNotEmpty(system.getBusinessUnit().getName()) ? system.getBusinessUnit().getName() : "";
				systemBusinessUnitNameCell = sI(systemBusinessUnitName).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, DashboardDataItemDrilldownDTO.SYSTEM_GDPR_STATUS, null));
			 	systemAssetClassName = system.getDataAssetClassification() != null && StringUtils.isNotEmpty(system.getDataAssetClassification().getName()) ? system.getDataAssetClassification().getName() : "";
				systemAssetClassNameCell = sI(systemAssetClassName).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, DashboardDataItemDrilldownDTO.SYSTEM_GDPR_STATUS, null));
			 	systemOwnerName = system.getOwner() != null && StringUtils.isNotEmpty(system.getOwner().getFullName()) ? system.getOwner().getFullName() : "";
				systemOwnerNameCell = sI(systemOwnerName).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, DashboardDataItemDrilldownDTO.SYSTEM_GDPR_STATUS, null));
			} else {
				systemNameCell = sI("");
				systemStatusNameCell = sI("");
				systemBusinessUnitNameCell = sI("");
				systemAssetClassNameCell = sI("");
				systemOwnerNameCell = sI("");
			}

			articlesNumberCell = dI(gdprSystemStatus.getArticlesNumber()).round(0);
			articlesProcessedCell = dI(gdprSystemStatus.getArticlesProcessed()).round(0);
			complianceCell = dI((gdprSystemStatus.getCompliance() != null ? gdprSystemStatus.getCompliance() : 0), "%").round(2);

			rowItems.addAll(Arrays.asList(
				systemNameCell,
				systemStatusNameCell,
				systemBusinessUnitNameCell,
				systemAssetClassNameCell,
				systemOwnerNameCell,
				articlesNumberCell,
				articlesProcessedCell,
				complianceCell
			));

			dashboardItem11.getGridItems().add(rowItems);
		}

		// Fill Organization GDPR Status section with data
		// Initialize Organization GDPR Article Statuses Summary
		DashboardTableItemDTO dashboardItem21 = new DashboardTableItemDTO(12700021L, clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$SUMMARY$ITEM_NAME));
		section2.getDashboardItems().add(dashboardItem21);

		String totalCompliance = gdprOrganizationStatus.getCompliance() != null ? gdprOrganizationStatus.getCompliance().toString() + "%" : "0.0%";
		DashboardDataItemDTO totalComplianceCell = sI(totalCompliance);

		String filesNumber = gdprOrganizationStatus.getFilesNumber() != null ? gdprOrganizationStatus.getFilesNumber().toString() : "0";
		DashboardDataItemDTO filesNumberCell = sI(filesNumber);

		String articlesNumber = gdprOrganizationStatus.getArticlesNumber() != null ? gdprOrganizationStatus.getArticlesNumber().toString() : "0";
		DashboardDataItemDTO articlesNumberCell = sI(articlesNumber);

		String articlesProcessed = gdprOrganizationStatus.getArticlesProcessed() != null ? gdprOrganizationStatus.getArticlesProcessed().toString() : "0";
		DashboardDataItemDTO articlesProcessedCell = sI(articlesProcessed);

		dashboardItem21.getGridItems().addAll(Arrays.asList(
			Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$SUMMARY$COMPLIANCE_HEADER)).applyHeader(true), totalComplianceCell),
			Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$SUMMARY$FILES_NUMBER_HEADER)).applyHeader(true), filesNumberCell),
			Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$SUMMARY$ARTICLES_NUMBER_HEADER)).applyHeader(true), articlesNumberCell),
			Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$SUMMARY$ARTICLES_PROCESSED_HEADER)).applyHeader(true), articlesProcessedCell)
		));

		if (gdprOrganizationArticleStatusDTOList.size() > 0) {
			DashboardDataGridItemDTO dashboardItem22 = new DashboardDataGridItemDTO(12700022L, clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$ARTICLES_STATUSES$ITEM_NAME));
			section2.getDashboardItems().add(dashboardItem22);
			dashboardItem22.getGridHeaders().add(Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$ARTICLES_STATUSES$RECORD_NUMBER_HEADER), 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$ARTICLES_STATUSES$CHAPTER_HEADER), 1L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$ARTICLES_STATUSES$ARTICLE_NUMBER_HEADER), 2L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$ARTICLES_STATUSES$ARTICLE_HEADER), 3L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$ARTICLES_STATUSES$QUESTION_HEADER), 4L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$ORGANIZATION_STATUS$ARTICLES_STATUSES$COMPLIANCE_HEADER), 5L)
			));

			// Fill dashboard item with actual data
			int i = 1;
			for (GDPRArticleStatusDTO gdprOrganizationArticleStatus: gdprOrganizationArticleStatusDTOList) {
				List<DashboardDataItemDTO> rowItems = new ArrayList<>();

				String gdprOrganizationArticleStatusNumber = Integer.toString(i);
				DashboardDataItemDTO gdprOrganizationArticleStatusNumberCell = sI(gdprOrganizationArticleStatusNumber);

				String chapterName = gdprOrganizationArticleStatus.getChapter() != null && StringUtils.isNotEmpty(gdprOrganizationArticleStatus.getChapter().getName()) ? gdprOrganizationArticleStatus.getChapter().getName() : "";
				DashboardDataItemDTO chapterNameCell = sI(chapterName);

				String articleNumber = gdprOrganizationArticleStatus.getArticle() != null && gdprOrganizationArticleStatus.getArticle().getArticleNumber() != null ? gdprOrganizationArticleStatus.getArticle().getArticleNumber().toString() : "";
				DashboardDataItemDTO articleNumberCell = sI(articleNumber);

				String articleName = gdprOrganizationArticleStatus.getArticle() != null && StringUtils.isNotEmpty(gdprOrganizationArticleStatus.getArticle().getName()) ? gdprOrganizationArticleStatus.getArticle().getName() : "";
				DashboardDataItemDTO articleNameCell = sI(articleName);

				String question = gdprOrganizationArticleStatus.getQuestion() != null && StringUtils.isNotEmpty(gdprOrganizationArticleStatus.getQuestion().getQuestion()) ? gdprOrganizationArticleStatus.getQuestion().getQuestion() : "";
				DashboardDataItemDTO questionCell = sI(question);

				String compliance = gdprOrganizationArticleStatus.getCompliance() != null ? gdprOrganizationArticleStatus.getCompliance().toString() + "%" : "0.0%";
				DashboardDataItemDTO complianceCell = sI(compliance);

				rowItems.addAll(Arrays.asList(
					gdprOrganizationArticleStatusNumberCell,
					chapterNameCell,
					articleNumberCell,
					articleNameCell,
					questionCell,
					complianceCell
				));
				dashboardItem22.getGridItems().add(rowItems);
				i++;
			}
		}

		return dashboard;
	}

	/**
	 * Build System GDPR Status drilldown
	 *
	 * @param	drilldown
	 * @param	riskModelId
	 * @param	dashboard
	 */
	public void buildSystemGDPRStatusDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {

		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO();
		dashboard.getSections().add(section);

		// Prepare system and gdpr data
		Long systemId = Long.valueOf(drilldown.getParams().get(DashboardDataItemDrilldownDTO.PARAM_ITEM));
		Systems system = systemRepository.findById(systemId).get();
		GDPRFilter filter = new GDPRFilter();
		filter.setSystemId(systemId);
		filter.setIsSystemLevel(true);
		PagedResult<GDPRSystemArticleStatusDTO> pagedResult = gdprSystemArticleStatusModelDAO.getItemsPageable(filter);
		List<GDPRSystemArticleStatusDTO> gdprSystemArticleStatusDTOList = pagedResult.getItems();

		String systemName = StringUtils.isNotEmpty(system.getName()) ? system.getName() : "";

		// Provide drilldown with actual data
		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_GDPR_STATUS$ITEM_NAME), systemName));

		if (gdprSystemArticleStatusDTOList.size() > 0) {
			// Initialize System GDPR Article Statuses Summary
			DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(1000000L, clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_GDPR_STATUS$SUMMARY$ITEM_NAME));
			section.getDashboardItems().add(dashboardItem);
			dashboardItem.getGridHeaders().add(Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_GDPR_STATUS$SUMMARY$RECORD_NUMBER_HEADER), 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_GDPR_STATUS$SUMMARY$CHAPTER_HEADER), 1L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_GDPR_STATUS$SUMMARY$ARTICLE_NUMBER_HEADER), 2L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_GDPR_STATUS$SUMMARY$ARTICLE_HEADER), 3L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_GDPR_STATUS$SUMMARY$QUESTION_HEADER), 4L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_GDPR_STATUS$SUMMARY$COMPLIANCE_HEADER), 5L)
			));

			// Fill Dashboard Item with actual data
			int i = 1;
			for (GDPRSystemArticleStatusDTO gdprSystemArticleStatus: gdprSystemArticleStatusDTOList) {
				List<DashboardDataItemDTO> rowItems = new ArrayList<>();

				String gdprSystemArticleStatusNumber = Integer.toString(i);
				DashboardDataItemDTO gdprSystemArticleStatusNumberCell = sI(gdprSystemArticleStatusNumber);

				String chapterName = gdprSystemArticleStatus.getChapter() != null && StringUtils.isNotEmpty(gdprSystemArticleStatus.getChapter().getName()) ? gdprSystemArticleStatus.getChapter().getName() : "";
				DashboardDataItemDTO chapterNameCell = sI(chapterName);

				String articleNumber = gdprSystemArticleStatus.getArticle() != null && gdprSystemArticleStatus.getArticle().getArticleNumber() != null ? gdprSystemArticleStatus.getArticle().getArticleNumber().toString() : "";
				DashboardDataItemDTO articleNumberCell = sI(articleNumber);

				String articleName = gdprSystemArticleStatus.getArticle() != null && StringUtils.isNotEmpty(gdprSystemArticleStatus.getArticle().getName()) ? gdprSystemArticleStatus.getArticle().getName() : "";
				DashboardDataItemDTO articleNameCell = sI(articleName);

				String question = gdprSystemArticleStatus.getQuestion() != null && StringUtils.isNotEmpty(gdprSystemArticleStatus.getQuestion().getQuestion()) ? gdprSystemArticleStatus.getQuestion().getQuestion() : "";
				DashboardDataItemDTO questionCell = sI(question);

				String compliance = gdprSystemArticleStatus.getCompliance() != null ? gdprSystemArticleStatus.getCompliance().toString() + "%" : "0.0%";
				DashboardDataItemDTO complianceCell = sI(compliance);

				rowItems.addAll(Arrays.asList(
					gdprSystemArticleStatusNumberCell,
					chapterNameCell,
					articleNumberCell,
					articleNameCell,
					questionCell,
					complianceCell
				));
				dashboardItem.getGridItems().add(rowItems);
				i++;
			}
		}
	}


	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getElastioDashboardDetails(Long riskModelId, Long dashboardId) {

		boolean isGDPRRegulatoryQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE);
		boolean isPrivacyQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.PRIVACY_EXPOSURE);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop;
		breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_EXECUTIVE(clientMessage).add("DASHBOARD_CYBER_INSURANCE", SLCT.DASHBOARDS$CYBER_INSURANCE$NAME, "/private/dashboards/2000");

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		// List<Systems> allSystemsList = systemRepository.getAllByOrganizationAndNotEtl(riskModel.getOrganizationId());

		ElastioOrganizationViewDTO elastioOrganizationDTO = new ElastioOrganizationViewDTO();
		elastioOrganizationDTO.setId(riskModel.getOrganizationId());
		elastioOrganizationDTO = elastioOrganizationService.evaluateElastio(elastioOrganizationDTO);

		DashboardDTO dashboard = new DashboardDTO(dashboardId, "RiskQ Elastio Dashboard: " + elastioOrganizationDTO.getName(), "RiskQ Elastio Dashboard", DashboardType.Organization);

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(2001001L, "RiskQ Elastio Dashboard: " + elastioOrganizationDTO.getName(), null);

		dashboard.getSections().add(section1);

		// Create breadcrumbs
		// section1.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_CYBER_INSURANCE_1", SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$ITEM_NAME, "/private/dashboards/2").getBreadcrumbs());
		section1.setBreadcrumbs(breadcrumbsTop.getBreadcrumbs());

		// DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(1000000L, "Elastio ROI Analysis");
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(1000000L, "Baseline Scenario");
		section1.getDashboardItems().add(dashboardItem);

		dashboardItem.getGridItems().add(Arrays.asList(sI("Current state without Elastio implementation").applyTextAlign("left").applyHeader(true).applyColspan(2L)));

		dashboardItem.getGridItems().add(Arrays.asList(sI("Risk Exposure").applyTextAlign("right").applyHeader(true).applyColspan(2L)));
		dashboardItem.getGridItems().add(Arrays.asList(sI("Baseline Ransomware Exposure").applyTextAlign("left").applyHeader(true), $I(elastioOrganizationDTO.getEvaluationResult().getBaselineRansomwareExposure()).applyColor("#ff0000").round(0)));
		dashboardItem.getGridItems().add(Arrays.asList(sI("Baseline Downtime Loss").applyTextAlign("left").applyHeader(true), $I(elastioOrganizationDTO.getEvaluationResult().getBaselineDowntimeLoss()).applyColor("#ff0000").round(0)));

		DashboardTableItemDTO dashboardItem2 = new DashboardTableItemDTO(1000000L, "ROI & Payback Analysis");
		section1.getDashboardItems().add(dashboardItem2);
		dashboardItem2.getGridItems().add(Arrays.asList(sI("Financial Impact with Elastio Implementation").applyTextAlign("left").applyHeader(true).applyColspan(2L)));
		dashboardItem2.getGridItems().add(Arrays.asList(sI("Cost Savings").applyTextAlign("right").applyHeader(true).applyColspan(2L)));
		dashboardItem2.getGridItems().add(Arrays.asList(sI("Downtime Loss Post Elastio").applyTextAlign("left").applyHeader(true), $I(elastioOrganizationDTO.getEvaluationResult().getDowntimeLossPostElastio()).applyColor("#00ff00").round(0)));
		dashboardItem2.getGridItems().add(Arrays.asList(sI("Downtime Savings").applyTextAlign("left").applyHeader(true), $I(elastioOrganizationDTO.getEvaluationResult().getDowntimeSavings()).applyColor("#00ff00").round(0)));
		dashboardItem2.getGridItems().add(Arrays.asList(sI("ROI Ransomware").applyTextAlign("left").applyHeader(true), $I(elastioOrganizationDTO.getEvaluationResult().getRoiRansomware()).applySymbol("%").round(2)));

		DashboardTableItemDTO dashboardItem3 = new DashboardTableItemDTO(1000000L, "");
		section1.getDashboardItems().add(dashboardItem3);
		dashboardItem3.getGridItems().add(Arrays.asList(sI("Ransomware").applyTextAlign("right").applyHeader(true).applyColspan(2L)));
		dashboardItem3.getGridItems().add(Arrays.asList(sI("Payback Period Ransomware").applyTextAlign("left").applyHeader(true), sI(elastioOrganizationDTO.getEvaluationResult().getPaybackPeriodRansomware()).applySymbol("month").applyColor("#9333ea").round(2)));

		DashboardTableItemDTO dashboardItem4 = new DashboardTableItemDTO(1000000L, "");
		section1.getDashboardItems().add(dashboardItem4);
		dashboardItem4.getGridItems().add(Arrays.asList(sI("").applyParam("width", "50%").applyParam("border", "none"),
														sI("Total Annual Savings").applyTextAlign("right").applyHeader(true).applyParam("border", "none"),
														$I(elastioOrganizationDTO.getEvaluationResult().getTotalAnnualSavings()).applyParam("width", "200px").applyParam("border", "none").applyColor("#00ff00").round(2)));

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getCysuranceDashboardDetails(Long riskModelId, Long dashboardId) {

		boolean isGDPRRegulatoryQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE);
		boolean isPrivacyQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.PRIVACY_EXPOSURE);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop;
		breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_EXECUTIVE(clientMessage).add("DASHBOARD_CYBER_INSURANCE", SLCT.DASHBOARDS$CYBER_INSURANCE$NAME, "/private/dashboards/2000");

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		// elastioOrganizationDTO = elastioOrganizationService.evaluateElastio(elastioOrganizationDTO);


		List<CysuranceQueryResponseDataEntityRating> ratingData = cysuranceIntegrationService.getCysuranceIntegrationsData(riskModel.getOrganizationId());
		Map<String, CysuranceQueryResponseDataEntityRating> ratingValuesMap = ratingData.stream().collect(Collectors.toMap(CysuranceQueryResponseDataEntityRating::getFactorCode, v -> v, (o, o2) -> o2));
		Map<String, List<CysuranceQueryResponseDataEntityRating>> ratingValuesByCategoryMap = ratingData.stream().collect(Collectors.groupingBy(CysuranceQueryResponseDataEntityRating::getCategoryCode));
		// Map<String, CysuranceQueryResponseDataEntityRating> ratingValuesByCodeMap = ratingData.stream().collect(Collectors.toMap(CysuranceQueryResponseDataEntityRating::getFactorCode, v -> v, (t, t2) -> t2));

		DashboardDTO dashboard = new DashboardDTO(dashboardId, "RiskQ Cysurance Dashboard: " + organization.getName(), "RiskQ Cysurance Dashboard", DashboardType.Organization);

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(2001001L, "RiskQ Cysurance Dashboard: " + organization.getName(), null);

		dashboard.getSections().add(section1);

		// Create breadcrumbs
		// section1.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_CYBER_INSURANCE_1", SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$ITEM_NAME, "/private/dashboards/2").getBreadcrumbs());
		section1.setBreadcrumbs(breadcrumbsTop.getBreadcrumbs());

		// DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(1000000L, "Elastio ROI Analysis");
		DashboardGridLayoutDTO headerBlock = new DashboardGridLayoutDTO(1000005L, "Warranty Control Conditions Compliance");
		DashboardControlTextBlockDTO controlsPassing = DashboardControlTextBlockDTO.of(1000006L, "Controls Passing", "0", "#3b82f6", "#3b82f6");
		DashboardControlTextBlockDTO complianceRate = DashboardControlTextBlockDTO.of(1000007L, "Controls Passing", "0", "#959ca7", "#f59e0b");
		headerBlock.addRowItems(new RichDashboardElementDTO(controlsPassing), new RichDashboardElementDTO(complianceRate));
		section1.getDashboardItems().add(headerBlock);

		DashboardItemDTO dashboardItem = new DashboardItemDTO(102006L, "Warranty Control Assessments", null, DashboardItemType.Text);
		section1.getDashboardItems().add(dashboardItem);

		CysuranceFactorResult factorsResult = new CysuranceFactorResult();

		// "EDR-P"
		applyDashboardCheckAction(section1, factorsResult, ratingValuesMap.get("NGAV-P"), "Next Gen Antivirus / EDR", 70, "Deploy an endpoint protection platform such as CrowdStrike Falcon, SentinelOne, or Microsoft Defender for Endpoint across all devices. Ensure definitions and agent versions are kept current via auto-update policies. Verify coverage with an asset inventory — no unmanaged endpoints.");
		// MDR-P
		applyDashboardCheckAction(section1, factorsResult, ratingValuesMap.get("SIEM-P"), "MDR / SIEM", 60, "Engage a Managed Detection and Response provider or deploy a SIEM (e.g., Splunk, Microsoft Sentinel, Rapid7 InsightIDR). At minimum, centralize log collection from endpoints, firewalls, and identity systems with 24/7 alerting. If budget is limited, an MDR service is faster to stand up than a self-managed SIEM.");
		applyDashboardCheckAction(section1, factorsResult, ratingValuesMap.get("MFA-P"), "MFA (Multi Factor Authentication)", 80, "Enable MFA on all email accounts (Microsoft 365, Google Workspace) immediately — this is the highest-ROI control. Use an authenticator app (not SMS where possible). Extend MFA to VPN, remote access, and any admin consoles. Enforce via Conditional Access or equivalent policy so it can't be bypassed.");
		applyDashboardCheckAction(section1, factorsResult, ratingValuesMap.get("BACK-P"), "Backups", 85, "Implement the 3-2-1 rule: 3 copies, 2 different media, 1 offsite. Backups must be immutable (write-once, cannot be deleted or encrypted by ransomware). Test restores on a regular schedule — an untested backup is not a backup. Solutions: Veeam, Acronis, or cloud-native options like AWS Backup.");
		// Data Privacy and Compliance
		// applyDashboardCheckAction(section1, factorsResult, ratingValuesMap.get("MFA-P"), "Data Privacy Compliance / Encryption", 80, "");
		applyDashboardCheckAction(section1, factorsResult, ratingValuesMap.get("PATCH-P"), "Patch Updates", 75, "Stand up a patch management process with a hard SLA: critical/high patches applied within 60 days of release (per the warranty condition), ideally sooner for critical CVEs. Tools: Microsoft WSUS/Intune, Automox, Qualys Patch Management, or NinjaRMM. Run monthly vulnerability scans to verify compliance and catch missed patches.");
		// applyDashboardCheckAction(section1, factorsResult, ratingValuesMap.get("MFA-P"), "Security Awareness Training", 80, "");
		// applyDashboardCheckAction(section1, factorsResult, ratingValuesMap.get("MFA-P"), "Invoice and Wire Change Procedures", 80, "");

		// dashboardItem.getGridItems().add(Arrays.asList(sI("Current state without Elastio implementation").applyTextAlign("left").applyHeader(true).applyColspan(2L)));

		controlsPassing.setTitle(String.format("%s of %s", factorsResult.getControlsPassing(), factorsResult.getControlsTotal()));
		complianceRate.setTitle(factorsResult.getComplianceRate() + "%");
		complianceRate.setTitleColor(factorsResult.getComplianceRate() > 75 ? "#22c55e" : "#f59e0b");

		return dashboard;
	}

	private void applyDashboardCheckAction(DashboardSectionDTO section, CysuranceFactorResult factorsResult, CysuranceQueryResponseDataEntityRating currentValue, String label, Integer requirement, String defaultAction) {
		if (currentValue != null) {
			Integer valueInteger = (Integer) currentValue.getValue();
			section.getDashboardItems().add(DashboardCheckStatusItemDTO.of(1L, label, requirement, valueInteger, (valueInteger < requirement ? defaultAction : null)));

			factorsResult.setControlsPassing(factorsResult.getControlsPassing() + (valueInteger < requirement ? 0 : 1));
			factorsResult.setControlsTotal(factorsResult.getControlsTotal() + 1);
		}
	}

	@Data
	@AllArgsConstructor
	public static class CysuranceFactorInfo {
		private String factorName;
		private Double value;
		private Double required;
		private String measurementUnit;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CysuranceFactorResult {
		private Integer controlsPassing = 0;
		private Integer controlsTotal = 0;
		public Integer getComplianceRate() {
			if (controlsTotal.equals(0)) {
				return 0;
			}

			return (controlsPassing * 100) / controlsTotal;
		}
	}


	// ========== Cyber Insurance Dashboard ========== //


	public void createOrganizationAggregateLimitDashboardItem(Long riskModelId, DashboardSectionDTO section, Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsDataMap, List<Systems> allSystemsList) {
		Double organizationInsuranceLimit = Optional.ofNullable(organizationService.getCurrentOrganizationEntity().getInsuranceLimit()).orElse(Organizations.INSURANCE_LIMIT);
		Double organizationRecordPrice = Optional.ofNullable(organizationService.getCurrentOrganizationEntity().getRecordPriceLimit()).orElse(Organizations.DEFAULT_RECORD_PRICE);
		Double uninsurableRecordCount = organizationInsuranceLimit / organizationRecordPrice;

		// Build Organization Summary Scores Dashboard
		DashboardTableItemDTO dashboardHeaderItem = new DashboardTableItemDTO(20010121l, "");
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(2001012l, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$GRID$ITEM_NAME));

		List<String> headers = Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$GRID$SYSTEM_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$GRID$RECORD_COUNT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$GRID$DATA_EXFILTRATION_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$RECORDS_OVER_LIMIT)
		);
		// clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$GRID$AGGREGATE_LIMIT_HEADER)
		dashboardItem.addGridHeaders(headers, true);
		double sublimit = 0d;
		double sublimitRecords = 0d;
		for (Systems system : allSystemsList) {
			Map<QuantMetrics, ExposureMetricResult> metricsDataMap = systemsDataMap.get(system);
			Double exposure = metricsDataMap.entrySet().stream().filter(item -> QuantsDomain.DATA_EXFILTRATION.getId().equals(item.getKey().getQuant().getId())).mapToDouble(item -> item.getValue().getResult()).max().orElse(0d);
			String exposureComment = exposure > organizationInsuranceLimit ? "Extraordinarily High Record Count" : "";
			Double recordsDifference = system.getNumberOfRecProcessed() != null ? system.getNumberOfRecProcessed() - uninsurableRecordCount : null;
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));
			rowItems.add(dI(system.getNumberOfRecProcessed()).applyTextAlign("right").round(0));
			rowItems.add($I(exposure).applyTextAlign("right").round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.DATA_EXFILTRATION)));
			if (recordsDifference != null && recordsDifference > 0 && exposure > organizationInsuranceLimit) {
				rowItems.add(dI(recordsDifference));
			} else {
				rowItems.add(sI(""));
			}
			dashboardItem.getGridItems().add(rowItems);

			if (exposure > sublimit && exposure < organizationInsuranceLimit) {
				sublimit = exposure;
			}
			if (system.getNumberOfRecProcessed() != null && system.getNumberOfRecProcessed() > sublimitRecords && system.getNumberOfRecProcessed() < uninsurableRecordCount) {
				sublimitRecords = system.getNumberOfRecProcessed();
			}
		}

		// dashboardHeaderItem.getGridItems().add(Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$TABLE$AGGREGATE_LIMIT_HEADER)).applyHeader(true), dI(sublimitRecords).round(0)));
		// dashboardHeaderItem.getGridItems().add(Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$TABLE$AGGREGATE_LIMIT_HEADER) + " ($)").applyHeader(true), $I(sublimit).round(0)));
		String recordPriceHeader = clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$RECORD_PRICE);
		if (organizationService.getCurrentOrganizationEntity().getRecordPriceLimit() == null || organizationService.getCurrentOrganizationEntity().getRecordPriceLimit() <= 0) {
			recordPriceHeader = clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$RECORD_PRICE);
		}
		dashboardHeaderItem.getGridItems().add(Arrays.asList(sI(recordPriceHeader).applyHeader(true), $I(organizationRecordPrice).round(2)));
		dashboardHeaderItem.getGridItems().add(Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$CURRENT_AGGREGATE_INSURANCE_LIMIT)).applyHeader(true), $I(organizationInsuranceLimit).round(0)));
		dashboardHeaderItem.getGridItems().add(Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$INSURABLE_RECORDS_COUNT)).applyHeader(true), dI(uninsurableRecordCount).round(0)));
		dashboardHeaderItem.getGridItems().add(Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$AGGREGATE_LIMIT$TABLE$AGGREGATE_LIMIT_HEADER)).applyHeader(true), $I(sublimit).round(0)));

		section.getDashboardItems().add(dashboardHeaderItem);
		section.getDashboardItems().add(dashboardItem);
	}

	public void createOrganizationBusinessInterruptionSublimitDashboardItem(Long riskModelId, DashboardSectionDTO section, Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsDataMap, List<Systems> allSystemsList) {
		Double organizationInsuranceLimit = Optional.ofNullable(organizationService.getCurrentOrganizationEntity().getInsuranceLimit()).orElse(Organizations.INSURANCE_LIMIT);

		// Build Organization Summary Scores Dashboard
		DashboardTableItemDTO dashboardHeaderItem = new DashboardTableItemDTO(20010131l, "");
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(2001013l, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$BUSINESS_INTERRUPTION$GRID$ITEM_NAME));

		List<String> headers = Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$BUSINESS_INTERRUPTION$GRID$SYSTEM_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$BUSINESS_INTERRUPTION$GRID$BUSINESS_INTERRUPTION_HEADER)
		);

		dashboardItem.addGridHeaders(headers, true);
		double sublimit = 0d;
		for (Systems system : allSystemsList) {
			Map<QuantMetrics, ExposureMetricResult> metricsDataMap = systemsDataMap.get(system);
			Double exposure = metricsDataMap.entrySet().stream().filter(item -> QuantsDomain.BUSINESS_INTERRUPTION_SUBLIMIT.getId().equals(item.getKey().getQuant().getId())).mapToDouble(item -> item.getValue().getResult()).max().orElse(0d);
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));
			rowItems.add($I(exposure).applyTextAlign("right").round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.BUSINESS_INTERRUPTION_SUBLIMIT)));
			dashboardItem.getGridItems().add(rowItems);

			if (exposure > sublimit && exposure < organizationInsuranceLimit) sublimit = exposure;
		}

		dashboardHeaderItem.getGridItems().add(Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$BUSINESS_INTERRUPTION$TABLE$BUSINESS_INTERRUPTION_HEADER)), $I(sublimit).round(0)));

		section.getDashboardItems().add(dashboardHeaderItem);
		section.getDashboardItems().add(dashboardItem);
	}

	public void createOrganizationCyberExtortionSublimitDashboardItem(Long riskModelId, DashboardSectionDTO section, Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsDataMap, List<Systems> allSystemsList) {
		Double organizationInsuranceLimit = Optional.ofNullable(organizationService.getCurrentOrganizationEntity().getInsuranceLimit()).orElse(Organizations.INSURANCE_LIMIT);

		// Build Organization Summary Scores Dashboard
		DashboardTableItemDTO dashboardHeaderItem = new DashboardTableItemDTO(20010141l, "");
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(2001014l, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$CYBER_EXTORTION$GRID$ITEM_NAME));

		List<String> headers = Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$CYBER_EXTORTION$GRID$SYSTEM_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$CYBER_EXTORTION$GRID$RANSOMWARE_HEADER)
		);
		dashboardItem.addGridHeaders(headers, true);
		double sublimit = 0d;
		for (Systems system : allSystemsList) {
			Map<QuantMetrics, ExposureMetricResult> metricsDataMap = systemsDataMap.get(system);
			Double exposure = metricsDataMap.entrySet().stream().filter(item -> QuantsDomain.RANSOMWARE_SUBLIMIT.getId().equals(item.getKey().getQuant().getId())).mapToDouble(item -> item.getValue().getResult()).max().orElse(0d);
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));
			rowItems.add($I(exposure).applyTextAlign("right").round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.RANSOMWARE_SUBLIMIT)));
			dashboardItem.getGridItems().add(rowItems);

			if (exposure > sublimit && exposure < organizationInsuranceLimit) sublimit = exposure;
		}

		dashboardHeaderItem.getGridItems().add(Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$CYBER_EXTORTION$TABLE$CYBER_EXTORTION_HEADER)), $I(sublimit).round(0)));

		section.getDashboardItems().add(dashboardHeaderItem);
		section.getDashboardItems().add(dashboardItem);
	}

	public void createOrganizationGDPRPrivacySublimitDashboardItem(Long riskModelId, DashboardSectionDTO section, Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsDataMap, List<Systems> allSystemsList) {
		Double organizationInsuranceLimit = Optional.ofNullable(organizationService.getCurrentOrganizationEntity().getInsuranceLimit()).orElse(Organizations.INSURANCE_LIMIT);

		// Build Organization Summary Scores Dashboard
		DashboardTableItemDTO dashboardHeaderItem = new DashboardTableItemDTO(20010151l, "");
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(2001015l, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$GDPR_PRIVACY$GRID$ITEM_NAME));

		List<String> headers = Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$GDPR_PRIVACY$GRID$SYSTEM_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$GDPR_PRIVACY$GRID$GDPR_REGULATORY_LOSS_HEADER)
		);
		dashboardItem.addGridHeaders(headers, true);
		double sublimit = 0d;
		for (Systems system : allSystemsList) {
			Map<QuantMetrics, ExposureMetricResult> metricsDataMap = systemsDataMap.get(system);
			Double exposure = metricsDataMap.entrySet().stream().filter(item -> QuantsDomain.GDPR_REGULATORY_EXPOSURE.getId().equals(item.getKey().getQuant().getId())).mapToDouble(item -> item.getValue().getResult()).max().orElse(0d);
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));
			rowItems.add($I(exposure).applyTextAlign("right").round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.GDPR_REGULATORY_EXPOSURE)));
			dashboardItem.getGridItems().add(rowItems);

			if (exposure > sublimit && exposure <= organizationInsuranceLimit) sublimit = exposure;
		}

		dashboardHeaderItem.getGridItems().add(Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$GDPR_PRIVACY$TABLE$GDPR_PRIVACY_HEADER)), $I(sublimit).round(0)));

		section.getDashboardItems().add(dashboardHeaderItem);
		section.getDashboardItems().add(dashboardItem);
	}

	public void createOrganizationPrivacySublimitDashboardItem(Long riskModelId, DashboardSectionDTO section, Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsDataMap, List<Systems> allSystemsList) {
		Double organizationInsuranceLimit = Optional.ofNullable(organizationService.getCurrentOrganizationEntity().getInsuranceLimit()).orElse(Organizations.INSURANCE_LIMIT);

		// Build Organization Summary Scores Dashboard
		DashboardTableItemDTO dashboardHeaderItem = new DashboardTableItemDTO(20010151l, "");
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(2001015l, clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$PRIVACY$GRID$ITEM_NAME));

		List<String> headers = Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$PRIVACY$GRID$SYSTEM_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$PRIVACY$GRID$GDPR_REGULATORY_LOSS_HEADER)
		);
		dashboardItem.addGridHeaders(headers, true);
		double sublimit = 0d;
		for (Systems system : allSystemsList) {
			Map<QuantMetrics, ExposureMetricResult> metricsDataMap = systemsDataMap.get(system);
			Double exposure = metricsDataMap.entrySet().stream().filter(item -> QuantsDomain.PRIVACY_EXPOSURE.getId().equals(item.getKey().getQuant().getId())).mapToDouble(item -> item.getValue().getResult()).max().orElse(0d);
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));
			rowItems.add($I(exposure).applyTextAlign("right").round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.PRIVACY_EXPOSURE)));
			dashboardItem.getGridItems().add(rowItems);

			if (exposure > sublimit && exposure < organizationInsuranceLimit) sublimit = exposure;
		}

		dashboardHeaderItem.getGridItems().add(Arrays.asList(sI(clientMessage.getMessage(SLCT.DASHBOARDS$CYBER_INSURANCE$PRIVACY$TABLE$GDPR_PRIVACY_HEADER)), $I(sublimit).round(0)));

		section.getDashboardItems().add(dashboardHeaderItem);
		section.getDashboardItems().add(dashboardItem);
	}

	// ========== END of Cyber Insurance Dashboard ========== //

	/**
	 * Combine few Data Streams to one
	 *
	 * @param impactData
	 * @param likelihoodData
	 * @return
	 */
	@Deprecated
	public Map<Long, SystemDataSeries> getLongSystemDataSeriesMap(List<SystemDataSeries> impactData, List<SystemDataSeries> likelihoodData) {
		Map<Long, SystemDataSeries> summaryQualData = new HashMap<>();
		impactData.stream().forEach(dataSeries -> {
			if (!summaryQualData.containsKey(dataSeries.getSystem().getId())) {
				SystemDataSeries target = dataSeries.clone();
				target.getItems().add(Double.valueOf(0));
				target.getItems().add(Double.valueOf(0));
				summaryQualData.put(dataSeries.getSystem().getId(), target);
			}
		});
		likelihoodData.stream().forEach(dataSeries -> {
			if (!summaryQualData.containsKey(dataSeries.getSystem().getId())) {
				SystemDataSeries series = new SystemDataSeries();
				series.setSystem(dataSeries.getSystem());
				series.setItems(Arrays.asList(Double.valueOf(0), Double.valueOf(0), Double.valueOf(0), Double.valueOf(0), Double.valueOf(0)));
				summaryQualData.put(dataSeries.getSystem().getId(), series);
			}

			SystemDataSeries target = summaryQualData.get(dataSeries.getSystem().getId());
			Double totalValue = (target.getItems().get(0) + dataSeries.getItems().get(0))/2;
			target.getItems().set(0, totalValue);
			target.getItems().set(3, dataSeries.getItems().get(1));
			target.getItems().set(4, dataSeries.getItems().get(2));
		});
		return summaryQualData;
	}

	@Deprecated
	public DashboardItemDTO createOrganizationSummaryScoresDashboardItem(Map<Long, SystemDataSeries> summaryQualData) {
		// Build Organization Summary Scores Dashboard
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(11l, clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$ITEM_NAME));
		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$SYSTEM_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$IMPACT_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$IMPACT_TOTAL_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$LIKELIHOOD_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$LIKELIHOOD_TOTAL_HEADER)
		), true);
		for (Map.Entry<Long, SystemDataSeries> entry : summaryQualData.entrySet()) {
			SystemDataSeries dataSeries = entry.getValue();
			List<DashboardDataItemDTO> rowItems = createRowItems(dataSeries);
			dashboardItem.getGridItems().add(rowItems);
			applyVendorDashboardQualsDrilldown(rowItems, dataSeries.getSystem());
		}
		return dashboardItem;
	}

	@Deprecated
	public DashboardItemDTO createOrganizationQuantScoresDashboardItem(Long riskModelId, List<SystemDataSeries> quantScores) {
		// Build Organization Summary Scores Dashboard
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(12l, "");

		boolean isGDPRRegulatoryQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE);

		List<String> headers = new ArrayList<>();
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$ORGANIZATION_QUANT_SCORES$SYSTEM_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$ORGANIZATION_QUANT_SCORES$DATA_EXFILTRATION_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$ORGANIZATION_QUANT_SCORES$BUSINESS_INTERRUPTION_HEADER));
		if (isGDPRRegulatoryQuantDefined) headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$ORGANIZATION_QUANT_SCORES$GDPR_REGULATORY_EXPOSURE_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$ORGANIZATION_QUANT_SCORES$TOTAL_EXPOSURE));

		dashboardItem.addGridHeaders(headers, true);
		for (SystemDataSeries dataSeries : quantScores) {
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(dataSeries.getSystem().getName()));
			rowItems.addAll(dataSeries.getItems().stream().map(item -> $I(item, "$").round(0)).collect(Collectors.toList()));
			dashboardItem.getGridItems().add(rowItems);
			applyVendorDashboardQuantsDrilldown(rowItems, dataSeries.getSystem());
		}

		return dashboardItem;
	}

	public DashboardItemDTO createOrganizationQuantScoresDashboardItem(RiskModels riskModel, Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap, List<Systems> systemsList) {
		// Build Organization Summary Scores Dashboard
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(12l, "");

		List<String> headers = new ArrayList<>();
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_EXPOSURES$QUANT_SCORES$SYSTEM_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_EXPOSURES$QUANT_SCORES$DATA_EXFILTRATION_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_EXPOSURES$QUANT_SCORES$BUSINESS_INTERRUPTION_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_EXPOSURES$QUANT_SCORES$REGULATORY_EXPOSURE_HEADER, "Regulatory Loss"));

		List<QuantsDomain> allowedQuants = Arrays.asList(QuantsDomain.DATA_EXFILTRATION, QuantsDomain.BUSINESS_INTERRUPTION, QuantsDomain.REGULATORY_LOSS);

		dashboardItem.addGridHeaders(headers, true);
		for (Systems system : systemsList) {
			Map<QuantsDomain, Double> quantExposures = new HashMap<>();

			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));

			Map<QuantMetrics, ExposureMetricResult> systemMetricDataMap = systemScoringDataMap.get(system);
			for (Map.Entry<QuantMetrics, ExposureMetricResult> entry : systemMetricDataMap.entrySet()) {
				ExposureMetricResult exposureMetricResult = entry.getValue();
				QuantMetrics quantMetric = entry.getKey();

				for (QuantsDomain quantsDomain: allowedQuants) {
					if (quantsDomain.getId().equals(quantMetric.getQuant().getId())) {
						quantExposures.put(quantsDomain, quantExposures.computeIfAbsent(quantsDomain, regulationTypes -> 0D) + exposureMetricResult.getResult());
					}
				}
			}

            for (QuantsDomain quantsDomain : allowedQuants) {
                rowItems.add($I(quantExposures.computeIfAbsent(quantsDomain, key -> 0D)).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, quantsDomain)));
            }

			dashboardItem.getGridItems().add(rowItems);
		}

		return dashboardItem;
	}

	public DashboardItemDTO createOrganizationRegulationScoresDashboardItem(RiskModels riskModel, Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap, List<Systems> systemsList) {
		// Build Organization Summary Scores Dashboard
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(14l, "");

		Set<Regulations> usedRegulations = regulationRepository.getAllUsedRegulationsInRiskModel(riskModel.getId());

		List<String> headers = new ArrayList<>();
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_EXPOSURES$QUANT_SCORES$SYSTEM_HEADER));
		for (Regulations regulation: usedRegulations) {
			headers.add(regulation.getAcronym());
		}

		dashboardItem.addGridHeaders(headers, true);
		for (Systems system : systemsList) {
			Map<Regulations, Double> regulationExposures = new HashMap<>();
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));

			Map<QuantMetrics, ExposureMetricResult> systemMetricDataMap = systemScoringDataMap.get(system);
			for (Map.Entry<QuantMetrics, ExposureMetricResult> entry : systemMetricDataMap.entrySet()) {
				ExposureMetricResult exposureMetricResult = entry.getValue();
				QuantMetrics quantMetric = entry.getKey();

				if (CollectionUtils.isNotEmpty(quantMetric.getRegulations())) {
					Set<Long> regulationIdSet = quantMetric.getRegulations().stream().map(Regulations::getId).collect(Collectors.toSet());
					for (Regulations regulation: usedRegulations) {
						if (regulationIdSet.contains(regulation.getId())) {
							regulationExposures.put(regulation, regulationExposures.computeIfAbsent(regulation, regulationTypes -> 0D) + exposureMetricResult.getResult());
						}
					}
				}
			}

			Double totalExposure = 0D;
			for (Regulations regulation: usedRegulations) {
				Double regulationValue = regulationExposures.computeIfAbsent(regulation, regulationTypes -> 0D);
				totalExposure += regulationValue;
				rowItems.add($I(regulationValue).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));
			}

			dashboardItem.getGridItems().add(rowItems);
		}

		return dashboardItem;
	}

}
