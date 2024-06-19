package com.cyberintech.vrisk.server.model.dao;

import com.cyberintech.vrisk.server.model.data.BaseSort;
import com.cyberintech.vrisk.server.model.data.ControlTestResultFilter;
import com.cyberintech.vrisk.server.model.dto.assessments.SystemRequirementControlTestResultDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentFrameworkLevel;
import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentLevel;
import com.cyberintech.vrisk.server.model.jpa.domains.RelationToRequirementType;
import com.cyberintech.vrisk.server.model.jpa.entity.Assessments;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.service.AssessmentService;
import com.cyberintech.vrisk.server.service.OrganizationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * System Requirement Control Test Result DAO Model
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.01.30
 */
@Service
public class SystemRequirementControlTestResultModelDAO implements PageableModelDAO<SystemRequirementControlTestResultDTO, ControlTestResultFilter> {

	@Autowired
	private AssessmentService assessmentService;

	@Autowired
	private OrganizationService organizationService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PagedResult<SystemRequirementControlTestResultDTO> getItemsPageable(ControlTestResultFilter filter, Pageable pageable, BaseSort sort) {

		Organizations organization = organizationService.getCurrentOrganizationEntity();
		Long organizationId = organization.getId();

		String codeFilter = Optional.ofNullable(filter.getName()).orElse("");

		Assessments assessment = null;

		if (filter.getSystemId() == null) {
			throw new BadRequestException("System id is required", ApplicationExceptionCodes.SYSTEM_REQUIRED);
		}

		if (filter.getAssessmentId() != null) {
			assessment = assessmentService.getAssessmentForCurrentOrganization(filter.getAssessmentId());
		}

		Boolean isControlMaturityDefined = true;
		String hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.assessments.SystemRequirementControlTestResultDTO(sctr, sr) FROM SecurityRequirements sr " +
			" LEFT JOIN sr.securityControlName scn LEFT JOIN scn.securityControlFamily scf LEFT OUTER JOIN SystemRequirementControlTestResults sctr ON (sctr.securityRequirement=sr AND sctr.system.id = :systemId) " +
			" LEFT JOIN sctr.controlMaturity controlMaturity ";

		// Define base hql count Query
		String hqlQueryCount = "SELECT count(distinct sr) FROM SecurityRequirements sr LEFT JOIN sr.securityControlName scn LEFT JOIN scn.securityControlFamily scf " +
			" LEFT OUTER JOIN SystemRequirementControlTestResults sctr ON (sctr.securityRequirement = sr AND sctr.system.id = :systemId) ";

		if (assessment != null && assessment.getRelationToRequirementType() != null && assessment.getRelationToRequirementType().equals(RelationToRequirementType.REQUIREMENTS)) {
			hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.assessments.SystemRequirementControlTestResultDTO(sctr, sr) FROM SecurityRequirements sr " +
				" LEFT JOIN sr.securityControlName scn LEFT JOIN scn.securityControlFamily scf LEFT JOIN sr.assessments assm LEFT OUTER JOIN SystemRequirementControlTestResults sctr ON (sctr.securityRequirement=sr AND sctr.system.id = :systemId) ";

			hqlQueryCount = "SELECT count(distinct sr) FROM SecurityRequirements sr LEFT JOIN sr.securityControlName scn LEFT JOIN scn.securityControlFamily scf LEFT JOIN sr.assessments assm " +
				" LEFT OUTER JOIN SystemRequirementControlTestResults sctr ON (sctr.securityRequirement = sr AND sctr.system.id = :systemId) ";

			isControlMaturityDefined = false;

		} else if (assessment != null && assessment.getRelationToRequirementType() != null && assessment.getRelationToRequirementType().equals(RelationToRequirementType.FRAMEWORKS)) {
			hqlQuery = "SELECT new com.cyberintech.vrisk.server.model.dto.assessments.SystemRequirementControlTestResultDTO(sctr, sr) FROM SecurityRequirements sr " +
				" LEFT JOIN sr.securityControlName scn LEFT JOIN scn.securityControlFamily scf LEFT JOIN sr.controlSubcategories ctrlsc LEFT JOIN ctrlsc.assessmentType ctrlsc_asst " +
				" LEFT JOIN Assessments assm ON ctrlsc_asst MEMBER OF assm.assessmentTypes LEFT OUTER JOIN SystemRequirementControlTestResults sctr ON (sctr.securityRequirement=sr AND sctr.system.id = :systemId) ";

			hqlQueryCount = "SELECT count(distinct sr) FROM SecurityRequirements sr LEFT JOIN sr.securityControlName scn LEFT JOIN scn.securityControlFamily scf LEFT JOIN sr.controlSubcategories ctrlsc LEFT JOIN ctrlsc.assessmentType ctrlsc_asst " +
				" LEFT JOIN Assessments assm ON ctrlsc_asst MEMBER OF assm.assessmentTypes LEFT OUTER JOIN SystemRequirementControlTestResults sctr ON (sctr.securityRequirement=sr AND sctr.system.id = :systemId) ";

			isControlMaturityDefined = false;
		}

		// Define base Group By clause
		String groupByString = " GROUP BY sr.id, sctr.id, sctr.assessmentWeight, sr.code, scn.name, scf.name";

		// Build Query String
		String whereString = " WHERE sr.organizationId = :organizationId AND sr.assessmentLevel.id = :assessmentLevelId ";

		if (StringUtils.isNotEmpty(codeFilter)) {
			// whereString += " AND UPPER(sr.code) LIKE (CONCAT(UPPER(:code), '%')) ";
			whereString += " AND (UPPER(sr.code) LIKE CONCAT('%', UPPER(:code), '%') OR UPPER(sr.description) LIKE CONCAT('%', UPPER(:code), '%'))";
		}
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) {
			whereString += " AND sctr.id NOT IN :excludeIds ";
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

		Map<String, String> sortMapping = new HashMap<>();
		sortMapping.put("id", "sctr.id");
		sortMapping.put("assessmentWeight", "sctr.assessmentWeight");
		sortMapping.put("code", "sr.code");
		sortMapping.put("securityControlName", "scn.name");
		sortMapping.put("securityControlFamily", "scf.name");

		if (isControlMaturityDefined) {
			sortMapping.put("controlMaturity", "controlMaturity.weight");
			groupByString += ", controlMaturity.weight";
		} else {
			sortMapping.put("controlMaturity", "sctr.assessmentWeight");
		}

		// Build Sort based on the mapping
		String searchQueryString = hqlQuery + whereString + groupByString;
		if (sort != null) {
			searchQueryString += sort.toOrderString(sortMapping);
		} else {
			searchQueryString += " ORDER BY sr.code ASC ";
		}

		// Build Query data
		TypedQuery<SystemRequirementControlTestResultDTO> typedQuery = entityManager.createQuery(searchQueryString, SystemRequirementControlTestResultDTO.class);
		applySearchFilterValues(filter, organization, typedQuery, assessment);
		typedQuery.setMaxResults(pageable.getPageSize());
		typedQuery.setFirstResult((int) pageable.getOffset());
		List<SystemRequirementControlTestResultDTO> resultList = typedQuery.getResultList();

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
		if (filter.getSystemId() != null) query.setParameter("systemId", filter.getSystemId());
		if (filter.getExcludeIds() != null && filter.getExcludeIds().size() > 0) query.setParameter("excludeIds", filter.getExcludeIds());
		if (assessment != null && assessment.getRelationToRequirementType() != null && !assessment.getRelationToRequirementType().equals(RelationToRequirementType.ALL_REQUIREMENTS)) {
			query.setParameter("assessmentId", assessment.getId());
		}
		query.setParameter("assessmentLevelId", AssessmentLevel.SYSTEM.getId());
	}
}
