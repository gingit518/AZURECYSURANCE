package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.MetricDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.domains.VariableType;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.service.BusinessUnitService;
import com.cyberintech.vrisk.server.service.SystemsService;
import com.cyberintech.vrisk.server.service.UserService;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scoring Questions Dashboard Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-02-27
 */
@Service
@Slf4j
public class ScoringQuestionsDashboardService extends DashboardServiceBase {

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private ControlMaturitiesRepository controlMaturitiesRepository;

	@Autowired
	private MetricDomainRepository metricDomainRepository;

	@Autowired
	private RiskMetricsRepository riskMetricsRepository;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private SystemRequirementControlTestResultsRepository systemRequirementControlTestResultsRepository;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private QualitativeQuestionRepository qualitativeQuestionRepository;

	@Autowired
	private QuestionAnswersForVendorRepository questionAnswersForVendorRepository;

	@Autowired
	private QuestionAnswersForSystemRepository questionAnswersForSystemRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private UserAssignedSystemRepository userAssignedSystemRepository;

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public Map<Systems, Map<MetricDomains, MetricResult>> getSystemsScoringData(Long riskModelId, List<VendorType> scoringTypes) {
		return getSystemsScoringData(riskModelId, scoringTypes, null);
	}

	/**
	 * Get organization QUAL scoring data
	 *
	 * @param riskModelId
	 * @param scoringTypes
	 * @return
	 */
	public Map<MetricDomains, MetricResult> getOrganizationScoringData(Long riskModelId, List<VendorType> scoringTypes) {

		// Registering Result Data Set
		Map<MetricDomains, MetricResult> result = new HashMap<>();

		// Obtain Most Valuable data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<Long> organizationFilter = Arrays.asList(riskModel.getOrganizationId());
		List<QuestionAnswersForVendor> allQuestionsAnswersList = questionAnswersForVendorRepository.getListByRiskModelAndScoringTypesAndVendors(riskModelId, scoringTypes, organizationFilter);
		List<QualitativeQuestions> allQuestionsList = qualitativeQuestionRepository.getListOfNotInternalByRiskModelIdAndTypes(riskModelId, scoringTypes, Arrays.asList(VendorType.VendorInternal, VendorType.CloudInternal));

		List<MetricDomains> metricDomains = metricDomainRepository.findAll();
		Map<MetricDomains, List<QualitativeQuestions>> questionsMetricsMap = allQuestionsList.stream().collect(Collectors.groupingBy(question -> question.getQualitativeMetric().getMetricDomain()));
		// questionAnswersForOrganization.getQuestion().getQualitativeMetric().getMetricDomain()
		Map<MetricDomains, List<QuestionAnswersForVendor>> questionAnswersMetricsMap = allQuestionsAnswersList.stream().collect(Collectors.groupingBy(questionAnswersForVendor -> questionAnswersForVendor.getQuestion().getQualitativeMetric().getMetricDomain()));
		Map<MetricDomains, MetricStatistics> metricQuestionStatsMap = metricDomains.stream().collect(Collectors.toMap(domain -> domain, domain -> MetricStatistics.of(questionsMetricsMap.get(domain))));

		// Preparing Result
		// Create Empty Metric Result
		for (MetricDomains domain : metricDomains) {
			// Create Default Metric Result
			MetricResult<QuestionAnswersForVendor> metricResult = new MetricResult(domain.getName(), 0d);
			metricResult.setMetricStatistic(metricQuestionStatsMap.get(domain));
			result.put(domain, metricResult);

			// Process Organization Question Details
			if (questionAnswersMetricsMap.containsKey(domain)) {
				List<QuestionAnswersForVendor> metricQuestionAnswers = questionAnswersMetricsMap.get(domain);
				metricResult.setQuestionAnswers(metricQuestionAnswers);

				Double currMaxMetricValue = 0d;
				for (QuestionAnswersForVendor questionAnswer : metricQuestionAnswers) {

					// Obtain Max Question answers Weight
					if (questionAnswer.getQuestion() != null) {
						double maxQuestionWeight = 1;
						for (QualitativeQuestionAnswers qualitativeQuestionAnswers : questionAnswer.getQuestion().getAnswers()) {
							if (qualitativeQuestionAnswers.getAnswerWeight() != null && maxQuestionWeight < qualitativeQuestionAnswers.getAnswerWeight().getValue()) {
								maxQuestionWeight = qualitativeQuestionAnswers.getAnswerWeight().getValue();
							}
						}
						if (questionAnswer.getQuestion().getQuestionWeight() != null) {
							currMaxMetricValue += Double.valueOf(maxQuestionWeight * questionAnswer.getQuestion().getQuestionWeight().getValue());
						} else {
							log.warn(MessageFormat.format("Question Weight not defined for question. [{0}: {1}]", questionAnswer.getQuestion().getId(), questionAnswer.getQuestion().getQuestion()));
						}
					}

					double answerWeight = questionAnswer.getAnswer() != null && questionAnswer.getAnswer().getAnswerWeight() != null ? questionAnswer.getAnswer().getAnswerWeight().getValue() : 0;
					double questionWeight = questionAnswer.getQuestion() != null && questionAnswer.getQuestion().getQuestionWeight() != null ? questionAnswer.getQuestion().getQuestionWeight().getValue() : 0;

					metricResult.setResult(metricResult.getResult() + answerWeight * questionWeight);
					metricResult.setMaxQuestionsAnswersWeight(currMaxMetricValue);
				}
			}
		}

		return result;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public Map<Systems, Map<MetricDomains, MetricResult>> getSystemsScoringData(Long riskModelId, List<VendorType> scoringTypes, List<Systems> systemListFilter) {

		// Registering Result Data Set
		Map<Systems, Map<MetricDomains, MetricResult>> result = new HashMap<>();

		// Obtain Most Valuable data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<Systems> allSystemsList;
		List<QuestionAnswersForSystem> allQuestionsAnswersList;
		if (systemListFilter == null || systemListFilter.size() == 0) {
			allSystemsList = systemRepository.getAllByOrganizationAndNotEtl(riskModel.getOrganizationId());
			allQuestionsAnswersList = questionAnswersForSystemRepository.getListByRiskModelAndScoringTypes(riskModelId, scoringTypes);
		} else {
			allSystemsList = systemListFilter;
			List<Long> systemIdsList = allSystemsList.stream().mapToLong(Systems::getId).boxed().collect(Collectors.toList());
			allQuestionsAnswersList = questionAnswersForSystemRepository.getListByRiskModelAndScoringTypesAndSystems(riskModelId, scoringTypes, systemIdsList);
		}

		List<QualitativeQuestions> allQuestionsList = qualitativeQuestionRepository.getListOfNotInternalByRiskModelIdAndTypes(riskModelId, scoringTypes, Arrays.asList(VendorType.VendorInternal, VendorType.CloudInternal));

		List<MetricDomains> metricDomains = metricDomainRepository.findAll();
		Map<MetricDomains, List<QualitativeQuestions>> questionsMetricsMap = allQuestionsList.stream().collect(Collectors.groupingBy(question -> question.getQualitativeMetric().getMetricDomain()));
		Map<Systems, Map<MetricDomains, List<QuestionAnswersForSystem>>> systemQuestionAnswersMetricsMap = allQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForSystem::getSystem, Collectors.groupingBy(questionAnswersForSystem -> questionAnswersForSystem.getQuestion().getQualitativeMetric().getMetricDomain())));
		Map<MetricDomains, MetricStatistics> metricQuestionStatsMap = metricDomains.stream().collect(Collectors.toMap(domain -> domain, domain -> MetricStatistics.of(questionsMetricsMap.get(domain))));

