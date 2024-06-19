package com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdConfigurationProvider;
import com.cyberintech.vrisk.server.model.jpa.entity.DataTypeClassification;
import com.cyberintech.vrisk.server.model.jpa.entity.DataTypeClassificationMetadata;
import com.cyberintech.vrisk.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataTypeClassificationMetadataHelper
	extends MetadataHelperBase<DataTypeClassificationMetadata, DataTypeClassification> {

	public DataTypeClassificationMetadataHelper(BigIdConfigurationProvider configurationProvider, UserService userService) {
		super(configurationProvider, userService);
	}

	@Override
	protected DataTypeClassificationMetadata createMetadataInstance() {
		return new DataTypeClassificationMetadata();
	}

	@Override
	protected void setMetaParent(DataTypeClassificationMetadata metadata, DataTypeClassification parent) {
		metadata.setDataTypeClassification(parent);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
