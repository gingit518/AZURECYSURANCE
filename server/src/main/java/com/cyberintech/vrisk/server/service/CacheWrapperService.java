package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.config.CacheNode;
import com.cyberintech.vrisk.server.model.jpa.entity.QuantMetrics;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelRepository;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.service.dashboards.ExposureMetricResult;
import com.cyberintech.vrisk.server.service.dashboards.ExposureMetricsDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache Storage Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-01
 */
@Service
public class CacheWrapperService {

	private static final String CACHE_KEY_PREFIX = "CACHE_KEY_";

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private ExposureMetricsDashboardService exposureMetricsDashboardService;

	public static String getCacheKey(String key){
		return CACHE_KEY_PREFIX + key;
	}

	@Cacheable(cacheNames = CacheNode.DASHBOARD_SEARCH, key = "T(com.cyberintech.vrisk.server.service.CacheWrapperService).getCacheKey(#key)")
	public String getSearchConfigStringFromCache(String key) {
		return null;
	}

	@CachePut(cacheNames = CacheNode.DASHBOARD_SEARCH, key = "T(com.cyberintech.vrisk.server.service.CacheWrapperService).getCacheKey(#key)")
	public String putSearchConfigStringToCache(String key, String value) {
		return value;
	}

	@CacheEvict(cacheNames = CacheNode.DASHBOARD_SEARCH, key = "T(com.cyberintech.vrisk.server.service.CacheWrapperService).getCacheKey(#key)")
	public void removeSearchConfigStringFromCache(String relevant) {
	}

	/**
	 * Find Map of Quant Metrics
	 *
	 * @return Map of Quant Metrics
	 */
	@Cacheable(cacheNames = "leagueSportPricesForYearMap", cacheManager = "inMemoryCacheManager")
	@Transactional
	public Map<QuantMetrics, ExposureMetricResult> getOrganizationScoringDataMap(Long riskModelId) {
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

		Map<QuantMetrics, ExposureMetricResult> organizationScoringDataMap = exposureMetricsDashboardService.getOrganizationCumulativeScoringData(riskModel, null);

		return organizationScoringDataMap;
	}

	/**
	 * Find Map of Quant Metrics
	 *
	 * @return Map of Quant Metrics
	 */
	@Cacheable(cacheNames = "leagueSportPricesForYearMap", cacheManager = "inMemoryCacheManager")
	@Transactional
	public Map<Long, ExposureMetricResult> getRiskModelExposureScoringByIdDataMap(Long riskModelId) {
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

		Map<QuantMetrics, ExposureMetricResult> organizationScoringDataMap = exposureMetricsDashboardService.getOrganizationCumulativeScoringData(riskModel, null);

		Map<Long, ExposureMetricResult> result = new HashMap<>();
		for (Map.Entry<QuantMetrics, ExposureMetricResult>  entry : organizationScoringDataMap.entrySet()) {
			result.put(entry.getKey().getId(), entry.getValue());
		}

		return result;
	}
}
