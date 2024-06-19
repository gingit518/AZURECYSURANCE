package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.data.OrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.technology_categories.AdminTechnologyCategoryDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryEditDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyCategoryRepository;
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
 * Technology Categories management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Service
public class TechnologyCategoryService {

	@Autowired
	private AuditLogService auditLogService;

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
	public List<TechnologyCategoryViewDTO> getList() {
		List<TechnologyCategories> items = technologyCategoryRepository.findAll();

		List<TechnologyCategoryViewDTO> itemDTOs = DTOBase.fromEntitiesList(items, TechnologyCategoryViewDTO.class);

		return itemDTOs;
	}

	public FilteredResponse<OrganizationFilter, AdminTechnologyCategoryDTO> getAdminListFiltered(FilteredRequest<OrganizationFilter> filteredRequest) {
		List<TechnologyCategories> items = null;
		Long count = 0l;
		FilteredResponse<OrganizationFilter, AdminTechnologyCategoryDTO> filteredResponse = new FilteredResponse<OrganizationFilter, AdminTechnologyCategoryDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = filteredRequest.getFilter().getParent() != null && filteredRequest.getFilter().getParent().getId() != null ? filteredRequest.getFilter().getParent().getId() : null;
		if (organizationId != null) {
			items = technologyCategoryRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
			count = technologyCategoryRepository.getCountByOrganizationAndName(organizationId, namePattern);
		} else {
			items = technologyCategoryRepository.getListByName(namePattern, filteredRequest.toPageRequest());
			count = technologyCategoryRepository.getCountByName(namePattern);
		}

		List<AdminTechnologyCategoryDTO> itemsDTOList = DTOBase.fromEntitiesList(items, AdminTechnologyCategoryDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Technology Categories List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, TechnologyCategoryViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<TechnologyCategories> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, TechnologyCategoryViewDTO> filteredResponse = new FilteredResponse<NameFilter, TechnologyCategoryViewDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = technologyCategoryRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = technologyCategoryRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<TechnologyCategoryViewDTO> itemsDTOList = DTOBase.fromEntitiesList(items, TechnologyCategoryViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Technology Category details
	 *
	 * @return Technology Category Details
	 */
	public TechnologyCategories getTechnologyCategoryForCurrentOrganization(Long itemId) {
		TechnologyCategories itemDetails;

		try {
			Optional<TechnologyCategories> itemOptional = technologyCategoryRepository.findById(itemId);
			itemDetails = itemOptional.get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Technology Category not found in the database [{0}]", itemId));
		}

		// Verify Technology Category and Organization
		if (!userService.isSuperAdmin()) {
			if (itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
				throw new ForbiddenException(MessageFormat.format("Organization for Technology Category [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
			}
		}

		return itemDetails;
	}

	/**
	 * Get Technology Category DTO details
	 *
	 * @return Technology Category Details
	 */
	public TechnologyCategoryEditDTO getDetails(Long itemId) {

		TechnologyCategories itemDetails = getTechnologyCategoryForCurrentOrganization(itemId);

		TechnologyCategoryEditDTO result = new TechnologyCategoryEditDTO(itemDetails);

		return result;
	}

	/**
	 * Get Technology Category DTO details
	 *
	 * @return Technology Category Details
	 */
	public AdminTechnologyCategoryDTO getAdminDetails(Long itemId) {

		TechnologyCategories itemDetails = getTechnologyCategoryForCurrentOrganization(itemId);

		AdminTechnologyCategoryDTO result = new AdminTechnologyCategoryDTO(itemDetails);

		return result;
	}

	/**
	 * Get Existing or Create new Technology Category
	 *
	 * @return New Technology Category
	 */
	public TechnologyCategories getOrCreate(Long organizationId, String technologyCategoryName) {
		Optional<TechnologyCategories> technologyCategoryDetails = technologyCategoryRepository.getFirstByNameAndOrganization(technologyCategoryName, organizationId);
		if (technologyCategoryDetails.isEmpty()) {
			TechnologyCategories technologyCategory = new TechnologyCategories();
			technologyCategory.setName(technologyCategoryName);
			technologyCategory.setOrganizationId(organizationId);
			technologyCategory.setCreatedAt(new Date());
			// technologyCategory.setCreatedBy(Sy);
			technologyCategory.setUpdatedAt(new Date());
			// technologyCategory.setUpdatedBy(new Date());
			technologyCategory = technologyCategoryRepository.save(technologyCategory);

			technologyCategoryDetails = Optional.of(technologyCategory);
		}

		return technologyCategoryDetails.get();
	}

	/**
	 * Create new Technology Category Domain
	 *
	 * @return New Technology Categorie
	 */
	public TechnologyCategoryEditDTO create(TechnologyCategoryEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		String name = StringUtils.trim(newItemDTO.getName());

		// Verify Technology Category with such name not exists
		if (StringUtils.isEmpty(name)) {
			throw new ConflictException("Technology category cannot be blank", ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_EMPTY);
		}
		if (technologyCategoryRepository.findFirstByNameAndIdIsNotInAndOrganizationIdIsNull(name, Arrays.asList(0l)).isPresent()) {
			throw new ConflictException(MessageFormat.format("Technology category with this name already exist ", name), ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_ALREADY_EXISTS);
		}
		if (organizationId != null && technologyCategoryRepository.findFirstByNameAndIdIsNotInAndOrganizationId(name, Arrays.asList(0l), organizationId).isPresent()) {
			throw new ConflictException(MessageFormat.format("Technology category with this name already exist ", name), ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_ALREADY_EXISTS);
		}

//		TechnologyCategories newItem = newItemDTO.toEntity();
		TechnologyCategories newItem = new TechnologyCategories();
		newItem.setOrganizationId(organizationId);
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		TechnologyCategories saveResult = technologyCategoryRepository.save(newItem);

		TechnologyCategoryEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.TECHNOLOGY_CATEGORY,
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
	public TechnologyCategoryEditDTO update(TechnologyCategoryEditDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		TechnologyCategories existingItem = getTechnologyCategoryForCurrentOrganization(itemDTO.getId());
		TechnologyCategoryEditDTO existingItemDTO = new TechnologyCategoryEditDTO(existingItem);

		// Verify Technology Category and Organization
		if (!isSuperAdmin) {
			if (existingItem.getOrganizationId() == null) {
				throw new ForbiddenException(MessageFormat.format("Technology Category [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
			} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
				throw new ForbiddenException(MessageFormat.format("Organization for Technology Category [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
			}
		} else {
			existingItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		}
		String name = StringUtils.trim(itemDTO.getName());

		// Verify Technology Category with such name not exists
		if (StringUtils.isEmpty(name)) {
			throw new ConflictException("Technology category cannot be blank", ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_EMPTY);
		}
		if (technologyCategoryRepository.findFirstByNameAndIdIsNotInAndOrganizationIdIsNull(name, Arrays.asList(itemDTO.getId())).isPresent()) {
			throw new ConflictException(MessageFormat.format("Technology category with this name already exist ", name), ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_ALREADY_EXISTS);
		}
		if (organizationService.getCurrentOrganizationId() != null && technologyCategoryRepository.findFirstByNameAndIdIsNotInAndOrganizationId(name, Arrays.asList(itemDTO.getId()), organizationService.getCurrentOrganizationId()).isPresent()) {
			throw new ConflictException(MessageFormat.format("Technology category with this name already exist ", name), ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_ALREADY_EXISTS);
		}
		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		TechnologyCategories saveResult = technologyCategoryRepository.save(existingItem);

		TechnologyCategoryEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.TECHNOLOGY_CATEGORY,
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
	private void applyEntityChanges(TechnologyCategoryEditDTO itemDTO, TechnologyCategories entity) {

		String name = StringUtils.trim(itemDTO.getName());

		// Verify Technology Category non-spase beginning
		if (name.substring(0, 1).isBlank()) {
			throw new ConflictException(MessageFormat.format("Technology category should begin with a non-spase character", name), ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_FIRST_CHARACTER);
		}

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

		TechnologyCategories existingItem = getTechnologyCategoryForCurrentOrganization(itemId);
		TechnologyCategoryEditDTO existingItemDTO = new TechnologyCategoryEditDTO(existingItem);
		technologyCategoryRepository.delete(existingItem);
		technologyCategoryRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.TECHNOLOGY_CATEGORY,
			existingItemDTO.getId(),
			existingItemDTO,
			null
		);

		return itemId;
	}

}
