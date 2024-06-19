package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.MetricDomains;
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
import org.apache.commons.collections4.CollectionUtils;
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
public class PrivacyRiskDashboardService extends DashboardServiceBase {

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
	public DashboardDTO getPrivacyRiskDashboardDetails(Long riskModelId, Long dashboardId) {

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		 Set<Systems> systemsSet = systemRepository.getSystemsListWithDataTypes(riskModel.getOrganizationId(), Arrays.asList(DataTypeDomain.PRIVACY.getId())).stream().collect(Collectors.toSet());
		//Set<Systems> systemsSet = systemRepository.getSystemsListWithDataTypes(riskModel.getOrganizationId(), Arrays.asList(DataTypeDomain.PII.getId(), DataTypeDomain.PRIVACY.getId())).stream().collect(Collectors.toSet());

		DashboardDTO dashboard;

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop;
		if (DashboardsConfig.DASHBOARD_PRIVACY_RISK.equals(dashboardId)) {
			breadcrumbsTop = DashboardBreadcrumbsHelper.DPO_DASHBOARD(clientMessage)
				.extend("DASHBOARD_PRIVACY_RISK", SLCT.DASHBOARDS$PRIVACY_RISK$NAME, "/private/dashboards/102");
			dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_PRIVACY_RISK, clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$DESCRIPTION), DashboardType.Organization);
		} else {
			// DASHBOARD_PRIVACY_IMPACT_ASSESSMENT from GDPR->Privacy Impact Assessment
			breadcrumbsTop = DashboardBreadcrumbsHelper.GDPR(clientMessage)
				.extend("PRIVACY_IMPACT_ASSESSMENT", SLCT.MENU$PRIVACY_IMPACT_ASSESSMENT, "/private/privacy-impact-assessment/2006");
			dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_PRIVACY_RISK, clientMessage.getMessage(SLCT.MENU$PRIVACY_IMPACT_ASSESSMENT), clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$DESCRIPTION), DashboardType.Organization);
		}

		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO(1L, clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$PRIVACY_RISK_SCORE_HEADER), null);
		dashboard.getSections().add(section);
		section.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_PRIVACY_RISK_PRIVACY_RISK_SCORE", SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$PRIVACY_RISK_SCORE_HEADER, "").getBreadcrumbs());

		// Add download button
		if (permissionService.checkCurrentUserPermission(PermissionType.DASHBOARD_PRIVACY_RISK)) {
			DashboardItemDTO downloadButton = buildDownloadButtonDashboardItemDTO(riskModelId, DashboardsConfig.PRIVACY_RISK_REPORT, 35701L);
			section.getDashboardItems().add(downloadButton);
		}

		// DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(102001L, clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$PRIVACY_RISK_SCORE_HEADER));
		DashboardDataGridItemDTO dashboardItem1 = new DashboardDataGridItemDTO(102001L, "");
		dashboardItem1.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$SYSTEM_OWNER_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$EMAIL_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$SYSTEM_NAME_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$BU_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$LIKELIHOOD_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$CONFIDENTIALITY_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$INTEGRITY_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$FINANCIAL_EXPOSURE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$UNINSURABLE_EXPOSURES$SYSTEMS_EXPOSURES$TOTAL_EXPOSURE_HEADER)
		));
		section.getDashboardItems().add(dashboardItem1);

		Map<Systems, SystemDataSeries> impactData = getQualMetricDataForSystems(riskModelId, MetricDomain.IMPACT, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> likelihoodData = getQualMetricDataForSystems(riskModelId, MetricDomain.LIKELIHOOD, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> confidentialityData = getQualMetricDataForSystems(riskModelId, MetricDomain.CONFIDENTIALITY, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> integrityData = getQualMetricDataForSystems(riskModelId, MetricDomain.INTEGRITY, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));

		// List<QuantsDomain> quantDomains = Arrays.asList(QuantsDomain.DATA_EXFILTRATION, QuantsDomain.BUSINESS_INTERRUPTION, QuantsDomain.RANSOMWARE, QuantsDomain.GDPR_REGULATORY_EXPOSURE);
		// Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsScoringDataMap = exposureMetricsDashboardService.getSystemsScoringData(riskModelId, new ArrayList<>(systemsSet), quantDomains);
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsScoringDataMap = exposureMetricsDashboardService.getSystemsScoringData(riskModelId, new ArrayList<>(systemsSet));

		for (Systems system : systemsSet) {
			Double qualMetricValue = 0D;
			Double likelihood = 0D;
			Double confidentiality = 0d;
			Double integrity = 0d;

			if (impactData.containsKey(system)) qualMetricValue += impactData.get(system).getItems().get(0);
			if (likelihoodData.containsKey(system)) qualMetricValue += likelihoodData.get(system).getItems().get(0);
			if (likelihoodData.containsKey(system)) likelihood += likelihoodData.get(system).getItems().get(0);
			if (confidentialityData.containsKey(system)) confidentiality += confidentialityData.get(system).getItems().get(0);
			if (integrityData.containsKey(system)) integrity += integrityData.get(system).getItems().get(0);
			// --------
			Double dataExfiltration = 0D;
			Double businessInterruption = 0D;
			Double gdprRegulatoryExposure = 0D;
			Double quantMetricValue = 0D;
			Double quantMetricTotalValue = 0D;

			Map<QuantMetrics, ExposureMetricResult> vendorMetricDataMap = Optional.ofNullable(systemsScoringDataMap.get(system)).orElse(new HashMap<>());

			for (Map.Entry<QuantMetrics, ExposureMetricResult> entry : vendorMetricDataMap.entrySet()) {
				ExposureMetricResult exposureMetricResult = entry.getValue();
				QuantMetrics quantMetric = entry.getKey();
				if (QuantsDomain.DATA_EXFILTRATION.getId().equals(quantMetric.getQuant().getId())) dataExfiltration += exposureMetricResult.getResult();
				if (QuantsDomain.BUSINESS_INTERRUPTION.getId().equals(quantMetric.getQuant().getId())) businessInterruption += exposureMetricResult.getResult();
				if (QuantsDomain.GDPR_REGULATORY_EXPOSURE.getId().equals(quantMetric.getQuant().getId())) gdprRegulatoryExposure += exposureMetricResult.getResult();

				quantMetricTotalValue += exposureMetricResult.getResult();
			}
			quantMetricValue += dataExfiltration + businessInterruption + gdprRegulatoryExposure;

			dashboardItem1.getGridItems().add(Arrays.asList(
				sI(system.getOwner() != null ? system.getOwner().getFullName() : "").applyTextAlign("left").applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				sI(system.getOwner() != null ? system.getOwner().getEmail() : "").applyTextAlign("left").applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				sI(system.getName()).applyTextAlign("left").applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				sI(businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true)).applyTextAlign("left"),
				//dI(qualMetricValue / 2).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				dI(likelihood).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				dI(confidentiality).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				dI(integrity).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				$I(quantMetricValue).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)),
				$I(quantMetricTotalValue).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null))
			));
		}

		// String tmpRecommendations = "Archive PII records > 5 years with no activity to reduce amount of financial exposure and cyber insurance needed reducing risk by $2,500,000.";
		DashboardSectionDTO section2 = new DashboardSectionDTO(2L, clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$RISK_REDUCTION_RECOMMENDATION), null);
		section2.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_PRIVACY_RISK_REDUCTION", SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$RISK_REDUCTION_RECOMMENDATION, "").getBreadcrumbs());
		dashboard.getSections().add(section2);
		String tmpRecommendations = getDashboardDataItemString(riskModelId, 102004L);
		if (userService.hasRole(RoleType.CHIEF_EXECUTIVE_OFFICER) || userService.hasRole(RoleType.CHIEF_RISK_OFFICER) || userService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			DashboardItemDTO dashboardItemTextarea = new DashboardItemDTO(102004L, clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$RISK_REDUCTION_RECOMMENDATION), tmpRecommendations, DashboardItemType.Textarea);
			section2.getDashboardItems().add(dashboardItemTextarea);
		} else {
			DashboardItemDTO dashboardItemText = new DashboardItemDTO(102006L, clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY_RISK$PRIVACY_RISK_TABLE$RISK_REDUCTION_RECOMMENDATION), tmpRecommendations, DashboardItemType.Text);
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
		return buildReport(riskModelId, null);
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public ByteArrayOutputStream buildReport(Long riskModelId, List<DataTypeDomain> domainsList) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		try {
			CSVPrinter csvPrinter = getReportCsvPrinter(result);

			List<Long> domainsFilter = Arrays.asList(DataTypeDomain.PII.getId(), DataTypeDomain.PRIVACY.getId());
			if (CollectionUtils.isNotEmpty(domainsList)) domainsFilter = domainsList.stream().map(DataTypeDomain::getId).collect(Collectors.toList());

			RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
			Set<Systems> systemsSet = systemRepository.getSystemsListWithDataTypes(riskModel.getOrganizationId(), domainsFilter).stream().collect(Collectors.toSet());

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
			"Privacy Risk Score",
			"Financial Exposure"
		);

		return new CSVPrinter(writer, csvFormat);
	}

}
