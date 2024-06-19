package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.ControlTestResultFilter;
import com.cyberintech.vrisk.server.model.dto.assessments.OrganizationRequirementControlTestResultDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentFrameworkLevel;
import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentLevel;
import com.cyberintech.vrisk.server.model.jpa.domains.RelationToRequirementType;
import com.cyberintech.vrisk.server.model.jpa.entity.Assessments;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.repository.jpa.AssessmentsRepository;
import com.cyberintech.vrisk.server.service.OrganizationService;
import org.apache.commons.collections4.CollectionUtils;
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
 * Organization Requirement Control Test Result DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.01.29
 */
@Service
public class OrganizationRequirementControlTestResultModelDAO implements PageableModelDAO<OrganizationRequirementControlTestResultDTO, ControlTestResultFilter> {

	private static final Map<String, String> SORT_MAPPING = Map.ofEntries(
		Map.entry("id", "octr.id"),
		Map.entry("assessmentWeight", "octr.assessmentWeight"),
		Map.entry("code", "sr.code"),
		Map.entry("securityControlFamily", "scf.name"),
		Map.entry("securityControlName", "scn.name"));

	@Autowired
	private AssessmentsRepository assessmentsRepository;

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<OrganizationRequirementControlTestResultDTO> getItemsPageable(ControlTestResultFilter filter, Pageable pageable, BaseSort sort) {

		Organizations organization = organizationService.getCurrentOrganizationEntity();
		Long organizationId = organization.getId();

		String codeFilter = Optional.ofNullable(filter.getName()).orElse("");

		Assessments assessment = null;

		if (filter.getAssessmentId() != null) {
			assessment = assessmentsRepository.findById(filter.getAssessmentId()).get();
		}

		// Define base hql data Query
		String hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.assessments.OrganizationRequirementControlTestResultDTO(octr, sr) FROM SecurityRequirements sr "
			+ " LEFT JOIN sr.securityControlName scn"
			+ " LEFT JOIN sr.securityControlFamily scf"
			+ " LEFT OUTER JOIN OrganizationRequirementControlTestResults octr ON (octr.securityRequirement=sr) ";

		// Define base hql count Query
		String hqlQueryCount = "SELECT count(distinct sr) FROM SecurityRequirements sr"
			+ " LEFT JOIN sr.securityControlName scn"
			+ " LEFT JOIN sr.securityControlFamily scf"
			+ " LEFT OUTER JOIN OrganizationRequirementControlTestResults octr ON (octr.securityRequirement=sr) ";

		if (assessment != null && assessment.getRelationToRequirementType() != null && assessment.getRelationToRequirementType().equals(RelationToRequirementType.REQUIREMENTS)) {
			hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.assessments.OrganizationRequirementControlTestResultDTO(octr, sr) FROM SecurityRequirements sr "
				+ " LEFT JOIN sr.securityControlName scn"
				+ " LEFT JOIN sr.securityControlFamily scf"
				+ " LEFT JOIN sr.assessments assm"
				+ " LEFT OUTER JOIN OrganizationRequirementControlTestResults octr ON (octr.securityRequirement=sr) ";

			hqlQueryCount = "SELECT count(distinct sr) FROM SecurityRequirements sr"
				+ " LEFT JOIN sr.securityControlName scn"
				+ " LEFT JOIN sr.securityControlFamily scf"
				+ " LEFT JOIN sr.assessments assm "
				+ " LEFT OUTER JOIN OrganizationRequirementControlTestResults octr ON (octr.securityRequirement=sr) ";

		} else if (assessment != null && assessment.getRelationToRequirementType() != null && assessment.getRelationToRequirementType().equals(RelationToRequirementType.FRAMEWORKS)) {
			hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.assessments.OrganizationRequirementControlTestResultDTO(octr, sr) FROM SecurityRequirements sr "
				+ " LEFT JOIN sr.securityControlName scn"
				+ " LEFT JOIN sr.securityControlFamily scf"
				+ " LEFT JOIN sr.controlSubcategories ctrlsc"
				+ " LEFT JOIN ctrlsc.assessmentType ctrlsc_asst "
				+ " LEFT JOIN Assessments assm ON ctrlsc_asst MEMBER OF assm.assessmentTypes"
				+ " LEFT OUTER JOIN OrganizationRequirementControlTestResults octr ON (octr.securityRequirement=sr) ";

			hqlQueryCount = "SELECT count(distinct sr) FROM SecurityRequirements sr"
				+ " LEFT JOIN sr.securityControlName scn"
				+ " LEFT JOIN sr.securityControlFamily scf"
				+ " LEFT JOIN sr.controlSubcategories ctrlsc"
				+ " LEFT JOIN ctrlsc.assessmentType ctrlsc_asst "
				+ " LEFT JOIN Assessments assm ON ctrlsc_asst MEMBER OF assm.assessmentTypes"
				+ " LEFT OUTER JOIN OrganizationRequirementControlTestResults octr ON (octr.securityRequirement=sr) ";
		}

		// Define base Group By clause
		String groupByString = " GROUP BY sr.id, octr.id, octr.assessmentWeight, sr.code, scn.name, scf.name";

		// Build Query String
		String whereString = " WHERE sr.organizationId = :organizationId AND sr.assessmentLevel.id = :assessmentLevelId ";

		if (StringUtils.isNotEmpty(codeFilter)) {
			// whereString += " AND UPPER(sr.code) LIKE (CONCAT(UPPER(:code), '%')) ";
			whereString += " AND (UPPER(sr.code) LIKE CONCAT('%', UPPER(:code), '%') OR UPPER(sr.description) LIKE CONCAT('%', UPPER(:code), '%'))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND octr.id NOT IN :excludeIds ";
		}
		if (assessment != null && assessment.getRelationToRequirementType() != null && !assessment.getRelationToRequirementType().equals(RelationToRequirementType.ALL_REQUIREMENTS)) {
			whereString += " AND assm.id = :assessmentId ";
		}

		/*
		if (organization.getAssessmentFrameworkLevel() != null && !AssessmentFrameworkLevel.NONE.equals(organization.getAssessmentFrameworkLevel())) {
			hqlQuery += " JOIN sr.securityRequirementLevels srl ";
			hqlQueryCount += " JOIN sr.securityRequirementLevels srl ";
			whereString += " AND srl.assessmentFrameworkLevel = :assessmentFrameworkLevel ";
		}
		*/

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString + groupByString;
		if (sort != null) {
			searchQueryString += sort.toOrderStringSafe(SORT_MAPPING);
		} else {
			searchQueryString += " ORDER BY sr.code ASC ";
		}

		// Build Query data
		TypedQuery<OrganizationRequirementControlTestResultDTO> typedQuery = entityManager.createQuery(searchQueryString, OrganizationRequirementControlTestResultDTO.class);
		applySearchFilterValues(filter, organization, typedQuery, assessment);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<OrganizationRequirementControlTestResultDTO> resultList = typedQuery.getResultList();

		// In GDPR here is going some magic with questions to articles

		// Calculate count query
		Query queryCount = entityManager.createQuery(hqlQueryCount + whereString);
		applySearchFilterValues(filter, organization, queryCount, assessment);
		Long resultCount = (Long) queryCount.getSingleResult();

		return new PagedResult<>(resultList, resultCount);
	}

	/**
	 * Apply query data
	 *
	 * @param filter
	 * @param organization
	 * @param query
	 */
	private void applySearchFilterValues(ControlTestResultFilter filter, Organizations organization, Query query, Assessments assessment) {
		String codeFilter = Optional.ofNullable(filter.getName()).orElse("");

		if (StringUtils.isNotEmpty(codeFilter)) query.setParameter("code", codeFilter);
		if (organization.getId() != null) query.setParameter("organizationId", organization.getId());
		/*
		if (organization.getAssessmentFrameworkLevel() != null && !AssessmentFrameworkLevel.NONE.equals(organization.getAssessmentFrameworkLevel())) {
			query.setParameter("assessmentFrameworkLevel", organization.getAssessmentFrameworkLevel());
		}
		*/
		if (CollectionUtils.isNotEmpty(filter.getExcludeIds())) query.setParameter("excludeIds", filter.getExcludeIds());
		if (assessment != null && assessment.getRelationToRequirementType() != null && !assessment.getRelationToRequirementType().equals(RelationToRequirementType.ALL_REQUIREMENTS)) {
			query.setParameter("assessmentId", assessment.getId());
		}
		query.setParameter("assessmentLevelId", AssessmentLevel.ORG.getId());
	}
}
