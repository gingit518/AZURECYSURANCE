package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.budget.FixedOperationalCostDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.FixedOperationalCosts;
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
 * Fixed Operational Cost DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-07-17
 */
@Service
public class FixedOperationalCostModelDAO implements PageableModelDAO<FixedOperationalCostDTO, NameFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;


	@Override
	public PagedResult<FixedOperationalCostDTO> getItemsPageable(NameFilter filter, Pageable pageable, BaseSort sort) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		String hqlQuery = "SELECT itm FROM FixedOperationalCosts itm LEFT JOIN FETCH itm.user u LEFT JOIN FETCH itm.task t LEFT JOIN FETCH itm.cyberRole cro " +
			"LEFT JOIN FETCH itm.businessUnit bm LEFT JOIN FETCH itm.rateType rt ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(itm) FROM FixedOperationalCosts itm LEFT JOIN itm.businessUnit bm ";

		// Build Query String
		String whereString = " WHERE itm.organizationId = :organizationId ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(bm.name) LIKE (CONCAT(UPPER(:name), '%')) ";
			whereString += " AND (UPPER(u.fullName) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(cro.name) LIKE CONCAT('%', UPPER(:name), '%'))";
			hqlQueryCount += " LEFT JOIN itm.user u LEFT JOIN itm.cyberRole cro ";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND itm.id NOT IN :excludeIds ";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "itm.id"),
			Map.entry("costDate", "itm.costDate"),
			Map.entry("user", "itm.id"),
			Map.entry("role", "cro.name"),
			Map.entry("cyberRole", "cro.name"),
			Map.entry("rateType", "rt.name"),
			Map.entry("businessUnit", "bm.name"),
			Map.entry("percentOfBudget", "itm.percentOfBudget"),
			Map.entry("percentOfTime", "itm.percentOfTime"),
			Map.entry("task", "t.name"),
			Map.entry("totalCosts", "itm.totalCosts"),
			Map.entry("name", "bm.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<FixedOperationalCosts> typedQuery = entityManager.createQuery(searchQueryString, FixedOperationalCosts.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<FixedOperationalCostDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), FixedOperationalCostDTO.class);

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

		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
	}
}
