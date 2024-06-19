package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.model.data.BusinessUnitFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitViewExtDTO;
import com.cyberintech.vrisk.server.service.admin.AdminBusinessUnitService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Business Unit management controller. Basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-02-06
 */
@RestController
@RequestMapping(
	value = AdminBusinessUnitController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Business Units Management Controller"
)
@Tags(value = {@Tag(name = "Admin Business Unit Management"), @Tag(name = "Administration")})
public class AdminBusinessUnitController {

	static final String CONTROLLER_URI = "/api/admin/business-units";

	@Autowired
	private AdminBusinessUnitService adminBusinessUnitService;

	/**
	 * Get Business Units List
	 *
	 * @return Business Units List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Get Business Units List")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_BUSINESS_UNIT_READ)")
	public List<BusinessUnitViewDTO> getList() {

		List<BusinessUnitViewDTO> result = adminBusinessUnitService.getList();

		return result;
	}

	/**
	 * Return Filtered list of Users
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Return Filtered list of Business Units", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_BUSINESS_UNIT_READ)")
	public FilteredResponse<BusinessUnitFilter, BusinessUnitViewExtDTO> filter(
		@Parameter(description = "Business Units Filtering", required = true) @RequestBody FilteredRequest<BusinessUnitFilter> filteredRequest
	) {

		FilteredResponse<BusinessUnitFilter, BusinessUnitViewExtDTO> result = adminBusinessUnitService.getAdminListFiltered(filteredRequest);

		return result;
	}
}
