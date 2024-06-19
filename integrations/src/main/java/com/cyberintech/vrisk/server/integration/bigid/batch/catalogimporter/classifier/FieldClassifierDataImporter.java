package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.CatalogDataImporterBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo.FieldClassifierDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo.FieldClassifierDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.CatalogImportMetadataConstants;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.ImportAction;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.FieldClassifierMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.client.classifier.vo.FieldClassifierVO;
import com.cyberintech.vrisk.server.model.jpa.entity.FieldClassifiers;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.FieldClassifierRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FieldClassifierDataImporter
	extends CatalogDataImporterBase<FieldClassifierDataImporterParamVO, FieldClassifierDataImporterResultVO> {

	private final FieldClassifierRepository fieldClassifierRepository;
	private final FieldClassifierMetadataHelper fieldClassifierMetadataHelper;

	public FieldClassifierDataImporter(OrganizationRepository organizationRepository, UserRepository userRepository,
									   ApplicationEventPublisher applicationEventPublisher,
									   UserMetadataHelper userMetadataHelper, UserImportHelper userImportHelper,
									   FieldClassifierRepository fieldClassifierRepository,
									   FieldClassifierMetadataHelper fieldClassifierMetadataHelper) {
		super(organizationRepository, userRepository, applicationEventPublisher, userMetadataHelper, userImportHelper);
		this.fieldClassifierRepository = fieldClassifierRepository;
		this.fieldClassifierMetadataHelper = fieldClassifierMetadataHelper;
	}

	@Override
	public FieldClassifierDataImporterResultVO process() {
		return process(null, null);
	}

	@Override
	public boolean supports(Object data) {
		return data instanceof FieldClassifierDataImporterParamVO;
	}

	private void fillMetadata() {
		FieldClassifiers fieldClassifier = fieldClassifierRepository.findById(getResult().getFieldClassifierId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("Field Classifier was not found by %s id.", getResult().getFieldClassifierId())));
		switch (getResult().getImportAction()) {
			case CREATED:
				fieldClassifierMetadataHelper.fillOnCreate(fieldClassifier, "classifier", fieldClassifier.getName(), getResult().getClassifierVO().getId(), null);
				break;
			case SYNCED:
				fieldClassifierMetadataHelper.fillOnUpdate(fieldClassifier);
				break;
			default:
		}
		fieldClassifierRepository.save(fieldClassifier);
	}

	@Override
	protected FieldClassifierDataImporterResultVO process(Organizations currentOrganization, Users currentUser) {
		FieldClassifierVO classifierVO = getParams().getClassifierVO();
		FieldClassifiers fieldClassifier = fieldClassifierRepository.findByMeta(CatalogImportMetadataConstants.SOURCE_ID, classifierVO.getId()).orElseGet(FieldClassifiers::new);
		getResult().setImportAction(fieldClassifier.getId() == null ? ImportAction.CREATED : ImportAction.SYNCED);
		fieldClassifier.setName(classifierVO.getName());
		fieldClassifier.setDescription(classifierVO.getDescription());
		fieldClassifierRepository.save(fieldClassifier);
		getResult().setFieldClassifierId(fieldClassifier.getId());
		fillMetadata();
		return getResult();
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected FieldClassifierDataImporterResultVO initResult(FieldClassifierDataImporterParamVO params) {
		return new FieldClassifierDataImporterResultVO(params.getOrganizationId(), params.getUserImporterId(), params.getClassifierVO());
	}
}
