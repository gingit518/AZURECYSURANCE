package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.OrganizationRequirementControlTestResultModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.ControlTestResultFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.OrganizationRequirementControlTestResultDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRequirementControlTestResultsRepository;
import com.cyberintech.vrisk.server.repository.jpa.TaskRepository;
import com.cyberintech.vrisk.server.rest.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;

@Service
public class OrganizationRequirementControlTestResultService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private OrganizationRequirementControlTestResultModelDAO organizationRequirementControlTestResultModelDAO;

	@Autowired
	private OrganizationRequirementControlTestResultsRepository organizationRequirementControlTestResultsRepository;

	@Autowired
	private SecurityRequirementService securityRequirementService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private ControlMaturityService controlMaturityService;

	/**
	 * Get Organization Requirement Control Test Result List
	 *
	 * @return items list
	 */
	public List<OrganizationRequirementControlTestResultDTO> getList() {
		List<OrganizationRequirementControlTestResults> items = organizationRequirementControlTestResultsRepository.findAll();

		List<OrganizationRequirementControlTestResultDTO> result = DTOBase.fromEntitiesList(items, OrganizationRequirementControlTestResultDTO.class);

		return result;
	}

	/**
	 * Get Organization Requirement Control Test Result list filtered
	 *
	 * @return items list
	 */
	public FilteredResponse<ControlTestResultFilter, OrganizationRequirementControlTestResultDTO> getListFiltered(FilteredRequest<ControlTestResultFilter> filteredRequest) {

		PagedResult<OrganizationRequirementControlTestResultDTO> result = organizationRequirementControlTestResultModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<ControlTestResultFilter, OrganizationRequirementControlTestResultDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Organization Requirement Control Test Result details
	 *
	 * @return Organization Requirement Control Test Result Details
	 */
	public OrganizationRequirementControlTestResults getItemForCurrentOrganization(Long itemId) {
		OrganizationRequirementControlTestResults itemDetails;

		try {
			itemDetails = organizationRequirementControlTestResultsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Organization Requirement Control Test Result not found in the database [{0}]", itemId));
		}

		// Verify Organization Requirement Control Test Result and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Organization Requirement Control Test Result [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Organization Requirement Control Test Result DTO details
	 *
	 * @return Organization Requirement Control Test Result Details
	 */
	public OrganizationRequirementControlTestResultDTO getDetails(Long itemId) {
		OrganizationRequirementControlTestResults itemDetails = getItemForCurrentOrganization(itemId);

		OrganizationRequirementControlTestResultDTO result = new OrganizationRequirementControlTestResultDTO(itemDetails);

		if (result.getDocument() != null) {
			String downloadUrl = documentService.buildDownloadUrl(result.getDocument());
			result.getDocument().setDownloadUrl(downloadUrl);
		}

		return result;
	}

	/**
	 * Create new Organization Requirement Control Test Result
	 *
	 * @return New Organization Requirement Control Test Result
	 */
	public OrganizationRequirementControlTestResultDTO create(OrganizationRequirementControlTestResultDTO newItemDTO) {
		// Throw exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		// Detect Assessment Weight
		Double assessmentWeight = newItemDTO.getControlMaturity() != null ? newItemDTO.getControlMaturity().getWeight() : newItemDTO.getAssessmentWeight();

//		OrganizationRequirementControlTestResults newItem = newItemDTO.toEntity();
		OrganizationRequirementControlTestResults newItem = new OrganizationRequirementControlTestResults();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setAssessmentWeight(assessmentWeight);
		newItem.setComments(newItemDTO.getComments());
		applyEntityChanges(newItemDTO, newItem);
		OrganizationRequirementControlTestResults saveResult = organizationRequirementControlTestResultsRepository.save(newItem);

		OrganizationRequirementControlTestResultDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event]
		auditLogService.create(
			VItemType.ORGANIZATION_REQUIREMENT_CONTROL_TEST_RESULT,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Get or Create if Not Exist System Requirement Control Test Result
	 *
	 * @return System Requirement Control Test Result
	 */
	public OrganizationRequirementControlTestResultDTO getOrCreateIfNotExist(OrganizationRequirementControlTestResultDTO itemDTO) {

		if (itemDTO.getId() != null) {
			return getDetails(itemDTO.getId());
		}

		Optional<OrganizationRequirementControlTestResults> itemFromDB;
		OrganizationRequirementControlTestResultDTO result;
		if (itemDTO.getSecurityRequirement() != null && itemDTO.getSecurityRequirement().getId() != null) {
			itemFromDB = organizationRequirementControlTestResultsRepository.findByOrganizationIdAndRequirementId(organizationService.getCurrentOrganizationId(), itemDTO.getSecurityRequirement().getId());
		} else {
			throw new BadRequestException("Security Requirement is required!", ApplicationExceptionCodes.SECURITY_REQUIREMENT_REQUIRED);
		}

		if (itemFromDB.isPresent()) {
			result = new OrganizationRequirementControlTestResultDTO(itemFromDB.get());
		} else {
			result = create(itemDTO);
		}

		return result;
	}

	/**
	 * Update Organization Requirement Control Test Result
	 *
	 * @return Updated Organization Requirement Control Test Result
	 */
	public OrganizationRequirementControlTestResultDTO update(OrganizationRequirementControlTestResultDTO itemDTO) {

		// Get Existing item from the database
		OrganizationRequirementControlTestResults existingItem = getItemForCurrentOrganization(itemDTO.getId());
		OrganizationRequirementControlTestResultDTO existingItemDTO = new OrganizationRequirementControlTestResultDTO(existingItem);

		// Verify Organization Requirement Control Test Result and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Organization Requirement Control Test Result [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Detect Assessment Weight
		Double assessmentWeight = itemDTO.getControlMaturity() != null ? itemDTO.getControlMaturity().getWeight() : itemDTO.getAssessmentWeight();

		// Update item details
		existingItem.setAssessmentWeight(assessmentWeight);
		existingItem.setComments(itemDTO.getComments());
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		OrganizationRequirementControlTestResults saveResult = organizationRequirementControlTestResultsRepository.save(existingItem);

		OrganizationRequirementControlTestResultDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.ORGANIZATION_REQUIREMENT_CONTROL_TEST_RESULT,
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
	private void applyEntityChanges(OrganizationRequirementControlTestResultDTO itemDTO, OrganizationRequirementControlTestResults entity) {

		if (itemDTO.getSecurityRequirement() != null && itemDTO.getSecurityRequirement().getId() != null) {
			SecurityRequirements securityRequirements = securityRequirementService.getSecurityRequirementForCurrentOrganization(itemDTO.getSecurityRequirement().getId());
			entity.setSecurityRequirement(securityRequirements);
		}

		if (itemDTO.getDocument() != null && itemDTO.getDocument().getId() != null) {
			Documents document = documentService.getItemForCurrentOrganization(itemDTO.getDocument().getId());
			entity.setDocument(document);
		} else {
			entity.setDocument(null);
		}

		if (itemDTO.getControlMaturity() != null && itemDTO.getControlMaturity().getId() != null) {
			ControlMaturities controlMaturity = controlMaturityService.getControlMaturityForCurrentOrganization(itemDTO.getControlMaturity().getId());
			entity.setControlMaturity(controlMaturity);
		} else {
			entity.setControlMaturity(null);
		}

		Optional.ofNullable(itemDTO.getTasks()).ifPresent(taskViewDTOList -> {
			entity.setTasks(new HashSet<>());
			taskViewDTOList.stream().forEach(taskViewDTO -> {
				entity.getTasks().add(taskRepository.findById(taskViewDTO.getId()).get());
			});
		});


	}

	/**
	 * Deletes Organization Requirement Control Test Result
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		OrganizationRequirementControlTestResults existingItem = getItemForCurrentOrganization(itemId);
		OrganizationRequirementControlTestResultDTO existingItemDTO = new OrganizationRequirementControlTestResultDTO(existingItem);
		organizationRequirementControlTestResultsRepository.delete(existingItem);
		organizationRequirementControlTestResultsRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.ORGANIZATION_REQUIREMENT_CONTROL_TEST_RESULT,
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
	private AuditLogItemId[] collectAuditLogItems(OrganizationRequirementControlTestResultDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		if (existingItemDTO.getSecurityRequirement() != null && existingItemDTO.getSecurityRequirement().getId() != null) {
			logItems.add(AuditLogItemId.of(VItemType.SECURITY_REQUIREMENT, existingItemDTO.getSecurityRequirement().getId()));
		}

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}
}
