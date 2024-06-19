package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentFindingEditDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentFindingViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.TaskLinkageType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

/**
 * Assessment Findings management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-14
 */
@Service
public class AssessmentFindingService {

	@Autowired
	private AssessmentFindingsRepository assessmentFindingsRepository;

	@Autowired
	private ControlSubcategoriesRepository controlSubcategoriesRepository;

	@Autowired
	private TechnologyCategoryRepository technologyCategoryRepository;

	@Autowired
	private TechnologyRepository technologyRepository;

	@Autowired
	private AssessmentService assessmentService;

	@Autowired
	private SecurityRequirementService securityRequirementService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private ControlMaturityService controlMaturityService;

	/**
	 * Get Assessment Items List
	 *
	 * @return Assessment Items List
	 */
	public FilteredResponse<NameFilter, AssessmentFindingViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<AssessmentFindings> items;
		Long count;
		FilteredResponse<NameFilter, AssessmentFindingViewDTO> filteredResponse = new FilteredResponse<NameFilter, AssessmentFindingViewDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = assessmentFindingsRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = assessmentFindingsRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<AssessmentFindingViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, AssessmentFindingViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Assessment Item details
	 *
	 * @return Assessment Item Details
	 */
	public AssessmentFindings getAssessmentForCurrentOrganization(Long itemId) {
		AssessmentFindings itemDetails;

		try {
			itemDetails = assessmentFindingsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Assessment Item not found in the database [{0}]", itemId), ApplicationExceptionCodes.ASSESSMENT_NOT_EXISTS);
		}

		// Verify Assessment Item and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Assessment Item [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Assessment Item DTO details
	 *
	 * @return Assessment Item Details
	 */
	public AssessmentFindingEditDTO getDetails(Long itemId) {

		AssessmentFindings itemDetails = getAssessmentForCurrentOrganization(itemId);

		return new AssessmentFindingEditDTO(itemDetails);
	}


	/**
	 * Create new Assessment Item Domain
	 *
	 * @return New Assessment Item
	 */
	public AssessmentFindingEditDTO create(AssessmentFindingEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		AssessmentFindings newItem = new AssessmentFindings();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		AssessmentFindings saveResult = assessmentFindingsRepository.save(newItem);

		return getDetails(saveResult.getId());
	}

	/**
	 * Update Assessment Item
	 *
	 * @return Updated Assessment Item
	 */
	public AssessmentFindingEditDTO update(AssessmentFindingEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		AssessmentFindings existingItem = getAssessmentForCurrentOrganization(itemDTO.getId());

		// Verify Assessment Item and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Assessment Item [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		AssessmentFindings saveResult = assessmentFindingsRepository.save(existingItem);

		return getDetails(saveResult.getId());
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(AssessmentFindingEditDTO itemDTO, AssessmentFindings entity) {

		entity.setName(itemDTO.getName());
		entity.setValue(itemDTO.getValue());
		entity.setPercentage(itemDTO.getPercentage());
		entity.setLinkType(itemDTO.getLinkType());
		entity.setIsGDPR(itemDTO.getIsGDPR());
		entity.setSubjectiveRiskLevel(itemDTO.getSubjectiveRiskLevel());

		if (itemDTO.getTechnologyCategory() != null && itemDTO.getTechnologyCategory().getId() != null) {
			TechnologyCategories technologyCategories = technologyCategoryRepository.findById(itemDTO.getTechnologyCategory().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Technology Category not found [{0}]", itemDTO.getTechnologyCategory().getId()), ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_NOT_EXISTS));
			entity.setTechnologyCategory(technologyCategories);
		}

		if (itemDTO.getTechnology() != null && itemDTO.getTechnology().getId() != null) {
			Technologies technologies = technologyRepository.findById(itemDTO.getTechnology().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Technology not found [{0}]", itemDTO.getTechnology().getId()), ApplicationExceptionCodes.TECHNOLOGY_NOT_EXISTS));
			entity.setTechnology(technologies);
		} else {
			entity.setTechnology(null);
		}

		if (itemDTO.getControlSubcategory() != null && itemDTO.getControlSubcategory().getId() != null) {
			ControlSubcategories subcategories = controlSubcategoriesRepository.findById(itemDTO.getControlSubcategory().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Control subcategory not found [{0}]", itemDTO.getControlSubcategory().getId()), ApplicationExceptionCodes.CONTROL_SUBCATEGORY_NOT_EXISTS));
			entity.setControlSubcategory(subcategories);
		}

		Optional.ofNullable(itemDTO.getAssessments()).ifPresent(assessmentViewDTOList -> {
			entity.setAssessments(new HashSet<>());
			assessmentViewDTOList.stream().forEach(assessmentViewDTO -> {
				entity.getAssessments().add(assessmentService.getAssessmentForCurrentOrganization(assessmentViewDTO.getId()));
			});

		});

		Optional.ofNullable(itemDTO.getSecurityRequirements()).ifPresent(securityRequirementDTOList -> {
			entity.setSecurityRequirements(new HashSet<>());
			securityRequirementDTOList.stream().forEach(securityRequirementDTO -> {
				entity.getSecurityRequirements().add(securityRequirementService.getSecurityRequirementForCurrentOrganization(securityRequirementDTO.getId()));
			});
		});

		Optional.ofNullable(itemDTO.getTasks()).ifPresent(taskViewDTOList -> {
			entity.setTasks(new HashSet<>());
			taskViewDTOList.stream().forEach(taskViewDTO -> {
				Tasks task = taskRepository.findById(taskViewDTO.getId()).get();
				task.setLinkageType(TaskLinkageType.FINDING);
				// task saving after changing the linkage type may go here, but testing showed that nested entities saving works pretty well
				// so since we will save the whole Assessment Finding there is no need to explicitly save each task
				entity.getTasks().add(task);
			});
		});

		if (itemDTO.getControlMaturity() != null && itemDTO.getControlMaturity().getId() != null) {
			ControlMaturities controlMaturity = controlMaturityService.getControlMaturityForCurrentOrganization(itemDTO.getControlMaturity().getId());
			entity.setControlMaturity(controlMaturity);
		} else {
			entity.setControlMaturity(null);
		}

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Assessment Item
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		AssessmentFindings existingItem = getAssessmentForCurrentOrganization(itemId);
		assessmentFindingsRepository.delete(existingItem);
		assessmentFindingsRepository.flush();

		return itemId;
	}

}
