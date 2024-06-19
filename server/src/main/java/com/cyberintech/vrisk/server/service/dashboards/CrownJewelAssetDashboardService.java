package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.QuantMetrics;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemRepository;
import com.cyberintech.vrisk.server.service.BusinessUnitService;
import com.cyberintech.vrisk.server.service.PermissionService;
import com.cyberintech.vrisk.server.service.UserService;
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
public class CrownJewelAssetDashboardService extends DashboardServiceBase {

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private ExposureMetricsDashboardService exposureMetricsDashboardService;


	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getCrownJewelsDashboardDetails(Long riskModelId, Long dashboardId) {

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Set<Systems> systemsSet = systemRepository.getAllByOrganizationAndAssetClass(riskModel.getOrganizationId(), AssetClass.CROWN_JEWEL.getId()).stream().collect(Collectors.toSet());

		DashboardDTO dashboard = new DashboardDTO(dashboardId, clientMessage.getMessage(SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$DESCRIPTION), DashboardType.Organization);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop;
		if (DashboardsConfig.DASHBOARD_CROWN_JEWEL_ASSET.equals(dashboardId)) {
			breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_CISO(clientMessage)
				.extend("DASHBOARD_CROWN_JEWEL_ASSET", SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$NAME, "/private/dashboards/101");
		} else {
			breadcrumbsTop = DashboardBreadcrumbsHelper.CFO_DASHBOARD(clientMessage)
				.extend("DASHBOARD_CROWN_JEWEL_ASSET", SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$NAME, "/private/dashboards/2001");
		}

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(101001L, clientMessage.getMessage(SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$CYBER_RISK_SCORE$ITEM_NAME), null);
		dashboard.getSections().add(section1);
		section1.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_CROWN_JEWEL_ASSET_CYBER_RISK_SCORE", SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$CYBER_RISK_SCORE$ITEM_NAME, "").getBreadcrumbs());

		// Add download button
		if (permissionService.checkCurrentUserPermission(PermissionType.DASHBOARD_CROWN_JEWEL_ASSET)) {
			DashboardItemDTO downloadButton = buildDownloadButtonDashboardItemDTO(riskModelId, DashboardsConfig.CROWN_JEWEL_REPORT, 35701L);
			section1.getDashboardItems().add(downloadButton);
		}

		DashboardDataGridItemDTO dashboardItem1 = new DashboardDataGridItemDTO(1010011L, "");
		dashboardItem1.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$CYBER_RISK_SCORE$SYSTEM_OWNER_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$CYBER_RISK_SCORE$EMAIL_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$CYBER_RISK_SCORE$SYSTEM_NAME_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$CYBER_RISK_SCORE$BU_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$CYBER_RISK_SCORE$CYBER_RISK_SCORE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$CYBER_RISK_SCORE$FINANCIAL_EXPOSURE_HEADER)
		));
		section1.getDashboardItems().add(dashboardItem1);

		Map<Systems, SystemDataSeries> impactData = getQualMetricDataForSystems(riskModelId, MetricDomain.IMPACT, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> likelihoodData = getQualMetricDataForSystems(riskModelId, MetricDomain.LIKELIHOOD, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));

		List<QuantsDomain> quantDomains = Arrays.asList(QuantsDomain.DATA_EXFILTRATION, QuantsDomain.BUSINESS_INTERRUPTION, QuantsDomain.RANSOMWARE, QuantsDomain.GDPR_REGULATORY_EXPOSURE);
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsScoringDataMap = exposureMetricsDashboardService.getSystemsScoringData(riskModelId, new ArrayList<>(systemsSet), quantDomains);

		for (Systems system : systemsSet) {
			Double qualMetricValue = 0D;
			if (impactData.containsKey(system)) qualMetricValue += impactData.get(system).getItems().get(0);
			if (likelihoodData.containsKey(system)) qualMetricValue += likelihoodData.get(system).getItems().get(0);

			Double dataExfiltration = 0D;
			Double businessInterruption = 0D;
			Double gdprRegulatoryExposure = 0D;
			Double quantMetricValue = 0D;

			Map<QuantMetrics, ExposureMetricResult> vendorMetricDataMap = Optional.ofNullable(systemsScoringDataMap.get(system)).orElse(new HashMap<>());

			for (Map.Entry<QuantMetrics, ExposureMetricResult> entry : vendorMetricDataMap.entrySet()) {
				ExposureMetricResult exposureMetricResult = entry.getValue();
				QuantMetrics quantMetric = entry.getKey();
				if (QuantsDomain.DATA_EXFILTRATION.getId().equals(quantMetric.getQuant().getId())) dataExfiltration += exposureMetricResult.getResult();
				if (QuantsDomain.BUSINESS_INTERRUPTION.getId().equals(quantMetric.getQuant().getId())) businessInterruption += exposureMetricResult.getResult();
				if (QuantsDomain.GDPR_REGULATORY_EXPOSURE.getId().equals(quantMetric.getQuant().getId())) gdprRegulatoryExposure += exposureMetricResult.getResult();
			}
			quantMetricValue += dataExfiltration + businessInterruption + gdprRegulatoryExposure;

			dashboardItem1.getGridItems().add(Arrays.asList(
				sI(system.getOwner() != null ? system.getOwner().getFullName() : "").applyTextAlign("left").applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				sI(system.getOwner() != null ? system.getOwner().getEmail() : "").applyTextAlign("left").applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				sI(system.getName()).applyTextAlign("left").applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				sI(businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true)).applyTextAlign("left"),
				dI(qualMetricValue / 2).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				$I(quantMetricValue).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null))
			));
		}

		DashboardSectionDTO section2 = new DashboardSectionDTO(1010012L, clientMessage.getMessage(SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$TMP_RECOMMENDATIONS$RISK_REDUCTION_RECOMMENDATION), null);
		dashboard.getSections().add(section2);
		section2.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_CROWN_JEWEL_ASSET_RECOMMENDATIONS", SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$TMP_RECOMMENDATIONS$RISK_REDUCTION_RECOMMENDATION, "").getBreadcrumbs());

		String tmpRecommendations = getDashboardDataItemString(riskModelId, 101004L);
		if (userService.hasRole(RoleType.CHIEF_EXECUTIVE_OFFICER) || userService.hasRole(RoleType.CHIEF_RISK_OFFICER) || userService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			DashboardItemDTO dashboardItemTextarea = new DashboardItemDTO(101004L, "", tmpRecommendations, DashboardItemType.Textarea);
			section2.getDashboardItems().add(dashboardItemTextarea);
		} else {
			DashboardItemDTO dashboardItemText = new DashboardItemDTO(101006L, "", tmpRecommendations, DashboardItemType.Text);
			section2.getDashboardItems().add(dashboardItemText);
		}

		return dashboard;
	}


	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public ByteArrayOutputStream buildReport(Long riskModelId) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		try {
			CSVPrinter csvPrinter = getReportCsvPrinter(result);

			RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
			Set<Systems> systemsSet = systemRepository.getAllByOrganizationAndAssetClass(riskModel.getOrganizationId(), AssetClass.CROWN_JEWEL.getId()).stream().collect(Collectors.toSet());

			Map<Systems, SystemDataSeries> impactData = getQualMetricDataForSystems(riskModelId, MetricDomain.IMPACT, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
			Map<Systems, SystemDataSeries> likelihoodData = getQualMetricDataForSystems(riskModelId, MetricDomain.LIKELIHOOD, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
			Map<Systems, SystemDataSeries> dataExfiltration = getOrganizationQuantMetricData(riskModelId, QuantsDomain.DATA_EXFILTRATION, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
			Map<Systems, SystemDataSeries> businessInterruption = getOrganizationQuantMetricData(riskModelId, QuantsDomain.BUSINESS_INTERRUPTION, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
			Map<Systems, SystemDataSeries> regulatoryLoss = getOrganizationQuantMetricData(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));

			for (Systems system : systemsSet) {
				Double qualMetricValue = 0D;
				if (impactData.containsKey(system)) qualMetricValue += impactData.get(system).getItems().get(0);
				if (likelihoodData.containsKey(system)) qualMetricValue += likelihoodData.get(system).getItems().get(0);

				Double quantMetricValue = 0D;
				if (dataExfiltration.containsKey(system)) quantMetricValue += dataExfiltration.get(system).getItems().get(0);
				if (businessInterruption.containsKey(system)) quantMetricValue += businessInterruption.get(system).getItems().get(0);
				if (regulatoryLoss.containsKey(system)) quantMetricValue += regulatoryLoss.get(system).getItems().get(0);

				String businessUnitPath = businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true, "/");

				csvPrinter.printRecord(
					system.getOwner() != null ? system.getOwner().getFullName() : "",
					system.getOwner() != null ? system.getOwner().getEmail() : "",
					system.getName(),
					businessUnitPath,
					qualMetricValue / 2,
					quantMetricValue
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
			"System Owner",
			"Email",
			"System Name",
			"Business Unit",
			"Cyber Risk Score",
			"Financial Exposure"
		);

		return new CSVPrinter(writer, csvFormat);
	}

}
