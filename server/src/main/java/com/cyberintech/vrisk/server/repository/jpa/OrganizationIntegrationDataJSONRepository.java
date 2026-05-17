package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationIntegrationDataJSON;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationIntegrationDataJSONRepository extends CoreRepository<OrganizationIntegrationDataJSON, Long> {
	void deleteAllByOrganizationId(Long organizationId);
}
