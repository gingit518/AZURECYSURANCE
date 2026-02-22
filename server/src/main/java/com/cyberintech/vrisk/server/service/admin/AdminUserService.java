package com.cyberintech.vrisk.server.service.admin;

import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.UserModelDAO;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.UsersFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.audit.items.UserAuditDTO;
import com.cyberintech.vrisk.server.model.dto.user.*;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.domains.TwoFactorType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.AuditLogService;
import com.cyberintech.vrisk.server.service.DocumentService;
import com.cyberintech.vrisk.server.service.UserService;
import com.cyberintech.vrisk.server.service.VendorService;
import com.cyberintech.vrisk.server.service.communication.EmailService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.*;

/**
 * User management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-19
 */
@Service
public class AdminUserService extends UserService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private DocumentsRepository documentsRepository;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BusinessUnitRepository businessUnitRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private VendorService vendorService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private IdpUserRepository idpUserRepository;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private UserModelDAO userModelDAO;
	@Autowired
	private ApiKeysRepository apiKeysRepository;

	/**
	 * Get Users List
	 *
	 * @return Users List
	 */
	@Override
	public List<UserListDTO> getList() {
		List<Users> items;

		items = userRepository.findAll();

		List<UserListDTO> usersDTOList = DTOBase.fromEntitiesList(items, UserListDTO.class);

		return usersDTOList;
	}

	/**
	 * Get Users List Filtered
	 *
	 * @return Users List
	 */
	public FilteredResponse<UsersFilter, UserListAdminDTO> getAdminListFiltered(FilteredRequest<UsersFilter> filteredRequest) {

		PagedResult<Users> pagedResult = userModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());

		// Convert to DTOs
		List<UserListAdminDTO> usersDTOList = DTOBase.fromEntitiesList(pagedResult.getItems(), UserListAdminDTO.class);

		FilteredResponse<UsersFilter, UserListAdminDTO> filteredResponse = new FilteredResponse<UsersFilter, UserListAdminDTO>(filteredRequest);
		filteredResponse.setItems(usersDTOList);
		filteredResponse.setTotal(pagedResult.getCount().intValue());

		return filteredResponse;
	}

	/**
	 * Get User details
	 *
	 * @return User Details
	 */
	public ExtendedUserEditDTO getUserDetails(Long itemId) {

		Users itemDetails = getUser(itemId);
		ExtendedUserEditDTO itemDTO = new ExtendedUserEditDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Reset TOTP details for User
	 *
	 * @return User Details
	 */
	public ExtendedUserEditDTO resetTOTP(Long itemId) {
		Users itemDetails = getUser(itemId);
		itemDetails.setIsTotpVerified(false);
		itemDetails.setTotpSecret(null);
		itemDetails = userRepository.save(itemDetails);

		ExtendedUserEditDTO itemDTO = new ExtendedUserEditDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Create new User
	 *
	 * @return New User
	 */
	public ExtendedUserEditDTO create(ExtendedUserEditDTO newItemDTO) {

		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()), ApplicationExceptionCodes.CREATE_IS_NOT_ALLOWED_FOR_ITEM_WITH_EXISTING_ID);
		}

		// Verify user with such email not exists
		if (userRepository.findFirstByEmailAndIdIsNotIn(newItemDTO.getEmail(), Arrays.asList(0l)).isPresent()) {
			throw new ConflictException(MessageFormat.format("User with this email already registered in the system [{0}]", newItemDTO.getEmail()), ApplicationExceptionCodes.USER_WITH_EMAIL_ALREADY_EXISTS);
		}

		Users newItem = new Users();

		applyEntityChanges(newItemDTO, newItem);

		Users saveResult = userRepository.save(newItem);

		// Send User Registration Email
		if (applicationProperties.isEmailNotificationsEnabled()) {
			emailService.sendUserRegistrationEmail(saveResult);
		}

		ExtendedUserEditDTO result = new ExtendedUserEditDTO(saveResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.USER,
			saveResult.getId(),
			new UserAuditDTO(saveResult),
			AuditLogItemId.of(VItemType.ORGANIZATION, saveResult.getOrganization().getId())
		);

		return result;
	}

	/**
	 * Update User
	 *
	 * @return New User
	 */
	public ExtendedUserEditDTO update(ExtendedUserEditDTO itemDTO) {

		ExtendedUserEditDTO result;

		try {

			// Verify user with such email exists
			if (userRepository.findFirstByEmailAndIdIsNotIn(itemDTO.getEmail(), Arrays.asList(itemDTO.getId())).isPresent()) {
				throw new ConflictException(MessageFormat.format("User with this email already registered in the system [{0}]", itemDTO.getEmail()), ApplicationExceptionCodes.USER_WITH_EMAIL_ALREADY_EXISTS);
			}

			// Get Existing item from the database
			Users existingItem = userRepository.findById(itemDTO.getId()).get();
			UserAuditDTO oldAuditValue = new UserAuditDTO(existingItem);

			// Update item details
			Users updatedItem = existingItem;

			// Set Idp User details
			if (itemDTO.getIdpUsers() != null) {
				Optional.ofNullable(itemDTO.getIdpUsers()).ifPresent(idpUserDTOList -> {
					updatedItem.setIdpUsers(new HashSet<>());
					itemDTO.getIdpUsers().stream().forEach(idpUserDTO -> {
						if (idpUserDTO.getId() != null) {
							IdpUsers idpUser = idpUserRepository.findById(idpUserDTO.getId()).get();
							idpUser.setUserIdentity(idpUserDTO.getUserIdentity());
							idpUser.setIdpId(idpUserDTO.getIdpId());
							updatedItem.getIdpUsers().add(idpUser);
						} else {
							Optional<IdpUsers> idpUserOptional = idpUserRepository.findFirstByUserIdentityIgnoreCaseAndIdpId(idpUserDTO.getUserIdentity(), idpUserDTO.getIdpId());
							IdpUsers idpUser = null;
							// Check on idpUser already assigned to some User
							if (idpUserOptional.isPresent()) {
								// if idpUser exists and assigned to some user -> throw error
								if (idpUserOptional.get().getUser() != null) {
									throw new ConflictException(MessageFormat.format("{0} account - {1} already assigned to another user.", idpUserDTO.getIdpId(), idpUserDTO.getUserIdentity()));
								} else {
									// if idpUser exists but not assigned to anyone -> just update it and assign it to user
									idpUser = idpUserOptional.get();
								}
							} else {
								// in this case idpUser doesn't exists -> create new one
								idpUser = new IdpUsers();
							}
							idpUser.setUserIdentity(idpUserDTO.getUserIdentity());
							idpUser.setIdpId(idpUserDTO.getIdpId());
							idpUser.setUser(updatedItem);
							IdpUsers savedItem = idpUserRepository.save(idpUser);
							updatedItem.getIdpUsers().add(savedItem);
						}
					});
				});
			}

			applyEntityChanges(itemDTO, updatedItem);

			if (!Boolean.TRUE.equals(updatedItem.getUseMultiFactorAuth()) && (updatedItem.getOrganization() == null || !Boolean.TRUE.equals(updatedItem.getOrganization().getUseMultiFactorAuth()))) {
				updatedItem.setIsTotpVerified(false);
			}

			// Save to the database
			Users saveResult = userRepository.save(updatedItem);

			result = new ExtendedUserEditDTO(saveResult);

			// Save Audit Log UPDATE event
			auditLogService.update(
				VItemType.USER,
				itemDTO.getId(),
				oldAuditValue,
				new UserAuditDTO(saveResult),
				AuditLogItemId.of(VItemType.ORGANIZATION, saveResult.getOrganization().getId())
			);

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("User not found in the system [{0}]", itemDTO.getId()), ApplicationExceptionCodes.USER_NOT_EXISTS);
		}

		return result;
	}

	/**
	 * Fill the set of entity relations
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(ExtendedUserEditDTO itemDTO, Users entity) {
		boolean isNewEntity = entity.getId() == null;

		entity.setFirstName(itemDTO.getFirstName());
		entity.setLastName(itemDTO.getLastName());
		entity.setEmail(itemDTO.getEmail());
		entity.setCorporatePhone(itemDTO.getCorporatePhone());
		entity.setMobilePhone(itemDTO.getMobilePhone());
		entity.setExpired(itemDTO.getExpired());
		entity.setEnabled(itemDTO.getEnabled());
		if (itemDTO.getTitle() != null) entity.setTitle(itemDTO.getTitle());
		if (itemDTO.getCredentialsExpired() != null) entity.setCredentialsExpired(itemDTO.getCredentialsExpired());
		entity.setCredentialsExpirationDate(itemDTO.getCredentialsExpirationDate());
		entity.setLocked(itemDTO.getLocked());
		entity.setExpirationDate(itemDTO.getExpirationDate());
		entity.setUseMultiFactorAuth(itemDTO.getUseMultiFactorAuth());

		// We may Change MultiFactor Auth type

		if (Boolean.TRUE.equals(entity.getUseMultiFactorAuth()) || (entity.getOrganization() != null && Boolean.TRUE.equals(entity.getOrganization().getUseMultiFactorAuth()))) {
			if (itemDTO.getTwoFactorType() != null && !itemDTO.getTwoFactorType().equals(TwoFactorType.NONE)) {
				entity.setTwoFactorType(itemDTO.getTwoFactorType());
			}
		}

		if (itemDTO.getOrganization() != null && itemDTO.getOrganization().getId() != null) {
			entity.setOrganization(organizationRepository.findById(itemDTO.getOrganization().getId()).orElse(null));
		}

		// Set Encoded Password if it is defined
		if (StringUtils.isNotEmpty(itemDTO.getPasswordPlain())) {
			entity.setPassword(passwordEncoder.encode(itemDTO.getPasswordPlain()));
			entity.setCredentialsExpired(false);
		} else if (isNewEntity) {
			entity.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
		}

		// Set Roles List from names
		final Boolean adminRoleEnabled = isSuperAdmin() ? true : hasRole(RoleType.ADMIN.role(), entity);
		if (itemDTO.getRoles() != null) {
			Optional.ofNullable(itemDTO.getRoles()).ifPresent(rolesList -> {
				entity.setRoles(new HashSet<>());
				itemDTO.getRoles().stream().filter(rolesItemViewDTO -> filterAdminRole(rolesItemViewDTO.getName(), adminRoleEnabled)).forEach(role -> {
					entity.getRoles().add(roleRepository.findById(role.getId()).get());
				});
			});
		} else {
			Optional.ofNullable(itemDTO.getRoleNames()).ifPresent(rolesList -> {
				entity.setRoles(new HashSet<>());
				itemDTO.getRoleNames().stream().filter(roleName -> filterAdminRole(roleName, adminRoleEnabled)).forEach(roleName -> {
					entity.getRoles().add(roleRepository.findOneByName(roleName));
				});
			});
		}

		// Get Business Unit
		if (itemDTO.getBusinessUnit() != null && itemDTO.getBusinessUnit().getId() != null) {
			BusinessUnits businessUnits = businessUnitRepository.findById(itemDTO.getBusinessUnit().getId()).get();
			entity.setBusinessUnit(businessUnits);
		}

		// Set Created/Updated info
		Users currentUser = getCurrentUserEntity();
		if (entity.getId() == null) {
			entity.setCreatedAt(new Date());
			entity.setCreatedBy(currentUser);
		}

		// Set Vendors
		if (isVendorEmployee(entity)) {
			Optional.ofNullable(itemDTO.getVendors()).ifPresent(vendors -> {
				entity.setVendors(new HashSet<>());
				vendors.stream().forEach(vendorRef -> {
					Organizations vendor = vendorService.getVendor(vendorRef.getId());
					if (vendor.getRootParent() != null && vendor.getRootParent().getId().equals(entity.getOrganization().getId())) {
						entity.getVendors().add(vendor);
					}
				});
			});
		} else {
			entity.setVendors(new HashSet<>());
		}

		// Manage Logo document
		if (itemDTO.getProfilePicture() != null) {
			Documents logoDocument = documentsRepository.findById(itemDTO.getProfilePicture().getId()).get();
			entity.setProfilePicture(logoDocument);
		} else if (Boolean.TRUE.equals(itemDTO.getRemoveProfilePicture())) {
			entity.setProfilePicture(null);
		}

		entity.setUpdatedAt(new Date());
		entity.setUpdatedBy(currentUser);

	}

	/**
	 * Deletes User
	 *
	 * @return ID of removed User
	 */
	public Long delete(UserUpdateDTO itemDTO) {



		Long result = delete(itemDTO.getId());

		return result;
	}

	/**
	 * Deletes User
	 *
	 * @return ID of removed User
	 */
	public Long delete(Long itemId) {

		Long result;

		try {
			// Get Existing item from the database
			Users existingItem = userRepository.findById(itemId).get();

			existingItem.setDeleted(true);

			// Delete item details
			userRepository.save(existingItem);

			// Save Audit Log DELETE event
			auditLogService.delete(
				VItemType.USER,
				itemId,
				new UserAuditDTO(existingItem),
				AuditLogItemId.of(VItemType.ORGANIZATION, existingItem.getOrganization().getId())
			);

			result = itemId;

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("User not found in the system [{0}]", itemId), ApplicationExceptionCodes.USER_NOT_EXISTS);
		}

		return result;
	}

	/**
	 * Create API Key for the User Account
	 *
	 * @return API Key
	 */
	@Transactional
	public APIKeyDTO createAPIKey(Long userAccountId, Long organizationId, Date expirationDate) {
		Users user = userRepository.findById(userAccountId).orElseThrow(() -> new ItemNotFoundException("User account not found"));

		Random RANDOM = new SecureRandom();
		RandomStringGenerator LETTERS_AND_DIGITS_GENERATOR = new RandomStringGenerator.Builder().withinRange(new char[]{'a', 'z'}, new char[]{'A', 'Z'}, new char[]{'0', '9'}).filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS).usingRandom(RANDOM::nextInt).get();

		ApiKeys entity = new ApiKeys();
		entity.setUser(user);
		entity.setOrganizationId(organizationId);
		entity.setApiKeyPublic("rqpk_" + LETTERS_AND_DIGITS_GENERATOR.generate(32));
		entity.setApiKeyPrivate("rqsk_" + LETTERS_AND_DIGITS_GENERATOR.generate(32));
		// entity.setStatus(APIKeyStatus.ACTIVE);
		entity.setCreatedAt(new Date());
		entity.setExpiredAt(expirationDate);
		entity = apiKeysRepository.save(entity);

		APIKeyDTO result = new APIKeyDTO(entity);

		return result;
	}

}
