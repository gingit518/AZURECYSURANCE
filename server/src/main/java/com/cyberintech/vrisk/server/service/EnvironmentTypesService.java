package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.technology.EnvironmentTypesDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.EnvironmentTypes;
import com.cyberintech.vrisk.server.repository.jpa.EnvironmentTypesRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Environment Types management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-21
 */
@Service
@Getter
public class EnvironmentTypesService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private EnvironmentTypesRepository environmentTypesRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Environment Types List
	 *
	 * @return Environment Types List
	 */
	public List<EnvironmentTypesDTO> getList() {
		List<EnvironmentTypes> items = environmentTypesRepository.findAll();

		List<EnvironmentTypesDTO> itemDTOs = DTOBase.fromEntitiesList(items, EnvironmentTypesDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Environment Types List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, EnvironmentTypesDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<EnvironmentTypes> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, EnvironmentTypesDTO> filteredResponse = new FilteredResponse<NameFilter, EnvironmentTypesDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = environmentTypesRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = environmentTypesRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<EnvironmentTypesDTO> itemsDTOList = DTOBase.fromEntitiesList(items, EnvironmentTypesDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Environment Type details
	 *
	 * @return Environment Type Details
	 */
	public EnvironmentTypes getEnvironmentTypesForCurrentOrganization(Long itemId) {
		EnvironmentTypes itemDetails;

		try {
			itemDetails = environmentTypesRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Environment Type not found in the database [{0}]", itemId));
		}

		// Verify Environment Type and Organization
		if (!userService.isSuperAdmin() && itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Environment Type [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Environment Type DTO details
	 *
	 * @return Environment Type Details
	 */
	public EnvironmentTypesDTO getDetails(Long itemId) {

		EnvironmentTypes itemDetails = getEnvironmentTypesForCurrentOrganization(itemId);

		EnvironmentTypesDTO result = new EnvironmentTypesDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Environment Type
	 *
	 * @return New Environment Type
	 */
	public EnvironmentTypesDTO create(EnvironmentTypesDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		EnvironmentTypes newItem = newItemDTO.toEntity();
		EnvironmentTypes newItem = new EnvironmentTypes();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		EnvironmentTypes saveResult = environmentTypesRepository.save(newItem);

		EnvironmentTypesDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.ENVIRONMENT_TYPE,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Environment Type
	 *
	 * @return Updated Environment Type
	 */
	public EnvironmentTypesDTO update(EnvironmentTypesDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		// Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		EnvironmentTypes existingItem = getEnvironmentTypesForCurrentOrganization(itemDTO.getId());
		EnvironmentTypesDTO existingItemDTO = new EnvironmentTypesDTO(existingItem);

		// Verify Environment Type and Organization
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Environment Type [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
		} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Environment Type [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		EnvironmentTypes saveResult = environmentTypesRepository.save(existingItem);

		EnvironmentTypesDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.ENVIRONMENT_TYPE,
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
	private void applyEntityChanges(EnvironmentTypesDTO itemDTO, EnvironmentTypes entity) {
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
	}

	/**
	 * Deletes Environment Type
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		EnvironmentTypes existingItem = getEnvironmentTypesForCurrentOrganization(itemId);
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Environment Type [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getName()));
		}
		EnvironmentTypesDTO existingItemDTO = new EnvironmentTypesDTO(existingItem);
		environmentTypesRepository.delete(existingItem);
		environmentTypesRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.ENVIRONMENT_TYPE,
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
	private AuditLogItemId[] collectAuditLogItems(EnvironmentTypesDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
