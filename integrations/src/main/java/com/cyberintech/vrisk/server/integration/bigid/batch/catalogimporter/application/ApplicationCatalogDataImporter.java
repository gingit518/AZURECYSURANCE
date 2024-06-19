package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application;

import static com.cyberintech.vrisk.server.integration.bigid.batch.util.FunctionUtil.getOr;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.CatalogDataImporterBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.CatalogImportMetadataConstants;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.ImportAction;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.CountryStateHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.CreateAuditRecordEvent;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.UpdateAuditRecordEvent;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.SystemMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.util.DateTimeFormatterUtil;
import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClientFactory;
import com.cyberintech.vrisk.server.integration.bigid.client.application.vo.ApplicationVO;
import com.cyberintech.vrisk.server.integration.bigid.client.attributerisk.vo.AttributeRiskVO;
import com.cyberintech.vrisk.server.integration.bigid.client.identitylocation.vo.IdentityLocationVO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.DataTypeClassificationRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class ApplicationCatalogDataImporter
	extends CatalogDataImporterBase<ApplicationCatalogDataImporterParamVO, ApplicationCatalogDataImporterResultVO> {

	private final CountryStateHelper countryStateHelper;
	private final SystemMetadataHelper systemMetadataHelper;
	private final SystemRepository systemRepository;
	private final BigIdClientFactory clientFactory;
	private final DataTypeClassificationRepository dataTypeClassificationRepository;

	public ApplicationCatalogDataImporter(OrganizationRepository organizationRepository,
										  UserRepository userRepository,
										  ApplicationEventPublisher applicationEventPublisher,
										  UserMetadataHelper userMetadataHelper, UserImportHelper userImportHelper,
										  CountryStateHelper countryStateHelper, SystemMetadataHelper systemMetadataHelper,
										  SystemRepository systemRepository,
										  BigIdClientFactory clientFactory,
										  DataTypeClassificationRepository dataTypeClassificationRepository) {
		super(organizationRepository, userRepository, applicationEventPublisher, userMetadataHelper, userImportHelper);
		this.countryStateHelper = countryStateHelper;
		this.systemMetadataHelper = systemMetadataHelper;
		this.systemRepository = systemRepository;
		this.clientFactory = clientFactory;
		this.dataTypeClassificationRepository = dataTypeClassificationRepository;
	}

	@Override
	public boolean supports(Object data) {
		return data instanceof ApplicationCatalogDataImporterParamVO;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected ApplicationCatalogDataImporterResultVO initResult(ApplicationCatalogDataImporterParamVO params) {
		return new ApplicationCatalogDataImporterResultVO(params.getOrganizationId(), params.getUserImporterId(), params.getApplicationVO());
	}

	@Override
	protected ApplicationCatalogDataImporterResultVO process(Organizations organization, Users user) {
		log.info("Processing remote application: {}, organization = {}, user importer = {}.", getParams().getApplicationVO(),
			new OrganizationRefDTO(organization), new UserRefDTO(user));

		processOwner(organization, user);
		processApplication(organization, user);
		fillMetadata();

		log.info("Application '{}' was processed. System was created: {}.", getParams().getApplicationVO(),
			new SystemRefDTO(systemRepository.findById(getResult().getSystemId()).orElseThrow(() -> new ItemNotFoundException(String.format("System not found by %s id.", getResult().getSystemId())))));
		return getResult();
	}

	private void resetAndStoreGeoSystemData(Systems systems, List<IdentityLocationVO> identityLocations) {
		log.info("Reset and store system geo data. System = {}, dataCatalogs: {}.", new SystemRefDTO(systems), identityLocations);
		systems.getSystemGeoParameters().clear();
		for (IdentityLocationVO identityLocation : identityLocations) {
			SystemGeoParameters systemGeoParameters = new SystemGeoParameters();
			systemGeoParameters.setSystem(systems);
			countryStateHelper.setCountryStatePair(identityLocation.getName(),
				systemGeoParameters::setState, systemGeoParameters::setCountry);
			systemGeoParameters.setNumberOfRecProcessed((double) identityLocation.getCount());
			systems.getSystemGeoParameters().add(systemGeoParameters);
		}
		systemRepository.save(systems);
		log.info("Finished to reset and store system geo data.");
	}

	private void setSumOfProcessedRecords(Systems system, List<IdentityLocationVO> identityLocations) {
		log.info("Store total records processed based on PII distribution (identity locations). System = {}, dataCatalogs: {}.",
			new SystemRefDTO(system), identityLocations);
		double acc = 0.0d;
		for (IdentityLocationVO identityLocation : identityLocations) {
			acc += identityLocation.getCount();
		}
		system.setNumberOfRecProcessed(acc);
		systemRepository.save(system);
		log.info("Finished to store total records processed based on PII distribution (identity locations).");
	}

	private void processApplication(Organizations currentOrganization, Users currentUser) {
		Systems system = mapToSystem(currentOrganization, currentUser, getParams().getApplicationVO());

		system.setOwner(userRepository.findByEmail(getParams().getApplicationVO().getOwnerEmail()).orElse(null));

		boolean isNewSystem = system.getId() == null;
		getResult().setSystemAction(isNewSystem ? ImportAction.CREATED : ImportAction.SYNCED);

		system = isNewSystem ? createSystem(currentOrganization, currentUser, system) : udpateSystem(currentOrganization, system);
		getResult().setSystemId(system.getId());

		List<IdentityLocationVO> identityLocations = clientFactory.createIdentityLocationClient(currentOrganization.getId()).getBySystemDatasource(getParams().getApplicationVO().getTargetDataSource());

		resetAndStoreGeoSystemData(system, identityLocations);

		setSumOfProcessedRecords(system, identityLocations);

		setDataTypeClassifications(system);
	}

	private Systems mapToSystem(Organizations currentOrganization, Users currentUser, ApplicationVO applicationVO) {
		Systems system = systemRepository
			.findByOrganizationAndMeta(currentOrganization.getId(), CatalogImportMetadataConstants.SOURCE_ID, applicationVO.getIdInternal())
			.orElseGet(Systems::new);
		system.setName(applicationVO.getName());
		system.setSystemStatus(SystemStatus.ACTIVE);
		system.setVersionNumber(applicationVO.getVersion());
		system.setSystemType(null);
		system.setUpdatedAt(new Date());
		system.setUpdatedBy(currentUser);
		system.setDescription(String.format("Big Id import: by application %s, at %s.", applicationVO.getName(), DateTimeFormatterUtil.YYYY_MM_DD_HH_MM_SS.format(LocalDateTime.now())));
		return system;
	}

	private void setDataTypeClassifications(Systems system) {
		if (system.getNumberOfRecProcessed() > 0) {
			getDataTypeClassification("PII", system.getOrganizationId()).ifPresent(system.getDataTypeClassifications()::add);
		}

		if (StringUtils.isNoneBlank(getParams().getApplicationVO().getName(), getParams().getApplicationVO().getTargetDataSource())) {
			List<AttributeRiskVO> attributeRisks = clientFactory.createAttributeRiskClient(system.getOrganizationId()).getAll(getParams().getApplicationVO().getName(), getParams().getApplicationVO().getTargetDataSource());
			List<String> fieldClassifierNames = new LinkedList<>();
			for (AttributeRiskVO attributeRisk : attributeRisks) {
				if (attributeRisk.getCount() > 0) {
					String id = attributeRisk.getId();
					if (StringUtils.startsWith(id, "classifier.")) {
						fieldClassifierNames.add(StringUtils.removeStart(id, "classifier."));
					}
				}
			}
			for (String fieldClassifierName : fieldClassifierNames) {
				Optional.ofNullable(dataTypeClassificationRepository.getDtcListByFieldClassifierNameAndOrganizationAndMeta(
						fieldClassifierName, system.getOrganizationId(), CatalogImportMetadataConstants.SOURCE_SYSTEM,
						CatalogImportMetadataConstants.SOURCE_SYSTEM_BIG_ID)).orElse(Collections.emptySet())
					.forEach(system.getDataTypeClassifications()::add);
			}
		}
		systemRepository.save(system);
	}

	private Systems udpateSystem(Organizations organization, Systems system) {
		log.info("Application exists. Updating.");
		system = systemRepository.save(system);
		applicationEventPublisher.publishEvent(new UpdateAuditRecordEvent(
			this.getClass(),
			VItemType.SYSTEM,
			system.getId(),
			new SystemRefDTO(system),
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()), AuditLogItemId.of(VItemType.BIG_ID_SYSTEM_SYNC, system.getId())).toArray(AuditLogItemId[]::new),
			null
		));
		return system;
	}

	private void fillMetadata() {
		Systems system = systemRepository.findById(getResult()
			.getSystemId()).orElseThrow(() -> new ItemNotFoundException(String.format("System was not found by %s id.", getResult().getSystemId())));
		switch (getResult().getSystemAction()) {
			case CREATED:
				systemMetadataHelper.fillOnCreate(system, "application", system.getName(), getResult().getApplicationVO().getIdInternal(), system.getOrganizationId());
				break;
			case SYNCED:
				systemMetadataHelper.fillOnUpdate(system);
				break;
			default:
		}
		systemRepository.save(system);
	}

	private Systems createSystem(Organizations organization, Users user, Systems system) {
		log.info("Application does not exists. Creating.");
		system.setOrganizationId(organization.getId());
		system.setCreatedAt(system.getUpdatedAt());
		system.setCreatedBy(user);
		system = systemRepository.save(system);
		applicationEventPublisher.publishEvent(new CreateAuditRecordEvent(
			this.getClass(),
			VItemType.SYSTEM,
			system.getId(),
			new SystemRefDTO(system),
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()), AuditLogItemId.of(VItemType.BIG_ID_SYSTEM_SYNC, system.getId())).toArray(AuditLogItemId[]::new)
		));
		return system;
	}

	private Optional<DataTypeClassification> getDataTypeClassification(String name, Long organizationId) {

		Optional<DataTypeClassification> pii = getOr(() -> dataTypeClassificationRepository.getFirstByNameForOrganization(name, organizationId), Optional.empty());
		if (pii.isPresent()) {
			return pii;
		}

		pii = getOr(() -> dataTypeClassificationRepository.findByNameAndOrganizationIdIsNull(name), Optional.empty());
		return pii;
	}

	private void processOwner(Organizations organization, Users user) {
		ApplicationVO application = getParams().getApplicationVO();
		Users systemOwner = userImportHelper.mapToUser(organization, user,
			application.getOwnerEmail(), application.getOwnerName(),
			application.getOwnerPhone(), List.of(RoleType.SYSTEM_OWNER));
		if (systemOwner != null) {
			if (systemOwner.getId() == null) {
				getResult().setOwnerAction(ImportAction.CREATED);
				systemOwner = userImportHelper.createUser(organization, user, systemOwner);
				userMetadataHelper.fillOnCreate(systemOwner, "application:owner",
					application.getName() + ":" + application.getOwnerEmail(), "system-owner");
				systemOwner = userRepository.save(systemOwner);
			} else {
				getResult().setOwnerAction(ImportAction.SYNCED);
				userMetadataHelper.fillOnUpdate(systemOwner);
				systemOwner = userImportHelper.updateUser(organization, systemOwner);
			}
			getResult().setOwnerId(systemOwner.getId());
			getResult().setOwnerName(systemOwner.getFullName());
			getResult().setOwnerEmail(systemOwner.getEmail());
		}
	}

}
