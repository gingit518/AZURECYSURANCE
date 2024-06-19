package com.cyberintech.vrisk.server.model.dao;


import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.CacheMetricsFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.risk_model.CacheMetricsDataDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model.RiskModelViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.CacheMetricsData;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.RiskModelCalculationsService;
import com.cyberintech.vrisk.server.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
/**
 * Cache Metrics DAO Model
 *
 * @author   Oleh Dmytrenko <odmytrenko@dfusiontech.com>
 * @since    2021-12-23
 */
@Service
public class CacheMetricsModelDAO implements PageableModelDAO<CacheMetricsDataDTO, CacheMetricsFilter> {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<CacheMetricsDataDTO> getItemsPageable(CacheMetricsFilter filter, Pageable pageable, BaseSort sort) {
		// Detect filtered values
		String metricName = Optional.ofNullable(filter.getName()).orElse("");
		String systemName = Optional.ofNullable(filter.getSystemName()).orElse("");
		String organizationName = Optional.ofNullable(filter.getOrganizationName()).orElse("");
		String riskModelName = Optional.ofNullable(filter.getRiskModelName()).orElse("");
		String metricType = Optional.ofNullable(filter.getMetricType()).orElse("");
		String metricLevel = Optional.ofNullable(filter.getMetricLevel()).orElse("");

		List<Long> excludeIds = null;
		if (filter != null && filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			excludeIds = filter.getExcludeIds();
		}

		Long organizationId = null;
		if (!userService.isSuperAdmin()) {
			organizationId = organizationService.getCurrentOrganizationId();
		} else {
			if (StringUtils.isNotEmpty(organizationName)) {
				organizationId = organizationRepository.findFirstByNameAndIdIsNotIn(organizationName, Arrays.asList(0L)).get().getId();
			}
		}

		// Define base hql data Query
		String hqlQuery = "SELECT cache FROM CacheMetricsData cache ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(cache) FROM CacheMetricsData cache ";

		// Build Query String
		String whereString = "";
		if (organizationId != null) {
			whereString += " WHERE  cache.organizationId = :organizationId";
		} else {
			whereString += " WHERE 1 = 1 ";
		}
		if (StringUtils.isNotEmpty(metricName)) {
			whereString += " AND UPPER(cache.metricName) LIKE (CONCAT(UPPER(:metricName), '%'))";
		}
		if (excludeIds != null) {
			whereString += " AND cache.id NOT IN :excludeIds";
		}
		if (StringUtils.isNotEmpty(systemName)) {
			whereString += " AND cache.systemName = :systemName";
		}
		if (StringUtils.isNotEmpty(organizationName)) {
			whereString += " AND cache.organizationName = :organizationName";
		}
		if (StringUtils.isNotEmpty(riskModelName)) {
			whereString += " AND cache.riskModelName = :riskModelName";
		}
		if (StringUtils.isNotEmpty(metricType)) {
			whereString += " AND cache.metricType = :metricType";
		}
		if (StringUtils.isNotEmpty(metricLevel)) {
			whereString += " AND cache.metricLevel = :metricLevel";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "cache.id"),
			Map.entry("name", "cache.metricName"),
			Map.entry("organization", "cache.organizationName"),
			Map.entry("riskModelName", "cache.riskModelName"),
			Map.entry("systemName", "cache.systemName"),
			Map.entry("metricDomainName", "cache.metricDomainName"),
			Map.entry("metricType", "cache.metricType"),
			Map.entry("metricLevel", "cache.metricLevel"),
			Map.entry("metricValue", "cache.metricValue")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<CacheMetricsData> typedQuery = entityManager.createQuery(searchQueryString, CacheMetricsData.class);
		applySearchFilterValues(metricName, systemName, organizationName, riskModelName, metricType, metricLevel, excludeIds, organizationId, typedQuery);

		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<CacheMetricsDataDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), CacheMetricsDataDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(metricName, systemName, organizationName, riskModelName, metricType, metricLevel, excludeIds, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();
		return new PagedResult<CacheMetricsDataDTO>( resultList, resultsCount);
	}


	/**
	 * Apply query data
	 *
	 * @param metricName
	// * @param organizationName
	 * @param riskModelName
	 * @param metricType
	 * @param metricLevel
	 * @param excludeIds
	 * @param organizationId
	 * @param query
	 */
	private void applySearchFilterValues(String metricName, String systemName, String organizationName, String riskModelName, String metricType, String metricLevel, List<Long> excludeIds, Long organizationId, Query query) {
		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (StringUtils.isNotEmpty(metricName)) query.setParameter("metricName", metricName);
		if (excludeIds != null) query.setParameter("excludeIds", excludeIds);
		if (StringUtils.isNotEmpty(systemName)) query.setParameter("systemName", systemName);
		if (StringUtils.isNotEmpty(organizationName)) query.setParameter("organizationName", organizationName);
		if (StringUtils.isNotEmpty(riskModelName)) query.setParameter("riskModelName", riskModelName);
		if (StringUtils.isNotEmpty(metricType)) query.setParameter("metricType", metricType);
		if (StringUtils.isNotEmpty(metricLevel)) query.setParameter("metricLevel", metricLevel);
	}
}
