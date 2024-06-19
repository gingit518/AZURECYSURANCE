package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo.FieldClassifierDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.CatalogDataImporterFactoryBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.FieldClassifierMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.repository.jpa.FieldClassifierRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class FieldClassifierDataImporterFactory
	extends CatalogDataImporterFactoryBase<FieldClassifierDataImporter, FieldClassifierDataImporterParamVO> {

	private final FieldClassifierRepository fieldClassifierRepository;
	private final FieldClassifierMetadataHelper fieldClassifierMetadataHelper;

	public FieldClassifierDataImporterFactory(OrganizationRepository organizationRepository, UserRepository userRepository,
											  UserMetadataHelper userMetadataHelper, UserImportHelper userImportHelper,
											  ApplicationEventPublisher applicationEventPublisher, FieldClassifierRepository
												  fieldClassifierRepository, FieldClassifierMetadataHelper fieldClassifierMetadataHelper) {
		super(organizationRepository, userRepository, userMetadataHelper, userImportHelper, applicationEventPublisher);
		this.fieldClassifierRepository = fieldClassifierRepository;
		this.fieldClassifierMetadataHelper = fieldClassifierMetadataHelper;
	}

	public FieldClassifierDataImporter create(FieldClassifierDataImporterParamVO paramVO) {
		FieldClassifierDataImporter fieldClassifierDataImporter = new FieldClassifierDataImporter(
			organizationRepository, userRepository, applicationEventPublisher, userMetadataHelper,
			userImportHelper, fieldClassifierRepository, fieldClassifierMetadataHelper);
		fieldClassifierDataImporter.init(paramVO);
		return fieldClassifierDataImporter;
	}
}
