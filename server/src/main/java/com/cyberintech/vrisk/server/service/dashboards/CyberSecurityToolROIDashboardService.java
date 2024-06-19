package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
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
 * Cyber Security Tool Dashboard Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-20
 */
@Service
@Slf4j
public class CyberSecurityToolROIDashboardService extends DashboardServiceBase {

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private CybersecurityToolRepository cybersecurityToolRepository;

	@Autowired
	private QuantMetricsRepository quantMetricsRepository;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private SystemToolRiskReductionRepository systemToolRiskReductionRepository;

	@Autowired
	private TechnologyRepository technologyRepository;

	@Autowired
	private UserService userService;

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getDashboardDetails(Long riskModelId, Long dashboardId) {

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<SystemToolRiskReductions> systemsToolRiskReductions = systemToolRiskReductionRepository.findAllByOrganizationId(riskModel.getOrganizationId());
		Set<Systems> systemsSet = technologyRepository.getSystemsByOrganizationForCyberSecurity(riskModel.getOrganizationId());
		List<QuantMetrics> quantMetrics = quantMetricsRepository.getListByRiskModelId(riskModelId);

		DashboardDTO dashboard = new DashboardDTO(dashboardId, clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$DESCRIPTION), DashboardType.Organization);
		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO();
		dashboard.getSections().add(section);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop;
		if (DashboardsConfig.DASHBOARD_CYBERSECURITY_TOOL_ROI.equals(dashboardId)) {
			breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_CISO(clientMessage);
		} else {
			breadcrumbsTop = DashboardBreadcrumbsHelper.CFO_DASHBOARD(clientMessage);
		}

		section.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_CYBERSECURITY_TOOL_ROI", "DASHBOARDS$CYBERSECURITY_TOOL_ROI$NAME", "").getBreadcrumbs());

