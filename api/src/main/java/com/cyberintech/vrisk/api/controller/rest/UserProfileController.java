package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.dto.user.ChangePasswordDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserUpdateDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;


/**
 * User profile controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-18
 */
@RestController
@RequestMapping(
	value = UserProfileController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Users Profile Controller"
)
@Tag(name = "User Profile")
public class UserProfileController {

	static final String CONTROLLER_URI = "/api/user-profile";

	@Autowired
	private UserService userService;

	/**
	 * Get User details
	 *
	 * @return User Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/self", name = "Get User details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public UserDTO getSelf() {

		UserDetailsImpl user = userService.getCurrentUser();
		UserDTO itemDTO = userService.getDetails(user.getUserId());

		return itemDTO;
	}

	/**
	 * Change current User password
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/change-password", name = "Change current User password", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Boolean changePassword(@Parameter(description = "User Details", required = true) @RequestBody ChangePasswordDTO itemDTO) {

		Users currentUser = userService.getCurrentUserEntity();

		// Compare password
		if (!userService.comparePasswords(itemDTO.getOldPassword(), currentUser.getPassword())) {
			throw new InternalServerErrorException("Old password doesn't match stored password", ApplicationExceptionCodes.PASSWORD_DOESNT_MATCH);
		}

		userService.changePassword(currentUser, itemDTO.getPassword());

		return true;
	}

	/**
	 * Update current User details
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/update", name = "Update current User details", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public UserDTO updateProfile(@Parameter(description = "User Details", required = true) @RequestBody UserUpdateDTO itemDTO) {
		return userService.updateProfile(itemDTO);
	}

}
