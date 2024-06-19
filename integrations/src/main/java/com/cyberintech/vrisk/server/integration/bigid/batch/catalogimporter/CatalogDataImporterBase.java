package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogDataImporterParamVOBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;


@RequiredArgsConstructor
@Slf4j
public abstract class CatalogDataImporterBase<P extends CatalogDataImporterParamVOBase, R>
	implements ICatalogDataImporter<R> {

	protected final OrganizationRepository organizationRepository;
	protected final UserRepository userRepository;
	protected final ApplicationEventPublisher applicationEventPublisher;
	protected final UserMetadataHelper userMetadataHelper;
	protected final UserImportHelper userImportHelper;

	private P params;
	private R result;

	@Override
	public R process() {
		Users currentUser = userRepository.findById(params.getUserImporterId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("No currentUser found by %s id", params.getUserImporterId())));
		Organizations currentOrganization = organizationRepository.findById(params.getOrganizationId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("No organization found by %s id", params.getOrganizationId())));
		return process(currentOrganization, currentUser);
	}

	public void init(P params) {
		this.params = params;
		this.result = initResult(params);
	}

	protected abstract R process(Organizations currentOrganization, Users currentUser);

	protected abstract Logger getLogger();

	protected abstract R initResult(P params);

	public P getParams() {
		return params;
	}

	public R getResult() {
		return result;
	}


}
