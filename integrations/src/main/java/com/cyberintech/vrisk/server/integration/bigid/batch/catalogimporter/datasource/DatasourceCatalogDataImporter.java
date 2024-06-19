package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.CatalogDataImporterBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.CatalogImportMetadataConstants;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.ImportAction;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.CountryStateHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.UserImportHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.CreateAuditRecordEvent;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.audit.vo.UpdateAuditRecordEvent;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.OrganizationMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.TechnologyCategoryMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.TechnologyMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.metadata.UserMetadataHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.util.DateTimeFormatterUtil;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceVO;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.OwnerVO;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.TagVO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Technologies;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyCategoryRepository;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

import static com.cyberintech.vrisk.server.integration.bigid.batch.common.TagConstants.INFO_SEC_PERSON_EMAIL;
import static com.cyberintech.vrisk.server.integration.bigid.batch.common.TagConstants.ORGANIZATION_PARENT_NAME;
import static com.cyberintech.vrisk.server.integration.bigid.batch.common.TagConstants.ORGANIZATION_SUBSIDIARY_NAME;

@Slf4j
public class DatasourceCatalogDataImporter extends CatalogDataImporterBase<DatasourceCatalogDataImporterParamVO, DatasourceCatalogDataImporterResultVO> {

	private final TechnologyCategoryRepository technologyCategoryRepository;
	private final TechnologyRepository technologyRepository;
	private final CountryStateHelper countryStateHelper;
	private final TechnologyCategoryMetadataHelper technologyCategoryMetadataHelper;
	private final TechnologyMetadataHelper technologyMetadataHelper;
	private final OrganizationMetadataHelper organizationMetadataHelper;

	public DatasourceCatalogDataImporter(OrganizationRepository organizationRepository,
										 ApplicationEventPublisher applicationEventPublisher,
										 UserMetadataHelper userMetadataHelper,
										 UserImportHelper userImportHelper,
										 UserRepository userRepository,
										 TechnologyCategoryRepository technologyCategoryRepository,
										 TechnologyRepository technologyRepository,
										 CountryStateHelper countryStateHelper,
										 TechnologyCategoryMetadataHelper technologyCategoryMetadataHelper,
										 TechnologyMetadataHelper technologyMetadataHelper, OrganizationMetadataHelper organizationMetadataHelper) {
		super(organizationRepository, userRepository, applicationEventPublisher, userMetadataHelper, userImportHelper);
		this.technologyCategoryRepository = technologyCategoryRepository;
		this.technologyRepository = technologyRepository;
		this.countryStateHelper = countryStateHelper;
		this.technologyCategoryMetadataHelper = technologyCategoryMetadataHelper;
		this.technologyMetadataHelper = technologyMetadataHelper;
		this.organizationMetadataHelper = organizationMetadataHelper;
	}

	@Override
	protected DatasourceCatalogDataImporterResultVO process(Organizations currentOrganization, Users currentUser) {
		processTechnologyCategory(currentOrganization, currentUser);
		fillTechnologyCategoryMeta();

		processTechnology(currentOrganization, currentUser);
		fillTechnologyMeta();

		connectTechnologyCategoryAndTechnology();

		processTechnologyOwners(currentOrganization, currentUser);

		processInfoSecPerson(currentOrganization, currentUser);
		fillInfoSecMetadata();

		processOrganizationOwner();
		fillParentOrganizationMeta();

		fillSubOrganizationMeta();

		return getResult();
	}

