package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.QuestionFilter;
import com.cyberintech.vrisk.server.model.data.QuestionVendorAdvancedFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.QualitativeQuestionViewDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.QualitativeQuestionWithAnswersViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.MetricDomainRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Qualitative Question Status DAO Model
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @since    2019-10-28
 */
@Service
public class QualitativeQuestionModelDAO {

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private MetricDomainRepository metricDomainRepository;

	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private RiskModelRepository riskModelRepository;

	public PagedResult<QualitativeQuestionViewDTO> getItemsPageable(QuestionFilter filter, Pageable pageable, BaseSort sort) {

		if (filter.getRiskModelId() == null) {
			throw new BadRequestException("Risk Model ID is required!", ApplicationExceptionCodes.RISK_MODEL_ID_REQUIRED);
		}

		RiskModels riskModel = riskModelRepository.findById(filter.getRiskModelId()).orElseThrow(() -> new ItemNotFoundException("Risk model is not found"));

		// Detect filtered values
		boolean isVendorEmployee = userService.isVendorEmployee();
		MetricDomains metricDomain = null;
		if (StringUtils.isNotEmpty(filter.getMetricDomain())) {
			metricDomain = getMetricDomainForOrganizationByCode(filter.getMetricDomain(), riskModel.getOrganizationId());
		}
		String nameFilter = Optional.ofNullable(filter.getQuestion()).orElse("");
		Collection<Long> excludeIds = filter.getExcludeIds();
		if (excludeIds == null || excludeIds.size() < 1) {
			excludeIds = Arrays.asList(0L);
		}
		VendorType vendorType = VendorType.of(filter.getVendorType(), null);
		List<VendorType> vendorTypes = null;
		boolean isOnlyInternalQuestionsAllowed = false;

		// Vendor Employee only allowed to see Vendor Questions
		if (isVendorEmployee) {
			vendorTypes = Arrays.asList(VendorType.CloudInternal, VendorType.VendorInternal);
//			vendorTypes = Arrays.asList(VendorType.Vendor, VendorType.Both, VendorType.Cloud);
//			isOnlyInternalQuestionsAllowed = true;
		} else if (vendorType != null) {
			vendorTypes = Arrays.asList(vendorType);
		}

		// Define base hql data Query
		String hqlQuery = "SELECT qq FROM QualitativeQuestions qq JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md LEFT JOIN FETCH qq.vendors v " +
			"LEFT JOIN FETCH qq.createdBy LEFT JOIN FETCH qq.updatedBy ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(qq) FROM QualitativeQuestions qq JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md ";

		// Build Query String
		String whereString = "WHERE qq.riskModelId = :riskModelId ";
		if (StringUtils.isNotEmpty(nameFilter)) {
			// whereString += " AND UPPER(qq.question) LIKE (CONCAT(UPPER(:question), '%'))";
			whereString += " AND (UPPER(qq.question) LIKE CONCAT('%', UPPER(:question), '%') OR UPPER(qq.description) LIKE CONCAT('%', UPPER(:question), '%'))";
		}
		if (excludeIds != null) {
			whereString += " AND qq.id NOT IN :excludeIds";
		}
		if (metricDomain != null) {
			whereString += " AND md.id=:metricDomainId";
		}
		if (vendorTypes != null && vendorTypes.size() > 0) {
			whereString += " AND qq.vendorType IN :vendorTypes";
		}
//		if (isOnlyInternalQuestionsAllowed) {
//			whereString += " AND qq.isInternal = true";
//		} else {
//			whereString += " AND (qq.isInternal = false OR qq.isInternal IS NULL)";
//		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "qq.id"),
			Map.entry("name", "qq.name"),
			Map.entry("code", "qq.code"),
			Map.entry("description", "qq.description"),
			Map.entry("vendorType", "qq.vendorType"),
			Map.entry("qualitativeMetric", "md.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		}

		// Build Query data
		TypedQuery<QualitativeQuestions> typedQuery = entityManager.createQuery(searchQueryString, QualitativeQuestions.class);
		applySearchFilterValues(filter, typedQuery, metricDomain, vendorTypes, excludeIds);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<QualitativeQuestions> resultList = typedQuery.getResultList();

		// Calculate count Query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, queryCount, metricDomain, vendorTypes, excludeIds);
		Long resultsCount = (Long) queryCount.getSingleResult();

		List<QualitativeQuestionViewDTO> itemsDTOList;
		// Set ReadOnly Flag for Vendor Employee
		if (isVendorEmployee) {
			Users currentUser = userService.getCurrentUserEntity();
			Set<Organizations> vendors = currentUser.getVendors();
			Set<Long> vendorIdsSet = vendors.stream().map(Organizations::getId).collect(Collectors.toSet());

			itemsDTOList = new ArrayList<>();
			for (QualitativeQuestions qualitativeQuestion: resultList) {
				QualitativeQuestionViewDTO itemDTO = new QualitativeQuestionViewDTO(qualitativeQuestion);
				Set<Long> currentVendors = qualitativeQuestion.getVendors().stream().map(Organizations::getId).collect(Collectors.toSet());
				boolean isEditable = CollectionUtils.containsAny(currentVendors, vendorIdsSet);
				itemDTO.setReadOnly(!isEditable);

				itemsDTOList.add(itemDTO);
			}
		} else {
			itemsDTOList = DTOBase.fromEntitiesList(resultList, QualitativeQuestionViewDTO.class);
		}

		return new PagedResult<>(itemsDTOList, resultsCount);
	}

	@Nullable
	private MetricDomains getMetricDomainForOrganizationByCode(String domainCode, Long organizationId) {
		MetricDomains metricDomain;
		metricDomain = metricDomainRepository.findFirstByCodeIgnoreCaseAndOrganizationId(domainCode, organizationId).orElse(null);
		if (metricDomain == null) {
			metricDomain = metricDomainRepository.findFirstByCodeIgnoreCaseAndOrganizationIdIsNull(domainCode).orElseThrow(() -> new ItemNotFoundException("Metric Domain is not found"));
		}
		return metricDomain;
	}

	public PagedResult<QualitativeQuestionWithAnswersViewDTO> getVendorItemsPageable(QuestionVendorAdvancedFilter filter, Pageable pageable, BaseSort sort) {

		Long organizationId = organizationService.getCurrentOrganizationId();

		// Define base hql data Query
		String hqlQuery = "SELECT distinct qq FROM QualitativeQuestions qq LEFT JOIN qq.vendors v JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md ";

		// Define base count Query
		String hqlQueryCount = "SELECT count(distinct qq) FROM QualitativeQuestions qq LEFT JOIN qq.vendors v JOIN qq.qualitativeMetric qm JOIN qm.metricDomain md ";

		// Build Query String
		String whereString = " WHERE qq.riskModelId = :riskModelId ";
		MetricDomains metricDomain = null;
		if (filter.getVendorId() != null && !Boolean.TRUE.equals(filter.getIgnoreVendorSelection())) {
			whereString += " AND (qq.allVendorsSelected = true OR v.id = :vendorId) ";
		}
		if (StringUtils.isNotEmpty(filter.getMetricDomain())) {
			metricDomain = getMetricDomainForOrganizationByCode(filter.getMetricDomain(), organizationId);
			whereString += " AND md.id=:metricDomainId ";
		}
		if (filter.getQuestionTypes() != null && filter.getQuestionTypes().size() > 0) {
			whereString += " AND qq.vendorType IN :vendorTypes ";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND qq.id NOT IN :excludeIds";
		}

		if (!Boolean.TRUE.equals(filter.getIgnoreInternal())) {
//			if (Boolean.TRUE.equals(filter.getIsInternal())) {
//				whereString += " AND qq.isInternal = true";
//			} else {
//				whereString += " AND (qq.isInternal = false OR qq.isInternal is null)";
//			}
		}

		List<String> vendorTypesLimit = new ArrayList<>();
		if (Boolean.TRUE.equals(filter.getIsServiceVendor())) {
			vendorTypesLimit.add("qq.isServiceVendor = true");
		}
		if (Boolean.TRUE.equals(filter.getIsSystemVendor())) {
			vendorTypesLimit.add("qq.isSystemVendor = true");
		}
		if (Boolean.TRUE.equals(filter.getIsTechnologyVendor())) {
			vendorTypesLimit.add("qq.isTechnologyVendor = true");
		}
		if (vendorTypesLimit.size() > 0) {
			whereString += " AND (" + StringUtils.join(vendorTypesLimit, " OR ");
			whereString += " OR (qq.isServiceVendor != true AND qq.isSystemVendor != true AND qq.isTechnologyVendor != true)";
			whereString += ")";
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString;
		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "qq.id")
			, Map.entry("vendor", "v.name")
		);
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		} else {
			searchQueryString += " ORDER BY qq.ordinal ASC, qq.id ASC";
		}

