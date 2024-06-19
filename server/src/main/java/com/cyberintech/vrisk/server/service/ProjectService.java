package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.tasks.ProjectDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.Projects;
import com.cyberintech.vrisk.server.repository.jpa.ProjectRepository;
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
 * Projects management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class ProjectService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Projects List
	 *
	 * @return Projects List
	 */
	public List<ProjectDTO> getList() {
		List<Projects> items = projectRepository.findAll();

		List<ProjectDTO> itemDTOs = DTOBase.fromEntitiesList(items, ProjectDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Projects List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, ProjectDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<Projects> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, ProjectDTO> filteredResponse = new FilteredResponse<NameFilter, ProjectDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = projectRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = projectRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<ProjectDTO> itemsDTOList = DTOBase.fromEntitiesList(items, ProjectDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Project details
	 *
	 * @return Project Details
	 */
	public Projects getItemForCurrentOrganization(Long itemId) {
		Projects itemDetails;

		try {
			itemDetails = projectRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Project not found in the database [{0}]", itemId));
		}

		// Verify Project and Organization
		if (itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Project [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Project DTO details
	 *
	 * @return Project Details
	 */
	public ProjectDTO getDetails(Long itemId) {

		Projects itemDetails = getItemForCurrentOrganization(itemId);

		ProjectDTO result = new ProjectDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Project Domain
	 *
	 * @return New Project
	 */
	public ProjectDTO create(ProjectDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		Projects newItem = new Projects();
		newItem.setName(newItemDTO.getName());
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		Projects saveResult = projectRepository.save(newItem);

		ProjectDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.PROJECT,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Project
	 *
	 * @return Updated Qualitative Domains
	 */
	public ProjectDTO update(ProjectDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		Projects existingItem = getItemForCurrentOrganization(itemDTO.getId());
		ProjectDTO existingItemDTO = new ProjectDTO(existingItem);

		// Verify Project and Organization
		if (!isSuperAdmin) {
			if (existingItem.getOrganizationId() == null) {
				throw new ForbiddenException(MessageFormat.format("Project [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
			} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
				throw new ForbiddenException(MessageFormat.format("Organization for Project [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
			}
		}

		// Update item details
		existingItem.setName(itemDTO.getName());
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		Projects saveResult = projectRepository.save(existingItem);

		ProjectDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.PROJECT,
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
	private void applyEntityChanges(ProjectDTO itemDTO, Projects entity) {
		// entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Project
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Projects existingItem = getItemForCurrentOrganization(itemId);
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Project [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getName()));
		}
		ProjectDTO existingItemDTO = new ProjectDTO(existingItem);
		projectRepository.delete(existingItem);
		projectRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.PROJECT,
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
	private AuditLogItemId[] collectAuditLogItems(ProjectDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
