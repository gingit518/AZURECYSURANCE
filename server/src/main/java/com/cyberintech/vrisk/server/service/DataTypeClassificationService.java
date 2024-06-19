package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.data.OrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.AdminDataTypeClassificationViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationEditDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.DataTypeClassification;
import com.cyberintech.vrisk.server.repository.jpa.DataTypeClassificationRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

/**
 * Data Type Classifications management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
@Getter
public class DataTypeClassificationService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private DataDomainsService dataDomainsService;

	@Autowired
	private DataFieldsService dataFieldsService;

	@Autowired
	private DataTypeClassificationRepository dataTypeClassificationRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private RegulationService regulationService;

	@Autowired
	private UserService userService;

	/**
	 * Get Data Type Classifications List
	 *
	 * @return Data Type Classifications List
	 */
	public List<DataTypeClassificationViewDTO> getList() {
		List<DataTypeClassification> items = dataTypeClassificationRepository.findAll();

		List<DataTypeClassificationViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, DataTypeClassificationViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Data Type Classifications List
	 *
	 * @return Users List
	 */
	public FilteredResponse<OrganizationFilter, AdminDataTypeClassificationViewDTO> getAdminListFiltered(FilteredRequest<OrganizationFilter> filteredRequest) {
		List<DataTypeClassification> items = null;
		Long count = 0l;
		FilteredResponse<OrganizationFilter, AdminDataTypeClassificationViewDTO> filteredResponse = new FilteredResponse<OrganizationFilter, AdminDataTypeClassificationViewDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = filteredRequest.getFilter().getParent() != null && filteredRequest.getFilter().getParent().getId() != null ? filteredRequest.getFilter().getParent().getId() : null;
		if (organizationId != null) {
			items = dataTypeClassificationRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
			count = dataTypeClassificationRepository.getCountByOrganizationAndName(organizationId, namePattern);
		} else if (filteredRequest.getFilter() != null && Boolean.TRUE.equals(filteredRequest.getFilter().getGlobalOnly())) {
			items = dataTypeClassificationRepository.getListByNameOnlyGlobal(namePattern, filteredRequest.toPageRequest());
			count = dataTypeClassificationRepository.getCountByNameOnlyGlobal(namePattern);
		} else {
			items = dataTypeClassificationRepository.getListByName(namePattern, filteredRequest.toPageRequest());
			count = dataTypeClassificationRepository.getCountByName(namePattern);
		}

		List<AdminDataTypeClassificationViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, AdminDataTypeClassificationViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Data Type Classifications List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, DataTypeClassificationViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<DataTypeClassification> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, DataTypeClassificationViewDTO> filteredResponse = new FilteredResponse<NameFilter, DataTypeClassificationViewDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = dataTypeClassificationRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = dataTypeClassificationRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<DataTypeClassificationViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, DataTypeClassificationViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Data Type Classification details
	 *
	 * @return Data Type Classification Details
	 */
	public DataTypeClassification getDataTypeClassificationForCurrentOrganization(Long itemId) {
		DataTypeClassification itemDetails;

		try {
			itemDetails = dataTypeClassificationRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Data Type Classification not found in the database [{0}]", itemId));
		}

		// Verify Data Type Classification and Organization
		if (!userService.isSuperAdmin() && itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Data Type Classification [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Data Type Classification DTO details
	 *
	 * @return Data Type Classification Details
	 */
	public DataTypeClassificationEditDTO getDetails(Long itemId) {

		DataTypeClassification itemDetails = getDataTypeClassificationForCurrentOrganization(itemId);

		DataTypeClassificationEditDTO result = new DataTypeClassificationEditDTO(itemDetails, userService.isSuperAdmin());

		return result;
	}


	/**
	 * Create new Data Type Classification Domain
	 *
	 * @return New Data Type Classification
	 */
	public DataTypeClassificationEditDTO create(DataTypeClassificationEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		DataTypeClassification newItem = newItemDTO.toEntity();
		DataTypeClassification newItem = new DataTypeClassification();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		DataTypeClassification saveResult = dataTypeClassificationRepository.save(newItem);

		DataTypeClassificationEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.DATA_TYPE_CLASS,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Data Type Classification
	 *
	 * @return Updated Qualitative Domains
	 */
	public DataTypeClassificationEditDTO update(DataTypeClassificationEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		DataTypeClassification existingItem = getDataTypeClassificationForCurrentOrganization(itemDTO.getId());
		DataTypeClassificationEditDTO existingItemDTO = new DataTypeClassificationEditDTO(existingItem);

		// Verify Data Type Classification and Organization
		if (!isSuperAdmin) {
			if (existingItem.getOrganizationId() == null) {
				throw new ForbiddenException(MessageFormat.format("Data Type Classification [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
			} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
				throw new ForbiddenException(MessageFormat.format("Organization for Data Type Classification [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
			}
		} else {
			existingItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		DataTypeClassification saveResult = dataTypeClassificationRepository.save(existingItem);

		DataTypeClassificationEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.DATA_TYPE_CLASS,
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
	private void applyEntityChanges(DataTypeClassificationEditDTO itemDTO, DataTypeClassification entity) {
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());

		Optional.ofNullable(itemDTO.getRegulations()).ifPresent(regulationDTOList -> {
			entity.setRegulations(new HashSet<>());
			regulationDTOList.stream().forEach(regulationDTO -> {
				entity.getRegulations().add(regulationService.getItem(regulationDTO.getId()));
			});
		});

		// Set Data Types
		Optional.ofNullable(itemDTO.getDataDomains()).ifPresent(dataDomainsList -> {
			entity.setDataDomains(new HashSet<>());
			dataDomainsList.stream().forEach(dataDomainDTO -> {
				entity.getDataDomains().add(dataDomainsService.getDataDomainsForCurrentOrganization(dataDomainDTO.getId()));
			});
		});

		// Set Data Fields
		Optional.ofNullable(itemDTO.getDataFields()).ifPresent(dataFieldsList -> {
			entity.setDataFields(new HashSet<>());
			dataFieldsList.stream().forEach(dataFieldDTO -> {
				entity.getDataFields().add(dataFieldsService.getDataFieldsForCurrentOrganization(dataFieldDTO.getId()));
			});
		});
	}

	/**
	 * Deletes Data Type Classification
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		DataTypeClassification existingItem = getDataTypeClassificationForCurrentOrganization(itemId);
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Data Type Classification [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getName()));
		}
		DataTypeClassificationEditDTO existingItemDTO = new DataTypeClassificationEditDTO(existingItem);
		dataTypeClassificationRepository.delete(existingItem);
		dataTypeClassificationRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.DATA_TYPE_CLASS,
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
	private AuditLogItemId[] collectAuditLogItems(DataTypeClassificationEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
