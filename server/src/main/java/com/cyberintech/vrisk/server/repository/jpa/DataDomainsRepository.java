package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.DataDomains;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataDomainsRepository extends CoreRepository<DataDomains, Long> {

	Optional<DataDomains> findById(Long id);

	Optional<DataDomains> findFirstByNameAndOrganizationId(String name, Long organizationId);

	@Query("SELECT dtc FROM DataDomains dtc WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) AND UPPER(dtc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<DataDomains> getAllByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT dtc FROM DataDomains dtc WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) " +
		"AND (UPPER(dtc.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(dtc.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<DataDomains> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(dtc) FROM DataDomains dtc WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) " +
		"AND (UPPER(dtc.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(dtc.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

}
