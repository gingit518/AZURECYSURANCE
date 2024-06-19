package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantsEditDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantsViewDTO;
import com.cyberintech.vrisk.server.model.dto.role.RoleListDTO;
import com.cyberintech.vrisk.server.service.RiskModelCalculationsService;
import com.cyberintech.vrisk.server.service.RoleService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Quants management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-17
 */
@RestController
@RequestMapping(
	value = AdminRoleController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Roles Management Controller"
)
@Tag(name = "Admin Roles Management")
public class AdminRoleController {

	static final String CONTROLLER_URI = "/api/admin/roles";

	@Autowired
	private RoleService roleService;

	/**
	 * Get Roles List for current Risk Model
	 *
	 * @return Roles List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Roles List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public FilteredResponse<NameFilter, RoleListDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, RoleListDTO> result = roleService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Deletes Role
	 *
	 * @return ID of removed Role
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Role", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public Long delete(
		@Parameter(description = "Simple Quant Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = roleService.delete(itemDTO.getId());

		return result;
	}

}
