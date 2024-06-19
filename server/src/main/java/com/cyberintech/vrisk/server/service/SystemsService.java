package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.SystemModelDAO;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.SystemFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.audit.items.SystemOwnerAuditDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.qualitative_question.ReassignScoringToUserDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemDataExfiltrationDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemEditDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemGeoParametersDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemViewDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Systems management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class SystemsService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private AssociateSystemRepository associateSystemRepository;

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	@Autowired
	private AssociateVendorService associateVendorService;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private CountryRepository countryRepository;

	@Lazy
	@Autowired
	private CyberRiskScoringService cyberRiskScoringService;

	@Autowired
	private DataAssetClassificationService dataAssetClassificationService;

	@Autowired
	private DataDomainsService dataDomainsService;

	@Autowired
	private DataTypeClassificationService dataTypeClassificationService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private ProcessRepository processRepository;

	@Lazy
	@Autowired
	private ProcessService processService;

	@Autowired
	private QuestionAnswersForSystemRepository questionAnswersForSystemRepository;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private SystemGeoParametersRepository systemGeoParametersRepository;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private SystemModelDAO systemModelDAO;

	@Lazy
	@Autowired
	private TechnologyService technologyService;

	@Autowired
	private UserAssignedSystemRepository userAssignedSystemRepository;

	@Autowired
	private UserService userService;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Get Systems List
	 *
	 * @return Systems List
	 */
	public List<SystemViewDTO> getList() {
		List<Systems> items = systemRepository.findAll();

		List<SystemViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, SystemViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Systems List
	 *
	 * @return Users List
	 */
	public FilteredResponse<SystemFilter, SystemViewDTO> getListFiltered(FilteredRequest<SystemFilter> filteredRequest) {

		PagedResult<SystemViewDTO> result = systemModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<SystemFilter, SystemViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Apply query data
	 *
	 * @param nameFilter
	 * @param systemStatus
	 * @param assetClass
	 * @param businessUnit
	 * @param systemOwner
	 * @param excludeIds
	 * @param organizationId
	 * @param query
	 */
	private void applySearchFilterValues(String nameFilter, Optional<SystemStatus> systemStatus, DataAssetClassificationRefDTO assetClass, DataTypeClassificationRefDTO dataType, BusinessUnitRefDTO businessUnit, UserRefDTO systemOwner, List<Long> excludeIds, Long organizationId, Query query) {
		query.setParameter("organizationId", organizationId);
		if (StringUtils.isNotEmpty(nameFilter)) query.setParameter("name", nameFilter);
		if (excludeIds != null) query.setParameter("excludeIds", excludeIds);
		if (systemStatus.isPresent()) query.setParameter("systemStatus", systemStatus.get());
		if (assetClass != null && assetClass.getId() != null) query.setParameter("assetClassId", assetClass.getId());
		if (dataType != null && dataType.getId() != null) query.setParameter("dataTypeId", dataType.getId());
		if (businessUnit != null && businessUnit.getId() != null) query.setParameter("businessUnitId", businessUnit.getId());
		if (systemOwner != null && systemOwner.getId() != null) query.setParameter("systemOwnerId", systemOwner.getId());
	}

	/**
	 * Get System details
	 *
	 * @return System Details
	 */
	public Systems getSystemForCurrentOrganization(Long itemId) {
		Systems itemDetails;

		try {
			itemDetails = systemRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("System not found in the database [{0}]", itemId));
		}

		// Verify System and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get System DTO details
	 *
	 * @return System Details
	 */
	public SystemEditDTO getDetails(Long itemId) {

		Systems itemDetails = getSystemForCurrentOrganization(itemId);

		SystemEditDTO result = new SystemEditDTO(itemDetails);

		List<Organizations> associateVendors = associateVendorRepository.getVendorsListForSystem(itemId);
		if (CollectionUtils.isNotEmpty(associateVendors)) {
			result.setAssociateVendors(associateVendors.stream().map(OrganizationRefDTO::new).collect(Collectors.toList()));
		}

		return result;
	}


	/**
	 * Update System with Number of Records Processes
	 *
	 * @return Updated Systems
	 */
	public List<SystemViewDTO> updateNumberOfRecordsList(List<SystemViewDTO> itemsList) {

		Users currentUser = userService.getCurrentUserEntity();

		List<SystemViewDTO> result = new ArrayList<>();
		for (SystemViewDTO itemDTO : itemsList) {
			Systems entity = getSystemForCurrentOrganization(itemDTO.getId());
			SystemEditDTO existingItemDTO = new SystemEditDTO(entity);

			if (itemDTO.getNumberOfRecProcessed() != null && !itemDTO.getNumberOfRecProcessed().equals(existingItemDTO.getNumberOfRecProcessed())) { {
				entity.setNumberOfRecProcessed(itemDTO.getNumberOfRecProcessed());
				entity.setUpdatedAt(new Date());
				entity.setUpdatedBy(currentUser);
				systemRepository.save(entity);

				// Save Audit Log UPDATE event
				SystemEditDTO newItemDTO = new SystemEditDTO(entity);
				auditLogService.update(
					VItemType.SYSTEM,
					entity.getId(),
					existingItemDTO,
					newItemDTO,
					collectAuditLogItems(newItemDTO, entity.getOrganizationId())
				);

				result.add(itemDTO);
			}}
		}

		return result;
	}


	/**
	 * Create new System Domain
	 *
	 * @return New System
	 */
	@Transactional
	public SystemEditDTO create(SystemEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		Systems newItem = newItemDTO.toEntity();
		Systems newItem = new Systems();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setIsEtl(false);
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		Systems saveResult = systemRepository.save(newItem);

		// Send User Assignment Notification to the User
		if (saveResult.getOwner() != null) {
			sendUserAssignmentNotification(saveResult);
		}

		SystemEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.SYSTEM,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update System
	 *
	 * @return Updated Qualitative Domains
	 */
	@Transactional
	public SystemEditDTO update(SystemEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		Systems existingItem = getSystemForCurrentOrganization(itemDTO.getId());
		SystemEditDTO existingItemDTO = new SystemEditDTO(existingItem);

		// Verify System and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Save Previous Owner
		Users prevOwner = existingItem.getOwner();

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		Systems saveResult = systemRepository.save(existingItem);

		// Saving Associate Vendors for the System
		if (itemDTO.getAssociateVendors() != null) {
			saveSystemAssociateVendors(existingItem.getId(), itemDTO.getAssociateVendors());
		}

		// Send notification to new system owner if changed
		if (
			(prevOwner != null && saveResult.getOwner() != null && !prevOwner.getId().equals(saveResult.getOwner().getId()))
			|| (prevOwner == null && saveResult.getOwner() != null)
		) {
			sendUserAssignmentNotification(saveResult);
		}

		SystemEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.SYSTEM,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, existingItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update System
	 *
	 * @return Updated Qualitative Domains
	 */
	@Transactional
	public SystemDataExfiltrationDTO update(SystemDataExfiltrationDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		Systems existingItem = getSystemForCurrentOrganization(itemDTO.getId());
		SystemEditDTO existingItemDTO = new SystemEditDTO(existingItem);

		// Verify System and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Save Previous Owner
		Users prevOwner = existingItem.getOwner();

		// Update item details
		existingItem.setNumberOfRecProcessed(itemDTO.getNumberOfRecProcessed());
		applyGeoRecordsProcessed(itemDTO.getGeoRecordsProcessed(), existingItem);

		// Save to the database
		Systems saveResult = systemRepository.save(existingItem);

		SystemDataExfiltrationDTO result = new SystemDataExfiltrationDTO(saveResult);

		// Save Audit Log UPDATE event
		SystemEditDTO systemDTO = new SystemEditDTO(saveResult);
		auditLogService.update(
			VItemType.SYSTEM,
			saveResult.getId(),
			existingItemDTO,
			systemDTO,
			collectAuditLogItems(systemDTO, existingItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Change owner to corresponding system
	 *
	 * @param systems
	 * @param owner
	 */
	public void changeOwner(Systems systems, Users owner) {

		// Save Audit Log UPDATE event
		UserRefDTO newOwner = (owner != null) ? new UserRefDTO(owner) : null;
		SystemOwnerAuditDTO oldValue = new SystemOwnerAuditDTO(systems);
		SystemOwnerAuditDTO newValue = new SystemOwnerAuditDTO(systems);
		newValue.setOwner(newOwner);
		auditLogService.update(
			VItemType.SYSTEM_OWNER,
			systems.getId(),
			oldValue,
			newValue,
			collectAuditLogItems(new SystemEditDTO(systems), systems.getOrganizationId())
		);

		// Update System Owner
		systems.setOwner(owner);
		systems.setUpdatedBy(userService.getCurrentUserEntity());
		systems.setUpdatedAt(new Date());
		systemRepository.save(systems);
	}

	/**
	 * Send User assignment notification
	 *
	 * @param system
	 */
	private void sendUserAssignmentNotification(Systems system) {
		// Create Reassign DTO Details
		ReassignScoringToUserDTO reassignScoringToUser = new ReassignScoringToUserDTO();
		reassignScoringToUser.setItemType(VendorType.System);
		reassignScoringToUser.setItem(new ItemViewDTO(system.getId(), system.getName()));
		reassignScoringToUser.setUser(new UserRefDTO(system.getOwner()));

		cyberRiskScoringService.reassignToUser(null, reassignScoringToUser);
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(SystemEditDTO itemDTO, Systems entity) {

		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		entity.setVersionNumber(itemDTO.getVersionNumber());
		entity.setSystemStatus(itemDTO.getSystemStatus());
		entity.setNumberOfRecProcessed(itemDTO.getNumberOfRecProcessed());
		entity.setRto(itemDTO.getRto());
		entity.setRpo(itemDTO.getRpo());
		entity.setCostToRestore(itemDTO.getCostToRestore());
		entity.setDeploymentType(itemDTO.getDeploymentType());
		if (itemDTO.getSystemType() != null) entity.setSystemType(itemDTO.getSystemType());

		if (itemDTO.getEolDate() != null) entity.setEolDate(itemDTO.getEolDate());
		if (itemDTO.getWarrantyExpiration() != null) entity.setWarrantyExpiration(itemDTO.getWarrantyExpiration());
		if (itemDTO.getIpAddress() != null) entity.setIpAddress(itemDTO.getIpAddress());
		if (itemDTO.getSerialNumber() != null) entity.setSerialNumber(itemDTO.getSerialNumber());
		if (itemDTO.getAssetDomainFunction() != null) entity.setAssetDomainFunction(itemDTO.getAssetDomainFunction());
		if (itemDTO.getOsName() != null) entity.setOsName(itemDTO.getOsName());
		if (itemDTO.getLocation() != null) entity.setLocation(itemDTO.getLocation());
		if (itemDTO.getHardwareSubstatus() != null) entity.setHardwareSubstatus(itemDTO.getHardwareSubstatus());
		if (itemDTO.getDiscoverySource() != null) entity.setDiscoverySource(itemDTO.getDiscoverySource());
		if (itemDTO.getDeviceId() != null) entity.setDeviceId(itemDTO.getDeviceId());

		// Set Default System Type if not defined
		if (entity.getSystemType() == null) {
			entity.setSystemType(SystemType.COTS);
		}
		if (entity.getDeploymentType() == null) {
			entity.setDeploymentType(DeploymentType.ON_PREMISE);
		}

		if (itemDTO.getOwner() != null && itemDTO.getOwner().getId() != null) {
			Users owner = userService.getOrganizationUser(itemDTO.getOwner().getId());
			entity.setOwner(owner);
		}

		if (itemDTO.getInfosecFocalPerson() != null && itemDTO.getInfosecFocalPerson().getId() != null) {
			Users infosecFocalPerson = userService.getOrganizationUser(itemDTO.getInfosecFocalPerson().getId());
			entity.setInfosecFocalPerson(infosecFocalPerson);
		}

		if (itemDTO.getBusinessUnit() != null && itemDTO.getBusinessUnit().getId() != null) {
			BusinessUnits businessUnit = businessUnitService.getBusinessUnitForCurrentOrganization(itemDTO.getBusinessUnit().getId());
			entity.setBusinessUnit(businessUnit);
		}

		// Set Data Asset Classification
		if (itemDTO.getDataAssetClassification() != null && itemDTO.getDataAssetClassification().getId() != null) {
			DataAssetClassification dataAssetClassification = dataAssetClassificationService.getDataAssetClassificationForCurrentOrganization(itemDTO.getDataAssetClassification().getId());
			entity.setDataAssetClassification(dataAssetClassification);
		}

		// Set Data Types
		Optional.ofNullable(itemDTO.getDataTypeClassifications()).ifPresent(dataTypeClassificationRefDTOList -> {
			entity.setDataTypeClassifications(new HashSet<>());
			dataTypeClassificationRefDTOList.stream().forEach(dataTypeClassificationRefDTO -> {
				entity.getDataTypeClassifications().add(dataTypeClassificationService.getDataTypeClassificationForCurrentOrganization(dataTypeClassificationRefDTO.getId()));
			});
		});

		// Set Data Types
		Optional.ofNullable(itemDTO.getDataDomains()).ifPresent(dataDomainsList -> {
			entity.setDataDomains(new HashSet<>());
			dataDomainsList.stream().forEach(dataDomainDTO -> {
				entity.getDataDomains().add(dataDomainsService.getDataDomainsForCurrentOrganization(dataDomainDTO.getId()));
			});
		});

		// Set Processes
		// TODO allow to reset processes to empty list after all Apps will be updated
		if (CollectionUtils.isNotEmpty(itemDTO.getProcesses())) {
			Optional.ofNullable(itemDTO.getProcesses()).ifPresent(processRefDTOS -> {
				entity.setProcesses(new HashSet<>());
				processRefDTOS.stream().forEach(processRefDTO -> {
					entity.getProcesses().add(processService.getProcessForCurrentOrganization(processRefDTO.getId()));
				});
			});
		}

		applyGeoRecordsProcessed(itemDTO.getGeoRecordsProcessed(), entity);

		// Set Technologies
		Optional.ofNullable(itemDTO.getTechnologies()).ifPresent(technologyRefDTOList -> {
			entity.setTechnologies(new HashSet<>());
			technologyRefDTOList.stream().forEach(technologyRefDTO -> {
				entity.getTechnologies().add(technologyService.getTechnologyForCurrentOrganization(technologyRefDTO.getId()));
			});
		});

		// Update is M&A Asses flag in case if it is set
		if (itemDTO.getIsMAAsset() != null) {
			entity.setIsMAAsset(itemDTO.getIsMAAsset());
		}

		// Update is ETL system flag in case if it is set
		if (itemDTO.getIsEtl() != null) {
			entity.setIsEtl(itemDTO.getIsEtl());
		}

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}


	/**
	 * Save associate vendors for the system
	 *
	 * @param systemId
	 * @param associateVendors
	 */
	public void saveSystemAssociateVendors(Long systemId, List<OrganizationRefDTO> associateVendors) {
		List<Organizations> originalVendors = associateVendorRepository.getVendorsListForSystem(systemId);
		Set<Long> originalVendorIdSet = originalVendors.stream().mapToLong(Organizations::getId).boxed().collect(Collectors.toSet());
		Set<Long> newVendorIdSet = associateVendors.stream().mapToLong(OrganizationRefDTO::getId).boxed().collect(Collectors.toSet());

		// Removing associate Vendors
		for (Long vendorId : originalVendorIdSet) {
			if (!newVendorIdSet.contains(vendorId)) {
				associateVendorService.removeSystemFromVendor(vendorId, systemId);
			}
		}

		// Adding Systems to Vendors
		for (Long vendorId : newVendorIdSet) {
			associateVendorService.addSystemToVendor(vendorId, systemId);
		}
	}

	private void applyGeoRecordsProcessed(List<SystemGeoParametersDTO> geoRecordsProcessed, Systems entity) {
		// Set System Geo Parameters Types
		Optional.ofNullable(geoRecordsProcessed).ifPresent(geoParametersDTOList -> {
			entity.setSystemGeoParameters(new HashSet<>());
			geoParametersDTOList.stream().forEach(geoParametersDTO -> {
				SystemGeoParameters systemGeoParameters = null;
				if (geoParametersDTO.getId() == null) {
					systemGeoParameters = new SystemGeoParameters();
					systemGeoParameters.setSystem(entity);
					if (geoParametersDTO.getCountry() != null && geoParametersDTO.getCountry().getId() != null) {
						systemGeoParameters.setCountry(countryRepository.findById(geoParametersDTO.getCountry().getId()).get());
					}
					if (geoParametersDTO.getState() != null && geoParametersDTO.getState().getId() != null) {
						systemGeoParameters.setState(stateRepository.findById(geoParametersDTO.getState().getId()).get());
					}
					systemGeoParameters.setNumberOfRecProcessed(geoParametersDTO.getNumberOfRecProcessed());
					systemGeoParameters = systemGeoParametersRepository.saveAndRefresh(systemGeoParameters);
				} else {
					systemGeoParameters = systemGeoParametersRepository.findById(geoParametersDTO.getId()).get();
					boolean geoUpdated = false;
					if (geoParametersDTO.getCountry() != null && (systemGeoParameters.getCountry() != null || !systemGeoParameters.getCountry().getId().equals(geoParametersDTO.getCountry().getId()))) {
						systemGeoParameters.setCountry(countryRepository.findById(geoParametersDTO.getCountry().getId()).get());
						geoUpdated = true;
					}
					if (geoParametersDTO.getState() != null && (systemGeoParameters.getState() != null || !systemGeoParameters.getState().getId().equals(geoParametersDTO.getState().getId()))) {
						systemGeoParameters.setState(stateRepository.findById(geoParametersDTO.getState().getId()).get());
						geoUpdated = true;
					} else if (systemGeoParameters.getState() != null && geoParametersDTO.getState() == null) {
						systemGeoParameters.setState(null);
						geoUpdated = true;
					}
					if (geoParametersDTO.getNumberOfRecProcessed() != null || systemGeoParameters.getNumberOfRecProcessed() == null) {
						systemGeoParameters.setNumberOfRecProcessed(geoParametersDTO.getNumberOfRecProcessed());
						geoUpdated = true;
					}

					// Updating GEO values
					if (geoUpdated) {
						systemGeoParameters = systemGeoParametersRepository.save(systemGeoParameters);
					}
				}
				entity.getSystemGeoParameters().add(systemGeoParameters);
			});
		});
	}

	/**
	 * Deletes System
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Systems existingItem = getSystemForCurrentOrganization(itemId);
		SystemEditDTO existingItemDTO = new SystemEditDTO(existingItem);
		associateSystemRepository.deleteAllBySystem(existingItem);
		processRepository.deleteAllBySystems(existingItem);
		questionAnswersForSystemRepository.deleteAllBySystem(existingItem);
		userAssignedSystemRepository.deleteAllBySystem(existingItem);
		systemRepository.delete(existingItem);
		systemRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.SYSTEM,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, existingItem.getOrganizationId())
		);

		return itemId;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(SystemEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getOwner() != null) logItems.add(AuditLogItemId.of(VItemType.SYSTEM_OWNER, existingItemDTO.getOwner().getId()));
		if (existingItemDTO.getBusinessUnit() != null) logItems.add(AuditLogItemId.of(VItemType.BUSINESS_UNIT, existingItemDTO.getBusinessUnit().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
