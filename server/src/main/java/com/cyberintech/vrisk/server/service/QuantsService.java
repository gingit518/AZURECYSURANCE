package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantsEditDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantsViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Quants;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.repository.jpa.MetricVariablesRepository;
import com.cyberintech.vrisk.server.repository.jpa.QuantsRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

/**
 * Quants management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class QuantsService {

	@Autowired
	private QuantsRepository quantsRepository;

	@Autowired
	private MetricVariablesRepository metricVariablesRepository;

	@Autowired
	private RegulationService regulationService;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private UserService userService;


	/**
	 * Get Quants List
	 *
	 * @return Quants List
	 */
	public FilteredResponse<NameFilter, QuantsViewDTO> getListFiltered(Long riskModelId, FilteredRequest<NameFilter> filteredRequest) {
		List<Quants> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, QuantsViewDTO> filteredResponse = new FilteredResponse<NameFilter, QuantsViewDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		// items = quantsRepository.getListByRiskModelAndName(riskModelId, namePattern, filteredRequest.toPageRequest());
		// count = quantsRepository.getCountByRiskModelAndName(riskModelId, namePattern);
		items = quantsRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = quantsRepository.getCountByName(namePattern);

		List<QuantsViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, QuantsViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Quant details
	 *
	 * @return Quant Details
	 */
	public QuantsEditDTO getDetails(Long itemId) {

		Quants itemDetails = getQuant(itemId);

		QuantsEditDTO itemDTO = new QuantsEditDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Quant details
	 *
	 * @return Quant Details
	 */
	public Quants getQuant(Long itemId) {
		Quants itemDetails;

		try {
			itemDetails = quantsRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Quant not found in the database [{0}]", itemId));
		}

		// Verify Risk Model and Organization
		// RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		return itemDetails;
	}

	/**
	 * Get Risk Quants List inside current Organization
	 *
	 * @return Quants List
	 */
	@Deprecated
	public List<QuantsViewDTO> getList() {

		List<Quants> items = quantsRepository.getList();

		List<QuantsViewDTO> itemDTOs = QuantsViewDTO.fromEntitiesList(items, QuantsViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Create new Quant
	 *
	 * @return New Quant
	 */
	@Deprecated
	public QuantsEditDTO create(QuantsEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		// RiskModels riskModel = riskModelService.getRiskModel(newItemDTO.getRiskModelId());

//		Quants newItem = newItemDTO.toEntity();
		Quants newItem = new Quants();
		newItem.setName(newItemDTO.getName());
		newItem.setDescription(newItemDTO.getDescription());
		newItem.setOrdinal(newItemDTO.getOrdinal());
		// newItem.setRiskModelId(riskModel.getId());
		newItem.setCreatedAt(new Date());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		applyEntityChanges(newItemDTO, newItem);

		Quants saveResult = quantsRepository.save(newItem);

		QuantsEditDTO result = new QuantsEditDTO(saveResult);

		return result;
	}

	/**
	 * Update Risk Quant
	 *
	 * @return Updated Quants
	 */
	@Deprecated
	public QuantsEditDTO update(QuantsEditDTO itemDTO) {

		QuantsEditDTO result;

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		Quants existingItem = getQuant(itemDTO.getId());

		// Update item details
		existingItem.setName(itemDTO.getName());
		existingItem.setDescription(itemDTO.getDescription());
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		Quants saveResult = quantsRepository.save(existingItem);

		result = new QuantsEditDTO(saveResult);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(QuantsEditDTO itemDTO, Quants entity) {

		// Set Regulations
		if (itemDTO.getRegulations() != null) {
			entity.setRegulations(new HashSet<>());
			Optional.ofNullable(itemDTO.getRegulations()).orElse(new ArrayList<>()).stream().forEach(regulationRef -> {
				entity.getRegulations().add(regulationService.getItem(regulationRef.getId()));
			});
		}

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Delete Risk Quant
	 *
	 * @return Deleted Quant
	 */
	@Deprecated
	public Long delete(Long itemId) {

		if (itemId >= 1024) {
			Optional<Quants> optionalItem = quantsRepository.findById(itemId);
            optionalItem.ifPresent(quants -> quantsRepository.delete(quants));
		}

		return itemId;
	}

}
