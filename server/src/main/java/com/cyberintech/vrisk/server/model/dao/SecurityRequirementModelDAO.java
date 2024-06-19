package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.SecurityRequirementFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityRequirementDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.SecurityRequirements;
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
 * Security Requirement DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-07-17
 */
@Service
public class SecurityRequirementModelDAO implements PageableModelDAO<SecurityRequirementDTO, SecurityRequirementFilter> {

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<SecurityRequirementDTO> getItemsPageable(SecurityRequirementFilter filter, Pageable pageable, BaseSort sort) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		String codeFilter = Optional.ofNullable(filter.getName()).orElse("");

		// Define base hql data Query
		String hqlQuery = "SELECT sr FROM SecurityRequirements sr LEFT JOIN FETCH sr.securityControlFamily scf " +
			" LEFT JOIN FETCH sr.securityControlName scn LEFT JOIN FETCH sr.assessmentLevel al ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(sr) FROM SecurityRequirements sr ";

		// Build Query String
		String whereString = " WHERE sr.organizationId = :organizationId ";
		if (StringUtils.isNotEmpty(codeFilter)) {
			// whereString += " AND UPPER(sr.code) LIKE (CONCAT(UPPER(:code), '%')) ";
			whereString += " AND (UPPER(sr.code) LIKE CONCAT('%', UPPER(:code), '%') OR UPPER(sr.description) LIKE CONCAT('%', UPPER(:code), '%'))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND sr.id NOT IN :excludeIds ";
		}
		if (filter.getAssessmentLevelId() != null) {
			whereString += " AND sr.assessmentLevel.id = :assessmentLevelId ";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("code", "sr.code"),
			Map.entry("securityControlFamily", "sr.securityControlFamily.name"),
			Map.entry("securityControlName", "sr.securityControlName.name"),
			Map.entry("assessmentLevel", "sr.assessmentLevel.name"),
			Map.entry("programArea", "sr.programArea")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<SecurityRequirements> typedQuery = entityManager.createQuery(searchQueryString, SecurityRequirements.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<SecurityRequirementDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), SecurityRequirementDTO.class);

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
	private void applySearchFilterValues(SecurityRequirementFilter filter, Long organizationId, Query query) {
		String codeFilter = Optional.ofNullable(filter.getName()).orElse("");

		if (StringUtils.isNotEmpty(codeFilter)) query.setParameter("code", codeFilter);
		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
		if (filter.getAssessmentLevelId() != null) query.setParameter("assessmentLevelId", filter.getAssessmentLevelId());

	}
}
