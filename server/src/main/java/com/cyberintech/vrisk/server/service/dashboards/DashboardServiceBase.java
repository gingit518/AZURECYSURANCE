package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.DashboardDataItemDTO;
import com.cyberintech.vrisk.server.model.dto.dashboards.DashboardDataItemDrilldownDTO;
import com.cyberintech.vrisk.server.model.dto.dashboards.DashboardItemDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.service.QuantMetricsService;
import com.cyberintech.vrisk.server.util.ClientMessage;
import com.cyberintech.vrisk.server.util.ScriptEngineUtil;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base Dashboard Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-02
 */
@Component
@Slf4j
public abstract class DashboardServiceBase {

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private ProcessRepository processRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private QuantMetricsRepository quantMetricsRepository;

	@Autowired
	private QuantMetricsService quantMetricsService;

	@Autowired
	private QuestionAnswersForVendorRepository questionAnswersForVendorRepository;

	@Autowired
	private QuestionAnswersForSystemRepository questionAnswersForSystemRepository;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private RiskModelItemCommentsRepository riskModelItemCommentsRepository;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private SystemRepository systemRepository;


	protected Map<BusinessUnits, List<QuestionAnswersForVendor>> getMapOfVendorAnswersByBusinessUnit(List<QuestionAnswersForVendor> impactVendorQuestionsAnswersList) {
		return impactVendorQuestionsAnswersList.stream().filter(o -> o.getVendor() != null && o.getVendor().getOwner() != null && o.getVendor().getOwner().getBusinessUnit() != null).collect(Collectors.groupingBy(o -> o.getVendor().getOwner().getBusinessUnit()));
	}

	protected Map<BusinessUnits, List<QuestionAnswersForSystem>> getMapOfSystemAnswersByBusinessUnitAndSystem(List<QuestionAnswersForSystem> questionsAnswersList) {
		return questionsAnswersList.stream().filter(o -> o.getSystem() != null && o.getSystem().getBusinessUnit() != null).collect(Collectors.groupingBy(o -> o.getSystem().getBusinessUnit()));
	}

	/**
	 * Build Download Button Dashboard
	 *
	 * @param riskModelId
	 * @param dashboardRefUUID
	 * @param l
	 * @return
	 */
	protected DashboardItemDTO buildDownloadButtonDashboardItemDTO(Long riskModelId, String dashboardRefUUID, long l) {
		DashboardItemDTO dashboardItem = new DashboardItemDTO(l, clientMessage.getMessage(SLCT.DASHBOARD_COMPONENTS$BUTTON$DOWNLOAD$NAME), "", DashboardItemType.Link);
		String authorization = request.getHeader("Authorization");
		String apiUrl = applicationProperties.getApiUrl();
		String tokenId = authorization;
		if (authorization != null && authorization.toLowerCase().contains("bearer")) {
			tokenId = authorization.substring("bearer".length() + 1);
		}
		String downloadUrl = apiUrl + "/api/dashboards/download/report?token=" + tokenId.trim() + "&riskModelId=" + riskModelId.toString() +
			"&dashboardRefUUID=" + dashboardRefUUID;
		dashboardItem.addParameter("href", downloadUrl);
		dashboardItem.addParameter("isExternal", "true");
		dashboardItem.addParameter("textAlign", "right");
		return dashboardItem;
	}

	/**
	 * Get Quant metrics System data
	 *
	 * @return Dashboard systems data
	 */
	@Deprecated
	public List<SystemDataSeries> buildQuantMetricDataBySystems(Long riskModelId, Set<Organizations> vendorSet, Set<Systems> systemsSet) {

		Map<Long, SystemDataSeries> targetQuantsMap = new HashMap<>();

		List<SystemDataSeries> dataExfiltration = getOrganizationQuantMetricData(riskModelId, QuantsDomain.DATA_EXFILTRATION, vendorSet, systemsSet);
		Map<Long, SystemDataSeries> dataExfiltrationMap = new HashMap<>();
		verifySystemDataSeries(dataExfiltration, dataExfiltrationMap, targetQuantsMap);
		synchronizeSystemDataSeries(dataExfiltrationMap, targetQuantsMap);

		List<SystemDataSeries> businessInterruption = getOrganizationQuantMetricData(riskModelId, QuantsDomain.BUSINESS_INTERRUPTION, vendorSet, systemsSet);
		Map<Long, SystemDataSeries> businessInterruptionMap = new HashMap<>();
		verifySystemDataSeries(businessInterruption, businessInterruptionMap, targetQuantsMap);
		synchronizeSystemDataSeries(businessInterruptionMap, targetQuantsMap);

		/**
		 * Check Regulatory Loss Map
		 */
		if (quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE)) {
			List<SystemDataSeries> regulatoryLoss = getOrganizationQuantMetricData(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE, vendorSet, systemsSet);
			Map<Long, SystemDataSeries> regulatoryLossMap = new HashMap<>();
			verifySystemDataSeries(regulatoryLoss, regulatoryLossMap, targetQuantsMap);
			synchronizeSystemDataSeries(regulatoryLossMap, targetQuantsMap);
		}

		List<SystemDataSeries> quantScores = calculateTargetSystemDataSeries(targetQuantsMap);

