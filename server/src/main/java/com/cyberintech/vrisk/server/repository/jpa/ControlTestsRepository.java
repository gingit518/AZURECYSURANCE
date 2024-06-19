package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.ControlTests;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlTests;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ControlTestsRepository extends CoreRepository<ControlTests, Long> {

	Optional<ControlTests> findById(Long id);

	@Query("SELECT ct FROM ControlTests ct JOIN ct.controlSubcategory cs " +
		"WHERE ct.organizationId = :organizationId AND cs.id=:subcategoryId")
	ControlTests getItemBySubcategoryIdAndOrganizationId(@Param("subcategoryId") Long subcategoryId, @Param("organizationId") Long organizationId);

	@Query("SELECT ct FROM ControlTests ct " +
		"LEFT JOIN FETCH ct.createdBy LEFT JOIN FETCH ct.updatedBy " +
		"WHERE ct.organizationId = :organizationId " +
		"AND (UPPER(ct.assessmentType.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR UPPER(ct.assessmentType.description) LIKE (CONCAT('%', UPPER(:name), '%')))")
	List<ControlTests> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(ct) FROM ControlTests ct " +
		"WHERE ct.organizationId = :organizationId AND UPPER(ct.assessmentType.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

}
