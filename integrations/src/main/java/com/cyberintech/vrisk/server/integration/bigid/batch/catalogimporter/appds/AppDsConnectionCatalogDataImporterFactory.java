package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.CatalogDataImporterFactoryBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.BigIdDatasourceTypeHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemRepository;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AppDsConnectionCatalogDataImporterFactory extends CatalogDataImporterFactoryBase<AppDsConnectionCatalogDataImporter, AppDsConnectionCatalogDataImporterParamVO> {

	private final TechnologyRepository technologyRepository;
	private final SystemRepository systemRepository;
	private final BigIdDatasourceTypeHelper bigIdDatasourceTypeHelper;

	public AppDsConnectionCatalogDataImporterFactory(OrganizationRepository organizationRepository, UserRepository userRepository, UserMetadataHelper userMetadataHelper, UserImportHelper userImportHelper, ApplicationEventPublisher applicationEventPublisher, TechnologyRepository technologyRepository, SystemRepository systemRepository, BigIdDatasourceTypeHelper bigIdDatasourceTypeHelper) {
		super(organizationRepository, userRepository, userMetadataHelper, userImportHelper, applicationEventPublisher);
		this.technologyRepository = technologyRepository;
		this.systemRepository = systemRepository;
		this.bigIdDatasourceTypeHelper = bigIdDatasourceTypeHelper;
	}

	@Override
	public AppDsConnectionCatalogDataImporter create(AppDsConnectionCatalogDataImporterParamVO params) {
		AppDsConnectionCatalogDataImporter appDsConnectionCatalogDataImporter
			= new AppDsConnectionCatalogDataImporter(organizationRepository, userRepository, applicationEventPublisher, userMetadataHelper,
			userImportHelper, technologyRepository, systemRepository, bigIdDatasourceTypeHelper);
		appDsConnectionCatalogDataImporter.init(params);
		return appDsConnectionCatalogDataImporter;
	}
}
