package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyClassTypes;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnologyClassTypeRepository extends CoreRepository<TechnologyClassTypes, Long> {

	Optional<TechnologyClassTypes> findById(Long id);

	@Query("SELECT tc FROM TechnologyClassTypes tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND tc.subcategoryId = :subcategoryId AND UPPER(tc.name) = UPPER(:name)")
	Optional<TechnologyClassTypes> getFirstByNameAndOrganizationAndParent(@Param("name") String name, @Param("organizationId") Long organizationId, @Param("subcategoryId") Long subcategoryId);

	@Query("SELECT tc FROM TechnologyClassTypes tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND tc.subcategoryId = :subcategoryId " +
		" AND (UPPER(tc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(tc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	List<TechnologyClassTypes> getListByNameAndOrganizationAndParent(@Param("organizationId") Long organizationId, @Param("subcategoryId") Long subcategoryId, @Param("name") String name, Pageable pageable);

	@Query("SELECT count(tc) FROM TechnologyClassTypes tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND tc.subcategoryId = :subcategoryId " +
		" AND (UPPER(tc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(tc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	Long getCountByNameAndOrganizationAndParent(@Param("organizationId") Long organizationId, @Param("subcategoryId") Long subcategoryId, @Param("name") String name);

	Optional<TechnologyClassTypes> findFirstByNameAndCategoryId(String name, Long categoryId);

	Optional<TechnologyClassTypes> findFirstByNameAndSubcategoryId(String name, Long categoryId);

}
