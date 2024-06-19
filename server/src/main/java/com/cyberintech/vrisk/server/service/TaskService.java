package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.TasksModelDAO;
import com.cyberintech.vrisk.server.model.data.*;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskBudgetViewDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskEditDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.TaskRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Task Categories management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class TaskService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private TaskCategoryService taskCategoryService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private UserMessageService userMessageService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private TasksModelDAO tasksModelDAO;

	/**
	 * Get Task Categories List
	 *
	 * @return Task Categories List
	 */
	public List<TaskViewDTO> getList() {
		List<Tasks> items = taskRepository.findAll();

		List<TaskViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, TaskViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Task Categories List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, TaskBudgetViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<Tasks> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, TaskBudgetViewDTO> filteredResponse = new FilteredResponse<NameFilter, TaskBudgetViewDTO>(filteredRequest);

		String namePattern = "";
		List<Long> excludeIds = Arrays.asList(0L);
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		if (filteredRequest.getFilter().getExcludeIds() != null && filteredRequest.getFilter().getExcludeIds().size() > 0) {
			excludeIds = filteredRequest.getFilter().getExcludeIds();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = taskRepository.getListByOrganizationAndName(organizationId, namePattern, excludeIds, filteredRequest.toPageRequest());
		count = taskRepository.getCountByOrganizationAndName(organizationId, namePattern, excludeIds);

		List<TaskBudgetViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, TaskBudgetViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Task List
	 *
	 * @return Tasks List
	 */
	public FilteredResponse<TasksFilter, TaskBudgetViewDTO> getTasksListFiltered(FilteredRequest<TasksFilter> filteredRequest) {
		PagedResult<TaskBudgetViewDTO> result = tasksModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<TasksFilter, TaskBudgetViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Task details
	 *
	 * @return Task Details
	 */
	public Tasks getItemForCurrentOrganization(Long itemId) {
		Tasks itemDetails;

		try {
			itemDetails = taskRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Task not found in the database [{0}]", itemId));
		}

		// Verify Task and Organization
		if (itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Task [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Task DTO details
	 *
	 * @return Task Details
	 */
	public TaskEditDTO getDetails(Long itemId) {

		Tasks itemDetails = getItemForCurrentOrganization(itemId);

		TaskEditDTO result = new TaskEditDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Task Domain
	 *
	 * @return New Task
	 */
	public TaskEditDTO create(TaskEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		Tasks newItem = new Tasks();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		Tasks saveResult = taskRepository.save(newItem);

		TaskEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.TASK,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		if (newItemDTO.getTaskAssignee() != null) {
			userMessageService.sendMessage(
				newItemDTO.getTaskAssignee().getId(),
				"You are assigned to the task",
				String.format("You are assigned to the task: %s", result.getName())
			);
		}

		if (newItemDTO.getTaskManager() != null) {
			userMessageService.sendMessage(
				newItemDTO.getTaskManager().getId(),
				"You are assigned as a manager to the task",
				String.format("You are assigned as a manager to the task: %s", result.getName())
			);
		}

		return result;
	}

	/**
	 * Update Task
	 *
	 * @return Updated Qualitative Domains
	 */
	public TaskEditDTO update(TaskEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		Tasks existingItem = getItemForCurrentOrganization(itemDTO.getId());
		TaskEditDTO existingItemDTO = new TaskEditDTO(existingItem);

		// Verify Task and Organization
		if (!isSuperAdmin) {
			if (existingItem.getOrganizationId() == null) {
				throw new ForbiddenException(MessageFormat.format("Task [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getId()));
			} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
				throw new ForbiddenException(MessageFormat.format("Organization for Task [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
			}
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		Tasks saveResult = taskRepository.save(existingItem);

		TaskEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.TASK,
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
	private void applyEntityChanges(TaskEditDTO itemDTO, Tasks entity) {
		// entity.setUpdatedAt(new Date());
		entity.setName(itemDTO.getName());
		entity.setTaskNotes(itemDTO.getTaskNotes());
		entity.setPriority(itemDTO.getPriority());
		entity.setStatus(itemDTO.getStatus());
		entity.setEstimatedHours(itemDTO.getEstimatedHours());
		entity.setActualHours(itemDTO.getActualHours());
		entity.setEstimatedStartDate(itemDTO.getEstimatedStartDate());
		entity.setEstimatedEndDate(itemDTO.getEstimatedEndDate());
		entity.setActualStartDate(itemDTO.getActualStartDate());
		entity.setActualEndDate(itemDTO.getActualEndDate());

		if (itemDTO.getBusinessUnit() != null && itemDTO.getBusinessUnit().getId() != null) {
			BusinessUnits businessUnit = businessUnitService.getBusinessUnitForCurrentOrganization(itemDTO.getBusinessUnit().getId());
			entity.setBusinessUnit(businessUnit);
		}

		if (itemDTO.getProject() != null && itemDTO.getProject().getId() != null) {
			Projects project = projectService.getItemForCurrentOrganization(itemDTO.getProject().getId());
			entity.setProject(project);
		}

		if (itemDTO.getTaskCategory() != null && itemDTO.getTaskCategory().getId() != null) {
			TaskCategories taskCategory = taskCategoryService.getItemForCurrentOrganization(itemDTO.getTaskCategory().getId());
			entity.setTaskCategory(taskCategory);
		}

		if (itemDTO.getTaskManager() != null && itemDTO.getTaskManager().getId() != null) {
			Users taskManager = userService.getUser(itemDTO.getTaskManager().getId());
			entity.setTaskManager(taskManager);
		}

		if (itemDTO.getTaskAssignee() != null && itemDTO.getTaskAssignee().getId() != null) {
			Users taskAssignee = userService.getUser(itemDTO.getTaskAssignee().getId());
			entity.setTaskAssignee(taskAssignee);
		}
	}

	/**
	 * Deletes Task
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Tasks existingItem = getItemForCurrentOrganization(itemId);
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Task [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getId()));
		}
		TaskEditDTO existingItemDTO = new TaskEditDTO(existingItem);
		taskRepository.delete(existingItem);
		taskRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.TASK,
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
	private AuditLogItemId[] collectAuditLogItems(TaskEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
