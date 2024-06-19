package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentWeights;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentWeights;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentWeightsRepository extends CoreRepository<AssessmentWeights, Long> {

	Optional<AssessmentWeights> findById(Long id);

	@Query("SELECT ast FROM AssessmentWeights ast " +
		"WHERE ast.organizationId = :organizationId AND UPPER(ast.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<AssessmentWeights> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(ast) FROM AssessmentWeights ast " +
		"WHERE ast.organizationId = :organizationId AND UPPER(ast.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT ast FROM AssessmentWeights ast " +
		"WHERE ast.organizationId = :organizationId AND ast.controlSubcategory.id = :controlSubcategoryId")
	List<AssessmentWeights> getListByOrganizationAndSubcategory(
		@Param("organizationId") Long organizationId,
		@Param("controlSubcategoryId") Long controlSubcategoryId,
		Pageable pageable
	);

	@Query("SELECT count(ast) FROM AssessmentWeights ast " +
		"WHERE ast.organizationId = :organizationId AND ast.controlSubcategory.id = :controlSubcategoryId")
	Long getCountByOrganizationAAndSubcategory(
		@Param("organizationId") Long organizationId,
		@Param("controlSubcategoryId") Long controlSubcategoryId
	);

}
