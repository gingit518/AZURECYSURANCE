package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.dao.UserModelDAO;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.UsersFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.audit.items.UserAuditDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.*;
import com.cyberintech.vrisk.server.model.jpa.domains.AuditOperationType;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.model.jpa.entity.Currency;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.rest.exception.*;
import com.cyberintech.vrisk.server.service.communication.EmailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User management Service. Implements basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-27
 */
@Service
public class UserService {

	@Autowired
	private AuditLogService auditLogService;

	@Lazy
	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private DocumentsRepository documentsRepository;

	@Autowired
	@Lazy
	private DocumentService documentService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Lazy
	@Autowired
	private OrganizationService organizationService;

	@Lazy
	@Autowired
	private EmailService emailService;

	@Lazy
	@Autowired
	private PermissionService permissionService;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Lazy
	@Autowired
	UserModelDAO userModelDAO;

	@Autowired
	UserRatesRepository userRatesRepository;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Lazy
	@Autowired
	private VendorService vendorService;

	/**
	 * Get Users List
	 *
	 * @return Users List
	 */
	public List<UserListDTO> getList() {
		List<Users> items;

		if (getCurrentUser().getOrganizationId() != null) {
			items = userRepository.getListByOrganization(getCurrentUser().getOrganizationId());
		} else {
			items = userRepository.findAll();
		}

		List<UserListDTO> usersDTOList = DTOBase.fromEntitiesList(items, UserListDTO.class);

		return usersDTOList;
	}

	/**
	 * Get Users List Filtered
	 *
	 * @return Users List
	 */
	public FilteredResponse<UsersFilter, UserListDTO> getListFiltered(FilteredRequest<UsersFilter> filteredRequest) {

		UsersFilter filter = filteredRequest.getFilter();
		filter.setOrganizationId(organizationService.getCurrentOrganizationId());
		// to prevent showing deleted users (Admin-only feature)
		filter.setIsDeleted(false);

		PagedResult<Users> pagedResult = userModelDAO.getItemsPageable(filter, filteredRequest.toPageRequest(), filteredRequest.getSort());

		// Convert to DTOs
		List<UserListDTO> usersDTOList = DTOBase.fromEntitiesList(pagedResult.getItems(), UserListDTO.class);

		FilteredResponse<UsersFilter, UserListDTO> filteredResponse = new FilteredResponse<UsersFilter, UserListDTO>(filteredRequest);
		filteredResponse.setItems(usersDTOList);
		filteredResponse.setTotal(pagedResult.getCount().intValue());

		return filteredResponse;
	}

	/**
	 * Get User details
	 *
	 * @return User Details
	 */
	public UserDTO getDetails(Long itemId) {

		Users itemDetails = getOrganizationUser(itemId);
		UserDTO itemDTO = new UserDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Get self User details
	 *
	 * @return User Details
	 */
	public UserDTO getSelf() {

		UserDetailsImpl user = getCurrentUser();
		Users itemDetails = getOrganizationUser(user.getUserId());
		UserDTO itemDTO = new UserDTO(itemDetails);

		// Trying to build Organization Details for the User
		Organizations organization = itemDetails.getOrganization();
		if (organization != null) {
			OrganizationRefDTO organizationRef = new OrganizationRefDTO(organization);
			if (organization.getLogoDocument() != null) {
				DocumentDTO organizationLogo = new DocumentDTO(organization.getLogoDocument(), true);
				organizationRef.setLogoDocument(organizationLogo);
			}
			itemDTO.setOrganization(organizationRef);
		}

		// Set User Permissions
		Set<String> permissions = permissionService.getUserPermissionNames(user.getUserId());
		itemDTO.setPermissions(permissions);

		return itemDTO;
	}

	/**
	 * Get User details
	 *
	 * @return User Details
	 */
	public Users getUser(Long itemId) {
		Users itemDetails;

		try {
			itemDetails = userRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("User not found in the database [{0}]", itemId), ApplicationExceptionCodes.USER_NOT_EXISTS);
		}

		return itemDetails;
	}

