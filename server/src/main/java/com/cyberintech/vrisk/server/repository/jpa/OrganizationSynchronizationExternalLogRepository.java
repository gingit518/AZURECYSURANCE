package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationSynchronizationExternalLog;
import com.cyberintech.vrisk.server.model.jpa.entity.Status;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationSynchronizationExternalLogRepository extends CoreRepository<OrganizationSynchronizationExternalLog, Long> {

	Optional<OrganizationSynchronizationExternalLog> findById(Long id);

	Optional<OrganizationSynchronizationExternalLog> findFirstByOrganizationIdAndIntegrationTypeAndObjectTypeAndExternalId(Long organizationId, String integrationType, String objectType, String externalId);

	int countAllByOrganizationIdAndIntegrationTypeAndObjectType(Long organizationId, String integrationType, String objectType);

}
