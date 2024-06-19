package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Projects;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends CoreRepository<Projects, Long> {

	Optional<Projects> findById(Long id);

	@Query("SELECT prj FROM Projects prj WHERE prj.organizationId = :organizationId")
	List<Projects> getListByOrganization(@Param("organizationId") Long organizationId);

	@Query("SELECT prj FROM Projects prj WHERE prj.organizationId = :organizationId AND UPPER(prj.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<Projects> getAllByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT prj FROM Projects prj WHERE prj.organizationId = :organizationId  AND UPPER(prj.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<Projects> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(prj) FROM Projects prj WHERE prj.organizationId = :organizationId AND UPPER(prj.name) LIKE (CONCAT(UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

}
