package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessUnitRepository extends CoreRepository<BusinessUnits, Long> {

	Optional<BusinessUnits> findById(Long id);

	Optional<BusinessUnits> findByNameAndOrganizationId(String name, Long organizationId);

	@Query("SELECT bm FROM BusinessUnits bm LEFT JOIN FETCH bm.owner ow LEFT JOIN FETCH bm.infosecFocalPerson ip " +
		"LEFT JOIN FETCH bm.createdBy LEFT JOIN FETCH bm.updatedBy " +
		"WHERE bm.organizationId = :organizationId AND UPPER(bm.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<BusinessUnits> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT bm FROM BusinessUnits bm JOIN bm.parent bmp WHERE bm.organizationId = :organizationId AND bm.name = :name AND bmp.name = :parentName")
	Optional<BusinessUnits> getByOrganizationAndNameAndParentName(
		@Param("name") String name,
		@Param("parentName") String parentName,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT distinct bm FROM BusinessUnits bm JOIN bm.parent bmp WHERE bm.organizationId = :organizationId AND bm.name = :name AND bmp.id = :parentId")
	Optional<BusinessUnits> getByOrganizationAndNameAndParentId(
		@Param("name") String name,
		@Param("parentId") Long parentId,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT distinct bm FROM BusinessUnits bm LEFT JOIN bm.parent bmp WHERE bm.organizationId = :organizationId AND bm.name = :name AND bmp.id IS NULL")
	Optional<BusinessUnits> getByOrganizationAndNameAndNoParent(
		@Param("name") String name,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT count(bm) FROM BusinessUnits bm " +
		"WHERE bm.organizationId = :organizationId AND UPPER(bm.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	// Helpers for Business Unit Levels flow
	List<BusinessUnits> getAllByParentNull();

	List<BusinessUnits> getAllByParentNullAndOrganizationId(Long organizationId);

	// child list
	@Query("SELECT bu FROM BusinessUnits bu JOIN FETCH bu.parent bup " +
		"WHERE bup.id = :parentId")
	List<BusinessUnits> getAllByParentId(Long parentId);

}
