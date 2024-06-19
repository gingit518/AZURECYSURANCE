package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.DataTypeClassification;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DataTypeClassificationRepository extends CoreRepository<DataTypeClassification, Long> {

	Optional<DataTypeClassification> findById(Long id);

	@Query("SELECT dtc FROM DataTypeClassification dtc WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) AND UPPER(dtc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<DataTypeClassification> getAllByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT dtc FROM DataTypeClassification dtc " +
		"LEFT JOIN FETCH dtc.createdBy LEFT JOIN FETCH dtc.updatedBy " +
		"WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) " +
		"AND (UPPER(dtc.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(dtc.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<DataTypeClassification> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(dtc) FROM DataTypeClassification dtc " +
		"WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) " +
		"AND (UPPER(dtc.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(dtc.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT dtc FROM DataTypeClassification dtc LEFT JOIN FETCH dtc.createdBy LEFT JOIN FETCH dtc.updatedBy " +
		"WHERE UPPER(dtc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<DataTypeClassification> getListByName(
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(dtc) FROM DataTypeClassification dtc WHERE UPPER(dtc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByName(
		@Param("name") String name
	);

	@Query("SELECT dtc FROM DataTypeClassification dtc LEFT JOIN FETCH dtc.createdBy LEFT JOIN FETCH dtc.updatedBy " +
		"WHERE UPPER(dtc.name) LIKE (CONCAT(UPPER(:name), '%')) AND dtc.organizationId IS NULL")
	List<DataTypeClassification> getListByNameOnlyGlobal(
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(dtc) FROM DataTypeClassification dtc WHERE UPPER(dtc.name) LIKE (CONCAT(UPPER(:name), '%')) AND dtc.organizationId IS NULL")
	Long getCountByNameOnlyGlobal(
		@Param("name") String name
	);

	Optional<DataTypeClassification> findByNameAndOrganizationIdIsNull(String name);

	@Query("SELECT dtc FROM DataTypeClassification dtc WHERE dtc.organizationId = :organizationId AND dtc.name=:name")
	Optional<DataTypeClassification> getFirstByNameForOrganization(
		@Param("name") String name,
		@Param("organizationId") Long organizationId
	);


	@Query("select dtc from DataTypeClassification dtc join dtc.fieldClassifiers fc join fc.metadata fcm " +
		"where fc.name = :fcname and dtc.organizationId = :organizationId " +
		"and fcm.key = :metadataKey and fcm.value = :metadataValue")
	Set<DataTypeClassification> getDtcListByFieldClassifierNameAndOrganizationAndMeta(@Param("fcname") String fieldClassifierName,
																					  @Param("organizationId") Long organizationId,
																					  @Param("metadataKey") String metadataKey,
																					  @Param("metadataValue") String metadataValue
	);

	@Query("SELECT dtc FROM DataTypeClassification dtc join dtc.metadata meta WHERE dtc.organizationId = :organizationId and meta.key = :metadataKey and meta.value = :metadataValue")
	Optional<DataTypeClassification> findByOrganizationAndMeta(
		@Param("organizationId") Long organizationId,
		@Param("metadataKey") String metadataKey,
		@Param("metadataValue") String metadataValue
	);

	Optional<DataTypeClassification> findFirstByNameIgnoreCase(String name);

}
