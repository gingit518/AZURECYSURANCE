package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.TechnologyAssetModelDAO;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.SystemFilter;
import com.cyberintech.vrisk.server.model.data.TechnologyAssetFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.systems.TechnologyAssetEditDTO;
import com.cyberintech.vrisk.server.model.dto.systems.TechnologyAssetViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DeploymentType;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyAssetRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

/**
 * TechnologyAssets management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-09
 */
@Service
public class TechnologyAssetsService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private DataAssetClassificationService dataAssetClassificationService;

	@Autowired
	private DataDomainsService dataDomainsService;

	@Autowired
	private DataTypeClassificationService dataTypeClassificationService;

	@Autowired
	private OrganizationService organizationService;

	@Lazy
	@Autowired
	private ProcessService processService;

	@Autowired
	private TechnologyAssetRepository technologyAssetRepository;

	@Autowired
	private TechnologyAssetModelDAO technologyAssetModelDAO;

	@Lazy
	@Autowired
	private TechnologyCategoryService technologyCategoryService;

	@Lazy
	@Autowired
	private TechnologyClassTypeService technologyClassTypeService;

	@Lazy
	@Autowired
	private TechnologyService technologyService;

	@Lazy
	@Autowired
	private TechnologySubcategoryService technologySubcategoryService;

	@Autowired
	private UserService userService;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Get TechnologyAssets List
	 *
	 * @return TechnologyAssets List
	 */
	public List<TechnologyAssetViewDTO> getList() {
		List<TechnologyAssets> items = technologyAssetRepository.findAll();

		List<TechnologyAssetViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, TechnologyAssetViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get TechnologyAssets List
	 *
	 * @return Users List
	 */
	public FilteredResponse<TechnologyAssetFilter, TechnologyAssetViewDTO> getListFiltered(FilteredRequest<TechnologyAssetFilter> filteredRequest) {

		PagedResult<TechnologyAssetViewDTO> result = technologyAssetModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<TechnologyAssetFilter, TechnologyAssetViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get TechnologyAsset details
	 *
	 * @return TechnologyAsset Details
	 */
	public TechnologyAssets getTechnologyAssetForCurrentOrganization(Long itemId) {
		TechnologyAssets itemDetails;

		try {
			itemDetails = technologyAssetRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("TechnologyAsset not found in the database [{0}]", itemId));
		}

		// Verify TechnologyAsset and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for TechnologyAsset [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get TechnologyAsset DTO details
	 *
	 * @return TechnologyAsset Details
	 */
	public TechnologyAssetEditDTO getDetails(Long itemId) {

		TechnologyAssets itemDetails = getTechnologyAssetForCurrentOrganization(itemId);

		TechnologyAssetEditDTO result = new TechnologyAssetEditDTO(itemDetails);

		return result;
	}

	/**
	 * Create new TechnologyAsset Domain
	 *
	 * @return New TechnologyAsset
	 */
	@Transactional
	public TechnologyAssetEditDTO create(TechnologyAssetEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		TechnologyAssets newItem = newItemDTO.toEntity();
		TechnologyAssets newItem = new TechnologyAssets();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setIsEtl(false);
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		TechnologyAssets saveResult = technologyAssetRepository.save(newItem);

		TechnologyAssetEditDTO result = getDetails(saveResult.getId());

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
	 * Update TechnologyAsset
	 *
	 * @return Updated Qualitative Domains
	 */
	@Transactional
	public TechnologyAssetEditDTO update(TechnologyAssetEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		TechnologyAssets existingItem = getTechnologyAssetForCurrentOrganization(itemDTO.getId());
		TechnologyAssetEditDTO existingItemDTO = new TechnologyAssetEditDTO(existingItem);

		// Verify TechnologyAsset and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for TechnologyAsset [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Save Previous Owner
		Users prevOwner = existingItem.getOwner();

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		TechnologyAssets saveResult = technologyAssetRepository.save(existingItem);

		TechnologyAssetEditDTO result = getDetails(saveResult.getId());

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
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(TechnologyAssetEditDTO itemDTO, TechnologyAssets entity) {

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
		if (itemDTO.getAssetName() != null) entity.setAssetName(itemDTO.getAssetName());
		if (itemDTO.getIpAddress() != null) entity.setIpAddress(itemDTO.getIpAddress());
		if (itemDTO.getSerialNumber() != null) entity.setSerialNumber(itemDTO.getSerialNumber());
		if (itemDTO.getAssetDomainFunction() != null) entity.setAssetDomainFunction(itemDTO.getAssetDomainFunction());
		if (itemDTO.getOsName() != null) entity.setOsName(itemDTO.getOsName());
		if (itemDTO.getLocation() != null) entity.setLocation(itemDTO.getLocation());
		if (itemDTO.getHardwareSubstatus() != null) entity.setHardwareSubstatus(itemDTO.getHardwareSubstatus());
		if (itemDTO.getDiscoverySource() != null) entity.setDiscoverySource(itemDTO.getDiscoverySource());
		if (itemDTO.getDeviceId() != null) entity.setDeviceId(itemDTO.getDeviceId());
		if (itemDTO.getOwnerType() != null) entity.setOwnerType(itemDTO.getOwnerType());

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

		// Set Technology Scope
		if (itemDTO.getTechnologyCategory() != null && itemDTO.getTechnologyCategory().getId() != null) {
			TechnologyCategories technologyCategory = technologyCategoryService.getTechnologyCategoryForCurrentOrganization(itemDTO.getTechnologyCategory().getId());
			entity.setTechnologyCategory(technologyCategory);
		}
		if (itemDTO.getTechnologySubcategory() != null && itemDTO.getTechnologySubcategory().getId() != null) {
			TechnologySubcategories technologySubcategory = technologySubcategoryService.getTechnologySubcategoryForCurrentOrganization(itemDTO.getTechnologySubcategory().getId());
			entity.setTechnologySubcategory(technologySubcategory);
		}
		if (itemDTO.getTechnologyClassType() != null && itemDTO.getTechnologyClassType().getId() != null) {
			TechnologyClassTypes technologyClassType = technologyClassTypeService.getTechnologyClassTypeForCurrentOrganization(itemDTO.getTechnologyClassType().getId());
			entity.setTechnologyClassType(technologyClassType);
		}
		if (itemDTO.getTechnology() != null && itemDTO.getTechnology().getId() != null) {
			Technologies technology = technologyService.getTechnologyForCurrentOrganization(itemDTO.getTechnology().getId());
			entity.setTechnology(technology);
		}

		if (itemDTO.getManufacturer() != null && itemDTO.getManufacturer().getId() != null) {
			Organizations manufacturer = organizationService.getVendorForCurrentOrganization(itemDTO.getManufacturer().getId());
			entity.setManufacturer(manufacturer);
		}

		/*
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
		*/

		// Set Technologies
		/*
		Optional.ofNullable(itemDTO.getTechnologies()).ifPresent(technologyRefDTOList -> {
			entity.setTechnologies(new HashSet<>());
			technologyRefDTOList.stream().forEach(technologyRefDTO -> {
				entity.getTechnologies().add(technologyService.getTechnologyForCurrentOrganization(technologyRefDTO.getId()));
			});
		});
		*/

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
	 * Deletes TechnologyAsset
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		TechnologyAssets existingItem = getTechnologyAssetForCurrentOrganization(itemId);
		TechnologyAssetEditDTO existingItemDTO = new TechnologyAssetEditDTO(existingItem);
		technologyAssetRepository.delete(existingItem);
		technologyAssetRepository.flush();

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
	private AuditLogItemId[] collectAuditLogItems(TechnologyAssetEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getOwner() != null) logItems.add(AuditLogItemId.of(VItemType.SYSTEM_OWNER, existingItemDTO.getOwner().getId()));
		if (existingItemDTO.getBusinessUnit() != null) logItems.add(AuditLogItemId.of(VItemType.BUSINESS_UNIT, existingItemDTO.getBusinessUnit().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
