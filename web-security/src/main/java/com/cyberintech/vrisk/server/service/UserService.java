package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.rest.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.NoSuchElementException;
import java.util.Set;
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
	private UserRepository userRepository;

	@Lazy
	@Autowired
	private OrganizationService organizationService;

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
