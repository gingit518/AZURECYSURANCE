package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskCategoryDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.TaskCategories;
import com.cyberintech.vrisk.server.repository.jpa.TaskCategoryRepository;
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
public class TaskCategoryService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private TaskCategoryRepository taskCategoryRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Task Categories List
	 *
	 * @return Task Categories List
	 */
	public List<TaskCategoryDTO> getList() {
		List<TaskCategories> items = taskCategoryRepository.findAll();

		List<TaskCategoryDTO> itemDTOs = DTOBase.fromEntitiesList(items, TaskCategoryDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Task Categories List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, TaskCategoryDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<TaskCategories> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, TaskCategoryDTO> filteredResponse = new FilteredResponse<NameFilter, TaskCategoryDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = taskCategoryRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = taskCategoryRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<TaskCategoryDTO> itemsDTOList = DTOBase.fromEntitiesList(items, TaskCategoryDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Task Category details
	 *
	 * @return Task Category Details
	 */
	public TaskCategories getItemForCurrentOrganization(Long itemId) {
		TaskCategories itemDetails;

		try {
			itemDetails = taskCategoryRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Task Category not found in the database [{0}]", itemId));
		}

		// Verify Task Category and Organization
		if (itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Task Category [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Task Category DTO details
	 *
	 * @return Task Category Details
	 */
	public TaskCategoryDTO getDetails(Long itemId) {

		TaskCategories itemDetails = getItemForCurrentOrganization(itemId);

		TaskCategoryDTO result = new TaskCategoryDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Task Category Domain
	 *
	 * @return New Task Category
	 */
	public TaskCategoryDTO create(TaskCategoryDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		TaskCategories newItem = new TaskCategories();
		newItem.setName(newItemDTO.getName());
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		TaskCategories saveResult = taskCategoryRepository.save(newItem);

		TaskCategoryDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.TASK_CATEGORY,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Task Category
	 *
	 * @return Updated Qualitative Domains
	 */
	public TaskCategoryDTO update(TaskCategoryDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		TaskCategories existingItem = getItemForCurrentOrganization(itemDTO.getId());
		TaskCategoryDTO existingItemDTO = new TaskCategoryDTO(existingItem);

		// Verify Task Category and Organization
		if (!isSuperAdmin) {
			if (existingItem.getOrganizationId() == null) {
				throw new ForbiddenException(MessageFormat.format("Task Category [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
			} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
				throw new ForbiddenException(MessageFormat.format("Organization for Task Category [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
			}
		}

		// Update item details
		existingItem.setName(itemDTO.getName());
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		TaskCategories saveResult = taskCategoryRepository.save(existingItem);

		TaskCategoryDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.TASK_CATEGORY,
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
	private void applyEntityChanges(TaskCategoryDTO itemDTO, TaskCategories entity) {
		// entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Task Category
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		TaskCategories existingItem = getItemForCurrentOrganization(itemId);
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Task Category [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getName()));
		}
		TaskCategoryDTO existingItemDTO = new TaskCategoryDTO(existingItem);
		taskCategoryRepository.delete(existingItem);
		taskCategoryRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.TASK_CATEGORY,
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
	private AuditLogItemId[] collectAuditLogItems(TaskCategoryDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