	@Override
	public boolean supports(Object data) {
		return data instanceof DatasourceCatalogDataImporterParamVO;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected DatasourceCatalogDataImporterResultVO initResult(DatasourceCatalogDataImporterParamVO params) {
		return new DatasourceCatalogDataImporterResultVO(params.getOrganizationId(), params.getUserImporterId(), params.getDatasourceVO());
	}

	private void processTechnologyCategory(Organizations organization, Users user) {
		log.info("Processing technology category by datasource type: {}, organization = {}, user importer = {}.", getParams().getDatasourceVO().getType(),
			new OrganizationRefDTO(organization), new UserRefDTO(user));

		TechnologyCategories technologyCategory = technologyCategoryRepository.findByOrganizationAndMeta(
				organization.getId(),
				CatalogImportMetadataConstants.SOURCE_ID, getParams().getDatasourceVO().getIdInternal())
			.orElseGet(TechnologyCategories::new);

		boolean isNewTechCategory = technologyCategory.getId() == null;
		technologyCategory.setName(getParams().getDatasourceVO().getType());
		technologyCategory.setUpdatedAt(new Date());
		technologyCategory.setUpdatedBy(user);
		technologyCategory.setDescription(String.format("Big Id import: by datasource %s, datasource type %s, at %s",
			getParams().getDatasourceVO().getName(), getParams().getDatasourceVO().getType(),
			DateTimeFormatterUtil.YYYY_MM_DD_HH_MM_SS.format(LocalDateTime.now())));

		if (isNewTechCategory) {
			log.info("Technology category by datasource type '{}' does not exist. Creating.", getParams().getDatasourceVO().getType());
			technologyCategory = creteTechCategory(organization, user, technologyCategory);
		} else {
			log.info("Technology category by datasource type '{}' exists. Updating.", getParams().getDatasourceVO().getType());
			technologyCategory = updateTechnologyCategory(organization, technologyCategory);
		}
		getResult().setTechnologyCategoryId(technologyCategory.getId());
		getResult().setTechnologyCategoryAction(isNewTechCategory ? ImportAction.CREATED : ImportAction.SYNCED);
		log.info("Technology category by datasource type '{}' was processed.", getParams().getDatasourceVO().getType());
	}

	private TechnologyCategories updateTechnologyCategory(Organizations organization, TechnologyCategories technologyCategory) {
		TechnologyCategories saved = technologyCategoryRepository.save(technologyCategory);
		applicationEventPublisher.publishEvent(new UpdateAuditRecordEvent(this.getClass(),
			VItemType.TECHNOLOGY_CATEGORY,
			saved.getId(),
			new TechnologyCategoryRefDTO(saved),
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()),
				AuditLogItemId.of(VItemType.BIG_ID_TECHNOLOGY_CATEGORY_SYNC, saved.getId())).toArray(AuditLogItemId[]::new),
			null));
		return saved;
	}

	private TechnologyCategories creteTechCategory(Organizations organization, Users user, TechnologyCategories technologyCategory) {
		technologyCategory.setOrganizationId(organization.getId());
		technologyCategory.setCreatedAt(technologyCategory.getUpdatedAt());
		technologyCategory.setCreatedBy(user);
		TechnologyCategories saved = technologyCategoryRepository.save(technologyCategory);
		applicationEventPublisher.publishEvent(new CreateAuditRecordEvent(this.getClass(),
			VItemType.TECHNOLOGY_CATEGORY,
			saved.getId(),
			new TechnologyCategoryRefDTO(saved),
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()),
				AuditLogItemId.of(VItemType.BIG_ID_TECHNOLOGY_CATEGORY_SYNC, saved.getId())).toArray(AuditLogItemId[]::new)
		));
		return saved;
	}

	private void processTechnology(Organizations organization, Users userImporter) {
		log.info("Processing technology by datasource: {}, organization = {}, user importer = {}.", getParams().getDatasourceVO(),
			new OrganizationRefDTO(organization), new UserRefDTO(userImporter));
		Technologies technology = technologyRepository.findByOrganizationAndMeta(organization.getId(),
				CatalogImportMetadataConstants.SOURCE_ID, getParams().getDatasourceVO().getIdInternal())
			.orElseGet(Technologies::new);
		boolean isNewTechnology = technology.getId() == null;
		technology.setName(getParams().getDatasourceVO().getName());
		technology.setUpdatedAt(new Date());
		technology.setUpdatedBy(userImporter);
		technology.setDescription(String.format("Big Id import: by datasource %s, at %s", getParams().getDatasourceVO().getName(),
			DateTimeFormatterUtil.YYYY_MM_DD_HH_MM_SS.format(LocalDateTime.now())));
		countryStateHelper.setCountryStatePair(getParams().getDatasourceVO().getLocation(), technology::setState, technology::setCountry);
		if (isNewTechnology) {
			log.info("Technology by datasource name '{}' does not exist. Creating.", getParams().getDatasourceVO().getName());
			technology = createTechnology(organization, userImporter, technology);
		} else {
			log.info("Technology by datasource name '{}' exists. Updating.", getParams().getDatasourceVO().getName());
			technology = updateTechnology(organization, technology);
		}
		getResult().setTechnologyId(technology.getId());
		getResult().setTechnologyAction(isNewTechnology ? ImportAction.CREATED : ImportAction.SYNCED);
		log.info("Technology by datasource name '{}' was processed.", getParams().getDatasourceVO().getName());
	}

	private Technologies updateTechnology(Organizations organization, Technologies technology) {
		technology = technologyRepository.save(technology);
		applicationEventPublisher.publishEvent(new UpdateAuditRecordEvent(this.getClass(),
			VItemType.TECHNOLOGY,
			technology.getId(),
			new TechnologyRefDTO(technology),
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()),
				AuditLogItemId.of(VItemType.BIG_ID_TECHNOLOGY_SYNC, technology.getId())).toArray(AuditLogItemId[]::new), null));
		return technology;
	}

	private Technologies createTechnology(Organizations organization, Users userImporter, Technologies technology) {
		technology.setOrganizationId(organization.getId());
		technology.setCreatedAt(technology.getUpdatedAt());
		technology.setCreatedBy(userImporter);
		technology = technologyRepository.save(technology);
		applicationEventPublisher.publishEvent(new CreateAuditRecordEvent(
			this.getClass(),
			VItemType.TECHNOLOGY,
			technology.getId(),
			new TechnologyRefDTO(technology),
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()),
				AuditLogItemId.of(VItemType.BIG_ID_TECHNOLOGY_SYNC, technology.getId())).toArray(AuditLogItemId[]::new)
		));
		return technology;
	}

	private void processTechnologyOwners(Organizations currentOrganization, Users currentUser) {
		log.info("Started to process datasource owners list: {}.", getParams().getDatasourceVO().getOwners());
		if (CollectionUtils.isEmpty(getParams().getDatasourceVO().getOwners())) {
			log.info("Datasource {} does not have any owners.", getParams().getDatasourceVO().getName());
			return;
		}
		Technologies technology = technologyRepository.findByOrganizationAndMeta(currentOrganization.getId(),
				CatalogImportMetadataConstants.SOURCE_ID, getParams().getDatasourceVO().getIdInternal())
			.orElseThrow(() -> new ItemNotFoundException(String.format("No technology found by %s name and %s organization id.", getParams().getDatasourceVO().getName(), currentOrganization.getId())));
		technology.setItOwner(null);
		technology.setBusinessOwner(null);
		for (OwnerVO owner : getParams().getDatasourceVO().getOwners()) {
			if (Objects.isNull(owner.getType())) {
				continue;
			}
			switch (owner.getType()) {
				case IT:
					processUser(currentOrganization, currentUser, owner.getEmail(),
						getResult()::setTechnologyItOwnerAction, getResult()::setTechnologyItOwnerId,
						getResult()::setTechnologyItOwnerEmail,
						technology::setItOwner, List.of(RoleType.TECH_IT_OWNER));
					fillOwnerUserMetadata(getResult().getTechnologyItOwnerId(), getResult().getDatasourceVO(), getResult().getTechnologyItOwnerAction(), "it-owner");
					break;
				case BUSINESS:
					processUser(currentOrganization, currentUser, owner.getEmail(),
						getResult()::setTechnologyBusinessOwnerAction, getResult()::setTechnologyBusinessOwnerId,
						getResult()::setTechnologyBusinessOwnerEmail,
						technology::setBusinessOwner, List.of(RoleType.BUSINESS_UNIT_OWNER));
					fillOwnerUserMetadata(getResult().getTechnologyBusinessOwnerId(), getResult().getDatasourceVO(), getResult().getTechnologyBusinessOwnerAction(), "business-owner");
					break;
				default:
					log.warn("Unexpected owner type: {}", owner.getType());
			}
		}
		technologyRepository.save(technology);
	}

	private void processUser(Organizations currentOrganization, Users currentUser, String email,
							 Consumer<ImportAction> actionConsumer,
							 Consumer<Long> idConsumer,
							 Consumer<String> emailConsumer,
							 Consumer<Users> usersConsumer,
							 List<RoleType> roles) {
		Users systemOwner = userImportHelper.mapToUser(currentOrganization, currentUser, email, null, null, roles);
		if (systemOwner == null) {
			idConsumer.accept(null);
			usersConsumer.accept(null);
		} else {
			if (systemOwner.getId() == null) {
				actionConsumer.accept(ImportAction.CREATED);
				systemOwner = userImportHelper.createUser(currentOrganization, currentUser, systemOwner);
				idConsumer.accept(systemOwner.getId());
				usersConsumer.accept(systemOwner);
				emailConsumer.accept(email);
			} else {
				actionConsumer.accept((ImportAction.SYNCED));
				systemOwner = userImportHelper.updateUser(currentOrganization, systemOwner);
				idConsumer.accept(systemOwner.getId());
				usersConsumer.accept(systemOwner);
				emailConsumer.accept(email);
			}
		}
	}

	private void connectTechnologyCategoryAndTechnology() {
		Technologies technology = technologyRepository.findById(getResult().getTechnologyId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("No technology found by %s id.", getResult().getTechnologyId())));
		TechnologyCategories technologyCategory = technologyCategoryRepository.findById(getResult().getTechnologyCategoryId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("No technology category found by %s id.", getResult().getTechnologyCategoryId())));
		technology.setTechnologyCategory(technologyCategory);
		technologyRepository.save(technology);
	}

	private void fillTechnologyCategoryMeta() {
		TechnologyCategories technologyCategory = technologyCategoryRepository.findById(getResult().getTechnologyCategoryId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("Technology Category was not found by %s id.", getResult().getTechnologyCategoryId())));
		switch (getResult().getTechnologyCategoryAction()) {
			case CREATED:
				technologyCategoryMetadataHelper.fillOnCreate(technologyCategory, "datasource",
					getResult().getDatasourceVO().getName(),
					getResult().getDatasourceVO().getIdInternal(),
					technologyCategory.getOrganizationId());
				break;
			case SYNCED:
				technologyCategoryMetadataHelper.fillOnUpdate(technologyCategory);
				break;
			default:
		}
		technologyCategoryRepository.save(technologyCategory);
	}

	private void fillTechnologyMeta() {
		Technologies technology = technologyRepository.findById(getResult().getTechnologyId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("Technology was not found by %s id.", getResult().getTechnologyCategoryId())));
		switch (getResult().getTechnologyCategoryAction()) {
			case CREATED:
				technologyMetadataHelper.fillOnCreate(technology, "datasource",
					getResult().getDatasourceVO().getName(),
					getResult().getDatasourceVO().getIdInternal(),
					technology.getOrganizationId());
				break;
			case SYNCED:
				technologyMetadataHelper.fillOnUpdate(technology);
				break;
			default:
		}
		technologyRepository.save(technology);
	}

	private void fillOwnerUserMetadata(Long ownerId, DatasourceVO datasourceVO, ImportAction ownerImportAction, String metaUserType) {
		if (ownerId == null) {
			return;
		}
		Optional<Users> maybeUser = userRepository.findById(ownerId);
		if (maybeUser.isEmpty()) {
			return;
		}
		Users ownerUser = maybeUser.get();
		switch (ownerImportAction) {
			case CREATED:
				userMetadataHelper.fillOnCreate(ownerUser, "datasource", datasourceVO.getName() + ":" + "owner", metaUserType);
				break;
			case SYNCED:
				userMetadataHelper.fillOnUpdate(ownerUser);
				break;
			default:
		}
		userRepository.save(ownerUser);
	}

	private void processInfoSecPerson(Organizations currentOrganization, Users currentUser) {
		log.info("Started to process info sec person.");
		Technologies technology = technologyRepository.findByOrganizationAndMeta(currentOrganization.getId(),
				CatalogImportMetadataConstants.SOURCE_ID, getParams().getDatasourceVO().getIdInternal())
			.orElseThrow(() -> new ItemNotFoundException(String.format("No technology found by %s name and %s organization id.", getResult().getDatasourceVO(), currentOrganization.getId())));

		if (CollectionUtils.isEmpty(getParams().getDatasourceVO().getTags())) {
			technology.setInfosecFocalPerson(null);
			technologyRepository.save(technology);
			log.info("Datasource does not have any tags.");
			return;
		}

		Optional<TagVO> maybeInfoSecTag = getParams().getDatasourceVO().getTags().stream().filter(t -> INFO_SEC_PERSON_EMAIL.equals(t.getTagName())).findFirst();
		if (maybeInfoSecTag.isEmpty()) {
			technology.setInfosecFocalPerson(null);
			technologyRepository.save(technology);
			log.info("Info sec tag was not found.");
			return;
		}

		TagVO tagVO = maybeInfoSecTag.get();
		if (StringUtils.isEmpty(tagVO.getTagValue())) //TODO email validation
		{
			technology.setInfosecFocalPerson(null);
			technologyRepository.save(technology);
			log.warn("Info sec tag has blank value.");
			return;
		}

		processUser(currentOrganization, currentUser, tagVO.getTagValue(), getResult()::setInfoSecFocalAction,
			getResult()::setInfoSecFocalId, getResult()::setInfoSecFocalEmail,
			technology::setInfosecFocalPerson, List.of(RoleType.INFORMATION_SECURITY));

		technologyRepository.save(technology);
	}

	// TODO generify with other similar methods
	private void fillInfoSecMetadata() {
		if (getResult().getInfoSecFocalId() == null) {
			return;
		}
		Optional<Users> maybeUser = userRepository.findById(getResult().getInfoSecFocalId());
		if (maybeUser.isEmpty()) {
			return;
		}
		Users ownerUser = maybeUser.get();
		switch (getResult().getInfoSecFocalAction()) {
			case CREATED:
				userMetadataHelper.fillOnCreate(ownerUser, "datasource",
					getParams().getDatasourceVO().getName() + ":tags:" + INFO_SEC_PERSON_EMAIL, "info-sec");
				break;
			case SYNCED:
				userMetadataHelper.fillOnUpdate(ownerUser);
				break;
			default:
		}
		userRepository.save(ownerUser);
	}

	private void processOrganizationOwner() {
		log.info("Processing organization owner tags.");
		Optional<TagVO> maybeRootOrgOwnerName = getParams().getDatasourceVO().getTags().stream().filter(t -> ORGANIZATION_PARENT_NAME.equals(t.getTagName())).findFirst();

		Organizations rootOrg = null;
		if (maybeRootOrgOwnerName.isPresent()) {
			TagVO parentOrgTag = maybeRootOrgOwnerName.get();
			String rootOrgName = parentOrgTag.getTagValue();
			log.info("Found ORGANIZATION_PARENT_NAME tag with '{}' value.", rootOrgName);
			if (StringUtils.isNotBlank(rootOrgName)) {
				rootOrg = processDsOrganizationTag(getResult()::setParentOrganizationId, getResult()::setParentOrganizationAction,
					null, rootOrgName, OrganizationType.Organization, VItemType.BIG_ID_ROOT_ORGANIZATION_SYNC);
				getResult().setParentOrganizationName(rootOrgName);
			}
		}

		Optional<TagVO> maybeSubsOrgOwnerName = getParams().getDatasourceVO().getTags()
			.stream().filter(t -> ORGANIZATION_SUBSIDIARY_NAME.equals(t.getTagName())).findFirst();
		Organizations subsOrg = null;
		if (maybeSubsOrgOwnerName.isPresent() && rootOrg != null) {
			TagVO parentOrgTag = maybeSubsOrgOwnerName.get();
			String subsOrgName = parentOrgTag.getTagValue();
			log.info("Found ORGANIZATION_SUBSIDIARY_NAME tag with '{}' value.", subsOrgName);
			if (StringUtils.isNotBlank(subsOrgName)) {
				subsOrg = processDsOrganizationTag(getResult()::setSubOrganizationId, getResult()::setSubOrganizationAction, rootOrg,
					subsOrgName, OrganizationType.Subsidiary, VItemType.BIG_ID_SUB_ORGANIZATION_SYNC);
				getResult().setSubOrganizationName(subsOrgName);
			}
		}

		if (subsOrg != null && !Objects.equals(subsOrg.getId(), rootOrg.getId())) {
			log.info("Subs org and root org are different. Linking root org with subs org.");
			subsOrg.setRootParent(rootOrg);
			subsOrg = organizationRepository.save(subsOrg);
		}

		Technologies technologies = technologyRepository.findById(getResult().getTechnologyId())
			.orElseThrow(() -> new ItemNotFoundException(String.format("No technology found by %s id.", getResult().getTechnologyId())));
		if (subsOrg != null) {
			log.info("Setting subs org as owning org for technology.");
			technologies.setOrganizationOwnerId(subsOrg.getId());
			technologyRepository.save(technologies);
		} else if (rootOrg != null) {
			log.info("Setting root org as owning org for technology.");
			technologies.setOrganizationOwnerId(rootOrg.getId());
			technologyRepository.save(technologies);
		} else {
			log.info("Setting null as owning org for technology.");
			technologies.setOrganizationOwnerId(null);
			technologyRepository.save(technologies);
		}
	}

	private Organizations processDsOrganizationTag(LongConsumer organizationIdConsumer, Consumer<ImportAction> organizationImportActionConsumer,
												   Organizations rootOrg, String organizationName,
												   OrganizationType organizationType, VItemType vItemType) {
		Organizations subsOrg;
		Organizations organization = organizationRepository
			.findByNameAndOrganizationType(organizationName, organizationType).orElseGet(Organizations::new);
		boolean isNew = organization.getId() == null;
		if (isNew) {
			organization.setName(organizationName);
			organization.setOrganizationType(organizationType);
			organization.setRootParent(rootOrg);
			organization = organizationRepository.save(organization);
			organizationIdConsumer.accept(organization.getId());
			organizationImportActionConsumer.accept(ImportAction.CREATED);
			subsOrg = organization;
			applicationEventPublisher.publishEvent(new CreateAuditRecordEvent(
				this.getClass(),
				VItemType.ORGANIZATION,
				organization.getId(),
				new OrganizationRefDTO(organization),
				Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()),
					AuditLogItemId.of(vItemType, organization.getId())).toArray(AuditLogItemId[]::new)
			));
		} else {
			subsOrg = organization;
			organizationIdConsumer.accept(organization.getId());
			organizationImportActionConsumer.accept(ImportAction.SYNCED);
		}
		return subsOrg;
	}

	private void fillParentOrganizationMeta() {
		fillOrganizationMetadata(getParams().getDatasourceVO(), getResult().getParentOrganizationId(), getResult().getParentOrganizationAction());
	}

	private void fillSubOrganizationMeta() {
		fillOrganizationMetadata(getParams().getDatasourceVO(), getResult().getSubOrganizationId(), getResult().getSubOrganizationAction());
	}

	private void fillOrganizationMetadata(DatasourceVO datasourceVO, Long organizationId, ImportAction orgImportAction) {
		if (organizationId == null) {
			return;
		}
		Optional<Organizations> maybeOrg = organizationRepository.findById(organizationId);
		if (maybeOrg.isEmpty()) {
			return;
		}
		Organizations organization = maybeOrg.get();
		switch (orgImportAction) {
			case CREATED:
				String tagType = organization.getOrganizationType().equals(OrganizationType.Subsidiary) ? ORGANIZATION_SUBSIDIARY_NAME : ORGANIZATION_PARENT_NAME;
				organizationMetadataHelper.fillOnCreate(organization, "datasource", datasourceVO.getName() + ":tags:" + tagType, organization.getRootParent().getId());
				break;
			case SYNCED:
				organizationMetadataHelper.fillOnUpdate(organization);
				break;
			default:
		}
		organizationRepository.save(organization);
	}
}
