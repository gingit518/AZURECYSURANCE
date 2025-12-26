package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.service.*;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Exposure Metrics Dashboard Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-02-27
 */
@Service
@Slf4j
public class ExposureMetricsDashboardService extends DashboardServiceBase {
	@Autowired
	private QuantsRepository quantsRepository;

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	@Autowired
	private IndustryRepository industryRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private ProcessRepository processRepository;

	@Autowired
	private QuantMetricsRepository quantMetricsRepository;

	@Autowired
	private QuantMetricsService quantMetricsService;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private ScoringQuestionsDashboardService scoringQuestionsDashboardService;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private VendorService vendorService;

	/**
	 * Get Dashboard definition
	 *
	 * @param riskModelId The risk model identifier.
	 *
	 * @return Dashboard
	 */
	public Map<Systems, Map<QuantMetrics, ExposureMetricResult>> getSystemsScoringData(Long riskModelId) {
		return getSystemsScoringData(riskModelId, null, null);
	}

	/**
	 * Get Dashboard definition
	 *
	 * @param riskModelId The risk model identifier.
	 * @param systemListFilter List of Systems to process. If {@code null}, then all Systems of current Organization would be processed.
	 *
	 * @return Dashboard
	 */
	public Map<Systems, Map<QuantMetrics, ExposureMetricResult>> getSystemsScoringData(Long riskModelId, List<Systems> systemListFilter) {
		return getSystemsScoringData(riskModelId, systemListFilter, null);
	}

	/**
	 * Get Dashboard definition.
	 *
	 * @param riskModelId The risk model identifier.
	 * @param systemListFilter List of Systems to process. If {@code null}, then all Systems of current Organization would be processed.
	 * @param quantDomainFilter List of Quants Domains to process. If {@code null}, then all Quants Domains would be processed.
	 *
	 * @return Dashboard
	 */
	public Map<Systems, Map<QuantMetrics, ExposureMetricResult>> getSystemsScoringData(Long riskModelId, List<Systems> systemListFilter, List<QuantsDomain> quantDomainFilter) {

		// Registering Result Data Set
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> result = new HashMap<>();

		long startTime = System.currentTimeMillis();

		// Obtain Most Valuable data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationService.getOrganization(riskModel.getOrganizationId());
		List<Systems> allSystemsList;
		if (systemListFilter == null) {
			allSystemsList = systemRepository.getAllByOrganizationAndNotEtl(riskModel.getOrganizationId());
		} else {
			allSystemsList = systemListFilter;
			List<Long> systemIdsList = allSystemsList.stream().mapToLong(Systems::getId).boxed().collect(Collectors.toList());
		}

		Map<Systems, Set<Processes>> allSystemProcessesMap = getSystemProcessesMap(organization);

		List<QuantMetrics> quantMetricList;
		if (quantDomainFilter == null) {
			quantMetricList = quantMetricsRepository.getListByRiskModelId(riskModelId);
		} else {
			List<Long> domainIds = quantDomainFilter.stream().mapToLong(QuantsDomain::getId).boxed().collect(Collectors.toList());
			quantMetricList = quantMetricsRepository.getListByRiskModelIdAndQuantIds(riskModelId, domainIds);
		}

		// Filtering SYSTEM Level formula builders
		List<FormulaBuilder> formulaBuilderList = quantMetricList.stream()
			.filter(quantMetrics -> {
				Boolean isMetricApplicable = quantMetrics.getQuantMetricLevel() == null
					|| quantMetrics.getQuantMetricLevel().equals(QuantMetricLevel.SYSTEM)
					|| quantMetrics.getQuantMetricLevel().equals(QuantMetricLevel.PROCESS)
					;

				return isMetricApplicable;
			})
			.map(quantMetrics -> FormulaBuilder.of(quantMetrics, quantMetrics.getName()).build()).collect(Collectors.toList());
		// Map<MetricDomains, MetricStatistics> metricQuestionStatsMap = metricDomains.stream().collect(Collectors.toMap(domain -> domain, domain -> MetricStatistics.of(questionsMetricsMap.get(domain))));

		// Check is regulations applied for t
		boolean isGeoRegulationsApplied = false;

		// TODO Verify THis
		Map<Systems, MetricResult> systemMitigateDataMap = scoringQuestionsDashboardService.getSystemsMitigateData(riskModelId);

		// Preparing Result
		HashMap<String, Double> variables = new HashMap<>();
		applyOrganizationVariables(organization, variables);
		for (Systems system : allSystemsList) {

			Set<Industries> systemIndustries = industryRepository.getVendorIndustriesForSystem(system.getId());

			// Create Empty Metric Result
			Map<QuantMetrics, ExposureMetricResult> metricResultMap = new HashMap<>();
			result.put(system, metricResultMap);

			HashMap<String, Double> systemVariables = new HashMap<>(variables);
			applySystemVariables(system, systemVariables, systemMitigateDataMap);
			for (FormulaBuilder formulaBuilder : formulaBuilderList) {
				Double systemResult = 0d;

				// We must reset Number of Records for System for each build, because GEO Records may override this value
				systemVariables.put(VariableType.SYSTEM_NUMBER_OF_REC.name(), system.getNumberOfRecProcessed() != null ? system.getNumberOfRecProcessed() : 0D);

				systemResult = buildSystemAndProcessValue(allSystemProcessesMap, systemVariables, formulaBuilder, system, systemIndustries);
				if (systemResult != null) {
					metricResultMap.put(formulaBuilder.getQuantMetrics(), ExposureMetricResult.of(formulaBuilder.getQuantMetrics().getName(), systemResult, formulaBuilder));
				}
			}
		}

		log.info(MessageFormat.format("## Build Exposure metrics data time {0,number,#}", System.currentTimeMillis() - startTime));

		return result;
	}

	/**
	 * Apply Organization level variables for Metrics Engine
	 *
	 * @param organization
	 * @param variables
	 */
	public static void applyOrganizationVariables(Organizations organization, Map<String, Double> variables) {
		variables.put(VariableType.ORGANIZATION_REVENUE.name(), organization.getAverageRevenue() != null ? organization.getAverageRevenue() : 0D);
		variables.put(VariableType.ORGANIZATION_RECORD_PRICE.name(), organization.getRecordPriceLimit() != null ? organization.getRecordPriceLimit() : Organizations.DEFAULT_RECORD_PRICE);
		variables.put(VariableType.MARKET_CAPITALIZATION.name(), organization.getMarketCapitalizationNumber() != null ? organization.getMarketCapitalizationNumber() : 0D);
		variables.put(VariableType.REVENUE.name(), organization.getRevenue() != null ? organization.getRevenue() : 0D);
		variables.put(VariableType.EBIDTA.name(), organization.getEbitda() != null ? organization.getEbitda() : 0D);
		variables.put(VariableType.DEBT.name(), organization.getDebt() != null ? organization.getDebt() : 0D);
		variables.put(VariableType.PENSION_DEBT.name(), organization.getPensionDebt() != null ? organization.getPensionDebt() : 0D);
		variables.put(VariableType.GROSS_RISK_BEARING_CAPACITY.name(), organization.getGrossRiskBearingCapacity() != null ? organization.getGrossRiskBearingCapacity() : 0D);
	}

