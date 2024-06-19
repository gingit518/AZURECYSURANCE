package com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdConfigurationProvider;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemsMetadata;
import com.cyberintech.vrisk.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SystemMetadataHelper extends MetadataHelperBase<SystemsMetadata, Systems> {

	public SystemMetadataHelper(BigIdConfigurationProvider configurationProvider, UserService userService) {
		super(configurationProvider, userService);
	}

	@Override
	protected SystemsMetadata createMetadataInstance() {
		return new SystemsMetadata();
	}

	@Override
	protected void setMetaParent(SystemsMetadata metadata, Systems parent) {
		metadata.setSystem(parent);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
