package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.budget.CybersecurityToolDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.CybersecurityTools;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import com.cyberintech.vrisk.server.repository.jpa.CybersecurityToolRepository;
import com.cyberintech.vrisk.server.repository.jpa.FixedCapitalCostRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemRepository;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

/**
 * Cybersecurity Tools management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-10
 */
@Service
public class CybersecurityToolService {

	@Autowired
	private CybersecurityToolRepository cybersecurityToolRepository;

	@Autowired
	private FixedCapitalCostRepository fixedCapitalCostRepository;

	@Autowired
	private QuantMetricsService quantMetricsService;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Cybersecurity Tools List
	 *
	 * @return Cybersecurity Tools List
	 */
	public List<CybersecurityToolDTO> getList() {
		List<CybersecurityTools> items = cybersecurityToolRepository.findAll();

		List<CybersecurityToolDTO> itemDTOs = DTOBase.fromEntitiesList(items, CybersecurityToolDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Cybersecurity Tools List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, CybersecurityToolDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<CybersecurityTools> items = null;
		Long count = 0l;
		FilteredResponse<NameFilter, CybersecurityToolDTO> filteredResponse = new FilteredResponse<NameFilter, CybersecurityToolDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();

		items = cybersecurityToolRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
		count = cybersecurityToolRepository.getCountByOrganizationAndName(organizationId, namePattern);

		List<CybersecurityToolDTO> itemsDTOList = DTOBase.fromEntitiesList(items, CybersecurityToolDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Cybersecurity Tool details
	 *
	 * @return Cybersecurity Tool Details
	 */
	public CybersecurityTools getCybersecurityToolForCurrentOrganization(Long itemId) {
		CybersecurityTools itemDetails;

		try {
			itemDetails = cybersecurityToolRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Cybersecurity Tool not found in the database [{0}]", itemId));
		}

		// Verify Cybersecurity Tool and Organization
		if (itemDetails.getOrganizationId() != null && !organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Cybersecurity Tool [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Cybersecurity Tool DTO details
	 *
	 * @return Cybersecurity Tool Details
	 */
	public CybersecurityToolDTO getDetails(Long itemId) {

		CybersecurityTools itemDetails = getCybersecurityToolForCurrentOrganization(itemId);

		CybersecurityToolDTO result = new CybersecurityToolDTO(itemDetails);

		return result;
	}


	/**
	 * Create new Cybersecurity Tool Domain
	 *
	 * @return New Cybersecurity Tool
	 */
	public CybersecurityToolDTO create(CybersecurityToolDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		CybersecurityTools newItem = newItemDTO.toEntity();
		CybersecurityTools newItem = new CybersecurityTools();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		CybersecurityTools saveResult = cybersecurityToolRepository.save(newItem);

		CybersecurityToolDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Update Cybersecurity Tool
	 *
	 * @return Updated Qualitative Domains
	 */
	public CybersecurityToolDTO update(CybersecurityToolDTO itemDTO) {

		// Long organizationId = organizationService.getCurrentOrganizationId();

		// Get Existing item from the database
		CybersecurityTools existingItem = getCybersecurityToolForCurrentOrganization(itemDTO.getId());

		// Verify Cybersecurity Tool and Organization
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Cybersecurity Tool [{0}] is marked as SYSTEM. You are not allowed to CHANGE it.", existingItem.getName()));
		} else if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Cybersecurity Tool [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		CybersecurityTools saveResult = cybersecurityToolRepository.save(existingItem);

		CybersecurityToolDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(CybersecurityToolDTO itemDTO, CybersecurityTools entity) {

		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());
		entity.setRiskReduction(itemDTO.getRiskReduction());
		entity.setRiskReductionPercent(itemDTO.getRiskReductionPercent());
		entity.setToolPrice(itemDTO.getToolPrice());

		// Set Quant Metrics Tools
		Optional.ofNullable(itemDTO.getQuantMetrics()).ifPresent(quantMetricsRefDTOList -> {
			entity.setQuantMetrics(new HashSet<>());
			quantMetricsRefDTOList.stream().forEach(quantMetricsRefDTO -> {
				entity.getQuantMetrics().add(quantMetricsService.getQuantMetric(quantMetricsRefDTO.getId()));
			});
		});

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Cybersecurity Tool
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		CybersecurityTools existingItem = getCybersecurityToolForCurrentOrganization(itemId);
		if (existingItem.getOrganizationId() == null) {
			throw new ForbiddenException(MessageFormat.format("Cybersecurity Tool [{0}] is marked as SYSTEM. You are not allowed to DELETE it.", existingItem.getName()));
		}

		// Remove from Systems
		List<Systems> systems = systemRepository.findAllByCybersecurityTools(existingItem);
		for (Systems system : systems) {
			system.getCybersecurityTools().remove(existingItem);
		}

		fixedCapitalCostRepository.deleteAllByCybersecurityTool(existingItem);
		cybersecurityToolRepository.delete(existingItem);
		cybersecurityToolRepository.flush();

		return itemId;
	}

}
