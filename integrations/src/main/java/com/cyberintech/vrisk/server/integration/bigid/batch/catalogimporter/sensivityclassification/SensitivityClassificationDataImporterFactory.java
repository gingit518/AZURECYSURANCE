package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.CatalogDataImporterFactoryBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.DataAssetClassificationMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.repository.jpa.DataAssetClassificationRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SensitivityClassificationDataImporterFactory
	extends CatalogDataImporterFactoryBase<SensitivityClassificationDataImporter, SensitivityClassificationDataImporterParamVO> {

	private final DataAssetClassificationRepository dataAssetClassificationRepository;
	private final DataAssetClassificationMetadataHelper dataAssetClassificationMetadataHelper;

	public SensitivityClassificationDataImporterFactory(OrganizationRepository organizationRepository, UserRepository userRepository,
														UserMetadataHelper userMetadataHelper, UserImportHelper userImportHelper,
														ApplicationEventPublisher applicationEventPublisher,
														DataAssetClassificationRepository dataAssetClassificationRepository,
														DataAssetClassificationMetadataHelper dataAssetClassificationMetadataHelper) {
		super(organizationRepository, userRepository, userMetadataHelper, userImportHelper, applicationEventPublisher);
		this.dataAssetClassificationRepository = dataAssetClassificationRepository;
		this.dataAssetClassificationMetadataHelper = dataAssetClassificationMetadataHelper;
	}

	public SensitivityClassificationDataImporter create(SensitivityClassificationDataImporterParamVO params) {
		SensitivityClassificationDataImporter sensitivityClassificationDataImporter
			= new SensitivityClassificationDataImporter(organizationRepository, userRepository, applicationEventPublisher,
			userMetadataHelper, userImportHelper, dataAssetClassificationRepository, dataAssetClassificationMetadataHelper);
		sensitivityClassificationDataImporter.init(params);
		return sensitivityClassificationDataImporter;

	}
}
