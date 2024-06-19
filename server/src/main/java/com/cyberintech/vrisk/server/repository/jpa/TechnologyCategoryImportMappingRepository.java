package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategoryImportMappings;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TechnologyCategoryImportMappingRepository extends CoreRepository<TechnologyCategoryImportMappings, Long> {

	@Query("SELECT tc FROM TechnologyCategoryImportMappings tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND UPPER(tc.technologyCategory) = UPPER(:technologyCategory)")
	Optional<TechnologyCategoryImportMappings> getFirstByNameAndOrganization(@Param("organizationId") Long organizationId, @Param("technologyCategory") String technologyCategory);

	@Query("SELECT tc FROM TechnologyCategoryImportMappings tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND UPPER(tc.technologyCategory) = UPPER(:technologyCategory)" +
		" AND UPPER(tc.technologyName) = UPPER(:technologyName)")
	Optional<TechnologyCategoryImportMappings> getFirstByNameAndTechnologyAndOrganization(@Param("organizationId") Long organizationId, @Param("technologyCategory") String technologyCategory
		, @Param("technologyName") String technologyName);

	@Query("SELECT tc FROM TechnologyCategoryImportMappings tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND UPPER(tc.technologyCategory) = UPPER(:technologyCategory)" +
		" AND UPPER(tc.technologyName) = UPPER(:technologyName)  AND UPPER(tc.technologyVendor) = UPPER(:technologyVendor)")
	Optional<TechnologyCategoryImportMappings> getFirstByNameAndTechnologyAndVendorAndOrganization(@Param("organizationId") Long organizationId, @Param("technologyCategory") String technologyCategory
		, @Param("technologyName") String technologyName, @Param("technologyVendor") String technologyVendor);

}
