package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.ProcessFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Processes;
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
 * Process DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-07-17
 */
@Service
public class ProcessModelDAO implements PageableModelDAO<ProcessViewDTO, ProcessFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<ProcessViewDTO> getItemsPageable(ProcessFilter filter, Pageable pageable, BaseSort sort) {

		// Detect filtered values
		Long organizationId = organizationService.getCurrentOrganizationId();
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");
		SystemRefDTO system = Optional.ofNullable(filter.getSystem()).orElse(null);
		BusinessUnitRefDTO businessUnitOwns = Optional.ofNullable(filter.getBusinessUnitOwns()).orElse(null);
		BusinessUnitRefDTO businessUnitUses = Optional.ofNullable(filter.getBusinessUnitUses()).orElse(null);

		// Define base hql data Query
		String hqlQuery = "SELECT pr FROM Processes pr LEFT JOIN FETCH pr.owner ow " +
			"LEFT JOIN FETCH pr.createdBy LEFT JOIN FETCH pr.updatedBy " ;

		// Define base count Query
		String hqlQueryCount = "SELECT count(pr) FROM Processes pr ";

		// Build Query String
		String whereString = " WHERE pr.organizationId = :organizationId";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(pr.name) LIKE (CONCAT(UPPER(:name), '%'))";
			whereString += " AND (UPPER(pr.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(pr.description) LIKE (CONCAT('%', UPPER(:name), '%'))))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND pr.id NOT IN :excludeIds";
		}
		if (system != null && system.getId() != null) {
			hqlQuery += " JOIN pr.systems sys";
			hqlQueryCount += " JOIN pr.systems sys";
			whereString += " AND sys.id=:systemId";
		} else {
			hqlQuery += " LEFT JOIN FETCH pr.systems sys";
		}
		if (businessUnitOwns != null && businessUnitOwns.getId() != null) {
			hqlQuery += " JOIN pr.businessUnit bu";
			hqlQueryCount += " JOIN pr.businessUnit bu";
			whereString += " AND bu.id=:businessUnitId";
		} else {
			hqlQuery += " LEFT JOIN FETCH pr.businessUnit bu";
		}
		if (businessUnitUses != null && businessUnitUses.getId() != null) {
			hqlQuery += " JOIN pr.businessUnitsUsed buu";
			hqlQueryCount += " JOIN pr.businessUnitsUsed buu";
			whereString += " AND buu.id=:businessUnitUsesId";
		} else {
			hqlQuery += " LEFT JOIN FETCH pr.businessUnitsUsed buu";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "pr.id"),
			Map.entry("name", "pr.name"),
			Map.entry("revenueProcessed", "pr.revenueProcessed"),
			Map.entry("description", "sys.description"),
			Map.entry("businessUnit", "bu.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<Processes> typedQuery = entityManager.createQuery(searchQueryString, Processes.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<ProcessViewDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), ProcessViewDTO.class);

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
	private void applySearchFilterValues(ProcessFilter filter, Long organizationId, Query query) {
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");
		SystemRefDTO system = Optional.ofNullable(filter.getSystem()).orElse(null);
		BusinessUnitRefDTO businessUnitOwns = Optional.ofNullable(filter.getBusinessUnitOwns()).orElse(null);
		BusinessUnitRefDTO businessUnitUses = Optional.ofNullable(filter.getBusinessUnitUses()).orElse(null);

		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
		if (system != null && system.getId() != null) query.setParameter("systemId", system.getId());
		if (businessUnitOwns != null && businessUnitOwns.getId() != null) query.setParameter("businessUnitId", businessUnitOwns.getId());
		if (businessUnitUses != null && businessUnitUses.getId() != null) query.setParameter("businessUnitUsesId", businessUnitUses.getId());
	}
}
