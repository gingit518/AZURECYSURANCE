package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Tasks;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends CoreRepository<Tasks, Long> {

	Optional<Tasks> findById(Long id);

	@Query("SELECT tc FROM Tasks tc WHERE tc.organizationId = :organizationId AND UPPER(tc.taskNotes) LIKE (CONCAT(UPPER(:name), '%'))")
	List<Tasks> getAllByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT tc FROM Tasks tc LEFT JOIN FETCH tc.taskManager tm LEFT JOIN FETCH tc.taskAssignee ta " +
		"WHERE tc.organizationId = :organizationId AND tc.id NOT IN :excludeIds " +
		"AND (UPPER(tc.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(tc.taskNotes) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<Tasks> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		@Param("excludeIds") Collection<Long> excludeIds,
		Pageable pageable
	);

	@Query("SELECT count(tc) FROM Tasks tc WHERE tc.organizationId = :organizationId AND tc.id NOT IN :excludeIds " +
		"AND (UPPER(tc.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(tc.taskNotes) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		@Param("excludeIds") Collection<Long> excludeIds
	);

	@Query("SELECT tc FROM Tasks tc LEFT JOIN FETCH tc.businessUnit LEFT JOIN FETCH tc.project " +
		"LEFT JOIN FETCH tc.taskAssignee LEFT JOIN FETCH tc.taskCategory LEFT JOIN FETCH tc.taskManager " +
		"WHERE tc.organizationId = :organizationId")
	List<Tasks> getListByOrganization(@Param("organizationId") Long organizationId);

	@Query("SELECT tc FROM Tasks tc LEFT JOIN tc.project p " +
		"WHERE tc.organizationId = :organizationId AND p.id = :projectId")
	List<Tasks> getListByOrganizationAndProject(
		@Param("organizationId") Long organizationId,
		@Param("projectId") Long projectId
	);

}
