package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.ControlTestResultFilter;
import com.cyberintech.vrisk.server.model.dto.assessments.SystemControlTestResultViewDTO;
import com.cyberintech.vrisk.server.service.AssessmentService;
import com.cyberintech.vrisk.server.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;

/**
 * System Requirement Control Test Result DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.01.30
 */
@Service
public class SystemControlTestResultModelDAO implements PageableModelDAO<SystemControlTestResultViewDTO, ControlTestResultFilter> {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private AssessmentService assessmentService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<SystemControlTestResultViewDTO> getItemsPageable(ControlTestResultFilter filter, Pageable pageable, BaseSort sort) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		// Define base hql data Query
		String hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.assessments.SystemControlTestResultViewDTO(sctr, s) FROM Systems s " +
			"LEFT OUTER JOIN SystemControlTestResults sctr ON sctr.system=s ";

		// Define base hql count Query
		String hqlQueryCount = "SELECT count(s) FROM Systems s LEFT OUTER JOIN SystemControlTestResults sctr ON sctr.system=s ";

		if (filter.getAssessmentId() != null) {
			hqlQuery += " LEFT JOIN Assessments assm ON s MEMBER OF assm.systems ";

			hqlQueryCount += " LEFT JOIN Assessments assm ON s MEMBER OF assm.systems ";
		}

		// Build Query String
		String whereString = " WHERE s.organizationId = :organizationId ";

		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND sctr.id NOT IN :excludeIds ";
		}
		if (filter.getSystemId() != null) {
			whereString += " AND s.id = :systemId ";
		}
		if (filter.getName() != null) {
			// whereString += " AND UPPER(s.name) LIKE (CONCAT(UPPER(:systemName), '%')) ";
			whereString += " AND (UPPER(s.name) LIKE CONCAT('%', UPPER(:systemName), '%') OR UPPER(s.description) LIKE CONCAT('%', UPPER(:systemName), '%'))";
		}
		if (filter.getAssessmentId() != null) {
			whereString += " AND assm.id = :assessmentId ";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "sctr.id"),
			Map.entry("assessmentWeight", "sctr.assessmentWeight"),
			Map.entry("systemName", "s.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		} else {
			searchQueryString += " ORDER BY s.name ASC ";
		}

		// Build Query data
		TypedQuery<SystemControlTestResultViewDTO> typedQuery = entityManager.createQuery(searchQueryString, SystemControlTestResultViewDTO.class);
		applySearchFilterValues(filter, organizationId, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<SystemControlTestResultViewDTO> resultList = typedQuery.getResultList();

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organizationId, queryCount);
		Long resultCount = (Long) queryCount.getSingleResult();

		return new PagedResult<>(resultList, resultCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param organizationId
	 * @param query
	 */
	private void applySearchFilterValues(ControlTestResultFilter filter, Long organizationId, Query query) {

		if (organizationId != null) query.setParameter("organizationId", organizationId);
		if (filter.getSystemId() != null) query.setParameter("systemId", filter.getSystemId());
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
		if (filter.getName() != null) query.setParameter("systemName", filter.getName());
		if (filter.getAssessmentId() != null) query.setParameter("assessmentId", filter.getAssessmentId());
	}
}
