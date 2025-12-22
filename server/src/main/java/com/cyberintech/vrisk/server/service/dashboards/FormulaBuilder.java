package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.jpa.domains.DeploymentType;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantMetricLevel;
import com.cyberintech.vrisk.server.model.jpa.domains.VariableOperation;
import com.cyberintech.vrisk.server.model.jpa.domains.VariableType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.util.ScriptEngineUtil;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Formula builder
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2020-02-28
 */
@Getter
@Setter
@Slf4j
@ToString(of = {"name", "formulaString", "script"})
public class FormulaBuilder implements Cloneable {

	private String name;

	private String formulaString;

	private String script;

	private Formulas formula;

	private Boolean isDataTypesRestricted;

	private Boolean isTechnologyCategoriesRestricted;

	private Boolean isTechnologiesRestricted;

	private Boolean isIndustryRestricted;

	private Boolean isDeploymentTypeRestricted;

	private QuantMetrics quantMetrics;

	private Set<Regulations> regulations;

	private Boolean isGeoRegulationsApplied;

	private Map<Systems, Double> geoNumberOfRecords;

	private QuantMetricLevel quantMetricLevel;

	private Set<TechnologyCategories> technologyCategories;

	private Set<Technologies> technologies;

	private Set<DataTypeClassification> dataTypeClassifications;

	private Set<Industries> industries;

	private Set<Long> dataTypeClassificationIds;

	private Set<Long> technologyCategoriesIds;

	private Set<Long> technologiesIds;

	private Set<Long> industryIds;

	private DeploymentType deploymentType;

	private RiskMetrics riskMetric;

	private Double result;

	private GraalJSScriptEngine engine;

	private InputStream fileStream;

	private Long quantLevelNumber = 0l;


	public FormulaBuilder() {
		isDataTypesRestricted = null;
		isIndustryRestricted = null;
		isDeploymentTypeRestricted = null;
		dataTypeClassificationIds = new HashSet<>();
		isTechnologyCategoriesRestricted = null;
		technologyCategoriesIds = new HashSet<>();
		isTechnologiesRestricted = null;
		technologiesIds = new HashSet<>();
		result = 0D;
		engine = ScriptEngineUtil.getJavaScriptEngine();
		geoNumberOfRecords = new HashMap<>();
		formulaString = "";
	}

	public List<IFormulaItem> getFormulaItems() {
		List<IFormulaItem> formulaItems = new ArrayList<>();
		if (formula != null) {
			formulaItems = formula.getFormulaItems().stream().collect(Collectors.toList());
		} else if (quantMetrics != null && quantMetrics.getMetricFormulaItems() != null) {
			formulaItems = quantMetrics.getMetricFormulaItems().stream().collect(Collectors.toList());
		}
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		return formulaItems;
	}

