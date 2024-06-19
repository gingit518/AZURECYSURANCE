package com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdConfigurationProvider;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationsMetadata;
import com.cyberintech.vrisk.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrganizationMetadataHelper extends MetadataHelperBase<OrganizationsMetadata, Organizations> {

	public OrganizationMetadataHelper(BigIdConfigurationProvider configurationProvider, UserService userService) {
		super(configurationProvider, userService);
	}

	@Override
	protected OrganizationsMetadata createMetadataInstance() {
		return new OrganizationsMetadata();
	}

	@Override
	protected void setMetaParent(OrganizationsMetadata metadata, Organizations parent) {
		metadata.setOrganization(parent);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
