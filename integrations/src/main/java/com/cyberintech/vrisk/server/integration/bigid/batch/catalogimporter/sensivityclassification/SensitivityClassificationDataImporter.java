package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.CatalogDataImporterBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.CatalogImportMetadataConstants;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.CreateAuditRecordEvent;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.UpdateAuditRecordEvent;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.DataAssetClassificationMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification.vo.SCConfigClassificationVO;
import com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification.vo.SCConfigVO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.DataAssetClassification;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.DataAssetClassificationRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;

@Slf4j
@Component
public class SensitivityClassificationDataImporter
	extends CatalogDataImporterBase<SensitivityClassificationDataImporterParamVO, SensitivityClassificationDataImporterResultVO> {

	private final DataAssetClassificationRepository dataAssetClassificationRepository;
	private final DataAssetClassificationMetadataHelper dataAssetClassificationMetadataHelper;

	public SensitivityClassificationDataImporter(OrganizationRepository organizationRepository, UserRepository userRepository,
												 ApplicationEventPublisher applicationEventPublisher, UserMetadataHelper userMetadataHelper,
												 UserImportHelper userImportHelper, DataAssetClassificationRepository dataAssetClassificationRepository,
												 DataAssetClassificationMetadataHelper dataAssetClassificationMetadataHelper) {
		super(organizationRepository, userRepository, applicationEventPublisher, userMetadataHelper, userImportHelper);
		this.dataAssetClassificationRepository = dataAssetClassificationRepository;
		this.dataAssetClassificationMetadataHelper = dataAssetClassificationMetadataHelper;
	}

	@Override
	protected SensitivityClassificationDataImporterResultVO process(Organizations currentOrganization, Users currentUser) {
		SCConfigVO scConfigVO = getParams().getScConfigVO();
		for (SCConfigClassificationVO classification : scConfigVO.getClassifications()) {
			DataAssetClassification dac = dataAssetClassificationRepository
				.findByNameAndOrganizationAndMeta(classification.getName(), currentOrganization.getId(),
					CatalogImportMetadataConstants.SOURCE_ID, scConfigVO.getId()).orElseGet(DataAssetClassification::new);
			boolean isNew = dac.getId() == null;
			dac.setName(classification.getName());
			dac.setDescription(classification.getQuery());
			if (isNew) {
				dac.setCreatedBy(currentUser);
				dac.setCreatedAt(new Date());
				dac.setUpdatedBy(currentUser);
				dac.setUpdatedAt(new Date());
				dac.setOrganizationId(currentOrganization.getId());
				dac = dataAssetClassificationRepository.save(dac);
				getResult().getCreatedDataAssetClassifiers().add(new DataAssetClassificationRefDTO(dac));
				applicationEventPublisher.publishEvent(new CreateAuditRecordEvent(
					this.getClass(),
					VItemType.ASSET_CLASS,
					dac.getId(),
					new DataAssetClassificationRefDTO(dac),
					Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, currentOrganization.getId()), AuditLogItemId.of(VItemType.BIG_ID_DATA_ASSET_CLASSIFICATION_SYNC, dac.getId())).toArray(AuditLogItemId[]::new)
				));
			} else {
				dac.setUpdatedBy(currentUser);
				dac.setUpdatedAt(new Date());
				dac = dataAssetClassificationRepository.save(dac);
				getResult().getSyncedDataAssetClassifiers().add(new DataAssetClassificationRefDTO(dac));
				applicationEventPublisher.publishEvent(new UpdateAuditRecordEvent(this.getClass(),
					VItemType.ASSET_CLASS,
					dac.getId(),
					new DataAssetClassificationRefDTO(dac),
					Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, currentOrganization.getId()),
						AuditLogItemId.of(VItemType.BIG_ID_DATA_ASSET_CLASSIFICATION_SYNC, dac.getId())).toArray(AuditLogItemId[]::new),
					null
				));
			}

		}
		fillMetadata();
		return getResult();
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected SensitivityClassificationDataImporterResultVO initResult(SensitivityClassificationDataImporterParamVO params) {
		return new SensitivityClassificationDataImporterResultVO(params.getOrganizationId(), params.getUserImporterId(), params.getScConfigVO(),
			new LinkedList<>(), new LinkedList<>());
	}

	@Override
	public boolean supports(Object data) {
		return data instanceof SensitivityClassificationDataImporterParamVO;
	}

	private void fillMetadata() {
		for (DataAssetClassificationRefDTO refDTO : Optional.ofNullable(getResult().getCreatedDataAssetClassifiers()).orElse(Collections.emptyList())) {
			DataAssetClassification dataTypeClassification = dataAssetClassificationRepository.findById(refDTO.getId())
				.orElseThrow(() -> new ItemNotFoundException(String.format("Data asset classification was not found by %s id.", refDTO.getId())));
			dataAssetClassificationMetadataHelper.fillOnCreate(dataTypeClassification,
				"sensitivity-classification", getResult().getScConfigVO().getName(),
				getResult().getScConfigVO().getId(), dataTypeClassification.getOrganizationId());
			dataAssetClassificationRepository.save(dataTypeClassification);
		}

		for (DataAssetClassificationRefDTO refDTO : Optional.ofNullable(getResult().getSyncedDataAssetClassifiers()).orElse(Collections.emptyList())) {
			// do nothing for noe
		}
	}
}
