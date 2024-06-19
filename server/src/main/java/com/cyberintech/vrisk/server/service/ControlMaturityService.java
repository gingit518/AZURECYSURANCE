package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.ControlMaturityEditDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.ControlMaturityViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlMaturities;
import com.cyberintech.vrisk.server.repository.jpa.ControlMaturitiesRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.List;

/**
 * Control Maturity management Service. Implements basic user CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-07-22
 */
@Service
public class ControlMaturityService {

	@Autowired
	private ControlMaturitiesRepository controlMaturitiesRepository;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Control Maturities List
	 *
	 * @return Control Maturities List
	 */
	public List<ControlMaturityViewDTO> getList() {
		List<ControlMaturities> items = controlMaturitiesRepository.findAll();

		List<ControlMaturityViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, ControlMaturityViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Control Maturities List
	 *
	 * @return Control Maturities List
	 */
	public FilteredResponse<NameFilter, ControlMaturityViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<ControlMaturities> items = null;
		Long count = 0L;
		FilteredResponse<NameFilter, ControlMaturityViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = controlMaturitiesRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = controlMaturitiesRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<ControlMaturityViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, ControlMaturityViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}
	/**
	 * Get Control Maturity details
	 *
	 * @return Control Maturity Details
	 */
	public ControlMaturities getControlMaturityForCurrentOrganization(Long itemId) {
		ControlMaturities itemDetails;

		itemDetails = controlMaturitiesRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(MessageFormat.format("Control Maturity not found in the database [{0}]", itemId)));

		// Verify Control Maturity and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Control Maturity [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Control Maturity DTO details
	 *
	 * @return Control Maturity Details
	 */
	public ControlMaturityEditDTO getDetails(Long itemId) {

		ControlMaturities itemDetails = getControlMaturityForCurrentOrganization(itemId);

		ControlMaturityEditDTO result = new ControlMaturityEditDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Control Maturity Domain
	 *
	 * @return New Control Maturity
	 */
	public ControlMaturityEditDTO create(ControlMaturityEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		ControlMaturities newItem = new ControlMaturities();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		applyEntityChanges(newItemDTO, newItem);
		ControlMaturities saveResult = controlMaturitiesRepository.save(newItem);

		return getDetails(saveResult.getId());
	}

	/**
	 * Update Control Maturity
	 *
	 * @return Updated Control Maturity
	 */
	public ControlMaturityEditDTO update(ControlMaturityEditDTO itemDTO) {

		// Get Existing item from the database
		ControlMaturities existingItem = getControlMaturityForCurrentOrganization(itemDTO.getId());

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		ControlMaturities saveResult = controlMaturitiesRepository.save(existingItem);

		return getDetails(saveResult.getId());
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(ControlMaturityEditDTO itemDTO, ControlMaturities entity) {

		entity.setName(itemDTO.getName());
		entity.setRiskModelId(itemDTO.getRiskModelId());
		entity.setWeight(itemDTO.getWeight());
		entity.setValue(itemDTO.getValue());
		System.out.println(entity);
	}

	/**
	 * Deletes Control Maturity
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		ControlMaturities existingItem = getControlMaturityForCurrentOrganization(itemId);
		controlMaturitiesRepository.delete(existingItem);
		controlMaturitiesRepository.flush();

		return itemId;
	}

}