		return quantScores;
	}

	public void verifySystemDataSeries(List<SystemDataSeries> series, Map<Long, SystemDataSeries> mapSeries, Map<Long, SystemDataSeries> targetSeries) {
		series.stream().forEach(systemDataSeries -> {
			mapSeries.put(systemDataSeries.getSystem().getId(), systemDataSeries);
			if (!targetSeries.containsKey(systemDataSeries.getSystem().getId())) {
				SystemDataSeries tmpSystemDataSeries = new SystemDataSeries();
				tmpSystemDataSeries.setSystem(systemDataSeries.getSystem());
				tmpSystemDataSeries.setItems(new ArrayList<>());

				targetSeries.put(systemDataSeries.getSystem().getId(), tmpSystemDataSeries);
			}
		});
	}

	public void synchronizeSystemDataSeries(Map<Long, SystemDataSeries> mapSeries, Map<Long, SystemDataSeries> targetSeries) {
		for (Map.Entry<Long, SystemDataSeries> syncSeries : targetSeries.entrySet()) {
			SystemDataSeries dataSeries = syncSeries.getValue();
			Long itemId = syncSeries.getKey();
			if (mapSeries.containsKey(itemId)) {
				dataSeries.getItems().add(mapSeries.get(itemId).getItems().get(0));
			} else {
				dataSeries.getItems().add(0D);
			}
		}
	}

	public List<SystemDataSeries> calculateTargetSystemDataSeries(Map<Long, SystemDataSeries> targetSeries) {
		List<SystemDataSeries> result = new ArrayList<>();
		for (Map.Entry<Long, SystemDataSeries> syncSeries : targetSeries.entrySet()) {
			SystemDataSeries dataSeries = syncSeries.getValue();
			Double totalParameter = 0D;
			for (Double item : dataSeries.getItems()) {
				totalParameter += item;
			}
			dataSeries.getItems().add(totalParameter);

			result.add(dataSeries);
		}
		return result;
	}

	/**
	 * Get Qual metrics Systems data
	 *
	 * @return Dashboard vendor data
	 */
	public List<SystemDataSeries> getQualMetricDataForSystems(Long riskModelId, MetricDomain metricDomain, Set<Systems> customSystemsSet) {

		List<SystemDataSeries> result = new ArrayList<>();

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<QuestionAnswersForSystem> questionAnswersForSystems;
		if (customSystemsSet == null) {
			questionAnswersForSystems = questionAnswersForSystemRepository.getListByRiskModelAndMetricDomainId(riskModelId, metricDomain.getId());
		} else {
			// List<Long> vendorIdList = vendorList.stream().mapToLong(Organizations::getId).boxed().collect(Collectors.toList());
			List<Long> systemIdList = customSystemsSet.stream().mapToLong(Systems::getId).boxed().collect(Collectors.toList());
			if (systemIdList.size() == 0) systemIdList.add(0l); // Quick Fix for Exception with empty in statement
			questionAnswersForSystems = questionAnswersForSystemRepository.getListByRiskModelAndMetricDomainIdAndSystems(riskModelId, metricDomain.getId(), systemIdList);
		}

		Map<Long, List<QuestionAnswersForSystem>> systemQuestionMap = new HashMap<>();
		questionAnswersForSystems.stream().forEach(questionAnswersForSystem -> {
			if (!systemQuestionMap.containsKey(questionAnswersForSystem.getSystem().getId())) {
				systemQuestionMap.put(questionAnswersForSystem.getSystem().getId(), new ArrayList<>());
			}
			systemQuestionMap.get(questionAnswersForSystem.getSystem().getId()).add(questionAnswersForSystem);
		});

		for (Map.Entry<Long, List<QuestionAnswersForSystem>> entry : systemQuestionMap.entrySet()) {
			// for (Map.Entry<Systems, List<Organizations>> entry : systemVendorsMap.entrySet()) {

			List<QuestionAnswersForSystem> questionAnswersForSystem = entry.getValue();
			Long systemId = entry.getKey();

			Double metricValue = Double.valueOf(0);
			Double maxMetricValue = Double.valueOf(0);
			Systems currentSystem = null;
			if (questionAnswersForSystem != null) {
				for (QuestionAnswersForSystem questionAnswerForSystem : questionAnswersForSystem) {
					if (currentSystem == null) currentSystem = questionAnswerForSystem.getSystem();

					double maxQuestionWeight = 1;
					for (QualitativeQuestionAnswers qualitativeQuestionAnswers : questionAnswerForSystem.getQuestion().getAnswers()) {
						if (qualitativeQuestionAnswers.getAnswerWeight() != null && maxQuestionWeight < qualitativeQuestionAnswers.getAnswerWeight().getValue()) {
							maxQuestionWeight = qualitativeQuestionAnswers.getAnswerWeight().getValue();
						}
					}

					double answerWeight = questionAnswerForSystem.getAnswer() != null && questionAnswerForSystem.getAnswer().getAnswerWeight() != null ? questionAnswerForSystem.getAnswer().getAnswerWeight().getValue() : 0;
					double questionWeight = questionAnswerForSystem.getQuestion() != null && questionAnswerForSystem.getQuestion().getQuestionWeight() != null ? questionAnswerForSystem.getQuestion().getQuestionWeight().getValue() : 0;

					metricValue += answerWeight * questionWeight;
					maxMetricValue += maxQuestionWeight * questionWeight;
				}
			}

			SystemDataSeries dataSeries = new SystemDataSeries();
			dataSeries.setSystem(currentSystem);
			dataSeries.getItems().add(metricValue / (maxMetricValue != 0 ? maxMetricValue : 1));
			dataSeries.getItems().add(metricValue);
			dataSeries.getItems().add(maxMetricValue);

			result.add(dataSeries);
		}

		return result;
	}

	/**
	 * Get Qual metrics Systems data
	 *
	 * @return Dashboard vendor data
	 */
	@Deprecated
	public List<SystemDataSeries> getQualMetricDataForSystems(Long riskModelId, MetricDomain metricDomain) {
		return getQualMetricDataForSystems(riskModelId, metricDomain, null);
	}

	/**
	 * Get Quant metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	@Deprecated
	public List<SystemDataSeries> getOrganizationQuantMetricData(Long riskModelId, QuantsDomain metricDomain, Set<Organizations> vendorSet, Set<Systems> systemsSet) {
		List<SystemDataSeries> result = new ArrayList<>();

		List<QuantMetrics> quantsList = quantMetricsRepository.getListByRiskModelIdAndQuantId(riskModelId, metricDomain.getId());
		if (quantsList.size() > 0) {

			QuantMetrics quantMetric = quantsList.get(0);
			if (metricDomain.equals(QuantsDomain.DATA_EXFILTRATION)) {
				result = getQuant_DataExfiltration_BySystem(quantMetric, vendorSet, systemsSet);
			} else if (metricDomain.equals(QuantsDomain.BUSINESS_INTERRUPTION)) {
				result = getQuant_BusinessInterruption_BySystem(quantsList, riskModelId, vendorSet, systemsSet);
			} else if (metricDomain.equals(QuantsDomain.REGULATORY_LOSS)) {
				result = getQuant_RegulatoryLoss_BySystem(quantMetric, vendorSet, systemsSet);
			} else if (metricDomain.equals(QuantsDomain.GDPR_REGULATORY_EXPOSURE)) {
				result = getQuant_RegulatoryLoss_BySystem(quantMetric, vendorSet, systemsSet);
			} else {
				result = getQuantMetric_Common_BySystem(quantMetric, vendorSet, systemsSet);
			}
		}

		return result;
	}

	@Deprecated
	protected List<Systems> getSystemListForQuantificationMetric(Set<Systems> systemsSet, RiskModels riskModel, AssetClass assetClass) {
		// Obtain systems List
		List<Systems> systemsList;
		if (systemsSet != null) {
			systemsList = systemsSet.stream().collect(Collectors.toList());
		} else if (assetClass != null) {
			systemsList = systemRepository.getAllByOrganizationAndAssetClass(riskModel.getOrganizationId(), assetClass.getId());
		} else {
			systemsList = systemRepository.getAllByOrganization(riskModel.getOrganizationId());
		}
		return systemsList;
	}

	/**
	 * Calculate Data Exfiltration for Systems inside the Organization
	 *
	 * @param quantMetric
	 * @return
	 */
	@Deprecated
	public List<SystemDataSeries> getQuant_DataExfiltration_BySystem(QuantMetrics quantMetric, Set<Organizations> vendorSet, Set<Systems> systemsSet) {
		List<SystemDataSeries> result = new ArrayList<>();

		RiskModels riskModel = riskModelRepository.findById(quantMetric.getRiskModelId()).get();
		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, null);
		// List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, AssetClass.CROWN_JEWEL);
		for (Systems system : systemsList) {
			Double formulaResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), 0D, 0D).getResult();

			SystemDataSeries systemDataSeries = new SystemDataSeries();
			systemDataSeries.setSystem(system);
			systemDataSeries.setItems(Arrays.asList(formulaResult));
			result.add(systemDataSeries);
		}

		return result;
	}

	/**
	 * Calculate Business Interruption for Systems inside the Organization
	 *
	 * @param quantMetric
	 * @return
	 */
	@Deprecated
	public Double getQuantValue4Organization(QuantMetrics quantMetric, Set<Organizations> vendorSet, Set<Systems> systemsSet) {
		Double result = 0D;

		RiskModels riskModel = riskModelRepository.findById(quantMetric.getRiskModelId()).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		QuantMetricsService.FormulaAnalysisResult formulaAnalysis = quantMetricsService.analyzeFormula(quantMetric);

		// Only organization / constant data involved to the formula
		if ((formulaAnalysis.isConstantInvolved || formulaAnalysis.isOrganizationInvolved) && !formulaAnalysis.isSystemsInvolved && !formulaAnalysis.isProcessesInvolved) {
			result = calculateFormula(formulaItems, 0D, 0D, organization.getAverageRevenue()).getResult();
		} else if (formulaAnalysis.isProcessesInvolved) {
			List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, null);
			// List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, AssetClass.CROWN_JEWEL);
			for (Systems system : systemsList) {
				for (Processes process : processRepository.getListBySystem(system.getId())) {
					Double formulaResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), process.getRevenueProcessed(), organization.getAverageRevenue()).getResult();
					result += formulaResult;
				}
			}
		} else if (formulaAnalysis.isSystemsInvolved) {
			List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, null);
			// List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, AssetClass.CROWN_JEWEL);
			for (Systems system : systemsList) {
				Double formulaResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), 0D, organization.getAverageRevenue()).getResult();
				result += formulaResult;
			}
		}

		return result;
	}

	/**
	 * Calculate Business Interruption for Systems inside the Organization
	 *
	 * @param quantMetric
	 * @return
	 */
	@Deprecated
	public List<SystemDataSeries> getQuantMetric_Common_BySystem(QuantMetrics quantMetric, Set<Organizations> vendorSet, Set<Systems> systemsSet) {
		List<SystemDataSeries> result = new ArrayList<>();

		RiskModels riskModel = riskModelRepository.findById(quantMetric.getRiskModelId()).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		boolean isProcessRevenueInvolved = formulaItems.stream().filter(item -> item.getVariableType() != null && item.getVariableType().getId().equals(2l)).count() > 0;

		List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, null);
		// List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, AssetClass.CROWN_JEWEL);
		for (Systems system : systemsList) {

			Double systemResult = 0D;
			if (isProcessRevenueInvolved) {
				for (Processes process : processRepository.getListBySystem(system.getId())) {
					Double formulaResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), process.getRevenueProcessed(), organization.getAverageRevenue()).getResult();
					systemResult += formulaResult;
				}
			} else {
				systemResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), null, organization.getAverageRevenue()).getResult();
			}

			SystemDataSeries systemDataSeries = new SystemDataSeries();
			systemDataSeries.setSystem(system);
			systemDataSeries.setItems(Arrays.asList(systemResult));
			result.add(systemDataSeries);
		}

		return result;
	}

	/**
	 * Calculate Business Interruption for Systems inside the Organization
	 *
	 * @param quantMetrics
	 * @return
	 */
	@Deprecated
	public List<SystemDataSeries> getQuant_BusinessInterruption_BySystem(List<QuantMetrics> quantMetrics, Long riskModelId, Set<Organizations> vendorSet, Set<Systems> systemsSet) {
		List<SystemDataSeries> result = new ArrayList<>();

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<FormulaInfo> formulas = new ArrayList<>();
		for (QuantMetrics quantMetric : quantMetrics) {
			List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
			formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

			formulas.add(new FormulaInfo(formulaItems));
		}

		List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, null);
		// List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, AssetClass.CROWN_JEWEL);
		for (Systems system : systemsList) {

			Double systemResult = 0D;

			for (FormulaInfo formulaInfo : formulas) {
				if (formulaInfo.isProcessRevenueInvolved()) {
					for (Processes process : processRepository.getListBySystem(system.getId())) {
						Double formulaResult = calculateFormula(formulaInfo.getFormulaItems(), system.getNumberOfRecProcessed(), process.getRevenueProcessed(), organization.getAverageRevenue()).getResult();
						systemResult += formulaResult;
					}
				} else if (formulaInfo.isSystemInvolved()) {
					Double formulaResult = calculateFormula(formulaInfo.getFormulaItems(), system.getNumberOfRecProcessed(), null, organization.getAverageRevenue()).getResult();
					systemResult += formulaResult;
				} else {
					Double formulaResult = calculateFormula(formulaInfo.getFormulaItems(), null, null, organization.getAverageRevenue()).getResult();
					systemResult += formulaResult;
				}
			}
			SystemDataSeries systemDataSeries = new SystemDataSeries();
			systemDataSeries.setSystem(system);
			systemDataSeries.setItems(Arrays.asList(systemResult));
			result.add(systemDataSeries);
		}

		return result;
	}

	@Getter
	public class FormulaInfo {

		public FormulaInfo (List<MetricFormulaItems> formulaItems) {
			this.formulaItems = formulaItems;

			processRevenueInvolved = formulaItems.stream().filter(item -> item.getVariableType() != null && item.getVariableType().getId().equals(2l)).count() > 0;
			systemInvolved = formulaItems.stream().filter(item -> item.getVariableType() != null && item.getVariableType().getId().equals(3l)).count() > 0;
			orgInvolved = formulaItems.stream().filter(item -> item.getVariableType() != null && item.getVariableType().getId().equals(4l)).count() > 0;
		}

		List<MetricFormulaItems> formulaItems;

		boolean processRevenueInvolved;
		boolean systemInvolved;
		boolean orgInvolved;
	}

	/**
	 * Calculate Regulatory Loss for Systems inside the Organization
	 *
	 * @param quantMetric
	 * @return
	 */
	@Deprecated
	public List<SystemDataSeries> getQuant_RegulatoryLoss_BySystem(QuantMetrics quantMetric, Set<Organizations> vendorSet, Set<Systems> systemsSet) {
		List<SystemDataSeries> result = new ArrayList<>();

		RiskModels riskModel = riskModelRepository.findById(quantMetric.getRiskModelId()).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, null);
		// List<Systems> systemsList = getSystemListForQuantificationMetric(systemsSet, riskModel, AssetClass.CROWN_JEWEL);
		for (Systems system : systemsList) {

			Double systemResult = 0D;
			boolean isPrivacyCalculated = false;
			for (DataTypeClassification dataTypeClassification : system.getDataTypeClassifications()) {

				if (
					QuantsDomain.REGULATORY_LOSS.getId().equals(quantMetric.getQuant().getId()) &&
					(DataTypeDomain.PII.getId().equals(dataTypeClassification.getId()) || DataTypeDomain.PRIVACY.getId().equals(dataTypeClassification.getId())) && !isPrivacyCalculated
				) {
					systemResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), 0D, organization.getAverageRevenue()).getResult();
					isPrivacyCalculated = true;
				}

				if (
					QuantsDomain.GDPR_REGULATORY_EXPOSURE.getId().equals(quantMetric.getQuant().getId()) &&
					(DataTypeDomain.CREDIT_CARD.getId().equals(dataTypeClassification.getId())
						|| (dataTypeClassification.getName().toLowerCase().indexOf(DataTypeDomain.CREDIT_CARD.getName().toLowerCase()) >= 0))
					&& !isPrivacyCalculated) {

					systemResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), 0D, organization.getAverageRevenue()).getResult();
					isPrivacyCalculated = true;
				}
			}

			SystemDataSeries systemDataSeries = new SystemDataSeries();
			systemDataSeries.setSystem(system);
			systemDataSeries.setItems(Arrays.asList(systemResult));
			result.add(systemDataSeries);
		}

		return result;
	}


	/**
	 * Calculate formula for items
	 *
	 * @param formulaItems
	 * @return
	 */
	public FormulaResult calculateFormula(List<MetricFormulaItems> formulaItems, Double systemNumerOfRecords, Double processRevenue, Double orgRevenue) {
		FormulaResult formulaResult = new FormulaResult();
		FormulaBuilder formulaBuilder = new FormulaBuilder();
		// Evaluate script
		try {

			GraalJSScriptEngine engine = ScriptEngineUtil.getJavaScriptEngine();
			formulaBuilder.getFormulaScript(engine);

			// JavaScript code in a String
			String script = "";
			String formula = "";

			int i = 1;
			for (MetricFormulaItems formulaItem : formulaItems) {
				if (formulaItem.getIsOperation()) {
					if (formulaItem.getOperation().equals(VariableOperation.PLUS)) {
						script += " + ";
						formula += " + ";
					} else if (formulaItem.getOperation().equals(VariableOperation.MULTIPLY)) {
						script += " * ";
						formula += " * ";
					} else if (formulaItem.getOperation().equals(VariableOperation.MINUS)) {
						script += " - ";
						formula += " - ";
					} else if (formulaItem.getOperation().equals(VariableOperation.DIVIDE)) {
						script += " / ";
						formula += " / ";
					} else if (formulaItem.getOperation().equals(VariableOperation.OPEN_BRACKET)) {
						script += "( ";
						formula += "( ";
					} else if (formulaItem.getOperation().equals(VariableOperation.CLOSE_BRACKET)) {
						script += " )";
						formula += " )";
					} else if (formulaItem.getOperation().equals(VariableOperation.MAX)) {
						script += " Math.max";
						formula += " MAX";
					} else if (formulaItem.getOperation().equals(VariableOperation.MIN)) {
						script += " Math.min";
						formula += " MIN";
					} else if (formulaItem.getOperation().equals(VariableOperation.COMMA)) {
						script += ",";
						formula += "; ";
					} else if (formulaItem.getOperation().equals(VariableOperation.ABS)) {
						script += " Math.abs";
						formula += " ABS";
					} else if (formulaItem.getOperation().equals(VariableOperation.MEDIAN)) {
						script += " formulaMedian";
						formula += " MEDIAN";
					} else if (formulaItem.getOperation().equals(VariableOperation.AVERAGE)) {
						script += " formulaAverage";
						formula += " AVERAGE";
					} else if (formulaItem.getOperation().equals(VariableOperation.MODE)) {
						script += " formulaModeEmpiric";
						formula += " MODE";
					} else if (formulaItem.getOperation().equals(VariableOperation.SQRT)) {
						script += " Math.sqrt";
						formula += " SQRT";
					} else if (formulaItem.getOperation().equals(VariableOperation.SUM)) {
						script += " formulaSum";
						formula += " SUM";
					} else if (formulaItem.getOperation().equals(VariableOperation.RAND)) {
						script += " Math.random";
						formula += " RAND";
					} else if (formulaItem.getOperation().equals(VariableOperation.POWER)) {
						script += " Math.pow";
						formula += " POW";
					} else if (formulaItem.getOperation().equals(VariableOperation.EXPONENT)) {
						script += " Math.exp";
						formula += " EXP";
					} else if (formulaItem.getOperation().equals(VariableOperation.LOG)) {
						script += " Math.log";
						formula += " LN";
					} else if (formulaItem.getOperation().equals(VariableOperation.HYPERBOLA)) {
						script += " 1 / ";
						formula += " 1 / ";
					}
				} else {

					/*
						(1, 'CONSTANT')
						(2, 'PROCESS_REVENUE')
						(3, 'SYSTEM_NUMBER_OF_REC')
						(4, 'ORGANIZATION_REVENUE')
					 */
					Double currentValue = 0D;
					if (formulaItem.getVariableType().getId().equals(1l)) { // CONSTANT
						currentValue = formulaItem.getValue();
					} else if (formulaItem.getVariableType().getId().equals(2l)) { // PROCESS_REVENUE
						currentValue = processRevenue;
					} else if (formulaItem.getVariableType().getId().equals(3l)) { // SYSTEM_NUMBER_OF_REC
						currentValue = systemNumerOfRecords;
					} else if (formulaItem.getVariableType().getId().equals(4l)) { // ORGANIZATION_REVENUE
						currentValue = orgRevenue;
					}
					formula += currentValue != null ? String.format("%,.2f", currentValue) : "";

					String variableName = "variable" + i;
					engine.put(variableName, currentValue);
					i++;

					script += variableName;
				}
			}

			formulaResult.setFormula(formula);
			script = "var resultItem = " + script + ";";
			engine.eval(script);

			// get script object on which we want to implement the interface with
			Object resultItem = engine.get("resultItem");
			Double doubleResult = 0D;
			if (resultItem instanceof String) {
				doubleResult = Double.valueOf((String) resultItem);
			} else if (resultItem instanceof Double) {
				doubleResult = Double.valueOf((Double) resultItem);
			} else if (resultItem instanceof Long) {
				doubleResult = Double.valueOf((Long) resultItem);
			} else if (resultItem instanceof Integer) {
				doubleResult = Double.valueOf((Integer) resultItem);
			}

			formulaResult.setResult(doubleResult);

		} catch (ScriptException e) {
			log.warn(e.getMessage(), e);
		}

		return formulaResult;
	}


	/**
	 * Create Row Items for Data Series
	 *
	 * @param dataSeries
	 * @return
	 */
	protected List<DashboardDataItemDTO> createRowItems(VendorDataSeries dataSeries) {
		// Init Table items
		List<DashboardDataItemDTO> rowItems = new ArrayList<>();
		rowItems.add(sI(dataSeries.getVendor().getName()));
		rowItems.addAll(dataSeries.getItems().stream().map(item -> sI(item)).collect(Collectors.toList()));
		return rowItems;
	}

	/**
	 * Create Row Items for Data Series
	 *
	 * @param dataSeries
	 * @return
	 */
	protected List<DashboardDataItemDTO> createChartItems(VendorDataSeries dataSeries) {
		List<DashboardDataItemDTO> chartItems = new ArrayList<>();
		chartItems.add(sI(dataSeries.getVendor().getName()));
		chartItems.add(sI(dataSeries.getItems().get(0)));
		return chartItems;
	}

	/**
	 * Create Row Items for Data Series
	 *
	 * @param dataSeries
	 * @return
	 */
	@Deprecated
	protected List<DashboardDataItemDTO> createRowItems(SystemDataSeries dataSeries) {
		// Init Table items
		List<DashboardDataItemDTO> rowItems = new ArrayList<>();
		rowItems.add(sI(dataSeries.getSystem().getName()));
		rowItems.addAll(dataSeries.getItems().stream().map(item -> sI(item)).collect(Collectors.toList()));
		return rowItems;
	}

	/**
	 * Create Row Items for Data Series
	 *
	 * @param dataSeries
	 * @return
	 */
	protected List<DashboardDataItemDTO> createChartItems(SystemDataSeries dataSeries) {
		List<DashboardDataItemDTO> chartItems = new ArrayList<>();
		chartItems.add(sI(dataSeries.getSystem().getName()));
		chartItems.add(sI(dataSeries.getItems().get(0)));
		return chartItems;
	}

	/**
	 * Apply system Dashboard Drilldowns for Qual Metrics
	 *
	 * @param rowItems
	 * @param system
	 */
	@Deprecated
	protected void applyVendorDashboardQualsDrilldown(List<DashboardDataItemDTO> rowItems, Systems system) {
		rowItems.get(0).setDrilldown(DashboardDataItemDrilldownDTO.of(system));
		if (rowItems.size() > 1) rowItems.get(1).setDrilldown(DashboardDataItemDrilldownDTO.of(system));
		if (rowItems.size() > 2) rowItems.get(2).setDrilldown(DashboardDataItemDrilldownDTO.of(system, MetricDomain.IMPACT));
		if (rowItems.size() > 4) rowItems.get(4).setDrilldown(DashboardDataItemDrilldownDTO.of(system, MetricDomain.LIKELIHOOD));
	}

	/**
	 * Apply system Dashboard Drilldowns for Quants Metrics
	 *
	 * @param rowItems
	 * @param system
	 */
	@Deprecated
	protected void applyVendorDashboardQuantsDrilldown(List<DashboardDataItemDTO> rowItems, Systems system) {
		rowItems.get(0).setDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null));
		if (rowItems.size() > 1) rowItems.get(1).setDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.DATA_EXFILTRATION));
		if (rowItems.size() > 2) rowItems.get(2).setDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.BUSINESS_INTERRUPTION));
		if (rowItems.size() > 3) rowItems.get(3).setDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.GDPR_REGULATORY_EXPOSURE));
	}

	/**
	 * Apply vendor Dashboard Drilldowns for Qual Metrics
	 *
	 * @param rowItems
	 * @param vendor
	 */
	@Deprecated
	protected void applyVendorDashboardQualsDrilldown(List<DashboardDataItemDTO> rowItems, Organizations vendor) {
		applyVendorDashboardQualsDrilldown(rowItems, vendor, null);
	}

	/**
	 * Apply vendor Dashboard Drilldowns for Qual Metrics
	 *
	 * @param rowItems
	 * @param vendor
	 */
	@Deprecated
	protected void applyVendorDashboardQualsDrilldown(List<DashboardDataItemDTO> rowItems, Organizations vendor, VendorType vendorType) {
		rowItems.get(0).setDrilldown(DashboardDataItemDrilldownDTO.of(vendor).applyDrillDownType(vendorType));
		if (rowItems.size() > 1) rowItems.get(1).setDrilldown(DashboardDataItemDrilldownDTO.of(vendor).applyDrillDownType(vendorType));
		if (rowItems.size() > 2) rowItems.get(2).setDrilldown(DashboardDataItemDrilldownDTO.of(vendor, MetricDomain.IMPACT).applyDrillDownType(vendorType));
		if (rowItems.size() > 4) rowItems.get(4).setDrilldown(DashboardDataItemDrilldownDTO.of(vendor, MetricDomain.LIKELIHOOD).applyDrillDownType(vendorType));
	}

	/**
	 * Apply vendor Dashboard Drilldows for Qual Metric
	 *
	 * @param rowItems
	 * @param vendor
	 * @param metricDomain
	 */
	@Deprecated
	protected void applyVendorDashboardQualMetricDrilldown(List<DashboardDataItemDTO> rowItems, Organizations vendor, MetricDomain metricDomain) {
		applyVendorDashboardQualMetricDrilldown(rowItems, vendor, metricDomain, null);
	}

	/**
	 * Apply vendor Dashboard Drilldowns for Qual Metric
	 *
	 * @param rowItems
	 * @param vendor
	 * @param metricDomain
	 * @param vendorType
	 */
	@Deprecated
	protected void applyVendorDashboardQualMetricDrilldown(List<DashboardDataItemDTO> rowItems, Organizations vendor, MetricDomain metricDomain, VendorType vendorType) {
		rowItems.get(0).setDrilldown(DashboardDataItemDrilldownDTO.of(vendor, metricDomain).applyDrillDownType(vendorType));
		if (rowItems.size() > 1) rowItems.get(1).setDrilldown(DashboardDataItemDrilldownDTO.of(vendor, metricDomain).applyDrillDownType(vendorType));
	}

	/**
	 * Apply Vendor Dashboard Drilldowns for Quants Metrics
	 *
	 * @param rowItems
	 * @param vendor
	 */
	@Deprecated
	protected void applyVendorDashboardQuantsDrilldown(List<DashboardDataItemDTO> rowItems, Organizations vendor) {
		rowItems.get(0).setDrilldown(DashboardDataItemDrilldownDTO.ofQuant(vendor, null));
		if (rowItems.size() > 1) rowItems.get(1).setDrilldown(DashboardDataItemDrilldownDTO.ofQuant(vendor, QuantsDomain.DATA_EXFILTRATION));
		if (rowItems.size() > 2) rowItems.get(2).setDrilldown(DashboardDataItemDrilldownDTO.ofQuant(vendor, QuantsDomain.BUSINESS_INTERRUPTION));
		if (rowItems.size() > 3) rowItems.get(3).setDrilldown(DashboardDataItemDrilldownDTO.ofQuant(vendor, QuantsDomain.GDPR_REGULATORY_EXPOSURE));
	}

	@Deprecated
	protected Map<Long, VendorDataSeries> getLongVendorDataSeriesMap(List<VendorDataSeries> impactData, List<VendorDataSeries> likelihoodData) {
		Map<Long, VendorDataSeries> summaryQualData = new HashMap<>();
		impactData.stream().forEach(vendorDataSeries -> {
			if (!summaryQualData.containsKey(vendorDataSeries.getVendor().getId())) {
				VendorDataSeries target = vendorDataSeries.clone();
				target.getItems().add(Double.valueOf(0));
				target.getItems().add(Double.valueOf(0));
				summaryQualData.put(vendorDataSeries.getVendor().getId(), target);
			}
		});
		likelihoodData.stream().forEach(vendorDataSeries -> {
			if (!summaryQualData.containsKey(vendorDataSeries.getVendor().getId())) {
				VendorDataSeries series = new VendorDataSeries();
				series.setVendor(vendorDataSeries.getVendor());
				series.setItems(Arrays.asList(Double.valueOf(0), Double.valueOf(0), Double.valueOf(0), Double.valueOf(0), Double.valueOf(0)));
				summaryQualData.put(vendorDataSeries.getVendor().getId(), series);
			}

			VendorDataSeries target = summaryQualData.get(vendorDataSeries.getVendor().getId());
			Double totalValue = (target.getItems().get(0) + vendorDataSeries.getItems().get(0))/2;
			target.getItems().set(0, totalValue);
			target.getItems().set(3, vendorDataSeries.getItems().get(1));
			target.getItems().set(4, vendorDataSeries.getItems().get(2));
		});
		return summaryQualData;
	}

	/**
	 * Round double to shorten value and return as String
	 *
	 * @param value
	 * @return
	 */
	public static String sRound(Double value) {
		return String.format("%,.2f", value);
	}

	/**
	 * Create number item
	 *
	 * @param value
	 * @return
	 */
	public static DashboardDataItemDTO sI(String value) {
		DashboardDataItemDTO result = new DashboardDataItemDTO(value);
		return result;
	}

	/**
	 * Create number item
	 *
	 * @param value
	 * @return
	 */
	public static DashboardDataItemDTO sI(Double value) {
		return dI(value);
	}

	/**
	 * Create number item
	 *
	 * @param value
	 * @return
	 */
	public static DashboardDataItemDTO dI(Double value, String symbol) {
		String actualSymbol = value != null ? symbol : null;
		DashboardDataItemDTO result = new DashboardDataItemDTO(value, "right", actualSymbol);
		if (value != null && value == Math.rint(value)) {
			result.round(0);
		} else {
			result.round(2);
		}

		return result;
	}

	/**
	 * Create number item
	 *
	 * @param value
	 * @return
	 */
	public static DashboardDataItemDTO dI(Double value) {
		return dI(value, null);
	}

	/**
	 * Create Currency item
	 *
	 * @param value
	 * @param symbol
	 * @return
	 */
	public static DashboardDataItemDTO $I(Double value, String symbol) {
		DashboardDataItemDTO result = new DashboardDataItemDTO(value, "right", symbol);
		return result;
	}

	/**
	 * Create Currency item
	 *
	 * @param value
	 * @return
	 */
	public static DashboardDataItemDTO $I(Double value) {
		return $I(value, "$");
	}

	/**
	 * Set background colour based on background
	 *
	 * @param value
	 * @return
	 */
	public DashboardDataItemDTO qualCondBGColor(DashboardDataItemDTO value) {
		Double valueNumber = value.getValueDouble();

		if (valueNumber != null) {
			value.setBackgroundColor(getQualCondBGColor(valueNumber));
		}

		return value;
	}

	/**
	 * Get background colour based on background
	 *
	 * @param value
	 * @return
	 */
	public String getQualCondBGColor(Double value) {
		return getQualCondBGColor(value, 1d);
	}

	/**
	 * Get background colour based on background
	 *
	 * @param value
	 * @return
	 */
	public String getQualCondBGColor(Double value, Double normalize) {
		String result = null;

		Double normalizedValue = value / normalize;

		if (normalizedValue >= 0.9d) {
			result = "#FF0000";
		} else if (normalizedValue >= 0.8d && normalizedValue < 0.9d) {
			result = "#FB5100";
		} else if (normalizedValue >= 0.7d && normalizedValue < 0.8d) {
			result = "#FF7400";
		} else if (normalizedValue >= 0.65d && normalizedValue < 0.7d) {
			result = "#FF9640";
		} else if (normalizedValue >= 0.6d && normalizedValue < 0.65d) {
			result = "#FFAE40";
		} else if (normalizedValue >= 0.55d && normalizedValue < 0.6d) {
			result = "#FFC000";
		} else if (normalizedValue >= 0.5d && normalizedValue < 0.55d) {
			result = "#FFD040";
		} else if (normalizedValue >= 0.45d && normalizedValue < 0.5d) {
			result = "#FFDD73";
		}

		return result;
	}

	public static Map<Long, Map<Long, String>> __DASHBOARD_DATA_STORAGE = new HashMap<>();
	@Deprecated
	public List<DashboardItemDTO> saveDashboardData(Long riskModelId, List<DashboardItemDTO> items) {
		if (!__DASHBOARD_DATA_STORAGE.containsKey(riskModelId)) {
			__DASHBOARD_DATA_STORAGE.put(riskModelId, new HashMap<>());
		}

		for (DashboardItemDTO item : items) {
			String method = item.getParameters() != null ? (String) item.getParameters().get("method") : null;

			if ("VENDOR_SCORING_QUESTION_COMMENT".equalsIgnoreCase(method)) {
				Long vendorId = Long.parseLong((String) item.getParameters().get("vendorId"));
				// Long riskModelId = Long.parseLong((String) item.getParameters().get("riskModelId"));
				Optional<RiskModelItemComments> vendorQuestionCommentOpt = riskModelItemCommentsRepository.findFirstByRiskModelIdAndItemTypeNameAndExternalId(riskModelId, "VENDOR_SCORING_QUESTION_COMMENT", vendorId);
				RiskModelItemComments riskModelItemComments = null;
				if (vendorQuestionCommentOpt.isPresent()) {
					riskModelItemComments = vendorQuestionCommentOpt.get();
				} else {
					riskModelItemComments = new RiskModelItemComments();
					riskModelItemComments.setRiskModelId(riskModelId);
					riskModelItemComments.setItemTypeName("VENDOR_SCORING_QUESTION_COMMENT");
					riskModelItemComments.setExternalId(vendorId);
					riskModelItemComments.setExternalUid(vendorId.toString());
				}

				riskModelItemComments.setComment(item.getDescription());

				riskModelItemCommentsRepository.save(riskModelItemComments);
			} else {
				__DASHBOARD_DATA_STORAGE.get(riskModelId).put(item.getId(), item.getDescription());
			}
		}

		return items;
	}

	@Deprecated
	public String getDashboardDataItemString(Long riskModelId, Long itemId) {
		String result = "";

		if (__DASHBOARD_DATA_STORAGE.containsKey(riskModelId) && __DASHBOARD_DATA_STORAGE.get(riskModelId).containsKey(itemId)) {
			result = __DASHBOARD_DATA_STORAGE.get(riskModelId).get(itemId);
		}

		return result;
	}

}
