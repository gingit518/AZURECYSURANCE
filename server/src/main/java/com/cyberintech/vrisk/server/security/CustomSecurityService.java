package com.cyberintech.vrisk.server.security;

import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.service.PermissionService;
import com.cyberintech.vrisk.server.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Simple security service based on permission names
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-30
 */
@Service("apiSecurity")
public class CustomSecurityService {

	@Autowired
	private UserService userService;

	@Autowired
	private PermissionService permissionService;

	/**
	 * Check is user is Admin
	 *
	 * @return
	 */
	public boolean isSuperAdmin() {
		return userService.isSuperAdmin();
	}

	public boolean hasPermission(String action) {
		return hasPermission(action, true);
	}

	public boolean hasPermission(String action, Boolean throwException) {
		boolean isUserAllowed = permissionService.checkCurrentUserPermission(action);

		if (!isUserAllowed) {
			// Allow ANY Permissions for SUPER ADMIN
			if (userService.isSuperAdmin()) {
				return true;
			}

			if (throwException) {
				String username = userService.getCurrentUser() != null ? userService.getCurrentUser().getUsername() : "ANONYMOUS";
				throw new ForbiddenException("You are not allowed to access this resource: " + StringUtils.capitalize(action).replaceAll("_", " "), username);
			}
		}

		return isUserAllowed;
	}

	public boolean hasOneOfPermissions(String... actions) {
		Boolean result = false;

		for (String action: actions) {
			result = hasPermission(action, false);

			if (result) break;
		}

		if (!result) {
			String username = userService.getCurrentUser() != null ? userService.getCurrentUser().getUsername() : "ANONYMOUS";
			throw new ForbiddenException("You are not allowed to access this resource: " +
				String.join(", ", Arrays.stream(actions).map(action -> StringUtils.capitalize(action).replaceAll("_", " ")).collect(Collectors.toList())),
				username);
		}

		return result;
	}

	public boolean hasPermission(Authentication authentication, String action) {
		return true;
	}
}