	/**
	 * Get details of User from current organization
	 *
	 * @return User Details
	 */
	public Users getOrganizationUser(Long itemId) {

		Users itemDetails = getUser(itemId);

		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganization().getId())) {
			throw new BadRequestException(MessageFormat.format("User [{0}] is not belongs to the current Organization [{1}].", itemDetails.getFullName(), organizationService.getCurrentOrganization().getName()), ApplicationExceptionCodes.USER_NOT_BELONGS_TO_CURRENT_ORGANIZATION);
		}

		return itemDetails;
	}

	/**
	 * Create new User
	 *
	 * @return New User
	 */
	public UserDTO create(UserUpdateDTO newItemDTO) {

		// Verify user with such email exists
		if (userRepository.findFirstByEmailAndIdIsNotIn(newItemDTO.getEmail(), Arrays.asList(0l)).isPresent()) {
			throw new ConflictException(MessageFormat.format("User with this email already registered in the system [{0}]", newItemDTO.getEmail()), ApplicationExceptionCodes.USER_WITH_EMAIL_ALREADY_EXISTS);
		}

		Users newItem = new Users();

		newItem.setCreatedBy(getCurrentUserEntity());
		newItem.setCreatedAt(new Date());

		// Set Organization for new User
		if (organizationService.getCurrentOrganizationId() != null) {
			newItem.setOrganization(organizationService.getOrganization(organizationService.getCurrentOrganizationId()));
		}

		newItem.setMobilePhone(newItemDTO.getMobilePhone());

		applyEntityChanges(newItemDTO, newItem);

		Users saveResult = userRepository.save(newItem);

		// Send User Registration Email
		if (applicationProperties.isEmailNotificationsEnabled()) {
			emailService.sendUserRegistrationEmail(saveResult);
		}

		UserDTO result = new UserDTO(saveResult);

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
	public UserDTO update(UserUpdateDTO itemDTO) {

		UserDTO result;

		try {

			// Verify user with such email exists
			if (userRepository.findFirstByEmailAndIdIsNotIn(itemDTO.getEmail(), Arrays.asList(itemDTO.getId())).isPresent()) {
				throw new ConflictException(MessageFormat.format("User with this email already registered in the system [{0}]", itemDTO.getEmail()), ApplicationExceptionCodes.USER_WITH_EMAIL_ALREADY_EXISTS);
			}

			// Get Existing item from the database
			Users existingItem = getOrganizationUser(itemDTO.getId());
			UserAuditDTO oldAuditValue = new UserAuditDTO(existingItem);

			// Update item details
			Users updatedItem = existingItem;

			updatedItem.setMobilePhone(itemDTO.getMobilePhone());

			applyEntityChanges(itemDTO, updatedItem);

			// Save to the database
			Users saveResult = userRepository.save(updatedItem);

			result = new UserDTO(saveResult);

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
	 * Update User
	 *
	 * @return New User
	 */
	public UserHRDataDTO update(UserHRDataDTO itemDTO) {

		UserHRDataDTO result;

		try {

			// Verify user with such email exists
			if (userRepository.findFirstByEmailAndIdIsNotIn(itemDTO.getEmail(), Arrays.asList(itemDTO.getId())).isPresent()) {
				throw new ConflictException(MessageFormat.format("User with this email already registered in the system [{0}]", itemDTO.getEmail()), ApplicationExceptionCodes.USER_WITH_EMAIL_ALREADY_EXISTS);
			}

			// Get Existing item from the database
			Users existingItem = getOrganizationUser(itemDTO.getId());
			final Long organizationId = organizationService.getCurrentOrganizationId();
			UserAuditDTO oldAuditValue = new UserAuditDTO(existingItem);

			// Update item details
			Users updatedItem = existingItem;
			updatedItem.setEmploymentType(itemDTO.getEmploymentType());

			// Set Roles List from objects or names
			if (itemDTO.getUserRates() != null) {
				updatedItem.setUserRates(new HashSet<>());
				itemDTO.getUserRates().stream().forEach(userRateDTO -> {
					UserRates userRate = null;
					if (userRateDTO.getId() != null) {
						Optional<UserRates> userRateOpt = userRatesRepository.findOneByIdAndOrganizationId(userRateDTO.getId(), organizationId);
						if (userRateOpt.isPresent()) {
							userRate = userRateOpt.get();
						}
					} else {
						userRate = new UserRates();
						userRate.setOrganizationId(organizationId);
					}

					if (userRate != null) {

						Currency currency = null;
						if (userRateDTO.getCurrency() != null && userRateDTO.getCurrency().getId() != null) {
							Optional<Currency> currencyOpt = currencyRepository.findById(userRateDTO.getCurrency().getId());
							if (currencyOpt.isPresent()) {
								currency = currencyOpt.get();
								userRate.setCurrency(currency);
							}
						}

						userRate.setRate(userRateDTO.getRate());
						userRate.setRateType(userRateDTO.getRateType());
						userRate.setUser(existingItem);
						userRate.setStartDate(userRateDTO.getStartDate());
						userRate.setEndDate(userRateDTO.getEndDate());
						userRate = userRatesRepository.save(userRate);

						updatedItem.getUserRates().add(userRate);
					}
				});
			}

			// Save to the database
			Users saveResult = userRepository.save(updatedItem);

			result = new UserHRDataDTO(saveResult);

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
	 * Update User Profile
	 *
	 * @return Updated User
	 */
	public UserDTO updateProfile(UserUpdateDTO itemDTO) {

		UserDTO result;

		// Verify user with such email exists
		if (userRepository.findFirstByEmailAndIdIsNotIn(itemDTO.getEmail(), Arrays.asList(itemDTO.getId())).isPresent()) {
			throw new ConflictException(MessageFormat.format("User with this email already registered in the system [{0}]", itemDTO.getEmail()), ApplicationExceptionCodes.USER_WITH_EMAIL_ALREADY_EXISTS);
		}

		// Get Existing item from the database
		Users currentUser = getCurrentUserEntity();
		UserAuditDTO oldAuditValue = new UserAuditDTO(currentUser);

		// Update item details
		Users updatedItem = currentUser;
		updatedItem.setFirstName(itemDTO.getFirstName());
		updatedItem.setLastName(itemDTO.getLastName());
		if (itemDTO.getCorporatePhone() != null) updatedItem.setCorporatePhone(itemDTO.getCorporatePhone());
		if (itemDTO.getMobilePhone() != null && StringUtils.isEmpty(updatedItem.getMobilePhone())) updatedItem.setMobilePhone(itemDTO.getMobilePhone());

		if (itemDTO.getTitle() != null) updatedItem.setTitle(itemDTO.getTitle());
		if (itemDTO.getBusinessUnit() != null && itemDTO.getBusinessUnit().getId() != null) {
			updatedItem.setBusinessUnit(businessUnitService.getBusinessUnitForCurrentOrganization(itemDTO.getBusinessUnit().getId()));
		}

		// Manage Logo document
		if (itemDTO.getProfilePicture() != null) {
			Documents logoDocument = documentsRepository.findById(itemDTO.getProfilePicture().getId()).get();
			updatedItem.setProfilePicture(logoDocument);
		} else if (Boolean.TRUE.equals(itemDTO.getRemoveProfilePicture())) {
			updatedItem.setProfilePicture(null);
		}

		updatedItem.setUpdatedBy(getCurrentUserEntity());
		updatedItem.setUpdatedAt(new Date());

		// Save to the database
		Users saveResult = userRepository.save(updatedItem);

		result = new UserDTO(saveResult);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.USER,
			itemDTO.getId(),
			oldAuditValue,
			new UserAuditDTO(saveResult),
			AuditLogItemId.of(VItemType.ORGANIZATION, saveResult.getOrganization().getId())
		);

		return result;
	}


	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	protected void applyEntityChanges(UserUpdateDTO itemDTO, Users entity) {

		entity.setFirstName(itemDTO.getFirstName());
		entity.setLastName(itemDTO.getLastName());
		entity.setEmail(itemDTO.getEmail());
		entity.setCorporatePhone(itemDTO.getCorporatePhone());
		entity.setExpired(itemDTO.getExpired());
		entity.setEnabled(itemDTO.getEnabled());
		entity.setTitle(itemDTO.getTitle());
		if (itemDTO.getCredentialsExpired() != null) entity.setCredentialsExpired(itemDTO.getCredentialsExpired());
		entity.setCredentialsExpirationDate(itemDTO.getCredentialsExpirationDate());
		entity.setLocked(itemDTO.getLocked());
		entity.setExpirationDate(itemDTO.getExpirationDate());
		entity.setUseMultiFactorAuth(itemDTO.getUseMultiFactorAuth());

		// Set Encoded Password if it is defined
		if (StringUtils.isNotEmpty(itemDTO.getPasswordPlain())) {
			entity.setPassword(passwordEncoder.encode(itemDTO.getPasswordPlain()));
			entity.setCredentialsExpired(false);
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

		if (itemDTO.getBusinessUnit() != null && itemDTO.getBusinessUnit().getId() != null) {
			BusinessUnits businessUnit = businessUnitService.getBusinessUnitForCurrentOrganization(itemDTO.getBusinessUnit().getId());
			entity.setBusinessUnit(businessUnit);
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
		}

		// Manage Logo document
		if (itemDTO.getProfilePicture() != null) {
			Documents logoDocument = documentsRepository.findById(itemDTO.getProfilePicture().getId()).get();
			entity.setProfilePicture(logoDocument);
		} else if (Boolean.TRUE.equals(itemDTO.getRemoveProfilePicture())) {
			entity.setProfilePicture(null);
		}

		entity.setUpdatedBy(getCurrentUserEntity());
		entity.setUpdatedAt(new Date());
	}


	/**
	 * Send forget password email
	 *
	 * @param forgetPasswordDTO
	 * @return
	 */
	public UserDTO sendResetPasswordEmail(ForgetPasswordDTO forgetPasswordDTO) {
		Users user = userRepository.findFirstByEmailIgnoreCase(forgetPasswordDTO.getEmail())
			.orElseThrow(() -> new ItemNotFoundException(MessageFormat.format("User with this email is not found [{0}]", forgetPasswordDTO.getEmail()), ApplicationExceptionCodes.USER_WITH_EMAIL_NOT_EXISTS));
		emailService.sendResetPasswordEmail(user);

		return new UserDTO(user);
	}


	/**
	 * Expire credentials for all users with Default password
	 *
	 * @return
	 */
	@Transactional
	public List<UserRefDTO> expireUserCredentialsWithDefaultPassword() {
		Long organizationId = organizationService.getCurrentOrganizationId();

		List<UserRefDTO> result = new ArrayList<>();

		List<Users> usersList = userRepository.getListByOrganization(organizationId);
		for (Users user : usersList) {
			if (comparePasswords("password", user.getPassword())) {
				user.setCredentialsExpired(true);
				Users saveResult = userRepository.save(user);
				//TODO Should we add saveResult to the list instead?
				result.add(new UserRefDTO(user));
			}
		}

		return result;
	}

	/**
	 * Change password for user
	 *
	 * @param user
	 * @param password
	 * @return
	 */
	public Users changePassword(Users user, String password) {
		user.setPassword(passwordEncoder.encode(password));
		user.setCredentialsExpired(false);
		user.setUpdatedAt(new Date());
		user.setUpdatedBy(user);
		Users result = userRepository.save(user);

		// There is no organization passed in case of call from ADMIN api
		// Save Audit Log UPDATE event
		if (user.getOrganization() != null) {
			auditLogService.audit(
				AuditOperationType.UPDATE,
				VItemType.USER_PASSWORD_CHANGED,
				user.getId(),
				new UserAuditDTO(user),
				new UserAuditDTO(user),
				user,
				AuditLogItemId.of(VItemType.ORGANIZATION, user.getOrganization().getId())
			);
		} else {
			auditLogService.audit(
				AuditOperationType.UPDATE,
				VItemType.USER_PASSWORD_CHANGED,
				user.getId(),
				new UserAuditDTO(user),
				new UserAuditDTO(user),
				user
			);
		}


		return result;
	}

	/**
	 * Update users last login date
	 *
	 * @param userId
	 * @return
	 */
	@Transactional
	public Users updateLastLoginDate(Long userId) {

		Users user = userRepository.findById(userId).get();
		user.setLastLoginDate(new Date());
		Users result = userRepository.save(user);

		return result;
	}

	/**
	 * Verify passwords
	 *
	 * @param rawPassword
	 * @param encodedPassword
	 * @return
	 */
	public boolean comparePasswords(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
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
			Users existingItem = getOrganizationUser(itemId);

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
	 * Check is Admin role enabled for the user
	 *
	 * @param roleName
	 * @param adminRoleEnabled
	 * @return
	 */
	protected boolean filterAdminRole(String roleName, boolean adminRoleEnabled) {
		if (roleName.equalsIgnoreCase(RoleType.ADMIN.role())) {
			if (adminRoleEnabled) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Check is current user Authorized
	 *
	 * @return
	 */
	public boolean isAuthorized() {
		Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Boolean result = false;

		// Verify current User Details
		if (user != null && user instanceof UserDetailsImpl) {
			result = true;
		}

		return result;
	}

	/**
	 * Check is specified user has role
	 *
	 * @return
	 */
	public static boolean hasRole(String roleName, Users user) {
		if (user == null && roleName == null) {
			return false;
		}

		// Get all user names set
		Set<String> roleNamesLowerSet = user.getRoles().stream().map(role -> role.getName().toLowerCase()).collect(Collectors.toSet());

		// Check is defined role in the list
		boolean result = roleNamesLowerSet.contains(roleName.toLowerCase());

		return result;
	}

	/**
	 * Check is current user has role
	 *
	 * @return
	 */
	public boolean hasRole(String roleName) {
		UserDetailsImpl user = getCurrentUser();

		boolean result = user.getAuthorities().contains(new SimpleGrantedAuthority(roleName));

		return result;
	}

	/**
	 * Check is current user has role
	 *
	 * @return
	 */
	public boolean hasRole(RoleType role) {
		return hasRole(role.role());
	}

	/**
	 * Check is user Super Admin
	 *
	 * @return
	 */
	public boolean isSuperAdmin() {
		boolean result = false;

		if (hasRole(RoleType.ADMIN)) {
			result = true;
		}

		return result;
	}

	/**
	 * Check is user Vendor Emplyee
	 *
	 * @return
	 */
	public boolean isVendorEmployee() {
		boolean result = false;

		if (hasRole(RoleType.VENDOR_EMPLOYEE) || hasRole(RoleType.VENDOR_ADMIN_EXTERNAL)) {
			result = true;
		}

		return result;
	}

	/**
	 * Check is user Vendor Emplyee
	 *
	 * @return
	 */
	public static boolean isVendorEmployee(Users user) {
		boolean result = false;

		if (hasRole(RoleType.VENDOR_EMPLOYEE.toString(), user) || hasRole(RoleType.VENDOR_ADMIN_EXTERNAL.toString(), user)) {
			result = true;
		}

		return result;
	}

	/**
	 * Find user by email and full name. Email has priority.
	 *
	 * @param email    User's email.
	 * @param fullName User's full name.
	 * @return {@linkplain UserRefDTO} or {@code null} if user doesn't exist
	 */
	public UserRefDTO findByEmailOrFullName(String email, String fullName) {
		UserRefDTO user = null;
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		Optional<Users> userDetails = Optional.empty();

		if (StringUtils.isNotEmpty(email)) {
			userDetails = userRepository
				.findFirstByEmailAndOrganization(email, organization);
			if (!userDetails.isPresent()) {
				userDetails = userRepository.findFirstByEmailIgnoreCaseAndOrganization(email,
					organization);
			}
		}
		if (!userDetails.isPresent()) {
			userDetails = userRepository.findFirstByFullNameAndOrganization(fullName,
				organization);
		}
		if (userDetails.isPresent()) {
			user = new UserRefDTO(userDetails.get());
		}
		return user;
	}

	/**
	 * Get Current Security User
	 *
	 * @return
	 */
	public UserDetailsImpl getCurrentUser() {
		Object securityUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UserDetailsImpl user = null;

		// Initialize current User Details
		if (securityUser != null && securityUser instanceof UserDetailsImpl) {
			user = (UserDetailsImpl) securityUser;
		}

		if (user == null) {
			throw new NotAuthenticatedException("User is not Authorized on this server.");
		}

		return user;
	}

	/**
	 * Get Current JPA User Entity
	 *
	 * @return
	 */
	public Users getCurrentUserEntity() {
		UserDetailsImpl user = getCurrentUser();

		return getUser(user.getUserId());
	}

}
