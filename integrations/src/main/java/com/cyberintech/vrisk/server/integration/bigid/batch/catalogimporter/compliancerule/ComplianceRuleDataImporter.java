package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.CatalogDataImporterBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.util.ComplianceRuleQueryUtil;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.CatalogImportMetadataConstants;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.ImportAction;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.CreateAuditRecordEvent;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.UpdateAuditRecordEvent;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.DataTypeClassificationMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.client.compliancerule.vo.ComplianceRuleVO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationEditDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.DataTypeClassification;
import com.cyberintech.vrisk.server.model.jpa.entity.FieldClassifiers;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.DataTypeClassificationRepository;
import com.cyberintech.vrisk.server.repository.jpa.FieldClassifierRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.cyberintech.vrisk.server.integration.bigid.batch.util.DateTimeFormatterUtil.YYYY_MM_DD_HH_MM_SS;

@Slf4j
public class ComplianceRuleDataImporter extends CatalogDataImporterBase<ComplianceRuleImporterParamVO, ComplianceRuleImporterResultVO> {
	private final DataTypeClassificationRepository dataTypeClassificationRepository;
	private final DataTypeClassificationMetadataHelper dataTypeClassificationMetadataHelper;
	private final FieldClassifierRepository fieldClassifierRepository;

	public ComplianceRuleDataImporter(OrganizationRepository organizationRepository,
									  UserRepository userRepository,
									  ApplicationEventPublisher applicationEventPublisher,
									  UserMetadataHelper userMetadataHelper,
									  UserImportHelper userImportHelper,
									  DataTypeClassificationRepository dataTypeClassificationRepository,
									  DataTypeClassificationMetadataHelper dataTypeClassificationMetadataHelper,
									  FieldClassifierRepository fieldClassifierRepository) {
		super(organizationRepository, userRepository, applicationEventPublisher, userMetadataHelper, userImportHelper);
		this.dataTypeClassificationRepository = dataTypeClassificationRepository;
		this.dataTypeClassificationMetadataHelper = dataTypeClassificationMetadataHelper;
		this.fieldClassifierRepository = fieldClassifierRepository;
	}

	@Override
	protected ComplianceRuleImporterResultVO process(Organizations organization, Users user) {
		processOwner(organization, user);
		processComplianceRule(organization, user);
		parseAndConnectWithClassifierFields();
		fillMetadata();
		return getResult();
	}

