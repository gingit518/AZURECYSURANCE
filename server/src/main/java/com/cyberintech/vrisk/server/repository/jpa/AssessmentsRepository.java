package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Assessments;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentsRepository extends CoreRepository<Assessments, Long> {

	Optional<Assessments> findById(Long id);

	@Query("SELECT DISTINCT ast FROM Assessments ast " +
		"LEFT JOIN FETCH ast.assessmentLevel LEFT JOIN FETCH ast.assessmentType " +
		"LEFT JOIN FETCH ast.technologyCategories LEFT JOIN FETCH ast.systems " +
		"LEFT JOIN FETCH ast.securityRequirements " +
		"WHERE ast.organizationId = :organizationId")
	List<Assessments> getListByOrOrganizationId(@Param("organizationId") Long organizationId);

}
