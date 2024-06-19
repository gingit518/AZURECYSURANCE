package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationEditDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.DataAssetClassification;
import com.cyberintech.vrisk.server.repository.jpa.DataAssetClassificationRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

/**
 * Data Asset Classifications management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class DataAssetClassificationService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private DataAssetClassificationRepository dataAssetClassificationRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Data Asset Classifications List
	 *
	 * @return Data Asset Classifications List
	 */
	public List<DataAssetClassificationViewDTO> getList() {
		List<DataAssetClassification> items = dataAssetClassificationRepository.findAll();

		List<DataAssetClassificationViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, DataAssetClassificationViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Data Asset Classifications List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, DataAssetClassificationViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<DataAssetClassification> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, DataAssetClassificationViewDTO> filteredResponse = new FilteredResponse<NameFilter, DataAssetClassificationViewDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = dataAssetClassificationRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = dataAssetClassificationRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<DataAssetClassificationViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, DataAssetClassificationViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Data Asset Classification details
	 *
	 * @return Data Asset Classification Details
	 */
	public DataAssetClassification getDataAssetClassificationForCurrentOrganization(Long itemId) {
		DataAssetClassification itemDetails;

		try {
			itemDetails = dataAssetClassificationRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Data Asset Classification not found in the database [{0}]", itemId));
		}

		// Verify Data Asset Classification and Organization
		if (itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Data Asset Classification [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Data Asset Classification DTO details
	 *
	 * @return Data Asset Classification Details
	 */
	public DataAssetClassificationEditDTO getDetails(Long itemId) {

		DataAssetClassification itemDetails = getDataAssetClassificationForCurrentOrganization(itemId);

		DataAssetClassificationEditDTO result = new DataAssetClassificationEditDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Data Asset Classification Domain
	 *
	 * @return New Data Asset Classification
	 */
	public DataAssetClassificationEditDTO create(DataAssetClassificationEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		DataAssetClassification newItem = newItemDTO.toEntity();
		DataAssetClassification newItem = new DataAssetClassification();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		DataAssetClassification saveResult = dataAssetClassificationRepository.save(newItem);

		DataAssetClassificationEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.ASSET_CLASS,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Data Asset Classification
	 *
	 * @return Updated Qualitative Domains
	 */
	public DataAssetClassificationEditDTO update(DataAssetClassificationEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		DataAssetClassification existingItem = getDataAssetClassificationForCurrentOrganization(itemDTO.getId());
		DataAssetClassificationEditDTO existingItemDTO = new DataAssetClassificationEditDTO(existingItem);

		// Verify Data Asset Classification and Organization
		if (!userService.isSuperAdmin()) {
			if (existingItem.getOrganizationId() == null) {
				throw new ForbiddenException(MessageFormat.format("Data Asset Classification [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
			} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
				throw new ForbiddenException(MessageFormat.format("Organization for Data Asset Classification [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
			}
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		DataAssetClassification saveResult = dataAssetClassificationRepository.save(existingItem);

		DataAssetClassificationEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.ASSET_CLASS,
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
	private void applyEntityChanges(DataAssetClassificationEditDTO itemDTO, DataAssetClassification entity) {
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		entity.setThreshold(itemDTO.getThreshold());
		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Data Asset Classification
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		DataAssetClassification existingItem = getDataAssetClassificationForCurrentOrganization(itemId);
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Data Asset Classification [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getName()));
		}
		DataAssetClassificationEditDTO existingItemDTO = new DataAssetClassificationEditDTO(existingItem);
		dataAssetClassificationRepository.delete(existingItem);
		dataAssetClassificationRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.ASSET_CLASS,
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
	private AuditLogItemId[] collectAuditLogItems(DataAssetClassificationEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