	/**
	 * Apply System level variables for Metrics Engine
	 *
	 * @param system
	 * @param variables
	 */
	public static void applySystemVariables(Systems system, Map<String, Double> variables, Map<Systems, MetricResult> systemMitigateDataMap) {
		variables.put(VariableType.SYSTEM_NUMBER_OF_REC.name(), system.getNumberOfRecProcessed() != null ? system.getNumberOfRecProcessed() : 0D);
		variables.put(VariableType.RTO.name(), system.getRto() != null ? system.getRto() : 0D);
		variables.put(VariableType.SYSTEM_COST_TO_RESTORE.name(), system.getCostToRestore() != null ? system.getCostToRestore() : 0D);
		variables.put(VariableType.RRTO.name(), 0D); // TODO Define RRTO value

		if (systemMitigateDataMap != null) {
			variables.put(VariableType.MITIGATION_RISK.name(), systemMitigateDataMap.containsKey(system) ? systemMitigateDataMap.get(system).getResult() : 0D);
		}
	}

	/**
	 *
	 */
	public Map<Organizations, Map<QuantMetrics, ExposureMetricResult>> getVendorsScoringData(Long riskModelId, List<Organizations> vendorListFilter, List<QuantsDomain> quantDomainFilter) {

		// Registering Result Data Set
		Map<Organizations, Map<QuantMetrics, ExposureMetricResult>> result = new HashMap<>();

		// Obtain Most Valuable data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationService.getOrganization(riskModel.getOrganizationId());
		List<Organizations> allVendorsList;
		if (vendorListFilter == null || vendorListFilter.size() == 0) {
			allVendorsList = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId()).stream().map(AssociateVendors::getVendor).collect(Collectors.toList());
		} else {
			allVendorsList = vendorListFilter;
//			List<Long> systemIdsList = allVendorsList.stream().mapToLong(Organizations::getId).boxed().collect(Collectors.toList());
		}

		// TODO: 26.03.20 finish vendors scoring data building

