package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitViewExtDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
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
 * Business Unit DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-02-24
 */
@Service
public class BusinessUnitModelDAO implements PageableModelDAO<BusinessUnitViewExtDTO, NameFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<BusinessUnitViewExtDTO> getItemsPageable(NameFilter filter, Pageable pageable, BaseSort sort) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		String hqlQuery = "";

		// Define base count Query
		String hqlQueryCount = "";

		if(StringUtils.isNotEmpty(nameFilter)) {
			hqlQuery = "SELECT DISTINCT bm FROM BusinessUnits bu JOIN BusinessUnitLevels bul ON bu.id = bul.parentId " +
				" INNER JOIN BusinessUnits bm ON bm.id = bul.childId LEFT JOIN FETCH bm.owner ow " +
				" LEFT JOIN FETCH bm.infosecFocalPerson ip LEFT JOIN FETCH bm.createdBy LEFT JOIN FETCH bm.updatedBy LEFT JOIN FETCH bm.parent AS bmp ";
			hqlQueryCount = "SELECT DISTINCT count(bm) FROM BusinessUnits bu JOIN BusinessUnitLevels bul ON bu.id = bul.parentId " +
				" INNER JOIN BusinessUnits bm ON bm.id = bul.childId ";
		} else {
			hqlQuery = "SELECT bm FROM BusinessUnits bm LEFT JOIN FETCH bm.owner ow LEFT JOIN FETCH bm.infosecFocalPerson ip " +
				"LEFT JOIN FETCH bm.createdBy LEFT JOIN FETCH bm.updatedBy LEFT JOIN FETCH bm.parent AS bmp ";
			hqlQueryCount = "SELECT count(bm) FROM BusinessUnits bm ";
		}

		// Build Query String
		String whereString = " WHERE bm.organizationId = :organizationId";
		if (StringUtils.isNotEmpty(nameFilter)) {
			whereString += " AND (UPPER(bm.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR UPPER(bm.description) LIKE (CONCAT('%', UPPER(:name), '%'))) ";
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
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<BusinessUnits> typedQuery = entityManager.createQuery(searchQueryString, BusinessUnits.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<BusinessUnitViewExtDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), BusinessUnitViewExtDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<BusinessUnitViewExtDTO>(resultList, resultsCount);
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
