package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.CatalogDataImporterFactoryBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.CountryStateHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.SystemMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClientFactory;
import com.cyberintech.vrisk.server.repository.jpa.DataTypeClassificationRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationCatalogDataImporterFactory extends CatalogDataImporterFactoryBase<ApplicationCatalogDataImporter,
	ApplicationCatalogDataImporterParamVO> {

	private final CountryStateHelper countryStateHelper;
	private final SystemMetadataHelper systemMetadataHelper;
	private final SystemRepository systemRepository;
	private final BigIdClientFactory bigIdClientFactory;
	private final DataTypeClassificationRepository dataTypeClassificationRepository;

	public ApplicationCatalogDataImporterFactory(OrganizationRepository organizationRepository, UserRepository userRepository,
												 UserMetadataHelper userMetadataHelper, UserImportHelper userImportHelper,
												 ApplicationEventPublisher applicationEventPublisher, CountryStateHelper countryStateHelper,
												 SystemMetadataHelper systemMetadataHelper, SystemRepository systemRepository,
												 BigIdClientFactory bigIdClientFactory,
												 DataTypeClassificationRepository dataTypeClassificationRepository) {
		super(organizationRepository, userRepository, userMetadataHelper, userImportHelper, applicationEventPublisher);
		this.countryStateHelper = countryStateHelper;
		this.systemMetadataHelper = systemMetadataHelper;
		this.systemRepository = systemRepository;
		this.bigIdClientFactory = bigIdClientFactory;
		this.dataTypeClassificationRepository = dataTypeClassificationRepository;
	}

	@Override
	public ApplicationCatalogDataImporter create(ApplicationCatalogDataImporterParamVO params) {
		ApplicationCatalogDataImporter applicationCatalogDataImporter = new ApplicationCatalogDataImporter(
			organizationRepository, userRepository, applicationEventPublisher, userMetadataHelper, userImportHelper,
			countryStateHelper, systemMetadataHelper, systemRepository, bigIdClientFactory,
			dataTypeClassificationRepository
		);
		applicationCatalogDataImporter.init(params);
		return applicationCatalogDataImporter;
	}
}
