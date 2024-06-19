package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.DashboardType;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantsDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.service.UserService;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ResidualRiskDashboardService extends DashboardServiceBase {

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private AssessmentFindingsRepository assessmentFindingsRepository;


	@Autowired
	private ExposureMetricsDashboardService exposureMetricsDashboardService;

	@Autowired
	private RiskMetricsRepository riskMetricsRepository;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private SystemControlTestResultsRepository systemControlTestResultsRepository;

	@Autowired
	private ScoringQuestionsDashboardService scoringQuestionsDashboardService;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private VulnerabilityRepository vulnerabilityRepository;

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getResidualScoreDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

		// Check Active Filters
		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_RESIDUAL_CYBER_RISK, clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$DESCRIPTION), DashboardType.Admin);
		DashboardSectionDTO section = new DashboardSectionDTO(123421L, clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$RESIDUAL_CYBER_RISK$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$RESIDUAL_CYBER_RISK$ITEM_DESCRIPTION));
		dashboard.getSections().add(section);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_CISO(clientMessage);

		// Create breadcrumbs
		section.setBreadcrumbs(breadcrumbsTop.extend("RESIDUAL_CYBER_RISK", SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$NAME, "").getBreadcrumbs());

		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(11l, clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$RESIDUAL_CYBER_RISK$ITEM_NAME));
		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$RESIDUAL_CYBER_RISK$SUMMARY_SCORES$SYSTEM_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$RESIDUAL_CYBER_RISK$SUMMARY_SCORES$DATA_EXFILTRATION_EXPOSURE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$RESIDUAL_CYBER_RISK$SUMMARY_SCORES$INHERENT_RISK_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$RESIDUAL_CYBER_RISK$SUMMARY_SCORES$ASSESSMENT_SCORE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$INHERENT_RISK_ASSESSMENT_SCORE),
			clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_SUMMARY$POST_ASSESSMENT_SCORE_COLUMN),
			// clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$RESIDUAL_CYBER_RISK$SUMMARY_SCORES$POST_SECURITY_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$RESIDUAL_CYBER_RISK$SUMMARY_SCORES$RESIDUAL_RISK_SCORE_HEADER)
		), true);
		section.getDashboardItems().add(dashboardItem);

		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap = exposureMetricsDashboardService.getSystemsScoringData(riskModelId, null, Arrays.asList(QuantsDomain.DATA_EXFILTRATION));
		Set<Systems> systemsSet = systemScoringDataMap.keySet();
		List<Systems> allSystemsList = systemRepository.getAllByOrganizationAndNotEtl(riskModel.getOrganizationId());

		List<RiskMetrics> riskMetricList = riskMetricsRepository.getListByRiskModelIdAAndIsResidual(riskModelId, true);
		Map<Systems, Map<FormulaBuilder, FormulaResult>> riskMetricsDataMap = scoringQuestionsDashboardService.getRiskMetricsDataMap(riskModelId, riskMetricList);

		Map<Systems, AssessmentScoringResult> assessmentScoringDataMap = getControlTestScoringDataMap(riskModel.getOrganizationId(), null);
		Map<Systems, VulnerabilityScoringResult> vulnerabilityScoringDataMap = getVulnerabilityScoringDataMap(riskModel.getOrganizationId(), null);

		for (Systems system : allSystemsList) {
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.RESIDUAL_RISK, "system", system.getId().toString())));

			// Build Risk Formulas
			Double dataExfiltration = 0d;
			Map<QuantMetrics, ExposureMetricResult> exposures = systemScoringDataMap.get(system);
			if (exposures != null) {
				for (Map.Entry<QuantMetrics, ExposureMetricResult> exposure : exposures.entrySet()) {
					ExposureMetricResult metricResult = exposure.getValue();
					if (metricResult != null && metricResult.getResult() != null) {
						dataExfiltration += metricResult.getResult();
					}
				}
			}
			rowItems.add($I(dataExfiltration).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.RESIDUAL_RISK, "system", system.getId().toString())));

			Double inherentRiskScore = 0d;
			Map<FormulaBuilder, FormulaResult> riskMetricsData = riskMetricsDataMap.get(system);
			if (riskMetricsData != null && riskMetricsData.size() > 0) {
				for (Map.Entry<FormulaBuilder, FormulaResult> riskMetric : riskMetricsData.entrySet()) {
					inherentRiskScore = riskMetric.getValue().getResult();
					break;
				}
			}
			rowItems.add(dI(inherentRiskScore).round(3).applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.RESIDUAL_RISK, "system", system.getId().toString())));

			Double assessmentScore = 0d;
			if (assessmentScoringDataMap.containsKey(system)) {
				AssessmentScoringResult assessmentScoringResult = assessmentScoringDataMap.get(system);
				assessmentScore = assessmentScoringResult.calculateScore();
			}
			rowItems.add(dI(assessmentScore).round(3).applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.RESIDUAL_RISK, "system", system.getId().toString())));

			// Calculate POST Security Score
			Double postSecurity = inherentRiskScore * assessmentScore;
			Double postAssessment = inherentRiskScore * (1 - assessmentScore);
			rowItems.add(dI(postSecurity).round(3).applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.RESIDUAL_RISK, "system", system.getId().toString())));
			rowItems.add(dI(postAssessment).round(3).applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.RESIDUAL_RISK, "system", system.getId().toString())));

			Double residualScore = postAssessment;
			if (vulnerabilityScoringDataMap.containsKey(system)) {
				VulnerabilityScoringResult vulnerabilityScoringResult = vulnerabilityScoringDataMap.get(system);
				residualScore += vulnerabilityScoringResult.calculateScore();
			}
			rowItems.add(dI(residualScore).round(3).applyDrilldown(DashboardDataItemDrilldownDTO.of(DashboardDataItemDrilldownDTO.RESIDUAL_RISK, "system", system.getId().toString())));

			dashboardItem.getGridItems().add(rowItems);
		}

		return dashboard;
	}

	/**
	 * Create summary score dashboard for Assessment Scoring
	 *
	 * @param organizationId
	 * @return
	 */
	public Map<Systems, AssessmentScoringResult> getAssessmentScoringDataMap(Long organizationId) {
		return getAssessmentScoringDataMap(organizationId, null);
	}

	/**
	 * Create summary score dashboard for Assessment Scoring
	 *
	 * @param organizationId
	 * @return
	 */
	public Map<Systems, AssessmentScoringResult> getControlTestScoringDataMap(Long organizationId, List<Systems> systemListFilter) {
		Map<Systems, AssessmentScoringResult> result = new HashMap<>();

		List<SystemControlTestResults> systemControlTestResultsList;
		if (systemListFilter != null && systemListFilter.size() > 0) {
			List<Long> systemIds = systemListFilter.stream().mapToLong(Systems::getId).boxed().collect(Collectors.toList());
			systemControlTestResultsList = systemControlTestResultsRepository.getListByOrganizationAndSystemIds(organizationId, systemIds);
		} else {
			systemControlTestResultsList = systemControlTestResultsRepository.getListByOrganization(organizationId);
		}

		for (SystemControlTestResults systemControlTestResult : systemControlTestResultsList) {
			Systems system = systemControlTestResult.getSystem();

			AssessmentScoringResult assessmentScoring = result.get(system);
			if (assessmentScoring == null) {
				assessmentScoring = new AssessmentScoringResult();
				result.put(system, assessmentScoring);
			}

			for (SystemRequirementControlTestResults systemRequirementControlTestResult : systemControlTestResult.getSystemRequirementControlTestResults()) {
				ControlMaturities controlMaturity = systemRequirementControlTestResult.getControlMaturity();
				if (controlMaturity != null && controlMaturity.getWeight() != null && controlMaturity.getValue() != null && (controlMaturity.getValue() > 0 || controlMaturity.getWeight() > 0)) {
					assessmentScoring.add(controlMaturity.getWeight(), 1d);
				}
			}
		}

		return result;
	}

	/**
	 * Create summary score dashboard for Assessment Scoring
	 *
	 * @param organizationId
	 * @return
	 */
	public Map<Systems, AssessmentScoringResult> getAssessmentScoringDataMap(Long organizationId, List<Systems> systemListFilter) {
		Map<Systems, AssessmentScoringResult> result = new HashMap<>();

		List<AssessmentFindings> assessmentFindingsList;
		if (systemListFilter != null && systemListFilter.size() > 0) {
			List<Long> systemIds = systemListFilter.stream().mapToLong(Systems::getId).boxed().collect(Collectors.toList());
			assessmentFindingsList = assessmentFindingsRepository.getAllListByOrganizationAndSystems(organizationId, systemIds);
		} else {
			assessmentFindingsList = assessmentFindingsRepository.getAllListByOrganization(organizationId);
		}

		for (AssessmentFindings assessmentFinding : assessmentFindingsList) {
			for (Assessments assessment : assessmentFinding.getAssessments()) {
				for (Systems system : assessment.getSystems()) {
					// Obtain Assessment Scoring
					AssessmentScoringResult assessmentScoring = result.get(system);
					if (assessmentScoring == null) {
						assessmentScoring = new AssessmentScoringResult();
						result.put(system, assessmentScoring);
					}

					assessmentScoring.add(assessmentFinding);
				}
			}
		}

		return result;
	}

	/**
	 * Create summary score dashboard for Vulnerability Scoring
	 *
	 * @param organizationId
	 * @return
	 */
	public Map<Systems, VulnerabilityScoringResult> getVulnerabilityScoringDataMap(Long organizationId, List<Systems> systemListFilter) {
		Map<Systems, VulnerabilityScoringResult> result = new HashMap<>();

		List<Vulnerabilities> vulnerabilitiesList;
		if (systemListFilter != null && systemListFilter.size() > 0) {
			List<Long> systemIds = systemListFilter.stream().mapToLong(Systems::getId).boxed().collect(Collectors.toList());
			vulnerabilitiesList = vulnerabilityRepository.getAllListByOrganizationAndSystems(organizationId, systemIds);
		} else {
			vulnerabilitiesList = vulnerabilityRepository.getListByOrganization(organizationId);
		}

		for (Vulnerabilities vulnerability : vulnerabilitiesList) {
			for (Technologies technology : vulnerability.getTechnologies()) {
				for (Systems system : technology.getSystems()) {
					// Obtain Assessment Scoring
					VulnerabilityScoringResult vulnerabilityScoring = result.get(system);
					if (vulnerabilityScoring == null) {
						vulnerabilityScoring = new VulnerabilityScoringResult();
						result.put(system, vulnerabilityScoring);
					}

					vulnerabilityScoring.add(vulnerability);
				}
			}
		}

		return result;
	}


	/**
	 * Build Residual Risk Scoring drilldown
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	public void buildResidualScoringDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

		String itemIdString = drilldown.getParams().get(DashboardDataItemDrilldownDTO.PARAM_ITEM);
		Long itemId = Long.parseLong(itemIdString);

		// QualitativeQuestions question = answer.get().getQualitativeQuestion();
		Systems system = systemRepository.findById(itemId).get();

		// Define Header Information
		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$ITEM_NAME), system.getName()));

		// Get Scoring Data
		Map<Systems, Map<MetricDomains, MetricResult>> systemScoringDataMap = scoringQuestionsDashboardService.getSystemsScoringData(riskModelId, Arrays.asList(VendorType.System), Arrays.asList(system));
		Map<MetricDomains, MetricResult> systemScoringData = systemScoringDataMap.get(system);

		// Build Scoring SUMMARY
		DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(1010l, clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_SUMMARY$ITEM_NAME));
		section1.getDashboardItems().add(dashboardItem1);

		// Create System User Assignments Dashboard
		List<FormulaBuilder> riskMetricsFormulaBuilders = scoringQuestionsDashboardService.getRiskScoringMetricsFormulaBuilders(riskModelId, null);
		// DashboardTableItemDTO dashboardItem2 = new DashboardTableItemDTO(1010l, "Scoring Risk Metrics");
		// section1.getDashboardItems().add(dashboardItem2);
		// dashboardItem2.addGridHeaders(Arrays.asList("Metric Name", "Formula", "Target Value"));
		FormulaBuilder inherentRisk = null;
		Double inherentRiskScoring = 0d;
		for (FormulaBuilder formulaBuilder : riskMetricsFormulaBuilders) {
			// Scoring SUMMARY
			if (formulaBuilder.getRiskMetric() != null && Boolean.TRUE.equals(formulaBuilder.getRiskMetric().getIsResidual())) {
				inherentRisk = formulaBuilder;
				Double formulaValue = formulaBuilder.calculate(systemScoringData);
				inherentRiskScoring = formulaValue;
			}
		}

		// Assessment Scoring details
		Map<Systems, AssessmentScoringResult> assessmentScoringDataMap = getControlTestScoringDataMap(riskModel.getOrganizationId(), Arrays.asList(system));
		AssessmentScoringResult assessmentScoringResult = assessmentScoringDataMap.get(system);
		DashboardTableItemDTO dashboardItem4 = new DashboardTableItemDTO(1010l, clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$ASSESSMENT_SCORING_METRICS$ITEM_NAME));
		section1.getDashboardItems().add(dashboardItem4);
		dashboardItem4.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$ASSESSMENT_SCORING_METRICS$ASSESSMENT_FINDING_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$ASSESSMENT_SCORING_METRICS$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$ASSESSMENT_SCORING_METRICS$WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$ASSESSMENT_SCORING_METRICS$WEIGHT_SCORE_HEADER)
		));
		if (assessmentScoringResult != null && assessmentScoringResult.getAssessmentFindings() != null) {
			for (int i = 0; i < assessmentScoringResult.getAssessmentFindings().size(); i++) {
				AssessmentFindings assessmentFinding = assessmentScoringResult.getAssessmentFindings().get(i);
				double weight = assessmentScoringResult.getWeights().get(i);
				double score = assessmentScoringResult.getScores().get(i);
				double weightedScore = assessmentScoringResult.getWeightedScores().get(i);
				dashboardItem4.getGridItems().add(
					Arrays.asList(
						sI(assessmentFinding.getName()),
						dI(score).round(2).applyTextAlign("right"),
						dI(weight).round(2).applyTextAlign("right"),
						dI(weightedScore).round(2).applyTextAlign("right")
					)
				);
			}
			dashboardItem4.getGridItems().add(
				Arrays.asList(
					sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$ASSESSMENT_SCORING_METRICS$TOTAL_COLUMN)).applyTextAlign("right").applyColspan(2l),
					dI(assessmentScoringResult.getCumulativeWeight()).round(2).applyTextAlign("right"),
					dI(assessmentScoringResult.getCumulativeWeightedScore()).round(2).applyTextAlign("right")
				)
			);
			dashboardItem4.getGridItems().add(
				Arrays.asList(
					sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$ASSESSMENT_SCORING_METRICS$ASSESSMENT_SCORE_COLUMN)).applyTextAlign("right").applyColspan(2l),
					sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$ASSESSMENT_SCORING_METRICS$ASSESSMENT_SCORE_FORMULA_COLUMN)).applyTextAlign("right"),
					dI(assessmentScoringResult.calculateScore()).round(2).applyTextAlign("right")
				)
			);
		}

		// Vulnerability Scoring
		Map<Systems, VulnerabilityScoringResult> vulnerabilitiesScoringDataMap = getVulnerabilityScoringDataMap(riskModel.getOrganizationId(), Arrays.asList(system));
		VulnerabilityScoringResult vulnerabilityScoringResult = vulnerabilitiesScoringDataMap.get(system);
		DashboardTableItemDTO dashboardItem5 = new DashboardTableItemDTO(1010l, clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$VULNERABILITY_SCORING_METRICS$ITEM_NAME));
		section1.getDashboardItems().add(dashboardItem5);
		dashboardItem5.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$VULNERABILITY_SCORING_METRICS$VULNERABILITY_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$VULNERABILITY_SCORING_METRICS$CODE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$VULNERABILITY_SCORING_METRICS$SCORE_HEADER)
		));
		if (vulnerabilityScoringResult != null && vulnerabilityScoringResult.getVulnerabilities() != null) {
			for (int i = 0; i < vulnerabilityScoringResult.getVulnerabilities().size(); i++) {
				Vulnerabilities vulnerability = vulnerabilityScoringResult.getVulnerabilities().get(i);
				dashboardItem5.getGridItems().add(
					Arrays.asList(
						sI(vulnerability.getName()),
						sI(vulnerability.getCode()),
						dI(vulnerability.getScore()).round(2).applyTextAlign("right")
					)
				);
			}
			dashboardItem5.getGridItems().add(
				Arrays.asList(
					sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$VULNERABILITY_SCORING_METRICS$MAX_SCORE_COLUMN)).applyTextAlign("right").applyColspan(2l),
					dI(vulnerabilityScoringResult.getResult()).round(2).applyTextAlign("right")
				)
			);
		}

		// SUMMARY
		double assessmentScoring = assessmentScoringResult != null ? assessmentScoringResult.calculateScore() : 0d;
		double postSecurityScoring = inherentRiskScoring * assessmentScoring;
		double postAssessmentScoring = inherentRiskScoring * (1 - assessmentScoring);
		double vulnerabilityScoring = (vulnerabilityScoringResult != null) ? vulnerabilityScoringResult.calculateScore() : 0d;
		if (inherentRisk != null) {
			dashboardItem1.getGridItems().add(
				Arrays.asList(
					sI(inherentRisk.getName()),
					sI(inherentRisk.getFormulaString()),
					dI(inherentRiskScoring).round(2)
				)
			);
		}
		dashboardItem1.getGridItems().add(
			Arrays.asList(
				sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_SUMMARY$ASSESSMENT_SCORE_COLUMN)), // Assessment Score
				sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_SUMMARY$ASSESSMENT_SCORE_FORMULA_COLUMN)), // Σ(Weight * Score) / Σ(Weight)
				dI(assessmentScoring).round(2).applyTextAlign("right")
			)
		);
		dashboardItem1.getGridItems().add(
			Arrays.asList(
				sI(clientMessage.getMessage("Post Security Score")), // Post Security Score
				sI(clientMessage.getMessage("Assessment Score * Inherent Risk")), // Assessment Score * Inherent Risk
				dI(postSecurityScoring).round(2).applyTextAlign("right")
			)
		);
		dashboardItem1.getGridItems().add(
			Arrays.asList(
				sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_SUMMARY$POST_ASSESSMENT_SCORE_COLUMN)), // Post Assessment Score
				sI(clientMessage.getMessage("Inherent Risk - Assessment Score * Inherent Risk")), // Inherent Risk - Assessment Score * Inherent Risk
				// sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_SUMMARY$POST_ASSESSMENT_SCORE_FORMULA_COLUMN)), // Inherent Risk - Assessment Score * Inherent Risk
				dI(postAssessmentScoring).round(2).applyTextAlign("right")
			)
		);
		dashboardItem1.getGridItems().add(
			Arrays.asList(
				sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_SUMMARY$VULNERABILITY_RISK_SCORE_COLUMN)), // Vulnerability Risk Score
				sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_SUMMARY$VULNERABILITY_RISK_SCORE_FORMULA_COLUMN)), // MAX(Vulnerability Score)
				dI(vulnerabilityScoring).round(2).applyTextAlign("right")
			)
		);
		dashboardItem1.getGridItems().add(
			Arrays.asList(
				sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_SUMMARY$RESIDUAL_RISK_SCORE_COLUMN)), // Residual Risk Score
				sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_SUMMARY$RESIDUAL_RISK_SCORE_FORMULA_COLUMN)), // Post Assessment Score + Vulnerability Score
				dI(postAssessmentScoring + vulnerabilityScoring).round(2).applyTextAlign("right")
			)
		);

		// Build Summary Dashboard
		DashboardTableItemDTO dashboardItem3 = new DashboardTableItemDTO(1010l, clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_METRICS$ITEM_NAME));
		section1.getDashboardItems().add(dashboardItem3);
		dashboardItem3.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_METRICS$METRIC_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_METRICS$FORMULA_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_METRICS$TARGET_VALUE_HEADER)
		));
		for (Map.Entry<MetricDomains, MetricResult> systemScoringDataEntry : systemScoringData.entrySet()) {
			dashboardItem3.getGridItems().add(
				Arrays.asList(
					sI(systemScoringDataEntry.getKey().getName()),
					sI(clientMessage.getMessage(SLCT.DRILLDOWNS$RISIDUAL_SCORING$SCORING_METRICS$FURMULA_COLUMN)),
					qualCondBGColor(dI(systemScoringDataEntry.getValue().buildNormalizedResult()).round())
				)
			);
		}


		/*
		List<MetricDomains> metricDomains = metricDomainRepository.findAll();
		Map<MetricDomains, DashboardItemDTO> dashboardMetricsMap = getMetricDomainsDashboardItemsMapByData(metricDomains, systemScoringData);
		for (MetricDomains metricDomain : metricDomains) {
			DashboardItemDTO dashboardItem3 = dashboardMetricsMap.get(metricDomain);
			section1.getDashboardItems().add(dashboardItem3);
		}
		*/
	}

}
