package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.DataFields;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataFieldsRepository extends CoreRepository<DataFields, Long> {

	Optional<DataFields> findById(Long id);

	@Query("SELECT dtc FROM DataFields dtc WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) AND UPPER(dtc.name) LIKE (CONCAT(UPPER(:name), '%'))")
	List<DataFields> getAllByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

	@Query("SELECT dtc FROM DataFields dtc WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) " +
		"AND (UPPER(dtc.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(dtc.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	List<DataFields> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(dtc) FROM DataFields dtc WHERE (dtc.organizationId = :organizationId OR dtc.organizationId IS NULL) " +
		"AND (UPPER(dtc.name) LIKE CONCAT('%', UPPER(:name), '%') OR UPPER(dtc.description) LIKE CONCAT('%', UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

}
