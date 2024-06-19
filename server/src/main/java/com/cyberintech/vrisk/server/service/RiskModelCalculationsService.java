package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.CacheMetricsModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.*;
import com.cyberintech.vrisk.server.model.dto.risk_model.CacheMetricsDataDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantMetricLevel;
import com.cyberintech.vrisk.server.model.jpa.domains.VariableType;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.AssociateVendorRepository;
import com.cyberintech.vrisk.server.repository.jpa.CacheMetricsDataRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskMetricsRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelRepository;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.dashboards.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Risk Model management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-27
 */
@Service
@Slf4j
public class RiskModelCalculationsService {

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private CacheMetricsDataRepository cacheMetricsDataRepository;

	@Autowired
	private ExposureMetricsDashboardService exposureMetricsDashboardService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private RiskMetricsRepository riskMetricsRepository;

	@Autowired
	private ScoringQuestionsDashboardService scoringQuestionsDashboardService;

	@Autowired
	private CacheMetricsModelDAO cacheMetricsModelDAO;


	/**
	 * Get Cache Metrics Data List
	 *
	 * @return Cache Metrics List
	 */
	public FilteredResponse<CacheMetricsFilter, CacheMetricsDataDTO> getCacheMetricsListFiltered(FilteredRequest<CacheMetricsFilter> filteredRequest) {

		PagedResult<CacheMetricsDataDTO> result = cacheMetricsModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<CacheMetricsFilter, CacheMetricsDataDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Apply query data
	 *
	 * @param metricName
	 * @param organizationName
	 * @param riskModelName
	 * @param metricType
	 * @param metricLevel
	 * @param excludeIds
	 * @param organizationId
	 * @param query
	 */
	private void applySearchFilterValues(String metricName, String systemName, String organizationName, String riskModelName, String metricType, String metricLevel, List<Long> excludeIds, Long organizationId, Query query) {
		query.setParameter("organizationId", organizationId);
		if (StringUtils.isNotEmpty(metricName)) query.setParameter("metricName", metricName);
		if (excludeIds != null) query.setParameter("excludeIds", excludeIds);
		if (systemName != null) query.setParameter("systemName", systemName);
		if (organizationName != null) query.setParameter("organizationName", organizationName);
		if (riskModelName != null) query.setParameter("riskModelName", riskModelName);
		if (metricType != null) query.setParameter("metricType", metricType);
		if (metricLevel != null) query.setParameter("metricLevel", metricLevel);
	}


	/**
	 * Get content for Download
	 */
	public ByteArrayInputStream getDownloadData() {

		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			Writer writer = new OutputStreamWriter(outputStream);
			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
				"organization_id"
				, "organization_name"
				, "risk_model_id"
				, "risk_model_name"
				, "system_id"
				, "system_name"
				, "system_number_of_records"
				, "data_asset_classification_name"
				, "business_unit_id"
				, "business_unit_name"
				, "process_id"
				, "metric_value"
				, "metric_name"
				, "metric_formula"
				, "metric_type"
				, "metric_level"
				, "metric_id"
				, "metric_domain_id"
				, "metric_domain_name"
				, "created_at"
				, "vendor_id"
				, "vendor_name"
				, "is_cloud_vendor"
				, "is_technology_vendor"
				, "is_system_vendor"
				, "is_service_vendor"
				, "data_class_ids"
				, "data_class_names"
				, "regulations"
			);
			CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

			List<CacheMetricsData> items = cacheMetricsDataRepository.getByOrganizationId(organizationService.getCurrentOrganizationId());
			for (CacheMetricsData item : items) {
				csvPrinter.printRecord(
					item.getOrganizationId()
					, item.getOrganizationName()
					, item.getRiskModelId()
					, item.getRiskModelName()
					, item.getSystemId()
					, item.getSystemName()
					, item.getSystemNumberOfRecords()
					, item.getDataAssetClassificationName()
					, item.getBusinessUnitId()
					, item.getBusinessUnitName()
					, item.getProcessId()
					, item.getMetricValue()
					, item.getMetricName()
					, item.getMetricFormula()
					, item.getMetricType()
					, item.getMetricLevel()
					, item.getMetricId()
					, item.getMetricDomainId()
					, item.getMetricDomainName()
					, item.getCreatedAt()
					, item.getVendorId()
					, item.getVendorName()
					, item.getIsCloudVendor()
					, item.getIsTechnologyVendor()
					, item.getIsSystemVendor()
					, item.getIsServiceVendor()
					, item.getDataClassIds()
					, item.getDataClassNames()
					, item.getRegulations()
				);
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
			throw new InternalServerErrorException("Failed to generate CSV file for Cache Metrics Data");
		}

		return byteArrayInputStream;
	}

