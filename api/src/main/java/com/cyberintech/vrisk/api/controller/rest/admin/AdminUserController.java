package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.UsersFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.user.ExtendedUserEditDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserListAdminDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserListDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.service.admin.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
	value = AdminUserController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Users Management Controller"
)
@Tags(value = {@Tag(name = "Admin User Management Controller"), @Tag(name = "Administration")})
public class AdminUserController {

	static final String CONTROLLER_URI = "/api/admin/users";

	@Autowired
	private AdminUserService adminUserService;

	/**
	 * Get Users List
	 *
	 * @return Users List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Get Users List")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public List<UserListDTO> getList() {

		List<UserListDTO> result = adminUserService.getList();

		return result;
	}

	/**
	 * Return Filtered list of Users
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Return Filtered list of Users", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public FilteredResponse<UsersFilter, UserListAdminDTO> filter(@Parameter(description = "User Filtering", required = true) @RequestBody FilteredRequest<UsersFilter> filteredRequest) {

		FilteredResponse<UsersFilter, UserListAdminDTO> result = adminUserService.getAdminListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get User details
	 *
	 * @return User Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/self", name = "Get User details")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public UserDTO getSelf() {

		UserDetailsImpl user = adminUserService.getCurrentUser();
		Users itemDetails = adminUserService.getUser(user.getUserId());
		UserDTO itemDTO = new UserDTO(itemDetails);

		return itemDTO;
	}

	/**
	 * Reset TOTP details for User
	 *
	 * @return User Details
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/totp/reset/{itemId}", name = "Reset User TOTP details")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public ExtendedUserEditDTO resetTOTP(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {
		ExtendedUserEditDTO itemDTO = adminUserService.resetTOTP(itemId);

		return itemDTO;
	}

	/**
	 * Get User details
	 *
	 * @return User Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get User details")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public ExtendedUserEditDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		ExtendedUserEditDTO itemDTO = adminUserService.getUserDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new User
	 *
	 * @return New User
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new User", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public ExtendedUserEditDTO create(@Parameter(description = "User Details", required = true) @RequestBody ExtendedUserEditDTO newItemDTO) {

		ExtendedUserEditDTO result = adminUserService.create(newItemDTO);

		return result;
	}

	/**
	 * Update User
	 *
	 * @return New User
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing User", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public ExtendedUserEditDTO update(@Parameter(description = "User update Details", required = true) @RequestBody ExtendedUserEditDTO itemDTO) {

		ExtendedUserEditDTO result = adminUserService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes User
	 *
	 * @return ID of removed User
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing User", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public Long delete(@Parameter(description = "Simple User Details", required = true) @RequestBody ItemViewDTO itemDTO) {

		Long result = adminUserService.delete(itemDTO.getId());

		return result;
	}

}