	/**
	 * Builds formula
	 */
	public FormulaBuilder build() {
		int i = 1;
		String script = "";
		String formulaString = "";
		getFormulaScript(engine);
		List<IFormulaItem> formulaItems = getFormulaItems();
		for (IFormulaItem formulaItem : formulaItems) {

			if (formulaItem.getIsOperation()) {
				VariableOperation operation = formulaItem.getOperation();
				formulaString += getFormulaOperationString(operation);
				script += getScriptOperationString(operation);

			} else {
				String variableName = "";
				if (formulaItem.getVariableType().getCode().equalsIgnoreCase("constant")) {
					variableName = "variable" + i;
					engine.put(variableName, formulaItem.getValue() != null ? formulaItem.getValue() : 0d);
					formulaString += formulaItem.getValue() != null ? String.format("%,.2f", formulaItem.getValue()) : "";
					i++;
				} else if (formulaItem.getVariableType().getCode().equalsIgnoreCase("risk_model_constant")) {
					variableName = "variable" + i;
					MetricFormulaItems metricFormulaItem = (MetricFormulaItems) formulaItem;
					engine.put(variableName, metricFormulaItem.getRiskModelConstantRef() != null && metricFormulaItem.getRiskModelConstantRef().getValue() != null ? metricFormulaItem.getRiskModelConstantRef().getValue() : 0d);
					formulaString += metricFormulaItem.getRiskModelConstantRef() != null && metricFormulaItem.getRiskModelConstantRef().getValue() != null ? String.format("%,.2f", metricFormulaItem.getRiskModelConstantRef().getValue()) : "";
					i++;
				} else if (formulaItem.getVariableType().getCode().equalsIgnoreCase("quant_metric")) {
					if (getQuantMetrics().getQuantMetricLevel().equals(QuantMetricLevel.ORGANIZATION) && formulaItem instanceof MetricFormulaItems) {
						MetricFormulaItems metricFormulaInner = ((MetricFormulaItems) formulaItem);
						variableName = String.format("QUANT_%s", metricFormulaInner.getQuantMetricRefId());
						formulaString += metricFormulaInner.getQuantMetricRef() != null ? "[" + metricFormulaInner.getQuantMetricRef().getName() + "]" : metricFormulaInner.getVariableType().getName();
					} else {
						StringBuilder deepScript = new StringBuilder();
						StringBuilder deepFormulaString = new StringBuilder();

						i += buildDeep(deepScript, deepFormulaString, ((MetricFormulaItems) formulaItem).getQuantMetricRef(), i);
						script += deepScript.toString();
						formulaString += deepFormulaString.toString();
					}
				} else {
					variableName = formulaItem.getVariableType().getCode();
					formulaString += StringUtils.isNotEmpty(formulaItem.getVariableType().getName()) ? formulaItem.getVariableType().getName() : formulaItem.getVariableType().getCode();
				}
				script += variableName;
			}
		}


		if (StringUtils.isNotEmpty(script)) {
			script = "var resultItem = " + script + ";";
		} else {
			script = "var resultItem = 0;";
		}

		this.script = script;
		this.formulaString = formulaString.trim();

		return this;
	}

	/**
	 * Builds part of the formula that is a Quant Metric
	 *
	 * @param script
	 * @param formulaString
	 */
	public int buildDeep(StringBuilder script, StringBuilder formulaString, QuantMetrics quantMetric, Integer variableNum) {
		return buildDeep(script, formulaString, quantMetric, variableNum, 0L);
	}

	/**
	 * Builds part of the formula that is a Quant Metric
	 *
	 * @param script
	 * @param formulaString
	 */
	public int buildDeep(StringBuilder script, StringBuilder formulaString, QuantMetrics quantMetric, Integer variableNum, Long level) {
		if (++level > 5) {
			throw new InternalServerErrorException("Formula exceeded its dependency deepness level limit");
		}

		List<IFormulaItem> formulaItems = new ArrayList<>();
		formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		for (IFormulaItem formulaItem : formulaItems) {
			if (formulaItem.getIsOperation()) {
				VariableOperation operation = formulaItem.getOperation();
				formulaString.append(getFormulaOperationString(operation));
				script.append(getScriptOperationString(operation));

			} else {
				String variableName = "";
				if (formulaItem.getVariableType().getCode().equalsIgnoreCase("constant")) {
					variableName = "variable" + variableNum;
					engine.put(variableName, formulaItem.getValue() != null ? formulaItem.getValue() : 0d);
					formulaString.append(formulaItem.getValue() != null ? String.format("%,.2f", formulaItem.getValue()) : "");
					variableNum++;
				} else if (formulaItem.getVariableType().getCode().equalsIgnoreCase("risk_model_constant")) {
					variableName = "variable" + variableNum;
					MetricFormulaItems metricFormulaItem = (MetricFormulaItems) formulaItem;
					engine.put(variableName, metricFormulaItem.getRiskModelConstantRef() != null && metricFormulaItem.getRiskModelConstantRef().getValue() != null ? metricFormulaItem.getRiskModelConstantRef().getValue().longValue() : 0d);
					formulaString.append(metricFormulaItem.getRiskModelConstantRef() != null && metricFormulaItem.getRiskModelConstantRef().getValue() != null ? String.format("%,.2f", metricFormulaItem.getRiskModelConstantRef().getValue()) : "");
					variableNum++;
				} else if (formulaItem.getVariableType().getCode().equalsIgnoreCase("quant_metric")) {
					variableName = String.format("QUANT_%s", getQuantMetrics().getId());
					// variableNum += buildDeep(script, formulaString, ((MetricFormulaItems) formulaItem).getQuantMetricRef(), variableNum, level);
				} else {
					variableName = formulaItem.getVariableType().getCode();
					formulaString.append(variableName);
				}

				script.append(variableName);
			}
		}
		return variableNum;
	}