	/**
	 * Rebuild metrics cache
	 *
	 * @return Risk Model Details
	 */
	@Transactional
	public Boolean rebuildMetricsCache(Long riskModelId) {
		// Build basic data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationService.getOrganization(riskModel.getOrganizationId());

		// Removing all cached data for rick model
		cacheMetricsDataRepository.deleteByRiskModelId(riskModelId);

		// Associate Vendors Map
		List<AssociateVendors> allVendorsList = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		Map<Systems, List<Organizations>> systemVendors = new HashMap<>();
		Map<Organizations, List<Systems>> vendorSystems = new HashMap<>();
		for (AssociateVendors associateVendor : allVendorsList) {
			if (associateVendor.getSystems() != null && associateVendor.getVendor() != null) {
				for (Systems system : associateVendor.getSystems()) {
					List<Organizations> associateVendorsList = systemVendors.get(system);
					if (associateVendorsList == null) {
						associateVendorsList = new ArrayList<>();
						systemVendors.put(system, associateVendorsList);
					}
					associateVendorsList.add(associateVendor.getVendor());

					List<Systems> associateSystemsList = vendorSystems.get(associateVendor.getVendor());
					if (associateSystemsList == null) {
						associateSystemsList = new ArrayList<>();
						vendorSystems.put(associateVendor.getVendor(), associateSystemsList);
					}
					associateSystemsList.add(system);
				}
			}
		}

		// Rebuild Organization Scoring for Exposure Metrics
		/*
		if (riskModelId.equals(110L)) {
			log.info("here");
		}
		*/
		Map<QuantMetrics, ExposureMetricResult> organizationScoringDataMap = exposureMetricsDashboardService.getOrganizationScoringData(riskModel, null);
		for (Map.Entry<QuantMetrics, ExposureMetricResult> quantScoringEntry : organizationScoringDataMap.entrySet()) {
			QuantMetrics quantMetric = quantScoringEntry.getKey();
			ExposureMetricResult metricResult = quantScoringEntry.getValue();
			String regulations = quantMetric.getRegulations().stream().map(Regulations::getAcronym).collect(Collectors.joining(","));

			CacheMetricsData cacheMetricsData = buildCacheMetricsData4System(riskModel, organization, null, null);
			cacheMetricsData.setMetricId(quantMetric.getId());
			cacheMetricsData.setMetricName(quantMetric.getName());
			cacheMetricsData.setMetricFormula(metricResult.getFormulaBuilder().getFormulaString());
			cacheMetricsData.setMetricType("QUANT");
			cacheMetricsData.setMetricLevel(QuantMetricLevel.ORGANIZATION.name());
			if (quantMetric.getQuant() != null) {
				cacheMetricsData.setMetricDomainId(quantMetric.getQuant().getId());
				cacheMetricsData.setMetricDomainName(quantMetric.getQuant().getName());
			}
			cacheMetricsData.setMetricValue(metricResult.getResult());
			if (StringUtils.isNotEmpty(regulations)) cacheMetricsData.setRegulations(regulations);
			cacheMetricsData.setCreatedAt(new Date());

			cacheMetricsDataRepository.save(cacheMetricsData);
		}

		// Rebuild System Scoring for Exposure Metrics
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsScoringDataMap = exposureMetricsDashboardService.getSystemsScoringData(riskModelId);
		Map<Organizations, Map<QuantMetrics, ExposureMetricResult>> vendorScoringDataMap = new HashMap<>();
		for (Map.Entry<Systems, Map<QuantMetrics, ExposureMetricResult>> systemsScoring: systemsScoringDataMap.entrySet()) {
			Systems system = systemsScoring.getKey();
			Map<QuantMetrics, ExposureMetricResult> scoringData = systemsScoring.getValue();
			for (Map.Entry<QuantMetrics, ExposureMetricResult> quantScoringEntry : scoringData.entrySet()) {
				QuantMetrics quantMetric = quantScoringEntry.getKey();
				ExposureMetricResult metricResult = quantScoringEntry.getValue();
				String regulations = quantMetric.getRegulations().stream().map(Regulations::getAcronym).collect(Collectors.joining(","));

				CacheMetricsData cacheMetricsData = buildCacheMetricsData4System(riskModel, organization, system, null);
				cacheMetricsData.setMetricId(quantMetric.getId());
				cacheMetricsData.setMetricName(quantMetric.getName());
				cacheMetricsData.setMetricFormula(metricResult.getFormulaBuilder().getFormulaString());
				cacheMetricsData.setMetricType("QUANT");
				cacheMetricsData.setMetricLevel(QuantMetricLevel.SYSTEM.name());
				if (quantMetric.getQuant() != null) {
					cacheMetricsData.setMetricDomainId(quantMetric.getQuant().getId());
					cacheMetricsData.setMetricDomainName(quantMetric.getQuant().getName());
				}
				cacheMetricsData.setMetricValue(metricResult.getResult());
				if (StringUtils.isNotEmpty(regulations)) cacheMetricsData.setRegulations(regulations);
				cacheMetricsData.setCreatedAt(new Date());

				cacheMetricsDataRepository.save(cacheMetricsData);

				// Calculate system vendors data
				if (systemVendors.containsKey(system)) {
					for (Organizations vendor : systemVendors.get(system)) {
						ExposureMetricResult vendorMetricResult;
						if (!vendorScoringDataMap.containsKey(vendor)) {
							vendorScoringDataMap.put(vendor, new HashMap<>());
						}
						if (!vendorScoringDataMap.get(vendor).containsKey(quantMetric)) {
							vendorMetricResult = ExposureMetricResult.of(metricResult.getMetricName(), 0d, metricResult.getFormulaBuilder());
							vendorScoringDataMap.get(vendor).put(quantMetric, vendorMetricResult);
						} else {
							vendorMetricResult = vendorScoringDataMap.get(vendor).get(quantMetric);
						}

						vendorMetricResult.setResult(vendorMetricResult.getResult() + metricResult.getResult());
					}
				}
			}
		}

		// Rebuild System Scoring for Exposure Metrics
		for (Map.Entry<Organizations, Map<QuantMetrics, ExposureMetricResult>> vendorsScoring: vendorScoringDataMap.entrySet()) {
			Organizations vendor = vendorsScoring.getKey();
			Map<QuantMetrics, ExposureMetricResult> scoringData = vendorsScoring.getValue();
			for (Map.Entry<QuantMetrics, ExposureMetricResult> quantScoringEntry : scoringData.entrySet()) {
				QuantMetrics quantMetric = quantScoringEntry.getKey();
				ExposureMetricResult metricResult = quantScoringEntry.getValue();
				String regulations = quantMetric.getRegulations().stream().map(Regulations::getAcronym).collect(Collectors.joining(","));

				CacheMetricsData cacheMetricsData = buildCacheMetricsData4System(riskModel, organization, null, vendorSystems.get(vendor));
				cacheMetricsData.setVendorId(vendor.getId());
				cacheMetricsData.setVendorName(vendor.getName());
				cacheMetricsData.setIsCloudVendor(vendor.getIsCloudVendor());
				cacheMetricsData.setIsTechnologyVendor(vendor.getIsTechnologyVendor());
				cacheMetricsData.setIsSystemVendor(vendor.getIsSystemVendor());
				cacheMetricsData.setIsServiceVendor(vendor.getIsServiceVendor());
				cacheMetricsData.setMetricId(quantMetric.getId());
				cacheMetricsData.setMetricName(quantMetric.getName());
				cacheMetricsData.setMetricFormula(metricResult.getFormulaBuilder().getFormulaString());
				cacheMetricsData.setMetricType("QUANT");
				cacheMetricsData.setMetricLevel("VENDOR");
				if (quantMetric.getQuant() != null) {
					cacheMetricsData.setMetricDomainId(quantMetric.getQuant().getId());
					cacheMetricsData.setMetricDomainName(quantMetric.getQuant().getName());
				}
				cacheMetricsData.setMetricValue(metricResult.getResult());
				if (StringUtils.isNotEmpty(regulations)) cacheMetricsData.setRegulations(regulations);
				cacheMetricsData.setCreatedAt(new Date());

				cacheMetricsDataRepository.save(cacheMetricsData);
			}
		}

		// Rebuild Organization Scoring for Qualitative Metrics
		Map<MetricDomains, MetricResult> organizationScoringQualDataMap = scoringQuestionsDashboardService.getOrganizationScoringData(riskModelId, Arrays.asList(VendorType.Organization));
		for (Map.Entry<MetricDomains, MetricResult> qualScoringEntry : organizationScoringQualDataMap.entrySet()) {
			MetricDomains qualMetric = qualScoringEntry.getKey();
			MetricResult metricResult = qualScoringEntry.getValue();

			// We should not insert empty metrics
			if (CollectionUtils.isEmpty(metricResult.getQuestionAnswers())) {
				continue;
			}

			CacheMetricsData cacheMetricsData = buildCacheMetricsData4System(riskModel, organization, null, null);
			cacheMetricsData.setMetricId(qualMetric.getId());
			cacheMetricsData.setMetricName(metricResult.getMetricName());
			cacheMetricsData.setMetricDomainId(qualMetric.getId());
			cacheMetricsData.setMetricDomainName(qualMetric.getName());
			cacheMetricsData.setMetricFormula(metricResult.getFormulaString());
			cacheMetricsData.setMetricType("QUAL");
			cacheMetricsData.setMetricLevel(QuantMetricLevel.ORGANIZATION.name());
			cacheMetricsData.setMetricValue(metricResult.buildNormalizedResult());
			cacheMetricsData.setCreatedAt(new Date());

			cacheMetricsDataRepository.save(cacheMetricsData);
		}

		// Rebuild System Scoring for Qualitative Metrics
		Map<Systems, Map<MetricDomains, MetricResult>> systemScoringQualDataMap = scoringQuestionsDashboardService.getSystemsScoringData(riskModelId, Arrays.asList(VendorType.System), null);
		for (Map.Entry<Systems, Map<MetricDomains, MetricResult>> systemsScoring: systemScoringQualDataMap.entrySet()) {
			Systems system = systemsScoring.getKey();
			Map<MetricDomains, MetricResult> scoringData = systemsScoring.getValue();
			for (Map.Entry<MetricDomains, MetricResult> qualScoringEntry : scoringData.entrySet()) {
				MetricDomains qualMetric = qualScoringEntry.getKey();
				MetricResult metricResult = qualScoringEntry.getValue();

				// We should not insert empty metrics
				if (CollectionUtils.isEmpty(metricResult.getQuestionAnswers())) {
					continue;
				}

				CacheMetricsData cacheMetricsData = buildCacheMetricsData4System(riskModel, organization, system, null);
				cacheMetricsData.setMetricId(qualMetric.getId());
				cacheMetricsData.setMetricName(metricResult.getMetricName());
				cacheMetricsData.setMetricDomainId(qualMetric.getId());
				cacheMetricsData.setMetricDomainName(qualMetric.getName());
				cacheMetricsData.setMetricFormula(metricResult.getFormulaString());
				cacheMetricsData.setMetricType("QUAL");
				cacheMetricsData.setMetricLevel(QuantMetricLevel.SYSTEM.name());
				cacheMetricsData.setMetricValue(metricResult.buildNormalizedResult());
				cacheMetricsData.setCreatedAt(new Date());

				cacheMetricsDataRepository.save(cacheMetricsData);
			}
		}

		// Mitigate Risk Data
		Map<Systems, MetricResult> systemMitigateRiskData = scoringQuestionsDashboardService.getSystemsMitigateData(riskModelId);
		for (Map.Entry<Systems, MetricResult> systemsScoring: systemMitigateRiskData.entrySet()) {
			Systems system = systemsScoring.getKey();
			MetricResult metricResult = systemsScoring.getValue();
			if (metricResult != null && metricResult.getResult() > 0) {
				CacheMetricsData cacheMetricsData = buildCacheMetricsData4System(riskModel, organization, system, null);
				cacheMetricsData.setMetricId(-1l);
				cacheMetricsData.setMetricName(VariableType.MITIGATION_RISK.name());
				cacheMetricsData.setMetricDomainId(-1l);
				cacheMetricsData.setMetricDomainName(VariableType.MITIGATION_RISK.name());
				cacheMetricsData.setMetricFormula(VariableType.MITIGATION_RISK.name());
				cacheMetricsData.setMetricType(VariableType.MITIGATION_RISK.name());
				cacheMetricsData.setMetricLevel(QuantMetricLevel.SYSTEM.name());
				cacheMetricsData.setMetricValue(metricResult.getResult());
				cacheMetricsData.setCreatedAt(new Date());

				cacheMetricsDataRepository.save(cacheMetricsData);
			}
		}

		// Rebuild Risk Metrics System Scoring (for Qualitative Metrics)
		List<RiskMetrics> riskMetricList = riskMetricsRepository.getListByRiskModelId(riskModelId);
		Map<Systems, Map<FormulaBuilder, FormulaResult>> riskMetricsDataMap = scoringQuestionsDashboardService.getRiskMetricsDataMap(riskModelId, riskMetricList);
		for (Map.Entry<Systems, Map<FormulaBuilder, FormulaResult>> systemsScoring: riskMetricsDataMap.entrySet()) {
			Systems system = systemsScoring.getKey();
			Map<FormulaBuilder, FormulaResult> scoringData = systemsScoring.getValue();
			for (Map.Entry<FormulaBuilder, FormulaResult> riskMetricsEntry : scoringData.entrySet()) {
				RiskMetrics riskMetric = riskMetricsEntry.getKey().getRiskMetric();
				FormulaResult metricResult = riskMetricsEntry.getValue();

				CacheMetricsData cacheMetricsData = buildCacheMetricsData4System(riskModel, organization, system, null);
				cacheMetricsData.setMetricId(riskMetric.getId());
				cacheMetricsData.setMetricName(riskMetric.getName());
				cacheMetricsData.setMetricDomainId(riskMetric.getId());
				cacheMetricsData.setMetricDomainName(riskMetric.getName());
				cacheMetricsData.setMetricFormula(metricResult.getFormula());
				if (BooleanUtils.isTrue(riskMetric.getIsResidual())) {
					cacheMetricsData.setMetricType("RESIDUAL_RISK_METRIC");
				} else {
					cacheMetricsData.setMetricType("RISK_METRIC");
				}
				cacheMetricsData.setMetricLevel(QuantMetricLevel.SYSTEM.name());
				cacheMetricsData.setMetricValue(metricResult.getResult());
				cacheMetricsData.setCreatedAt(new Date());

				cacheMetricsDataRepository.save(cacheMetricsData);
			}
		}

		return true;
	}

