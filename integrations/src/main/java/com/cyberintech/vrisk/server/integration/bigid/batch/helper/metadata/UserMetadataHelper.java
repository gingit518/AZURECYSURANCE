package com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.CatalogImportMetadataConstants;
import com.cyberintech.vrisk.server.integration.bigid.client.BigIdConfigurationProvider;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.model.jpa.entity.UsersMetadata;
import com.cyberintech.vrisk.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserMetadataHelper extends MetadataHelperBase<UsersMetadata, Users> {

	public UserMetadataHelper(BigIdConfigurationProvider configurationProvider, UserService userService) {
		super(configurationProvider, userService);
	}

	public void fillOnCreate(Users user, String sourceEntity, String sourceEntityName, String userType) {
		super.fillOnCreate(user, sourceEntity, sourceEntityName, null);
		asOptional(userType).ifPresent(v -> user.getMetadata().add(createMeta(user, CatalogImportMetadataConstants.USER_TYPE, v)));
	}

	@Override
	protected UsersMetadata createMetadataInstance() {
		return new UsersMetadata();
	}

	@Override
	protected void setMetaParent(UsersMetadata metadata, Users parent) {
		metadata.setUser(parent);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}

