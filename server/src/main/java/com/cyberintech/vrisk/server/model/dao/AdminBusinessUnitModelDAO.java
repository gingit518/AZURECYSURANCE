package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.BusinessUnitFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitViewExtDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import org.apache.commons.lang3.StringUtils;
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
 * Admin Business Unit DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-07-17
 */
@Service
public class AdminBusinessUnitModelDAO implements PageableModelDAO<BusinessUnitViewExtDTO, BusinessUnitFilter> {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<BusinessUnitViewExtDTO> getItemsPageable(BusinessUnitFilter filter, Pageable pageable, BaseSort sort) {

		// Detect filtered values
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		if (filter.getOrganizationId() == null) {
			throw new BadRequestException("Organization ID is required!", ApplicationExceptionCodes.ORGANIZATION_ID_REQUIRED);
		}

		// Define base hql data Query
		String hqlQuery = "SELECT bm FROM BusinessUnits bm LEFT JOIN FETCH bm.owner ow LEFT JOIN FETCH bm.infosecFocalPerson ip " +
			"LEFT JOIN FETCH bm.createdBy LEFT JOIN FETCH bm.updatedBy LEFT JOIN FETCH bm.parent AS bmp ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(bm) FROM BusinessUnits bm ";

		// Build Query String
		String whereString = " WHERE bm.organizationId = :organizationId ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			whereString += " AND UPPER(bm.name) LIKE (CONCAT(UPPER(:name), '%'))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND bm.id NOT IN :excludeIds";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "bm.id"),
			Map.entry("name", "bm.name"),
			Map.entry("parent", "bmp.name"),
			Map.entry("description", "bm.description"),
			Map.entry("infosecFocalPersonName", "ip.name")
		);
		if (sort != null) searchQueryString += sort.toOrderString(sortMapping);

		// Build Query data
		TypedQuery<BusinessUnits> typedQuery = entityManager.createQuery(searchQueryString, BusinessUnits.class);
		applySearchFilterValues(filter, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<BusinessUnitViewExtDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), BusinessUnitViewExtDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<>(resultList, resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param query
	 */
	private void applySearchFilterValues(BusinessUnitFilter filter, Query query) {
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (filter.getOrganizationId() != null) query.setParameter("organizationId", filter.getOrganizationId());
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
	}
}
