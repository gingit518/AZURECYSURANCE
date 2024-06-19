package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.ByFrameworkFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.control_subcategory.ControlSubcategoryViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlSubcategories;
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
 * Control Subategory DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-08-20
 */
@Service
public class ControlSubcategoryModelDAO implements PageableModelDAO<ControlSubcategoryViewDTO, ByFrameworkFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<ControlSubcategoryViewDTO> getItemsPageable(ByFrameworkFilter filter, Pageable pageable, BaseSort sort) {
		// Detect filtering values
		Long organizationId = organizationService.getCurrentOrganizationId();
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");
		Long parentId = null;
		Long frameworkId = null;
		List<Long> excludeIds = null;
		if (filter != null && filter.getParentId() != null) {
			parentId = filter.getParentId();
		}
		if (filter != null && filter.getFrameworkId() != null) {
			frameworkId = filter.getFrameworkId();
		}
		if (filter != null && filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			excludeIds = filter.getExcludeIds();
		}

		// Define base hql data Query
		String hqlQuery = "SELECT cs FROM ControlSubcategories cs JOIN FETCH cs.controlCategory cc LEFT JOIN cs.assessmentType fm ";

		// Define base hql count Query
		String hqlCountQuery = "SELECT count(cs) FROM ControlSubcategories cs JOIN cs.controlCategory cc JOIN cs.assessmentType fm ";

		// Build Query String
		String whereString = " WHERE cs.organizationId = :organizationId";
		if (StringUtils.isNotEmpty(nameFilter)) {
			whereString += " AND (UPPER(cs.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR UPPER(cs.code) LIKE CONCAT(UPPER(:name), '%') OR UPPER(cs.description) LIKE (CONCAT('%', UPPER(:name), '%'))) ";
		}
		if (parentId != null) {
			whereString += " AND cc.id = :parentId ";
		}
		if (frameworkId != null) {
			whereString += " AND fm.id = :frameworkId ";
		}
		if (excludeIds != null) {
			whereString += " AND cs.id NOT IN :excludeId ";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.of(
			"id", "cs.id",
			"name", "cs.name",
			"code", "cs.code",
			"description", "cs.description",
			"controlCategory", "cc.name",
			"framework", "fm.name"
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<ControlSubcategories> typedQuery = entityManager.createQuery(searchQueryString, ControlSubcategories.class);
		applySearchFilterValues(organizationId, nameFilter, parentId, frameworkId, excludeIds, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<ControlSubcategoryViewDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), ControlSubcategoryViewDTO.class);

		// Calculate count Query
		Query countQuery = entityManager.createQuery(hqlCountQuery + whereString);
		applySearchFilterValues(organizationId, nameFilter, parentId, frameworkId, excludeIds, countQuery);
		Long resultsCount = (Long) countQuery.getSingleResult();

		return new PagedResult<>(resultList, resultsCount);
	}

	/**
	 * Apply filter values to Query
	 *
	 * @param organizationId
	 * @param nameFilter
	 * @param parentId
	 * @param frameworkId
	 * @param excludeIds
	 * @param query
	 */
	private void applySearchFilterValues(Long organizationId, String nameFilter, Long parentId, Long frameworkId, List<Long> excludeIds, Query query) {
		query.setParameter("organizationId", organizationId);
		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (parentId != null) query.setParameter("parentId", parentId);
		if (frameworkId != null) query.setParameter("frameworkId", frameworkId);
		if (excludeIds != null) query.setParameter("excludeIds", excludeIds);
	}

}
