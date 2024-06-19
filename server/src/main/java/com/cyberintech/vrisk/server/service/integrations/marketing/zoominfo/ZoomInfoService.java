package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo;

import com.cyberintech.vrisk.server.context.ApplicationContextThreadLocal;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitEditDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationEditDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemEditDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyEditDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryEditDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.service.AssociateVendorService;
import com.cyberintech.vrisk.server.service.AuditLogService;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.UserService;
import com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Zoom Info Marketing integrations
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-02-23
 */
@Service
@Slf4j
public class ZoomInfoService {

	private static final String ENTERPRISE_API_AUDIENCE = "enterprise_api";
	private static final String USERNAME_CLAIM = "username";
	private static final String CLIENT_ID_CLAIM = "client_id";
	private static final String ISSUER = "api-client@zoominfo.com";
	private static final String BASE_API_URL = "https://api.zoominfo.com";
	private static final String AUTHENTICATE_URL = "/authenticate";
	private static final String ORGANIZATION_SEARCH_ENDPOINT = "/search/company";
	private static final String ORGANIZATION_ENRICH_ENDPOINT = "/enrich/company";
	private static final String ORGANIZATION_HIERARCHY_ENRICH_ENDPOINT = "/enrich/corporatehierarchy";
	private static final String ORG_CHART_ENRICH_ENDPOINT = "/enrich/orgchart";

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	@Autowired
	private AssociateVendorService associateVendorService;

	@Autowired
	private BusinessUnitRepository businessUnitRepository;

	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private IndustryRepository industryRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationSynchronizationExternalLogRepository organizationSynchronizationExternalLogRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private TechnologyCategoryRepository technologyCategoryRepository;

	@Autowired
	private TechnologyRepository technologyRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private ZoomInfoConfig zoomInfoConfig;

	@Autowired
	private ZoomInfoCache zoomInfoCache;

