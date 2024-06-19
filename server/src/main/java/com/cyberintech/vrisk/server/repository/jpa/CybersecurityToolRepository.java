package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.CybersecurityTools;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CybersecurityToolRepository extends CoreRepository<CybersecurityTools, Long> {

	Optional<CybersecurityTools> findById(Long id);

	@Query("SELECT dtc FROM CybersecurityTools dtc LEFT JOIN FETCH dtc.createdBy LEFT JOIN FETCH dtc.updatedBy " +
		"WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) " +
		"AND (UPPER(dtc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(dtc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	List<CybersecurityTools> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(dtc) FROM CybersecurityTools dtc " +
		"WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) " +
		"AND (UPPER(dtc.name) LIKE (CONCAT('%', UPPER(:name), '%')) OR (UPPER(dtc.description) LIKE (CONCAT('%', UPPER(:name), '%'))))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT DISTINCT sys FROM Systems sys LEFT JOIN FETCH sys.owner ow LEFT JOIN FETCH sys.cybersecurityTools cst " +
		"LEFT JOIN FETCH sys.businessUnit bu LEFT JOIN FETCH sys.dataAssetClassification dac " +
		"WHERE sys.organizationId = :organizationId AND sys.cybersecurityTools IS NOT EMPTY ORDER BY sys.name ASC")
	Set<Systems> getSystemsByOrganization(
		@Param("organizationId") Long organizationId
	);

}