	@Override
	public boolean supports(Object data) {
		return data instanceof ComplianceRuleImporterParamVO;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected ComplianceRuleImporterResultVO initResult(ComplianceRuleImporterParamVO params) {
		return new ComplianceRuleImporterResultVO(params.getOrganizationId(), params.getUserImporterId(), params.getComplianceRuleVO());
	}

	private void processOwner(Organizations organization, Users user) {
		Users complianceRuleOwner = userImportHelper.mapToUser(organization, user, getParams().getComplianceRuleVO().getOwner(),
			null, null, List.of(RoleType.DATA_TYPE_CLASS_OWNER));
		if (complianceRuleOwner == null) {
			return;
		}
		if (complianceRuleOwner.getId() == null) {
			getResult().setOwnerAction(ImportAction.CREATED);
			complianceRuleOwner = userImportHelper.createUser(organization, user, complianceRuleOwner);
			userMetadataHelper.fillOnCreate(complianceRuleOwner, "compliance-rule:owner",
				getParams().getComplianceRuleVO().getName() + ":" + getParams().getComplianceRuleVO().getOwner(), null, organization.getId());
			complianceRuleOwner = userRepository.save(complianceRuleOwner);
		} else {
			getResult().setOwnerAction(ImportAction.SYNCED);
			userMetadataHelper.fillOnUpdate(complianceRuleOwner);
			complianceRuleOwner = userImportHelper.updateUser(organization, complianceRuleOwner);
		}
		getResult().setOwnerId(complianceRuleOwner.getId());
	}

	private void processComplianceRule(Organizations organization, Users user) {
		DataTypeClassification dataTypeClassification = dataTypeClassificationRepository
			.findByOrganizationAndMeta(organization.getId(), CatalogImportMetadataConstants.SOURCE_ID, getParams().getComplianceRuleVO().getId()).orElseGet(DataTypeClassification::new);
		boolean isNew = dataTypeClassification.getId() == null;
		if (isNew) {
			dataTypeClassification = createNewDataAssetClassification(organization, user, dataTypeClassification);
		} else {
			dataTypeClassification = updateDataAssecClassification(dataTypeClassification);
		}
		if (StringUtils.isNotBlank(getParams().getComplianceRuleVO().getOwner())) {
			userRepository.findByEmail(getParams().getComplianceRuleVO().getOwner()).ifPresent(dataTypeClassification::setOwner);
		}
		dataTypeClassification = dataTypeClassificationRepository.save(dataTypeClassification);
		getResult().setDataTypeClassificationId(dataTypeClassification.getId());
	}

	private DataTypeClassification updateDataAssecClassification(DataTypeClassification dataTypeClassification) {
		getResult().setDataTypeClassificationAction(ImportAction.SYNCED);
		dataTypeClassification.setCreatedAt(new Date());
		dataTypeClassification.setUpdatedAt(new Date());
		DataTypeClassification saved = dataTypeClassificationRepository.save(dataTypeClassification);
		applicationEventPublisher.publishEvent(
			new UpdateAuditRecordEvent(this.getClass(),
				VItemType.DATA_TYPE_CLASS,
				saved.getId(),
				new DataTypeClassificationRefDTO(saved),
				Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, dataTypeClassification.getOrganizationId()), AuditLogItemId.of(VItemType.BIG_ID_DATA_TYPE_CLASSIFICATION_SYNC, saved.getId())).toArray(AuditLogItemId[]::new),
				null
			));
		return saved;

	}

	private DataTypeClassification createNewDataAssetClassification(Organizations organization, Users user,
																	DataTypeClassification dataTypeClassification) {
		getResult().setDataTypeClassificationAction(ImportAction.CREATED);
		dataTypeClassification.setName(getParams().getComplianceRuleVO().getName());
		dataTypeClassification.setDescription(String.format("Big Id import: by compliance rule %s, at %s.", getParams().getComplianceRuleVO().getName(),
			YYYY_MM_DD_HH_MM_SS.format(LocalDateTime.now())));
		dataTypeClassification.setOrganizationId(organization.getId());
		dataTypeClassification.setCreatedBy(user);
		dataTypeClassification.setUpdatedBy(user);
		dataTypeClassification.setCreatedAt(new Date());
		dataTypeClassification.setUpdatedAt(new Date());
		DataTypeClassification saved = dataTypeClassificationRepository.save(dataTypeClassification);
		applicationEventPublisher.publishEvent(new CreateAuditRecordEvent(
			this.getClass(),
			VItemType.DATA_TYPE_CLASS,
			saved.getId(),
			new DataTypeClassificationEditDTO(saved),
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()), AuditLogItemId.of(VItemType.BIG_ID_DATA_TYPE_CLASSIFICATION_SYNC, saved.getId())).toArray(AuditLogItemId[]::new)
		));
		return saved;
	}

	private void fillMetadata() {
		DataTypeClassification dataTypeClassification = dataTypeClassificationRepository.findById(getResult().getDataTypeClassificationId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("Data type classification was not found by %s id.", getResult().getDataTypeClassificationId())));
		switch (getResult().getDataTypeClassificationAction()) {
			case CREATED:
				dataTypeClassificationMetadataHelper.fillOnCreate(dataTypeClassification, "compliance-rule", dataTypeClassification.getName(),
					getParams().getComplianceRuleVO().getId(), dataTypeClassification.getOrganizationId());
				break;
			case SYNCED:
				dataTypeClassificationMetadataHelper.fillOnUpdate(dataTypeClassification);
				break;
			default:
		}
		dataTypeClassificationRepository.save(dataTypeClassification);
	}

	private void parseAndConnectWithClassifierFields() {
		DataTypeClassification dataTypeClassification = dataTypeClassificationRepository.findById(getResult().getDataTypeClassificationId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("Data type classification was not found by %s id.", getResult().getDataTypeClassificationId())));
		ComplianceRuleVO complianceRuleVO = getParams().getComplianceRuleVO();
		dataTypeClassification.getFieldClassifiers().clear();
		if (complianceRuleVO.getComplianceRuleCalc() == null ||
			StringUtils.isBlank(complianceRuleVO.getComplianceRuleCalc().getBigidQuery())) {
			dataTypeClassificationRepository.save(dataTypeClassification);
			return;
		}
		String query = complianceRuleVO.getComplianceRuleCalc().getBigidQuery();
		Set<String> fieldClassifiers = ComplianceRuleQueryUtil.parseListOfFieldClassifiers(query);
		for (String classifier : fieldClassifiers) {
			Optional<FieldClassifiers> maybeClassifier = fieldClassifierRepository.findByName(classifier);
			if (maybeClassifier.isEmpty()) {
				continue;
			}
			FieldClassifiers fieldClassifier = maybeClassifier.get();
			dataTypeClassification.getFieldClassifiers().add(fieldClassifier);
		}
		dataTypeClassificationRepository.save(dataTypeClassification);
	}

}
