package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.ControlTestFilter;
import com.cyberintech.vrisk.server.model.dto.assessments.ControlTestViewDTO;
import com.cyberintech.vrisk.server.repository.results.ControlTestResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ControlTestModelDAO implements PageableModelDAO<ControlTestViewDTO, ControlTestFilter> {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<ControlTestViewDTO> getItemsPageable(ControlTestFilter filter, Pageable pageable, BaseSort sort) {

		// Detect filtered values
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");
		String statusFilter = Optional.ofNullable(filter.getStatus()).orElse("");

		// Define base hql data Query
		String hqlQuery = "SELECT new com.cyberintech.vrisk.server.repository.results.ControlTestResult(ct, csc, aw) FROM ControlTests ct " +
			"RIGHT OUTER JOIN ct.controlSubcategory csc LEFT JOIN ct.assessmentWeight aw ";

		// Define base count hql Query
		String hqlQueryCount = "SELECT count(csc) FROM ControlTests ct RIGHT OUTER JOIN ct.controlSubcategory csc ";

		// Build Query String
		String whereString = " WHERE 1 = 1 ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(csc.name) LIKE (CONCAT(UPPER(:name), '%'))";
			whereString += " AND (UPPER(csc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR UPPER(csc.description) LIKE (CONCAT('%', UPPER(:name), '%')))";
		}
		if (StringUtils.isNotEmpty(statusFilter)) {
			if (statusFilter.equalsIgnoreCase(ControlTestFilter.STATUS_ANSWERED)) {
				whereString += " AND ct.assessmentWeight IS NOT NULL ";

			} else if (statusFilter.equalsIgnoreCase(ControlTestFilter.STATUS_UNANSWERED)) {
				whereString += " AND ct.assessmentWeight IS NULL ";
			}
		}

		// Build sort
		String searchQueryString = hqlQuery + whereString;
//		if (sort != null) {
//			searchQueryString += sort.toOrderString();
//		}

		// Build Query data
		TypedQuery<ControlTestResult> typedQuery = entityManager.createQuery(searchQueryString, ControlTestResult.class);
		applySearchFilterValues(filter, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<ControlTestViewDTO> resultList = typedQuery.getResultList().stream().map(ControlTestViewDTO::new).collect(Collectors.toList());

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<>(resultList, resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param query
	 */
	private void applySearchFilterValues(ControlTestFilter filter, Query query) {
		String nameFilter = Optional.ofNullable(filter.getName()).orElse("");

		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
	}
}
