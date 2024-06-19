package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.CyberRoles;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CyberRoleRepository extends CoreRepository<CyberRoles, Long> {

	Optional<CyberRoles> findById(Long id);

	@Query("SELECT dtc FROM CyberRoles dtc LEFT JOIN FETCH dtc.createdBy LEFT JOIN FETCH dtc.updatedBy " +
		"WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) " +
		"AND (UPPER(dtc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(dtc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	List<CyberRoles> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(dtc) FROM CyberRoles dtc " +
		"WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) " +
		"AND (UPPER(dtc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(dtc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

}
