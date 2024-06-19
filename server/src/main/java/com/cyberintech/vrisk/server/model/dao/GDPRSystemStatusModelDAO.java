package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.GDPRFilter;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRSystemStatusDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRSystemStatus;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.service.OrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * System Status DAO Model
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @since    2019-07-23
 */
@Service
public class GDPRSystemStatusModelDAO implements PageableModelDAO<GDPRSystemStatusDTO, GDPRFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<GDPRSystemStatusDTO> getItemsPageable(GDPRFilter filter, Pageable pageable, BaseSort sort) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		// String hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.gdpr.GDPRSystemStatusDTO(ss, s) FROM Systems s LEFT OUTER JOIN GDPRSystemStatus ss ON ss.system=s ";
		String hqlQuery = "SELECT distinct s, ss FROM Systems s LEFT OUTER JOIN GDPRSystemStatus ss ON ss.system=s ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(distinct s) FROM Systems s LEFT OUTER JOIN GDPRSystemStatus ss ON ss.system=s ";

		// Build Query String
		String whereString = " WHERE s.organizationId = :organizationId ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(s.name) LIKE (CONCAT(UPPER(:name), '%')) ";
			whereString += " AND (UPPER(s.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(s.description) LIKE CONCAT('%', UPPER(:name), '%'))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND ss.id NOT IN :excludeIds";
		}
		if (filter.getDataTypeClassification() != null && filter.getDataTypeClassification().size() > 0) {
			hqlQuery += " JOIN s.dataTypeClassifications dtc ";
			hqlQueryCount += " JOIN s.dataTypeClassifications dtc ";
			whereString += " AND dtc.id IN :dataTypeClassifications";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "s.id"),
			Map.entry("system", "s.name"),
			Map.entry("articlesNumber", "ss.articlesNumber"),
			Map.entry("articlesProcessed", "ss.articlesProcessed"),
			Map.entry("compliance", "ss.compliance"),
			Map.entry("name", "s.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		} else {
			searchQueryString += " ORDER BY s.name ASC";
		}

		// Build Query data
		// TypedQuery<GDPRSystemStatusDTO> typedQuery = entityManager.createQuery(searchQueryString, GDPRSystemStatusDTO.class);
		Query typedQuery = entityManager.createQuery(searchQueryString);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<Object[]> queryResultList = typedQuery.getResultList();
		List<GDPRSystemStatusDTO> resultList = queryResultList.stream().map(objects -> {
			Systems system = (Systems) objects[0];
			GDPRSystemStatus systemStatus = (GDPRSystemStatus) objects[1];

			GDPRSystemStatusDTO result = new GDPRSystemStatusDTO(systemStatus, system);

			return result;
		}).collect(Collectors.toList());

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<GDPRSystemStatusDTO>(resultList, resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param organizationId
	 * @param query
	 */
	private void applySearchFilterValues(GDPRFilter filter, Long organizationId, Query query) {
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		if (filter.getDataTypeClassification() != null && filter.getDataTypeClassification().size() > 0) query.setParameter("dataTypeClassifications", filter.getDataTypeClassification());
		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
	}

	/**
	 * Get GDPR System Status Items List for Organization
	 *
	 * @param 	organizationId
	 * @return	GDPR System Status Items List
	 */
	public List<GDPRSystemStatusDTO> getItemsForOrganization(Long organizationId) {

		// Define base hql data Query
		String hqlQuery = "SELECT distinct s, ss FROM Systems s LEFT OUTER JOIN GDPRSystemStatus ss ON ss.system=s WHERE s.organizationId = :organizationId";

		// Build Query data
		Query typedQuery = entityManager.createQuery(hqlQuery);

		// Apply query data
		typedQuery.setParameter("organizationId", organizationId);

		List<Object[]> queryResultList = typedQuery.getResultList();
		List<GDPRSystemStatusDTO> result = queryResultList.stream().map(objects -> {
			Systems system = (Systems) objects[0];
			GDPRSystemStatus systemStatus = (GDPRSystemStatus) objects[1];

			GDPRSystemStatusDTO item = new GDPRSystemStatusDTO(systemStatus, system);

			return item;
		}).collect(Collectors.toList());

		return result;
	}

}
