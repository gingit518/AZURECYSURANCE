package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.organization.PackagePlansDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.PackagePlans;
import com.cyberintech.vrisk.server.repository.jpa.PackagePlansRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
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
 * Package Plans management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-07-22
 */
@Service
@Getter
public class PackagePlansService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private PackagePlansRepository packagePlansRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Package Plans List
	 *
	 * @return Package Plans List
	 */
	public List<PackagePlansDTO> getList() {
		List<PackagePlans> items = packagePlansRepository.findAll();

		List<PackagePlansDTO> itemDTOs = DTOBase.fromEntitiesList(items, PackagePlansDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Package Plans List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, PackagePlansDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<PackagePlans> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, PackagePlansDTO> filteredResponse = new FilteredResponse<NameFilter, PackagePlansDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = packagePlansRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = packagePlansRepository.getCountByName(namePattern);

		List<PackagePlansDTO> itemsDTOList = DTOBase.fromEntitiesList(items, PackagePlansDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Package Plan details
	 *
	 * @return Package Plan Details
	 */
	public PackagePlans getPackagePlansDetails(Long itemId) {
		PackagePlans itemDetails;

		try {
			itemDetails = packagePlansRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Package Plan not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Get Package Plan DTO details
	 *
	 * @return Package Plan Details
	 */
	public PackagePlansDTO getDetails(Long itemId) {

		PackagePlans itemDetails = getPackagePlansDetails(itemId);

		PackagePlansDTO result = new PackagePlansDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Package Plan
	 *
	 * @return New Package Plan
	 */
	public PackagePlansDTO create(PackagePlansDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		PackagePlans newItem = newItemDTO.toEntity();
		PackagePlans newItem = new PackagePlans();
		applyEntityChanges(newItemDTO, newItem);
		PackagePlans saveResult = packagePlansRepository.save(newItem);

		PackagePlansDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.PACKAGE_PLAN,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, null)
		);

		return result;
	}

	/**
	 * Update Package Plan
	 *
	 * @return Updated Qualitative Fields
	 */
	public PackagePlansDTO update(PackagePlansDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		// Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		PackagePlans existingItem = getPackagePlansDetails(itemDTO.getId());
		PackagePlansDTO existingItemDTO = new PackagePlansDTO(existingItem);

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		PackagePlans saveResult = packagePlansRepository.save(existingItem);

		PackagePlansDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.PACKAGE_PLAN,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, null)
		);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(PackagePlansDTO itemDTO, PackagePlans entity) {
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
	}

	/**
	 * Deletes Package Plan
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		PackagePlans existingItem = getPackagePlansDetails(itemId);
		PackagePlansDTO existingItemDTO = new PackagePlansDTO(existingItem);
		packagePlansRepository.delete(existingItem);
		packagePlansRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.PACKAGE_PLAN,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, null)
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
	private AuditLogItemId[] collectAuditLogItems(PackagePlansDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
