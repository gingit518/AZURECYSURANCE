package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TechnologyCategoryRepository extends CoreRepository<TechnologyCategories, Long> {

	Optional<TechnologyCategories> findById(Long id);

	@Query("SELECT tc FROM TechnologyCategories tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND UPPER(tc.name) = UPPER(:name)")
	Optional<TechnologyCategories> getFirstByNameAndOrganization(@Param("name") String name, @Param("organizationId") Long organizationId);

	@Query("SELECT tc FROM TechnologyCategories tc " +
		"LEFT JOIN FETCH tc.createdBy LEFT JOIN FETCH tc.updatedBy " +
		"WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) " +
		"AND (UPPER(tc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(tc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	List<TechnologyCategories> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(tc) FROM TechnologyCategories tc " +
		"WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) " +
		"AND (UPPER(tc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(tc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT tc FROM TechnologyCategories tc LEFT JOIN FETCH tc.createdBy LEFT JOIN FETCH tc.updatedBy " +
		"WHERE UPPER(tc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<TechnologyCategories> getListByName(
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(tc) FROM TechnologyCategories tc WHERE UPPER(tc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(
		@Param("name") String name
	);

	Optional<TechnologyCategories> findFirstByNameAndIdIsNotIn(String name, Collection<Long> excludeIds);

	Optional<TechnologyCategories> findFirstByNameAndIdIsNotInAndOrganizationIdIsNull(String name, Collection<Long> excludeIds);

	Optional<TechnologyCategories> findFirstByNameAndIdIsNotInAndOrganizationId(String name, Collection<Long> excludeIds, Long organizationId);

	@Query("SELECT tc FROM TechnologyCategories tc join tc.metadata meta WHERE tc.organizationId = :organizationId and meta.key = :metadataKey and meta.value = :metadataValue")
	Optional<TechnologyCategories> findByOrganizationAndMeta(@Param("organizationId") Long organizationId,
															 @Param("metadataKey") String metadataKey,
															 @Param("metadataValue") String metadataValue);

	Optional<TechnologyCategories> findFirstByNameIgnoreCase(String name);

}
