package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.datadomains.DataDomainsDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.DataDomains;
import com.cyberintech.vrisk.server.repository.jpa.DataDomainsRepository;
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
 * Data Domains management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-16
 */
@Service
@Getter
public class DataDomainsService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private DataDomainsRepository dataDomainsRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Data Domains List
	 *
	 * @return Data Domains List
	 */
	public List<DataDomainsDTO> getList() {
		List<DataDomains> items = dataDomainsRepository.findAll();

		List<DataDomainsDTO> itemDTOs = DTOBase.fromEntitiesList(items, DataDomainsDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Data Domains List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, DataDomainsDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<DataDomains> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, DataDomainsDTO> filteredResponse = new FilteredResponse<NameFilter, DataDomainsDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = dataDomainsRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = dataDomainsRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<DataDomainsDTO> itemsDTOList = DTOBase.fromEntitiesList(items, DataDomainsDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Data Domain details
	 *
	 * @return Data Domain Details
	 */
	public DataDomains getDataDomainsForCurrentOrganization(Long itemId) {
		DataDomains itemDetails;

		try {
			itemDetails = dataDomainsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Data Domain not found in the database [{0}]", itemId));
		}

		// Verify Data Domain and Organization
		if (!userService.isSuperAdmin() && itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Data Domain [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Data Domain DTO details
	 *
	 * @return Data Domain Details
	 */
	public DataDomainsDTO getDetails(Long itemId) {

		DataDomains itemDetails = getDataDomainsForCurrentOrganization(itemId);

		DataDomainsDTO result = new DataDomainsDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Data Domain Domain
	 *
	 * @return New Data Domain
	 */
	public DataDomainsDTO create(DataDomainsDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		DataDomains newItem = newItemDTO.toEntity();
		DataDomains newItem = new DataDomains();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		DataDomains saveResult = dataDomainsRepository.save(newItem);

		DataDomainsDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.DATA_DOMAIN,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Data Domain
	 *
	 * @return Updated Qualitative Domains
	 */
	public DataDomainsDTO update(DataDomainsDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		// Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		DataDomains existingItem = getDataDomainsForCurrentOrganization(itemDTO.getId());
		DataDomainsDTO existingItemDTO = new DataDomainsDTO(existingItem);

		// Verify Data Domain and Organization
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Data Domain [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
		} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Data Domain [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		DataDomains saveResult = dataDomainsRepository.save(existingItem);

		DataDomainsDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.DATA_DOMAIN,
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
	private void applyEntityChanges(DataDomainsDTO itemDTO, DataDomains entity) {
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
	}

	/**
	 * Deletes Data Domain
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		DataDomains existingItem = getDataDomainsForCurrentOrganization(itemId);
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Data Domain [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getName()));
		}
		DataDomainsDTO existingItemDTO = new DataDomainsDTO(existingItem);
		dataDomainsRepository.delete(existingItem);
		dataDomainsRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.DATA_DOMAIN,
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
	private AuditLogItemId[] collectAuditLogItems(DataDomainsDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
