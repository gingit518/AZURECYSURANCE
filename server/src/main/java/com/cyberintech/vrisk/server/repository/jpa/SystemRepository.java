package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import com.cyberintech.vrisk.server.model.jpa.entity.CybersecurityTools;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemRepository extends CoreRepository<Systems, Long> {

	Optional<Systems> findById(Long id);

	@Query("SELECT sys FROM Systems sys WHERE sys.organizationId = :organizationId AND sys.name=:name")
	Optional<Systems> getFirstByNameForOrganization(
		@Param("name") String name,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sys FROM Systems sys LEFT JOIN FETCH sys.owner ow LEFT JOIN FETCH sys.infosecFocalPerson ip " +
		"LEFT JOIN FETCH sys.businessUnit bu LEFT JOIN FETCH sys.dataAssetClassification dac " +
		"WHERE sys.organizationId = :organizationId")
	List<Systems> getAllByOrganization(
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sys FROM Systems sys LEFT JOIN FETCH sys.owner ow LEFT JOIN FETCH sys.infosecFocalPerson ip " +
		"LEFT JOIN FETCH sys.businessUnit bu LEFT JOIN FETCH sys.dataAssetClassification dac " +
		"WHERE sys.organizationId = :organizationId ORDER BY sys.name")
	List<Systems> getAllByOrganizationOrderedByName(
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sys FROM Systems sys LEFT JOIN FETCH sys.owner ow LEFT JOIN FETCH sys.infosecFocalPerson ip " +
		"LEFT JOIN FETCH sys.businessUnit bu LEFT JOIN FETCH sys.dataAssetClassification dac " +
		"WHERE sys.organizationId = :organizationId AND sys.isEtl != true ORDER BY sys.name ASC")
	List<Systems> getAllByOrganizationAndNotEtl(
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sys FROM Systems sys LEFT JOIN FETCH sys.owner ow " +
		"WHERE ow.id=:ownerId AND sys.organizationId = :organizationId")
	List<Systems> getAllBySystemOwnerAndOrganization(
		@Param("ownerId") Long ownerId,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sys FROM Systems sys JOIN sys.dataAssetClassification dac " +
		"WHERE sys.organizationId = :organizationId AND dac.id = :assetClassId")
	List<Systems> getAllByOrganizationAndAssetClass(
		@Param("organizationId") Long organizationId,
		@Param("assetClassId") Long assetClassId
	);

	@Query("SELECT sys FROM Systems sys LEFT JOIN FETCH sys.owner ow LEFT JOIN FETCH sys.infosecFocalPerson ip " +
		"LEFT JOIN FETCH sys.businessUnit bu LEFT JOIN FETCH sys.dataAssetClassification dac " +
		"LEFT JOIN FETCH sys.createdBy LEFT JOIN FETCH sys.updatedBy " +
		"WHERE sys.organizationId = :organizationId AND UPPER(sys.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<Systems> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(sys) FROM Systems sys " +
		"WHERE sys.organizationId = :organizationId AND UPPER(sys.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT DISTINCT s FROM Systems s JOIN s.dataTypeClassifications dtc " +
		"WHERE s.organizationId = :organizationId AND dtc.id IN :dataTypeIdList")
	List<Systems> getSystemsListWithDataTypes(@Param("organizationId") Long organizationId, @Param("dataTypeIdList") List<Long> dataTypeIdList);

	@Query("SELECT DISTINCT s FROM Technologies t JOIN t.systems s WHERE s.organizationId = :organizationId AND t.technologyCategory.id IN :technologyCategoryIdList")
	List<Systems> getSystemsListWithTechnologyCategories(@Param("organizationId") Long organizationId, @Param("technologyCategoryIdList") List<Long> technologyCategoryIdList);

	@Query("SELECT DISTINCT s FROM Systems s WHERE s.organizationId = :organizationId AND s.systemType=:systemType")
	List<Systems> getSystemsListSystemType(@Param("systemType") SystemType systemType, @Param("organizationId") Long organizationId);

	@Query("SELECT DISTINCT s FROM Systems s WHERE s.organizationId = :organizationId AND s.isMAAsset=:isMAAsset")
	List<Systems> getSystemsListByMA(@Param("isMAAsset") boolean isMAAsset, @Param("organizationId") Long organizationId);

	List<Systems> findAllByCybersecurityTools(CybersecurityTools cybersecurityTool);

	@Query("SELECT sys FROM Systems sys join sys.metadata meta WHERE sys.organizationId = :organizationId and meta.key = :metadataKey and meta.value = :metadataValue")
	Optional<Systems> findByOrganizationAndMeta(
		@Param("organizationId") Long organizationId,
		@Param("metadataKey") String metadataKey,
		@Param("metadataValue") String metadataValue
	);

}
