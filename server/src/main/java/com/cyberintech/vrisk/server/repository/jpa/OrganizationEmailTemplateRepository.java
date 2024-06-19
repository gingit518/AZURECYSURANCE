package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationEmailTemplateType;
import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationEmailTemplates;
import com.cyberintech.vrisk.server.repository.jpa.core.CoreRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationEmailTemplateRepository extends CoreRepository<OrganizationEmailTemplates, Long> {

	Optional<OrganizationEmailTemplates> findById(Long id);

	Optional<OrganizationEmailTemplates> findByOrganizationIdAndType(Long organizationId, OrganizationEmailTemplateType type);
}
