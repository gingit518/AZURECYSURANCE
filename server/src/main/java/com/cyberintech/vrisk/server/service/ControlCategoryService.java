package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.ControlCategoryModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.ByFrameworkFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.control_category.ControlCategoryEditDTO;
import com.cyberintech.vrisk.server.model.dto.control_category.ControlCategoryViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlCategories;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlFunctions;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlSubcategories;
import com.cyberintech.vrisk.server.repository.jpa.ControlCategoriesRepository;
import com.cyberintech.vrisk.server.repository.jpa.ControlFunctionsRepository;
import com.cyberintech.vrisk.server.repository.jpa.ControlSubcategoriesRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Control Categories management Service. Implements basic user CRUD.
 * [RENAMED to Control Tests]
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-08
 */
@Service
public class ControlCategoryService {

	@Autowired
	private ControlCategoriesRepository controlCategoriesRepository;

	@Autowired
	private ControlFunctionsRepository controlFunctionsRepository;

	@Autowired
	private ControlCategoryModelDAO controlCategoryModelDAO;

	@Autowired
	private ControlSubcategoriesRepository controlSubcategoriesRepository;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Control Categories List
	 *
	 * @return Control Categories List
	 */
	public FilteredResponse<ByFrameworkFilter, ControlCategoryViewDTO> getListFiltered(FilteredRequest<ByFrameworkFilter> filteredRequest) {

		PagedResult<ControlCategoryViewDTO> pagedResult = controlCategoryModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<ByFrameworkFilter, ControlCategoryViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, pagedResult);

		return filteredResponse;
	}

	/**
	 * Get Control Category details
	 *
	 * @return Control Category Details
	 */
	public ControlCategories getControlCategoryForCurrentOrganization(Long itemId) {
		ControlCategories itemDetails;

		try {
			itemDetails = controlCategoriesRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Control Category not found in the database [{0}]", itemId));
		}

		// Verify Control Category and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Control Category [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Control Category DTO details
	 *
	 * @return Control Category Details
	 */
	public ControlCategoryEditDTO getDetails(Long itemId) {

		ControlCategories itemDetails = getControlCategoryForCurrentOrganization(itemId);

		ControlCategoryEditDTO result = new ControlCategoryEditDTO(itemDetails);

		return result;
	}

	/**
	 * Create new Control Category
	 *
	 * @return New Control Category
	 */
	public ControlCategoryEditDTO create(ControlCategoryEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		ControlCategories newItem = new ControlCategories();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		// newItem.setCreatedBy(userService.getCurrentUserEntity());
		// newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		ControlCategories saveResult = controlCategoriesRepository.save(newItem);

		ControlCategoryEditDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Update Control Category
	 *
	 * @return Updated Control Category
	 */
	public ControlCategoryEditDTO update(ControlCategoryEditDTO itemDTO) {

		// Get Existing item from the database
		ControlCategories existingItem = getControlCategoryForCurrentOrganization(itemDTO.getId());

		// Verify Control Category and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Control Category [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		ControlCategories saveResult = controlCategoriesRepository.save(existingItem);

		ControlCategoryEditDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(ControlCategoryEditDTO itemDTO, ControlCategories entity) {

		// Apply new Values
		entity.setCode(itemDTO.getCode());
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());

		if (itemDTO.getControlFunction() != null && itemDTO.getControlFunction().getId() != null) {
			ControlFunctions controlFunction = controlFunctionsRepository.findById(itemDTO.getControlFunction().getId()).get();
			entity.setControlFunction(controlFunction);

			// Set Assessment Type cache
			entity.setAssessmentType(controlFunction.getAssessmentType());
		}

		// entity.setUpdatedBy(userService.getCurrentUserEntity());
		// entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Control Category
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		ControlCategories existingItem = getControlCategoryForCurrentOrganization(itemId);
		List<ControlSubcategories> subcategories = controlSubcategoriesRepository.findAllByControlCategory(existingItem);
		for (ControlSubcategories subcategory : subcategories) {
			controlSubcategoriesRepository.delete(subcategory);
		}
		controlCategoriesRepository.delete(existingItem);
		controlCategoriesRepository.flush();

		return itemId;
	}

}
