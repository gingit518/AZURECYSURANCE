package com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.CatalogImportMetadataConstants;
import com.cyberintech.vrisk.server.integration.bigid.batch.util.DateTimeFormatterUtil;
import com.cyberintech.vrisk.server.integration.bigid.client.BigIdConfigurationProvider;
import com.cyberintech.vrisk.server.model.jpa.entity.common.IMetadata;
import com.cyberintech.vrisk.server.model.jpa.entity.common.IMetadataAware;
import com.cyberintech.vrisk.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class MetadataHelperBase<M extends IMetadata, P extends IMetadataAware<M>> {

	private final BigIdConfigurationProvider configurationProvider;
	private final UserService userService;

	public void fillOnCreate(P parent, String sourceEntity, String sourceEntityName, Long organizationId) {
		this.fillOnCreate(parent, sourceEntity, sourceEntityName, null);
	}

	public void fillOnCreate(P parent, String sourceEntity, String sourceEntityName, String sourceId, Long organizationId) {
		if (parent.getMetadata() == null) {
			parent.setMetadata(new HashSet<>());
		}
		parent.getMetadata().add(createMeta(parent, CatalogImportMetadataConstants.SOURCE_SYSTEM,
			CatalogImportMetadataConstants.SOURCE_SYSTEM_BIG_ID));
		asOptional(sourceId).ifPresent(v -> parent.getMetadata().add(createMeta(parent, CatalogImportMetadataConstants.SOURCE_ID, v)));
		asOptional(sourceEntity).ifPresent(v -> parent.getMetadata().add(createMeta(parent, CatalogImportMetadataConstants.SOURCE_ENTITY, v)));
		asOptional(sourceEntityName).ifPresent(v -> parent.getMetadata().add(createMeta(parent, CatalogImportMetadataConstants.SOURCE_ENTITY_NAME, v)));
		asOptional(configurationProvider.getBaseServiceUrl(organizationId)).ifPresent(v -> parent.getMetadata().add(createMeta(parent, CatalogImportMetadataConstants.SOURCE_REMOTE, v)));
		parent.getMetadata().add(createMeta(parent, CatalogImportMetadataConstants.IMPORTED_BY, userService.getCurrentUserEntity().getFullName()));
		parent.getMetadata().add(createMeta(parent, CatalogImportMetadataConstants.IMPORTED_AT_UTC,
			DateTimeFormatterUtil.YYYY_MM_DD_HH_MM_SS_UTC.format(ZonedDateTime.now().withZoneSameInstant(ZoneId.of(DateTimeFormatterUtil.UTC_TZ)))));
	}

	public void fillOnUpdate(P parent) {
		//not required
	}

	protected M createMeta(P parent, String key, String value) {
		M metadata = createMetadataInstance();
		setMetaParent(metadata, parent);
		metadata.setKey(key);
		metadata.setValue(value);
		return metadata;
	}

	protected abstract M createMetadataInstance();

	protected abstract void setMetaParent(M metadata, P parent);

	protected abstract Logger getLogger();

	protected Optional<String> asOptional(String s) {
		return Optional.ofNullable(s).map(StringUtils::trimToEmpty).filter(StringUtils::isNoneBlank);
	}
}
