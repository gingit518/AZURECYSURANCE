package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.AuditLogFilter;
import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.dto.audit.ItemTypeDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Audit Log DAO Model
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @since    2019-07-23
 */
@Service
public class AuditLogModelDAO implements PageableModelDAO<AuditLog, AuditLogFilter> {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<AuditLog> getItemsPageable(AuditLogFilter filter, Pageable pageable, BaseSort sort) {

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");
		List<Long> users = Optional.ofNullable(filter.getUsers()).orElse(new ArrayList<>()).stream().map(UserRefDTO::getId).collect(Collectors.toList());
		List<Long> itemTypes = Optional.ofNullable(filter.getItemTypes()).orElse(new ArrayList<>()).stream().map(ItemTypeDTO::getId).collect(Collectors.toList());

		// Define base hql data Query
		String hqlQuery = "SELECT a FROM AuditLog a ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(a) FROM AuditLog a ";

		// Build Query String
		String whereString = " WHERE 1=1";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND (UPPER(u.fullName) LIKE (CONCAT(UPPER(:name), '%')) OR UPPER(u.email) LIKE (CONCAT(UPPER(:name), '%')))";
		}
		if (filter.getOrganizationId() != null) {
			whereString += " AND a.organizationId = :organizationId ";
		}
		if (filter.getItemId() != null) {
			whereString += " AND a.auditItemId = :auditItemId ";
		}
		if (filter.getDateFrom() != null) {
			whereString += " AND a.logDate >= :dateFrom ";
		}
		if (filter.getDateTo() != null) {
			whereString += " AND a.logDate < :dateTo ";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND a.id NOT IN :excludeIds";
		}
		if (users.size() > 0) {
			whereString += " AND a.auditUserId IN :users";
		}
		if (itemTypes.size() > 0) {
			whereString += " AND a.itemType IN :itemTypes";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "a.id"),
			Map.entry("logDate", "a.logDate")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		} else {
			searchQueryString += " ORDER BY a.id DESC";
		}

		// Build Query data
		TypedQuery<AuditLog> typedQuery = entityManager.createQuery(searchQueryString, AuditLog.class);
		applySearchFilterValues(filter, users, itemTypes, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<AuditLog> resultList = typedQuery.getResultList();

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, users, itemTypes, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<AuditLog>(resultList, resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param users
	 * @param query
	 */
	private void applySearchFilterValues(AuditLogFilter filter, List<Long> users, List<Long> itemTypes, Query query) {
		if (filter.getOrganizationId() != null) query.setParameter("organizationId", filter.getOrganizationId());
		if (filter.getDateFrom() != null) query.setParameter("dateFrom", filter.getDateFrom());
		if (filter.getDateTo() != null) query.setParameter("dateTo", filter.getDateTo());
		// if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
		if (users != null && users.size() > 0) query.setParameter("users", users);
		if (itemTypes != null && itemTypes.size() > 0) query.setParameter("itemTypes", itemTypes);
		if (filter.getItemId() != null) query.setParameter("auditItemId", filter.getItemId());
	}

}