		return result;
	}

	/**
	 * Build Organization Scoring Data
	 * @param riskModel
	 * @return
	 */
	public Map<QuantMetrics, ExposureMetricResult> getOrganizationScoringData(RiskModels riskModel, List<QuantsDomain> quantDomainFilter) {
		Map<QuantMetrics, ExposureMetricResult> result = new HashMap<>();

		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		List<Systems> allOrganizationSystems = systemRepository.getAllByOrganization(riskModel.getOrganizationId());
		Map<Systems, List<Industries>> systemIndustriesMap = getSystemIndustriesListMap(riskModel);
		Map<Systems, MetricResult> systemMitigateDataMap = scoringQuestionsDashboardService.getSystemsMitigateData(riskModel.getId());

		List<QuantMetrics> quantMetricList;
		if (quantDomainFilter == null || quantDomainFilter.size() < 1) {
			quantMetricList = quantMetricsRepository.getListByRiskModelIdAndQuantMetricLevel(riskModel.getId(), QuantMetricLevel.ORGANIZATION);
		} else {
			List<Long> domainIds = quantDomainFilter.stream().mapToLong(QuantsDomain::getId).boxed().collect(Collectors.toList());
			quantMetricList = quantMetricsRepository.getListByRiskModelIdAndQuantIds(riskModel.getId(), domainIds);
		}
		List<FormulaBuilder> formulaBuilderList = quantMetricList.stream()
			.filter(quantMetrics -> quantMetrics.getQuantMetricLevel() != null && quantMetrics.getQuantMetricLevel().equals(QuantMetricLevel.ORGANIZATION))
			.map(quantMetrics -> FormulaBuilder.of(quantMetrics, quantMetrics.getName()).build()).collect(Collectors.toList());

		Map<Systems, Set<Processes>> allSystemProcessesMap = getSystemProcessesMap(organization);

		log.info(String.format("Building Organization Quants for the Risk Model [%d, %s]. Found %d metrics.", riskModel.getId(), riskModel.getName(), formulaBuilderList.size()));
		Map<String, Double> variables = new HashMap<>();
		applyOrganizationVariables(organization, variables);
		for (FormulaBuilder formulaBuilder : formulaBuilderList) {

			// Verify is Metric applicable to Organization
			boolean isQuantApplicable = false;
			if (CollectionUtils.isNotEmpty(allOrganizationSystems)) {
				for (Systems system : allOrganizationSystems) {
					// Check is Quant Applicable for the System
					if (formulaBuilder.checkIsQuantApplicable(system, systemIndustriesMap.get(system))) {
						log.info(String.format("Quant [%s] is applicable by the System [%s, %s]", formulaBuilder.getName(), system.getId(), system.getName()));
						isQuantApplicable = true;
						break;
					}
				}
			} else {
				log.warn("There is no systems in the Organization. Some metrics may not work properly.");
			}
			// If Quant is not applicable - apply it
			if (!isQuantApplicable) {
				log.info(String.format("Quant [%s] is NOT APPLICABLE in the Risk Model [%d]", formulaBuilder.getName(), riskModel.getId()));
				continue;
			}

			Double organizationResult = 0D;
			if (formulaBuilder.isVariableUsed(VariableType.SYSTEM_NUMBER_OF_REC) || formulaBuilder.isVariableUsed(VariableType.PROCESS_REVENUE)) {
				organizationResult = calculateSystemLevelExposure(allOrganizationSystems, systemIndustriesMap, allSystemProcessesMap, variables, formulaBuilder, organizationResult, systemMitigateDataMap);
			} else {
				organizationResult = formulaBuilder.run(variables);
			}
			result.put(formulaBuilder.getQuantMetrics(), ExposureMetricResult.of(formulaBuilder.getQuantMetrics().getName(), organizationResult, formulaBuilder));
		}

		return result;
	}

	private Map<Systems, List<Industries>> getSystemIndustriesListMap(RiskModels riskModel) {
		// Fill system industries map
		Set<ImmutablePair<Systems, Industries>> vendorIndustriesForSystemByOrganization = industryRepository.getVendorIndustriesForSystemByOrganization(riskModel.getOrganizationId());
		Map<Systems, List<Industries>> systemIndustriesMap = vendorIndustriesForSystemByOrganization.stream().collect(
			Collectors.groupingBy(
				systemsIndustriesImmutablePair -> systemsIndustriesImmutablePair.getKey(),
				Collectors.mapping(systemsIndustriesImmutablePair -> systemsIndustriesImmutablePair.getValue(), Collectors.toList())
			)
		);
		return systemIndustriesMap;
	}

	/**
	 * Build Organization Scoring Data
	 * @param riskModel
	 * @return
	 */
	public Map<QuantMetrics, ExposureMetricResult> getOrganizationCumulativeScoringData(RiskModels riskModel, List<Long> quantDomainFilter) {
		Map<QuantMetrics, ExposureMetricResult> result = new LinkedHashMap<>();
		Map<QuantMetrics, ExposureMetricResult> tmpResult = new HashMap<>();

		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		List<Systems> allSystemsList = systemRepository.getAllByOrganizationAndNotEtl(riskModel.getOrganizationId());
		Map<Systems, MetricResult> systemMitigateDataMap = scoringQuestionsDashboardService.getSystemsMitigateData(riskModel.getId());

		// Fill system industries map
		Map<Systems, List<Industries>> systemIndustriesMap = getSystemIndustriesListMap(riskModel);

		List<QuantMetrics> quantMetricList;
		if (quantDomainFilter == null || quantDomainFilter.size() < 1) {
			quantMetricList = quantMetricsRepository.getListByRiskModelId(riskModel.getId());
		} else {
			quantMetricList = quantMetricsRepository.getListByRiskModelIdAndQuantIds(riskModel.getId(), quantDomainFilter);
		}
		List<FormulaBuilder> formulaBuilderList = quantMetricList.stream().map(quantMetrics -> FormulaBuilder.of(quantMetrics, quantMetrics.getName()).build()).collect(Collectors.toList());
		formulaBuilderList.forEach(FormulaBuilder::calcQuantLevel);
		formulaBuilderList.sort(Comparator.comparing(FormulaBuilder::getQuantLevelNumber));

		Map<Systems, Set<Processes>> allSystemProcessesMap = getSystemProcessesMap(organization);

		Map<String, Double> variables = new HashMap<>();
		applyOrganizationVariables(organization, variables);
		for (int i = 0; i < formulaBuilderList.size(); i++) {

			FormulaBuilder formulaBuilder = formulaBuilderList.get(i);

			Double organizationResult = 0D;
			String quantVariableName = String.format("QUANT_%s", formulaBuilder.getQuantMetrics().getId());
			// if (!formulaBuilder.isVariableUsed(VariableType.SYSTEM_NUMBER_OF_REC) && !formulaBuilder.isVariableUsed(VariableType.PROCESS_REVENUE) && !formulaBuilder.isVariableUsed(VariableType.RTO)) {
			if (
				formulaBuilder.getQuantMetrics() != null
				&& QuantMetricLevel.ORGANIZATION.equals(formulaBuilder.getQuantMetrics().getQuantMetricLevel())
				&& !formulaBuilder.isVariableUsed(VariableType.PROCESS_REVENUE)
				&& !formulaBuilder.isVariableUsed(VariableType.SYSTEM_NUMBER_OF_REC)
			) {

				// Verify is Metric applicable to Organization
				boolean isQuantApplicable = false;
				if (CollectionUtils.isNotEmpty(allSystemsList)) {
					for (Systems system : allSystemsList) {
						// Check is Quant Applicable for the System
						if (formulaBuilder.checkIsQuantApplicable(system, systemIndustriesMap.get(system))) {
							log.info(String.format("Quant [%s] is applicable by the System [%s, %s]", formulaBuilder.getName(), system.getId(), system.getName()));
							isQuantApplicable = true;
							break;
						}
					}
				} else {
					log.warn("There is no systems in the Organization. Some metrics may not work properly.");
				}

				// If Quant is not applicable - apply it
				if (!isQuantApplicable) {
					log.info(String.format("Quant [%s] is NOT APPLICABLE in the Risk Model [%d]", formulaBuilder.getName(), riskModel.getId()));
					continue;
				}

				organizationResult = formulaBuilder.run(variables);

			} else {
				organizationResult = calculateSystemLevelExposure(allSystemsList, systemIndustriesMap, allSystemProcessesMap, variables, formulaBuilder, organizationResult, systemMitigateDataMap);
			}
			variables.put(quantVariableName, organizationResult);
			tmpResult.put(formulaBuilder.getQuantMetrics(), ExposureMetricResult.of(formulaBuilder.getQuantMetrics().getName(), organizationResult, formulaBuilder));
		}

		// Resort results
		formulaBuilderList.sort((o1, o2) -> o1.getQuantMetrics().getName().compareTo(o2.getQuantMetrics().getName()));
		for (int i = 0; i < formulaBuilderList.size(); i++) {
			result.put(formulaBuilderList.get(i).getQuantMetrics(), tmpResult.get(formulaBuilderList.get(i).getQuantMetrics()));
		}

		return result;
	}

	private Double calculateSystemLevelExposure(List<Systems> allSystemsList, Map<Systems, List<Industries>> systemIndustriesMap, Map<Systems, Set<Processes>> allSystemProcessesMap, Map<String, Double> variables, FormulaBuilder formulaBuilder, Double organizationResult, Map<Systems, MetricResult> systemMitigateDataMap) {

		Optional<IFormulaItem> firstItem = formulaBuilder.getFormulaItems().stream().findFirst();
		IFormulaItem aggregateItem = firstItem.orElse(FormulaItems.ofOperation(VariableOperation.NONE));
		DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
		for (Systems system : allSystemsList) {
			// Create Empty Metric Result
			Map<QuantMetrics, ExposureMetricResult> metricResultMap = new HashMap<>();
			applySystemVariables(system, variables, systemMitigateDataMap);
			Double systemResult = buildSystemAndProcessValue(allSystemProcessesMap, variables, formulaBuilder, system, systemIndustriesMap.get(system));

			if (systemResult != null) {
				organizationResult += systemResult;
				descriptiveStatistics.addValue(systemResult);
			}
		}

		// TODO need to process linear Aggregation Flow for all First Level formula Aggregators
		if (formulaBuilder.getQuantMetrics() != null && QuantMetricLevel.ORGANIZATION.equals(formulaBuilder.getQuantMetrics().getQuantMetricLevel()) && aggregateItem.getOperation() != null) {
			log.warn("## Rebuilding Aggregation for the metric: " + formulaBuilder.getQuantMetrics().getName());
			switch (aggregateItem.getOperation()) {
				case MAX:
					organizationResult = descriptiveStatistics.getMax();
					break;
				case MIN:
					organizationResult = descriptiveStatistics.getMin();
					break;
				case AVERAGE:
					organizationResult = descriptiveStatistics.getMean();
					break;
				case MEDIAN:
					organizationResult = descriptiveStatistics.getPercentile(50);
					break;
				case MODE:
					// Not Available
					// organizationResult = descriptiveStatistics.getM(50);
					break;
			}
		}

		return organizationResult;
	}

	/**
	 * Build System Level Drilldown
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @return
	 */
	public DashboardDTO buildOrganizationDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationService.getOrganization(riskModel.getOrganizationId());

		DashboardSectionDTO section1 = new DashboardSectionDTO(20301L, clientMessage.getMessage(""), null);
		dashboard.getSections().add(section1);

		Long metricId = null;
		Long metricDomainId = null;
		QuantMetrics quantMetric = null;
		Quants metricDomain = null;
		List<Long> quantDomainFilter = null;
		if (drilldown.getParams().containsKey("metricId")) {
			metricId = Long.valueOf(drilldown.getParams().get("metricId"));
			quantMetric = quantMetricsService.getQuantMetric(metricId);
			metricDomainId = quantMetric.getQuant().getId();
			metricDomain = quantMetric.getQuant();
		} else if (drilldown.getParams().containsKey("metricDomain")) {
			metricDomainId = Long.valueOf(drilldown.getParams().get("metricDomain"));
			metricDomain = quantsRepository.findById(metricDomainId).get();
		}

		if (metricDomainId != null) quantDomainFilter = Arrays.asList(metricDomainId);

		Map<QuantMetrics, ExposureMetricResult> organizationCumulativeScoring = getOrganizationCumulativeScoringData(riskModel, quantDomainFilter);
		// Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap = getSystemsScoringData(riskModelId, null, quantDomainFilter);

		// Apply Dashboard name
		String dashboardItemName = quantMetric != null ? quantMetric.getName() : (metricDomain != null ? metricDomain.getName() : "ALL");
		dashboard.setName(String.format(clientMessage.getMessage("Organization Scoring for: %s"), dashboardItemName));

		Double totalSystemExposure = 0d;
		Double maxSystemExposure = 0d;
		Map<Quants, ExposureMetricResult> totalQuantValuesMap = new HashMap<>();

		/*
		// Adding total Quants data
		DashboardTableItemDTO dashboardItem0 = new DashboardTableItemDTO(1102l, "");
		dashboardItem0.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$TOTAL_EXPOSURE$METRIC_QUANT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$TOTAL_EXPOSURE$SUMMARY_HEADER)
		));
		section1.getDashboardItems().add(dashboardItem0);

		// Adding total Quants data
		DashboardTableItemDTO dashboardItem02 = new DashboardTableItemDTO(1102l, clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$INSURANCE_QUANTIFICATION$ITEM_NAME));
		dashboardItem02.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$TOTAL_EXPOSURE$METRIC_QUANT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$TOTAL_EXPOSURE$SUMMARY_HEADER)
		));
		section1.getDashboardItems().add(dashboardItem02);
		 */

		DashboardTableItemDTO dashboardItem01 = new DashboardTableItemDTO(1103l, "");
		dashboardItem01.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$VAR_AND_VAL$VARIABLE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$VAR_AND_VAL$VALUE_HEADER)
		));
		section1.getDashboardItems().add(dashboardItem01);
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.ORGANIZATION_REVENUE.name()), $I(organization.getAverageRevenue()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.MARKET_CAPITALIZATION.name()), $I(organization.getMarketCapitalizationNumber()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.REVENUE.name()), $I(organization.getRevenue()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.EBIDTA.name()), $I(organization.getEbitda()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.DEBT.name()), $I(organization.getDebt()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.PENSION_DEBT.name()), $I(organization.getPensionDebt()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.GROSS_RISK_BEARING_CAPACITY.name()), $I(organization.getGrossRiskBearingCapacity()).round(0).applyTextAlign("right")));

		// Quant Metrics Summary
		DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(1105l, clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$ITEM_NAME));
		section1.getDashboardItems().add(dashboardItem1);
		dashboardItem1.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$METRIC_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$QUANT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$REGULATIONS_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$INDUSTRIES_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$FORMULA_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$CALCULATIONS_HEADER)
		));
		for (Map.Entry<QuantMetrics, ExposureMetricResult> entry : organizationCumulativeScoring.entrySet()) {
			ExposureMetricResult exposureMetricResult = entry.getValue();
			QuantMetrics currentQuantMetric = entry.getKey();

			if (metricId != null && !metricId.equals(currentQuantMetric.getId())) {
				continue;
			}

			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			String quantDetails = currentQuantMetric.getQuant() != null ? currentQuantMetric.getQuant().getName() : "";
			String regulations = currentQuantMetric.getRegulations().stream().map(Regulations::getAcronym).collect(Collectors.joining(", "));
			String industries = currentQuantMetric.getIndustries().stream().map(Industries::getName).collect(Collectors.joining("; "));
			if (currentQuantMetric.getDataTypeClassifications().size() > 0) {
				quantDetails += " (" + StringUtils.join(currentQuantMetric.getDataTypeClassifications().stream().map(DataTypeClassification::getName).collect(Collectors.toList()), ", ") + ")";
			}
			rowItems.add(sI(exposureMetricResult.getMetricName()));
			rowItems.add(sI(quantDetails));
			rowItems.add(sI(regulations));
			rowItems.add(sI(industries));
			rowItems.add(sI(exposureMetricResult.getFormulaBuilder().getFormulaString()));
			rowItems.add($I(exposureMetricResult.getResult(), exposureMetricResult.getMeasurementUnit("$")).round(0));
			dashboardItem1.getGridItems().add(rowItems);

			if (exposureMetricResult.getResult() != null) totalSystemExposure += exposureMetricResult.getResult();

			// Qual Domain Result
			ExposureMetricResult quantDomainResult = totalQuantValuesMap.get(quantMetric.getQuant());
			if (quantDomainResult == null) {
				quantDomainResult = ExposureMetricResult.of(quantMetric.getQuant().getName(), 0d);
				totalQuantValuesMap.put(quantMetric.getQuant(), quantDomainResult);
			}
			quantDomainResult.setResult(quantDomainResult.getResult() + exposureMetricResult.getResult());
		}

		dashboardItem1.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage("Total")).applyTextAlign("right").applyHeader(true).applyColspan(5l),
			$I(totalSystemExposure, "").round(0).applyTextAlign("right").applyBackgroundColor("yellow").applyHeader(true)
		));

		/*
		dashboardItem1.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$MAX_EXPOSURE$ITEM_NAME_STATIC)).applyTextAlign("right").applyHeader(true).applyColspan(4l),
			$I(maxSystemExposure, "$").round(0).applyTextAlign("right").applyBackgroundColor("yellow").applyHeader(true)
		));
		*/

		/*
		for (Map.Entry<Quants, ExposureMetricResult> entry : totalQuantValuesMap.entrySet()) {
			ExposureMetricResult quantDomainResult = entry.getValue();

			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(quantDomainResult.getMetricName()));
			rowItems.add($I(quantDomainResult.getResult(), "$").round(0));

			if (entry.getKey().getId().equals(QuantsDomain.CYBER_INSURANCE_NEEDS.getId())) {
				dashboardItem02.getGridItems().add(rowItems);
			} else {
				dashboardItem0.getGridItems().add(rowItems);
				totalSystemExposure += quantDomainResult.getResult();
				if (quantDomainResult.getResult() > maxSystemExposure) maxSystemExposure = quantDomainResult.getResult();
			}
		}
		*/

		dashboardItem01.setName(MessageFormat.format(clientMessage.getMessage("Total for Organization: {0}"), sRound(totalSystemExposure)));

		return dashboard;
	}

	/**
	 * Build System Level Drilldown
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @return
	 */
	public DashboardDTO buildSystemDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationService.getOrganization(riskModel.getOrganizationId());

		DashboardSectionDTO section1 = new DashboardSectionDTO(20301L, clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$ITEM_NAME), null);
		dashboard.getSections().add(section1);

		Long systemId = Long.valueOf(drilldown.getParams().get("system"));
		Systems system = systemsService.getSystemForCurrentOrganization(systemId);
		String systemDatatypesString = system.getDataTypeClassifications().stream().map(DataTypeClassification::getName).collect(Collectors.joining(", "));
		String technologyCategoriesString = system.getTechnologies().stream().map(Technologies::getTechnologyCategory).map(TechnologyCategories::getName).collect(Collectors.joining(", "));
		String technologiesString = system.getTechnologies().stream().map(Technologies::getName).collect(Collectors.joining(", "));
		List<Processes> allProcessList = processRepository.getListBySystem(system.getId());
		Set<Industries> systemIndustries = industryRepository.getVendorIndustriesForSystem(system.getId());

		// Create Metric Domain Filter
		List<QuantsDomain> quantDomainFilter = null;
		if (drilldown.getParams().containsKey("metricDomain")) {
			Long metricDomainId = Long.valueOf(drilldown.getParams().get("metricDomain"));
			QuantsDomain metricDomain = QuantsDomain.of(metricDomainId);
			quantDomainFilter = Arrays.asList(metricDomain);
		}

		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap = getSystemsScoringData(riskModelId, Arrays.asList(system), quantDomainFilter);

		// Apply Dashboard name
		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$ITEM_NAME), system.getName()));

		Double totalSystemExposure = 0d;
		Double maxSystemExposure = 0d;
		Map<Quants, ExposureMetricResult> totalQuantValuesMap = new HashMap<>();

		// Adding total Quants data
		DashboardTableItemDTO dashboardItem0 = new DashboardTableItemDTO(1102l, "");
		dashboardItem0.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$TOTAL_EXPOSURE$METRIC_QUANT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$TOTAL_EXPOSURE$SUMMARY_HEADER)
		));
		section1.getDashboardItems().add(dashboardItem0);

		// Adding total Quants data
		DashboardTableItemDTO dashboardItem02 = new DashboardTableItemDTO(1102l, clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$INSURANCE_QUANTIFICATION$ITEM_NAME));
		dashboardItem02.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$TOTAL_EXPOSURE$METRIC_QUANT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$TOTAL_EXPOSURE$SUMMARY_HEADER)
		));
		section1.getDashboardItems().add(dashboardItem02);

		DashboardTableItemDTO dashboardItem01 = new DashboardTableItemDTO(1103l, "");
		dashboardItem01.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$VAR_AND_VAL$VARIABLE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$VAR_AND_VAL$VALUE_HEADER)
		));
		section1.getDashboardItems().add(dashboardItem01);
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.ORGANIZATION_REVENUE.name()), $I(organization.getAverageRevenue()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.MARKET_CAPITALIZATION.name()), $I(organization.getMarketCapitalizationNumber()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.REVENUE.name()), $I(organization.getRevenue()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.EBIDTA.name()), $I(organization.getEbitda()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.DEBT.name()), $I(organization.getDebt()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.PENSION_DEBT.name()), $I(organization.getPensionDebt()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.GROSS_RISK_BEARING_CAPACITY.name()), $I(organization.getGrossRiskBearingCapacity()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.SYSTEM_NUMBER_OF_REC.name()), dI(system.getNumberOfRecProcessed()).round(0).applyTextAlign("right")));
		for (SystemGeoParameters geoParameter : system.getSystemGeoParameters()) {
			String variableLabel = VariableType.SYSTEM_NUMBER_OF_REC.name() + ", " + (geoParameter.getCountry() != null ? geoParameter.getCountry().getName() : "-");
			if (geoParameter.getState() != null) variableLabel += ", " + geoParameter.getState().getName();
			dashboardItem01.getGridItems().add(Arrays.asList(sI(variableLabel), dI(geoParameter.getNumberOfRecProcessed()).round(0).applyTextAlign("right")));
		}
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.RTO.name()), dI(system.getRto()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.SYSTEM_COST_TO_RESTORE.name()), dI(system.getCostToRestore()).round(0).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI("SYSTEM DATA TYPES"), sI(systemDatatypesString).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI("TECHNOLOGY CATEGORIES"), sI(technologyCategoriesString).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI("TECHNOLOGIES"), sI(technologiesString).applyTextAlign("right")));
		dashboardItem01.getGridItems().add(Arrays.asList(sI(VariableType.PROCESS_REVENUE.name() + " by process").applyColspan(2l)));
		for (Processes process : allProcessList) {
			dashboardItem01.getGridItems().add(Arrays.asList(sI(process.getName()), $I(process.getRevenueProcessed()).round(0).applyTextAlign("right")));
		}

		// Quant Metrics Summary
		DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(1105l, clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$ITEM_NAME));
		section1.getDashboardItems().add(dashboardItem1);
		dashboardItem1.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$METRIC_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$QUANT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$REGULATIONS_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$INDUSTRIES_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$FORMULA_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$CALCULATIONS_HEADER)
		));
		Map<QuantMetrics, ExposureMetricResult> scoringDataMap = systemScoringDataMap.get(system);
		for (Map.Entry<QuantMetrics, ExposureMetricResult> entry : scoringDataMap.entrySet()) {
			ExposureMetricResult exposureMetricResult = entry.getValue();
			QuantMetrics quantMetric = entry.getKey();
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			String quantDetails = quantMetric.getQuant() != null ? quantMetric.getQuant().getName() : "";
			String regulations = quantMetric.getRegulations().stream().map(Regulations::getAcronym).collect(Collectors.joining(", "));
			String industries = quantMetric.getIndustries().stream().map(Industries::getName).collect(Collectors.joining("; "));
			if (quantMetric.getDataTypeClassifications().size() > 0) {
				quantDetails += " (" + StringUtils.join(quantMetric.getDataTypeClassifications().stream().map(DataTypeClassification::getName).collect(Collectors.toList()), ", ") + ")";
			}
			rowItems.add(sI(exposureMetricResult.getMetricName()));
			rowItems.add(sI(quantDetails));
			rowItems.add(sI(regulations));
			rowItems.add(sI(industries));
			rowItems.add(sI(exposureMetricResult.getFormulaBuilder().getFormulaString()));
			if (exposureMetricResult.getFormulaBuilder().checkIsQuantApplicable(system, systemIndustries)) {
				rowItems.add($I(exposureMetricResult.getResult(), "$").round(0));
			} else {
				rowItems.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_APPLICABLE)).applyTextAlign("right"));
			}
			dashboardItem1.getGridItems().add(rowItems);

			// Qual Domain Result
			ExposureMetricResult quantDomainResult = totalQuantValuesMap.get(quantMetric.getQuant());
			if (quantDomainResult == null) {
				quantDomainResult = ExposureMetricResult.of(quantMetric.getQuant().getName(), 0d);
				totalQuantValuesMap.put(quantMetric.getQuant(), quantDomainResult);
			}
			quantDomainResult.setResult(quantDomainResult.getResult() + exposureMetricResult.getResult());

			// totalSystemExposure += exposureMetricResult.getResult();
			// if (exposureMetricResult.getResult() > maxSystemExposure) maxSystemExposure = exposureMetricResult.getResult();
		}
		/*
		dashboardItem1.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$TOTAL_EXPOSURE$ITEM_NAME_STATIC)).applyTextAlign("right").applyHeader(true).applyColspan(3l),
			$I(totalSystemExposure, "$").round(0).applyTextAlign("right").applyBackgroundColor("yellow").applyHeader(true)
		));

		dashboardItem1.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$MAX_EXPOSURE$ITEM_NAME_STATIC)).applyTextAlign("right").applyHeader(true).applyColspan(4l),
			$I(maxSystemExposure, "$").round(0).applyTextAlign("right").applyBackgroundColor("yellow").applyHeader(true)
		));*/

		for (Map.Entry<Quants, ExposureMetricResult> entry : totalQuantValuesMap.entrySet()) {
			ExposureMetricResult quantDomainResult = entry.getValue();

			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(quantDomainResult.getMetricName()));
			rowItems.add($I(quantDomainResult.getResult(), "$").round(0));

			if (entry.getKey().getId().equals(QuantsDomain.CYBER_INSURANCE_NEEDS.getId())) {
				dashboardItem02.getGridItems().add(rowItems);
			} else {
				dashboardItem0.getGridItems().add(rowItems);
				totalSystemExposure += quantDomainResult.getResult();
				if (quantDomainResult.getResult() > maxSystemExposure) maxSystemExposure = quantDomainResult.getResult();
			}
		}

		dashboardItem1.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$MAX_EXPOSURE$ITEM_NAME_STATIC)).applyTextAlign("right").applyHeader(true).applyColspan(5l),
			$I(maxSystemExposure, "$").round(0).applyTextAlign("right").applyBackgroundColor("yellow").applyHeader(true)
		));

		dashboardItem0.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$TOTAL_EXPOSURE$ITEM_NAME), sRound(totalSystemExposure)));
		dashboardItem0.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$TOTAL_EXPOSURE$ITEM_NAME_STATIC)).applyTextAlign("right").applyHeader(true),
			$I(totalSystemExposure, "$").round(0).applyTextAlign("right").applyBackgroundColor("yellow").applyHeader(true)
		));
		dashboardItem0.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$MAX_EXPOSURE$ITEM_NAME_STATIC)).applyTextAlign("right").applyHeader(true),
			$I(maxSystemExposure, "$").round(0).applyTextAlign("right").applyBackgroundColor("yellow").applyHeader(true)
		));

		return dashboard;
	}

	/**
	 * Build Vendor Level Drilldown
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 * @return DashboardDTO
	 */
	public DashboardDTO buildVendorDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {

		Long vendorId = Long.valueOf(drilldown.getParams().get("vendor"));
		Organizations vendor = vendorService.getVendor(vendorId);
		List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganizationAndVendor(organizationService.getCurrentOrganizationId(), vendorId);

		// Apply Dashboard name
		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUANT$ITEM_NAME), vendor.getName()));

		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO(20301L, clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUANT$VENDOR$ITEM_NAME), null);
		dashboard.getSections().add(section);

		// Create Metric Domain Filter
		List<QuantsDomain> quantDomainFilter = null;
		if (drilldown.getParams().containsKey("metricDomain")) {
			Long metricDomainId = Long.valueOf(drilldown.getParams().get("metricDomain"));
			QuantsDomain metricDomain = QuantsDomain.of(metricDomainId);
			quantDomainFilter = Arrays.asList(metricDomain);
		}

		List<Systems> allVendorSystems = new ArrayList<>();
		associateVendors.stream().forEach(associateVendor -> allVendorSystems.addAll(associateVendor.getSystems()));

		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsScoringDataMap = getSystemsScoringData(riskModelId, allVendorSystems, quantDomainFilter);

		// Adding total Quants data
		Double totalSystemExposure = 0D;
		DashboardTableItemDTO dashboardItem0 = new DashboardTableItemDTO(1102L, "");
		dashboardItem0.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUANT$VENDOR$TOTAL_EXPOSURE$METRIC_QUANT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUANT$VENDOR$TOTAL_EXPOSURE$SUMMARY_HEADER)
		));
		section.getDashboardItems().add(dashboardItem0);
		Map<Quants, ExposureMetricResult> totalQuantValuesMap = new HashMap<>();

		DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(1105L, clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUANT$VENDOR$SUMMARY$ITEM_NAME));
		section.getDashboardItems().add(dashboardItem1);
		dashboardItem1.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUANT$SYSTEM_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUANT$NUMBER_OF_RECORDS_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUANT$VENDOR$SUMMARY$METRIC_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUANT$VENDOR$SUMMARY$QUANT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUANT$SYSTEM$SUMMARY$INDUSTRIES_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUANT$VENDOR$SUMMARY$FORMULA_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUANT$VENDOR$SUMMARY$CALCULATION_HEADER)
		));

		for (Systems system: allVendorSystems) {
			Map<QuantMetrics, ExposureMetricResult> scoringDataMap = Optional.ofNullable(systemsScoringDataMap.get(system)).orElse(new HashMap<>());
			for (Map.Entry<QuantMetrics, ExposureMetricResult> entry : scoringDataMap.entrySet()) {
				ExposureMetricResult exposureMetricResult = entry.getValue();
				QuantMetrics quantMetric = entry.getKey();
				String recordsProcessedString = "Total: "  + String.format("%,.0f", system.getNumberOfRecProcessed());
				String geoRecordsProcessedString = system.getSystemGeoParameters().stream()
					.filter(systemGeoParameters -> systemGeoParameters.getNumberOfRecProcessed() != null)
					.map(systemGeoParameters -> {
						String result = systemGeoParameters.getCountry().getName();
						if (systemGeoParameters.getState() != null) {
							result += ", " + systemGeoParameters.getState().getName();
						}
						result += ": " + String.format("%,.0f", systemGeoParameters.getNumberOfRecProcessed());

						return result;
					}).collect(Collectors.joining("<br/>\n"));
				if (StringUtils.isNotEmpty(geoRecordsProcessedString)) {
					recordsProcessedString += "<br/>\n" + geoRecordsProcessedString;
				}

				List<DashboardDataItemDTO> rowItems = new ArrayList<>();
				rowItems.add(sI(system.getName()));
				rowItems.add(sI(recordsProcessedString));
				rowItems.add(sI(exposureMetricResult.getMetricName()));
				rowItems.add(sI(quantMetric.getQuant() != null ? quantMetric.getQuant().getName() : ""));
				rowItems.add(sI(!quantMetric.getIndustries().isEmpty() ? quantMetric.getIndustries().stream().map(Industries::getName).collect(Collectors.joining("; ")) : ""));
				rowItems.add(sI(exposureMetricResult.getFormulaBuilder().getFormulaString()));
				rowItems.add($I(exposureMetricResult.getResult(), "$").round(0));
				dashboardItem1.getGridItems().add(rowItems);

				// Qual Domain Result
				ExposureMetricResult quantDomainResult = totalQuantValuesMap.get(quantMetric.getQuant());
				if (quantDomainResult == null) {
					quantDomainResult = ExposureMetricResult.of(quantMetric.getQuant().getName(), 0d);
					totalQuantValuesMap.put(quantMetric.getQuant(), quantDomainResult);
				}
				quantDomainResult.setResult(quantDomainResult.getResult() + exposureMetricResult.getResult());

				totalSystemExposure += exposureMetricResult.getResult();
			}
		}
		for (Map.Entry<Quants, ExposureMetricResult> entry : totalQuantValuesMap.entrySet()) {
			ExposureMetricResult quantDomainResult = entry.getValue();

			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(quantDomainResult.getMetricName()));
			rowItems.add($I(quantDomainResult.getResult(), "$").round(0));
			dashboardItem0.getGridItems().add(rowItems);
		}
		dashboardItem0.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUANT$VENDOR$TOTAL_EXPOSURE$ITEM_NAME), sRound(totalSystemExposure)));

		return dashboard;
	}

	private Map<Systems, Set<Processes>> getSystemProcessesMap(Organizations organization) {
		Map<Systems, Set<Processes>> allSystemProcessesMap = new HashMap<>();
		List<Processes> allProcessList = processRepository.getListByOrganization(organization.getId());
		for (Processes process : allProcessList) {
			for (Systems system : process.getSystems()) {
				if (!allSystemProcessesMap.containsKey(system)) {
					allSystemProcessesMap.put(system, new HashSet<>());
				}

				allSystemProcessesMap.get(system).add(process);
			}
		}
		return allSystemProcessesMap;
	}

	private Double buildSystemAndProcessValue(Map<Systems, Set<Processes>> allSystemProcessesMap, Map<String, Double> variables, FormulaBuilder formulaBuilder, Systems system, Collection<Industries> systemIndustries) {
		Double systemResult = 0D;

		if (system.getId().equals(1592L) && formulaBuilder.getRegulations().size() > 0) {
			// log.debug("HERE");
		}
		if (formulaBuilder.getQuantMetrics().getId().equals(157L)) {
			// log.debug("HERE");
		}

		// Check is Quant Applicable for the System
		if (!formulaBuilder.checkIsQuantApplicable(system, systemIndustries)) {
			// log.info(String.format("Quant [%s] is NOT applicable by the System [%s, %s]", formulaBuilder.getName(), system.getId(), system.getName()));
			return null;
		}
		if (formulaBuilder.getQuantMetrics().getId().equals(136L) && system.getId().equals(1592L)) {
			// log.debug("HERE");
		}

		if (formulaBuilder.getIsGeoRegulationsApplied()) {
			if (formulaBuilder.getGeoNumberOfRecords().containsKey(system)) {
				variables.put(VariableType.SYSTEM_NUMBER_OF_REC.name(), formulaBuilder.getGeoNumberOfRecords().get(system));
			} else {
				variables.put(VariableType.SYSTEM_NUMBER_OF_REC.name(), 0D);
			}
		}

		if (formulaBuilder.isVariableUsed(VariableType.PROCESS_REVENUE)) {
			Set<Processes> systemProcesses = Optional.ofNullable(allSystemProcessesMap.get(system)).orElse(new HashSet<>());
			// for (Processes process : processRepository.getListBySystem(system.getId())) {
			for (Processes process : systemProcesses) {
				variables.put(VariableType.PROCESS_REVENUE.name(), process.getRevenueProcessed() != null ? process.getRevenueProcessed() : 0D);
				Double formulaResult = formulaBuilder.run(variables);
				systemResult += formulaResult;
			}
		} else {
			systemResult = formulaBuilder.run(variables);
		}
		return systemResult;
	}

	/**
	 * Get Dashboard definitionF
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getETLSystemsDashboardDetails(Long riskModelId) {

		DashboardDTO dashboard = new DashboardDTO(121L, clientMessage.getMessage(SLCT.DASHBOARDS$ETL_SYSTEMS_EXPOSURES$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ETL_SYSTEMS_EXPOSURES$DESCRIPTION), DashboardType.None);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.VENDOR_CYBER_RISK_MANAGER(clientMessage);

		DashboardSectionDTO section1 = new DashboardSectionDTO();

		dashboard.getSections().add(section1);

		// Create breadcrumbs
		section1.setBreadcrumbs(breadcrumbsTop.extend("DATA_SHARING_EXPOSURE", SLCT.DASHBOARDS$ETL_SYSTEMS_EXPOSURES$NAME, "").getBreadcrumbs());

		// TODO 04.06.2020	implement ETL Systems Dashboard

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getIOTExposuresDashboardDetails(Long riskModelId) {

		DashboardDTO dashboard = new DashboardDTO(121L, clientMessage.getMessage(SLCT.DASHBOARDS$IOT_EXPOSURES$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$IOT_EXPOSURES$DESCRIPTION), DashboardType.None);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_CISO(clientMessage);

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<Systems> systemsList = systemRepository.getSystemsListWithTechnologyCategories(riskModel.getOrganizationId(), Arrays.asList(TechnologyCategoryDomain.IOT.getId()));

		List<QuantsDomain> metricsDomains = Arrays.asList(QuantsDomain.DATA_EXFILTRATION, QuantsDomain.BUSINESS_INTERRUPTION, QuantsDomain.GDPR_REGULATORY_EXPOSURE);
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap = getSystemsScoringData(riskModel.getId(), systemsList, metricsDomains);

		boolean isGDPRRegulatoryQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE);

		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO();
		dashboard.getSections().add(section);

		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(121001l, "");
		section.getDashboardItems().add(dashboardItem);

		// Create breadcrumbs
		section.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_IOT_EXPOSURES", SLCT.DASHBOARDS$IOT_EXPOSURES$NAME, "").getBreadcrumbs());

		List<String> headers = new ArrayList<>();
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$IOT_EXPOSURES$SYSTEMS_EXPOSURES$SYSTEM_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$IOT_EXPOSURES$SYSTEMS_EXPOSURES$DATA_EXFILTRATION_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$IOT_EXPOSURES$SYSTEMS_EXPOSURES$BUSINESS_INTERRUPTION_HEADER));
		if (isGDPRRegulatoryQuantDefined) headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$IOT_EXPOSURES$SYSTEMS_EXPOSURES$GDPR_REGULATORY_EXPOSURE_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$IOT_EXPOSURES$SYSTEMS_EXPOSURES$TOTAL_EXPOSURE_HEADER));

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

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getUninsurableExposuresDashboardDetails(Long riskModelId) {

		DashboardDTO dashboard = new DashboardDTO(121L, clientMessage.getMessage(SLCT.DASHBOARDS$UNINSURABLE_EXPOSURES$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$UNINSURABLE_EXPOSURES$DESCRIPTION), DashboardType.None);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_EXECUTIVE(clientMessage);

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<Systems> systemsList = systemRepository.getAllByOrganization(riskModel.getOrganizationId());

		List<QuantsDomain> metricsDomains = Arrays.asList(QuantsDomain.DATA_EXFILTRATION, QuantsDomain.BUSINESS_INTERRUPTION, QuantsDomain.GDPR_REGULATORY_EXPOSURE, QuantsDomain.TOTAL_EXPOSURE);
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap = getSystemsScoringData(riskModel.getId(), systemsList, metricsDomains);

		boolean isGDPRRegulatoryQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE);

		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO();
		dashboard.getSections().add(section);

		// Create breadcrumbs
		section.setBreadcrumbs(breadcrumbsTop.extend("UNINSURABLE_EXPOSURES", SLCT.DASHBOARDS$UNINSURABLE_EXPOSURES$NAME, "").getBreadcrumbs());

		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(121001l, "");
		section.getDashboardItems().add(dashboardItem);

		List<String> headers = new ArrayList<>();
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$UNINSURABLE_EXPOSURES$SYSTEMS_EXPOSURES$SYSTEM_NAME_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$UNINSURABLE_EXPOSURES$SYSTEMS_EXPOSURES$DATA_EXFILTRATION_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUANT$NUMBER_OF_RECORDS_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$UNINSURABLE_EXPOSURES$NUMBER_OF_RECORDS_INSURED));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$UNINSURABLE_EXPOSURES$NUMBER_OF_UNINSURED_RECORDS));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$UNINSURABLE_EXPOSURES$EXPOSURE_OF_UNINSURED_RECORDS));
		dashboardItem.addGridHeaders(headers, true);

		for (Systems system : systemsList) {
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			Double dataExfiltration = 0d;
			Double numberOfRecords = 0d;
			Double totalSystemExposure = 0d;
			Double numberOfRecordsInsured = 0d;
			Double numberOfUninsuredRecords = 0d;
			Double exposureOfUninsuredRecords = 0d;
			Double businessInterruption = 0d;
			Double gdprRegultoryExposure = 0d;
			Map<QuantMetrics, ExposureMetricResult> systemMetricDataMap = systemScoringDataMap.get(system);

			for (Map.Entry<QuantMetrics, ExposureMetricResult> entry : systemMetricDataMap.entrySet()) {
				ExposureMetricResult exposureMetricResult = entry.getValue();
				QuantMetrics quantMetric = entry.getKey();
				if (QuantsDomain.DATA_EXFILTRATION.getId().equals(quantMetric.getQuant().getId())) dataExfiltration += exposureMetricResult.getResult();
				if (QuantsDomain.BUSINESS_INTERRUPTION.getId().equals(quantMetric.getQuant().getId())) businessInterruption += exposureMetricResult.getResult();
				if (QuantsDomain.GDPR_REGULATORY_EXPOSURE.getId().equals(quantMetric.getQuant().getId())) gdprRegultoryExposure += exposureMetricResult.getResult();
				if (QuantsDomain.TOTAL_EXPOSURE.getId().equals(quantMetric.getQuant().getId())) totalSystemExposure += exposureMetricResult.getResult();
			}
			//Map<Systems, Set<Processes>> allSystemProcessesMap = getSystemProcessesMap(organization);

			Double totalExposure = dataExfiltration + businessInterruption + gdprRegultoryExposure;
			ExposureMetricResult exposureMetricResult = new ExposureMetricResult();
			if (totalExposure >= 250000000D) {

				numberOfRecords = (system.getNumberOfRecProcessed());
				numberOfRecordsInsured = (totalSystemExposure/(Organizations.DEFAULT_RECORD_PRICE));
				numberOfUninsuredRecords = system.getNumberOfRecProcessed()-numberOfRecordsInsured;
				exposureOfUninsuredRecords = dataExfiltration - totalSystemExposure;

				rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));
				rowItems.add($I(dataExfiltration).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.DATA_EXFILTRATION)));
				rowItems.add(sI(numberOfRecords).round(0));
				rowItems.add(sI(numberOfRecordsInsured).round(0));
				rowItems.add(sI(numberOfUninsuredRecords).round(0));
				rowItems.add($I(exposureOfUninsuredRecords).round(0));

				dashboardItem.getGridItems().add(rowItems);
			}
		}

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getExposureTypeDashboardDetails(Long riskModelId, QuantsDomain quantsDomain, String header) {

		DashboardDTO dashboard = new DashboardDTO(1000000L + riskModelId, header, null, DashboardType.None);

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<Systems> systemsList = systemRepository.getAllByOrganization(riskModel.getOrganizationId());

		List<QuantsDomain> metricsDomains = Arrays.asList(quantsDomain);
		List<QuantMetrics> quantMetricList = quantMetricsRepository.getListByRiskModelIdAndQuantIds(riskModelId, metricsDomains.stream().mapToLong(QuantsDomain::getId).boxed().collect(Collectors.toUnmodifiableList()));
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap = getSystemsScoringData(riskModel.getId(), systemsList, metricsDomains);

		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO();
		dashboard.getSections().add(section);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD(clientMessage);
		section.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_DATA_EXFILTRATION", SLCT.DASHBOARD$DATA_EXFILTRATION$NAME, "").getBreadcrumbs());

		// Add download button
		DashboardItemDTO downloadButton = buildDownloadButtonDashboardItemDTO(riskModelId, DashboardsConfig.DASHBOARD_EXPOSURE_RISK_REPORT, 45721L);
		section.getDashboardItems().add(downloadButton);

		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(121001l, "");
		section.getDashboardItems().add(dashboardItem);

		List<String> headers = new ArrayList<>();
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_EXPOSURES$QUANT_SCORES$SYSTEM_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$BUSINESS_UNIT_HEADER));
		headers.add(clientMessage.getMessage(quantsDomain.getName()));
		for (int i = 0; i < quantMetricList.size(); i++) {
			QuantMetrics quantMetric = quantMetricList.get(i);
			headers.add(quantMetric.getName());
		}
		dashboardItem.addGridHeaders(headers, true);

		for (Systems system : systemsList) {
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			Double totalExposure = 0d;
			Map<QuantMetrics, ExposureMetricResult> systemMetricDataMap = systemScoringDataMap.get(system);

			DashboardDataItemDTO firstCellInRow = $I(0d).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, quantsDomain));
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));
			rowItems.add(sI(businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true)).applyTextAlign("left"));
			rowItems.add(firstCellInRow);
			if (systemMetricDataMap != null) {
				for (int i = 0; i < quantMetricList.size(); i++) {
					QuantMetrics quantMetric = quantMetricList.get(i);
					ExposureMetricResult exposureMetricResult = systemMetricDataMap.get(quantMetric);
					double exposure = exposureMetricResult != null && exposureMetricResult.getResult() != null ? exposureMetricResult.getResult() : 0d;
					totalExposure += exposure;
					rowItems.add($I(exposure).round(0));
				}
			}
			firstCellInRow.applyValue(totalExposure).round(0);
			dashboardItem.getGridItems().add(rowItems);
		}

		return dashboard;
	}

	/**
	 * Create summary score dashboard for Scoring Types
	 *
	 * @param riskModelId
	 * @param scoringTypes
	 * @return
	 */
	/*
	public DashboardItemDTO createSummaryScoresDashboardItem(Long riskModelId) {
		// Build Organization Summary Scores Dashboard
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(11l, "Summary Scores");
		Map<Systems, Map<MetricDomains, MetricResult>> systemsDataMap = getSystemsScoringData(riskModelId, null);
		List<MetricDomains> metricDomains = metricDomainRepository.findAll();

		List<FormulaBuilder> riskMetricsFormulaBuilders = getRiskScoringMetricsFormulaBuilders(riskModelId);

		List<String> headerList = new ArrayList<>();
		headerList.add("System");
		for (FormulaBuilder formulaBuilder : riskMetricsFormulaBuilders) {
			headerList.add(formulaBuilder.getName());
		}
		for (MetricDomains domain : metricDomains) {
			headerList.add(domain.getName());
		}

		dashboardItem.addGridHeaders(headerList, true);
		for (Map.Entry<Systems, Map<MetricDomains, MetricResult>> entry : systemsDataMap.entrySet()) {
			Systems system = entry.getKey();
			Map<MetricDomains, MetricResult> metricResultMap = entry.getValue();
			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.of(system)));

			// Build Risk Formulas
			for (FormulaBuilder formulaBuilder : riskMetricsFormulaBuilders) {
				Double formulaValue = formulaBuilder.calculate(metricResultMap);
				rowItems.add(sI(formulaValue).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system)));
			}

			// Build Metrics Part
			for (MetricDomains domain : metricDomains) {
				MetricResult metricResult = metricResultMap.get(domain);
				rowItems.add(sI(metricResult.getNormalizedResult()).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, MetricDomain.of(domain.getId()))));
			}
			dashboardItem.getGridItems().add(rowItems);
		}
		return dashboardItem;
	}
	*/

	/**
	 * Get Dashboard Report
	 *
	 * @return Dashboard
	 */
	public ByteArrayOutputStream buildReport(Long riskModelId, QuantsDomain quantsDomain) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		try {

			RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

			List<Systems> systemsList = systemRepository.getAllByOrganization(riskModel.getOrganizationId());
			List<QuantsDomain> metricsDomains = Arrays.asList(quantsDomain);
			List<QuantMetrics> quantMetricList = quantMetricsRepository.getListByRiskModelIdAndQuantIds(riskModelId, metricsDomains.stream().mapToLong(QuantsDomain::getId).boxed().collect(Collectors.toUnmodifiableList()));
			Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringDataMap = getSystemsScoringData(riskModel.getId(), systemsList, metricsDomains);

			// Create Report Headers
			List<String> headers = new ArrayList<>();
			headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$ORGANIZATION$CYBER_EXPOSURES$QUANT_SCORES$SYSTEM_HEADER));
			headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$BUSINESS_UNIT_HEADER));
			headers.add(clientMessage.getMessage(quantsDomain.getName()));
			for (int i = 0; i < quantMetricList.size(); i++) {
				QuantMetrics quantMetric = quantMetricList.get(i);
				headers.add(quantMetric.getName());
			}
			String[] headersArray = new String[headers.size()];
			headers.toArray(headersArray);

			// Create CSV Printer
			Writer writer = new OutputStreamWriter(result);
			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headersArray);
			CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);;

			for (Systems system : systemsList) {
				List<DashboardDataItemDTO> rowItems = new ArrayList<>();
				Double totalExposure = 0d;
				Map<QuantMetrics, ExposureMetricResult> systemMetricDataMap = systemScoringDataMap.get(system);

				DashboardDataItemDTO firstCellInRow = $I(0d).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, quantsDomain));
				rowItems.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, null)));
				rowItems.add(sI(businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true)).applyTextAlign("left"));
				rowItems.add(firstCellInRow);

				firstCellInRow.applyValue(totalExposure).round(0);

				List<Object> itemValues = new ArrayList<>();
				itemValues.add(system.getName());
				itemValues.add(businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true));
				itemValues.add(0D);
				if (systemMetricDataMap != null) {
					for (int i = 0; i < quantMetricList.size(); i++) {
						QuantMetrics quantMetric = quantMetricList.get(i);
						ExposureMetricResult exposureMetricResult = systemMetricDataMap.get(quantMetric);
						double exposure = exposureMetricResult != null && exposureMetricResult.getResult() != null ? exposureMetricResult.getResult() : 0d;
						totalExposure += exposure;
						itemValues.add(exposure);
					}
				}
				itemValues.set(2, totalExposure);
				csvPrinter.printRecord(itemValues);
			}
			csvPrinter.flush();
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}

		return result;
	}

}
