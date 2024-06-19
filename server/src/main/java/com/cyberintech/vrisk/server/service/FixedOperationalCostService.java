package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.FixedOperationalCostModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.budget.FixedOperationalCostDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.FixedOperationalCostRepository;
import com.cyberintech.vrisk.server.repository.jpa.RateTypeRepository;
import com.cyberintech.vrisk.server.repository.jpa.RoleRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.NoSuchElementException;

/**
 * Fixed Operational Costs management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Service
public class FixedOperationalCostService {

	@Autowired
	private FixedOperationalCostRepository fixedOperationalCostRepository;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private CyberRoleService cyberRoleService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RateTypeRepository rateTypeRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private FixedOperationalCostModelDAO fixedOperationalCostModelDAO;

	/**
	 * Get Fixed Operational Costs List
	 *
	 * @return Fixed Operational Costs List
	 */
	public FilteredResponse<NameFilter, FixedOperationalCostDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {

		PagedResult<FixedOperationalCostDTO> result = fixedOperationalCostModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<NameFilter, FixedOperationalCostDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Fixed Operational Costs details
	 *
	 * @return Fixed Operational Costs Details
	 */
	public FixedOperationalCosts getItemForCurrentOrganization(Long itemId) {
		FixedOperationalCosts itemDetails;

		try {
			itemDetails = fixedOperationalCostRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Fixed Operational Costs not found in the database [{0}]", itemId));
		}

		// Verify Fixed Operational Costs and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Fixed Operational Costs [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Fixed Operational Costs DTO details
	 *
	 * @return Fixed Operational Costs Details
	 */
	public FixedOperationalCostDTO getDetails(Long itemId) {

		FixedOperationalCosts itemDetails = getItemForCurrentOrganization(itemId);

		FixedOperationalCostDTO result = new FixedOperationalCostDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Fixed Operational Costs Domain
	 *
	 * @return New Fixed Operational Costs
	 */
	public FixedOperationalCostDTO create(FixedOperationalCostDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		FixedOperationalCosts newItem = new FixedOperationalCosts();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		FixedOperationalCosts saveResult = fixedOperationalCostRepository.save(newItem);

		FixedOperationalCostDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Update Fixed Operational Costs
	 *
	 * @return Updated Fixed Operational Costs
	 */
	public FixedOperationalCostDTO update(FixedOperationalCostDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		FixedOperationalCosts existingItem = getItemForCurrentOrganization(itemDTO.getId());

		// Verify Fixed Operational Costs and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Fixed Operational Costs [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		FixedOperationalCosts saveResult = fixedOperationalCostRepository.save(existingItem);

		FixedOperationalCostDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(FixedOperationalCostDTO itemDTO, FixedOperationalCosts entity) {

		entity.setRate(itemDTO.getRate());
		entity.setPercentOfTime(itemDTO.getPercentOfTime());
		entity.setPercentOfBudget(itemDTO.getPercentOfBudget());
		entity.setComments(itemDTO.getComments());
		entity.setCostDate(itemDTO.getCostDate());
		if (itemDTO.getTotalCosts() != null) entity.setTotalCosts(itemDTO.getTotalCosts());

		if (itemDTO.getTask() != null && itemDTO.getTask().getId() != null) {
			Tasks task = taskService.getItemForCurrentOrganization(itemDTO.getTask().getId());
			entity.setTask(task);
		}

		if (itemDTO.getCyberRole() != null && itemDTO.getCyberRole().getId() != null) {
			CyberRoles cyberRole = cyberRoleService.getCyberRoleForCurrentOrganization(itemDTO.getCyberRole().getId());
			entity.setCyberRole(cyberRole);
		}

		if (itemDTO.getUser() != null && itemDTO.getUser().getId() != null) {
			Users user = userService.getOrganizationUser(itemDTO.getUser().getId());
			entity.setUser(user);
		}

		if (itemDTO.getBusinessUnit() != null && itemDTO.getBusinessUnit().getId() != null) {
			BusinessUnits businessUnit = businessUnitService.getBusinessUnitForCurrentOrganization(itemDTO.getBusinessUnit().getId());
			entity.setBusinessUnit(businessUnit);
		}

//		entity.setUpdatedBy(userService.getCurrentUserEntity());
//		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Fixed Operational Costs
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		FixedOperationalCosts existingItem = getItemForCurrentOrganization(itemId);
		fixedOperationalCostRepository.delete(existingItem);
		fixedOperationalCostRepository.flush();

		return itemId;
	}

}
