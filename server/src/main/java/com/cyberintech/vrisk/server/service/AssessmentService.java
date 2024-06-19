package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.AssessmentModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.AssessmentFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentEditDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.RelationToRequirementType;
import com.cyberintech.vrisk.server.model.jpa.domains.TaskLinkageType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Assessment Items management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class AssessmentService {

	@Autowired
	private AssessmentModelDAO assessmentModelDAO;

	@Autowired
	private AssessmentsRepository assessmentsRepository;

	@Autowired
	private AssessmentTypesRepository assessmentTypesRepository;

	@Autowired
	private AssessmentTypeService assessmentTypeService;

	@Autowired
	private AssessmentLevelsRepository assessmentLevelsRepository;

	@Autowired
	private TechnologyCategoryService technologyCategoryService;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private ProcessService processService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private SecurityRequirementService securityRequirementService;

	@Autowired
	private TaskRepository taskRepository;

	/**
	 * Get Assessment Items List
	 *
	 * @return Assessment Items List
	 */
	public FilteredResponse<AssessmentFilter, AssessmentViewDTO> getListFiltered(FilteredRequest<AssessmentFilter> filteredRequest) {

		PagedResult<AssessmentViewDTO> result = assessmentModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<AssessmentFilter, AssessmentViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Assessment Item details
	 *
	 * @return Assessment Item Details
	 */
	public Assessments getAssessmentForCurrentOrganization(Long itemId) {
		Assessments itemDetails;

		try {
			itemDetails = assessmentsRepository.findById(itemId).get();
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
	public AssessmentEditDTO getDetails(Long itemId) {

		Assessments itemDetails = getAssessmentForCurrentOrganization(itemId);

		return new AssessmentEditDTO(itemDetails);
	}


	/**
	 * Create new Assessment Item
	 *
	 * @return New Assessment Item
	 */
	public AssessmentEditDTO create(AssessmentEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		Assessments newItem = new Assessments();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		Assessments saveResult = assessmentsRepository.save(newItem);

		return getDetails(saveResult.getId());
	}

	/**
	 * Update Assessment Item
	 *
	 * @return Updated Assessment Item
	 */
	public AssessmentEditDTO update(AssessmentEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		Assessments existingItem = getAssessmentForCurrentOrganization(itemDTO.getId());

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		Assessments saveResult = assessmentsRepository.save(existingItem);

		return getDetails(saveResult.getId());
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(AssessmentEditDTO itemDTO, Assessments entity) {

		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		entity.setIsAllSelected(itemDTO.getIsAllSelected());
		entity.setRelationToRequirementType(itemDTO.getRelationToRequirementType());

		entity.setEstimatedStartDate(itemDTO.getEstimatedStartDate());
		entity.setEstimatedEndDate(itemDTO.getEstimatedEndDate());
		entity.setActualStartDate(itemDTO.getActualStartDate());
		entity.setActualEndDate(itemDTO.getActualEndDate());

		if (itemDTO.getAssessmentLevel() != null && itemDTO.getAssessmentLevel().getId() != null) {
			AssessmentLevels assessmentLevels = assessmentLevelsRepository.findById(itemDTO.getAssessmentLevel().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Assessment Level not found [{0}]", itemDTO.getAssessmentLevel().getId()), ApplicationExceptionCodes.ASSESSMENT_LEVEL_NOT_EXISTS));
			entity.setAssessmentLevel(assessmentLevels);
		}

		if (itemDTO.getAssessmentType() != null && itemDTO.getAssessmentType().getId() != null) {
			AssessmentTypes assessmentTypes = assessmentTypesRepository.findById(itemDTO.getAssessmentType().getId())
				.orElseThrow(() -> new BadRequestException(MessageFormat.format("Assessment Type not found [{0}]", itemDTO.getAssessmentType().getId()), ApplicationExceptionCodes.ASSESSMENT_TYPE_NOT_EXISTS));
			entity.setAssessmentType(assessmentTypes);
		}

		if (itemDTO.getLegalOrganization() != null && itemDTO.getLegalOrganization().getId() != null) {
			Organizations organization = organizationService.getOrganization(itemDTO.getLegalOrganization().getId());
			entity.setLegalOrganization(organization);
		}

		Optional.ofNullable(itemDTO.getTechnologyCategories()).ifPresent(technologyCategoryRefDTOList -> {
			entity.setTechnologyCategories(new HashSet<>());
			technologyCategoryRefDTOList.stream().forEach(technologyCategoryRefDTO -> {
				entity.getTechnologyCategories().add(technologyCategoryService.getTechnologyCategoryForCurrentOrganization(technologyCategoryRefDTO.getId()));
			});
		});

		Optional.ofNullable(itemDTO.getSystems()).ifPresent(systemRefDTOList -> {
			entity.setSystems(new HashSet<>());
			systemRefDTOList.stream().forEach(systemRefDTO -> {
				entity.getSystems().add(systemsService.getSystemForCurrentOrganization(systemRefDTO.getId()));
			});
		});

		Optional.ofNullable(itemDTO.getProcesses()).ifPresent(processRefDTOList -> {
			entity.setProcesses(new HashSet<>());
			processRefDTOList.stream().forEach(processRefDTO -> {
				entity.getProcesses().add(processService.getProcessForCurrentOrganization(processRefDTO.getId()));
			});
		});

		if (itemDTO.getRelationToRequirementType() == null || itemDTO.getRelationToRequirementType().equals(RelationToRequirementType.REQUIREMENTS)) {
			entity.setRelationToRequirementType(RelationToRequirementType.REQUIREMENTS);
			entity.setAssessmentTypes(new HashSet<>());

			Optional.ofNullable(itemDTO.getSecurityRequirements()).ifPresent(securityRequirementDTOList -> {
				entity.setSecurityRequirements(new HashSet<>());
				securityRequirementDTOList.stream().forEach(securityRequirementDTO -> {
					entity.getSecurityRequirements().add(securityRequirementService.getSecurityRequirementForCurrentOrganization(securityRequirementDTO.getId()));
				});
			});

		} else if (itemDTO.getRelationToRequirementType().equals(RelationToRequirementType.ALL_REQUIREMENTS)) {
			entity.setSecurityRequirements(new HashSet<>());
			entity.setAssessmentTypes(new HashSet<>());

		} else if (itemDTO.getRelationToRequirementType().equals(RelationToRequirementType.FRAMEWORKS)) {
			entity.setSecurityRequirements(new HashSet<>());

			Optional.ofNullable(itemDTO.getAssessmentTypes()).ifPresent(assessmentTypeRefDTOList -> {
				entity.setAssessmentTypes(new HashSet<>());
				assessmentTypeRefDTOList.stream().forEach(assessmentTypeRefDTO -> {
					entity.getAssessmentTypes().add(assessmentTypeService.getAssessmentTypeForCurrentOrganization(assessmentTypeRefDTO.getId()));
				});
			});
		}

		Optional.ofNullable(itemDTO.getTasks()).ifPresent(taskViewDTOList -> {
			entity.setTasks(new HashSet<>());
			taskViewDTOList.stream().forEach(taskViewDTO -> {
				Tasks task = taskRepository.findById(taskViewDTO.getId()).get();
				task.setLinkageType(TaskLinkageType.ASSESSMENT);
				// task saving after changing the linkage type may go here, but testing showed that nested entities saving works pretty well
				// so since we will save the whole Assessment there is no need to explicitly save each task
				entity.getTasks().add(task);
			});
		});

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

		Assessments existingItem = getAssessmentForCurrentOrganization(itemId);
		assessmentsRepository.delete(existingItem);
		assessmentsRepository.flush();

		return itemId;
	}

}