	@PostConstruct
	public void initializeZoomInfo() {
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * Searching organizations across Zoom Info database
	 *
	 * @param filteredRequest
	 */
	public FilteredResponse<OrganizationSearchFilter, OrganizationSearchItem> searchOrganizations(FilteredRequest<OrganizationSearchFilter> filteredRequest) {
		FilteredResponse<OrganizationSearchFilter, OrganizationSearchItem> filteredResponse = new FilteredResponse<OrganizationSearchFilter, OrganizationSearchItem>(filteredRequest);

		OrganizationSearchRequest searchRequest = OrganizationSearchRequest.of(filteredRequest.getFilter());
		ResponseEntity<PagedSearchResults> responseEntity = postForEntity(BASE_API_URL + ORGANIZATION_SEARCH_ENDPOINT, searchRequest, PagedSearchResults.class);

		PagedSearchResults searchResults = responseEntity.getBody();
		if (searchResults == null) {
			throw new RuntimeException("Could not obtain organizations from Zoom Info");
		}
		filteredResponse.setItems(searchResults.getData());

		return filteredResponse;
	}

	/**
	 * Searching organizations across Zoom Info database
	 *
	 * @param filteredRequest
	 */
	public OrganizationEnrichResponse enrichOrganizations(FilteredRequest<OrganizationSearchFilter> filteredRequest) {
		OrganizationEnrichResponse filteredResponse = null;

		OrganizationEnrichRequest searchRequest = OrganizationEnrichRequest.of(filteredRequest.getFilter());
		ResponseEntity<OrganizationEnrichResponse> responseEntity = postForEntity(BASE_API_URL + ORGANIZATION_ENRICH_ENDPOINT, searchRequest, OrganizationEnrichResponse.class);
		filteredResponse = responseEntity.getBody();

		return filteredResponse;
	}

	/**
	 * Searching organizations across Zoom Info database
	 *
	 * @param filter
	 */
	public OrganizationEnrichItem enrichOrganizations(OrganizationSearchFilter filter) {
		OrganizationEnrichItem result = null;

		OrganizationEnrichRequest searchRequest = OrganizationEnrichRequest.of(filter);
		ResponseEntity<OrganizationEnrichResponse> responseEntity = postForEntity(BASE_API_URL + ORGANIZATION_ENRICH_ENDPOINT, searchRequest, OrganizationEnrichResponse.class);
		OrganizationEnrichDataResult enrichDataResultItem = responseEntity.getBody().getData().getResult().get(0);
		result = enrichDataResultItem.getData().get(0);

		return result;
	}

	/**
	 * Searching organizations across Zoom Info database
	 *
	 * @param filter
	 */
	public OrganizationHierarchyEnrichItem enrichOrganizationHierarchy(OrganizationSearchFilter filter) {
		OrganizationHierarchyEnrichItem result = null;

		OrganizationEnrichRequest searchRequest = OrganizationEnrichRequest.of(filter, Arrays.asList(OrganizationHierarchyEnrichItem.OUTPUT_COLUMNS));
		ResponseEntity<OrganizationHierarchyEnrichResponse> responseEntity = postForEntity(BASE_API_URL + ORGANIZATION_HIERARCHY_ENRICH_ENDPOINT, searchRequest, OrganizationHierarchyEnrichResponse.class);
		OrganizationHierarchyEnrichDataResult enrichDataResultItem = responseEntity.getBody().getData().getResult().get(0);
		result = enrichDataResultItem.getData().get(0);

		return result;
	}

	/**
	 * Searching organizations across Zoom Info database
	 *
	 * @param companyId
	 */
	public List<OrgChartEnrichItem> enrichOrgChart(String companyId) {
		List<OrgChartEnrichItem> result = new ArrayList<>();

		for (int i = 0; i < 50; i++) {
			Long currentPage = (long) i + 1;
			OrgChartEnrichRequest searchRequest = OrgChartEnrichRequest.of(companyId, currentPage, 100L);
			ResponseEntity<OrgChartEnrichItemPagedResponse> responseEntity = postForEntity(BASE_API_URL + ORG_CHART_ENRICH_ENDPOINT, searchRequest, OrgChartEnrichItemPagedResponse.class);
			OrgChartEnrichItemPagedResponse enrichDataResult = responseEntity.getBody();

			if (enrichDataResult.getData() != null) {
				result.addAll(enrichDataResult.getData());
			}

			if (enrichDataResult.getCurrentPage() == null || enrichDataResult.getCurrentPage() * searchRequest.getRpp() >= enrichDataResult.getMaxResults()) {
				break;
			}
		}

		return result;
	}

	/**
	 * Get extended Zoom Info data for Organization
	 *
	 * @param zoomInfoOrganizationId
	 */
	public OrganizationZoomInfoExtendedDetails getOrganizationZoomInfoExtended(Long zoomInfoOrganizationId) {
		return zoomInfoCache.getOrganization(zoomInfoOrganizationId);
	}

	/**
	 * Apply Zoom Info data for Organization
	 *
	 * @param organizationId
	 */
	public OrganizationEditDTO applyIntegration(Long organizationId, OrganizationEditDTO organizationDTO) {
		Organizations organization = organizationService.getOrganization(organizationId);
		Users currentUser = userService.getCurrentUserEntity();

		if (organization.getZoomInfoId() == null && organizationDTO != null && !organizationDTO.getZoomInfoId().equals(organization.getZoomInfoId())) {
			organization.setZoomInfoId(organizationDTO.getZoomInfoId());
			// organizationRepository.save(organization);
		}

		// Saving Organization data
		OrganizationEditDTO organizationEditDTO = new OrganizationEditDTO(organization);

		// Get Zoom Info Sync Data
		final OrganizationZoomInfoExtendedDetails zoomInfoSyncDetails = getOrganizationZoomInfoExtended(organizationEditDTO.getZoomInfoId());

		organization = synchronizeOrganizationInfo(organization, zoomInfoSyncDetails);

		Status defaultVendorStatus = statusRepository.findById(1l).orElse(null);

		List<TechnologyEnrichItem> technologyAttributes = zoomInfoSyncDetails.getTechAttributes();
		final Map<String, TechnologyCategories> technologyCategoriesMap = new HashMap<>();
		final Map<String, Technologies> technologiesMap = new HashMap<>();
		final Map<String, Organizations> vendorsMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(technologyAttributes)) {
			for (TechnologyEnrichItem technologyEnrichItem : technologyAttributes) {
				TechnologyCategories technologyCategory = getOrCreateTechnologyCategory(technologyEnrichItem.getTechnologyCategory(), organization, currentUser);
				Technologies technology = getOrCreateTechnology(technologyEnrichItem.getTechnology(), technologyEnrichItem.getVersion(), technologyCategory, organization, currentUser);
				Organizations vendor = getOrCreateVendor(technologyEnrichItem, technology, organization, currentUser, defaultVendorStatus);
				Systems system = getOrCreateSystem(technologyEnrichItem, technology, vendor, organization, currentUser);
				AssociateVendors associateVendors = getOrCreateAssociateVendors(system, vendor, organization, currentUser);
			}
		}

		// Apply business units
		if (CollectionUtils.isNotEmpty(zoomInfoSyncDetails.getOrgChartItems())) {
			Set<String> businessUnitNames = zoomInfoSyncDetails.getOrgChartItems().stream().map(OrgChartEnrichItem::getDepartment).collect(Collectors.toSet());
			for (String businessUnitName : businessUnitNames) {
				// Process business units
				BusinessUnits businessUnit = getOrCreateBusinessUnit(businessUnitName, organization, currentUser);
			}
		}

		// Apply subsidiaries
		if (CollectionUtils.isNotEmpty(zoomInfoSyncDetails.getSubsidiaries())) {
			List<OrganizationHierarchyEnrichTreeNodeItem> subsidiariesHierarchy = zoomInfoSyncDetails.getSubsidiaries().get(0).getFamilyNodes();
			for (OrganizationHierarchyEnrichTreeNodeItem subsidiaryItem : subsidiariesHierarchy) {
				// Process business units
				Organizations subsidiaryDetails = getOrCreateSubsidiary(subsidiaryItem, null, organization, currentUser);
			}
		}

		// Get result organization data
		Organizations result = organizationService.getOrganization(organizationId);
		return new OrganizationEditDTO(result);
	}

