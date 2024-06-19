package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.model.jpa.entity.Technologies;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyClassTypes;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TechnologyRepository extends CoreRepository<Technologies, Long> {

	Optional<Technologies> findById(Long id);

	List<Technologies> findAllByVendor(Organizations vendor);

	@Query("SELECT t FROM Technologies t WHERE (t.organizationId = :organizationId OR t.organizationId IS NULL) AND UPPER(t.name) = UPPER(:name)")
	Optional<Technologies> getFirstByNameAndOrganization(@Param("name") String name, @Param("organizationId") Long organizationId);

	@Query("SELECT tc FROM Technologies tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND tc.technologyClassTypeId = :technologyClassTypeId AND UPPER(tc.name) = UPPER(:name)")
	Optional<Technologies> getFirstByNameAndOrganizationAndParent(@Param("name") String name, @Param("organizationId") Long organizationId, @Param("technologyClassTypeId") Long technologyClassTypeId);

	@Query("SELECT t FROM Technologies t LEFT JOIN FETCH t.technologyCategory tc " +
		"LEFT JOIN FETCH t.createdBy LEFT JOIN FETCH t.updatedBy LEFT JOIN FETCH t.vendor " +
		"WHERE t.organizationId = :organizationId AND (UPPER(t.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(t.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	List<Technologies> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT t FROM Technologies t WHERE t.organizationId = :organizationId AND t.name in :names")
	Set<Technologies> getItemsByOrganizationAndNames(
		@Param("organizationId") Long organizationId,
		@Param("names") List<String> names
	);

	@Query("SELECT count(t) FROM Technologies t " +
		"WHERE t.organizationId = :organizationId AND (UPPER(t.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(t.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT DISTINCT sys FROM Systems sys LEFT JOIN FETCH sys.owner ow LEFT JOIN FETCH sys.technologies cst " +
		"LEFT JOIN FETCH sys.businessUnit bu LEFT JOIN FETCH sys.dataAssetClassification dac " +
		"WHERE sys.organizationId = :organizationId AND sys.technologies IS NOT EMPTY ORDER BY sys.name ASC")
	Set<Systems> getSystemsByOrganizationForCyberSecurity(
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT t FROM Technologies t join t.metadata meta WHERE t.organizationId = :organizationId and meta.key = :metadataKey and meta.value = :metadataValue ")
	Optional<Technologies> findByOrganizationAndMeta(@Param("organizationId") Long organizationId,
													 @Param("metadataKey") String metadataKey,
													 @Param("metadataValue") String metadataValue);


}
