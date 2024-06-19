package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.VariableCostModelDAO;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.budget.FixedCapitalCostRefDTO;
import com.cyberintech.vrisk.server.model.dto.budget.FixedOperationalCostRefDTO;
import com.cyberintech.vrisk.server.model.dto.budget.VariableCostDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.CostTypes;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.model.jpa.entity.VariableCosts;
import com.cyberintech.vrisk.server.repository.jpa.CostTypeRepository;
import com.cyberintech.vrisk.server.repository.jpa.VariableCostRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;

/**
 * Variable Costs management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Service
public class VariableCostService {

	@Autowired
	private FixedCapitalCostService fixedCapitalCostService;

	@Autowired
	private FixedOperationalCostService fixedOperationalCostService;

	@Autowired
	private VariableCostRepository variableCostRepository;

	@Autowired
	private CostTypeRepository costTypeRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private VariableCostModelDAO variableCostModelDAO;

	/**
	 * Get Variable Costs List
	 *
	 * @return Variable Costs List
	 */
	public FilteredResponse<NameFilter, VariableCostDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {

		PagedResult<VariableCostDTO> result = variableCostModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<NameFilter, VariableCostDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Variable Costs details
	 *
	 * @return Variable Costs Details
	 */
	public VariableCosts getItemForCurrentOrganization(Long itemId) {
		VariableCosts itemDetails;

		try {
			itemDetails = variableCostRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Variable Costs not found in the database [{0}]", itemId));
		}

		// Verify Variable Costs and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Variable Costs [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Variable Costs DTO details
	 *
	 * @return Variable Costs Details
	 */
	public VariableCostDTO getDetails(Long itemId) {

		VariableCosts itemDetails = getItemForCurrentOrganization(itemId);

		VariableCostDTO result = new VariableCostDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Variable Costs Domain
	 *
	 * @return New Variable Costs
	 */
	public VariableCostDTO create(VariableCostDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		VariableCosts newItem = new VariableCosts();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		VariableCosts saveResult = variableCostRepository.save(newItem);

		VariableCostDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Update Variable Costs
	 *
	 * @return Updated Variable Costs
	 */
	public VariableCostDTO update(VariableCostDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		VariableCosts existingItem = getItemForCurrentOrganization(itemDTO.getId());

		// Verify Variable Costs and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Variable Costs [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		VariableCosts saveResult = variableCostRepository.save(existingItem);

		VariableCostDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(VariableCostDTO itemDTO, VariableCosts entity) {

		entity.setEquipmentCost(itemDTO.getEquipmentCost());
		entity.setPersonnelCost(itemDTO.getPersonnelCost());
		entity.setPercentOfBudget(itemDTO.getPercentOfBudget());
		entity.setPercentOfTime(itemDTO.getPercentOfTime());
		entity.setComments(itemDTO.getComments());
		if (itemDTO.getTotalCosts() != null) entity.setTotalCosts(itemDTO.getTotalCosts());
		entity.setCostDate(itemDTO.getCostDate());

		if (itemDTO.getSystem() != null && itemDTO.getSystem().getId() != null) {
			Systems system = systemsService.getSystemForCurrentOrganization(itemDTO.getSystem().getId());
			entity.setSystem(system);
		}

		if (itemDTO.getCostType() != null && itemDTO.getCostType().getId() != null) {
			CostTypes costType = costTypeRepository.findById(itemDTO.getCostType().getId()).get();
			entity.setCostType(costType);
		}

		if (CollectionUtils.isNotEmpty(itemDTO.getFixedCapitalCosts())) {
			entity.setFixedCapitalCosts(new HashSet<>());
			for (FixedCapitalCostRefDTO fixedCapitalCostRef : itemDTO.getFixedCapitalCosts()) {
				entity.getFixedCapitalCosts().add(fixedCapitalCostService.getItemForCurrentOrganization(fixedCapitalCostRef.getId()));
			}
		}

		if (CollectionUtils.isNotEmpty(itemDTO.getFixedOperationalCosts())) {
			entity.setFixedOperationalCosts(new HashSet<>());
			for (FixedOperationalCostRefDTO fixedOperationalCostRef : itemDTO.getFixedOperationalCosts()) {
				entity.getFixedOperationalCosts().add(fixedOperationalCostService.getItemForCurrentOrganization(fixedOperationalCostRef.getId()));
			}
		}

//		entity.setUpdatedBy(userService.getCurrentUserEntity());
//		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Variable Costs
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		VariableCosts existingItem = getItemForCurrentOrganization(itemId);
		variableCostRepository.delete(existingItem);
		variableCostRepository.flush();

		return itemId;
	}

}