	/**
	 * Get Formula Operation
	 *
	 * @param operation
	 * @return
	 */
	public static String getFormulaOperationString(VariableOperation operation) {
		String result = "";
		if (operation.equals(VariableOperation.PLUS)) {
			result += " + ";
		} else if (operation.equals(VariableOperation.MULTIPLY)) {
			result += " * ";
		} else if (operation.equals(VariableOperation.MINUS)) {
			result += " - ";
		} else if (operation.equals(VariableOperation.DIVIDE)) {
			result += " / ";
		} else if (operation.equals(VariableOperation.OPEN_BRACKET)) {
			result += "(";
		} else if (operation.equals(VariableOperation.CLOSE_BRACKET)) {
			result += ")";
		} else if (operation.equals(VariableOperation.MAX)) {
			result += " MAX";
		} else if (operation.equals(VariableOperation.MIN)) {
			result += " MIN";
		} else if (operation.equals(VariableOperation.COMMA)) {
			result += "; ";
		} else if (operation.equals(VariableOperation.ABS)) {
			result += " ABS";
		} else if (operation.equals(VariableOperation.MEDIAN)) {
			result += " MEDIAN";
		} else if (operation.equals(VariableOperation.AVERAGE)) {
			result += " AVERAGE";
		} else if (operation.equals(VariableOperation.MODE)) {
			result += " MODE";
		} else if (operation.equals(VariableOperation.SQRT)) {
			result += " SQRT";
		} else if (operation.equals(VariableOperation.SUM)) {
			result += " SUM";
		} else if (operation.equals(VariableOperation.RAND)) {
			result += " RAND";
		} else if (operation.equals(VariableOperation.POWER)) {
			result += " POW";
		} else if (operation.equals(VariableOperation.EXPONENT)) {
			result += " EXP";
		} else if (operation.equals(VariableOperation.LOG)) {
			result += " LN";
		} else if (operation.equals(VariableOperation.HYPERBOLA)) {
			result += " 1 / ";
		}

		return result;
	}

	/**
	 * Get Formula Script Operation
	 *
	 * @param operation
	 * @return
	 */
	public String getScriptOperationString(VariableOperation operation) {
		String result = "";
		if (operation.equals(VariableOperation.PLUS)) {
			result += " + ";
		} else if (operation.equals(VariableOperation.MULTIPLY)) {
			result += " * ";
		} else if (operation.equals(VariableOperation.MINUS)) {
			result += " - ";
		} else if (operation.equals(VariableOperation.DIVIDE)) {
			result += " / ";
		} else if (operation.equals(VariableOperation.OPEN_BRACKET)) {
			result += "( ";
		} else if (operation.equals(VariableOperation.CLOSE_BRACKET)) {
			result += " )";
		} else if (operation.equals(VariableOperation.MAX)) {
			result += " Math.max";
		} else if (operation.equals(VariableOperation.MIN)) {
			result += " Math.min";
		} else if (operation.equals(VariableOperation.COMMA)) {
			result += ",";
		} else if (operation.equals(VariableOperation.ABS)) {
			result += " Math.abs";
		} else if (operation.equals(VariableOperation.MEDIAN)) {
			result += " formulaMedian";
		} else if (operation.equals(VariableOperation.AVERAGE)) {
			result += " formulaAverage";
		} else if (operation.equals(VariableOperation.MODE)) {
			result += " formulaModeEmpiric";
		} else if (operation.equals(VariableOperation.SQRT)) {
			result += " Math.sqrt";
		} else if (operation.equals(VariableOperation.SUM)) {
			result += "  formulaSum";
		} else if (operation.equals(VariableOperation.RAND)) {
			result += " Math.random";
		} else if (operation.equals(VariableOperation.POWER)) {
			result += " Math.pow";
		} else if (operation.equals(VariableOperation.EXPONENT)) {
			result += " Math.exp";
		} else if (operation.equals(VariableOperation.LOG)) {
			result += " Math.log";
		} else if (operation.equals(VariableOperation.HYPERBOLA)) {
			result += " 1 / ";
		}
		return result;
	}