		// Preparing Result
		for (Systems system : allSystemsList) {
			// Create Empty Metric Result
			Map<MetricDomains, MetricResult> metricResultMap = new HashMap<>();
			for (MetricDomains domain : metricDomains) {
				// Create Default Metric Result
				MetricResult<QuestionAnswersForSystem> metricResult = new MetricResult(domain.getName(), 0d);
				metricResult.setMetricStatistic(metricQuestionStatsMap.get(domain));
				metricResultMap.put(domain, metricResult);

				// Process System Question Details
				if (systemQuestionAnswersMetricsMap.containsKey(system) && systemQuestionAnswersMetricsMap.get(system).containsKey(domain)) {
					List<QuestionAnswersForSystem> metricQuestionAnswers = systemQuestionAnswersMetricsMap.get(system).get(domain);
					metricResult.setQuestionAnswers(metricQuestionAnswers);

					Double currMaxMetricValue = 0d;
					for (QuestionAnswersForSystem questionAnswer : metricQuestionAnswers) {

						// Obtain Max Question answers Weight
						if (questionAnswer.getQuestion() != null) {
							double maxQuestionWeight = 1;
							for (QualitativeQuestionAnswers qualitativeQuestionAnswers : questionAnswer.getQuestion().getAnswers()) {
								if (qualitativeQuestionAnswers.getAnswerWeight() != null && maxQuestionWeight < qualitativeQuestionAnswers.getAnswerWeight().getValue()) {
									maxQuestionWeight = qualitativeQuestionAnswers.getAnswerWeight().getValue();
								}
							}
							if (questionAnswer.getQuestion().getQuestionWeight() != null) {
								currMaxMetricValue += Double.valueOf(maxQuestionWeight * questionAnswer.getQuestion().getQuestionWeight().getValue());
							} else {
								log.warn(MessageFormat.format("Question Weight not defined for question. [{0}: {1}]", questionAnswer.getQuestion().getId(), questionAnswer.getQuestion().getQuestion()));
							}
						}

						double answerWeight = questionAnswer.getAnswer() != null && questionAnswer.getAnswer().getAnswerWeight() != null ? questionAnswer.getAnswer().getAnswerWeight().getValue() : 0;
						double questionWeight = questionAnswer.getQuestion() != null && questionAnswer.getQuestion().getQuestionWeight() != null ? questionAnswer.getQuestion().getQuestionWeight().getValue() : 0;

						metricResult.setResult(metricResult.getResult() + answerWeight * questionWeight);
						metricResult.setMaxQuestionsAnswersWeight(currMaxMetricValue);
					}
				}
			}
			result.put(system, metricResultMap);
		}