	/**
	 * Rebuild full metrics cache
	 *
	 * @return Risk Model Details
	 */
	@Transactional
	public void rebuildAllMetricsCache() {
		List<RiskModels> allRiskModels = riskModelRepository.findAll();

		log.info("## Rebuilding metrics cache for all risk models");
		for (RiskModels riskModels : allRiskModels) {
			log.info(String.format("#### Rebuilding cache for Risk model [%s, %s]", riskModels.getId(), riskModels.getName()));
			rebuildMetricsCache(riskModels.getId());
		}
	}

	@Async
	@Transactional
	public void rebuildAllMetricsCacheAsync() {
		rebuildAllMetricsCache();
	}

	private CacheMetricsData buildCacheMetricsData4System(RiskModels riskModel, Organizations organization, Systems system, List<Systems> systems) {
		CacheMetricsData cacheMetricsData = new CacheMetricsData();
		cacheMetricsData.setOrganizationId(organization.getId());
		cacheMetricsData.setOrganizationName(organization.getName());
		cacheMetricsData.setRiskModelId(riskModel.getId());
		cacheMetricsData.setRiskModelName(riskModel.getName());
		if (system != null) {
			cacheMetricsData.setSystemId(system.getId());
			cacheMetricsData.setSystemName(system.getName());
			if (system.getNumberOfRecProcessed() != null) {
				cacheMetricsData.setSystemNumberOfRecords(system.getNumberOfRecProcessed().longValue());
			}
			if (system.getDataAssetClassification() != null) {
				cacheMetricsData.setDataAssetClassificationName(system.getDataAssetClassification().getName());
			}
			if (CollectionUtils.isNotEmpty(system.getDataTypeClassifications())) {
				String ids = system.getDataTypeClassifications().stream().map(dataTypeClassification -> dataTypeClassification.getId().toString()).collect(Collectors.joining(";"));
				String names = system.getDataTypeClassifications().stream().map(dataTypeClassification -> dataTypeClassification.getName()).collect(Collectors.joining(";"));
				cacheMetricsData.setDataClassIds(ids);
				cacheMetricsData.setDataClassNames(names);
			}
			if (system.getBusinessUnit() != null) {
				String businessUnitPath = businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true, " \\ ");
				cacheMetricsData.setBusinessUnitId(system.getBusinessUnit().getId());
				// cacheMetricsData.setBusinessUnitName(system.getBusinessUnit().getName());
				cacheMetricsData.setBusinessUnitName(businessUnitPath);
			}
		} else if (CollectionUtils.isNotEmpty(systems)) {
			Set<DataTypeClassification> allDataClasses = new HashSet<>();
			systems.stream().forEach(systemsItem -> allDataClasses.addAll(systemsItem.getDataTypeClassifications()));
			if (CollectionUtils.isNotEmpty(allDataClasses)) {
				String ids = allDataClasses.stream().map(dataTypeClassification -> dataTypeClassification.getId().toString()).collect(Collectors.joining(";"));
				String names = allDataClasses.stream().map(dataTypeClassification -> dataTypeClassification.getName()).collect(Collectors.joining(";"));
				cacheMetricsData.setDataClassIds(ids);
				cacheMetricsData.setDataClassNames(names);
			}
		}
		return cacheMetricsData;
	}

	/**
	 * Get Cache Metric DTO details
	 *
	 * @return Cache Metric Details
	 */
	public CacheMetricsDataDTO getDetails(Long itemId) {
		CacheMetricsData itemDetails;
		try {
			itemDetails = cacheMetricsDataRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Cache Metric not found in the database [{0}]", itemId));
		}
		CacheMetricsDataDTO result = new CacheMetricsDataDTO(itemDetails);
		return  result;
	}
}
