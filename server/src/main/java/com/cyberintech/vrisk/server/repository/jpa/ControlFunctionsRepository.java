package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentTypes;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlFunctions;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlFunctions;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ControlFunctionsRepository extends CoreRepository<ControlFunctions, Long> {

	Optional<ControlFunctions> findById(Long id);

	@Query("SELECT cf FROM ControlFunctions cf WHERE cf.organizationId = :organizationId AND cf.id NOT IN :excludeIds " +
		"AND (UPPER(cf.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(cf.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	List<ControlFunctions> getListByNameForOrganization(@Param("name") String name, @Param("organizationId") Long organizationId, @Param("excludeIds") Collection<Long> excludeIds, Pageable pageable);

	@Query("SELECT count(cf) FROM ControlFunctions cf WHERE cf.organizationId = :organizationId AND cf.id NOT IN :excludeIds " +
		"AND (UPPER(cf.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(cf.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	Long getCountByNameForOrganization(@Param("name") String name, @Param("organizationId") Long organizationId, @Param("excludeIds") Collection<Long> excludeIds);

	@Query("SELECT cf FROM ControlFunctions cf WHERE cf.organizationId = :organizationId AND cf.assessmentType.id = :parentId AND cf.id NOT IN :excludeIds " +
		"AND (UPPER(cf.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(cf.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	List<ControlFunctions> getListByNameAndParentForOrganization(@Param("name") String name, @Param("parentId") Long parentId, @Param("organizationId") Long organizationId, @Param("excludeIds") Collection<Long> excludeIds, Pageable pageable);

	@Query("SELECT count(cf) FROM ControlFunctions cf WHERE cf.organizationId = :organizationId AND cf.assessmentType.id = :parentId AND cf.id NOT IN :excludeIds " +
		"AND (UPPER(cf.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(cf.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	Long getCountByNameAndParentForOrganization(@Param("name") String name, @Param("parentId") Long parentId, @Param("organizationId") Long organizationId, @Param("excludeIds") Collection<Long> excludeIds);

	@Query("SELECT cf FROM ControlFunctions cf WHERE UPPER(cf.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<ControlFunctions> getListByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT count(cf) FROM ControlFunctions cf WHERE UPPER(cf.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(@Param("name") String name);

	Optional<ControlFunctions> findFirstByNameIgnoreCaseAndAssessmentType(String name, AssessmentTypes assessmentType);

	Optional<ControlFunctions> findFirstByCodeIgnoreCaseAndAssessmentType(String code, AssessmentTypes assessmentType);

}
