package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.UsersFilter;
import com.cyberintech.vrisk.server.model.dto.menu_items.MenuItemsDTO;
import com.cyberintech.vrisk.server.model.dto.user.*;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.service.MenuItemsService;
import com.cyberintech.vrisk.server.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
	name = "Users Management Controller"
)
@Tag(name = "User Management")
public class UserController {

	static final String CONTROLLER_URI = "/api/users";

	@Autowired
	private UserService userService;

	@Autowired
	private MenuItemsService menuItemsService;

	/**
	 * Get Users List
	 *
	 * @return Users List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/expire-credentials", name = "Service method to expire creds")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<UserRefDTO> expireUserCredentialsWithDefaultPassword() {

		List<UserRefDTO> result = userService.expireUserCredentialsWithDefaultPassword();

		return result;
	}

	/**
	 * Get Users List
	 *
	 * @return Users List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Get Users List")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).USER_READ)")
	public List<UserListDTO> getList() {

		List<UserListDTO> result = userService.getList();

		return result;
	}

	/**
	 * Return Filtered list of Users
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Return Filtered list of Users", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasOneOfPermissions(" +
		"T(com.cyberintech.vrisk.api.config.APIAction).USER_READ" +
		", T(com.cyberintech.vrisk.api.config.APIAction).USER_SETTING_READ" +
		", T(com.cyberintech.vrisk.api.config.APIAction).VENDOR_UPDATE" +
		")"
	)
	public FilteredResponse<UsersFilter, UserListDTO> filter(@Parameter(description = "User Filtering", required = true) @RequestBody FilteredRequest<UsersFilter> filteredRequest) {

		FilteredResponse<UsersFilter, UserListDTO> result = userService.getListFiltered(filteredRequest);

		return result;
	}

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

		UserDTO itemDTO = userService.getSelf();

		userService.updateLastLoginDate(itemDTO.getId());

		return itemDTO;
	}

	/**
	 * Ping User details
	 *
	 * @return User Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/ping", name = "Ping current user")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public UserRefDTO pingUser() {

		UserDetailsImpl userDetails = userService.getCurrentUser();

		// Get user reference
		UserRefDTO result = new UserRefDTO();
		result.setId(userDetails.getUserId());
		result.setEmail(userDetails.getUsername());

		return result;
	}

	/**
	 * Get User Menu
	 *
	 * @return User Menu
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/menu", name = "Get User Menu")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<MenuItemsDTO> getSelfMenu() {

		List<MenuItemsDTO> result = menuItemsService.getSelfMenu();

		return result;
	}

	/**
	 * Get User details
	 *
	 * @return User Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get User details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).USER_UPDATE)")
	public UserDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		UserDTO itemDTO = userService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Get User details
	 *
	 * @return User Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/hr/{itemId}", name = "Get User HR details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).USER_UPDATE)")
	public UserHRDataDTO getHrDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		Users itemDetails = userService.getOrganizationUser(itemId);
		UserHRDataDTO itemDTO = new UserHRDataDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Create new User
	 *
	 * @return New User
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new User", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).USER_CREATE)")
	public UserDTO create(@Parameter(description = "User Details", required = true) @RequestBody @Valid UserUpdateDTO newItemDTO) {

		UserDTO result = userService.create(newItemDTO);

		return result;
	}

	/**
	 * Update User
	 *
	 * @return New User
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing User", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasOneOfPermissions(T(com.cyberintech.vrisk.api.config.APIAction).USER_UPDATE, T(com.cyberintech.vrisk.api.config.APIAction).USER_SETTING_UPDATE)")
	public UserDTO update(@Parameter(description = "User update Details", required = true) @RequestBody @Valid UserUpdateDTO itemDTO) {

		UserDTO result = userService.update(itemDTO);

		return result;
	}

	/**
	 * Update User
	 *
	 * @return New User
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/hr", name = "Update existing User", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).USER_UPDATE)")
	public UserHRDataDTO updateHR(@Parameter(description = "User update Details", required = true) @RequestBody UserHRDataDTO itemDTO) {

		UserHRDataDTO result = userService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes User
	 *
	 * @return ID of removed User
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing User", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).USER_DELETE)")
	public Long delete(@Parameter(description = "Simple User Details", required = true) @RequestBody UserUpdateDTO itemDTO) {

		Long result = userService.delete(itemDTO);

		return result;
	}

}