		return result;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public Map<Systems, MetricResult> getSystemsMitigateData(Long riskModelId) {

		// Registering Result Data Set
		Map<Systems, MetricResult> result = new HashMap<>();

		// Obtain Most Valuable data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<Systems> allSystemsList = systemRepository.getAllByOrganizationAndNotEtl(riskModel.getOrganizationId());
		/*
		if (systemListFilter == null || systemListFilter.size() == 0) {
			allSystemsList = systemRepository.getAllByOrganizationAndNotEtl(riskModel.getOrganizationId());
		} else {
			allSystemsList = systemListFilter;
			List<Long> systemIdsList = allSystemsList.stream().mapToLong(Systems::getId).boxed().collect(Collectors.toList());
		}
		*/

		Set<SystemRequirementControlTestResults> allQuestionsAnswersList = systemRequirementControlTestResultsRepository.findAllByOrganizationId(riskModel.getOrganizationId());
		Map<Systems, List<SystemRequirementControlTestResults>> systemAnswersMap = allQuestionsAnswersList.stream().collect(Collectors.groupingBy(SystemRequirementControlTestResults::getSystem));
		List<ControlMaturities> allMaturities = controlMaturitiesRepository.getListByOrganizationAndName(riskModel.getOrganizationId(), "", PageRequest.of(0, Integer.MAX_VALUE));
		OptionalDouble maxMaturity = allMaturities.stream().filter(controlMaturities -> controlMaturities.getValue() != null && controlMaturities.getWeight() != null).mapToDouble(controlMaturities -> controlMaturities.getValue() * controlMaturities.getWeight()).max();

		// Preparing Result
		for (Systems system : allSystemsList) {
			// Create Empty Metric Result
			MetricResult metricResult = new MetricResult("Mitigation risk", 0d);
			List<SystemRequirementControlTestResults> systemAnswers = systemAnswersMap.get(system);
			if (CollectionUtils.isNotEmpty(systemAnswers)) {
				Double maturityValue = systemAnswers.stream().filter(value -> value.getControlMaturity() != null).mapToDouble(value -> value.getControlMaturity().getValue() * value.getControlMaturity().getWeight()).sum() / systemAnswers.size();
				maturityValue = maturityValue / maxMaturity.orElse(1d);
				metricResult.setResult(maturityValue);
			}

			result.put(system, metricResult);
		}

		return result;
	}


