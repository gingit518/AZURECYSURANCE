package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentTypes;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlCategories;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlCategories;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlFunctions;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ControlCategoriesRepository extends CoreRepository<ControlCategories, Long> {

	Optional<ControlCategories> findById(Long id);

	@Query("SELECT cc FROM ControlCategories cc JOIN FETCH cc.controlFunction AS cf " +
		"WHERE cc.organizationId = :organizationId " +
		"AND (UPPER(cc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(cc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	List<ControlCategories> getListByName(
		@Param("name") String name,
		@Param("organizationId") Long organizationId,
		Pageable pageable
	);

	@Query("SELECT count(cc) FROM ControlCategories cc JOIN cc.controlFunction AS cf " +
		"WHERE cc.organizationId = :organizationId " +
		"AND (UPPER(cc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(cc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	Long getCountByName(
		@Param("name") String name,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT cc FROM ControlCategories cc JOIN FETCH cc.controlFunction AS cf " +
		"WHERE cc.organizationId = :organizationId AND cf.id = :controlFunctionId AND UPPER(cc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<ControlCategories> getListByControlFunctionIdAndName(
		@Param("controlFunctionId") Long controlFunctionId,
		@Param("name") String name,
		@Param("organizationId") Long organizationId,
		Pageable pageable
	);

	@Query("SELECT count(cc) FROM ControlCategories cc JOIN cc.controlFunction AS cf " +
		"WHERE cc.organizationId = :organizationId AND cf.id = :controlFunctionId AND UPPER(cc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByControlFunctionIdAndName(
		@Param("controlFunctionId") Long controlFunctionId,
		@Param("name") String name,
		@Param("organizationId") Long organizationId
	);

	Optional<ControlCategories> findFirstByNameIgnoreCaseAndControlFunction(String name, ControlFunctions controlFunction);

	Optional<ControlCategories> findFirstByCodeIgnoreCaseAndControlFunction(String code, ControlFunctions controlFunction);

	Optional<ControlCategories> findFirstByNameIgnoreCaseAndAssessmentType(String name, AssessmentTypes assessmentType);

	Optional<ControlCategories> findFirstByCodeIgnoreCaseAndAssessmentType(String code, AssessmentTypes assessmentType);

}
