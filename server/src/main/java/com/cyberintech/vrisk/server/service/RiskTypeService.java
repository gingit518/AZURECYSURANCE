package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.risk_type.RiskTypeEditDTO;
import com.cyberintech.vrisk.server.model.dto.risk_type.RiskTypeViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.CategoryDomains;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskTypes;
import com.cyberintech.vrisk.server.repository.jpa.CategoryDomainRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskTypeRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

/**
 * Risk Types management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class RiskTypeService {

	@Autowired
	private RiskTypeRepository riskTypeRepository;

	@Autowired
	private CategoryDomainRepository categoryDomainRepository;

	@Autowired
	private CategoryDomainService categoryDomainService;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private RiskModelDomainService riskModelDomainService;

	/**
	 * Get Risk Types List
	 *
	 * @return Risk Types List
	 */
	public List<RiskTypeViewDTO> getList() {
		List<RiskTypes> items = riskTypeRepository.findAll();

		List<RiskTypeViewDTO> itemDTOs = RiskTypeViewDTO.fromEntitiesList(items, RiskTypeViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Risk Types List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, RiskTypeViewDTO> getListFiltered(Long riskModelId, FilteredRequest<NameFilter> filteredRequest) {
		List<RiskTypes> items;
		Long count = 0l;
		FilteredResponse<NameFilter, RiskTypeViewDTO> filteredResponse = new FilteredResponse<NameFilter, RiskTypeViewDTO>(filteredRequest);

		String nameFilter = Optional.ofNullable(filteredRequest.getFilter().getName()).orElse("");
		Collection<Long> excludeIds = filteredRequest.getFilter().getExcludeIds();
		if (excludeIds == null || excludeIds.size() < 1) {
			excludeIds = Arrays.asList(0l);
		}

		items = riskTypeRepository.getListByRiskModelPaged(riskModelId, nameFilter, excludeIds, filteredRequest.toPageRequest());
		count = riskTypeRepository.getListByRiskModelCount(riskModelId, nameFilter, excludeIds);

		List<RiskTypeViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, RiskTypeViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Risk Type details
	 *
	 * @return Risk Type Details
	 */
	public RiskTypeViewDTO getDetails(Long itemId) {

		RiskTypes itemDetails = getRiskType(itemId);

		RiskTypeViewDTO itemDTO = new RiskTypeViewDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Risk Type details
	 *
	 * @return Risk Type Details
	 */
	public RiskTypes getRiskType(Long itemId) {
		RiskTypes itemDetails;

		try {
			itemDetails = riskTypeRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Risk Type not found in the database [{0}]", itemId));
		}

		// Verify Risk Model and Organization
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getCategoryDomain().getRiskModelId());

		return itemDetails;
	}

	/**
	 * Get Risk Risk Types List inside current Organization
	 *
	 * @return Risk Risk Types List
	 */
	public List<RiskTypeViewDTO> getListByCategoryDomain(Long categoryDomainId) {

		// RiskModels riskModel = riskModelService.getRiskModel(categoryDomainId);

		List<RiskTypes> items = riskTypeRepository.getListByCategoryDomainId(categoryDomainId);

		List<RiskTypeViewDTO> itemDTOs = RiskTypeViewDTO.fromEntitiesList(items, RiskTypeViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Create new Risk Model Domain
	 *
	 * @return New Risk Model
	 */
	public RiskTypeViewDTO create(RiskTypeEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		CategoryDomains categoryDomain = categoryDomainService.getCategoryDomain(newItemDTO.getCategoryDomain().getId());

//		RiskTypes newItem = newItemDTO.toEntity();
		RiskTypes newItem = new RiskTypes();
		newItem.setName(newItemDTO.getName());
		newItem.setDescription(newItemDTO.getDescription());
		newItem.setCategoryDomain(categoryDomain);
		newItem.setCreatedAt(new Date());
		newItem.setUpdatedAt(new Date());
		RiskTypes saveResult = riskTypeRepository.save(newItem);

		RiskTypeViewDTO result = new RiskTypeViewDTO(saveResult);

		return result;
	}

	/**
	 * Update Risk Risk Types
	 *
	 * @return Updated Risk Risk Types
	 */
	public RiskTypeViewDTO update(RiskTypeEditDTO itemDTO) {

		RiskTypeViewDTO result;

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		RiskTypes existingItem = getRiskType(itemDTO.getId());

		// Verify Category Domain, Risk Model and Organization
		CategoryDomains categoryDomain = categoryDomainService.getCategoryDomain(existingItem.getCategoryDomain().getId());

		// Update item details
		existingItem.setName(itemDTO.getName());
		existingItem.setDescription(itemDTO.getDescription());
		existingItem.setUpdatedAt(new Date());

		// Save to the database
		RiskTypes saveResult = riskTypeRepository.save(existingItem);

		result = new RiskTypeViewDTO(saveResult);

		return result;
	}

	/**
	 * Deletes Risk Type
	 *
	 * @return ID of removed item
	 */
	public Long delete(Long itemId) {

		RiskTypes existingItem = getRiskType(itemId);

		riskTypeRepository.delete(existingItem);

		return itemId;
	}

}
