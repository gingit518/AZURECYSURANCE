package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.*;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.control_function.ControlFunctionEditDTO;
import com.cyberintech.vrisk.server.model.dto.control_function.ControlFunctionViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentTypes;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlFunctions;
import com.cyberintech.vrisk.server.repository.jpa.ControlFunctionsRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

/**
 * Control Functions management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-08
 */
@Service
public class ControlFunctionService {

	@Autowired
	private AssessmentTypeService assessmentTypeService;

	@Autowired
	private ControlFunctionsRepository controlFunctionsRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Control Functions List
	 *
	 * @return Control Functions List
	 */
	public FilteredResponse<ByFrameworkFilter, ControlFunctionViewDTO> getListFiltered(FilteredRequest<ByFrameworkFilter> filteredRequest) {
		List<ControlFunctions> items = null;
		Long count = 0l;
		FilteredResponse<ByFrameworkFilter, ControlFunctionViewDTO> filteredResponse = new FilteredResponse<ByFrameworkFilter, ControlFunctionViewDTO>(filteredRequest);

		Long organizationId = organizationService.getCurrentOrganizationId();

		List<Long> excludeIds = Arrays.asList(0L);
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getExcludeIds() != null && filteredRequest.getFilter().getExcludeIds().size() > 0) {
			excludeIds = filteredRequest.getFilter().getExcludeIds();
		}

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long parentId = null;
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getParentId() != null) {
			parentId = filteredRequest.getFilter().getParentId();
		}

		if (parentId != null && parentId > 0) {
			items = controlFunctionsRepository.getListByNameAndParentForOrganization(namePattern, parentId, organizationId, excludeIds, filteredRequest.toPageRequest());
			count = controlFunctionsRepository.getCountByNameAndParentForOrganization(namePattern, parentId, organizationId, excludeIds);
		} else {
			items = controlFunctionsRepository.getListByNameForOrganization(namePattern, organizationId, excludeIds, filteredRequest.toPageRequest());
			count = controlFunctionsRepository.getCountByNameForOrganization(namePattern, organizationId, excludeIds);
		}

		List<ControlFunctionViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, ControlFunctionViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Control Function details
	 *
	 * @return Control Function Details
	 */
	public ControlFunctions getControlFunctionForCurrentOrganization(Long itemId) {
		ControlFunctions itemDetails;

		try {
			itemDetails = controlFunctionsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Control Function not found in the database [{0}]", itemId));
		}

		// Verify Control Function and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Control Function [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Control Function DTO details
	 *
	 * @return Control Function Details
	 */
	public ControlFunctionEditDTO getDetails(Long itemId) {

		ControlFunctions itemDetails = getControlFunctionForCurrentOrganization(itemId);

		ControlFunctionEditDTO result = new ControlFunctionEditDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Control Function
	 *
	 * @return New Control Function
	 */
	public ControlFunctionEditDTO create(ControlFunctionEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		ControlFunctions newItem = new ControlFunctions();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		ControlFunctions saveResult = controlFunctionsRepository.save(newItem);

		ControlFunctionEditDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Update Control Function
	 *
	 * @return Updated Control Function
	 */
	public ControlFunctionEditDTO update(ControlFunctionEditDTO itemDTO) {

		// Get Existing item from the database
		ControlFunctions existingItem = getControlFunctionForCurrentOrganization(itemDTO.getId());

		// Verify Control Function and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Control Function [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		existingItem.setName(itemDTO.getName());
		existingItem.setDescription(itemDTO.getDescription());
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		ControlFunctions saveResult = controlFunctionsRepository.save(existingItem);

		ControlFunctionEditDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(ControlFunctionEditDTO itemDTO, ControlFunctions entity) {

		// Apply new Values
		entity.setCode(itemDTO.getCode());
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());

		if (itemDTO.getAssessmentType() != null && itemDTO.getAssessmentType().getId() != null) {
			AssessmentTypes assessmentType = assessmentTypeService.getAssessmentTypeForCurrentOrganization(itemDTO.getAssessmentType().getId());
			entity.setAssessmentType(assessmentType);
		}

		// entity.setUpdatedBy(userService.getCurrentUserEntity());
		// entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Control Function
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		ControlFunctions existingItem = getControlFunctionForCurrentOrganization(itemId);
		controlFunctionsRepository.delete(existingItem);
		controlFunctionsRepository.flush();

		return itemId;
	}

}
