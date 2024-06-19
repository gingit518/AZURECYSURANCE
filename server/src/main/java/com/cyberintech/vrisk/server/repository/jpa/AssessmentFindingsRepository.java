package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentFindings;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentFindingsRepository extends CoreRepository<AssessmentFindings, Long> {

	Optional<AssessmentFindings> findById(Long id);

	@Query("SELECT ast FROM AssessmentFindings ast " +
		"LEFT JOIN FETCH ast.technologyCategory LEFT JOIN FETCH ast.controlSubcategory csc " +
		"LEFT JOIN FETCH csc.controlCategory ccc LEFT JOIN FETCH ccc.controlFunction " +
		"LEFT JOIN FETCH ast.createdBy LEFT JOIN FETCH ast.updatedBy " +
		"LEFT JOIN FETCH ast.securityRequirements LEFT JOIN FETCH ast.assessments " +
		"WHERE ast.organizationId = :organizationId AND UPPER(ast.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	List<AssessmentFindings> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT distinct ast FROM AssessmentFindings ast LEFT JOIN FETCH ast.assessments ass LEFT JOIN FETCH ass.systems s " +
		"WHERE ast.organizationId = :organizationId ")
	List<AssessmentFindings> getAllListByOrganization(@Param("organizationId") Long organizationId);

	@Query("SELECT distinct ast FROM AssessmentFindings ast LEFT JOIN FETCH ast.assessments ass LEFT JOIN FETCH ass.systems s " +
		"WHERE ast.organizationId = :organizationId AND s.id IN :systemIds")
	List<AssessmentFindings> getAllListByOrganizationAndSystems(@Param("organizationId") Long organizationId, @Param("systemIds") List<Long> systemIds);

	@Query("SELECT count(ast) FROM AssessmentFindings ast " +
		"WHERE ast.organizationId = :organizationId AND UPPER(ast.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT distinct ast FROM AssessmentFindings ast LEFT JOIN FETCH ast.assessments ass LEFT JOIN FETCH ass.systems s " +
		"WHERE ast.organizationId = :organizationId AND ast.isGDPR = :isGDPR")
	List<AssessmentFindings> getListByOrganizationAndIsGDPR(@Param("organizationId") Long organizationId, @Param("isGDPR") Boolean isGDPR);

}
