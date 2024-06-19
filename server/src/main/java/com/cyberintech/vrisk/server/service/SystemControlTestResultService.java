package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.SystemControlTestResultModelDAO;
import com.cyberintech.vrisk.server.model.data.ControlTestResultFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.SystemControlTestResultEditDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.SystemControlTestResultViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemControlTestResults;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemRequirementControlTestResults;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.SystemControlTestResultsRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemRequirementControlTestResultsRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;

/**
 * System Control Test Result management Service. Implements basic entity CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.01.31
 */
@Service
public class SystemControlTestResultService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private SystemControlTestResultModelDAO systemControlTestResultModelDAO;

	@Autowired
	private SystemControlTestResultsRepository systemControlTestResultsRepository;

	@Lazy
	@Autowired
	private SystemRequirementControlTestResultService systemRequirementControlTestResultService;

	@Autowired
	private SystemRequirementControlTestResultsRepository systemRequirementControlTestResultsRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private SystemsService systemService;

	/**
	 * Get System Control Test Result List
	 *
	 * @return items list
	 */
	public List<SystemControlTestResultViewDTO> getList() {
		List<SystemControlTestResults> items = systemControlTestResultsRepository.findAll();

		List<SystemControlTestResultViewDTO> result = DTOBase.fromEntitiesList(items, SystemControlTestResultViewDTO.class);

		return result;
	}

	/**
	 * Get System Requirement Control Test Result list filtered
	 *
	 * @return items list
	 */
	public FilteredResponse<ControlTestResultFilter, SystemControlTestResultViewDTO> getListFiltered(FilteredRequest<ControlTestResultFilter> filteredRequest) {

		PagedResult<SystemControlTestResultViewDTO> result = systemControlTestResultModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<ControlTestResultFilter, SystemControlTestResultViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get System Control Test Result details
	 *
	 * @return System Control Test Result Details
	 */
	public SystemControlTestResults getItemForCurrentOrganization(Long itemId) {
		SystemControlTestResults itemDetails;

		try {
			itemDetails = systemControlTestResultsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("System Control Test Result not found in the database [{0}]", itemId));
		}

		// Verify System Control Test Result and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System Control Test Result [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Or Create if not exist System Control Test Result
	 *
	 * @return System Control Test Result Details
	 */
	public SystemControlTestResults getOrCreateItemForCurrentOrganizationAndSystem(Long systemId) {
		SystemControlTestResults itemDetails;
		Optional<SystemControlTestResults> itemFromDB = systemControlTestResultsRepository.findBySystemId(systemId);

		if (itemFromDB.isPresent()) {
			itemDetails = itemFromDB.get();
		} else {
			Systems system = systemService.getSystemForCurrentOrganization(systemId);
			SystemControlTestResultEditDTO newItemDTO = new SystemControlTestResultEditDTO();
			newItemDTO.setSystem(new SystemRefDTO(system));

			SystemControlTestResultEditDTO saveResultDTO = create(newItemDTO);

			itemDetails = getItemForCurrentOrganization(saveResultDTO.getId());
		}

		// Verify System Control Test Result and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System Control Test Result [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get System Control Test Result DTO details
	 *
	 * @return System Control Test Result Details
	 */
	public SystemControlTestResultEditDTO getDetails(Long itemId) {
		SystemControlTestResults itemDetails = getItemForCurrentOrganization(itemId);

		SystemControlTestResultEditDTO result = new SystemControlTestResultEditDTO(itemDetails);

		return result;
	}

	/**
	 * Create new System Control Test Result
	 *
	 * @return New System Control Test Result
	 */
	public SystemControlTestResultEditDTO create(SystemControlTestResultEditDTO newItemDTO) {
		// Throw exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		SystemControlTestResults newItem = newItemDTO.toEntity();
		SystemControlTestResults newItem = new SystemControlTestResults();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setAssessmentWeight(newItemDTO.getAssessmentWeight());
		applyEntityChanges(newItemDTO, newItem);
		SystemControlTestResults saveResult = systemControlTestResultsRepository.save(newItem);

		SystemControlTestResultEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.SYSTEM_CONTROL_TEST_RESULT,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update System Control Test Result
	 *
	 * @return Updated System Control Test Result
	 */
	public SystemControlTestResultEditDTO update(SystemControlTestResultEditDTO itemDTO) {

		// Get Existing item from the database
		SystemControlTestResults existingItem = getItemForCurrentOrganization(itemDTO.getId());
		SystemControlTestResultEditDTO existingItemDTO = new SystemControlTestResultEditDTO(existingItem);

		// Verify System Control Test Result and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System Control Test Result [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item Details
		existingItem.setAssessmentWeight(itemDTO.getAssessmentWeight());
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		SystemControlTestResults saveResult = systemControlTestResultsRepository.save(existingItem);

		SystemControlTestResultEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.SYSTEM_CONTROL_TEST_RESULT,
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
	private void applyEntityChanges(SystemControlTestResultEditDTO itemDTO, SystemControlTestResults entity) {

		if (itemDTO.getSystem() != null && itemDTO.getSystem().getId() != null) {
			Systems systems = systemService.getSystemForCurrentOrganization(itemDTO.getSystem().getId());
			entity.setSystem(systems);
		}

		Optional.ofNullable(itemDTO.getSystemRequirementControlTestResults()).ifPresent(systemRequirementControlTestResultDTOList -> {
			entity.setSystemRequirementControlTestResults(new HashSet<>());
			systemRequirementControlTestResultDTOList.stream().forEach(systemRequirementControlTestResultDTO -> {
				entity.getSystemRequirementControlTestResults().add(systemRequirementControlTestResultService.getItemForCurrentOrganization(systemRequirementControlTestResultDTO.getId()));
			});
		});
	}

	/**
	 * Deletes System Control Test Result
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		SystemControlTestResults existingItem = getItemForCurrentOrganization(itemId);
		SystemControlTestResultEditDTO existingItemDTO = new SystemControlTestResultEditDTO(existingItem);
		systemControlTestResultsRepository.delete(existingItem);
		systemControlTestResultsRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.SYSTEM_CONTROL_TEST_RESULT,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, existingItem.getOrganizationId())
		);

		return itemId;
	}

	public void recalculateAssessmentWeight(Long itemId) {
		SystemControlTestResults item = getItemForCurrentOrganization(itemId);
		Double assessmentWeightsSum = 0D;

		if (item.getSystemRequirementControlTestResults() != null && item.getSystemRequirementControlTestResults().size() > 0) {
			for (SystemRequirementControlTestResults systemRequirementControlTestResults : item.getSystemRequirementControlTestResults()) {
				assessmentWeightsSum += systemRequirementControlTestResults.getAssessmentWeight() != null ? systemRequirementControlTestResults.getAssessmentWeight(): 0;
			}
			item.setAssessmentWeight(assessmentWeightsSum / item.getSystemRequirementControlTestResults().size());

		} else {
			item.setAssessmentWeight(0D);
		}

		systemControlTestResultsRepository.save(item);
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(SystemControlTestResultEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		if (existingItemDTO.getSystem() != null && existingItemDTO.getSystem().getId() != null) {
			logItems.add(AuditLogItemId.of(VItemType.SYSTEM, existingItemDTO.getSystem().getId()));
		}
		Optional.ofNullable(existingItemDTO.getSystemRequirementControlTestResults()).ifPresent(systemRequirementControlTestResultDTOList -> {
			systemRequirementControlTestResultDTOList.stream().forEach(systemRequirementControlTestResultDTO -> {
				logItems.add(AuditLogItemId.of(VItemType.SYSTEM_REQUIREMENT_CONTROL_TEST_RESULT, systemRequirementControlTestResultDTO.getId()));
			});
		});

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}
}
