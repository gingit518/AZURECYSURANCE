package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.role.RoleListDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Roles;
import com.cyberintech.vrisk.server.repository.jpa.RoleRepository;
import com.cyberintech.vrisk.server.service.RoleService;
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

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple Roles controller. Used for Roles Listing
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-27
 */
@RestController
@RequestMapping(
	value = RoleController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Roles Management Controller"
)
@Tag(name = "Role Viewer")
public class RoleController {

	static final String CONTROLLER_URI = "/api/roles";

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RoleService roleService;

	@Autowired
	private UserService userService;

	/**
	 * Get Roles List
	 *
	 * @return Roles List
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Get Roles List")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<RoleListDTO> getList() {
		final Boolean isSuperAdmin = userService.isSuperAdmin();
		List<Roles> items = roleRepository.findAll().stream().filter(roles -> {
			boolean include = true;

			if (roles.getId() <= 8 && !isSuperAdmin) {
				include = false;
			}

			return include;
		}).collect(Collectors.toList());

		List<RoleListDTO> roleListDTOs = RoleListDTO.fromEntitiesList(items, RoleListDTO.class);

		return roleListDTOs;
	}

	/**
	 * Get Roles List for current Risk Model
	 *
	 * @return Roles List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Roles List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, RoleListDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, RoleListDTO> result = roleService.getListFiltered(filteredRequest);

		return result;
	}

}
