package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.ControlTestModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.ControlTestFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.assessments.ControlTestEditDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.ControlTestViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Control Tests management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-08
 */
@Service
public class ControlTestService {

	@Autowired
	private ControlTestsRepository controlTestsRepository;

	@Autowired
	private AssessmentTypesRepository assessmentTypesRepository;

	@Autowired
	private ControlFunctionsRepository controlFunctionsRepository;

	@Autowired
	private ControlCategoriesRepository controlCategoriesRepository;

	@Autowired
	private ControlSubcategoriesRepository controlSubcategoriesRepository;

	@Autowired
	private AssessmentWeightsRepository assessmentWeightsRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private ControlTestModelDAO controlTestModelDAO;

	/**
	 * Get Control Tests List
	 *
	 * @return Control Tests List
	 */
	public FilteredResponse<ControlTestFilter, ControlTestViewDTO> getListFiltered(FilteredRequest<ControlTestFilter> filteredRequest) {

		PagedResult<ControlTestViewDTO> result = controlTestModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<ControlTestFilter, ControlTestViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Save list of Control Test Results
	 *
	 * @return New Control Tests
	 */
	public List<ControlTestEditDTO> saveItems(List<ControlTestEditDTO> itemsList) {
		List<ControlTestEditDTO> result = new ArrayList<>();
		Long organizationId = organizationService.getCurrentOrganizationId();

		for (ControlTestEditDTO item : itemsList) {
			if (item.getId() == null) {
				ControlTests entity = controlTestsRepository.getItemBySubcategoryIdAndOrganizationId(item.getControlSubcategory().getId(), organizationId);
				if (entity != null && entity.getId() != null) item.setId(entity.getId());
			}

			ControlTestEditDTO savedDTO;
			if (item.getId() != null) {
				savedDTO = update(item);
			} else {
				savedDTO = create(item);
			}
			result.add(savedDTO);
		}

		return result;
	}

	/**
	 * Get Control Test details
	 *
	 * @return Control Test Details
	 */
	public ControlTests getControlTestForCurrentOrganization(Long itemId) {
		ControlTests itemDetails;

		try {
			itemDetails = controlTestsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Control Test not found in the database [{0}]", itemId));
		}

		// Verify Control Test and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Control Test [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Control Test DTO details
	 *
	 * @return Control Test Details
	 */
	public ControlTestEditDTO getDetails(Long itemId) {

		ControlTests itemDetails = getControlTestForCurrentOrganization(itemId);

		ControlTestEditDTO result = new ControlTestEditDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Control Test Domain
	 *
	 * @return New Control Test
	 */
	public ControlTestEditDTO create(ControlTestEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		ControlTests newItem = newItemDTO.toEntity();
		ControlTests newItem = new ControlTests();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		ControlTests saveResult = controlTestsRepository.save(newItem);

		ControlTestEditDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Update Control Test
	 *
	 * @return Updated Control Test
	 */
	public ControlTestEditDTO update(ControlTestEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		ControlTests existingItem = getControlTestForCurrentOrganization(itemDTO.getId());

		// Verify Control Test and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Control Test [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		ControlTests saveResult = controlTestsRepository.save(existingItem);

		ControlTestEditDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(ControlTestEditDTO itemDTO, ControlTests entity) {

		// Set Assessment to NULL to fix Crash upon save
		entity.setAssessment(null);

		if (itemDTO.getAssessmentType() != null && itemDTO.getAssessmentType().getId() != null) {
			AssessmentTypes assessmentType = assessmentTypesRepository.findById(itemDTO.getAssessmentType().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Assessment type is not exist [{0}]", itemDTO.getAssessmentType().getId()), ApplicationExceptionCodes.ASSESSMENT_TYPE_NOT_EXISTS));
			entity.setAssessmentType(assessmentType);
		} else {
			entity.setAssessmentType(null);
		}

		if (itemDTO.getControlFunction() != null && itemDTO.getControlFunction().getId() != null) {
			ControlFunctions controlFunction = controlFunctionsRepository.findById(itemDTO.getControlFunction().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Control Function is not exist [{0}]", itemDTO.getControlFunction().getId()), ApplicationExceptionCodes.CONTROL_FUNCTION_NOT_EXISTS));
			entity.setControlFunction(controlFunction);
		} else {
			entity.setControlFunction(null);
		}

		if (itemDTO.getControlCategory() != null && itemDTO.getControlCategory().getId() != null) {
			ControlCategories controlCategory = controlCategoriesRepository.findById(itemDTO.getControlCategory().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Control Category is not exist [{0}]", itemDTO.getControlCategory().getId()), ApplicationExceptionCodes.CONTROL_FUNCTION_NOT_EXISTS));
			entity.setControlCategory(controlCategory);
		} else {
			entity.setControlCategory(null);
		}

		if (itemDTO.getControlSubcategory() != null && itemDTO.getControlSubcategory().getId() != null) {
			ControlSubcategories controlSubcategory = controlSubcategoriesRepository.findById(itemDTO.getControlSubcategory().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Control Subcategory is not exist [{0}]", itemDTO.getControlSubcategory().getId()), ApplicationExceptionCodes.CONTROL_FUNCTION_NOT_EXISTS));
			entity.setControlSubcategory(controlSubcategory);
		} else {
			entity.setControlSubcategory(null);
		}

		if (itemDTO.getAssessmentWeight() != null && itemDTO.getAssessmentWeight().getId() != null) {
			AssessmentWeights assessmentWeight = assessmentWeightsRepository.findById(itemDTO.getAssessmentWeight().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Assessment Weight is not exist [{0}]", itemDTO.getAssessmentWeight().getId()), ApplicationExceptionCodes.CONTROL_FUNCTION_NOT_EXISTS));
			entity.setAssessmentWeight(assessmentWeight);
		} else {
			entity.setAssessmentWeight(null);
		}

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Control Test
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		ControlTests existingItem = getControlTestForCurrentOrganization(itemId);
		controlTestsRepository.delete(existingItem);
		controlTestsRepository.flush();

		return itemId;
	}

}
