package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.QuestionFilter;
import com.cyberintech.vrisk.server.model.dto.answers.MetricResultAnswerViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.repository.results.QualitativeQuestionAnswerResult;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.service.RiskModelService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MetricResultAnswerModelDAO implements PageableModelDAO<MetricResultAnswerViewDTO, QuestionFilter> {

	@Autowired
	private RiskModelService riskModelService;

	@PersistenceContext
	private EntityManager entityManager;


	@Override
	public PagedResult<MetricResultAnswerViewDTO> getItemsPageable(QuestionFilter filter, Pageable pageable, BaseSort sort) {

		// Detect filtered values
		if (filter.getRiskModelId() == null) {
			throw new BadRequestException("Risk Model ID is required!", ApplicationExceptionCodes.RISK_MODEL_ID_REQUIRED);
		}
		RiskModels riskModel = riskModelService.getRiskModel(filter.getRiskModelId());
		String questionFilter = Optional.ofNullable(filter.getQuestion()).orElse("");
		String statusFilter = Optional.ofNullable(filter.getStatus()).orElse("");
		String vendorTypeFilter = Optional.ofNullable(filter.getVendorType()).orElse("");
		String metricDomainFilter = Optional.ofNullable(filter.getMetricDomain()).orElse("");

		// Define base hql data Query
		String hqlQuery = "SELECT new com.cyberintech.vrisk.server.repository.results.QualitativeQuestionAnswerResult(mr, q, a) " +
			"FROM MetricResultAnswers mr RIGHT OUTER JOIN mr.question q LEFT JOIN mr.answer a " +
			"JOIN q.qualitativeMetric qm JOIN qm.metricDomain md ";

		// Define base count hql Query
		String hqlQueryCount = "SELECT count(ast) FROM Assessments ast ";

		// Build Query String
		String whereString = " WHERE q.riskModelId = :riskModelId ";
		if (StringUtils.isNotEmpty(questionFilter)) {
			whereString += " AND UPPER(q.question) LIKE (CONCAT(UPPER(:question), '%'))";
		}
		if (StringUtils.isNotEmpty(statusFilter)) {
			if (statusFilter.equalsIgnoreCase(QuestionFilter.STATUS_ANSWERED)) {
				whereString += " AND mr.answer IS NOT NULL";

			} else if (statusFilter.equalsIgnoreCase(QuestionFilter.STATUS_UNANSWERED)) {
				whereString += " AND mr.answer IS NULL";
			}
		}
		if (StringUtils.isNotEmpty(metricDomainFilter)) {
			whereString += " AND md.code = :metricDomain ";
		}
		if (StringUtils.isNotEmpty(vendorTypeFilter)) {
			whereString += " AND q.vendorType = :vendorType ";

		} else {
			whereString += " AND (q.vendorType='Organization' OR q.vendorType='Both') ";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		if (sort != null) {
			searchQueryString += sort.toOrderString();
		}

		// Build Query data
		TypedQuery<QualitativeQuestionAnswerResult> typedQuery = entityManager.createQuery(searchQueryString, QualitativeQuestionAnswerResult.class);
		applySearchFilterValues(filter, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<MetricResultAnswerViewDTO> resultList = typedQuery.getResultList().stream().map(MetricResultAnswerViewDTO::new).collect(Collectors.toList());

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
	private void applySearchFilterValues(QuestionFilter filter, Query query) {
		String questionFilter = Optional.ofNullable(filter.getQuestion()).orElse("");
		String metricDomainFilter = Optional.ofNullable(filter.getMetricDomain()).orElse("");
		String vendorTypeFilter = Optional.ofNullable(filter.getVendorType()).orElse("");

		query.setParameter("riskModelId", filter.getRiskModelId());
		if (StringUtils.isNotEmpty(questionFilter)) query.setParameter("question", questionFilter);
		if (StringUtils.isNotEmpty(metricDomainFilter)) query.setParameter("metricDomain", metricDomainFilter);
		if (StringUtils.isNotEmpty(vendorTypeFilter)) query.setParameter("vendorType", vendorTypeFilter);
	}
}