	/**
	 * Find existing technology category or create new one
	 *
	 * @param name
	 * @param organization
	 * @param currentUser
	 * @return
	 */
	private TechnologyCategories getOrCreateTechnologyCategory(String name, Organizations organization, Users currentUser) {
		TechnologyCategories result;
		Optional<TechnologyCategories> technologyCategoryOptional = technologyCategoryRepository.getFirstByNameAndOrganization(name, organization.getId());
		if (technologyCategoryOptional.isPresent()) {
			result = technologyCategoryOptional.get();
		} else {
			TechnologyCategories newItem = new TechnologyCategories();
			newItem.setOrganizationId(organization.getId());
			newItem.setName(name);
			newItem.setCreatedBy(currentUser);
			newItem.setCreatedAt(new Date());
			newItem.setUpdatedBy(currentUser);
			newItem.setUpdatedAt(new Date());
			result = technologyCategoryRepository.save(newItem);


			// Save Audit Log CREATE event
			auditLogService.create(
				VItemType.TECHNOLOGY_CATEGORY,
				result.getId(),
				new TechnologyCategoryEditDTO(result),
				Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()), AuditLogItemId.of(VItemType.ZOOM_INFO_TECHNOLOGY_CATEGORY_SYNC, result.getId())).stream().toArray(AuditLogItemId[]::new)
			);
		}

		return result;
	}

	/**
	 * Find existing technology category or create new one
	 *
	 * @param name
	 * @param version
	 * @param technologyCategory
	 * @param organization
	 * @param currentUser
	 * @return
	 */
	private Technologies getOrCreateTechnology(String name, String version, TechnologyCategories technologyCategory, Organizations organization, Users currentUser) {
		Technologies result;
		Optional<Technologies> technologyOptional = technologyRepository.getFirstByNameAndOrganization(name, organization.getId());
		if (technologyOptional.isPresent()) {
			result = technologyOptional.get();
		} else {
			Technologies newItem = new Technologies();
			newItem.setOrganizationId(organization.getId());
			newItem.setName(name);
			// newItem.setVersion(version);
			newItem.setTechnologyCategory(technologyCategory);
			newItem.setCreatedBy(currentUser);
			newItem.setCreatedAt(new Date());
			newItem.setUpdatedBy(currentUser);
			newItem.setUpdatedAt(new Date());
			result = technologyRepository.save(newItem);


			// Save Audit Log CREATE event
			auditLogService.create(
				VItemType.TECHNOLOGY,
				result.getId(),
				new TechnologyEditDTO(result),
				Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()), AuditLogItemId.of(VItemType.ZOOM_INFO_TECHNOLOGY_SYNC, result.getId())).stream().toArray(AuditLogItemId[]::new)
			);
		}

		return result;
	}

	/**
	 * Find existing vendor or create new one
	 *
	 *
	 * @param item
	 * @param technology
	 * @param organization
	 * @param currentUser
	 * @return
	 */
	private Organizations getOrCreateVendor(TechnologyEnrichItem item, Technologies technology, Organizations organization, Users currentUser, Status defaultVendorStatus) {
		Organizations result;
		Optional<Organizations> vendorOptional = organizationRepository.findFirstByNameAndOrganizationTypeAndRootParent(item.getVendor(), OrganizationType.Vendor, organization);
		Boolean isNew = false;
		if (vendorOptional.isPresent()) {
			result = vendorOptional.get();
		} else {
			isNew = true;
			Organizations newItem = new Organizations();
			newItem.setName(item.getVendor());
			newItem.setDescription(item.getDescription());
			newItem.setOrganizationType(OrganizationType.Vendor);
			newItem.setRootParent(organization);
			newItem.setOwner(currentUser);
			newItem.setIsSystemVendor(true);
			newItem.setSite(item.getWebsite());
			newItem.setLogo(item.getLogo());
			result = organizationRepository.save(newItem);

			// Save Audit Log SYNC event
			auditLogService.create(
				VItemType.VENDOR,
				result.getId(),
				new OrganizationEditDTO(result),
				Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()), AuditLogItemId.of(VItemType.ZOOM_INFO_VENDOR_SYNC, organization.getId())).stream().toArray(AuditLogItemId[]::new)
			);
		}

		// Update logo && website if needed
		if (result.getStatus() == null) result.setStatus(defaultVendorStatus);
		if (!isNew && (StringUtils.isNotEmpty(item.getLogo()))) result.setLogo(item.getLogo());
		if (!isNew && (StringUtils.isNotEmpty(item.getWebsite()))) result.setSite(item.getWebsite());

		// Save technology
		result.getTechnologies().add(technology);
		result = organizationRepository.save(result);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.VENDOR,
			result.getId(),
			new OrganizationEditDTO(result),
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()), AuditLogItemId.of(VItemType.ZOOM_INFO_VENDOR_SYNC, result.getId())).stream().toArray(AuditLogItemId[]::new)
		);

		return result;
	}

	/**
	 * Find existing system or create new one
	 *
	 * @param technologyEnrichItem
	 * @param technology
	 * @param vendor
	 * @param organization
	 * @param currentUser
	 * @return
	 */
	private Systems getOrCreateSystem(TechnologyEnrichItem technologyEnrichItem, Technologies technology, Organizations vendor, Organizations organization, Users currentUser) {
		Systems result;
		Optional<Systems> systemOptional = systemRepository.getFirstByNameForOrganization(technologyEnrichItem.getSystem(), organization.getId());
		if (systemOptional.isPresent()) {
			result = systemOptional.get();
		} else {
			Systems newItem = new Systems();
			newItem.setOrganizationId(organization.getId());
			newItem.setName(technologyEnrichItem.getSystem());
			newItem.setCreatedBy(currentUser);
			newItem.setCreatedAt(new Date());
			newItem.setUpdatedBy(currentUser);
			newItem.setUpdatedAt(new Date());
			result = systemRepository.save(newItem);


			// Save Audit Log CREATE event
			auditLogService.create(
				VItemType.SYSTEM,
				result.getId(),
				new SystemEditDTO(result),
				Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()), AuditLogItemId.of(VItemType.ZOOM_INFO_SYSTEM_SYNC, result.getId())).stream().toArray(AuditLogItemId[]::new)
			);
		}

		// Save system technology
		if (result.getSystemStatus() == null) result.setSystemStatus(SystemStatus.ACTIVE);
		if (StringUtils.isNotEmpty(technologyEnrichItem.getDescription())) result.setDescription(technologyEnrichItem.getDescription());
		result.getTechnologies().add(technology);
		result = systemRepository.save(result);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.SYSTEM,
			organization.getId(),
			new SystemEditDTO(result),
			Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()), AuditLogItemId.of(VItemType.ZOOM_INFO_SYSTEM_SYNC, result.getId())).stream().toArray(AuditLogItemId[]::new)
		);

		return result;
	}

	/**
	 * Find existing business unit or create new one
	 *
	 * @param name
	 * @param organization
	 * @param currentUser
	 * @return
	 */
	private BusinessUnits getOrCreateBusinessUnit(String name, Organizations organization, Users currentUser) {
		BusinessUnits result;
		Optional<BusinessUnits> businessUnitOptional = businessUnitRepository.findByNameAndOrganizationId(name, organization.getId());
		if (businessUnitOptional.isPresent()) {
			result = businessUnitOptional.get();
		} else {
			BusinessUnits newItem = new BusinessUnits();
			newItem.setOrganizationId(organization.getId());
			newItem.setName(name);
			newItem.setCreatedBy(currentUser);
			newItem.setCreatedAt(new Date());
			newItem.setUpdatedBy(currentUser);
			newItem.setUpdatedAt(new Date());
			result = businessUnitRepository.save(newItem);


			// Save Audit Log CREATE event
			auditLogService.create(
				VItemType.BUSINESS_UNIT,
				result.getId(),
				new BusinessUnitEditDTO(result),
				Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()), AuditLogItemId.of(VItemType.ZOOM_INFO_BUSINESS_UNIT_SYNC, result.getId())).stream().toArray(AuditLogItemId[]::new)
			);
		}

		return result;
	}


	/**
	 * Find existing subsidiary or create new one
	 *
	 * @param subsidiaryNode
	 * @param organization
	 * @param currentUser
	 * @return
	 */
	private Organizations getOrCreateSubsidiary(OrganizationHierarchyEnrichTreeNodeItem subsidiaryNode, Organizations parent, Organizations organization, Users currentUser) {
		Organizations result = null;

		Optional<OrganizationSynchronizationExternalLog> logItemOptional = organizationSynchronizationExternalLogRepository.findFirstByOrganizationIdAndIntegrationTypeAndObjectTypeAndExternalId(
			organization.getId()
			, OrganizationSynchronizationExternalLog.IntegrationType.ZOOMINFO
			, OrganizationSynchronizationExternalLog.ObjectType.SUBSIDIARY_ORG
			, subsidiaryNode.getCompanyId().toString()
		);
		Optional<Organizations> subsidiaryOptional = Optional.empty();
		if (logItemOptional.isPresent()) {
			Organizations subsidiary = organizationRepository.getSubsidiaryForRootOrganization(logItemOptional.get().getLocalId(), organization.getId(), OrganizationType.Subsidiary);
			subsidiaryOptional = Optional.ofNullable(subsidiary);
		}
		if (subsidiaryOptional.isEmpty() && StringUtils.isNotEmpty(subsidiaryNode.getName())) {
			subsidiaryOptional = organizationRepository.findFirstByNameAndOrganizationTypeAndRootParent(subsidiaryNode.getName(), OrganizationType.Subsidiary, organization);
		}

		if (subsidiaryOptional.isPresent()) {
			result = subsidiaryOptional.get();
		} else {

			String name = subsidiaryNode.getName();
			if (StringUtils.isEmpty(name)) {
				int itemsCount = organizationSynchronizationExternalLogRepository.countAllByOrganizationIdAndIntegrationTypeAndObjectType(
					organization.getId()
					, OrganizationSynchronizationExternalLog.IntegrationType.ZOOMINFO
					, OrganizationSynchronizationExternalLog.ObjectType.SUBSIDIARY_ORG
				);

				name = "Name Missing " + itemsCount;
			}

			Optional<State> stateOptional = Optional.empty();
			Optional<City> cityOptional = Optional.empty();
			if (StringUtils.isNotEmpty(subsidiaryNode.getState())) {
				stateOptional = stateRepository.findFirstByName(subsidiaryNode.getState());
				if (stateOptional.isPresent()) {
					cityOptional = cityRepository.findFirstByNameAndState(subsidiaryNode.getCity(), stateOptional.get());
				}
			}

			Organizations newItem = new Organizations();
			newItem.setRootParent(organization);
			newItem.setOrganizationType(OrganizationType.Subsidiary);
			newItem.setName(name);
			newItem.setParent(parent);
			newItem.setState(stateOptional.orElse(null));
			newItem.setCity(cityOptional.orElse(null));
			result = organizationRepository.save(newItem);


			// Save Audit Log CREATE event
			auditLogService.create(
				VItemType.SUBSIDIARY_ORGANIZATION,
				result.getId(),
				new OrganizationEditDTO(result),
				Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId()), AuditLogItemId.of(VItemType.ZOOM_INFO_SUBSIDIARY_ORGANIZATION_SYNC, result.getId())).stream().toArray(AuditLogItemId[]::new)
			);

			// Saving sync log data
			OrganizationSynchronizationExternalLog syncLogItem = new OrganizationSynchronizationExternalLog();
			syncLogItem.setOrganizationId(organization.getId());
			syncLogItem.setIntegrationType(OrganizationSynchronizationExternalLog.IntegrationType.ZOOMINFO);
			syncLogItem.setObjectType(OrganizationSynchronizationExternalLog.ObjectType.SUBSIDIARY_ORG);
			syncLogItem.setExternalId(subsidiaryNode.getCompanyId().toString());
			syncLogItem.setLocalId(result.getId());
			syncLogItem = organizationSynchronizationExternalLogRepository.save(syncLogItem);
		}

		// Proceed child subsidiary organizations
		if (CollectionUtils.isNotEmpty(subsidiaryNode.getFamilyNodes())) {
			for (OrganizationHierarchyEnrichTreeNodeItem childSubsidiary : subsidiaryNode.getFamilyNodes()) {
				getOrCreateSubsidiary(childSubsidiary, result, organization, currentUser);
			}
		}

		return result;
	}

	/**
	 * Find existing associate vendor or create new one
	 *
	 * @param system
	 * @param vendor
	 * @param organization
	 * @param currentUser
	 * @return
	 */
	private AssociateVendors getOrCreateAssociateVendors(Systems system, Organizations vendor, Organizations organization, Users currentUser) {
		// Create Associate Vendors
		Optional<AssociateVendors> associateVendorsOptional = associateVendorRepository.findByOrganizationIdAndVendor(organization.getId(), vendor);
		AssociateVendors associateVendors;
		if (!associateVendorsOptional.isPresent()) {
			AssociateVendors newItem = new AssociateVendors();
			newItem.setOrganizationId(organization.getId());
			newItem.setVendor(vendor);
			newItem.setCreatedBy(currentUser);
			newItem.setCreatedAt(new Date());
			newItem.setUpdatedBy(currentUser);
			newItem.setUpdatedAt(new Date());
			associateVendors = associateVendorRepository.save(newItem);
		} else {
			associateVendors = associateVendorsOptional.get();
		}

		// Updating Associate Vendors
		associateVendors.getSystems().add(system);
		associateVendors = associateVendorRepository.save(associateVendors);

		return associateVendors;
	}

	/**
	 * Synchronize Organization Info between Zoom Info and local system
	 *
	 * @param organization
	 * @param zoomInfoSyncDetails
	 * @return
	 */
	private Organizations synchronizeOrganizationInfo(final Organizations organization, OrganizationZoomInfoExtendedDetails zoomInfoSyncDetails) {

		if (zoomInfoSyncDetails.getDescriptionList() != null && zoomInfoSyncDetails.getDescriptionList().size() > 0) {
			String extendedDescription = StringUtils.join(zoomInfoSyncDetails.getDescriptionList().stream().map(SearchItem::getDescription).collect(Collectors.toList()), "\n");
			String description = zoomInfoSyncDetails.getDescriptionList().get(0).getDescription();
			organization.setDescription(description);
			organization.setNotes(extendedDescription);
		}

		if (StringUtils.isNotEmpty(zoomInfoSyncDetails.getWebsite())) organization.setSite(zoomInfoSyncDetails.getWebsite());
		if (StringUtils.isNotEmpty(zoomInfoSyncDetails.getLogo())) organization.setLogo(zoomInfoSyncDetails.getLogo());
		if (zoomInfoSyncDetails.getRevenue() != null) organization.setAverageRevenue(zoomInfoSyncDetails.getRevenue());
		if (zoomInfoSyncDetails.getEmployeeCount() != null) organization.setEmployeeCount(zoomInfoSyncDetails.getEmployeeCount());
		if (StringUtils.isNotEmpty(zoomInfoSyncDetails.getPhone())) organization.setPhone(zoomInfoSyncDetails.getPhone());
		if (StringUtils.isNotEmpty(zoomInfoSyncDetails.getFax())) organization.setFax(zoomInfoSyncDetails.getFax());
		if (StringUtils.isNotEmpty(zoomInfoSyncDetails.getStreet())) organization.setStreetAddress1(zoomInfoSyncDetails.getStreet());
		if (StringUtils.isNotEmpty(zoomInfoSyncDetails.getCountry())) {
			countryRepository.findFirstByName(zoomInfoSyncDetails.getCountry()).ifPresent(country -> {
				organization.setCountry(country);
				if (StringUtils.isNotEmpty(zoomInfoSyncDetails.getCity())) {
					cityRepository.findFirstByNameAndCountry(zoomInfoSyncDetails.getCity(), country).ifPresent(organization::setCity);
				}
				if (StringUtils.isNotEmpty(zoomInfoSyncDetails.getState())) {
					stateRepository.findFirstByNameAndCountry(zoomInfoSyncDetails.getState(), country).ifPresent(organization::setState);
				}
			});
		}
		if (CollectionUtils.isNotEmpty(zoomInfoSyncDetails.getNaicsCodes())) {
			for (SearchItem naicsDetails : zoomInfoSyncDetails.getNaicsCodes()) {
				Optional<Industries> industryOptional = industryRepository.findFirstByName(naicsDetails.getName());
				if (industryOptional.isPresent()) {
					organization.setIndustry(industryOptional.get());
					break;
				}
			}
		}
		// sicCodes
		// naicsCodes
		// competitors
		// if (StringUtils.isNotEmpty(zoomInfoSyncDetails.getFax())) organization.setFax(zoomInfoSyncDetails.getFax());
		if (StringUtils.isNotEmpty(zoomInfoSyncDetails.getZipCode())) organization.setZip(zoomInfoSyncDetails.getZipCode());

		Organizations saveResult = organizationRepository.save(organization);
		OrganizationEditDTO saveResultDTO = new OrganizationEditDTO(saveResult);

		// Save Audit Log SYNC event
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organization.getId())));
		logItems.add(AuditLogItemId.of(VItemType.ZOOM_INFO_ORGANIZATION_SYNC, organization.getId()));

		auditLogService.update(
			VItemType.ORGANIZATION,
			organization.getId(),
			saveResultDTO,
			logItems.stream().toArray(AuditLogItemId[]::new)
		);

		return saveResult;
	}

	// ----- PRIVATE KEY SECTION ----- //

	public <ENTITY> ResponseEntity<ENTITY> postForEntity(String url, @Nullable Object request, Class<ENTITY> responseClass) {
		HttpEntity requestEntity = new HttpEntity<>(request, buildAuthorizedHeaders());
		ResponseEntity<ENTITY> responseEntity = restTemplate.postForEntity(url, requestEntity, responseClass);

		Long proceedCount = (Long) ApplicationContextThreadLocal.getContext().getProperty("zoomInfoAPICallsCount", 0L) + 1;
		ApplicationContextThreadLocal.getContext().setProperty("zoomInfoAPICallsCount", proceedCount);

		return responseEntity;
	}

	private HttpHeaders buildAuthorizedHeaders() {

		String accessToken = zoomInfoCache.getJWTAccessToken();

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Bearer " + accessToken);
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("user-agent", ""); // Without user-agent you will get 403 error

		return httpHeaders;
	}

	/**
	 * Get JWT token for Zoom Info Authorization
	 *
	 * @return
	 */
	public String getAccessToken() {
		String clientJwt = getClientJwt();

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Bearer " + clientJwt);
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("user-agent", ""); // Without user-agent you will get 403 error

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(null, httpHeaders);

		return postAndGetJwt(request);
	}

	/*
	private String usernamePasswordAuthentication() {

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("user-agent", ""); // Without user-agent you will get 403 error

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("username", username);
		requestBody.put("password", password);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, httpHeaders);

		return postAndGetJwt(request);
	}
	*/

	private String postAndGetJwt(HttpEntity<Map<String, Object>> request) {
		ResponseEntity<Map> responseEntity = postForEntity(BASE_API_URL + AUTHENTICATE_URL, request, Map.class);

		if (responseEntity.getBody() == null) {
			throw new RuntimeException("Could not authenticate, empty response body");
		}

		return String.valueOf(responseEntity.getBody().get("jwt"));
	}

	public String getClientJwt() {
		String token = "";

		try {
			PrivateKey privateKey;
			final int expiration = 1000 * 5 * 60;

			byte[] privateKeyBytes = Base64.getDecoder().decode(zoomInfoConfig.getPrivateKey());
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			// RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec);
			privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec);

			Claims claims = Jwts.claims();
			// claims.setSubject(parametersMap.get(ExternalAnalyticsQlikParameters.USER_EMAIL.name())); // app developer
			claims.setAudience(ENTERPRISE_API_AUDIENCE);
			claims.setIssuer(ISSUER); // issuer: "api-client@zoominfo.com",
			claims.put(USERNAME_CLAIM, zoomInfoConfig.getUsername());
			claims.put(CLIENT_ID_CLAIM, zoomInfoConfig.getClientId());
			claims.setIssuedAt(new Date(System.currentTimeMillis()));
			claims.setExpiration(new Date(System.currentTimeMillis() + expiration));

			token = Jwts.builder()
				.signWith(SignatureAlgorithm.RS256, privateKey) // ECDSA using P-256 and SHA-256
				// .setHeaderParam("typ", "JWT")
				// .setHeaderParam(JwsHeader.KEY_ID, zoomInfoConfig.getClientId())
				.setClaims(claims)
				.compact();

		} catch (NoSuchAlgorithmException e) {
			log.warn(e.getMessage(), e);
		} catch (InvalidKeySpecException e) {
			log.warn(e.getMessage(), e);
		}

		return token;
	}

	/*
	private String getClientJwt() {
		String clientJWT = "";
		try {
			clientJWT = this.generateClientToken();
			return clientJWT;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	private String generateClientToken() throws NoSuchAlgorithmException, InvalidKeySpecException {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -2);
		Date issuedAtDate = cal.getTime();
		Date expiryDate = Date.from(issuedAtDate.toInstant().plusSeconds(EXPIRY_TIME_SECONDS));
		return JWT
			.create()
			.withAudience(ENTERPRISE_API_AUDIENCE)
			.withIssuer(ISSUER)
			.withClaim(USERNAME_CLAIM, username)
			.withClaim(CLIENT_ID_CLAIM, clientId)
			.withIssuedAt(issuedAtDate)
			.withExpiresAt(expiryDate)
			.sign(generateSigningAlgorithm(privateKey));
	}

	private Algorithm generateSigningAlgorithm(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String cleanedPrivateKey = privateKey.replaceAll("-----END PRIVATE KEY-----", "")
			.replaceAll("-----BEGIN PRIVATE KEY-----", "")
			.replaceAll("\n", "").trim();
		byte[] privateKeyBytes = Base64.getDecoder().decode(cleanedPrivateKey);
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
			.generatePrivate(privateKeySpec);
		RSAKeyProvider keyProvider = new AuthClientRSAKeyProvider(rsaPrivateKey);
		return Algorithm.RSA256(keyProvider);
	}

	private static class AuthClientRSAKeyProvider implements RSAKeyProvider {
		private final RSAPrivateKey privateKey;

		public AuthClientRSAKeyProvider(RSAPrivateKey privateKey) {
			this.privateKey = privateKey;
		}

		@Override
		public RSAPublicKey getPublicKeyById(String keyId) {
			return null;
		}

		@Override
		public RSAPrivateKey getPrivateKey() {
			return this.privateKey;
		}

		@Override
		public String getPrivateKeyId() {
			return null;
		}
	}
	*/
	// ----- END of PRIVATE KEY SECTION ----- //

}
