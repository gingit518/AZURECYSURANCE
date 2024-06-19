package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.budget.VariableCostDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.VariableCosts;
import com.cyberintech.vrisk.server.service.OrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Variable Cost DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-07-17
 */
@Service
public class VariableCostModelDAO implements PageableModelDAO<VariableCostDTO, NameFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<VariableCostDTO> getItemsPageable(NameFilter filter, Pageable pageable, BaseSort sort) {

		// Detect filtered values
		Long organizationId = organizationService.getCurrentOrganizationId();
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		String hqlQuery = "SELECT itm FROM VariableCosts itm LEFT JOIN FETCH itm.system s LEFT JOIN FETCH itm.costType ct ";

		// Define base count hql Query
		String hqlQueryCount = "SELECT count(itm) FROM VariableCosts itm LEFT JOIN itm.system s LEFT JOIN itm.costType ct ";

		// Build Query String
		String whereString = " WHERE itm.organizationId = :organizationId";
		if (StringUtils.isNotEmpty(nameFilter)) {
			whereString += " AND (UPPER(s.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(s.description) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(ct.name) LIKE CONCAT('%', UPPER(:name), '%')) ";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND itm.id NOT IN :excludeIds";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "itm.id"),
			Map.entry("costDate", "itm.costDate"),
			Map.entry("equipmentCost", "itm.equipmentCost"),
			Map.entry("personnelCost", "itm.personnelCost"),
			Map.entry("percentOfTime", "itm.percentOfTime"),
			Map.entry("totalCosts", "itm.totalCosts"),
			Map.entry("costType", "ct.name"),
			Map.entry("system", "s.name"),
			Map.entry("name", "s.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<VariableCosts> typedQuery = entityManager.createQuery(searchQueryString, VariableCosts.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<VariableCostDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), VariableCostDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<>(resultList, resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param organizationId
	 * @param query
	 */
	private void applySearchFilterValues(NameFilter filter, Long organizationId, Query query) {
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
	}
}
