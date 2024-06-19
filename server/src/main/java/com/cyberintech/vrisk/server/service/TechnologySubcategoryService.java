package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.ByParentFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.technology_categories.AdminTechnologyCategoryDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologySubcategoryDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologySubcategories;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyCategoryRepository;
import com.cyberintech.vrisk.server.repository.jpa.TechnologySubcategoryRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

/**
 * Technology Subategories management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-03
 */
@Service
public class TechnologySubcategoryService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private TechnologySubcategoryRepository technologySubcategoryRepository;

	@Autowired
	private TechnologyCategoryRepository technologyCategoryRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Technology Categories List
	 *
	 * @return Technology Categories List
	 */
	public List<TechnologySubcategoryDTO> getList() {
		List<TechnologySubcategories> items = technologySubcategoryRepository.findAll();

		List<TechnologySubcategoryDTO> itemDTOs = DTOBase.fromEntitiesList(items, TechnologySubcategoryDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Technology Categories List
	 *
	 * @return Users List
	 */
	public FilteredResponse<ByParentFilter, TechnologySubcategoryDTO> getListFiltered(FilteredRequest<ByParentFilter> filteredRequest) {
		List<TechnologySubcategories> items = null;
		Long count = 0l;
		FilteredResponse<ByParentFilter, TechnologySubcategoryDTO> filteredResponse = new FilteredResponse<ByParentFilter, TechnologySubcategoryDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}
		Long parentId = filteredRequest.getFilter().getParentId();

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = technologySubcategoryRepository.getListByNameAndOrganizationAndParent(organizationId, parentId, namePattern, filteredRequest.toPageRequest());
		count = technologySubcategoryRepository.getCountByNameAndOrganizationAndParent(organizationId, parentId, namePattern);

		List<TechnologySubcategoryDTO> itemsDTOList = DTOBase.fromEntitiesList(items, TechnologySubcategoryDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Technology Subcategory details
	 *
	 * @return Technology Subcategory Details
	 */
	public TechnologySubcategories getTechnologySubcategoryForCurrentOrganization(Long itemId) {
		TechnologySubcategories itemDetails;

		try {
			Optional<TechnologySubcategories> itemOptional = technologySubcategoryRepository.findById(itemId);
			itemDetails = itemOptional.get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Technology Subcategory not found in the database [{0}]", itemId));
		}

		// Verify Technology Subcategory and Organization
		if (!userService.isSuperAdmin()) {
			if (itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
				throw new ForbiddenException(MessageFormat.format("Organization for Technology Subcategory [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
			}
		}

		return itemDetails;
	}

	/**
	 * Get Technology Subcategory DTO details
	 *
	 * @return Technology Subcategory Details
	 */
	public TechnologySubcategoryDTO getDetails(Long itemId) {

		TechnologySubcategories itemDetails = getTechnologySubcategoryForCurrentOrganization(itemId);

		TechnologySubcategoryDTO result = new TechnologySubcategoryDTO(itemDetails);

		return result;
	}

	/**
	 * Get Existing or Create new Technology Subcategory
	 *
	 * @return New Technology Subcategory
	 */
	public TechnologySubcategories getOrCreate(Long organizationId, Long technologyCategoryId, String technologyCategoryName) {
		Optional<TechnologySubcategories> technologyCategoryDetails = technologySubcategoryRepository.getFirstByNameAndOrganizationAndParent(technologyCategoryName, organizationId, technologyCategoryId);
		if (technologyCategoryDetails.isEmpty()) {
			TechnologySubcategories technologySubcategory = new TechnologySubcategories();
			technologySubcategory.setName(technologyCategoryName);
			technologySubcategory.setOrganizationId(organizationId);
			technologySubcategory.setCategoryId(technologyCategoryId);
			technologySubcategory.setCreatedAt(new Date());
			technologySubcategory.setUpdatedAt(new Date());
			technologySubcategory = technologySubcategoryRepository.save(technologySubcategory);

			technologyCategoryDetails = Optional.of(technologySubcategory);
		}

		return technologyCategoryDetails.get();
	}

	/**
	 * Create new Technology Subcategory Domain
	 *
	 * @return New Technology Categorie
	 */
	public TechnologySubcategoryDTO create(TechnologySubcategoryDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		String name = StringUtils.trim(newItemDTO.getName());

		// Verify Technology Subcategory with such name not exists
		if (StringUtils.isEmpty(name)) {
			throw new ConflictException("Technology Subcategory cannot be blank", ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_EMPTY);
		}
		if (technologySubcategoryRepository.findFirstByNameAndCategoryId(name, newItemDTO.getTechnologyCategory().getId()).isPresent()) {
			throw new ConflictException(MessageFormat.format("Technology Subcategory with this name already exist ", name), ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_ALREADY_EXISTS);
		}

//		TechnologySubcategories newItem = newItemDTO.toEntity();
		TechnologySubcategories newItem = new TechnologySubcategories();
		newItem.setOrganizationId(organizationId);
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		TechnologySubcategories saveResult = technologySubcategoryRepository.save(newItem);

		TechnologySubcategoryDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.TECHNOLOGY_SUBCATEGORY,
			saveResult.getId(),
			result,
			null
		);

		return result;
	}

	/**
	 * Update Technology Categorie
	 *
	 * @return Updated Qualitative Domains
	 */
	public TechnologySubcategoryDTO update(TechnologySubcategoryDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		TechnologySubcategories existingItem = getTechnologySubcategoryForCurrentOrganization(itemDTO.getId());
		TechnologySubcategoryDTO existingItemDTO = new TechnologySubcategoryDTO(existingItem);

		// Verify Technology Subcategory and Organization
		if (!isSuperAdmin) {
			if (existingItem.getOrganizationId() == null) {
				throw new ForbiddenException(MessageFormat.format("Technology Subcategory [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
			} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
				throw new ForbiddenException(MessageFormat.format("Organization for Technology Subcategory [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
			}
		} else {
			existingItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		}
		String name = StringUtils.trim(itemDTO.getName());

		// Verify Technology Subcategory with such name not exists
		if (StringUtils.isEmpty(name)) {
			throw new ConflictException("Technology Subcategory cannot be blank", ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_EMPTY);
		}
		/*
		if (technologySubcategoryRepository.findFirstByNameAndIdIsNotInAndOrganizationIdIsNull(name, Arrays.asList(itemDTO.getId())).isPresent()) {
			throw new ConflictException(MessageFormat.format("Technology Subcategory with this name already exist ", name), ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_ALREADY_EXISTS);
		}
		*/
		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		TechnologySubcategories saveResult = technologySubcategoryRepository.save(existingItem);

		TechnologySubcategoryDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.TECHNOLOGY_SUBCATEGORY,
			saveResult.getId(),
			existingItemDTO,
			result,
			null
		);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(TechnologySubcategoryDTO itemDTO, TechnologySubcategories entity) {

		String name = StringUtils.trim(itemDTO.getName());

		entity.setCategoryId(itemDTO.getTechnologyCategory().getId());
		entity.setName(name);
		entity.setDescription(itemDTO.getDescription());
		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Technology Categorie
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		TechnologySubcategories existingItem = getTechnologySubcategoryForCurrentOrganization(itemId);
		TechnologySubcategoryDTO existingItemDTO = new TechnologySubcategoryDTO(existingItem);
		technologySubcategoryRepository.delete(existingItem);
		technologySubcategoryRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.TECHNOLOGY_SUBCATEGORY,
			existingItemDTO.getId(),
			existingItemDTO,
			null
		);

		return itemId;
	}

}
