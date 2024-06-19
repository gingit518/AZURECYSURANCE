package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.agreements.UserAgreementEditDTO;
import com.cyberintech.vrisk.server.model.dto.agreements.UserAgreementViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationAgreementRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserAgreementRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Organization Agreements management Service. Implements basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.1
 * @since	 2020-01-17
 */
@Service
public class UserAgreementService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private OrganizationAgreementService organizationAgreementService;

	@Autowired
	private OrganizationAgreementRepository organizationAgreementRepository;

	@Autowired
	private UserAgreementRepository userAgreementRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get User Agreements List
	 *
	 * @return User Agreements List
	 */
	public List<UserAgreementViewDTO> getList() {
		List<UsersAgreements> items = userAgreementRepository.findAll();

		List<UserAgreementViewDTO> itemsDTOs = DTOBase.fromEntitiesList(items, UserAgreementViewDTO.class);

		return itemsDTOs;
	}

	/**
	 * Get User Agreements List
	 *
	 * TODO: Change NameFilter => BaseFilter
	 *
	 * @return User Agreements List
	 */
	public FilteredResponse<NameFilter, UserAgreementViewDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		List<UsersAgreements> items;
		Long count = 0L;
		FilteredResponse<NameFilter, UserAgreementViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		Long organizationId = organizationService.getCurrentOrganizationId();
		Long userId = userService.getCurrentUserEntity().getId();

		items = userAgreementRepository.getListByOrganizationIdAndUserId(organizationId, userId);
		count = userAgreementRepository.getCountByOrganizationIdAndUserId(organizationId, userId);

		List<UserAgreementViewDTO> itemsDTOs = DTOBase.fromEntitiesList(items, UserAgreementViewDTO.class);

		filteredResponse.setItems(itemsDTOs);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get User Agreement Items which aren't answered for current User
	 *
	 * @return User Agreements List
	 */
	@Deprecated
	public List<UserAgreementViewDTO> getListOfNotAnsweredAgreementsForCurrentUser() {
		List<UsersAgreements> allUserAgreements;
		List<UsersAgreements> userAgreementsAnsweredFalse;
		List<UsersAgreements> items = new ArrayList<>();
		List<OrganizationsAgreements> organizationAgreements;

		Long organizationId = organizationService.getCurrentOrganizationId();
		Users currentUser = userService.getCurrentUserEntity();

		organizationAgreements = organizationAgreementRepository.getListByOrganizationId(organizationId);
		userAgreementsAnsweredFalse = userAgreementRepository.getListByUserIdAndIsAnsweredFalse(currentUser.getId());
		allUserAgreements = userAgreementRepository.getListByOrganizationIdAndUserId(organizationId, currentUser.getId());

		// List of IDs of all Answered Organization Agreements for current User
		List<Long> answeredOrganizationAgreementsIds = allUserAgreements.stream()
			.map(usrAgreement -> usrAgreement.getIsAnswered() ? usrAgreement.getOrganizationAgreement().getId(): null)
			.filter(Objects::nonNull).collect(Collectors.toList());

		items.addAll(userAgreementsAnsweredFalse);

		List<UsersAgreements> newItems = organizationAgreements.stream()
			.filter(orgAgreement -> answeredOrganizationAgreementsIds.indexOf(orgAgreement.getId()) < 0)
			.map(UserAgreementEditDTO::new)
			.map(this::create)
			.map(userAgreementEditDTO -> {
				UsersAgreements entity = new UsersAgreements();
				entity.setAnswer(userAgreementEditDTO.getAnswer());
				entity.setIsAnswered(userAgreementEditDTO.getIsAnswered());

				return entity;
			}).collect(Collectors.toList());
//			.map(UserAgreementEditDTO::toEntity).collect(Collectors.toList());

		items.addAll(newItems);

		return DTOBase.fromEntitiesList(items, UserAgreementViewDTO.class);
	}

	/**
	 * Get User Agreement Items which aren't answered for current User
	 *
	 * @return User Agreements List
	 */
	public List<UserAgreementViewDTO> getListOfUnansweredUserAgreements() {
		// Get List of unanswered Organization Agreements for current User and Organization
		List<OrganizationsAgreements> organizationAgreements = organizationAgreementService
			.getListOfNotAnsweredOrganizationAgreements(
				organizationService.getCurrentOrganizationId(),
				userService.getCurrentUserEntity().getId()
			);

		// Create User Agreements details from Organization Agreements List
		List<UserAgreementViewDTO> items = organizationAgreements.stream()
			.map(UserAgreementViewDTO::new)
			.collect(Collectors.toList());

		return items;
	}

	/**
	 * Get User Agreement Item details
	 *
	 * @return User Agreement Detail
	 */
	private UsersAgreements getUserAgreementForCurrentUser(Long itemId) {
		UsersAgreements itemDetails;
		Long organizationId = organizationService.getCurrentOrganizationId();

		try {
			itemDetails = userAgreementRepository.findByIdAndUserId(itemId, userService.getCurrentUserEntity().getId()).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("User Agreement Item not found in the database [{0}]", itemId), ApplicationExceptionCodes.USER_AGREEMENT_NOT_EXIST);
		}

		// Verify Item and Organization
		for (Organizations agreementOrganization : itemDetails.getOrganizationAgreement().getOrganizations()) {
			if (agreementOrganization.getId().equals(organizationId)) {
				return itemDetails;
			}
		}
		throw new ForbiddenException(MessageFormat.format("Organization for User Agreement Item [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationAgreement().getId(), organizationService.getCurrentOrganizationId()));

		// Verify Item and Organization
//		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationAgreement().getOrganizations().getOrganizationId())) {
//			throw new ForbiddenException(MessageFormat.format("Organization for User Agreement Item [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationAgreement().getId(), organizationService.getCurrentOrganizationId()));
//		}

//		return itemDetails;
	}

	/**
	 * Get User Agreement Item DTO details
	 *
	 * @return User Agreement Detail
	 */
	public UserAgreementEditDTO getDetails(Long itemId) {

		UsersAgreements itemDetails = getUserAgreementForCurrentUser(itemId);

		return new UserAgreementEditDTO(itemDetails);
	}

	/**
	 *
	 * @param newItemDTO
	 * @return
	 */
	public UserAgreementEditDTO create(UserAgreementEditDTO newItemDTO) {
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create User Agreement item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

//		UsersAgreements newItem = newItemDTO.toEntity();
		UsersAgreements newItem = new UsersAgreements();

		newItem.setUser(userService.getCurrentUserEntity());
		applyEntityChanges(newItemDTO, newItem);
		UsersAgreements saveResult = userAgreementRepository.save(newItem);

		UserAgreementEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.USER_AGREEMENT,
			saveResult.getId(),
			result,
			collectAuditLogItems(result, organizationService.getCurrentOrganizationId())
		);

		return result;
	}

	/**
	 * Update User Agreement Item
	 *
	 * @return Updated User Agreement Item
	 */
	public UserAgreementEditDTO update(UserAgreementEditDTO itemDTO) {

		Long organizationAgreementId = itemDTO.getOrganizationAgreement().getId();
		Long userId = userService.getCurrentUserEntity().getId();
		UsersAgreements existingItem;

		// case when item provided with id, so it is presented in database
		if (itemDTO.getId() != null) {
			existingItem = getUserAgreementForCurrentUser(itemDTO.getId());

		}
		// case when item provided without id, but it still already presented in database
		else if (userAgreementRepository.existByOrganizationAgreementIdAndUserId(organizationAgreementId, userId)) {
			existingItem = userAgreementRepository
				.getByOrganizationAgreementIdAndUserId(organizationAgreementId, userId).get();

		}
		// case when item is not presented in the database so we should just save it and leave
		else {
			UserAgreementEditDTO result = create(itemDTO);

			return result;
		}
		UserAgreementEditDTO existingItemDTO = new UserAgreementEditDTO(existingItem);

		// Update item details
		existingItem.setUser(userService.getCurrentUserEntity());

		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		UsersAgreements saveResult = userAgreementRepository.save(existingItem);

		UserAgreementEditDTO result = getDetails(saveResult.getId());

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.USER_AGREEMENT,
			saveResult.getId(),
			existingItemDTO,
			result,
			collectAuditLogItems(result, organizationService.getCurrentOrganizationId())
		);

		return result;
	}

	/**
	 * Save List of User Agreement Items
	 *
	 * @return User Agreement Items
	 */
	public List<UserAgreementEditDTO> saveListOfAnsweredUserAgreements(List<UserAgreementEditDTO> itemsDTOs) {

		List<UserAgreementEditDTO> items = itemsDTOs.stream().map(this::update).collect(Collectors.toList());

		return items;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(UserAgreementEditDTO itemDTO, UsersAgreements entity) {

		if (itemDTO.getAnswer() != null) {
			entity.setAnswer(itemDTO.getAnswer());
			entity.setIsAnswered(true);
		} else {
			entity.setIsAnswered(false);
		}

		if (itemDTO.getOrganizationAgreement() != null && itemDTO.getOrganizationAgreement().getId() != null) {
			OrganizationsAgreements organizationAgreement = organizationAgreementService.getOrganizationAgreementForCurrentOrganization(itemDTO.getOrganizationAgreement().getId());
			organizationAgreement.getOrganizations().add(organizationService.getCurrentOrganizationEntity());
			organizationAgreementRepository.save(organizationAgreement);
			entity.setOrganizationAgreement(organizationAgreement);
		}

		entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes User Agreement
	 *
	 * @return ID of removed item
	 */
	public Long delete(Long itemId) {

		UsersAgreements existingItem = getUserAgreementForCurrentUser(itemId);
		UserAgreementEditDTO existingItemDTO = new UserAgreementEditDTO(existingItem);

		userAgreementRepository.delete(existingItem);
		userAgreementRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.USER_AGREEMENT,
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
	private AuditLogItemId[] collectAuditLogItems(UserAgreementEditDTO existingItemDTO, Long organizationId) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, organizationId)));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}
}
