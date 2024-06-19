package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.agreements.OrganizationAgreementEditDTO;
import com.cyberintech.vrisk.server.model.dto.agreements.OrganizationAgreementViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.OrganizationsAgreements;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationAgreementRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

/**
 * Organization Agreements management Service. Implements basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since	 2020-01-16
 */
@Service
public class OrganizationAgreementService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private OrganizationAgreementRepository organizationAgreementRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Organization Agreements List
	 *
	 * @return Organization Agreements List
	 */
	public List<OrganizationAgreementViewDTO> getList() {
		List<OrganizationsAgreements> items = organizationAgreementRepository.findAll();

		List<OrganizationAgreementViewDTO> itemsDTOs = DTOBase.fromEntitiesList(items, OrganizationAgreementViewDTO.class);

		return itemsDTOs;
	}

	/**
	 * Get Organization Agreements List
	 *
	 * @return Organization Agreements List
	 */
	public FilteredResponse<NameFilter, OrganizationAgreementViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<OrganizationsAgreements> items;
		Long count = 0L;
		FilteredResponse<NameFilter, OrganizationAgreementViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

//		Long organizationId = organizationService.getCurrentOrganizationId();

//		items = organizationAgreementRepository.getListByOrganizationAndName(organizationId, namePattern, filteredRequest.toPageRequest());
//		count = organizationAgreementRepository.getCountByOrganizationAndName(organizationId, namePattern);
		items = organizationAgreementRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		count = organizationAgreementRepository.getCountByNameIsLike(namePattern);

		List<OrganizationAgreementViewDTO> itemsDTOs = DTOBase.fromEntitiesList(items, OrganizationAgreementViewDTO.class);

		filteredResponse.setItems(itemsDTOs);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Organization Agreement Item details
	 *
	 * @return Organization Agreement Detail
	 */
	public OrganizationsAgreements getOrganizationAgreementForCurrentOrganization(Long itemId) {
		OrganizationsAgreements itemDetails;

		try {
			itemDetails = organizationAgreementRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Organization Agreement Item not found in the database [{0}]", itemId), ApplicationExceptionCodes.ORGANIZATION_AGREEMENT_NOT_EXIST);
		}

		// Verify Item and Organization
//		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
//			throw new ForbiddenException(MessageFormat.format("Organization for Organization Agreement Item [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
//		}

		return itemDetails;
	}

	/**
	 * Get Organization Agreement Item DTO details
	 *
	 * @return Organization Agreement Detail
	 */
	public OrganizationAgreementEditDTO getDetails(Long itemId) {

		OrganizationsAgreements itemDetails = getOrganizationAgreementForCurrentOrganization(itemId);

		return new OrganizationAgreementEditDTO(itemDetails);
	}

	/**
	 * Create new Organization Agreement Item
	 *
	 * @return new Organization Agreement Item
	 */
	public OrganizationAgreementEditDTO create(OrganizationAgreementEditDTO newItemDTO) {
		// Throw exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create Organization Agreement item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		OrganizationsAgreements newItem = newItemDTO.toEntity();
		OrganizationsAgreements newItem = new OrganizationsAgreements();
		// Since it is an Admin API method there is no current organization
//		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		OrganizationsAgreements saveResult = organizationAgreementRepository.save(newItem);

		OrganizationAgreementEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.ORGANIZATION_AGREEMENT,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, organizationService.getCurrentOrganizationId())
		);

		return result;
	}

	/**
	 * Update Organization Agreement Item
	 *
	 * @return Updated Organization Agreement Item
	 */
	public OrganizationAgreementEditDTO update(OrganizationAgreementEditDTO itemDTO) {

		// Get Existing item from the database
		OrganizationsAgreements existingItem = getOrganizationAgreementForCurrentOrganization(itemDTO.getId());
		OrganizationAgreementEditDTO existingItemDTO = new OrganizationAgreementEditDTO(existingItem);

		// Verify Item and Organization
//		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
//			throw new ForbiddenException(MessageFormat.format("Organization for Organization Agreement Item [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
//		}

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		OrganizationsAgreements saveResult = organizationAgreementRepository.save(existingItem);

		OrganizationAgreementEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.ORGANIZATION_AGREEMENT,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, organizationService.getCurrentOrganizationId())
		);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(OrganizationAgreementEditDTO itemDTO, OrganizationsAgreements entity) {
		entity.setName(itemDTO.getName());
		entity.setContent(itemDTO.getContent());

		Optional.ofNullable(itemDTO.getOrganizations()).ifPresent(organizationRefDTOList -> {
			entity.setOrganizations(new HashSet<>());
			organizationRefDTOList.stream().forEach(assessmentViewDTO -> {
				entity.getOrganizations().add(organizationService.getOrganization(assessmentViewDTO.getId()));
			});
		});

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Organization Agreement
	 *
	 * @return ID of removed item
	 */
	public Long delete(Long itemId) {

		OrganizationsAgreements existingItem = getOrganizationAgreementForCurrentOrganization(itemId);
		OrganizationAgreementEditDTO existingItemDTO = new OrganizationAgreementEditDTO(existingItem);
		organizationAgreementRepository.delete(existingItem);
		organizationAgreementRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.ORGANIZATION_AGREEMENT,
			existingItemDTO.getId(),
			existingItem,
			collectAuditLogItems(existingItemDTO, organizationService.getCurrentOrganizationId())
		);

		return itemId;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param existingItemDTO
	 * @param organizationId
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(OrganizationAgreementEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	/**
	 * Get Organization Agreement Items which aren't answered for current User
	 *
	 * @param organizationId
	 * @param userId
	 *
	 * @return Organization Agreements List
	 */
	public List<OrganizationsAgreements> getListOfNotAnsweredOrganizationAgreements(Long organizationId, Long userId) {

		List<OrganizationsAgreements> result = organizationAgreementRepository.getListOfNotAnsweredByOrganizationIdAndUserId(organizationId, userId);

		return result;
	}
}
