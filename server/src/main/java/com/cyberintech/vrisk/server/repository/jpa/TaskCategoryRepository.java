package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.TaskCategories;
import com.cyberintech.vrisk.server.model.jpa.entity.TaskCategories;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskCategoryRepository extends CoreRepository<TaskCategories, Long> {

	Optional<TaskCategories> findById(Long id);

	@Query("SELECT tc FROM TaskCategories tc WHERE tc.organizationId = :organizationId AND UPPER(tc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<TaskCategories> getAllByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT tc FROM TaskCategories tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND UPPER(tc.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	List<TaskCategories> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(tc) FROM TaskCategories tc WHERE (tc.organizationId = :organizationId OR tc.organizationId IS NULL) AND UPPER(tc.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

}
