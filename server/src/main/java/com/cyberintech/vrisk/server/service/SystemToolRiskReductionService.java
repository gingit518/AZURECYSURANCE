package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.systems.SystemToolRiskReductionDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemToolRiskReductions;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.SystemRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemToolRiskReductionRepository;
import com.cyberintech.vrisk.server.repository.results.SystemToolRiskReductionResult;
import com.cyberintech.vrisk.server.rest.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * System Tool Risk Reduction Repository management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-26
 */
@Service
public class SystemToolRiskReductionService {

	@Autowired
	private SystemToolRiskReductionRepository systemToolRiskReductionRepository;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private UserService userService;

	/**
	 * Get System Tool Risk Reductions List
	 *
	 * @return System Tool Risk Reductions List
	 */
	public FilteredResponse<NameFilter, SystemToolRiskReductionDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		// List<SystemToolRiskReductions> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, SystemToolRiskReductionDTO> filteredResponse = new FilteredResponse<NameFilter, SystemToolRiskReductionDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		// items = systemToolRiskReductionRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		// count = systemToolRiskReductionRepository.getCountByOrganizationAndName(organizationId, namePattern);

		Map<String, String> sortMapping = Map.ofEntries(
			Map.entry("id", "id"),
			Map.entry("name", "name"),
			Map.entry("riskReductionPercent", "strr.riskReductionPercent"),
			Map.entry("toolPrice", "strr.toolPrice"),
			Map.entry("system", "name")
		);
		if (filteredRequest.getSort() != null) filteredRequest.getSort().setSortMapping(sortMapping);

		// List<Object[]> objectsList = systemToolRiskReductionRepository.getListByOrganization(organizationId, namePattern, filteredRequest.toPageRequest());
		// List<SystemToolRiskReductionResult> resultItems = objectsList.stream().map(objects -> new SystemToolRiskReductionResult((Systems) objects[0], (SystemToolRiskReductions) objects[1])).collect(Collectors.toList());
		List<SystemToolRiskReductionResult> resultItems = systemToolRiskReductionRepository.getListByOrganization(organizationId, namePattern, filteredRequest.toPageRequest());
		List<SystemToolRiskReductionDTO> items = resultItems.stream().map(SystemToolRiskReductionResult::getSystemToolRiskReduction).collect(Collectors.toList());
		count = systemToolRiskReductionRepository.getCountByOrganization(organizationId, namePattern);

		filteredResponse.setItems(items);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get System Tool Risk Reduction details
	 *
	 * @return System Tool Risk Reduction Details
	 */
	public SystemToolRiskReductions getItemForCurrentOrganization(Long itemId) {
		SystemToolRiskReductions itemDetails;

		try {
			itemDetails = systemToolRiskReductionRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("System Risk Reduction not found in the database [{0}]", itemId));
		}

		// Verify System Tool Risk Reduction and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System Risk Reduction [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get System Tool Risk Reduction DTO details
	 *
	 * @return System Tool Risk Reduction Details
	 */
	public SystemToolRiskReductionDTO getDetails(Long itemId) {

		SystemToolRiskReductions itemDetails = getItemForCurrentOrganization(itemId);

		SystemToolRiskReductionDTO result = new SystemToolRiskReductionDTO(itemDetails);

		return result;
	}


	/**
	 * Create new System Tool Risk Reduction Domain
	 *
	 * @return New System Tool Risk Reduction
	 */
	public SystemToolRiskReductionDTO create(SystemToolRiskReductionDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		if (newItemDTO.getSystem() == null || newItemDTO.getSystem().getId() == null) {
			throw new BadRequestException("System is required", ApplicationExceptionCodes.SYSTEM_REQUIRED);
		}

		Systems system = systemsService.getSystemForCurrentOrganization(newItemDTO.getSystem().getId());
		Long organationId = organizationService.getCurrentOrganizationId();

		SystemToolRiskReductions newItem = systemToolRiskReductionRepository.findByOrganizationIdAndSystem(organationId, system).orElse(new SystemToolRiskReductions());

		if (newItem.getId() == null) {
			newItem.setOrganizationId(organationId);
			newItem.setSystem(system);
		}
		applyEntityChanges(newItemDTO, newItem);
		SystemToolRiskReductions saveResult = systemToolRiskReductionRepository.save(newItem);

		SystemToolRiskReductionDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Update System Tool Risk Reduction
	 *
	 * @return Updated System Tool Risk Reduction
	 */
	public SystemToolRiskReductionDTO update(SystemToolRiskReductionDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		SystemToolRiskReductions existingItem = getItemForCurrentOrganization(itemDTO.getId());

		// Verify Business Unit and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for System Tool Risk Reduction [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		SystemToolRiskReductions saveResult = systemToolRiskReductionRepository.save(existingItem);

		SystemToolRiskReductionDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Apply question changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(SystemToolRiskReductionDTO itemDTO, SystemToolRiskReductions entity) {
		// Set Qual Metrics
		entity.setRiskReductionPercent(itemDTO.getRiskReductionPercent());
		entity.setRiskReduction(itemDTO.getRiskReduction());
		entity.setToolPrice(itemDTO.getToolPrice());
		entity.setToolName(itemDTO.getToolName());
	}

	/**
	 * Deletes System Tool Risk Reduction
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		SystemToolRiskReductions existingItem = getItemForCurrentOrganization(itemId);
		systemToolRiskReductionRepository.delete(existingItem);
		systemToolRiskReductionRepository.flush();

		return itemId;
	}

}