		DashboardDataGridItemDTO dashboardItem1 = new DashboardDataGridItemDTO(103001L, clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$CALCULATIONS$ITEM_NAME));

		dashboardItem1.getGridHeaders().add(Arrays.asList(
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$CALCULATIONS$SYSTEM_NAME_HEADER), 0l),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$CALCULATIONS$BU_HEADER), 1l),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$CALCULATIONS$SECURITY_TOOL_HEADER), 2l),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$CALCULATIONS$QUANT_METRIC_HEADER), 3l),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$CALCULATIONS$QUANT_VALUE_HEADER), 4l),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$CALCULATIONS$RISK_REDUCTION_PERCENTS_HEADER), null),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$CALCULATIONS$RISK_REDUCTION_VALUE_HEADER), null),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$CALCULATIONS$TOTAL_COST_HEADER), null),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$CALCULATIONS$ROI_HEADER), null)
		));
		section.getDashboardItems().add(dashboardItem1);

		Map<Systems, SystemDataSeries> dataExfiltration = getOrganizationQuantMetricData(riskModelId, QuantsDomain.DATA_EXFILTRATION, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> businessInterruption = getOrganizationQuantMetricData(riskModelId, QuantsDomain.BUSINESS_INTERRUPTION, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> regulatoryLoss = getOrganizationQuantMetricData(riskModelId, QuantsDomain.REGULATORY_LOSS, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));

		// Define Quant Metrics / Technologies Map
		Map<Long, Set<QuantMetrics>> technologyCategoriesQuantsMap = new HashMap<>();
		for (QuantMetrics quantMetric : quantMetrics) {
			for (TechnologyCategories technologyCategory : quantMetric.getTechnologyCategories()) {
				if (!technologyCategoriesQuantsMap.containsKey(technologyCategory.getId())) {
					technologyCategoriesQuantsMap.put(technologyCategory.getId(), new HashSet<>());
				}

				technologyCategoriesQuantsMap.get(technologyCategory.getId()).add(quantMetric);
			}
		}

		for (Systems system : systemsSet) {

			Double dataExfiltrationValue = (dataExfiltration.containsKey(system)) ? dataExfiltration.get(system).getItems().get(0) : 0D;
			Double businessInterruptionValue = (businessInterruption.containsKey(system)) ? businessInterruption.get(system).getItems().get(0) : 0D;
			Double regulatoryLossValue = (regulatoryLoss.containsKey(system)) ? regulatoryLoss.get(system).getItems().get(0) : 0D;
			Double totalQuantMetricValue = dataExfiltrationValue + businessInterruptionValue + regulatoryLossValue;

			for (Technologies technology : system.getTechnologies()) {

				TechnologyCategories technologyCategory = technology.getTechnologyCategory();
				if (technologyCategory != null && technologyCategoriesQuantsMap.containsKey(technologyCategory.getId())) {
					for (QuantMetrics quantMetric : technologyCategoriesQuantsMap.get(technologyCategory.getId())) {

						Double quantMetricValue = 0D;
						if (QuantsDomain.BUSINESS_INTERRUPTION.getId().equals(quantMetric.getQuant().getId())) {
							quantMetricValue = businessInterruptionValue;
						} else if (QuantsDomain.DATA_EXFILTRATION.getId().equals(quantMetric.getQuant().getId())) {
							quantMetricValue = dataExfiltrationValue;
						} else if (QuantsDomain.REGULATORY_LOSS.getId().equals(quantMetric.getQuant().getId())) {
							quantMetricValue = regulatoryLossValue;
						}

						Double riskReduction = technology.getRiskReduction() != null ? technology.getRiskReduction() : 0d;
						if (technology.getRiskReductionPercent() != null) {
							Double riskReductionPercent = technology.getRiskReductionPercent() <= 100 ? technology.getRiskReductionPercent() : 0D;
							riskReduction = riskReductionPercent * quantMetricValue / 100;
						}

						Double toolCost = (technology.getToolPrice() != null) ? technology.getToolPrice() : 0D;
						Double roi = 0d;
						if (toolCost > 0 && riskReduction > toolCost) {
							roi = (riskReduction - toolCost) * 100 / toolCost;
						}

						dashboardItem1.getGridItems().add(Arrays.asList(
							sI(system.getName()).applyTextAlign("left").applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
							sI(businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true)).applyTextAlign("left"),
							sI(technology.getName()).applyTextAlign("left").applyLink(DashboardLinkDTO.of("/private/technologies/edit/" + technology.getId())),
							sI(quantMetric.getName()).applyTextAlign("left"),
							$I(quantMetricValue).round(0),
							$I(technology.getRiskReductionPercent() != null ? technology.getRiskReductionPercent() : 0d, "%").round(2).applyLink(DashboardLinkDTO.of("/private/technologies/edit/" + technology.getId())),
							$I(riskReduction, "$").round(0),
							$I(technology.getToolPrice() != null ? technology.getToolPrice() : 0d).round(0),
							$I(roi, "%").round(0)
						));
					}
				}

			}

			/*
			for (CybersecurityTools cybersecurityTool : system.getCybersecurityTools()) {

				for (QuantMetrics quantMetric : cybersecurityTool.getQuantMetrics()) {

					Double quantMetricValue = 0D;
					if (QuantsDomain.BUSINESS_INTERRUPTION.getId().equals(quantMetric.getQuant().getId())) {
						quantMetricValue = businessInterruptionValue;
					} else if (QuantsDomain.DATA_EXFILTRATION.getId().equals(quantMetric.getQuant().getId())) {
						quantMetricValue = dataExfiltrationValue;
					} else if (QuantsDomain.REGULATORY_LOSS.getId().equals(quantMetric.getQuant().getId())) {
						quantMetricValue = regulatoryLossValue;
					}

					Double riskReduction = cybersecurityTool.getRiskReduction();
					if (cybersecurityTool.getRiskReductionPercent() != null) {
						Double riskReductionPercent = cybersecurityTool.getRiskReductionPercent() <= 100 ? cybersecurityTool.getRiskReductionPercent() : 0D;
						riskReduction = riskReductionPercent * quantMetricValue / 100;
					}

					Double toolCost = (cybersecurityTool.getToolPrice() != null) ? cybersecurityTool.getToolPrice() : 0D;
					Double roi = 0d;
					if (toolCost > 0 && riskReduction > toolCost) {
						roi = (riskReduction - toolCost) * 100 / toolCost;
					}

					dashboardItem1.getGridItems().add(Arrays.asList(
						sI(system.getName()).applyTextAlign("left").applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
						sI(businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true)).applyTextAlign("left"),
						sI(cybersecurityTool.getName()).applyTextAlign("left").applyLink(DashboardLinkDTO.of("/private/security-tools/edit/" + cybersecurityTool.getId())),
						sI(quantMetric.getName()).applyTextAlign("left"),
						$I(quantMetricValue).round(0),
						$I(cybersecurityTool.getRiskReductionPercent(), "%").round(2).applyLink(DashboardLinkDTO.of("/private/security-tools/edit/" + cybersecurityTool.getId())),
						$I(riskReduction, "$").round(0),
						$I(cybersecurityTool.getToolPrice()).round(0),
						$I(roi, "%").round(0)
					));
				}
			}
			*/
		}

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getCyberSecurityToolROIDashboardDetails(Long riskModelId) {

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<SystemToolRiskReductions> systemsToolRiskReductions = systemToolRiskReductionRepository.findAllByOrganizationId(riskModel.getOrganizationId());
		Set<Systems> systemsSet = systemsToolRiskReductions.stream().map(SystemToolRiskReductions::getSystem).collect(Collectors.toSet());
		// Set<Systems> systemsSet = systemRepository.getAllByOrganization(riskModel.getOrganizationId()).stream().collect(Collectors.toSet());

		DashboardDTO dashboard = new DashboardDTO(103L, "DASHBOARDS$CYBERSECURITY_TOOL_ROI$NAME", "Cybersecurity Tool ROI Dashboard", DashboardType.Organization);
		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO();
		dashboard.getSections().add(section);

		DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(103001L, "Cybersecurity Tool ROI Calculations");
		dashboardItem1.addGridHeaders(Arrays.asList("System Name", "Financial Exposure", "BU", "Risk Reduction (%)", "Risk Reduction ($)", "Tool Cost", "ROI"));
		section.getDashboardItems().add(dashboardItem1);

		Map<Systems, SystemDataSeries> dataExfiltration = getOrganizationQuantMetricData(riskModelId, QuantsDomain.DATA_EXFILTRATION, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> businessInterruption = getOrganizationQuantMetricData(riskModelId, QuantsDomain.BUSINESS_INTERRUPTION, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> regulatoryLoss = getOrganizationQuantMetricData(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));

		for (SystemToolRiskReductions systemsToolRiskReduction : systemsToolRiskReductions) {

			Systems system = systemsToolRiskReduction.getSystem();

			Double quantMetricValue = 0D;
			if (dataExfiltration.containsKey(system)) quantMetricValue += dataExfiltration.get(system).getItems().get(0);
			if (businessInterruption.containsKey(system)) quantMetricValue += businessInterruption.get(system).getItems().get(0);
			if (regulatoryLoss.containsKey(system)) quantMetricValue += regulatoryLoss.get(system).getItems().get(0);

			Double riskReductionPercent = (systemsToolRiskReduction.getRiskReductionPercent() != null) ? systemsToolRiskReduction.getRiskReductionPercent() : 0D;
			Double riskReduction = riskReductionPercent * quantMetricValue / 100;
			Double toolCost = (systemsToolRiskReduction.getToolPrice() != null) ? systemsToolRiskReduction.getToolPrice() : 0D;
			Double roi = 0d;
			if (toolCost > 0) {
				roi = (riskReduction - toolCost) * 100 / toolCost;
			}

			dashboardItem1.getGridItems().add(Arrays.asList(
				sI(system.getName()).applyTextAlign("left").applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				$I(quantMetricValue).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)),
				sI(businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true)).applyTextAlign("left"),
				$I(riskReductionPercent, "%").round(2),
				$I(riskReduction, "$").round(0),
				$I(toolCost, "$").round(0),
				$I(roi, "%").round(0)
			));
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
			Set<Systems> systemsSet = systemRepository.getSystemsListWithDataTypes(riskModel.getOrganizationId(), Arrays.asList(DataTypeDomain.PII.getId(), DataTypeDomain.PRIVACY.getId())).stream().collect(Collectors.toSet());

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
