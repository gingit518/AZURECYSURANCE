package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitEditDTO;
import com.cyberintech.vrisk.server.model.dto.hints.HintsDTO;
import com.cyberintech.vrisk.server.model.dto.organization.IndustryDTO;
import com.cyberintech.vrisk.server.model.dto.organization.IndustryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.LanguageConstantScopeType;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.IndustryRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.util.ClientMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Industries management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-08
 */
@Service
public class IndustryService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private IndustryRepository industryRepository;

	/**
	 * Get Industries List
	 *
	 * @return Industries List
	 */
	public FilteredResponse<NameFilter, IndustryRefDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {

		FilteredResponse<NameFilter, IndustryRefDTO> filteredResponse = new FilteredResponse<NameFilter, IndustryRefDTO>(filteredRequest);

		List<Industries> items = null;
		Long count = 0l;
		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		items = industryRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = industryRepository.getCountByName(namePattern);

		List<IndustryRefDTO> itemsDTOList = DTOBase.fromEntitiesList(items, IndustryRefDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Industries List
	 *
	 * @return Industries List
	 */
	public FilteredResponse<NameFilter, IndustryDTO> getListFilteredWithParents(FilteredRequest<NameFilter> filteredRequest) {

		FilteredResponse<NameFilter, IndustryDTO> filteredResponse = new FilteredResponse<NameFilter, IndustryDTO>(filteredRequest);

		List<Industries> items = null;
		Long count = 0l;
		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		items = industryRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = industryRepository.getCountByName(namePattern);

		List<IndustryDTO> itemsDTOList = DTOBase.fromEntitiesList(items, IndustryDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Industry details
	 *
	 * @return Industry Details
	 */
	public Industries getItem(Long itemId) {
		Industries itemDetails;

		try {
			itemDetails = industryRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Industry not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Get Industry DTO details
	 *
	 * @return Industry Details
	 */
	public IndustryDTO getDetails(Long itemId) {

		Industries itemDetails = getItem(itemId);

		IndustryDTO result = new IndustryDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Industry
	 *
	 * @return New Industry
	 */
	public IndustryDTO create(IndustryDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		String name = newItemDTO.getName();

		Industries newItem = new Industries();
		applyEntityChanges(newItemDTO, newItem);
		Industries saveResult = industryRepository.save(newItem);

		IndustryDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.INDUSTRY,
			saveResult.getId(),
			result,
			null
		);

		return result;
	}

	/**
	 * Update Industry
	 *
	 * @return Updated Industry
	 */
	public IndustryDTO update(IndustryDTO itemDTO) {

		// Get Existing item from the database
		Industries existingItem = getItem(itemDTO.getId());
		IndustryDTO existingItemDTO = new IndustryDTO(existingItem);

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		Industries saveResult = industryRepository.save(existingItem);

		IndustryDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.INDUSTRY,
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
	private void applyEntityChanges(IndustryDTO itemDTO, Industries entity) {

		// Verify Industry with such name not exists
		if (industryRepository.findFirstByNameAndIdNotIn(itemDTO.getName(), Arrays.asList(entity.getId() != null ? entity.getId() : 0l)).isPresent()) {
			throw new ConflictException(MessageFormat.format("Industry with this name already exist {0}", itemDTO.getName()), ApplicationExceptionCodes.INDUSTRY_ALREADY_EXISTS);
		}

		// Apply entity changes
		entity.setName(itemDTO.getName());
		entity.setNaicsCode(itemDTO.getNaicsCode());
		entity.setNaicsCodeUpper(itemDTO.getNaicsCodeUpper());

		if (itemDTO.getParent() != null && itemDTO.getParent().getId() != null) {
			Optional<Industries> parentItem = industryRepository.findById(itemDTO.getParent().getId());
			if (parentItem.isPresent()) {
				entity.setParent(parentItem.get());
			}
		} else {
			entity.setParent(null);
		}
	}


	/**
	 * Deletes Business Unit
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Industries existingItem = getItem(itemId);
		IndustryDTO existingItemDTO = new IndustryDTO(existingItem);

		List<Industries> childs = industryRepository.findAllByParent(existingItem);
		if (!childs.isEmpty()) {
			String message = "Industry already contains list of children, therefore it cannot be removed:";
			message += "\\n" + childs.stream().map(industries -> industries.getName()).filter(Objects::nonNull).collect(Collectors.joining("\\n"));
			throw new ConflictException(message, ApplicationExceptionCodes.INDUSTRY_HAS_CHILDREN);
		}

		try {
			industryRepository.delete(existingItem);
			industryRepository.flush();

			// Save Audit Log DELETE event
			auditLogService.delete(
				VItemType.INDUSTRY,
				existingItemDTO.getId(),
				existingItemDTO,
				null
			);
		} catch (Exception exception) {
			String message = "Industry already linked to another entities in the database, like vendors, subsidiaries etc. Therefore it cannot be removed!";
			throw new ConflictException(message, ApplicationExceptionCodes.INDUSTRY_HAS_LINKS);
		}

		return itemId;
	}


}
