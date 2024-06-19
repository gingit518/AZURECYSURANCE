package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataFieldsDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.DataFields;
import com.cyberintech.vrisk.server.repository.jpa.DataFieldsRepository;
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
 * Data Fields management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-16
 */
@Service
@Getter
public class DataFieldsService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private DataFieldsRepository dataFieldsRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Data Fields List
	 *
	 * @return Data Fields List
	 */
	public List<DataFieldsDTO> getList() {
		List<DataFields> items = dataFieldsRepository.findAll();

		List<DataFieldsDTO> itemDTOs = DTOBase.fromEntitiesList(items, DataFieldsDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Data Fields List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, DataFieldsDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<DataFields> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, DataFieldsDTO> filteredResponse = new FilteredResponse<NameFilter, DataFieldsDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = dataFieldsRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = dataFieldsRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<DataFieldsDTO> itemsDTOList = DTOBase.fromEntitiesList(items, DataFieldsDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Data Field details
	 *
	 * @return Data Field Details
	 */
	public DataFields getDataFieldsForCurrentOrganization(Long itemId) {
		DataFields itemDetails;

		try {
			itemDetails = dataFieldsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Data Field not found in the database [{0}]", itemId));
		}

		// Verify Data Field and Organization
		if (!userService.isSuperAdmin() && itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Data Field [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Data Field DTO details
	 *
	 * @return Data Field Details
	 */
	public DataFieldsDTO getDetails(Long itemId) {

		DataFields itemDetails = getDataFieldsForCurrentOrganization(itemId);

		DataFieldsDTO result = new DataFieldsDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Data Field
	 *
	 * @return New Data Field
	 */
	public DataFieldsDTO create(DataFieldsDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		DataFields newItem = newItemDTO.toEntity();
		DataFields newItem = new DataFields();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		DataFields saveResult = dataFieldsRepository.save(newItem);

		DataFieldsDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.DATA_FIELD,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Data Field
	 *
	 * @return Updated Qualitative Fields
	 */
	public DataFieldsDTO update(DataFieldsDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		// Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		DataFields existingItem = getDataFieldsForCurrentOrganization(itemDTO.getId());
		DataFieldsDTO existingItemDTO = new DataFieldsDTO(existingItem);

		// Verify Data Field and Organization
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Data Field [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
		} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Data Field [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		DataFields saveResult = dataFieldsRepository.save(existingItem);

		DataFieldsDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.DATA_FIELD,
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
	private void applyEntityChanges(DataFieldsDTO itemDTO, DataFields entity) {
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
	}

	/**
	 * Deletes Data Field
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		DataFields existingItem = getDataFieldsForCurrentOrganization(itemId);
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Data Field [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getName()));
		}
		DataFieldsDTO existingItemDTO = new DataFieldsDTO(existingItem);
		dataFieldsRepository.delete(existingItem);
		dataFieldsRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.DATA_FIELD,
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
	private AuditLogItemId[] collectAuditLogItems(DataFieldsDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
