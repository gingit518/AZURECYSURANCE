package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.TechnologyFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Technologies;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Technology DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-03-13
 */
@Service
public class TechnologyModelDAO implements PageableModelDAO<TechnologyViewDTO, TechnologyFilter> {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private UserService userService;

	@Autowired
	private EntityManager entityManager;

	@Override
	public PagedResult<TechnologyViewDTO> getItemsPageable(TechnologyFilter filter, Pageable pageable, BaseSort sort) {

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		Long organizationId = organizationService.getCurrentOrganizationId();

		// Define base hql data Query
		String hqlQuery = "SELECT tec FROM Technologies tec LEFT JOIN FETCH tec.technologyCategory cat " +
			"LEFT JOIN FETCH tec.country LEFT JOIN FETCH tec.systems LEFT JOIN FETCH tec.vendor " +
			"LEFT JOIN FETCH tec.createdBy LEFT JOIN FETCH tec.updatedBy";

		// Define base count Query
		String hqlQueryCount = "SELECT count(tec) FROM Technologies tec LEFT JOIN tec.technologyCategory cat ";

		// Build Query String
		String whereString = " WHERE tec.organizationId = :organizationId ";
		if (organizationId == null && userService.isSuperAdmin()) {
			whereString = " WHERE tec.organizationId IS NULL ";
		}
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(tec.name) LIKE (CONCAT(UPPER(:name), '%')) ";
			whereString += " AND (UPPER(tec.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(tec.description) LIKE (CONCAT('%', UPPER(:name), '%'))))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND tec.id NOT IN :excludeIds";
		}
		if (filter.getTechnologyCategoryId() != null) {
			whereString += " AND cat.id = :technologyCategoryId ";
		}
		if (filter.getTechnologySubcategoryId() != null) {
			whereString += " AND tec.technologySubcategoryId = :technologySubcategoryId ";
		}
		if (filter.getTechnologyClassTypeId() != null) {
			whereString += " AND tec.technologyClassTypeId = :technologyClassTypeId ";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "tec.id"),
			Map.entry("name", "tec.name"),
			Map.entry("version", "tec.version"),
			Map.entry("description", "tec.description"),
			Map.entry("technologyCategory", "cat.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		} else {
			 searchQueryString += "ORDER BY tec.id ASC";
		}

		// Build Query data
		TypedQuery<Technologies> typedQuery = entityManager.createQuery(searchQueryString, Technologies.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<TechnologyViewDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), TechnologyViewDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<TechnologyViewDTO>(resultList, resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param organizationId
	 * @param query
	 */
	private void applySearchFilterValues(TechnologyFilter filter, Long organizationId, Query query) {
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (filter.getTechnologyCategoryId() != null) query.setParameter("technologyCategoryId", filter.getTechnologyCategoryId());
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
		if (filter.getTechnologySubcategoryId() != null) query.setParameter("technologySubcategoryId", filter.getTechnologySubcategoryId());
		if (filter.getTechnologyClassTypeId() != null) query.setParameter("technologyClassTypeId", filter.getTechnologyClassTypeId());
	}
}
