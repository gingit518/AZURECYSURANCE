package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.ByFrameworkFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.control_function.ControlFunctionEditDTO;
import com.cyberintech.vrisk.server.model.dto.control_function.ControlFunctionViewDTO;
import com.cyberintech.vrisk.server.service.ControlFunctionService;
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
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-09
 */
@RestController
@RequestMapping(
	value = ControlFunctionController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Control Functions Management Controller"
)
@Tag(name = "Control Functions Management")
public class ControlFunctionController {

	static final String CONTROLLER_URI = "/api/control-functions";

	@Autowired
	private ControlFunctionService controlFunctionService;

	/**
	 * Get Control Functions List for current Risk Model
	 *
	 * @return Control Functions List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Control Functions List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_FUNCTION_READ)")
	public FilteredResponse<ByFrameworkFilter, ControlFunctionViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<ByFrameworkFilter> filteredRequest
	) {

		FilteredResponse<ByFrameworkFilter, ControlFunctionViewDTO> result = controlFunctionService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Control Function details
	 *
	 * @return Control Function Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Control Function details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_FUNCTION_READ)")
	public ControlFunctionEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		ControlFunctionEditDTO itemDTO = controlFunctionService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Control Function
	 *
	 * @return New Control Function
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Control Function", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_FUNCTION_CREATE)")
	public ControlFunctionEditDTO create(
		@Parameter(description = "Control Function Details", required = true) @RequestBody ControlFunctionEditDTO newItemDTO
	) {

		ControlFunctionEditDTO result = controlFunctionService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Control Function
	 *
	 * @return Updated Control Function
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Control Function", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_FUNCTION_UPDATE)")
	public ControlFunctionEditDTO update(
		@Parameter(description = "Control Functions update Details", required = true) @RequestBody ControlFunctionEditDTO itemDTO
	) {

		ControlFunctionEditDTO result = controlFunctionService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Control Function
	 *
	 * @return ID of removed Control Function
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Control Function", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_FUNCTION_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Control Function Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = controlFunctionService.delete(itemDTO.getId());

		return result;
	}

}
