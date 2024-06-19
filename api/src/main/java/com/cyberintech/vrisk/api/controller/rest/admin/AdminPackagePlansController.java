package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.PackagePlansDTO;
import com.cyberintech.vrisk.server.service.PackagePlansService;
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

/**
 * Package Plans management controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-07-22
 */
@RestController
@RequestMapping(
	value = AdminPackagePlansController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Package Plans Management Controller"
)
@Tag(name = "Package Plans Management")
public class AdminPackagePlansController {

	static final String CONTROLLER_URI = "/api/admin/package-plans";

	@Autowired
	private PackagePlansService packagePlansService;

	/**
	 * Get Package Plans List for current Risk Model
	 *
	 * @return Package Plans List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Package Plans List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public FilteredResponse<NameFilter, PackagePlansDTO> getListFiltered(
		@Parameter(name = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, PackagePlansDTO> result = packagePlansService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Package Plan details
	 *
	 * @return Package Plan Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Package Plan details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public PackagePlansDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		PackagePlansDTO itemDTO = packagePlansService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Package Plan
	 *
	 * @return New Package Plan
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Package Plan", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public PackagePlansDTO create(
		@Parameter(name = "Package Plan Details", required = true) @RequestBody PackagePlansDTO newItemDTO
	) {

		PackagePlansDTO result = packagePlansService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Package Plan
	 *
	 * @return Updated Package Plan
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Package Plan", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public PackagePlansDTO update(
		@Parameter(name = "Package Plans update Details", required = true) @RequestBody PackagePlansDTO itemDTO
	) {

		PackagePlansDTO result = packagePlansService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Package Plan
	 *
	 * @return ID of removed Package Plan
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Package Plan", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public Long delete(
		@Parameter(name = "Simple Package Plan Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = packagePlansService.delete(itemDTO.getId());

		return result;
	}

}
