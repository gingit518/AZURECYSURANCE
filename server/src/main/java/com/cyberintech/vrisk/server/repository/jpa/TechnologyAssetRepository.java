package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyAssets;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnologyAssetRepository extends CoreRepository<TechnologyAssets, Long> {

	Optional<TechnologyAssets> findById(Long id);

	@Query("SELECT sys FROM TechnologyAssets sys WHERE sys.organizationId = :organizationId AND sys.name=:name")
	Optional<TechnologyAssets> getFirstByNameForOrganization(
		@Param("name") String name,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sys FROM TechnologyAssets sys LEFT JOIN FETCH sys.owner ow LEFT JOIN FETCH sys.infosecFocalPerson ip " +
		"LEFT JOIN FETCH sys.businessUnit bu " +
		"WHERE sys.organizationId = :organizationId")
	List<TechnologyAssets> getAllByOrganization(
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sys FROM TechnologyAssets sys LEFT JOIN FETCH sys.owner ow LEFT JOIN FETCH sys.infosecFocalPerson ip " +
		"LEFT JOIN FETCH sys.businessUnit bu " +
		"WHERE sys.organizationId = :organizationId ORDER BY sys.name")
	List<TechnologyAssets> getAllByOrganizationOrderedByName(
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sys FROM TechnologyAssets sys LEFT JOIN FETCH sys.owner ow LEFT JOIN FETCH sys.infosecFocalPerson ip " +
		"LEFT JOIN FETCH sys.businessUnit bu " +
		"WHERE sys.organizationId = :organizationId AND sys.isEtl != true ORDER BY sys.name ASC")
	List<TechnologyAssets> getAllByOrganizationAndNotEtl(
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT sys FROM TechnologyAssets sys LEFT JOIN FETCH sys.owner ow " +
		"WHERE ow.id=:ownerId AND sys.organizationId = :organizationId")
	List<TechnologyAssets> getAllByTechnologyAssetOwnerAndOrganization(
		@Param("ownerId") Long ownerId,
		@Param("organizationId") Long organizationId
	);

	@Query("SELECT count(sys) FROM TechnologyAssets sys " +
		"WHERE sys.organizationId = :organizationId AND UPPER(sys.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT DISTINCT s FROM Technologies t JOIN t.systems s WHERE s.organizationId = :organizationId AND t.technologyCategory.id IN :technologyCategoryIdList")
	List<TechnologyAssets> getTechnologyAssetsListWithTechnologyCategories(@Param("organizationId") Long organizationId, @Param("technologyCategoryIdList") List<Long> technologyCategoryIdList);

	@Query("SELECT DISTINCT s FROM TechnologyAssets s WHERE s.organizationId = :organizationId AND s.systemType=:systemType")
	List<TechnologyAssets> getTechnologyAssetsListTechnologyAssetType(@Param("systemType") SystemType systemType, @Param("organizationId") Long organizationId);

	@Query("SELECT DISTINCT s FROM TechnologyAssets s WHERE s.organizationId = :organizationId AND s.isMAAsset=:isMAAsset")
	List<TechnologyAssets> getTechnologyAssetsListByMA(@Param("isMAAsset") boolean isMAAsset, @Param("organizationId") Long organizationId);

}
