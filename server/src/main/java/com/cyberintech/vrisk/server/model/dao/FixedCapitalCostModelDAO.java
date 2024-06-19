package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.budget.FixedCapitalCostDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.FixedCapitalCosts;
import com.cyberintech.vrisk.server.service.OrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
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
 * Fixed Capital Cost DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-07-17
 */
@Service
public class FixedCapitalCostModelDAO implements PageableModelDAO<FixedCapitalCostDTO, NameFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<FixedCapitalCostDTO> getItemsPageable(NameFilter filter, Pageable pageable, BaseSort sort) {

		// Detect filtered values
		Long organizationId = organizationService.getCurrentOrganizationId();
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
	//	String hqlQuery = "SELECT itm FROM FixedCapitalCosts itm LEFT JOIN FETCH itm.vendor v LEFT JOIN itm.cybersecurityTol cst " +
	//		"LEFT JOIN FETCH itm.businessUnit bm LEFT JOIN FETCH itm.licenseType lt ";

		@Language("HQL")
		String hqlQuery = "SELECT itm FROM FixedCapitalCosts itm LEFT JOIN FETCH itm.vendor v LEFT JOIN itm.technology tch " +
			"LEFT JOIN FETCH itm.businessUnit bm LEFT JOIN FETCH itm.licenseType lt ";

		@Language("HQL")
		// Define base count hql Query
		String hqlQueryCount = "SELECT count(itm) FROM FixedCapitalCosts itm ";

		// Build Query String
		String whereString = " WHERE itm.organizationId = :organizationId";
		if (StringUtils.isNotEmpty(nameFilter)) {
			whereString += " AND (UPPER(tch.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(v.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(lt.name) LIKE CONCAT('%', UPPER(:name), '%'))";
			hqlQueryCount += " LEFT JOIN itm.vendor v LEFT JOIN itm.technology tch LEFT JOIN itm.licenseType lt ";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND itm.id NOT IN :excludeIds";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "itm.id"),
			Map.entry("costDate", "itm.costDate"),
			Map.entry("name", "bm.name"),
			Map.entry("vendor", "v.name"),
			Map.entry("licenseType", "lt.name"),
			Map.entry("licenseCost", "itm.licenseCost"),
			Map.entry("totalCosts", "itm.totalCosts"),
			Map.entry("percentOfBudget", "itm.percentOfBudget"),
			Map.entry("businessUnit", "bm.name"),
	//		Map.entry("cybersecurityTool", "cst.name"),
			Map.entry("technology", "tch.name"),
			Map.entry("securityToolName", "itm.securityToolName")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<FixedCapitalCosts> typedQuery = entityManager.createQuery(searchQueryString, FixedCapitalCosts.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<FixedCapitalCostDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), FixedCapitalCostDTO.class);

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
