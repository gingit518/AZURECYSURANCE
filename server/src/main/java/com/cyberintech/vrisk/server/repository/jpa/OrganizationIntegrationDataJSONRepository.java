package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationIntegrationDataJSON;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationIntegrationDataJSONRepository extends CoreRepository<OrganizationIntegrationDataJSON, Long> {

	List<OrganizationIntegrationDataJSON> findAllByOrganizationId(Long organizationId);

	void deleteAllByOrganizationId(Long organizationId);
}
