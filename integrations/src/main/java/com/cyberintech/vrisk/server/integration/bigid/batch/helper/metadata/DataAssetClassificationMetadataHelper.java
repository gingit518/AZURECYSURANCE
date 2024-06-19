package com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdConfigurationProvider;
import com.cyberintech.vrisk.server.model.jpa.entity.DataAssetClassification;
import com.cyberintech.vrisk.server.model.jpa.entity.DataAssetClassificationMetadata;
import com.cyberintech.vrisk.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataAssetClassificationMetadataHelper
	extends MetadataHelperBase<DataAssetClassificationMetadata, DataAssetClassification> {

	public DataAssetClassificationMetadataHelper(BigIdConfigurationProvider configurationProvider, UserService userService) {
		super(configurationProvider, userService);
	}

	@Override
	protected DataAssetClassificationMetadata createMetadataInstance() {
		return new DataAssetClassificationMetadata();
	}

	@Override
	protected void setMetaParent(DataAssetClassificationMetadata metadata, DataAssetClassification parent) {
		metadata.setDataAssetClassification(parent);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
