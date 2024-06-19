package com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdConfigurationProvider;
import com.cyberintech.vrisk.server.model.jpa.entity.FieldClassifiers;
import com.cyberintech.vrisk.server.model.jpa.entity.FieldClassifiersMetadata;
import com.cyberintech.vrisk.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FieldClassifierMetadataHelper extends MetadataHelperBase<FieldClassifiersMetadata, FieldClassifiers> {

	public FieldClassifierMetadataHelper(BigIdConfigurationProvider configurationProvider, UserService userService) {
		super(configurationProvider, userService);
	}

	@Override
	protected FieldClassifiersMetadata createMetadataInstance() {
		return new FieldClassifiersMetadata();
	}

	@Override
	protected void setMetaParent(FieldClassifiersMetadata metadata, FieldClassifiers parent) {
		metadata.setFieldClassifier(parent);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