		// Build Query data
		TypedQuery<QualitativeQuestions> typedQuery = entityManager.createQuery(searchQueryString, QualitativeQuestions.class);
		applySearchFilterValues(filter, metricDomain, typedQuery);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<QualitativeQuestions> resultList = typedQuery.getResultList();

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, metricDomain, queryCount);
		Long resultsCount = (Long) queryCount.getSingleResult();

		return new PagedResult<QualitativeQuestionWithAnswersViewDTO>(DTOBase.fromEntitiesList(resultList, QualitativeQuestionWithAnswersViewDTO.class), resultsCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param metricDomain
	 * @param query
	 */
	private void applySearchFilterValues(QuestionVendorAdvancedFilter filter, MetricDomains metricDomain, Query query) {
		query.setParameter("riskModelId", filter.getRiskModelId());
		if (filter.getVendorId() != null && !Boolean.TRUE.equals(filter.getIgnoreVendorSelection())) query.setParameter("vendorId", filter.getVendorId());
		if (metricDomain != null) query.setParameter("metricDomainId", metricDomain.getId());
		if (filter.getQuestionTypes() != null && filter.getQuestionTypes().size() > 0) query.setParameter("vendorTypes", filter.getQuestionTypes());
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param query
	 * @param metricDomain
	 * @param vendorTypes
	 */
	private void applySearchFilterValues(QuestionFilter filter, Query query, MetricDomains metricDomain, List<VendorType> vendorTypes, Collection<Long> excludeIds) {
		String nameFilter = Optional.ofNullable(filter.getQuestion()).orElse("");

		query.setParameter("riskModelId", filter.getRiskModelId());
		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("question", nameFilter);
		if (excludeIds != null) query.setParameter("excludeIds", excludeIds);
		if (metricDomain != null) query.setParameter("metricDomainId", metricDomain.getId());
		if (vendorTypes != null && vendorTypes.size() > 0) query.setParameter("vendorTypes", vendorTypes);
	}

}
