package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common;

import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

@RequiredArgsConstructor
public abstract class CatalogDataImporterFactoryBase<I, P> {

	protected final OrganizationRepository organizationRepository;
	protected final UserRepository userRepository;
	protected final UserMetadataHelper userMetadataHelper;
	protected final UserImportHelper userImportHelper;
	protected final ApplicationEventPublisher applicationEventPublisher;

	public abstract I create(P params);
}
