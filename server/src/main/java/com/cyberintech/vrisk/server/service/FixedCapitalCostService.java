package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.FixedCapitalCostModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.budget.FixedCapitalCostDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.FixedCapitalCostRepository;
import com.cyberintech.vrisk.server.repository.jpa.LicenseTypeRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.NoSuchElementException;

/**
 * Fixed Capital Costs management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Service
public class FixedCapitalCostService {

	@Autowired
	private FixedCapitalCostRepository fixedCapitalCostRepository;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private CybersecurityToolService cybersecurityToolService;

	@Autowired
	private LicenseTypeRepository licenseTypeRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private TechnologyService technologyService;

	@Autowired
	private VendorService vendorService;

	@Autowired
	private FixedCapitalCostModelDAO fixedCapitalCostModelDAO;

	/**
	 * Get Fixed Capital Costs List
	 *
	 * @return Fixed Capital Costs List
	 */
	public FilteredResponse<NameFilter, FixedCapitalCostDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {

		PagedResult<FixedCapitalCostDTO> result = fixedCapitalCostModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<NameFilter, FixedCapitalCostDTO> filteredResponse = new FilteredResponse<>(filteredRequest, result);

		return filteredResponse;
	}

	/**
	 * Get Fixed Capital Costs details
	 *
	 * @return Fixed Capital Costs Details
	 */
	public FixedCapitalCosts getItemForCurrentOrganization(Long itemId) {
		FixedCapitalCosts itemDetails;

		try {
			itemDetails = fixedCapitalCostRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Fixed Capital Costs not found in the database [{0}]", itemId));
		}

		// Verify Fixed Capital Costs and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Fixed Capital Costs [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Fixed Capital Costs DTO details
	 *
	 * @return Fixed Capital Costs Details
	 */
	public FixedCapitalCostDTO getDetails(Long itemId) {

		FixedCapitalCosts itemDetails = getItemForCurrentOrganization(itemId);

		FixedCapitalCostDTO result = new FixedCapitalCostDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Fixed Capital Costs Domain
	 *
	 * @return New Fixed Capital Costs
	 */
	public FixedCapitalCostDTO create(FixedCapitalCostDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		FixedCapitalCosts newItem = new FixedCapitalCosts();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		FixedCapitalCosts saveResult = fixedCapitalCostRepository.save(newItem);

		FixedCapitalCostDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Update Fixed Capital Costs
	 *
	 * @return Updated Fixed Capital Costs
	 */
	public FixedCapitalCostDTO update(FixedCapitalCostDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		FixedCapitalCosts existingItem = getItemForCurrentOrganization(itemDTO.getId());

		// Verify Fixed Capital Costs and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Fixed Capital Costs [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		FixedCapitalCosts saveResult = fixedCapitalCostRepository.save(existingItem);

		FixedCapitalCostDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(FixedCapitalCostDTO itemDTO, FixedCapitalCosts entity) {

		entity.setLicenseCost(itemDTO.getLicenseCost());
		entity.setPercentOfBudget(itemDTO.getPercentOfBudget());
		// entity.setSecurityToolName(itemDTO.getSecurityToolName());
		if (itemDTO.getTotalCosts() != null) entity.setTotalCosts(itemDTO.getTotalCosts());
		entity.setCostDate(itemDTO.getCostDate());
		entity.setStartDate(itemDTO.getStartDate());
		entity.setEndDate(itemDTO.getEndDate());

		if (itemDTO.getVendor() != null && itemDTO.getVendor().getId() != null) {
			Organizations vendor = vendorService.getVendor(itemDTO.getVendor().getId());
			entity.setVendor(vendor);
		}

		if (itemDTO.getBusinessUnit() != null && itemDTO.getBusinessUnit().getId() != null) {
			BusinessUnits businessUnit = businessUnitService.getBusinessUnitForCurrentOrganization(itemDTO.getBusinessUnit().getId());
			entity.setBusinessUnit(businessUnit);
		}

		if (itemDTO.getTechnology() != null && itemDTO.getTechnology().getId() != null) {
			Technologies technology = technologyService.getTechnologyForCurrentOrganization(itemDTO.getTechnology().getId());
			entity.setTechnology(technology);
		}
/*
		if (itemDTO.getCybersecurityTool() != null && itemDTO.getCybersecurityTool().getId() != null) {
			CybersecurityTools cybersecurityTool = cybersecurityToolService.getCybersecurityToolForCurrentOrganization(itemDTO.getCybersecurityTool().getId());
			entity.setCybersecurityTool(cybersecurityTool);
		}
*/
		if (itemDTO.getLicenseType() != null && itemDTO.getLicenseType().getId() != null) {
			LicenseTypes licenseType = licenseTypeRepository.findById(itemDTO.getLicenseType().getId()).get();
			entity.setLicenseType(licenseType);
		}

//		entity.setUpdatedBy(userService.getCurrentUserEntity());
//		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Fixed Capital Costs
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		FixedCapitalCosts existingItem = getItemForCurrentOrganization(itemId);
		fixedCapitalCostRepository.delete(existingItem);
		fixedCapitalCostRepository.flush();

		return itemId;
	}

}
