package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.DataAssetClassification;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataAssetClassificationRepository extends CoreRepository<DataAssetClassification, Long> {

	Optional<DataAssetClassification> findById(Long id);

	@Query("SELECT dac FROM DataAssetClassification dac " +
		"WHERE (dac.organizationId = :organizationId OR dac.organizationId IS NULL) AND dac.name=:name")
	Optional<DataAssetClassification> getFirstByNameForOrganization(
		@Param("name") String name,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT dac FROM DataAssetClassification dac " +
		"LEFT JOIN FETCH dac.createdBy LEFT JOIN FETCH dac.updatedBy " +
		"WHERE (dac.organizationId = :organizationId OR dac.organizationId IS NULL) " +
		"AND (UPPER(dac.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(dac.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	List<DataAssetClassification> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(dac) FROM DataAssetClassification dac " +
		"WHERE (dac.organizationId = :organizationId OR dac.organizationId IS NULL) " +
		"AND (UPPER(dac.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(dac.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT dac FROM DataAssetClassification dac join dac.metadata meta WHERE dac.name = :name and dac.organizationId = :organizationId and meta.key = :metadataKey and meta.value = :metadataValue")
	Optional<DataAssetClassification> findByNameAndOrganizationAndMeta(
		@Param("name") String name,
		@Param("organizationId") Long organizationId,
		@Param("metadataKey") String metadataKey,
		@Param("metadataValue") String metadataValue
	);

}
