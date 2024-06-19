package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.risk_model.RiskModelViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantMetricLevel;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.domains.VariableType;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.AssociateVendorRepository;
import com.cyberintech.vrisk.server.repository.jpa.CacheMetricsDataRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskMetricsRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelRepository;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.dashboards.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Risk Model management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-27
 */
@Service
@Slf4j
public class RiskModelService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private RiskModelRepository riskModelRepository;

	/**
	 * Get Risk Domains List inside current Organization
	 *
	 * @return Risk Models List
	 */
	public List<RiskModelViewDTO> getList() {
		List<RiskModels> items = riskModelRepository.findAllByOrganizationId(organizationService.getCurrentOrganizationId());

		List<RiskModelViewDTO> itemDTOs = RiskModelViewDTO.fromEntitiesList(items, RiskModelViewDTO.class);

		// Create Default item if NO items found
		if (itemDTOs == null || itemDTOs.size() < 1) {
			RiskModelViewDTO newItemDTO = new RiskModelViewDTO();
			newItemDTO.setName("DEFAULT");
			newItemDTO.setDescription("Default risk model for Organization");
			RiskModelViewDTO createdItem = create(newItemDTO);
			itemDTOs = Arrays.asList(createdItem);
		}

		return itemDTOs;
	}

	/**
	 * Get Risk Domain details inside current Organization
	 *
	 * @return Risk Model Details
	 */
	public RiskModelViewDTO getDetails(Long itemId) {
		RiskModelViewDTO itemDTO;

		RiskModels itemDetails = getRiskModel(itemId);

		itemDTO = new RiskModelViewDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Risk Model details inside current Organization
	 *
	 * @return Risk Model Details
	 */
	public RiskModels getRiskModel(Long itemId) {
		RiskModels riskModel;

		try {
			riskModel = riskModelRepository.findByIdAndOrganizationId(itemId, organizationService.getCurrentOrganizationId()).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Item not found [{0}]", itemId));
		}

		if (!organizationService.getCurrentOrganizationId().equals(riskModel.getOrganizationId())) {
			throw new BadRequestException(MessageFormat.format("Organization for Risk Model [{0}] doesn't match your organization [{1}]", riskModel.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return riskModel;
	}

	/**
	 * Create new Risk Model
	 *
	 * @return New Risk Model
	 */
	public RiskModelViewDTO create(RiskModelViewDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException("Conflict while create item. ID is not allowed on create [" + newItemDTO.getId() + "]");
		}

		Long organizationId = organizationService.getCurrentOrganizationId();
		Long maxOrdinal = riskModelRepository.getMaxOrdinal(organizationId).orElse(0l);

//		RiskModels newItem = newItemDTO.toEntity();
		RiskModels newItem = new RiskModels();
		newItem.setName(newItemDTO.getName());
		newItem.setDescription(newItemDTO.getDescription());
		newItem.setOrganizationId(organizationId);
		newItem.setOrdinal(++maxOrdinal);
		RiskModels saveResult = riskModelRepository.save(newItem);

		RiskModelViewDTO result = new RiskModelViewDTO(saveResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.RISK_MODEL,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, newItem.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Risk Model
	 *
	 * @return Updated Risk Model
	 */
	public RiskModelViewDTO update(RiskModelViewDTO itemDTO) {

		RiskModelViewDTO result;

		try {
			Long organizationId = organizationService.getCurrentOrganizationId();

			// Get Existing item from the database
			RiskModels existingItem = riskModelRepository.findByIdAndOrganizationId(itemDTO.getId(), organizationId).get();
			RiskModelViewDTO existingItemDTO = new RiskModelViewDTO(existingItem);

			// Update item details
//			RiskModels updatedItem = itemDTO.toEntity(existingItem);
			existingItem.setName(itemDTO.getName());
			existingItem.setDescription(itemDTO.getDescription());
			existingItem.setOrdinal(itemDTO.getOrdinal());

			// Save to the database
			RiskModels saveResult = riskModelRepository.save(existingItem);

			result = new RiskModelViewDTO(saveResult);

			// Save Audit Log UPDATE event
			auditLogService.update(
				VItemType.RISK_MODEL,
				saveResult.getId(),
				existingItemDTO,
				result,
				collectAuditLogItems(result, existingItem.getOrganizationId())
			);

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException("Item not found in the database [" + itemDTO.getId() + "]");
		}

		return result;
	}

	/**
	 * Delete Risk Model
	 *
	 * @return Updated Risk Model
	 */
	public RiskModelViewDTO delete(RiskModelViewDTO itemDTO) {

		RiskModelViewDTO result;

		try {
			Long organizationId = organizationService.getCurrentOrganizationId();

			// Get Existing item from the database
			RiskModels existingItem;
			try {
				existingItem = riskModelRepository.findByIdAndOrganizationId(itemDTO.getId(), organizationId).get();
			} catch (NoSuchElementException exception) {
				throw new ItemNotFoundException(MessageFormat.format("Risk Model not found in the database [{0}]", itemDTO.getId()));
			}

			// Get Existing Model
			result = new RiskModelViewDTO(existingItem);

			riskModelRepository.deleteRiskModel(existingItem.getId());

			// Save Audit Log DELETE event
			auditLogService.delete(
				VItemType.SYSTEM,
				result.getId(),
				result,
				collectAuditLogItems(result, existingItem.getOrganizationId())
			);

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException("Item not found in the database [" + itemDTO.getId() + "]");
		}

		return result;
	}


	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(RiskModelViewDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
