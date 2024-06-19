package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.CatalogDataImporterFactoryBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.CountryStateHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.OrganizationMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.TechnologyCategoryMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.TechnologyMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyCategoryRepository;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DatasourceCatalogDataImporterFactory
	extends CatalogDataImporterFactoryBase<DatasourceCatalogDataImporter, DatasourceCatalogDataImporterParamVO> {

	private final TechnologyCategoryRepository technologyCategoryRepository;
	private final TechnologyRepository technologyRepository;
	private final CountryStateHelper countryStateHelper;
	private final TechnologyCategoryMetadataHelper technologyCategoryMetadataHelper;
	private final TechnologyMetadataHelper technologyMetadataHelper;
	private final OrganizationMetadataHelper organizationMetadataHelper;

	public DatasourceCatalogDataImporterFactory(OrganizationRepository organizationRepository, UserRepository userRepository,
												UserMetadataHelper userMetadataHelper, UserImportHelper userImportHelper,
												ApplicationEventPublisher applicationEventPublisher,
												TechnologyCategoryRepository technologyCategoryRepository,
												TechnologyRepository technologyRepository, CountryStateHelper countryStateHelper,
												TechnologyCategoryMetadataHelper technologyCategoryMetadataHelper,
												TechnologyMetadataHelper technologyMetadataHelper,
												OrganizationMetadataHelper organizationMetadataHelper) {
		super(organizationRepository, userRepository, userMetadataHelper, userImportHelper, applicationEventPublisher);
		this.technologyCategoryRepository = technologyCategoryRepository;
		this.technologyRepository = technologyRepository;
		this.countryStateHelper = countryStateHelper;
		this.technologyCategoryMetadataHelper = technologyCategoryMetadataHelper;
		this.technologyMetadataHelper = technologyMetadataHelper;
		this.organizationMetadataHelper = organizationMetadataHelper;
	}

	@Override
	public DatasourceCatalogDataImporter create(DatasourceCatalogDataImporterParamVO params) {
		DatasourceCatalogDataImporter datasourceCatalogDataImporter =
			new DatasourceCatalogDataImporter(organizationRepository, applicationEventPublisher, userMetadataHelper,
				userImportHelper, userRepository, technologyCategoryRepository,
				technologyRepository, countryStateHelper, technologyCategoryMetadataHelper,
				technologyMetadataHelper, organizationMetadataHelper);
		datasourceCatalogDataImporter.init(params);
		return datasourceCatalogDataImporter;
	}
}
