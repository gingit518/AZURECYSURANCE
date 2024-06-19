package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentTypes;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlCategories;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlSubcategories;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ControlSubcategoriesRepository extends CoreRepository<ControlSubcategories, Long> {

	Optional<ControlSubcategories> findById(Long id);

	@Query("SELECT cs FROM ControlSubcategories cs JOIN cs.controlCategory AS cc " +
		"WHERE cc.organizationId = :organizationId " +
		"AND (UPPER(cs.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR UPPER(cs.description) LIKE (CONCAT('%', UPPER(:name), '%')) OR UPPER(cs.code) LIKE (CONCAT('%', UPPER(:name), '%')))")
	List<ControlSubcategories> getListByName(
		@Param("name") String name,
		@Param("organizationId") Long organizationId,
		Pageable pageable
	);

	@Query("SELECT count(cs) FROM ControlSubcategories cs JOIN cs.controlCategory AS cc " +
		"WHERE cc.organizationId = :organizationId " +
		"AND (UPPER(cs.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR UPPER(cs.description) LIKE (CONCAT('%', UPPER(:name), '%')) OR UPPER(cs.code) LIKE (CONCAT('%', UPPER(:name), '%')))")
	Long getCountByName(
		@Param("name") String name,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT cs FROM ControlSubcategories cs JOIN cs.controlCategory AS cc " +
		"WHERE cc.organizationId = :organizationId AND cc.id = :controlCategoryId AND (UPPER(cs.name) LIKE (CONCAT(UPPER(:name), '%')) OR UPPER(cs.code) LIKE (CONCAT(UPPER(:name), '%')))")
	List<ControlSubcategories> getListByControlCategoryIdAndName(
		@Param("controlCategoryId") Long controlCategoryId,
		@Param("name") String name,
		@Param("organizationId") Long organizationId,
		Pageable pageable
	);

	@Query("SELECT count(cs) FROM ControlSubcategories cs JOIN cs.controlCategory AS cc " +
		"WHERE cc.organizationId = :organizationId AND cc.id = :controlCategoryId AND (UPPER(cs.name) LIKE (CONCAT(UPPER(:name), '%')) OR UPPER(cs.code) LIKE (CONCAT(UPPER(:name), '%')))")
	Long getCountByControlCategoryIdAndName(
		@Param("controlCategoryId") Long controlCategoryId,
		@Param("name") String name,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT cs FROM ControlSubcategories cs JOIN FETCH cs.controlCategory cc JOIN FETCH cs.assessmentType ast " +
		"JOIN FETCH cc.controlFunction cf WHERE cc.organizationId = :organizationId AND ast.id = :assessmentTypeId " +
		"ORDER BY cf.code ASC, cf.id ASC, cc.code ASC, cc.id ASC, cs.code ASC")
	Set<ControlSubcategories> findAllByOrganizationIdAndAssessmentType(
		@Param("organizationId") Long organizationId,
		@Param("assessmentTypeId") Long assessmentTypeId
	);

	List<ControlSubcategories> findAllByControlCategory(ControlCategories controlCategory);

	Optional<ControlSubcategories> findFirstByNameIgnoreCaseAndControlCategory(String name, ControlCategories controlCategory);

	Optional<ControlSubcategories> findFirstByCodeIgnoreCaseAndOrganizationId(String code, Long organizationId);

	Optional<ControlSubcategories> findFirstByNameIgnoreCaseAndOrganizationId(String name, Long organizationId);

	Optional<ControlSubcategories> findFirstByNameIgnoreCaseAndAssessmentType(String name, AssessmentTypes assessmentType);

	Optional<ControlSubcategories> findFirstByCodeIgnoreCaseAndAssessmentType(String code, AssessmentTypes assessmentType);

}
