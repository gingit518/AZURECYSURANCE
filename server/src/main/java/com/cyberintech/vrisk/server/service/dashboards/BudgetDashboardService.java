package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.DashboardType;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantsDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemToolRiskReductions;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Budget Dashboard Service
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-05-04
 */
@Service
@Slf4j
public class BudgetDashboardService extends DashboardServiceBase {

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	RiskModelRepository riskModelRepository;

	@Autowired
	OrganizationRepository organizationRepository;

	@Autowired
	FixedCapitalCostRepository fixedCapitalCostRepository;

	@Autowired
	FixedOperationalCostRepository fixedOperationalCostRepository;

	@Autowired
	VariableCostRepository variableCostRepository;

	@Autowired
	SystemToolRiskReductionRepository systemToolRiskReductionRepository;

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getBudgetingDashboardDetails(Long riskModelId) {

		DashboardDTO dashboard = new DashboardDTO(104L, clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$DESCRIPTION), DashboardType.Organization);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_CISO(clientMessage).add("DASHBOARDS_BUDGETING",SLCT.DASHBOARDS$BUDGETING$NAME,"/private/dashboards/104");

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		Double fixedCapitalCosts = Optional.ofNullable(fixedCapitalCostRepository.getTotalCosts(organization.getId())).orElse(0D);
		Double fixedOperational = Optional.ofNullable(fixedOperationalCostRepository.getTotalCosts(organization.getId())).orElse(0D);
		Double variableCosts = Optional.ofNullable(variableCostRepository.getTotalCosts(organization.getId())).orElse(0D);
		Double totalCosts = fixedCapitalCosts + fixedOperational + variableCosts;


		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(10400L, clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$CYBERSECURITY$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$CYBERSECURITY$ITEM_DESCRIPTION));
		dashboard.getSections().add(section1);

		DashboardSectionDTO section2 = new DashboardSectionDTO(10401L, clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$ITEM_DESCRIPTION));
		dashboard.getSections().add(section2);

		// Create breadcrumbs
		section1.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARDS_BUDGETING_1", SLCT.DASHBOARDS$BUDGETING$CYBERSECURITY$ITEM_NAME, "").getBreadcrumbs());
		section2.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARDS_BUDGETING_2", SLCT.DASHBOARDS$BUDGETING$SUMMARY$ITEM_NAME, "").getBreadcrumbs());

		// Initialize Cybersecurity Budgeting Scores
		DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(104001L, clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$CYBERSECURITY$BUDGET$ITEM_NAME));
		section1.getDashboardItems().add(dashboardItem1);

		dashboardItem1.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$CYBERSECURITY$BUDGET$FIXED_CAPITAL_COSTS_HEADER)).applyTextAlign("left").applyHeader(true)
				.applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.BUDGET, DashboardDataItemDrilldownDTO.CATEGORY_BUDGET_FIXED_CAPITAL_COSTS)),
			$I(fixedCapitalCosts).round(0)
		));
		dashboardItem1.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$CYBERSECURITY$BUDGET$FIXED_OPERATIONAL_COSTS_HEADER)).applyTextAlign("left").applyHeader(true)
				.applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.BUDGET, DashboardDataItemDrilldownDTO.CATEGORY_BUDGET_FIXED_OPERATIONAL_COSTS)),
			$I(fixedOperational).round(0)
		));
		dashboardItem1.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$CYBERSECURITY$BUDGET$VARIABLE_OPERATIONAL_COSTS_HEADER)).applyTextAlign("left").applyHeader(true)
				.applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.BUDGET, DashboardDataItemDrilldownDTO.CATEGORY_BUDGET_VARIABLE_OPERATIONAL_COSTS)),
			$I(variableCosts).round(0)
		));
		dashboardItem1.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$CYBERSECURITY$BUDGET$TOTAL_OPTIMAL_BUDGET_HEADER)).applyTextAlign("left").applyHeader(true),
			$I(totalCosts).round(0)
		));

		// Initialize Summary Scores
		DashboardDataGridItemDTO dashboardItem2 = new DashboardDataGridItemDTO(104011L, clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$ITEM_NAME));
		dashboardItem2.getGridHeaders().add(
			Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$PROJECT_NAME_HEADER), 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$BUDGET_MANAGER_HEADER), 1L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$EMPLOYEE_NAME_HEADER), 2L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$EST_HOURS_HEADER), 3L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$EST_COST_HEADER), 4L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$ACTUAL_HOURS_HEADER), 5L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$ACTUAL_COST_HEADER), 6L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$LICENSE_NAME_HEADER), 7L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$LICENSE_TYPE_HEADER), 8L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$LICENSE_COST_HEADER), 9L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$CONSULTANT_NAME_HEADER), 10L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$EST_HEADER), 11L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$ACTUAL_END_DATE_HEADER), 12L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$TASKS_HEADER), 13L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$BUDGETING$SUMMARY$SCORES_GRID$COMMENTS_HEADER), 14L)
			)
		);
		section2.getDashboardItems().add(dashboardItem2);

		// TODO 04.05.2020	Fill Summary Scores section with actual data

		return dashboard;
	}

	/**
	 * Build Cyber Security Budget Scenario Analysis Dashboard
	 *
	 * @param riskModelId
	 */
	public DashboardDTO getCyberSecurityBudgetScenarioAnalysisDashBoard(Long riskModelId) {
		DashboardDTO dashboard = new DashboardDTO(105L, "Cyber Security Budget Scenario Analysis Dashboard", "Cyber Security Budget Scenario Analysis Dashboard", DashboardType.Organization);

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<SystemToolRiskReductions> systemsToolRiskReductions = systemToolRiskReductionRepository.findAllByOrganizationId(riskModel.getOrganizationId());
		Set<Systems> systemsSet = systemsToolRiskReductions.stream().map(SystemToolRiskReductions::getSystem).collect(Collectors.toSet());

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		// Initialize Variable cost summary
		Double variableCosts = variableCostRepository.getTotalCosts(organization.getId());
		DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(105001L, null);
		section1.getDashboardItems().add(dashboardItem1);
		dashboardItem1.getGridItems().add(Arrays.asList(
			sI("Variable Operational Costs").applyTextAlign("left").applyHeader(true),
			$I(variableCosts).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.BUDGET, DashboardDataItemDrilldownDTO.CATEGORY_BUDGET_VARIABLE_OPERATIONAL_COSTS))
		));

		// TODO 04.05.2020	check is data related to Quant Metrics is correct [DEPRECATED METHODS IN USE]
		Map<Systems, SystemDataSeries> dataExfiltration = getOrganizationQuantMetricData(riskModelId, QuantsDomain.DATA_EXFILTRATION, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> businessInterruption = getOrganizationQuantMetricData(riskModelId, QuantsDomain.BUSINESS_INTERRUPTION, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> regulatoryLoss = getOrganizationQuantMetricData(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));

		DashboardTableItemDTO dashboardItem2 = new DashboardTableItemDTO(105001L, "Cybersecurity Budget Scenario Analysis");
		section1.getDashboardItems().add(dashboardItem2);
		dashboardItem2.addGridHeaders(Arrays.asList("System Name", "Finding Desc", "Action to Reduce Risk", "Risk Reduction", "Project Cost", "ROI", "Decision"));

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

			dashboardItem2.getGridItems().add(Arrays.asList(
				sI(system.getName()).applyTextAlign("left").applyDrilldown(DashboardDataItemDrilldownDTO.of(system)),
				sI("Encryption Deprecated").applyTextAlign("left"),
				sI("Encryption Project").applyTextAlign("left"),
				// $I(quantMetricValue).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)),
				// sI(businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true)).applyTextAlign("left"),
				// $I(riskReductionPercent, "%").round(2),
				$I(riskReduction, "$").round(0),
				$I(toolCost, "$").round(0),
				$I(roi, "%").round(0),
				sI("None").applyTextAlign("left")
			));
		}

		return dashboard;
	}
}
