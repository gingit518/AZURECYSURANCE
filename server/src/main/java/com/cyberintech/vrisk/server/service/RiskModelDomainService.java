package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.datadomains.DataDomainsDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model_domains.RiskModelDomainEditDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model_domains.RiskModelDomainViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelDomainRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Risk Model Domains management Service. Implements basic CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-09
 */
@Service
public class RiskModelDomainService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private RiskModelDomainRepository riskModelDomainRepository;

	@Autowired
	private RiskDomainService riskDomainService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private UserService userService;

	/**
	 * Get Risk Model Domains List inside current Organization
	 *
	 * @return Risk Model Domains List
	 */
	public List<RiskModelDomainViewDTO> getListByRiskModel(Long riskModelId) {

		RiskModels riskModel = riskModelService.getRiskModel(riskModelId);

		List<RiskModelDomains> items = riskModelDomainRepository.getListByRiskModelId(riskModelId);

		List<RiskModelDomainViewDTO> itemDTOs = RiskModelDomainViewDTO.fromEntitiesList(items, RiskModelDomainViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Risk Model Domain details inside current Organization
	 *
	 * @return Risk Model Domain Details
	 */
	public RiskModelDomainViewDTO getDetails(Long itemId) {
		RiskModelDomainViewDTO itemDTO;

		RiskModelDomains itemDetails = getRiskModelDomain(itemId);

		// Verify Risk Model and Organization
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		itemDTO = new RiskModelDomainViewDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Risk Model Domain details
	 *
	 * @return Risk Model Details
	 */
	public RiskModelDomains getRiskModelDomain(Long itemId) {
		RiskModelDomains riskModelDomain;

		try {
			riskModelDomain = riskModelDomainRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Item not found [{0}]", itemId));
		}

		return riskModelDomain;
	}

	/**
	 * Create new Risk Model Domain
	 *
	 * @return New Risk Model
	 */
	public RiskModelDomainViewDTO create(RiskModelDomainEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		RiskModels riskModel = riskModelService.getRiskModel(newItemDTO.getRiskModelId());
		RiskDomains riskDomain = null;
		String name = null;
		String description = null;
		Users createdByUser = userService.getOrganizationUser(userService.getCurrentUser().getUserId());
		Users riskManagementOwnerUser = userService.getOrganizationUser(newItemDTO.getRiskManagementOwnerUserId());

		// inheriting predefined risk domain
		if (newItemDTO.getRiskDomainId() != null) {
			riskDomain = riskDomainService.getRiskDomain(newItemDTO.getRiskDomainId());
			name = riskDomain.getName();
			description = riskDomain.getDescription();

		} else {
			// custom risk domain
			name = newItemDTO.getName();
			description = newItemDTO.getDescription();
		}

		RiskModelDomains newItem = new RiskModelDomains();
		newItem.setRiskModelId(riskModel.getId());
		newItem.setRiskDomain(riskDomain);
		newItem.setName(name);
		newItem.setDescription(description);
		newItem.setCreatedBy(createdByUser);
		newItem.setRiskManagementOwner(riskManagementOwnerUser);
		newItem.setCreatedAt(new Date());
		newItem.setUpdatedAt(new Date());
		RiskModelDomains saveResult = riskModelDomainRepository.save(newItem);

		RiskModelDomainViewDTO result = new RiskModelDomainViewDTO(saveResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.RISK_MODEL_DOMAIN,
			saveResult.getId(),
			result,
			null
		);

		return result;
	}

	/**
	 * Update Risk Model Domains
	 *
	 * @return Updated Risk Model Domains
	 */
	public RiskModelDomainViewDTO update(RiskModelDomainEditDTO itemDTO) {

		RiskModelDomainViewDTO result;

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		RiskModelDomains existingItem = getRiskModelDomain(itemDTO.getId());
		RiskModelDomainViewDTO existingItemDTO = new RiskModelDomainViewDTO(existingItem);

		// Verify Risk Model and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());
		Users riskManagementOwnerUser = userService.getOrganizationUser(itemDTO.getRiskManagementOwnerUserId());

		RiskDomains riskDomain = null;
		String name = null;
		String description = null;

		// inheriting predefined risk domain
		if (itemDTO.getRiskDomainId() != null) {
			riskDomain = riskDomainService.getRiskDomain(itemDTO.getRiskDomainId());
			name = riskDomain.getName();
			description = riskDomain.getDescription();

		} else {
			// custom risk domain
			name = itemDTO.getName();
			description = itemDTO.getDescription();
		}

		// Update item details
		existingItem.setRiskDomain(riskDomain);
		existingItem.setName(name);
		existingItem.setDescription(description);
		existingItem.setRiskManagementOwner(riskManagementOwnerUser);
		existingItem.setUpdatedAt(new Date());

		// Save to the database
		RiskModelDomains saveResult = riskModelDomainRepository.save(existingItem);

		result = new RiskModelDomainViewDTO(saveResult);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.RISK_MODEL_DOMAIN,
			saveResult.getId(),
			existingItemDTO,
			result,
			null
		);

		return result;
	}


	/**
	 * Deletes Risk Model Domain
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		RiskModelDomains existingItem = getRiskModelDomain(itemId);
		if (existingItem.getRiskModelId() == null) {
			throw new ForbiddenException(MessageFormat.format("Risk Model Domain [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getName()));
		}
		RiskModelDomainViewDTO existingItemDTO = new RiskModelDomainViewDTO(existingItem);
		riskModelDomainRepository.delete(existingItem);

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.RISK_MODEL_DOMAIN,
			existingItemDTO.getId(),
			existingItemDTO,
			null
		);

		return itemId;
	}

}
