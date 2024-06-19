package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.CatalogDataImporterFactoryBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.DataTypeClassificationMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.repository.jpa.DataTypeClassificationRepository;
import com.cyberintech.vrisk.server.repository.jpa.FieldClassifierRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ComplianceRuleDataImporterFactory
	extends CatalogDataImporterFactoryBase<ComplianceRuleDataImporter, ComplianceRuleImporterParamVO> {
	private final DataTypeClassificationRepository dataTypeClassificationRepository;
	private final FieldClassifierRepository fieldClassifierRepository;
	private final DataTypeClassificationMetadataHelper dataTypeClassificationMetadataHelper;

	public ComplianceRuleDataImporterFactory(OrganizationRepository organizationRepository, UserRepository userRepository, UserMetadataHelper userMetadataHelper,
											 UserImportHelper userImportHelper, ApplicationEventPublisher applicationEventPublisher,
											 DataTypeClassificationRepository dataTypeClassificationRepository, FieldClassifierRepository fieldClassifierRepository,
											 DataTypeClassificationMetadataHelper dataTypeClassificationMetadataHelper) {
		super(organizationRepository, userRepository, userMetadataHelper, userImportHelper, applicationEventPublisher);
		this.dataTypeClassificationRepository = dataTypeClassificationRepository;
		this.fieldClassifierRepository = fieldClassifierRepository;
		this.dataTypeClassificationMetadataHelper = dataTypeClassificationMetadataHelper;
	}

	public ComplianceRuleDataImporter create(ComplianceRuleImporterParamVO params) {
		ComplianceRuleDataImporter complianceRuleDataImporter = new ComplianceRuleDataImporter(organizationRepository, userRepository, applicationEventPublisher,
			userMetadataHelper, userImportHelper, dataTypeClassificationRepository, dataTypeClassificationMetadataHelper,
			fieldClassifierRepository);
		complianceRuleDataImporter.init(params);
		return complianceRuleDataImporter;
	}

}
