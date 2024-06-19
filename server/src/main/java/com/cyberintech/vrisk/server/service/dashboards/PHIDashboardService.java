package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.DashboardType;
import com.cyberintech.vrisk.server.model.jpa.domains.DataTypeDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantsDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.entity.QuantMetrics;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemRepository;
import com.cyberintech.vrisk.server.service.QuantMetricsService;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * PHI Data Dashboard Service
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-04-06
 */
@Service
@Slf4j
public class PHIDashboardService extends DashboardServiceBase {

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private ExposureMetricsDashboardService exposureMetricsDashboardService;

	@Autowired
	private QuantMetricsService quantMetricsService;

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getDashboardDetails(Long riskModelId) {

		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_PHI_DATA, clientMessage.getMessage(SLCT.DASHBOARDS$PHI_DATA$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$PHI_DATA$DESCRIPTION), DashboardType.None);

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<Systems> systemsList = systemRepository.getSystemsListWithDataTypes(riskModel.getOrganizationId(), Arrays.asList(DataTypeDomain.HEALTHCARE.getId()));

		List<QuantsDomain> metricsDomains = Arrays.asList(QuantsDomain.DATA_EXFILTRATION, QuantsDomain.BUSINESS_INTERRUPTION, QuantsDomain.GDPR_REGULATORY_EXPOSURE);
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap = exposureMetricsDashboardService.getSystemsScoringData(riskModel.getId(), systemsList, metricsDomains);

		boolean isGDPRRegulatoryQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE);

		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO();
		dashboard.getSections().add(section);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DPO_DASHBOARD(clientMessage);
		section.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_PHI_DATA", "DASHBOARDS$PHI_DATA$NAME", "").getBreadcrumbs());

		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(120001l, "");
		section.getDashboardItems().add(dashboardItem);
		List<String> headers = new ArrayList<>();
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$PHI_DATA$PHI_DATA$SYSTEM_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$PHI_DATA$PHI_DATA$DATA_EXFILTRATION_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$PHI_DATA$PHI_DATA$BUSINESS_INTERRUPTION_HEADER));
		if (isGDPRRegulatoryQuantDefined) headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$PHI_DATA$PHI_DATA$GDPR_REGULATORY_EXPOSURE_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$PHI_DATA$PHI_DATA$TOTAL_EXPOSURE_HEADER));

		dashboardItem.addGridHeaders(headers, true);
		for (Systems system : systemsList) {
			Double dataExfiltration = 0d;
			Double businessInterruption = 0d;
			Double gdprRegultoryExposure = 0d;

			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));

			Map<QuantMetrics, ExposureMetricResult> systemMetricDataMap = systemScoringDataMap.get(system);
			for (Map.Entry<QuantMetrics, ExposureMetricResult> entry : systemMetricDataMap.entrySet()) {
				ExposureMetricResult exposureMetricResult = entry.getValue();
				QuantMetrics quantMetric = entry.getKey();
				if (QuantsDomain.DATA_EXFILTRATION.getId().equals(quantMetric.getQuant().getId())) dataExfiltration += exposureMetricResult.getResult();
				if (QuantsDomain.BUSINESS_INTERRUPTION.getId().equals(quantMetric.getQuant().getId())) businessInterruption += exposureMetricResult.getResult();
				if (QuantsDomain.GDPR_REGULATORY_EXPOSURE.getId().equals(quantMetric.getQuant().getId())) gdprRegultoryExposure += exposureMetricResult.getResult();
			}

			Double totalExposure = dataExfiltration + businessInterruption + gdprRegultoryExposure;

			rowItems.add($I(dataExfiltration).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.DATA_EXFILTRATION)));
			rowItems.add($I(businessInterruption).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.BUSINESS_INTERRUPTION)));
			if (isGDPRRegulatoryQuantDefined) {
				rowItems.add($I(gdprRegultoryExposure).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.GDPR_REGULATORY_EXPOSURE)));
			}
			rowItems.add($I(totalExposure).round(0));

			dashboardItem.getGridItems().add(rowItems);
		}

		return dashboard;
	}
}
