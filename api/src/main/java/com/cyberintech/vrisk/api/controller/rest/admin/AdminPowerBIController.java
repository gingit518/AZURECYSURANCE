package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.role.RoleListDTO;
import com.cyberintech.vrisk.server.service.RoleService;
import com.cyberintech.vrisk.server.service.azure.PowerBIAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * Admin PowerBI Controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-24
 */
@RestController
@RequestMapping(
	value = AdminPowerBIController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Azure PowerBI Management Controller"
)
@Tag(name = "Admin Azure PowerBI Management")
public class AdminPowerBIController {

	static final String CONTROLLER_URI = "/api/admin/azure/powerbi";

	@Autowired
	private PowerBIAdminService powerBIAdminService;

	/**
	 * Get Roles List for current Risk Model
	 *
	 * @return Roles List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/load-capacities", name = "Azure PowerBI capacities list")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public String getCapacitiesList() {

		String result = "OK";

		return result;
	}

}
