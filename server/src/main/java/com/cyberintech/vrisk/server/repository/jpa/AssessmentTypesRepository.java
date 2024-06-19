package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentTypes;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentTypesRepository extends CoreRepository<AssessmentTypes, Long> {

	Optional<AssessmentTypes> findById(Long id);

	@Query("SELECT at FROM AssessmentTypes at " +
		"WHERE UPPER(at.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<AssessmentTypes> getListByName(
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(at) FROM AssessmentTypes at " +
		"WHERE UPPER(at.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(
		@Param("name") String name
	);

	@Query("SELECT ast FROM AssessmentTypes ast " +
		// "LEFT JOIN FETCH ast.createdBy LEFT JOIN FETCH ast.updatedBy " +
		"WHERE ast.organizationId = :organizationId AND ast.id NOT IN :excludeIds " +
		"AND (UPPER(ast.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(ast.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<AssessmentTypes> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		@Param("excludeIds") Collection<Long> excludeIds,
		Pageable pageable
	);

	@Query("SELECT count(ast) FROM AssessmentTypes ast " +
		"WHERE ast.organizationId = :organizationId AND ast.id NOT IN :excludeIds " +
		"AND (UPPER(ast.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(ast.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		@Param("excludeIds") Collection<Long> excludeIds
	);

	Optional<AssessmentTypes> findFirstByNameIgnoreCaseAndOrganizationId(String name, Long organizationId);

}
