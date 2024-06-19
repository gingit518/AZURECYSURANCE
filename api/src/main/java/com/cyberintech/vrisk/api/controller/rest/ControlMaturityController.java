package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.ControlMaturityEditDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.ControlMaturityViewDTO;
import com.cyberintech.vrisk.server.service.ControlMaturityService;
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
 * Control Functions management controller. Basic risk model CRUD.
 *
 * @author    Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version   0.1.0
 * @since     2020-07-22
 */
@RestController
@RequestMapping(
	value = ControlMaturityController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Control Maturities Management Controller"
)
@Tag(name = "Control Maturities Management")
public class ControlMaturityController {

	static final String CONTROLLER_URI = "/api/control-maturities";

	@Autowired
	private ControlMaturityService controlMaturityService;

	/**
	 * Get Control Maturities List for current Organization
	 *
	 * @return Control Maturities List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Control Maturities List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_MATURITY_READ)")
	public FilteredResponse<NameFilter, ControlMaturityViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {
		FilteredResponse<NameFilter, ControlMaturityViewDTO> result = controlMaturityService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Control Maturity details
	 *
	 * @return Control Maturity Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Control Maturity details")
	@Parameters(
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	)
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_MATURITY_READ)")
	public ControlMaturityEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {
		ControlMaturityEditDTO itemDTO = controlMaturityService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Control Maturity
	 *
	 * @return New Control Maturity
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Control Maturity", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_MATURITY_CREATE)")
	public ControlMaturityEditDTO create(
		@Parameter(description = "Control Maturity Details", required = true) @RequestBody ControlMaturityEditDTO newItemDTO
	) {
		ControlMaturityEditDTO result = controlMaturityService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Control Maturity
	 *
	 * @return Updated Control Maturity
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Control Maturity", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_MATURITY_UPDATE)")
	public ControlMaturityEditDTO update(
		@Parameter(description = "Control Maturity update Details", required = true) @RequestBody ControlMaturityEditDTO itemDTO
	) {
		ControlMaturityEditDTO result = controlMaturityService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Control Maturity
	 *
	 * @return ID of removed Control Maturity
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Control Maturity", consumes = {MediaType.APPLICATION_JSON})
	@Parameters(
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	)
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_MATURITY_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Control Function Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {
		Long result = controlMaturityService.delete(itemDTO.getId());

		return result;
	}

}

