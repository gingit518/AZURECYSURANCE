package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.category_domain.CategoryDomainDetailsDTO;
import com.cyberintech.vrisk.server.model.dto.category_domain.CategoryDomainEditDTO;
import com.cyberintech.vrisk.server.model.dto.category_domain.CategoryDomainViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_type.CategoryRiskTypeViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.CategoryDomainRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskTypeRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Category Domains management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class CategoryDomainService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private CategoryDomainRepository categoryDomainRepository;

	@Autowired
	private RiskTypeRepository riskTypeRepository;

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private RiskModelDomainService riskModelDomainService;

	/**
	 * Get Category Domains List
	 *
	 * @return Category Domains List
	 */
	public List<CategoryDomainViewDTO> getList() {
		List<CategoryDomains> items = categoryDomainRepository.findAll();

		List<CategoryDomainViewDTO> itemDTOs = CategoryDomainViewDTO.fromEntitiesList(items, CategoryDomainViewDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Category Domain details
	 *
	 * @return Category Domain Details
	 */
	public CategoryDomainDetailsDTO getDetails(Long itemId) {

		CategoryDomains itemDetails = getCategoryDomain(itemId);

		CategoryDomainDetailsDTO itemDTO = new CategoryDomainDetailsDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get Category Domain details
	 *
	 * @return Category Domain Details
	 */
	public CategoryDomains getCategoryDomain(Long itemId) {
		CategoryDomains itemDetails;

		try {
			itemDetails = categoryDomainRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Category Domain not found in the database [{0}]", itemId));
		}

		// Verify Risk Model and Organization
		RiskModels riskModel = riskModelService.getRiskModel(itemDetails.getRiskModelId());

		return itemDetails;
	}

	/**
	 * Get Risk Category Domains List inside current Organization
	 *
	 * @return Risk Category Domains List
	 */
	public List<CategoryDomainDetailsDTO> getListByRiskModel(Long riskModelId) {

		RiskModels riskModel = riskModelService.getRiskModel(riskModelId);

		List<CategoryDomains> items = categoryDomainRepository.getListByRiskModelId(riskModelId);

		List<CategoryDomainDetailsDTO> itemDTOs = CategoryDomainViewDTO.fromEntitiesList(items, CategoryDomainDetailsDTO.class);

		return itemDTOs;
	}

	/**
	 * Create new Risk Model Domain
	 *
	 * @return New Risk Model
	 */
	public CategoryDomainDetailsDTO create(CategoryDomainEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		RiskModels riskModel = riskModelService.getRiskModel(newItemDTO.getRiskModelId());
		RiskModelDomains riskModelDomain = riskModelDomainService.getRiskModelDomain(newItemDTO.getDomain().getId());

//		CategoryDomains newItem = newItemDTO.toEntity();
		CategoryDomains newItem = new CategoryDomains();
		newItem.setName(newItemDTO.getName());
		newItem.setDescription(newItemDTO.getDescription());
		if (newItem.getRiskTypes() != null) newItem.getRiskTypes().clear(); // Clear Risk types as it is not usable to get there outside JPA
		newItem.setRiskModelId(riskModel.getId());
		newItem.setRiskModelDomain(riskModelDomain);
		newItem.setCreatedAt(new Date());
		newItem.setUpdatedAt(new Date());
		CategoryDomains saveResult = categoryDomainRepository.save(newItem);

		// Save risk types
		synchronizeRiskTypes(saveResult, newItemDTO.getRiskTypes());
		categoryDomainRepository.flush();
		saveResult = categoryDomainRepository.findById(saveResult.getId()).get();

		CategoryDomainDetailsDTO result = new CategoryDomainDetailsDTO(saveResult);

		// Save Audit Log CREATE event
		CategoryDomainEditDTO createdItemDTO = new CategoryDomainEditDTO(saveResult);
		auditLogService.create(
			VItemType.CATEGORY_DOMAIN,
			saveResult.getId(),
			createdItemDTO,
			collectAuditLogItems(createdItemDTO, riskModel.getOrganizationId())
		);

		return result;
	}

	/**
	 * Update Risk Category Domains
	 *
	 * @return Updated Risk Category Domains
	 */
	public CategoryDomainDetailsDTO update(CategoryDomainEditDTO itemDTO) {

		CategoryDomainDetailsDTO result;

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		CategoryDomains existingItem = getCategoryDomain(itemDTO.getId());
		CategoryDomainEditDTO existingItemDTO = new CategoryDomainEditDTO(existingItem);

		// Verify Risk Model and Organization Id
		RiskModels riskModel = riskModelService.getRiskModel(existingItem.getRiskModelId());

		RiskModelDomains riskModelDomain = riskModelDomainService.getRiskModelDomain(itemDTO.getDomain().getId());

		// Update item details
		existingItem.setRiskModelDomain(riskModelDomain);
		existingItem.setName(itemDTO.getName());
		existingItem.setDescription(itemDTO.getDescription());
		existingItem.setUpdatedAt(new Date());

		// Save to the database
		CategoryDomains saveResult = categoryDomainRepository.save(existingItem);

		// Save risk types
		synchronizeRiskTypes(saveResult, itemDTO.getRiskTypes());
		saveResult = categoryDomainRepository.findById(saveResult.getId()).get();

		result = new CategoryDomainDetailsDTO(saveResult);

		// Save Audit Log UPDATE event
		CategoryDomainEditDTO newItemDTO = new CategoryDomainEditDTO(saveResult);
		auditLogService.update(
			VItemType.CATEGORY_DOMAIN,
			saveResult.getId(),
			existingItemDTO,
			newItemDTO,
			collectAuditLogItems(newItemDTO, riskModel.getOrganizationId())
		);

		return result;
	}

	/**
	 * Synchronize Risk Types for Category Domain
	 *
	 * @param categoryDomain
	 * @param riskTypes
	 */
	private void synchronizeRiskTypes(CategoryDomains categoryDomain, List<CategoryRiskTypeViewDTO> riskTypes) {

		List<CategoryRiskTypeViewDTO> riskTypesCollection = Optional.ofNullable(riskTypes).orElse(new ArrayList<>());

		Map<Long, CategoryRiskTypeViewDTO> riskTypesMap = riskTypesCollection.stream().filter(riskType -> riskType.getId() != null).collect(Collectors.toMap(CategoryRiskTypeViewDTO::getId, riskType -> riskType));

		// Collect Items to Remove
		List<RiskTypes> itemsToRemove = new ArrayList<>();
		categoryDomain.getRiskTypes().stream().forEach(riskType -> {
			if (!riskTypesMap.containsKey(riskType.getId())) {
				itemsToRemove.add(riskType);
			}
		});

		// Physically delete item from DB
		itemsToRemove.stream().forEach(item -> riskTypeRepository.delete(item));

		categoryDomain.getRiskTypes().removeAll(itemsToRemove);

		// Save Risk Types
		riskTypesCollection.stream().forEach(riskTypeViewDTO -> {
			RiskTypes riskTypesEntity;
			if (riskTypeViewDTO.getId() != null) {
				riskTypesEntity = riskTypeRepository.findById(riskTypeViewDTO.getId()).get();
			} else {
				riskTypesEntity = new RiskTypes();
			}

			riskTypesEntity.setCategoryDomain(categoryDomain);
			riskTypesEntity.setName(riskTypeViewDTO.getName());
			riskTypesEntity.setDescription(riskTypeViewDTO.getDescription());
			riskTypesEntity.setCreatedAt(new Date());
			riskTypesEntity.setUpdatedAt(new Date());

			riskTypeRepository.save(riskTypesEntity);

			categoryDomain.getRiskTypes().add(riskTypesEntity);
		});


		// Remove Links to Item
		categoryDomainRepository.save(categoryDomain);
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(CategoryDomainEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));
		if (existingItemDTO.getDomain() != null) logItems.add(AuditLogItemId.of(VItemType.RISK_MODEL_DOMAIN, existingItemDTO.getDomain().getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

}
