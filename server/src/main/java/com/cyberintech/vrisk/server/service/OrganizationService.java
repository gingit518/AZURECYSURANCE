package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.context.ApplicationContextThreadLocal;
import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.dao.OrganizationModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.data.SubsidiaryOrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.audit.items.VendorOwnerAuditDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationEditDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.VendorEditDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.*;
import com.cyberintech.vrisk.server.service.azure.PowerBIAdminService;
import com.cyberintech.vrisk.server.service.dashboards.powerbi.PowerBIConfig;
import com.cyberintech.vrisk.server.service.dashboards.powerbi.PowerBIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Organization management Service. Implements basic Organization logic.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@Service("organizationService")
@Slf4j
public class OrganizationService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Lazy
	@Autowired
	private CyberRiskScoringService cyberRiskScoringService;

	@Lazy
	@Autowired
	private DocumentService documentService;

	@Autowired
	private DocumentsRepository documentsRepository;

	@Autowired
	private IndustryRepository industryRepository;

	@Autowired
	private LanguageRepository languageRepository;

	@Lazy
	@Autowired
	private OrganizationModelDAO organizationModelDAO;

	@Autowired
	protected OrganizationRepository organizationRepository;

	@Lazy
	@Autowired
	private PermissionService permissionService;

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Lazy
	@Autowired
	private PowerBIAdminService powerBIAdminService;

	@Lazy
	@Autowired
	private PowerBIConfig powerBIConfig;

	@Lazy
	@Autowired
	private PowerBIService powerBIService;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private SupportedLanguagesRepository supportedLanguagesRepository;

	@Lazy
	@Autowired
	private TechnologyService technologyService;

	@Autowired
	private UserService userService;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private ExternalAnalyticsRepository externalAnalyticsRepository;

	/**
	 * Get Current Organization
	 *
	 * @return
	 */
	public OrganizationViewDTO getCurrentOrganization() {
		UserDetailsImpl user = userService.getCurrentUser();

		if (user.getOrganizationId() != null) {
			Organizations organization = getOrganization(user.getOrganizationId());
			OrganizationViewDTO result = new OrganizationViewDTO(organization);

			return result;
		}

		return null;
	}

	/**
	 * Update Current Organization Details
	 *
	 * @return
	 */
	public OrganizationEditDTO updateCurrentOrganization(OrganizationEditDTO organizationEditDTO) {

		Organizations organization = getCurrentOrganizationEntity();
		OrganizationEditDTO existingItemDTO = new OrganizationEditDTO(organization);

		if (StringUtils.isNotEmpty(organizationEditDTO.getName())) {
			// Verify organization with such name not exists
			if (organizationRepository.findFirstByNameAndOrganizationTypeAndIdIsNotIn(organizationEditDTO.getName(), OrganizationType.Organization, Arrays.asList(organization.getId())).isPresent()) {
				throw new ConflictException(
					MessageFormat.format(
						"Failed to rename your Organization [{1}]. Organization with this name already registered in the system [{0}]"
						, organization.getName()
						, organizationEditDTO.getName()
					)
					, ApplicationExceptionCodes.ORGANIZATION_WITH_NAME_ALREADY_EXISTS
				);
			}

			organization.setName(organizationEditDTO.getName());
		}
		if (StringUtils.isNotEmpty(organizationEditDTO.getDescription())) {
			organization.setDescription(organizationEditDTO.getDescription());
		}
		if (StringUtils.isNotEmpty(organizationEditDTO.getSite())) {
			organization.setSite(organizationEditDTO.getSite());
		}
		if (StringUtils.isNotEmpty(organizationEditDTO.getPhone())) {
			organization.setPhone(organizationEditDTO.getPhone());
		}
		if (StringUtils.isNotEmpty(organizationEditDTO.getTaxId())) {
			organization.setTaxId(organizationEditDTO.getTaxId());
		}
		if (StringUtils.isNotEmpty(organizationEditDTO.getStreetAddress1())) {
			organization.setStreetAddress1(organizationEditDTO.getStreetAddress1());
		}
		if (StringUtils.isNotEmpty(organizationEditDTO.getStreetAddress2())) {
			organization.setStreetAddress2(organizationEditDTO.getStreetAddress2());
		}

		if (permissionService.checkCurrentUserPermission(PermissionType.ORGANIZATION_RECORD_PRICE_LIMIT)) {
			if (organizationEditDTO.getRecordPriceLimit() != null) organization.setRecordPriceLimit(organizationEditDTO.getRecordPriceLimit());
		}
		if (permissionService.checkCurrentUserPermission(PermissionType.ORGANIZATION_AGGREGATE_INSURANCE_LIMIT)) {
			if (organizationEditDTO.getInsuranceLimit() != null) organization.setInsuranceLimit(organizationEditDTO.getInsuranceLimit());
		}
		if (permissionService.checkCurrentUserPermission(PermissionType.ORGANIZATION_CYBER_INSURANCE_DEDUCTIBLE)) {
			if (organizationEditDTO.getInsuranceDeductible() != null) organization.setInsuranceDeductible(organizationEditDTO.getInsuranceDeductible());
		}
		if (permissionService.checkCurrentUserPermission(PermissionType.ORGANIZATION_AVERAGE_REVENUE)) {
			if (organizationEditDTO.getAverageRevenue() != null) organization.setAverageRevenue(organizationEditDTO.getAverageRevenue());
		}
		if (organizationEditDTO.getQualThreshold() != null) {
			organization.setQualThreshold(organizationEditDTO.getQualThreshold());
		}
		if (organizationEditDTO.getIsPublicCompany() != null) {
			organization.setIsPublicCompany(organizationEditDTO.getIsPublicCompany());
		}
		if (permissionService.checkCurrentUserPermission(PermissionType.ORGANIZATION_MARKET_CAPITALIZATION)) {
			if (organizationEditDTO.getMarketCapitalizationNumber() != null) organization.setMarketCapitalizationNumber(organizationEditDTO.getMarketCapitalizationNumber());
		}
		if (organizationEditDTO.getNumberOfEmployees() != null) organization.setNumberOfEmployees(organizationEditDTO.getNumberOfEmployees());
		if (organizationEditDTO.getNumberOfIndependentContractors() != null) organization.setNumberOfIndependentContractors(organizationEditDTO.getNumberOfIndependentContractors());
		if (organizationEditDTO.getRevenueIn2021() != null) organization.setRevenueIn2021(organizationEditDTO.getRevenueIn2021());
		if (organizationEditDTO.getRevenueIn2022() != null) organization.setRevenueIn2022(organizationEditDTO.getRevenueIn2022());
		if (organizationEditDTO.getRevenueIn2023() != null) organization.setRevenueIn2023(organizationEditDTO.getRevenueIn2023());

		if (StringUtils.isNotEmpty(organizationEditDTO.getZip())) {
			organization.setZip(organizationEditDTO.getZip());
		}
		if (StringUtils.isNotEmpty(organizationEditDTO.getVatId())) {
			organization.setVatId(organizationEditDTO.getVatId());
		}
		if (StringUtils.isNotEmpty(organizationEditDTO.getNotes())) {
			organization.setNotes(organizationEditDTO.getNotes());
		}

		if (organizationEditDTO.getAssessmentFrameworkLevel() != null) {
			organization.setAssessmentFrameworkLevel(organizationEditDTO.getAssessmentFrameworkLevel());
		}

		// Manage Logo document
		if (organizationEditDTO.getLogoDocument() != null) {
			Documents logoDocument = documentsRepository.findById(organizationEditDTO.getLogoDocument().getId()).get();
			organization.setLogoDocument(logoDocument);

			DocumentDTO logoDocumentDTO = new DocumentDTO(logoDocument);
			organization.setLogo(documentService.buildStorageUrl(logoDocumentDTO));
		} else if (Boolean.TRUE.equals(organizationEditDTO.getRemoveLogo())) {
			organization.setLogoDocument(null);
		}

		if (permissionService.checkCurrentUserPermission(PermissionType.ORGANIZATION_SUPPORTED_LANGUAGES_UPDATE)) {

			Optional.ofNullable(organizationEditDTO.getSupportedLanguages()).ifPresent(supportedLanguageEditDTOList -> {
				organization.setSupportedLanguages(new HashSet<>());
				organizationEditDTO.getSupportedLanguages().stream().forEach(supportedLanguageEditDTO -> {
					organization.getSupportedLanguages().add(supportedLanguagesRepository.findById(supportedLanguageEditDTO.getId()).get());
				});
			});

			if (organization.getSupportedLanguages().size() > 1) {
				organization.setIsMultiLanguage(true);
			} else {
				organization.setIsMultiLanguage(false);
			}
		}

		if (organizationEditDTO.getCountry() != null && organizationEditDTO.getCountry().getId() != null) {
			organization.setCountry(countryRepository.findById(organizationEditDTO.getCountry().getId()).orElse(null));
			if (organizationEditDTO.getState() != null && organizationEditDTO.getState().getId() != null) {
				organization.setState(stateRepository.findById(organizationEditDTO.getState().getId()).orElse(null));
			} else {
				organization.setState(null);
			}
			if (organizationEditDTO.getCity() != null && organizationEditDTO.getCity().getId() != null) {
				organization.setCity(cityRepository.findById(organizationEditDTO.getCity().getId()).orElse(null));
			} else {
				organization.setCity(null);
			}
		}
		if (organizationEditDTO.getCurrency() != null && organizationEditDTO.getCurrency().getId() != null) {
			organization.setCurrency(currencyRepository.findById(organizationEditDTO.getCurrency().getId()).orElse(null));
		}
		if (organizationEditDTO.getLanguage() != null && organizationEditDTO.getLanguage().getId() != null) {
			organization.setLanguage(languageRepository.findById(organizationEditDTO.getLanguage().getId()).orElse(null));
		}
		if (organizationEditDTO.getStatus() != null && organizationEditDTO.getStatus().getId() != null) {
			organization.setStatus(statusRepository.findById(organizationEditDTO.getStatus().getId()).orElse(null));
		}
		if (organizationEditDTO.getParent() != null && organizationEditDTO.getParent().getId() != null) {
			organization.setParent(organizationRepository.findById(organizationEditDTO.getParent().getId()).orElse(null));
		}
		if (organizationEditDTO.getOwner() != null && organizationEditDTO.getOwner().getId() != null) {
			organization.setOwner(userService.getUser(organizationEditDTO.getOwner().getId()));
		}
		if (organizationEditDTO.getIndustry() != null && organizationEditDTO.getIndustry().getId() != null) {
			organization.setIndustry(industryRepository.findById(organizationEditDTO.getIndustry().getId()).orElse(null));
		}

		organizationRepository.save(organization);

		OrganizationEditDTO result = new OrganizationEditDTO(organization);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.ORGANIZATION,
			result.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, (organization.getRootParent() != null ? organization.getRootParent().getId() : organization.getId()))
		);

		return result;
	}

	/**
	 * Update Current Organization Details
	 *
	 * @return
	 */
	public OrganizationEditDTO updateCurrentOrganizationCyberInfo(OrganizationEditDTO organizationEditDTO) {

		Organizations organization = getCurrentOrganizationEntity();
		OrganizationEditDTO existingItemDTO = new OrganizationEditDTO(organization);

		if (organizationEditDTO.getRecordPriceLimit() != null) organization.setRecordPriceLimit(organizationEditDTO.getRecordPriceLimit());
		if (organizationEditDTO.getInsuranceLimit() != null) organization.setInsuranceLimit(organizationEditDTO.getInsuranceLimit());
		if (organizationEditDTO.getInsuranceDeductible() != null) organization.setInsuranceDeductible(organizationEditDTO.getInsuranceDeductible());
		if (organizationEditDTO.getAverageRevenue() != null) organization.setAverageRevenue(organizationEditDTO.getAverageRevenue());
		if (organizationEditDTO.getMarketCapitalizationNumber() != null) organization.setMarketCapitalizationNumber(organizationEditDTO.getMarketCapitalizationNumber());

		if (organizationEditDTO.getRevenue() != null) organization.setRevenue(organizationEditDTO.getRevenue());
		if (organizationEditDTO.getEbitda() != null) organization.setEbitda(organizationEditDTO.getEbitda());
		if (organizationEditDTO.getDebt() != null) organization.setDebt(organizationEditDTO.getDebt());
		if (organizationEditDTO.getPensionDebt() != null) organization.setPensionDebt(organizationEditDTO.getPensionDebt());
		if (organizationEditDTO.getCreditRating() != null) organization.setCreditRating(organizationEditDTO.getCreditRating());
		if (organizationEditDTO.getGrossRiskBearingCapacity() != null) organization.setGrossRiskBearingCapacity(organizationEditDTO.getGrossRiskBearingCapacity());
		if (organizationEditDTO.getBusinessRiskBearingCapacity() != null) organization.setBusinessRiskBearingCapacity(organizationEditDTO.getBusinessRiskBearingCapacity());
		if (organizationEditDTO.getCyberRiskBearingCapacity() != null) organization.setCyberRiskBearingCapacity(organizationEditDTO.getCyberRiskBearingCapacity());

		if (organizationEditDTO.getMaximumWrittenPremium() != null) organization.setMaximumWrittenPremium(organizationEditDTO.getMaximumWrittenPremium());
		if (organizationEditDTO.getMaximumCarrierLimit() != null) organization.setMaximumCarrierLimit(organizationEditDTO.getMaximumCarrierLimit());
		if (organizationEditDTO.getRansomwareSublimit() != null) organization.setRansomwareSublimit(organizationEditDTO.getRansomwareSublimit());
		if (organizationEditDTO.getPrivacyGdprSublimit() != null) organization.setPrivacyGdprSublimit(organizationEditDTO.getPrivacyGdprSublimit());
		if (organizationEditDTO.getPrivacyCcpaSublimit() != null) organization.setPrivacyCcpaSublimit(organizationEditDTO.getPrivacyCcpaSublimit());
		if (organizationEditDTO.getDdosSublimit() != null) organization.setDdosSublimit(organizationEditDTO.getDdosSublimit());

		// Cyber Insurance
		if (organizationEditDTO.getTotalHeadCount() != null) organization.setTotalHeadCount(organizationEditDTO.getTotalHeadCount());
		if (organizationEditDTO.getCompanyITBudget() != null) organization.setCompanyITBudget(organizationEditDTO.getCompanyITBudget());
		if (organizationEditDTO.getItHeadCount() != null) organization.setItHeadCount(organizationEditDTO.getItHeadCount());
		if (organizationEditDTO.getSecurityHeadCount() != null) organization.setSecurityHeadCount(organizationEditDTO.getSecurityHeadCount());
		if (organizationEditDTO.getItSecurityBudget() != null) organization.setItSecurityBudget(organizationEditDTO.getItSecurityBudget());
		if (organizationEditDTO.getNumberOfEmployees() != null) organization.setNumberOfEmployees(organizationEditDTO.getNumberOfEmployees());
		if (organizationEditDTO.getNumberOfIndependentContractors() != null) organization.setNumberOfIndependentContractors(organizationEditDTO.getNumberOfIndependentContractors());
		if (organizationEditDTO.getRevenueIn2021() != null) organization.setRevenueIn2021(organizationEditDTO.getRevenueIn2021());
		if (organizationEditDTO.getRevenueIn2022() != null) organization.setRevenueIn2022(organizationEditDTO.getRevenueIn2022());
		if (organizationEditDTO.getRevenueIn2023() != null) organization.setRevenueIn2023(organizationEditDTO.getRevenueIn2023());
		if (organizationEditDTO.getInhouseDataCentersCount() != null) organization.setInhouseDataCentersCount(organizationEditDTO.getInhouseDataCentersCount());
		if (organizationEditDTO.getExternalDataCentersCount() != null) organization.setExternalDataCentersCount(organizationEditDTO.getExternalDataCentersCount());
		if (organizationEditDTO.getPersonallyIdentifiableInformation() != null) organization.setPersonallyIdentifiableInformation(organizationEditDTO.getPersonallyIdentifiableInformation());
		if (organizationEditDTO.getPaymentCardIndustry() != null) organization.setPaymentCardIndustry(organizationEditDTO.getPaymentCardIndustry());
		if (organizationEditDTO.getProtectedHealthInformation() != null) organization.setProtectedHealthInformation(organizationEditDTO.getProtectedHealthInformation());

		if (organizationEditDTO.getBusinessCriticalApplications() != null) {
			organization.setBusinessCriticalApplications(new HashSet<>());
			organizationEditDTO.getBusinessCriticalApplications().forEach(systemRef -> {
				organization.getBusinessCriticalApplications().add(systemRepository.findById(systemRef.getId()).get());
			});
		}

		if (organizationEditDTO.getOutsourcedCriticalProviders() != null) {
			organization.setOutsourcedCriticalProviders(new HashSet<>());
			organizationEditDTO.getOutsourcedCriticalProviders().forEach(vendorRef -> {
				organization.getOutsourcedCriticalProviders().add(organizationRepository.findById(vendorRef.getId()).get());
			});
		}

		organizationRepository.save(organization);

		OrganizationEditDTO result = new OrganizationEditDTO(organization);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.ORGANIZATION,
			result.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, (organization.getRootParent() != null ? organization.getRootParent().getId() : organization.getId()))
		);

		return result;
	}

	/**
	 * Get Specific Organization
	 *
	 * @return
	 */
	public Organizations getOrganization(Long itemId) {
		Organizations organization = organizationRepository.findById(itemId)
			.orElseThrow(() -> new BadRequestException(MessageFormat.format("Organization not found [{0}]", itemId), ApplicationExceptionCodes.ORGANIZATION_NOT_EXISTS));

		return organization;
	}

	/**
	 * Get Current Organization Id
	 *
	 * @return
	 */
	public Long getCurrentOrganizationId() {
		UserDetailsImpl user = userService.getCurrentUser();

		Long organizationId = user.getOrganizationId();

		if (organizationId == null && userService.isSuperAdmin()) {
			organizationId = ApplicationContextThreadLocal.getContext().getOrganizationId();
		}

		return organizationId;
	}

	/**
	 * Get Current Organization Entity
	 *
	 * @return
	 */
	public Organizations getCurrentOrganizationEntity() {
		Long currentOrganizationId = getCurrentOrganizationId();
		Organizations result = currentOrganizationId != null ? getOrganization(currentOrganizationId) : null;

		return result;
	}

	/**
	 * Get Subsidiary Organization of Current Organization
	 *
	 * @return
	 */
	public Organizations getSubsidiaryForCurrentOrganization(Long subsidiaryId) {
		Organizations result = organizationRepository.getSubsidiaryForRootOrganization(subsidiaryId, getCurrentOrganizationId(), OrganizationType.Subsidiary);

		return result;
	}

	/**
	 * Get Vendor Organization of Current Organization
	 *
	 * @return
	 */
	public Organizations getVendorForCurrentOrganization(Long vendorId) {
		Organizations result = organizationRepository.getSubsidiaryForRootOrganization(vendorId, getCurrentOrganizationId(), OrganizationType.Vendor);

		return result;
	}

	/**
	 * Get Existing or Create new Vendor
	 *
	 * @return New Vendor
	 */
	public Organizations getOrCreateVendor(Organizations organization, String vendorName) {
		Optional<Organizations> vendorDetails = organizationRepository.findFirstByNameAndOrganizationTypeAndRootParent(vendorName, OrganizationType.Vendor, organization);
		if (vendorDetails.isEmpty()) {
			Organizations vendor = new Organizations();
			vendor.setName(vendorName);
			vendor.setParent(organization);
			vendor.setRootParent(organization);
			vendor.setOrganizationType(OrganizationType.Vendor);
			vendor = organizationRepository.save(vendor);

			vendorDetails = Optional.of(vendor);
		}

		return vendorDetails.get();
	}
	/**
	 * Get List of Organizations by Type and Filter
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, OrganizationViewDTO> getListFiltered(OrganizationType organizationType, FilteredRequest<NameFilter> filteredRequest) {
		List<Organizations> items;
		Long count = 0l;
		FilteredResponse<NameFilter, OrganizationViewDTO> filteredResponse = new FilteredResponse<NameFilter, OrganizationViewDTO>(filteredRequest);

		String nameFilter = Optional.ofNullable(filteredRequest.getFilter().getName()).orElse("");
		List<Long> excludeIds = Arrays.asList(0L);
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getExcludeIds() != null && filteredRequest.getFilter().getExcludeIds().size() > 0) {
			excludeIds = filteredRequest.getFilter().getExcludeIds();
		}

		items = organizationRepository.filterOrganizationsByType(organizationType, nameFilter, excludeIds, filteredRequest.toPageRequest());
		count = organizationRepository.getOrganizationsCountByType(organizationType, nameFilter, excludeIds);

		List<OrganizationViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, OrganizationViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get List of Organizations by Type and Filter
	 *
	 * @return Users List
	 */
	public FilteredResponse<SubsidiaryOrganizationFilter, OrganizationViewDTO> getOrganizationListFiltered(OrganizationType organizationType, FilteredRequest<SubsidiaryOrganizationFilter> filteredRequest) {
		return getOrganizationListFiltered(organizationType, filteredRequest, OrganizationViewDTO.class);
	}

	/**
	 * Get List of Organizations by Type and Filter
	 *
	 * @return Users List
	 */
	public <T extends DTOBase> FilteredResponse<SubsidiaryOrganizationFilter, T> getOrganizationListFiltered(OrganizationType organizationType, FilteredRequest<SubsidiaryOrganizationFilter> filteredRequest, Class<T> viewClass) {
		SubsidiaryOrganizationFilter filter = filteredRequest.getFilter();
		filter.setOrganizationType(organizationType);

		PagedResult<T> result = organizationModelDAO.getItemsPageable(filter, filteredRequest.toPageRequest(), filteredRequest.getSort(), viewClass);
		FilteredResponse<SubsidiaryOrganizationFilter, T> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Change owner to corresponding vendor
	 *
	 * @param vendor
	 * @param owner
	 */
	public void changeOwner(Organizations vendor, Users owner) {
		// Save Audit Log UPDATE event
		UserRefDTO newOwner = (owner != null) ? new UserRefDTO(owner) : null;
		VendorOwnerAuditDTO oldValue = new VendorOwnerAuditDTO(vendor);
		VendorOwnerAuditDTO newValue = new VendorOwnerAuditDTO(vendor);
		newValue.setOwner(newOwner);
		auditLogService.update(
			VItemType.VENDOR_OWNER,
			vendor.getId(),
			oldValue,
			newValue,
			collectAuditLogItems(new VendorEditDTO(vendor), (vendor.getRootParent() != null ? vendor.getRootParent().getId() : null))
		);

		vendor.setOwner(owner);
		organizationRepository.save(vendor);
	}

	/**
	 * Get Subsidiary organization Details to edit
	 *
	 * @param id
	 * @return
	 */
	public OrganizationEditDTO getSubsidiaryDetails(Long id) {
		Organizations organization = getOrganization(id);
		Long currentOrganizationId = getCurrentOrganizationId();

		if (!organization.getOrganizationType().equals(OrganizationType.Subsidiary)) {
			throw new BadRequestException(MessageFormat.format("Only Subsidiary Organization can be Edited [{0}]", organization.getName()), ApplicationExceptionCodes.ONLY_SUBSIDIARY_ORGANIZATION_CAN_BE_UPDATED);
		}

		if (!currentOrganizationId.equals(organization.getRootParent().getId())) {
			throw new ForbiddenException(MessageFormat.format("Organization is not allowed for this user [{0}]", organization.getName()), ApplicationExceptionCodes.ACCESS_TO_ORGANIZATION_FORBIDDEN);
		}

		OrganizationEditDTO result = new OrganizationEditDTO(organization);

		return result;
	}

	/**
	 * Create new Organization
	 *
	 * @return New Organization
	 */
	public OrganizationEditDTO createSubsidiary(OrganizationEditDTO newItemDTO) {

		Organizations currentOrganization = getCurrentOrganizationEntity();

		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()), ApplicationExceptionCodes.CREATE_IS_NOT_ALLOWED_FOR_ITEM_WITH_EXISTING_ID);
		}

		// Verify organization with such name not exists
		if (organizationRepository.findFirstByNameAndRootParentAndIdIsNotIn(newItemDTO.getName(), currentOrganization, Arrays.asList(0l)).isPresent()) {
			throw new ConflictException(MessageFormat.format("Organization with this name already registered in the system [{0}]", newItemDTO.getName()), ApplicationExceptionCodes.ORGANIZATION_WITH_NAME_ALREADY_EXISTS);
		}

		Organizations newItem = new Organizations();
		newItem.setOrganizationType(OrganizationType.Subsidiary);
		newItem.setRootParent(getCurrentOrganizationEntity());

		applyEntityChanges(newItemDTO, newItem);

		Organizations saveResult = organizationRepository.save(newItem);

		OrganizationEditDTO result = new OrganizationEditDTO(saveResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.SUBSIDIARY_ORGANIZATION,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getRootParent().getId())
		);

		return result;
	}

	/**
	 * Update Organization
	 *
	 * @return New Organization
	 */
	public OrganizationEditDTO updateSubsidiary(OrganizationEditDTO itemDTO) {

		OrganizationEditDTO result;

		try {

			Organizations currentOrganization = getCurrentOrganizationEntity();

			// Get Existing item from the database
			Organizations existingItem = organizationRepository.findById(itemDTO.getId()).get();
			OrganizationEditDTO existingItemDTO = new OrganizationEditDTO(existingItem);

			if (!existingItem.getOrganizationType().equals(OrganizationType.Subsidiary)) {
				throw new BadRequestException(MessageFormat.format("Only Subsidiary Organization can be Edited [{0}]", itemDTO.getName()), ApplicationExceptionCodes.ONLY_SUBSIDIARY_ORGANIZATION_CAN_BE_UPDATED);
			}

			// Verify organization with such name not exists
			if (organizationRepository.findFirstByNameAndRootParentAndIdIsNotIn(itemDTO.getName(), currentOrganization, Arrays.asList(itemDTO.getId())).isPresent()) {
				throw new ConflictException(MessageFormat.format("Organization with this name already registered in the system [{0}]", itemDTO.getName()), ApplicationExceptionCodes.ORGANIZATION_WITH_NAME_ALREADY_EXISTS);
			}

			// Update item details
			Organizations updatedItem = existingItem;

			applyEntityChanges(itemDTO, updatedItem);

			// Save to the database
			Organizations saveResult = organizationRepository.save(updatedItem);

			result = new OrganizationEditDTO(saveResult);

			// Save Audit Log UPDATE event
			auditLogService.update(
				VItemType.SUBSIDIARY_ORGANIZATION,
				saveResult.getId(),
				existingItemDTO,
				result,
				collectAuditLogItems(result, (existingItem.getRootParent() != null ? existingItem.getRootParent().getId() : null))
			);

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Organization not found in the system [{0}]", itemDTO.getId()), ApplicationExceptionCodes.ORGANIZATION_NOT_EXISTS);
		}

		return result;
	}

	/**
	 * Delete Organization
	 *
	 * @return New Organization
	 */
	public OrganizationEditDTO deleteSubsidiaryById(Long itemId) {

		OrganizationEditDTO result;

		try {
			Long currentOrganizationId = getCurrentOrganizationId();

			// Get Existing item from the database
			Organizations organization = organizationRepository.findById(itemId).get();

			if (!organization.getOrganizationType().equals(OrganizationType.Subsidiary)) {
				throw new BadRequestException(MessageFormat.format("Only Subsidiary Organization can be Deleted [{0}]", organization.getName()), ApplicationExceptionCodes.ONLY_SUBSIDIARY_ORGANIZATION_CAN_BE_REMOVED);
			}

			if (!currentOrganizationId.equals(organization.getRootParent().getId())) {
				throw new ForbiddenException(MessageFormat.format("Organization is not allowed for this user [{0}]", organization.getName()), ApplicationExceptionCodes.ACCESS_TO_ORGANIZATION_FORBIDDEN);
			}

			result = new OrganizationEditDTO(organization);
			OrganizationEditDTO existingItemDTO = result;

			// Save to the database
			organizationRepository.delete(organization);

			// Save Audit Log DELETE event
			auditLogService.delete(
				VItemType.SUBSIDIARY_ORGANIZATION,
				existingItemDTO.getId(),
				existingItemDTO,
				collectAuditLogItems(existingItemDTO, organization.getId())
			);
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Organization not found in the system [{0}]", itemId), ApplicationExceptionCodes.ORGANIZATION_NOT_EXISTS);
		} catch (ConstraintViolationException exception) {
			throw new ForbiddenException(MessageFormat.format("Failed to delete Organization [{0}] because it has constraints with other data.", itemId), ApplicationExceptionCodes.ORGANIZATION_CANNOT_BE_REMOVED);
		}

		return result;
	}

	/**
	 * Fill the set of entity relations
	 *
	 * @param itemDTO
	 * @param entity
	 */
	protected void applyEntityChanges(OrganizationEditDTO itemDTO, Organizations entity) {

		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		entity.setTaxId(itemDTO.getTaxId());
		entity.setVatId(itemDTO.getVatId());
		entity.setStreetAddress1(itemDTO.getStreetAddress1());
		entity.setStreetAddress2(itemDTO.getStreetAddress2());
		entity.setZip(itemDTO.getZip());
		entity.setPhone(itemDTO.getPhone());
		entity.setSite(itemDTO.getSite());
		entity.setLogo(itemDTO.getLogo());
		entity.setNotes(itemDTO.getNotes());
		entity.setAverageRevenue(itemDTO.getAverageRevenue());
		entity.setIsPublicCompany(itemDTO.getIsPublicCompany());
		entity.setMarketCapitalizationNumber(itemDTO.getMarketCapitalizationNumber());
		entity.setUseMultiFactorAuth(itemDTO.getUseMultiFactorAuth());
		entity.setInsuranceLimit(itemDTO.getInsuranceLimit());
		entity.setInsuranceDeductible(itemDTO.getInsuranceDeductible());

		// Net Risk Bearing Capacity
		entity.setRevenue(itemDTO.getRevenue());
		entity.setEbitda(itemDTO.getEbitda());
		entity.setDebt(itemDTO.getDebt());
		entity.setPensionDebt(itemDTO.getPensionDebt());
		entity.setCreditRating(itemDTO.getCreditRating());
		entity.setGrossRiskBearingCapacity(itemDTO.getGrossRiskBearingCapacity());
		entity.setBusinessRiskBearingCapacity(itemDTO.getBusinessRiskBearingCapacity());
		entity.setCyberRiskBearingCapacity(itemDTO.getCyberRiskBearingCapacity());

		// Additional Properties
		if (itemDTO.getRecordPriceLimit() != null) entity.setRecordPriceLimit(itemDTO.getRecordPriceLimit());
		if (itemDTO.getInsuranceLimit() != null) entity.setInsuranceLimit(itemDTO.getInsuranceLimit());
		if (itemDTO.getInsuranceDeductible() != null) entity.setInsuranceDeductible(itemDTO.getInsuranceDeductible());
		if (itemDTO.getAverageRevenue() != null) entity.setAverageRevenue(itemDTO.getAverageRevenue());
		if (itemDTO.getMarketCapitalizationNumber() != null) entity.setMarketCapitalizationNumber(itemDTO.getMarketCapitalizationNumber());

		if (itemDTO.getRevenue() != null) entity.setRevenue(itemDTO.getRevenue());
		if (itemDTO.getEbitda() != null) entity.setEbitda(itemDTO.getEbitda());
		if (itemDTO.getDebt() != null) entity.setDebt(itemDTO.getDebt());
		if (itemDTO.getPensionDebt() != null) entity.setPensionDebt(itemDTO.getPensionDebt());
		if (itemDTO.getCreditRating() != null) entity.setCreditRating(itemDTO.getCreditRating());
		if (itemDTO.getGrossRiskBearingCapacity() != null) entity.setGrossRiskBearingCapacity(itemDTO.getGrossRiskBearingCapacity());
		if (itemDTO.getBusinessRiskBearingCapacity() != null) entity.setBusinessRiskBearingCapacity(itemDTO.getBusinessRiskBearingCapacity());
		if (itemDTO.getCyberRiskBearingCapacity() != null) entity.setCyberRiskBearingCapacity(itemDTO.getCyberRiskBearingCapacity());

		if (itemDTO.getMaximumWrittenPremium() != null) entity.setMaximumWrittenPremium(itemDTO.getMaximumWrittenPremium());
		if (itemDTO.getMaximumCarrierLimit() != null) entity.setMaximumCarrierLimit(itemDTO.getMaximumCarrierLimit());
		if (itemDTO.getRansomwareSublimit() != null) entity.setRansomwareSublimit(itemDTO.getRansomwareSublimit());
		if (itemDTO.getPrivacyGdprSublimit() != null) entity.setPrivacyGdprSublimit(itemDTO.getPrivacyGdprSublimit());
		if (itemDTO.getPrivacyCcpaSublimit() != null) entity.setPrivacyCcpaSublimit(itemDTO.getPrivacyCcpaSublimit());
		if (itemDTO.getDdosSublimit() != null) entity.setDdosSublimit(itemDTO.getDdosSublimit());

		// Cyber Insurance
		if (itemDTO.getTotalHeadCount() != null) entity.setTotalHeadCount(itemDTO.getTotalHeadCount());
		if (itemDTO.getCompanyITBudget() != null) entity.setCompanyITBudget(itemDTO.getCompanyITBudget());
		if (itemDTO.getItHeadCount() != null) entity.setItHeadCount(itemDTO.getItHeadCount());
		if (itemDTO.getSecurityHeadCount() != null) entity.setSecurityHeadCount(itemDTO.getSecurityHeadCount());
		if (itemDTO.getItSecurityBudget() != null) entity.setItSecurityBudget(itemDTO.getItSecurityBudget());
		if (itemDTO.getNumberOfEmployees() != null) entity.setNumberOfEmployees(itemDTO.getNumberOfEmployees());
		if (itemDTO.getNumberOfIndependentContractors() != null) entity.setNumberOfIndependentContractors(itemDTO.getNumberOfIndependentContractors());
		if (itemDTO.getRevenueIn2021() != null) entity.setRevenueIn2021(itemDTO.getRevenueIn2021());
		if (itemDTO.getRevenueIn2022() != null) entity.setRevenueIn2022(itemDTO.getRevenueIn2022());
		if (itemDTO.getRevenueIn2023() != null) entity.setRevenueIn2023(itemDTO.getRevenueIn2023());
		if (itemDTO.getInhouseDataCentersCount() != null) entity.setInhouseDataCentersCount(itemDTO.getInhouseDataCentersCount());
		if (itemDTO.getExternalDataCentersCount() != null) entity.setExternalDataCentersCount(itemDTO.getExternalDataCentersCount());
		if (itemDTO.getPersonallyIdentifiableInformation() != null) entity.setPersonallyIdentifiableInformation(itemDTO.getPersonallyIdentifiableInformation());
		if (itemDTO.getPaymentCardIndustry() != null) entity.setPaymentCardIndustry(itemDTO.getPaymentCardIndustry());
		if (itemDTO.getProtectedHealthInformation() != null) entity.setProtectedHealthInformation(itemDTO.getProtectedHealthInformation());

		if (itemDTO.getIsInOfacList() != null) entity.setIsInOfacList(itemDTO.getIsInOfacList());
		if (itemDTO.getPastSecurityIncidents() != null) entity.setPastSecurityIncidents(itemDTO.getPastSecurityIncidents());

		if (itemDTO.getZoomInfoId() != null) {
			if (itemDTO.getZoomInfoId() > 0) {
				entity.setZoomInfoId(itemDTO.getZoomInfoId());
			} else {
				entity.setZoomInfoId(null);
			}
		}

		if (itemDTO.getCountry() != null && itemDTO.getCountry().getId() != null) {
			entity.setCountry(countryRepository.findById(itemDTO.getCountry().getId()).orElse(null));
		} else {
			entity.setCountry(null);
		}
		if (itemDTO.getState() != null && itemDTO.getState().getId() != null) {
			entity.setState(stateRepository.findById(itemDTO.getState().getId()).orElse(null));
		} else {
			entity.setState(null);
		}
		if (itemDTO.getCity() != null && itemDTO.getCity().getId() != null) {
			entity.setCity(cityRepository.findById(itemDTO.getCity().getId()).orElse(null));
		} else {
			entity.setCity(null);
		}
		if (itemDTO.getCurrency() != null && itemDTO.getCurrency().getId() != null) {
			entity.setCurrency(currencyRepository.findById(itemDTO.getCurrency().getId()).orElse(null));
		} else {
			entity.setCurrency(null);
		}
		if (itemDTO.getLanguage() != null && itemDTO.getLanguage().getId() != null) {
			entity.setLanguage(languageRepository.findById(itemDTO.getLanguage().getId()).orElse(null));
		} else {
			entity.setLanguage(null);
		}
		if (itemDTO.getStatus() != null && itemDTO.getStatus().getId() != null) {
			entity.setStatus(statusRepository.findById(itemDTO.getStatus().getId()).orElse(null));
		} else {
			entity.setStatus(null);
		}
		if (itemDTO.getParent() != null && itemDTO.getParent().getId() != null) {
			entity.setParent(organizationRepository.findById(itemDTO.getParent().getId()).orElse(null));
		} else {
			entity.setParent(null);
		}
		if (itemDTO.getOwner() != null && itemDTO.getOwner().getId() != null) {
			entity.setOwner(userService.getUser(itemDTO.getOwner().getId()));
		} else {
			entity.setOwner(null);
		}
		if (itemDTO.getIndustry() != null && itemDTO.getIndustry().getId() != null) {
			entity.setIndustry(industryRepository.findById(itemDTO.getIndustry().getId()).orElse(null));
		} else {
			entity.setIndustry(null);
		}

		// Set Assessment level
		if (itemDTO.getAssessmentFrameworkLevel() != null) {
			entity.setAssessmentFrameworkLevel(itemDTO.getAssessmentFrameworkLevel());
		}

		// Set Technologies
		Optional.ofNullable(itemDTO.getTechnologies()).ifPresent(technologiesRefDTOList -> {
			entity.setTechnologies(new HashSet<>());
			technologiesRefDTOList.stream().forEach(technologyRefDTO -> {
				entity.getTechnologies().add(technologyService.getTechnologyForCurrentOrganization(technologyRefDTO.getId()));
			});
		});
	}

	/**
	 * Get Path for Parent and Organization
	 *
	 * @param parentPath
	 * @param organizationId
	 * @return
	 */
	public Organizations getParentByPath(String parentPath, Long organizationId) {
		Organizations result = null;

		if (StringUtils.isNotEmpty(parentPath)) {
			String[] parents = StringUtils.split(parentPath,";");
			List<String> parentsList = Arrays.stream(parents).map(parentName -> parentName.trim()).collect(Collectors.toList());
			Collections.reverse(parentsList);

			Optional<Organizations> currentParent = Optional.empty();
			if (parentsList.size() > 0) {
				currentParent = organizationRepository.getByNameAndNoParentForRootOrganization(parentsList.get(0), organizationId);

				if (currentParent.isPresent()) {
					for (int i = 1; i < parentsList.size(); i++) {
						String parentName = parentsList.get(i).trim();
						currentParent = organizationRepository.getByParentNameForRootOrganization(parentName, currentParent.get().getId(), organizationId);

						if (currentParent.isEmpty()) return null;
					}

					result = currentParent.get();
				}
			}
		}

		return result;
	}

	@Transactional
	public void updateOrganizationPowerBICapacity(Long userId) {
		Users user = userService.getUser(userId);
		if (user.getOrganization() != null) {
			Organizations organization = organizationRepository.findById(user.getOrganization().getId()).get();
			final Long organizationId = organization.getId();
			Set<Roles> userRoles = user.getRoles();

			List<Long> userRoleIds = userRoles.stream().map(Roles::getId).collect(Collectors.toList());

			List<ExternalAnalytics> powerBIReports = externalAnalyticsRepository.getListByRolesAndOrganizationIdAndType(userRoleIds, organizationId, ExternalAnalyticsType.POWER_BI);
			if (CollectionUtils.isNotEmpty(powerBIReports)) {
				log.info("## Updating PowerBI Capacity. For User '{}' and Organization '{}'.", user.getFullName(), organization.getName());

				// Date expirationDate = new LocalDateTime().plusMinutes(30).toDate();
				final Date expirationDate = powerBIAdminService.calculatePowerBIEmbedCapacityExpirationDate();
				if (organization.getPowerbiCapacityExpirationDate() == null || organization.getPowerbiCapacityExpirationDate().before(new Date())
					|| (!"STARTED".equalsIgnoreCase(organization.getPowerbiCapacityStatus()) && !"PENDING".equalsIgnoreCase(organization.getPowerbiCapacityStatus()))
				) {

					// Saving Organization Details in the Separate Flow
					TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
					transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
					organization = transactionTemplate.execute(status -> {
						log.info("## [updateOrganizationPowerBICapacity()] PENDING PowerBI Capacity status.");
						Organizations organizationItem = organizationRepository.findById(organizationId).get();
						organizationItem.setPowerbiCapacityExpirationDate(LocalDateTime.now().plusSeconds(30).toDate());
						organizationItem.setPowerbiCapacityStatus("PENDING");
						organizationItem = organizationRepository.save(organizationItem);
						status.flush();

						return organizationItem;
					});

					boolean isStarted = powerBIAdminService.startPowerBIEmbedCapacity();
					if (isStarted) {

						organization = transactionTemplate.execute(status -> {
							log.info("## [updateOrganizationPowerBICapacity()] Set STARTED PowerBI Capacity status with Expiration: {}.", expirationDate);
							Organizations organizationItem = organizationRepository.findById(organizationId).get();
							organizationItem.setPowerbiCapacityName(powerBIConfig.getCapacityName());
							organizationItem.setPowerbiCapacityStatus("STARTED");
							organizationItem.setPowerbiCapacityExpirationDate(expirationDate);
							organizationItem = organizationRepository.save(organizationItem);
							status.flush();

							return organizationItem;
						});

						// Refreshing Reports
						for (ExternalAnalytics externalAnalytics : powerBIReports) {
							Optional<ExternalAnalyticsParameters> applicationIdOpt = externalAnalytics.getExternalAnalyticsParameters().stream().filter(externalAnalyticsParameter -> ExternalAnalyticsParameterType.POWERBI_CLIENT_ID.name().equalsIgnoreCase(externalAnalyticsParameter.getName())).findFirst();
							Optional<ExternalAnalyticsParameters> workspaceIdOpt = externalAnalytics.getExternalAnalyticsParameters().stream().filter(externalAnalyticsParameter -> ExternalAnalyticsParameterType.POWERBI_WORKSPACE_ID.name().equalsIgnoreCase(externalAnalyticsParameter.getName())).findFirst();
							Optional<ExternalAnalyticsParameters> reportIdOpt = externalAnalytics.getExternalAnalyticsParameters().stream().filter(externalAnalyticsParameter -> ExternalAnalyticsParameterType.POWERBI_REPORT_ID.name().equalsIgnoreCase(externalAnalyticsParameter.getName())).findFirst();
							if (applicationIdOpt.isPresent() && workspaceIdOpt.isPresent() && reportIdOpt.isPresent()) {
                                try {
                                    powerBIService.refreshPowerBIReportDataset(applicationIdOpt.get().getValue(), workspaceIdOpt.get().getValue(), reportIdOpt.get().getValue());
                                } catch (JsonProcessingException | MalformedURLException | ExecutionException | InterruptedException e) {
                                    log.error(String.format("[POWER_BI] Failed to Refresh Power BI %s Workspace %s Report %s data", applicationIdOpt.get().getValue(), workspaceIdOpt.get().getValue(), reportIdOpt.get().getValue()), e);
                                }
                            }
						}
					}

				}

				// Save capacity status for Organization
				if (!"STARTED".equalsIgnoreCase(organization.getPowerbiCapacityStatus())) {
					// Handle Failed Start
				}

			} else {
				log.info("## Verifying PowerBI Capacity expiration. User '{}' for Organization '{}' doesn't have any ALLOWED PowerBI reports. Skipping.", user.getFullName(), user.getOrganization().getName());
			}

		} else {
			log.info("## Verifying PowerBI Capacity expiration. User '{}' doesn't belong to any organization. Skipping.", user.getFullName());
		}

	}

	@Transactional
	public void releaseOrganizationPowerBICapacity(Long userId) {
		Users user = userService.getUser(userId);
		if (user.getOrganization() != null) {
			Organizations organization = organizationRepository.findById(user.getOrganization().getId()).get();
			Long organizationId = organization.getId();
			Set<Roles> userRoles = user.getRoles();

			List<Long> userRoleIds = userRoles.stream().map(Roles::getId).collect(Collectors.toList());

			List<ExternalAnalytics> powerBIReports = externalAnalyticsRepository.getListByRolesAndOrganizationIdAndType(userRoleIds, organizationId, ExternalAnalyticsType.POWER_BI);
			if (CollectionUtils.isNotEmpty(powerBIReports)) {
				log.info("## Pausing PowerBI Capacity. For User '{}' and Organization '{}'.", user.getFullName(), organization.getName());

				// Date expirationDate = new LocalDateTime().plusMinutes(30).toDate();
				boolean isPaused = powerBIAdminService.suspendPowerBIEmbedCapacity();
				if (isPaused) {
					organization.setPowerbiCapacityName(powerBIConfig.getCapacityName());
					organization.setPowerbiCapacityStatus("SUSPENDED");
					organization.setPowerbiCapacityExpirationDate(LocalDateTime.now().plusSeconds(120).toDate());
					organizationRepository.save(organization);
				}

			} else {
				log.info("## Verifying PowerBI Capacity SUSPENSION. User '{}' for Organization '{}' doesn't have any ALLOWED PowerBI reports. Skipping.", user.getFullName(), user.getOrganization().getName());
			}

		} else {
			log.info("## Verifying PowerBI Capacity SUSPENSION. User '{}' doesn't belong to any organization. Skipping.", user.getFullName());
		}

	}

	@Transactional
	public void cleanupOrganizationPowerBICapacity() {
		List<Organizations> powerBiOrganizationsList = organizationRepository.filterOrganizationsByPowerBIExpirationDateAndStatus(new Date(), Arrays.asList("STARTED", "SUSPENDED"));

		if (CollectionUtils.isNotEmpty(powerBiOrganizationsList)) {
			log.info("## [cleanupOrganizationPowerBICapacity()] Trying to PAUSE PowerBI Embed environment: " + powerBIConfig.getCapacityName());
			boolean isSuspended = powerBIAdminService.suspendPowerBIEmbedCapacity();

			for (Organizations organization : powerBiOrganizationsList) {
				if (isSuspended) {
					organization.setPowerbiCapacityStatus("PAUSED");
					organization.setPowerbiCapacityExpirationDate(null);
					organizationRepository.save(organization);
				}
			}
		}
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	protected AuditLogItemId[] collectAuditLogItems(OrganizationEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getOwner() != null) logItems.add(AuditLogItemId.of(VItemType.VENDOR_OWNER, existingItemDTO.getOwner().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
