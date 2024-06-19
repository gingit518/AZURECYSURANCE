package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologySubcategories;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TechnologySubcategoryRepository extends CoreRepository<TechnologySubcategories, Long> {

	Optional<TechnologySubcategories> findById(Long id);

	@Query("SELECT tc FROM TechnologySubcategories tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND tc.categoryId = :categoryId AND UPPER(tc.name) = UPPER(:name)")
	Optional<TechnologySubcategories> getFirstByNameAndOrganizationAndParent(@Param("name") String name, @Param("organizationId") Long organizationId, @Param("categoryId") Long categoryId);

	@Query("SELECT tc FROM TechnologySubcategories tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND tc.categoryId = :categoryId " +
		" AND (UPPER(tc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(tc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	List<TechnologySubcategories> getListByNameAndOrganizationAndParent(@Param("organizationId") Long organizationId, @Param("categoryId") Long categoryId, @Param("name") String name, Pageable pageable);

	@Query("SELECT count(tc) FROM TechnologySubcategories tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND tc.categoryId = :categoryId " +
		" AND (UPPER(tc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(tc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	Long getCountByNameAndOrganizationAndParent(@Param("organizationId") Long organizationId, @Param("categoryId") Long categoryId, @Param("name") String name);

	Optional<TechnologyCategories> findFirstByNameAndCategoryId(String name, Long categoryId);

}
