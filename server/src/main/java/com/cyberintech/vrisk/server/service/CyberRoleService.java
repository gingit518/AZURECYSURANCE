package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.budget.CyberRoleDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.CyberRoles;
import com.cyberintech.vrisk.server.repository.jpa.CyberRoleRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Cyber Role management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-10
 */
@Service
public class CyberRoleService {

	@Autowired
	private CyberRoleRepository cyberRoleRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Cyber Role List
	 *
	 * @return Cyber Role List
	 */
	public List<CyberRoleDTO> getList() {
		List<CyberRoles> items = cyberRoleRepository.findAll();

		List<CyberRoleDTO> itemDTOs = DTOBase.fromEntitiesList(items, CyberRoleDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Cyber Role List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, CyberRoleDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<CyberRoles> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, CyberRoleDTO> filteredResponse = new FilteredResponse<NameFilter, CyberRoleDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = cyberRoleRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = cyberRoleRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<CyberRoleDTO> itemsDTOList = DTOBase.fromEntitiesList(items, CyberRoleDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Cyber Role details
	 *
	 * @return Cyber Role Details
	 */
	public CyberRoles getCyberRoleForCurrentOrganization(Long itemId) {
		CyberRoles itemDetails;

		try {
			itemDetails = cyberRoleRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Cyber Role not found in the database [{0}]", itemId));
		}

		// Verify Cyber Role and Organization
		if (itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Cyber Role [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Cyber Role DTO details
	 *
	 * @return Cyber Role Details
	 */
	public CyberRoleDTO getDetails(Long itemId) {

		CyberRoles itemDetails = getCyberRoleForCurrentOrganization(itemId);

		CyberRoleDTO result = new CyberRoleDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Cyber Role Domain
	 *
	 * @return New Cyber Role
	 */
	public CyberRoleDTO create(CyberRoleDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		CyberRoles newItem = newItemDTO.toEntity();
		CyberRoles newItem = new CyberRoles();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		CyberRoles saveResult = cyberRoleRepository.save(newItem);

		CyberRoleDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Update Cyber Role
	 *
	 * @return Updated Qualitative Domains
	 */
	public CyberRoleDTO update(CyberRoleDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		CyberRoles existingItem = getCyberRoleForCurrentOrganization(itemDTO.getId());

		// Verify Cyber Role and Organization
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Cyber Role [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
		} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Cyber Role [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		CyberRoles saveResult = cyberRoleRepository.save(existingItem);

		CyberRoleDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(CyberRoleDTO itemDTO, CyberRoles entity) {
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Cyber Role
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		CyberRoles existingItem = getCyberRoleForCurrentOrganization(itemId);
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Cyber Role [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getName()));
		}
		cyberRoleRepository.delete(existingItem);
		cyberRoleRepository.flush();

		return itemId;
	}

}