	/**
	 * Get Maturities for System
	 *
	 * @return Dashboard
	 */
	public MetricResult getSystemMitigateData(Long riskModelId, Systems system) {

		// Obtain Most Valuable data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<ControlMaturities> allMaturities = controlMaturitiesRepository.getListByOrganizationAndName(riskModel.getOrganizationId(), "", PageRequest.of(0, Integer.MAX_VALUE));
		OptionalDouble maxMaturity = allMaturities.stream().filter(controlMaturities -> controlMaturities.getValue() != null && controlMaturities.getWeight() != null).mapToDouble(controlMaturities -> controlMaturities.getValue() * controlMaturities.getWeight()).max();

		// Create Empty Metric Result
		MetricResult metricResult = new MetricResult("Mitigation risk", 0d);
		Set<SystemRequirementControlTestResults> systemAnswers = systemRequirementControlTestResultsRepository.findAllBySystemIdAndOrganizationId(system.getId(), riskModel.getOrganizationId());
		if (CollectionUtils.isNotEmpty(systemAnswers)) {
			Double maturityValue = systemAnswers.stream().filter(value -> value.getControlMaturity() != null).mapToDouble(value -> value.getControlMaturity().getValue() * value.getControlMaturity().getWeight()).sum() / systemAnswers.size();
			maturityValue = maturityValue / maxMaturity.orElse(1d);
			metricResult.setResult(maturityValue);
		}
		return metricResult;
	}

	/**
	 * Create summary score dashboard for Scoring Types
	 *
	 * @param riskModelId
	 * @param riskMetrics
	 * @return
	 */
	public Map<Systems, Map<FormulaBuilder, FormulaResult>> getRiskMetricsDataMap(Long riskModelId, List<RiskMetrics> riskMetrics) {
		// Build Organization Summary Scores Dashboard
		Map<Systems, Map<FormulaBuilder, FormulaResult>> result = new HashMap<>();

		Map<Systems, Map<MetricDomains, MetricResult>> systemsDataMap = getSystemsScoringData(riskModelId, Arrays.asList(VendorType.System));
		Map<Systems, MetricResult> systemMitigateDataMap = getSystemsMitigateData(riskModelId);
		// List<MetricDomains> metricDomains = metricDomainRepository.findAll();

		List<FormulaBuilder> riskMetricsFormulaBuilders = getRiskScoringMetricsFormulaBuilders(riskModelId, riskMetrics);

		for (Map.Entry<Systems, Map<MetricDomains, MetricResult>> entry : systemsDataMap.entrySet()) {
			Systems system = entry.getKey();
			Map<String, MetricResult> metricResultMap = new HashMap<>();
			if (systemMitigateDataMap.containsKey(system)) metricResultMap.put(VariableType.MITIGATION_RISK.name(), systemMitigateDataMap.get(system));
			for (Map.Entry<MetricDomains, MetricResult> metricMapItem : entry.getValue().entrySet()) {
				metricResultMap.put(metricMapItem.getKey().getCode(), metricMapItem.getValue());
			}
			Map<FormulaBuilder, FormulaResult> systemFormulaData = new HashMap<>();
			result.put(system, systemFormulaData);

			// Build Risk Formulas
			for (FormulaBuilder formulaBuilder : riskMetricsFormulaBuilders) {
				Double formulaValue = formulaBuilder.calculate2(metricResultMap);

				systemFormulaData.put(formulaBuilder, new FormulaResult(formulaBuilder.getFormulaString(), formulaValue));
			}
		}

		return result;
	}

