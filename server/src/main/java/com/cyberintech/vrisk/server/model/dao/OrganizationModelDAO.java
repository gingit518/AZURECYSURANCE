package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.OrganizationFilter;
import com.cyberintech.vrisk.server.model.data.SubsidiaryOrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Organization DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-05-11
 */
@Service
public class OrganizationModelDAO  implements PageableModelDAO<Organizations, OrganizationFilter> {

	private static final Map<String, String> SORT_MAPPING = Collections.unmodifiableMap(
		Map.ofEntries(
			Map.entry("id", "o.id"),
			Map.entry("createDate", "o.name"),
			Map.entry("countryName", "ct.name"),
			Map.entry("description", "o.description"),
			Map.entry("name", "o.name"),
			Map.entry("organizationType", "o.organizationType"),
			Map.entry("owner", "ow.fullName"),
			Map.entry("parent", "pa.name"),
			Map.entry("rootParent", "rpa.name"),
			Map.entry("site", "o.site")));

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private UserService userService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<Organizations> getItemsPageable(OrganizationFilter filter, Pageable pageable, BaseSort sort) {

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");
		List<Long> excludeIds = null;
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			excludeIds = filter.getExcludeIds();
		}
		OrganizationType organizationType = null;
		if (filter.getOrganizationType() != null) {
			organizationType = filter.getOrganizationType();
		}
		Long ownerId = null;
		if (filter.getOwner() != null && filter.getOwner().getId() != null) {
			ownerId = filter.getOwner().getId();
		}
		Long parentId = null;
		if (filter.getParent() != null && filter.getParent().getId() != null) {
			parentId = filter.getParent().getId();
		}
		Long rootParentId = null;
		if (filter.getRootParent() != null && filter.getRootParent().getId() != null) {
			rootParentId = filter.getRootParent().getId();
		}

		String hqlQuery = "SELECT o FROM Organizations o LEFT JOIN FETCH o.country ct " +
			"LEFT JOIN FETCH o.state st LEFT JOIN FETCH o.city ci LEFT JOIN FETCH o.currency cu " +
			"LEFT JOIN FETCH o.language ln LEFT JOIN FETCH o.status sa LEFT JOIN FETCH o.owner ow " +
			"LEFT JOIN FETCH o.parent pa LEFT JOIN FETCH o.rootParent rpa ";

		String hqlQueryCount = "SELECT count(o) FROM Organizations o LEFT JOIN o.owner ow LEFT JOIN o.parent pa LEFT JOIN o.rootParent rpa ";

		// Build Query String
		String whereString = " WHERE o.id > 0 ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(o.name) LIKE (CONCAT(UPPER(:name), '%')) ";
			whereString += " AND (UPPER(o.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(o.description) LIKE CONCAT('%', UPPER(:name), '%'))";
		}
		if (excludeIds != null) {
			whereString += " AND o.id NOT IN :excludeIds";
		}
		if (organizationType != null) {
			whereString += " AND o.organizationType = :organizationType ";
		}
		if (ownerId != null) {
			whereString += " AND ow.id = :ownerId ";
		}
		if (parentId != null) {
			whereString += " AND pa.id = :parentId ";
		}
		if (rootParentId != null) {
			whereString += " AND rpa.id = :rootParentId ";
		}
		if (CollectionUtils.isNotEmpty(filter.getPackagePlanIds())) {
			whereString += " AND o.packagePlan.id in :packagePlanIds ";
		}

		// Build Sort based on mapping
		String searchQueryString = hqlQuery + whereString;
		if (sort != null) searchQueryString += sort.toOrderStringSafe(SORT_MAPPING);

		// Build Query data
		Query typedQuery = entityManager.createQuery(searchQueryString);
		applySearchFilterValues(typedQuery, filter, nameFilter, excludeIds, organizationType, ownerId, parentId, rootParentId);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<Organizations> resultList = typedQuery.getResultList();

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(queryCount, filter, nameFilter, excludeIds, organizationType, ownerId, parentId, rootParentId);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<>(resultList, resultsCount);
	}

	public <T extends DTOBase> PagedResult<T> getItemsPageable(SubsidiaryOrganizationFilter filter, Pageable pageable, BaseSort sort, Class<T> viewClass) {

		// Detect filtered values
		Long organizationId = organizationService.getCurrentOrganizationId();
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		String hqlQuery = "SELECT o FROM Organizations o JOIN o.rootParent rpa LEFT JOIN o.parent pa " +
			"LEFT JOIN FETCH o.country ct LEFT JOIN FETCH o.state st LEFT JOIN FETCH o.city ci " +
			"LEFT JOIN FETCH o.currency cu LEFT JOIN FETCH o.language ln LEFT JOIN FETCH o.status sa " +
			"LEFT JOIN FETCH o.owner ow LEFT JOIN FETCH o.packagePlan pp ";

		// Define base count hql Query
		String hqlQueryCount = "SELECT count(o) FROM Organizations o JOIN o.rootParent rpa ";

		// Build Query String
		String whereString = " WHERE o.organizationType = :organizationType AND rpa.id = :rootParentId ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			whereString += " AND (UPPER(o.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR UPPER(o.description) LIKE (CONCAT('%', UPPER(:name), '%'))) ";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND o.id NOT IN :excludeIds ";
		}
		if (filter.getIsCloudVendor() != null && filter.getIsCloudVendor()) {
			whereString += " AND o.isCloudVendor = true ";
		}

		// Filter Available Vendors for Vendor Call
		List<Long> vendorIds = null;
		if (OrganizationType.Vendor.equals(filter.getOrganizationType()) && userService.isVendorEmployee()) {
			Users currentUser = userService.getCurrentUserEntity();
			vendorIds = currentUser.getVendors().stream().map(Organizations::getId).collect(Collectors.toList());
			whereString += " AND o.id IN :vendorIds ";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		if (sort != null) {
			searchQueryString += sort.toOrderStringSafe(SORT_MAPPING);
		}

		// Build Query data
		TypedQuery<Organizations> typedQuery = entityManager.createQuery(searchQueryString, Organizations.class);
		applySearchFilterValues(filter, organizationId, typedQuery, vendorIds);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<T> resultList = (List<T>) DTOBase.fromEntitiesList(typedQuery.getResultList(), viewClass);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount, vendorIds);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<>(resultList, resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param query
	 * @param filter
	 * @param nameFilter
	 * @param excludeIds
	 * @param organizationType
	 * @param ownerId
	 * @param parentId
	 * @param rootParentId
	 */
	private void applySearchFilterValues(Query query, OrganizationFilter filter, String nameFilter, List<Long> excludeIds, OrganizationType organizationType, Long ownerId, Long parentId, Long rootParentId) {

		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (excludeIds != null) query.setParameter("excludeIds", excludeIds);
		if (organizationType != null) query.setParameter("organizationType", organizationType);
		if (ownerId != null) query.setParameter("ownerId", ownerId);
		if (parentId != null) query.setParameter("parentId", parentId);
		if (rootParentId != null) query.setParameter("rootParentId", rootParentId);
		if (CollectionUtils.isNotEmpty(filter.getPackagePlanIds())) {
			query.setParameter("packagePlanIds", filter.getPackagePlanIds());
		}
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param organizationId
	 * @param query
	 * @param vendorIds
	 */
	private void applySearchFilterValues(SubsidiaryOrganizationFilter filter, Long organizationId, Query query, List<Long> vendorIds) {
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		query.setParameter("organizationType", filter.getOrganizationType());
		query.setParameter("rootParentId", organizationId);
		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
		if (vendorIds != null) query.setParameter("vendorIds", vendorIds);
	}
}
