package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.AssessmentFilter;
import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Assessments;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.SystemsService;
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
 * Assessment DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @since    2020-07-15
 */
@Service
public class AssessmentModelDAO implements PageableModelDAO<AssessmentViewDTO, AssessmentFilter> {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private SystemsService systemsService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<AssessmentViewDTO> getItemsPageable(AssessmentFilter filter, Pageable pageable, BaseSort sort) {

		// Detect filtered values
		Long organizationId = organizationService.getCurrentOrganizationId();
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");
		Long assessmentLevelId = filter.getAssessmentLevelId();
		Systems system = null;
		if (filter.getSystemId() != null) {
			system = systemsService.getSystemForCurrentOrganization(filter.getSystemId());
		}

		// Define base hql data Query
		String hqlQuery = "SELECT DISTINCT ast FROM Assessments ast LEFT JOIN FETCH ast.assessmentLevel LEFT JOIN FETCH ast.assessmentType " +
			" LEFT JOIN FETCH ast.technologyCategories LEFT JOIN FETCH ast.systems LEFT JOIN FETCH ast.securityRequirements " +
			" LEFT JOIN FETCH ast.processes LEFT JOIN FETCH ast.legalOrganization LEFT JOIN FETCH ast.createdBy LEFT JOIN FETCH ast.updatedBy ";

		// Define base count hql Query
		String hqlQueryCount = "SELECT count(ast) FROM Assessments ast ";

		// Build Query String
		String whereString = " WHERE ast.organizationId = :organizationId ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(ast.name) LIKE (CONCAT(UPPER(:name), '%')) ";
			whereString += " AND (UPPER(ast.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(ast.description) LIKE CONCAT('%', UPPER(:name), '%'))";
		}
		if (assessmentLevelId != null) {
			whereString += " AND ast.assessmentLevel.id = :assessmentLevelId ";
		}
		if (system != null) {
			whereString += " AND :system MEMBER OF ast.systems ";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND ast.id NOT IN :excludeIds ";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("name", "ast.name"),
			Map.entry("description", "ast.description"),
			Map.entry("assessmentLevel", "ast.assessmentLevel.name"),
			Map.entry("relationToRequirementType", "ast.relationToRequirementType")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<Assessments> typedQuery = entityManager.createQuery(searchQueryString, Assessments.class);
		applySearchFilterValues(filter, organizationId, typedQuery, system);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<AssessmentViewDTO> resultList = DTOBase.fromEntitiesList(typedQuery.getResultList(), AssessmentViewDTO.class);

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount, system);
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
	private void applySearchFilterValues(AssessmentFilter filter, Long organizationId, Query query, Systems system) {
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");
		Long assessmentLevelId = filter.getAssessmentLevelId();

		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (assessmentLevelId != null) query.setParameter("assessmentLevelId", assessmentLevelId);
		if (system != null) query.setParameter("system", system);
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
	}

}
