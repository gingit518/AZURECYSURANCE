package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.GDPRSystemStatus;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GDPRSystemStatusRepository extends CoreRepository<GDPRSystemStatus, Long> {

	Optional<GDPRSystemStatus> findById(Long id);

	Optional<GDPRSystemStatus> findByIdAndOrganizationId(Long id, Long organizationId);

	@Query("SELECT ss FROM GDPRSystemStatus ss JOIN ss.system s WHERE ss.organizationId = :organizationId AND s.id = :systemId")
	Optional<GDPRSystemStatus> getOneByOrganizationAndSystem(
		@Param("systemId") Long systemId,
		@Param("organizationId") Long organizationId
	);

}
