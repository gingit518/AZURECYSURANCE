package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.ControlMaturities;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ControlMaturitiesRepository extends CoreRepository<ControlMaturities, Long> {

	Optional<ControlMaturities> findById(Long id);

	@Query("SELECT cm FROM ControlMaturities cm " +
		"WHERE cm.organizationId = :organizationId AND UPPER(cm.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	List<ControlMaturities> getListByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name,
		Pageable pageable
	);

	@Query("SELECT count(cm) FROM ControlMaturities cm " +
		"WHERE cm.organizationId = :organizationId AND UPPER(cm.name) LIKE (CONCAT('%', UPPER(:name), '%'))")
	Long getCountByOrganizationAndName(
		@Param("organizationId") Long organizationId,
		@Param("name") String name
	);

}
