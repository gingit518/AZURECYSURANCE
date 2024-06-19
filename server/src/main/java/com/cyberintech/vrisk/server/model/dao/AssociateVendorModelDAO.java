package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.results.AssociateVendorResult;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.UserService;
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
import java.util.stream.Collectors;

/**
 * Associate Vendor DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-07-17
 */
@Service
public class AssociateVendorModelDAO implements PageableModelDAO<AssociateVendorResult, NameFilter> {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private UserService userService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<AssociateVendorResult> getItemsPageable(NameFilter filter, Pageable pageable, BaseSort sort) {

		// Detect filtered values
		Long organizationId = organizationService.getCurrentOrganizationId();
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		String hqlQuery = "SELECT new com.cyberintech.vrisk.server.repository.results.AssociateVendorResult(allv, av) " +
			"FROM Organizations allv LEFT OUTER JOIN AssociateVendors av ON av.vendor=allv ";

		// Define base count hql Query
		String hqlQueryCount = "SELECT count(allv) FROM Organizations allv ";

		// Build Query String
		String whereString = " WHERE allv.rootParent.id = :organizationId AND allv.organizationType=:organizationType ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(allv.name) LIKE (CONCAT(UPPER(:name), '%'))";
			whereString += " AND (UPPER(allv.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(allv.description) LIKE CONCAT('%', UPPER(:name), '%'))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND allv.id NOT IN :excludeIds";
		}

		// Filter Available Vendors for Vendors Call
		List<Long> vendorIds = null;
		if (userService.isVendorEmployee()) {
			Users currentUser = userService.getCurrentUserEntity();
			vendorIds = currentUser.getVendors().stream().map(Organizations::getId).collect(Collectors.toList());
			whereString += " AND allv.id IN :vendorIds";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "allv.id"),
			Map.entry("name", "allv.name"),
			Map.entry("vendor", "allv.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<AssociateVendorResult> typedQuery = entityManager.createQuery(searchQueryString, AssociateVendorResult.class);
		applySearchFilterValues(filter, organizationId, typedQuery, vendorIds);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<AssociateVendorResult> resultList = typedQuery.getResultList();

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount, vendorIds);
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
	private void applySearchFilterValues(NameFilter filter, Long organizationId, Query query, List<Long> vendorIds) {
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		query.setParameter("organizationType", OrganizationType.Vendor);
		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
		if (vendorIds != null) query.setParameter("vendorIds", vendorIds);

	}
}
