package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.GDPROrganizationStatus;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GDPROrganizationStatusRepository extends CoreRepository<GDPROrganizationStatus, Long> {

	Optional<GDPROrganizationStatus> findById(Long id);

	Optional<GDPROrganizationStatus> findByIdAndOrganizationId(Long id, Long organizationId);

	@Query("SELECT ss FROM GDPROrganizationStatus ss WHERE ss.organizationId = :organizationId")
	Optional<GDPROrganizationStatus> getOneByOrganization(
		@Param("organizationId") Long organizationId
	);

}
