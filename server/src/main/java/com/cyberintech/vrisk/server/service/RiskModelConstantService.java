package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.risk_model_constants.RiskModelConstantEditDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model_constants.RiskModelConstantViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModelConstants;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelConstantRepository;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

/**
 * Risk Model Constants management Service. Implements basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-10-27
 */
@Service
public class RiskModelConstantService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private UserService userService;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private RiskModelConstantRepository riskModelConstantRepository;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Risk Model Constants List
	 *
	 * @return Risk Model Constants List
	 */
	public List<RiskModelConstantViewDTO> getList() {
		List<RiskModelConstants> items = riskModelConstantRepository.findAll();

		List<RiskModelConstantViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, RiskModelConstantViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Risk Model Constants List
	 *
	 * @param filteredRequest
	 * @return Risk Model Constants List
	 */
	public FilteredResponse<NameFilter, RiskModelConstantViewDTO> getListFiltered(Long riskModelId, FilteredRequest<NameFilter> filteredRequest) {
		FilteredResponse<NameFilter, RiskModelConstantViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest);

		List<RiskModelConstants> items = null;
		Long count = 0L;

		String namePattern = "";
		if (filteredRequest.getFilter() != null && StringUtils.isNotEmpty(filteredRequest.getFilter().getName())) {
			namePattern = filteredRequest.getFilter().getName();
		}

		items = riskModelConstantRepository.getListByRiskModelAndName(riskModelId, namePattern, filteredRequest.toPageRequest());
		count = riskModelConstantRepository.getCountByRiskModelAndName(riskModelId, namePattern);

		List<RiskModelConstantViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, RiskModelConstantViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Risk Model Constant details
	 *
	 * @param itemId
	 * @return Risk Model Constant Details
	 */
	public RiskModelConstants getItem(Long itemId) {
		RiskModelConstants itemDetails;

		try {
			itemDetails = riskModelConstantRepository.findById(itemId).get();

			// Verify Risk Model and Organization Id
			RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Risk Model Constant not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Get Risk Model Constant details
	 *
	 * @param itemId
	 * @return Risk Model Constant details
	 */
	public RiskModelConstantEditDTO getDetails(Long itemId) {

		RiskModelConstants itemDetails = getItem(itemId);

		RiskModelConstantEditDTO itemDTO = new RiskModelConstantEditDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Create new Risk Model Constant
	 *
	 * @param newItemDTO
	 * @return New Risk Model Constant details
	 */
	public RiskModelConstantEditDTO create(RiskModelConstantEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		// Verify Risk Model and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(newItemDTO.getRiskModelId());

		RiskModelConstants newItem = new RiskModelConstants();
		newItem.setRiskModelId(riskModel.getId());
		newItem.setCreatedAt(new Date());
		newItem.setCreatedBy(userService.getCurrentUserEntity());

		applyEntityChanges(newItemDTO, newItem);

		RiskModelConstants savedResult = riskModelConstantRepository.save(newItem);

		RiskModelConstantEditDTO result = new RiskModelConstantEditDTO(savedResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.RISK_MODEL_CONSTANT,
			savedResult.getId(),
			result,
			collectAuditLogItems(result, riskModel.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Risk Model Constant
	 *
	 * @param itemDTO
	 * @return New Risk Model Constant details
	 */
	public RiskModelConstantEditDTO update(RiskModelConstantEditDTO itemDTO) {

		// Get Existing item from the database
		RiskModelConstants existingItem = getItem(itemDTO.getId());
		RiskModelConstantEditDTO existingItemDTO = new RiskModelConstantEditDTO(existingItem);

		// Verify Risk Model and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(itemDTO.getRiskModelId());

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		RiskModelConstants savedResult = riskModelConstantRepository.save(existingItem);

		RiskModelConstantEditDTO result = new RiskModelConstantEditDTO(savedResult);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.RISK_MODEL_CONSTANT,
			savedResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, riskModel.getOrganizationId())
		);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(RiskModelConstantEditDTO itemDTO, RiskModelConstants entity) {
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());

		if (itemDTO.getValue() != null) {
			entity.setValue(itemDTO.getValue());
		} else {
			throw new BadRequestException("Value is required!");
		}

		entity.setUpdatedAt(new Date());
		entity.setUpdatedBy(userService.getCurrentUserEntity());
	}

	/**
	 * Deletes Risk Model Constant
	 *
	 * @param itemId
	 * @return ID of removed item
	 */
	public Long delete(Long itemId) {

		RiskModelConstants existingItem = getItem(itemId);

		// Verify Risk Model and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());
		RiskModelConstantEditDTO existingItemDTO = new RiskModelConstantEditDTO(existingItem);
		riskModelConstantRepository.delete(existingItem);
		riskModelConstantRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.RISK_MODEL_CONSTANT,
			existingItemDTO.getId(),
			existingItemDTO,
			collectAuditLogItems(existingItemDTO, riskModel.getOrganizationId())
		);

		return itemId;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return Audit Log Item Ids
	 */
	private AuditLogItemId[] collectAuditLogItems(RiskModelConstantEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(
			AuditLogItemId.of(VItemType.ORGANIZATION, organizationId),
			AuditLogItemId.of(VItemType.RISK_MODEL, existingItemDTO.getRiskModelId())
		));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}
}
