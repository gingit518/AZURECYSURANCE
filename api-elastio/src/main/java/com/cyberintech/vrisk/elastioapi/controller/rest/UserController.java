package com.cyberintech.vrisk.elastioapi.controller.rest;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.UsersFilter;
import com.cyberintech.vrisk.server.model.dto.menu_items.MenuItemsDTO;
import com.cyberintech.vrisk.server.model.dto.user.*;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.security.SecurityProfile;
import com.cyberintech.vrisk.server.service.MenuItemsService;
import com.cyberintech.vrisk.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import java.util.List;


/**
 * User management controller. Basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-27
 */
@RestController
@RequestMapping(
	value = UserController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Users Controller"
)
@Tag(name = "Users Info")
public class UserController {

	static final String CONTROLLER_URI = "/api/users";

	@Autowired
	private UserService userService;

	@Autowired
	private MenuItemsService menuItemsService;

	/**
	 * Get User details
	 *
	 * @return User Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/self", name = "Get User details")
	@Operation(security = {@SecurityRequirement(name = SecurityProfile.AUTHORIZATION_SCHEME_API_KEY)})
	public UserDTO getSelf() {

		UserDTO itemDTO = userService.getSelf();

		return itemDTO;
	}

	/**
	 * Ping User details
	 *
	 * @return User Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/ping", name = "Ping current user")
	@Operation(security = {@SecurityRequirement(name = SecurityProfile.AUTHORIZATION_SCHEME_API_KEY)})
	public UserRefDTO pingUser() {

		UserDetailsImpl userDetails = userService.getCurrentUser();

		// Get user reference
		UserRefDTO result = new UserRefDTO();
		result.setId(userDetails.getUserId());
		result.setEmail(userDetails.getUsername());

		return result;
	}

}
