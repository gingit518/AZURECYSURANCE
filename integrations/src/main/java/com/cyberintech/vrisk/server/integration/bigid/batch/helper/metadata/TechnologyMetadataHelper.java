package com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdConfigurationProvider;
import com.cyberintech.vrisk.server.model.jpa.entity.Technologies;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologiesMetadata;
import com.cyberintech.vrisk.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TechnologyMetadataHelper extends MetadataHelperBase<TechnologiesMetadata, Technologies> {

	public TechnologyMetadataHelper(BigIdConfigurationProvider configurationProvider, UserService userService) {
		super(configurationProvider, userService);
	}

	@Override
	protected TechnologiesMetadata createMetadataInstance() {
		return new TechnologiesMetadata();
	}

	@Override
	protected void setMetaParent(TechnologiesMetadata metadata, Technologies parent) {
		metadata.setTechnology(parent);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
