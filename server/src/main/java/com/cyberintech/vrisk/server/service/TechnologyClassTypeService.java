package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.ByParentFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyClassTypeDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyClassTypes;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologySubcategories;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyCategoryRepository;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyClassTypeRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Technology Class Types management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-03
 */
@Service
public class TechnologyClassTypeService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private TechnologyClassTypeRepository technologyClassTypeRepository;

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
	public List<TechnologyClassTypeDTO> getList() {
		List<TechnologyClassTypes> items = technologyClassTypeRepository.findAll();

		List<TechnologyClassTypeDTO> itemDTOs = DTOBase.fromEntitiesList(items, TechnologyClassTypeDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Technology Categories List
	 *
	 * @return Users List
	 */
	public FilteredResponse<ByParentFilter, TechnologyClassTypeDTO> getListFiltered(FilteredRequest<ByParentFilter> filteredRequest) {
		List<TechnologyClassTypes> items = null;
		Long count = 0l;
		FilteredResponse<ByParentFilter, TechnologyClassTypeDTO> filteredResponse = new FilteredResponse<ByParentFilter, TechnologyClassTypeDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}
		Long parentId = filteredRequest.getFilter().getParentId();

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = technologyClassTypeRepository.getListByNameAndOrganizationAndParent(organizationId, parentId, namePattern, filteredRequest.toPageRequest());
		count = technologyClassTypeRepository.getCountByNameAndOrganizationAndParent(organizationId, parentId, namePattern);

		List<TechnologyClassTypeDTO> itemsDTOList = DTOBase.fromEntitiesList(items, TechnologyClassTypeDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Technology Class Type details
	 *
	 * @return Technology Class Type Details
	 */
	public TechnologyClassTypes getTechnologyClassTypeForCurrentOrganization(Long itemId) {
		TechnologyClassTypes itemDetails;

		try {
			Optional<TechnologyClassTypes> itemOptional = technologyClassTypeRepository.findById(itemId);
			itemDetails = itemOptional.get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Technology Class Type not found in the database [{0}]", itemId));
		}

		// Verify Technology Class Type and Organization
		if (!userService.isSuperAdmin()) {
			if (itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
				throw new ForbiddenException(MessageFormat.format("Organization for Technology Class Type [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
			}
		}

		return itemDetails;
	}

	/**
	 * Get Technology Class Type DTO details
	 *
	 * @return Technology Class Type Details
	 */
	public TechnologyClassTypeDTO getDetails(Long itemId) {

		TechnologyClassTypes itemDetails = getTechnologyClassTypeForCurrentOrganization(itemId);

		TechnologyClassTypeDTO result = new TechnologyClassTypeDTO(itemDetails);

		return result;
	}

	/**
	 * Get Existing or Create new Technology Class
	 *
	 * @return New Technology Class
	 */
	public TechnologyClassTypes getOrCreate(Long organizationId, Long technologyCategoryId, Long technologySubcategoryId, String technologyCategoryName) {
		Optional<TechnologyClassTypes> technologyClassTypeDetails = technologyClassTypeRepository.getFirstByNameAndOrganizationAndParent(technologyCategoryName, organizationId, technologySubcategoryId);
		if (technologyClassTypeDetails.isEmpty()) {
			TechnologyClassTypes technologyClassType = new TechnologyClassTypes();
			technologyClassType.setName(technologyCategoryName);
			technologyClassType.setOrganizationId(organizationId);
			technologyClassType.setCategoryId(technologyCategoryId);
			technologyClassType.setSubcategoryId(technologySubcategoryId);
			technologyClassType.setCreatedAt(new Date());
			technologyClassType.setUpdatedAt(new Date());
			technologyClassType = technologyClassTypeRepository.save(technologyClassType);

			technologyClassTypeDetails = Optional.of(technologyClassType);
		}

		return technologyClassTypeDetails.get();
	}

	/**
	 * Create new Technology Class Type Domain
	 *
	 * @return New Technology Categorie
	 */
	public TechnologyClassTypeDTO create(TechnologyClassTypeDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		String name = StringUtils.trim(newItemDTO.getName());

		// Verify Technology Class Type with such name not exists
		if (StringUtils.isEmpty(name)) {
			throw new ConflictException("Technology Class Type cannot be blank", ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_EMPTY);
		}
		if (technologyClassTypeRepository.findFirstByNameAndSubcategoryId(name, newItemDTO.getTechnologySubcategory().getId()).isPresent()) {
			throw new ConflictException(MessageFormat.format("Technology Class Type with this name already exist ", name), ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_ALREADY_EXISTS);
		}

//		TechnologyClassTypes newItem = newItemDTO.toEntity();
		TechnologyClassTypes newItem = new TechnologyClassTypes();
		newItem.setOrganizationId(organizationId);
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		TechnologyClassTypes saveResult = technologyClassTypeRepository.save(newItem);

		TechnologyClassTypeDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.TECHNOLOGY_TYPE_CLASS,
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
	public TechnologyClassTypeDTO update(TechnologyClassTypeDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();
		Boolean isSuperAdmin = userService.isSuperAdmin();

		// Get Existing item from the database
		TechnologyClassTypes existingItem = getTechnologyClassTypeForCurrentOrganization(itemDTO.getId());
		TechnologyClassTypeDTO existingItemDTO = new TechnologyClassTypeDTO(existingItem);

		// Verify Technology Class Type and Organization
		if (!isSuperAdmin) {
			if (existingItem.getOrganizationId() == null) {
				throw new ForbiddenException(MessageFormat.format("Technology Class Type [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
			} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
				throw new ForbiddenException(MessageFormat.format("Organization for Technology Class Type [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
			}
		} else {
			existingItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		}
		String name = StringUtils.trim(itemDTO.getName());

		// Verify Technology Class Type with such name not exists
		if (StringUtils.isEmpty(name)) {
			throw new ConflictException("Technology Class Type cannot be blank", ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_EMPTY);
		}
		/*
		if (technologySubcategoryRepository.findFirstByNameAndIdIsNotInAndOrganizationIdIsNull(name, Arrays.asList(itemDTO.getId())).isPresent()) {
			throw new ConflictException(MessageFormat.format("Technology Class Type with this name already exist ", name), ApplicationExceptionCodes.TECHNOLOGY_CATEGORY_ALREADY_EXISTS);
		}
		*/
		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		TechnologyClassTypes saveResult = technologyClassTypeRepository.save(existingItem);

		TechnologyClassTypeDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.TECHNOLOGY_TYPE_CLASS,
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
	private void applyEntityChanges(TechnologyClassTypeDTO itemDTO, TechnologyClassTypes entity) {

		String name = StringUtils.trim(itemDTO.getName());

		entity.setCategoryId(itemDTO.getTechnologyCategory().getId());
		entity.setSubcategoryId(itemDTO.getTechnologySubcategory().getId());
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

		TechnologyClassTypes existingItem = getTechnologyClassTypeForCurrentOrganization(itemId);
		TechnologyClassTypeDTO existingItemDTO = new TechnologyClassTypeDTO(existingItem);
		technologyClassTypeRepository.delete(existingItem);
		technologyClassTypeRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.TECHNOLOGY_TYPE_CLASS,
			existingItemDTO.getId(),
			existingItemDTO,
			null
		);

		return itemId;
	}

}
