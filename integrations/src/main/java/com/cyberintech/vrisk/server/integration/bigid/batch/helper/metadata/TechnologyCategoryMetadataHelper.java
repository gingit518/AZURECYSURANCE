package com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdConfigurationProvider;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategoriesMetadata;
import com.cyberintech.vrisk.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TechnologyCategoryMetadataHelper
	extends MetadataHelperBase<TechnologyCategoriesMetadata, TechnologyCategories> {

	public TechnologyCategoryMetadataHelper(BigIdConfigurationProvider configurationProvider, UserService userService) {
		super(configurationProvider, userService);
	}

	@Override
	protected TechnologyCategoriesMetadata createMetadataInstance() {
		return new TechnologyCategoriesMetadata();
	}

	@Override
	protected void setMetaParent(TechnologyCategoriesMetadata metadata, TechnologyCategories parent) {
		metadata.setTechnologyCategory(parent);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