	/**
	 * Create summary score dashboard for Scoring Types
	 *
	 * @param riskModelId
	 * @param scoringTypes
	 * @return
	 */
	public DashboardItemDTO createSummaryScoresDashboardItem(Long riskModelId, List<VendorType> scoringTypes) {
		// Build Organization Summary Scores Dashboard
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(11l, clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_RISK$SUMMARY_SCORES$ITEM_NAME));
		Map<Systems, Map<MetricDomains, MetricResult>> systemsDataMap = getSystemsScoringData(riskModelId, scoringTypes);
		List<MetricDomains> metricDomains = metricDomainRepository.getAllByTypeAndNotEmpty(VendorType.System, riskModelId);

		List<FormulaBuilder> riskMetricsFormulaBuilders = getRiskScoringMetricsFormulaBuilders(riskModelId, null);

		List<String> headerList = new ArrayList<>();
		headerList.add(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_RISK$SUMMARY_SCORES$SYSTEM_HEADER));
		for (FormulaBuilder formulaBuilder : riskMetricsFormulaBuilders) {
			headerList.add(formulaBuilder.getName());
		}
		for (MetricDomains domain : metricDomains) {
			headerList.add(domain.getName());
		}

		// Systems Mitigations Map
		Map<Systems, MetricResult> systemMitigateDataMap = getSystemsMitigateData(riskModelId);

		dashboardItem.addGridHeaders(headerList, true);
		for (Map.Entry<Systems, Map<MetricDomains, MetricResult>> entry : systemsDataMap.entrySet()) {
			Systems system = entry.getKey();
			Map<MetricDomains, MetricResult> metricResultMap = entry.getValue();
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.of(system)));

			// Add System Mitigation Risk
			if (systemMitigateDataMap.containsKey(system)) {
				MetricDomains mitigationRiskDomain = new MetricDomains();
				mitigationRiskDomain.setId(-100L);
				mitigationRiskDomain.setCode(VariableType.MITIGATION_RISK.name());
				mitigationRiskDomain.setName(VariableType.MITIGATION_RISK.name());
				metricResultMap.put(mitigationRiskDomain, systemMitigateDataMap.get(system));
			}

			// Build Risk Formulas
			for (FormulaBuilder formulaBuilder : riskMetricsFormulaBuilders) {
				Double formulaValue = formulaBuilder.calculate(metricResultMap);
				rowItems.add(sI(formulaValue).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system)));
			}

			// Build Metrics Part
			for (MetricDomains domain : metricDomains) {
				MetricResult metricResult = metricResultMap.get(domain);
				rowItems.add(sI(metricResult.buildNormalizedResult()).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, MetricDomain.of(domain.getId()))));
			}
			dashboardItem.getGridItems().add(rowItems);
		}
		return dashboardItem;
	}

	/**
	 * Get Scoring Questions drilldown for the System And Metric
	 *
	 * @return Dashboard
	 */
	public DashboardItemDTO createSystemScoringMetricDrilldownDashboardItems(Long riskModelId, Systems system, List<VendorType> scoringTypes, MetricDomain metricDomain) {
		MetricDomains metricDoman = metricDomainRepository.findFirstById(metricDomain.getId());

		Map<MetricDomains, DashboardItemDTO> itemsMap = createSystemScoringDrilldownDashboardItems(riskModelId, system, scoringTypes, Arrays.asList(metricDoman));

		return itemsMap.get(metricDoman);
	}

	/**
	 * Get Scoring Questions drilldown for the System And Metric
	 *
	 * @return Dashboard
	 */
	public Map<MetricDomains, DashboardItemDTO> createSystemScoringDrilldownDashboardItems(Long riskModelId, Systems system, List<VendorType> scoringTypes, List<MetricDomains> metricDomains) {

		Map<Systems, Map<MetricDomains, MetricResult>> systemScoringDataMap = getSystemsScoringData(riskModelId, scoringTypes, Arrays.asList(system));
		Map<MetricDomains, MetricResult> systemScoringData = systemScoringDataMap.get(system);

		return getMetricDomainsDashboardItemsMapByData(metricDomains, systemScoringData);
	}

	private Map<MetricDomains, DashboardItemDTO> getMetricDomainsDashboardItemsMapByData(List<MetricDomains> metricDomains, Map<MetricDomains, MetricResult> systemScoringData) {
		Map<MetricDomains, DashboardItemDTO> result = new HashMap<>();
		for (MetricDomains domain : metricDomains) {
			DashboardTableItemDTO dashboard = new DashboardTableItemDTO(1001L, MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUALS_DOMAIN$TABLE$ITEM_NAME), domain));
			dashboard.addGridHeaders(Arrays.asList(
				clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUALS_DOMAIN$TABLE$QUESTION_HEADER),
				clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUALS_DOMAIN$TABLE$WEIGHT_HEADER),
				clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUALS_DOMAIN$TABLE$ANSWER_HEADER),
				clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUALS_DOMAIN$TABLE$ANSWER_WEIGHT_HEADER),
				clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUALS_DOMAIN$TABLE$SCORE_HEADER),
				clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUALS_DOMAIN$TABLE$MAX_SCORE_HEADER)
			), true);
			result.put(domain, dashboard);

			Double metricValue = Double.valueOf(0);
			Double maxMetricValue = Double.valueOf(0);
			MetricResult<QuestionAnswersForSystem> metricResult = systemScoringData.get(domain);
			Map<QualitativeQuestions, QuestionAnswersForSystem> questionAnswersMap = new HashMap<>();
			Optional.ofNullable(metricResult.getQuestionAnswers()).orElse(new ArrayList<>()).stream().forEach(questionAnswersForSystem -> {
				questionAnswersMap.put(questionAnswersForSystem.getQuestion(), questionAnswersForSystem);
			});
			for (QualitativeQuestions question : metricResult.getMetricStatistic().getQualitativeQuestionList()) {

				// Obtain Max Question answers Weight
				double maxQuestionWeight = 1;
				for (QualitativeQuestionAnswers qualitativeQuestionAnswers : question.getAnswers()) {
					if (qualitativeQuestionAnswers.getAnswerWeight() != null && maxQuestionWeight < qualitativeQuestionAnswers.getAnswerWeight().getValue()) {
						maxQuestionWeight = qualitativeQuestionAnswers.getAnswerWeight().getValue();
					}
				}

				QuestionAnswersForSystem questionAnswersForSystem = questionAnswersMap.get(question);
				Long questionWeight = question.getQuestionWeight() != null ? question.getQuestionWeight().getValue() : 0;
				String answer = questionAnswersForSystem != null && questionAnswersForSystem.getAnswer() != null
					? questionAnswersForSystem.getAnswer().getAnswer() : clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_ANSWERED);

				String answerWeight = "-";
				Double currMetricValue = 0d;
				Double currMaxMetricValue = 0d;
				if (questionAnswersForSystem != null && questionAnswersForSystem.getAnswer() != null && questionAnswersForSystem.getAnswer().getAnswerWeight() != null) {
					answerWeight = questionAnswersForSystem.getAnswer().getAnswerWeight().getValue().toString();
					currMetricValue = Double.valueOf(questionAnswersForSystem.getAnswer().getAnswerWeight().getValue() * questionWeight);
					currMaxMetricValue = Double.valueOf(maxQuestionWeight * questionWeight);
				}

				List<DashboardDataItemDTO> questionDetails = new ArrayList<>();
				questionDetails.add(sI(question.getQuestion()));
				questionDetails.add(sI(questionWeight.toString()).applyTextAlign("right"));
				questionDetails.add(sI(answer));
				questionDetails.add(sI(answerWeight).applyTextAlign("right"));
				questionDetails.add(dI(currMetricValue).round(0));
				questionDetails.add(dI(currMaxMetricValue).round(0));
				dashboard.getGridItems().add(questionDetails);

				if (questionAnswersForSystem == null || questionAnswersForSystem.getAnswer() == null) {
					questionDetails.stream().forEach(dashboardDataItemDTO -> dashboardDataItemDTO.applyColor("#FFAE40"));
				}

				metricValue += currMetricValue;
				maxMetricValue += currMaxMetricValue;
			}

			Double targetMetricValue = metricValue / (maxMetricValue != 0 ? maxMetricValue : 1);
			String statusColor = getQualCondBGColor(targetMetricValue);
			List<DashboardDataItemDTO> questionDetails = new ArrayList<>();
			DashboardDataItemDTO totalCell = new DashboardDataItemDTO(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$TOTAL), "right", null);
			totalCell.setColSpan(4l);
			totalCell.setHeader(true);
			questionDetails.add(totalCell);
			questionDetails.add(sI(metricValue).round(0).applyBackgroundColor(statusColor).applyHeader(true));
			questionDetails.add(sI(maxMetricValue).round(0).applyHeader(true));
			dashboard.getGridItems().add(questionDetails);

			dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUALS_DOMAIN$TABLE$ALTERNATIVE_ITEM_NAME), domain.getCode(), sRound(targetMetricValue)));
		}

		return result;
	}


	/**
	 * Build summary Qual drilldown for System
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	public void buildSystemScoringDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {

		// Obtain Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Long systemId = Long.valueOf(drilldown.getParams().get("system"));
		Systems system = systemsService.getSystemForCurrentOrganization(systemId);

		// Get Scoring Data
		Map<Systems, Map<MetricDomains, MetricResult>> systemScoringDataMap = getSystemsScoringData(riskModelId, Arrays.asList(VendorType.System), Arrays.asList(system));
		Map<MetricDomains, MetricResult> systemScoringData = systemScoringDataMap.get(system);

		// Add System Mitigation Risk
		MetricResult systemMitigateData = getSystemMitigateData(riskModelId, system);
		if (systemMitigateData != null) {
			MetricDomains mitigationRiskDomain = new MetricDomains();
			mitigationRiskDomain.setId(-100L);
			mitigationRiskDomain.setCode(VariableType.MITIGATION_RISK.name());
			mitigationRiskDomain.setName(VariableType.MITIGATION_RISK.name());
			systemScoringData.put(mitigationRiskDomain, systemMitigateData);
		}


		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);
		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$ITEM_NAME), system.getName()));

		// Create System User Assignments Dashboard
		DashboardTableItemDTO dashboardItem00 = createUserSystemAssignmentsDashboardItem(riskModel, system);
		section1.getDashboardItems().add(dashboardItem00);

		List<FormulaBuilder> riskMetricsFormulaBuilders = getRiskScoringMetricsFormulaBuilders(riskModelId, null);
		DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(1010l, clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SCORING_RISK_METRICS$ITEM_NAME));
		section1.getDashboardItems().add(dashboardItem1);
		dashboardItem1.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SCORING_RISK_METRICS$METRIC_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SCORING_RISK_METRICS$FORMULA_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SCORING_RISK_METRICS$TARGET_VALUE_HEADER)
		));
		for (FormulaBuilder formulaBuilder : riskMetricsFormulaBuilders) {
			Double formulaValue = formulaBuilder.calculate(systemScoringData);
			dashboardItem1.getGridItems().add(
				Arrays.asList(
					sI(formulaBuilder.getName()),
					sI(formulaBuilder.getFormulaString()),
					dI(formulaValue).round(2)
				)
			);
		}

		// Build Summary Dashboard
		DashboardTableItemDTO dashboardItem0 = new DashboardTableItemDTO(1010l, clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SCORING_METRICS$ITEM_NAME));
		section1.getDashboardItems().add(dashboardItem0);
		dashboardItem0.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SCORING_METRICS$METRIC_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SCORING_METRICS$FORMULA_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SCORING_METRICS$TARGET_VALUE_HEADER)
		));
		for (Map.Entry<MetricDomains, MetricResult> systemScoringDataEntry : systemScoringData.entrySet()) {
			dashboardItem0.getGridItems().add(
				Arrays.asList(
					sI(systemScoringDataEntry.getKey().getName()),
					sI(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SCORING_METRICS$FORMULA_VALUE)),
					qualCondBGColor(dI(systemScoringDataEntry.getValue().buildNormalizedResult()).round())
				)
			);
		}

		List<MetricDomains> metricDomains = metricDomainRepository.findAll();
		Map<MetricDomains, DashboardItemDTO> dashboardMetricsMap = getMetricDomainsDashboardItemsMapByData(metricDomains, systemScoringData);
		for (MetricDomains metricDomain : metricDomains) {
			DashboardItemDTO dashboardItem3 = dashboardMetricsMap.get(metricDomain);
			section1.getDashboardItems().add(dashboardItem3);
		}

	}

	/**
	 * Create dashboard item for System User Assignments
	 *
	 * @param riskModel
	 * @param system
	 * @return
	 */
	public DashboardTableItemDTO createUserSystemAssignmentsDashboardItem(RiskModels riskModel, Systems system) {
		DashboardTableItemDTO dashboardItem00 = new DashboardTableItemDTO(1015l, "");
		Set<Users> usersSet = userAssignedSystemRepository.getUsersForSystem(system.getId(), riskModel.getOrganizationId()).stream().collect(Collectors.toSet());
		if (system.getOwner() != null) {
			usersSet.add(system.getOwner());
		}
		dashboardItem00.setName(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SYSTEM_ASSIGNMENTS$ITEM_NAME));
		dashboardItem00.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SYSTEM_ASSIGNMENTS$PERSON_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SYSTEM_ASSIGNMENTS$BUSINESS_UNIT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUAL$SYSTEM_ASSIGNMENTS$STATUS_HEADER)
		));
		for (Users currentAssignment : usersSet) {
			String assignmentStatus = currentAssignment.equals(system.getOwner()) ? clientMessage.getMessage(SLCT.DASHBOARD_VALUES$OWNER) : clientMessage.getMessage(SLCT.DASHBOARD_VALUES$ASSIGNMENT);
			dashboardItem00.getGridItems().add(
				Arrays.asList(
					sI(currentAssignment.getFullName()),
					sI(businessUnitService.getBusinessUnitPath(currentAssignment.getBusinessUnit(), true)),
					sI(assignmentStatus)
				)
			);
		}
		return dashboardItem00;
	}

	/**
	 * Get list of Formula Builders for Scoring Risk Metrics
	 * @param riskModelId
	 * @return
	 */
	public List<FormulaBuilder> getRiskScoringMetricsFormulaBuilders(Long riskModelId, List<RiskMetrics> riskMetrics) {
		// Build Risk Metrics Data if Required
		if (riskMetrics == null) riskMetrics = riskMetricsRepository.getListByRiskModelId(riskModelId);

		List<FormulaBuilder> result = new ArrayList<>();
		for (RiskMetrics riskMetric : riskMetrics) {
			FormulaBuilder formulaBuilder = FormulaBuilder.of(riskMetric);
			formulaBuilder.build();
			result.add(formulaBuilder);
		}

		return result;
	}


}