	public void getFormulaScript(GraalJSScriptEngine scriptEngine) {
		InputStream fileStream;
		fileStream = loadFormulaFunctionsDataFileStream("/formula-builder/formula-average.js");
		try {
			scriptEngine.eval(new InputStreamReader(fileStream));
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		fileStream = loadFormulaFunctionsDataFileStream("/formula-builder/formula-median.js");
		try {
			scriptEngine.eval(new InputStreamReader(fileStream));
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		fileStream = loadFormulaFunctionsDataFileStream("/formula-builder/formula-sum.js");
		try {
			scriptEngine.eval(new InputStreamReader(fileStream));
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
		fileStream = loadFormulaFunctionsDataFileStream("/formula-builder/formula-mode-empiric.js");
		try {
			scriptEngine.eval(new InputStreamReader(fileStream));
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Load formula functions data file content from file
	 *
	 * @param fileName
	 * @return
	 */
	public InputStream loadFormulaFunctionsDataFileStream(String fileName) {
		InputStream in = this.getClass().getResourceAsStream(fileName);

		return in;
	}

	/**
	 * Calculate Scoring Risk Metric
	 *
	 * @param metricResultMap
	 * @return
	 */
	public double calculate(Map<MetricDomains, MetricResult> metricResultMap) {
		double result = 0d;

		Map<String, MetricResult> variablesMap = new HashMap<>();
		for (Map.Entry<MetricDomains, MetricResult> entry : metricResultMap.entrySet()) {
			String variableName = entry.getKey().getCode().toUpperCase();
			variablesMap.put(variableName, entry.getValue());
		}

		result = calculate2(variablesMap);

		return result;
	}

	/**
	 * Calculate Scoring Risk Metric
	 *
	 * @param metricResultMap
	 * @return
	 */
	public double calculate2(Map<String, MetricResult> metricResultMap) {
		double result = 0d;

		for (Map.Entry<String, MetricResult> entry : metricResultMap.entrySet()) {
			String variableName = entry.getKey().toUpperCase();
			Double variableValue = entry.getValue().buildNormalizedResult();
			engine.put(variableName, variableValue);
		}

		result = getFormulaResult(result);

		return result;
	}

	private double getFormulaResult(double result) {
		Object resultItem = null;
		try {
			engine.eval(script);
			resultItem = engine.get("resultItem");
		} catch (ScriptException e) {
			if (this.quantMetrics != null) {
				log.error(MessageFormat.format("[FormulaBuilder] Failed to evaluate formula for QuantMetrics[{0}, {1}]: {2}", this.quantMetrics.getId(), this.quantMetrics.getName(), script));
			} else if (this.riskMetric != null) {
				log.error(MessageFormat.format("[FormulaBuilder] Failed to evaluate formula for RiskMetrics[{0}, {1}]: {2}", this.riskMetric.getId(), this.riskMetric.getName(), script));
			} else {
				log.warn(e.getMessage(), e);
			}
		}

		if (resultItem instanceof String) {
			result = Double.valueOf((String) resultItem);
		} else if (resultItem instanceof Double) {
			result = Double.valueOf((Double) resultItem);
		} else if (resultItem instanceof Long) {
			result = Double.valueOf((Long) resultItem);
		} else if (resultItem instanceof Integer) {
			result = Double.valueOf((Integer) resultItem);
		}
		return result;
	}

	/**
	 * Calculate Script Value Metric
	 *
	 * @param variables
	 * @return
	 */
	public double run(Map<String, Double> variables) {
		double result = 0d;

		for (Map.Entry<String, Double> entry : variables.entrySet()) {
			String variableName = entry.getKey().toUpperCase();
			Double variableValue = entry.getValue();
			engine.put(variableName, variableValue);
		}

		result = getFormulaResult(result);

		return result;
	}

	/**
	 * Get Formula Operation
	 *
	 * @param variableType
	 * @return
	 */
	public boolean isVariableUsed(VariableType variableType) {
		boolean result = false;

		if (StringUtils.contains(script, variableType.name())) {
			result = true;
		}

		return result;
	}

	/**
	 * Set Quantification metrics
	 *
	 * @param quantMetrics
	 */
	public void setQuantMetrics(QuantMetrics quantMetrics) {
		this.quantMetrics = quantMetrics;

		dataTypeClassifications = quantMetrics.getDataTypeClassifications();
		technologyCategories = quantMetrics.getTechnologyCategories();
		technologies = quantMetrics.getTechnologies();
		industries = quantMetrics.getIndustries();
		deploymentType = quantMetrics.getDeploymentType();

		dataTypeClassificationIds = quantMetrics.getDataTypeClassifications().stream().mapToLong(DataTypeClassification::getId).boxed().collect(Collectors.toSet());
		industryIds = quantMetrics.getIndustries().stream().mapToLong(Industries::getId).boxed().collect(Collectors.toSet());
		technologyCategoriesIds = quantMetrics.getTechnologyCategories().stream().mapToLong(TechnologyCategories::getId).boxed().collect(Collectors.toSet());
		technologiesIds = quantMetrics.getTechnologies().stream().mapToLong(Technologies::getId).boxed().collect(Collectors.toSet());

		isDataTypesRestricted = dataTypeClassificationIds.size() > 0;
		isTechnologyCategoriesRestricted = technologyCategoriesIds.size() > 0;
		isTechnologiesRestricted = technologiesIds.size() > 0;
		isIndustryRestricted = industryIds.size() > 0;
		isDeploymentTypeRestricted = quantMetrics.getDeploymentType() != null;

		// Regulations list
		regulations = quantMetrics.getRegulations();
		isGeoRegulationsApplied = regulations.stream().filter(regulations1 -> Boolean.TRUE.equals(regulations1.getIsGeography())).count() > 0;

		// Set Quant Metrics Level
		quantMetricLevel = quantMetrics.getQuantMetricLevel() != null ? quantMetrics.getQuantMetricLevel() : QuantMetricLevel.SYSTEM;
	}

	/**
	 * Check is Quant applicable
	 *
	 * @param system
	 * @return
	 */
	public boolean checkIsQuantApplicable(Systems system, Collection<Industries> systemIndustries) {
		boolean result = true;

		boolean isDataTypesApplicable = true;
		boolean isTechnologyApplicable = true;
		boolean isIndustryApplicable = true;
		boolean isDeploymentTypeApplicable = true;
		boolean isGeoApplicable = true;

		if (isDataTypesRestricted) {
			isDataTypesApplicable = false;
			for (DataTypeClassification dataType : system.getDataTypeClassifications()) {
				if (dataTypeClassifications.contains(dataType)) {
					isDataTypesApplicable = true;
					break;
				}
			}

			if (!isDataTypesApplicable) {
				return false;
			}
		}

		// Apply Industry restrictions
		if (isIndustryRestricted) {
			isIndustryApplicable = false;
			if (CollectionUtils.isNotEmpty(systemIndustries)) {
				for (Industries industry : systemIndustries) {
					if (industries.contains(industry)) {
						isIndustryApplicable = true;
						break;
					}
				}
			}

			// Check Organization Industry
			if (!isIndustryApplicable && system.getOrganization() != null && system.getOrganization().getIndustry() != null) {
				if (industries.contains(system.getOrganization().getIndustry())) {
					isIndustryApplicable = true;
				}
			}

			if (!isIndustryApplicable) {
				return false;
			}
		}

		// Apply deployment type restrictions
		if (isDeploymentTypeRestricted) {
			isDeploymentTypeApplicable = false;
			if (deploymentType.equals(system.getDeploymentType())) {
				isDeploymentTypeApplicable = true;
			}

			if (!isDeploymentTypeApplicable) {
				return false;
			}
		}

		if (isTechnologyCategoriesRestricted) {
			isTechnologyApplicable = false;
			for (Technologies technology : system.getTechnologies()) {
				if (technologyCategories.contains(technology.getTechnologyCategory())) {
					isTechnologyApplicable = true;
					break;
				}
			}

			if (!isTechnologyApplicable) {
				return false;
			}
		}

		// Check is Technology Applicable to the Metric
		if (isTechnologiesRestricted) {
			isTechnologyApplicable = false;
			for (Technologies technology : system.getTechnologies()) {
				if (technologies.contains(technology)) {
					isTechnologyApplicable = true;
					break;
				}
			}

			if (!isTechnologyApplicable) {
				return false;
			}
		}

		// Process Regulations logic for system
		if (CollectionUtils.isNotEmpty(regulations)) {
			geoNumberOfRecords.put(system, 0D);
			for (Regulations regulation : regulations) {
				// Check Data Types in regulations
				if (Boolean.TRUE.equals(regulation.getIsDataType())) {
					isDataTypesApplicable = false;
					for (DataTypeClassification dataType : system.getDataTypeClassifications()) {
						if (regulation.getDataTypes().contains(dataType)) {
							isDataTypesApplicable = true;
							break;
						}
					}

					if (!isDataTypesApplicable) {
						return false;
					}
				}

				// Check Technology in regulations
				if (Boolean.TRUE.equals(regulation.getIsTechnology()) && regulation.getTechnologyCategories().size() > 0) {
					isTechnologyApplicable = false;
					for (Technologies technology : system.getTechnologies()) {
						if (regulation.getTechnologyCategories().contains(technology.getTechnologyCategory())) {
							isTechnologyApplicable = true;
							break;
						}
					}

					if (!isTechnologyApplicable) {
						return false;
					}
				}

				// Check Industry in regulations
				if (Boolean.TRUE.equals(regulation.getIsIndustry()) && regulation.getIndustries().size() > 0) {
					isIndustryApplicable = false;
					Set<Industries> regulationIndustries = regulation.getIndustries();
					if (system.getOrganization() != null && regulationIndustries.contains(system.getOrganization().getIndustry())) {
						isIndustryApplicable = true;
					}
					if (!isIndustryApplicable && CollectionUtils.isNotEmpty(systemIndustries)) {
						for (Industries industry : systemIndustries) {
							if (regulationIndustries.contains(industry)) {
								isIndustryApplicable = true;
								break;
							}
						}
					}

					if (!isIndustryApplicable) {
						return false;
					}
				}

				// Check Industry in regulations
				if (Boolean.TRUE.equals(regulation.getIsGeography()) && (regulation.getCountries().size() > 0 || regulation.getStates().size() > 0)) {
					isGeoApplicable = false;
					Set<SystemGeoParameters> systemGeoParams = system.getSystemGeoParameters();
					Map<Long, List<State>> countryStates = regulation.getStates().stream().collect(Collectors.groupingBy(state -> state.getCountry().getId()));
					for (Country country : regulation.getCountries()) {
						if (!countryStates.containsKey(country.getId())) {
							countryStates.put(country.getId(), new ArrayList<>());
						}
					}

					for (SystemGeoParameters systemGeoParameter : systemGeoParams) {
						if (systemGeoParameter.getState() == null && systemGeoParameter.getCountry() != null && countryStates.containsKey(systemGeoParameter.getCountry().getId())) {
							if (systemGeoParameter.getNumberOfRecProcessed() != null)
								geoNumberOfRecords.put(system, geoNumberOfRecords.get(system) + systemGeoParameter.getNumberOfRecProcessed());
							isGeoApplicable = true;
						} else if (
							systemGeoParameter.getCountry() != null
								&& countryStates.containsKey(systemGeoParameter.getCountry().getId())
								&& countryStates.get(systemGeoParameter.getCountry().getId()).size() == 0
						) {
							if (systemGeoParameter.getNumberOfRecProcessed() != null)
								geoNumberOfRecords.put(system, geoNumberOfRecords.get(system) + systemGeoParameter.getNumberOfRecProcessed());
							isGeoApplicable = true;
						} else if (
							systemGeoParameter.getState() != null
								&& countryStates.containsKey(systemGeoParameter.getState().getCountry().getId())
								&& countryStates.get(systemGeoParameter.getState().getCountry().getId()).contains(systemGeoParameter.getState())
						) {
							if (systemGeoParameter.getNumberOfRecProcessed() != null)
								geoNumberOfRecords.put(system, geoNumberOfRecords.get(system) + systemGeoParameter.getNumberOfRecProcessed());
							isGeoApplicable = true;
						}
					}

					if (!isGeoApplicable) {
						return false;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Check is Quant applicable
	 *
	 * @param system
	 * @return
	 */
	public Double calculateGeoRecordsNumber(Systems system) {
		Double result = 0d;

		return result;
	}

	/**
	 * Check is Quant applicable
	 *
	 * @param organization
	 * @return
	 */
	public boolean checkIsQuantApplicable(Organizations organization) {
		boolean result = true;

		// Process Regulations logic for system
		if (CollectionUtils.isNotEmpty(regulations)) {

		}

		return result;
	}

	public static Long buildQuantMetricEmbedLevel(QuantMetrics quantMetric, Long parentLevel) {
		if (parentLevel == null) {
			parentLevel = 0L;
		}

		Long result = parentLevel + 1;

		// Max level is 8
		if (result > 8) {
			return result;
		}

		// Calculate DEEP level of the metric
		if (quantMetric != null && quantMetric.getMetricFormulaItems() != null) {
			Long maxChildQuantLevel = result;
			for (MetricFormulaItems formulaItem: quantMetric.getMetricFormulaItems()) {
				if (formulaItem != null && formulaItem.getQuantMetricRef() != null) {
					Long childQuantLevel = buildQuantMetricEmbedLevel(formulaItem.getQuantMetricRef(), result);
					if (childQuantLevel > maxChildQuantLevel) {
						maxChildQuantLevel = childQuantLevel;
						// break;
					}
				}
			}

			// Detect Max Child Quant Level
			if (maxChildQuantLevel > result) {
				result = maxChildQuantLevel;
			}
		}

		return result;
	}

	public void calcQuantLevel() {
		quantLevelNumber = buildQuantMetricEmbedLevel(this.getQuantMetrics(), null);
	}

	/**
	 * Static constructor
	 *
	 * @param riskMetric
	 * @return
	 */
	public static FormulaBuilder of(RiskMetrics riskMetric) {
		FormulaBuilder result = new FormulaBuilder();
		result.setFormula(riskMetric.getFormula());
		if (riskMetric.getFormula() != null && riskMetric.getFormula().getFormula() != null)
			result.setFormulaString(riskMetric.getFormula().getFormula());
		result.setName(riskMetric.getName());
		result.setRiskMetric(riskMetric);

		return result;
	}

	/**
	 * Static constructor
	 *
	 * @param quantMetrics
	 * @param name
	 * @return
	 */
	public static FormulaBuilder of(QuantMetrics quantMetrics, String name) {
		FormulaBuilder result = new FormulaBuilder();
		result.setQuantMetrics(quantMetrics);
		result.setName(name);

		return result;
	}
}
