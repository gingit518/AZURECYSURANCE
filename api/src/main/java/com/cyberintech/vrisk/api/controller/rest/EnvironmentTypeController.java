package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology.EnvironmentTypesDTO;
import com.cyberintech.vrisk.server.service.EnvironmentTypesService;
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
 * Environment Type management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-21
 */
@RestController
@RequestMapping(
	value = EnvironmentTypeController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Environment Types Management Controller"
)
@Tag(name = "Environment Types Management")
public class EnvironmentTypeController {

	static final String CONTROLLER_URI = "/api/environment-types";

	@Autowired
	private EnvironmentTypesService EnvironmentTypesService;

	/**
	 * Get Environment Types List for current Risk Model
	 *
	 * @return Environment Types List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Environment Types List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ENVIRONMENT_TYPE_READ)")
	public FilteredResponse<NameFilter, EnvironmentTypesDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, EnvironmentTypesDTO> result = EnvironmentTypesService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Environment Type details
	 *
	 * @return Environment Type Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Environment Type details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ENVIRONMENT_TYPE_READ)")
	public EnvironmentTypesDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		EnvironmentTypesDTO itemDTO = EnvironmentTypesService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Environment Type
	 *
	 * @return New Environment Type
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Environment Type", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ENVIRONMENT_TYPE_CREATE)")
	public EnvironmentTypesDTO create(
		@Parameter(description = "Environment Type Details", required = true) @RequestBody EnvironmentTypesDTO newItemDTO
	) {

		EnvironmentTypesDTO result = EnvironmentTypesService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Environment Type
	 *
	 * @return Updated Environment Type
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Environment Type", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ENVIRONMENT_TYPE_UPDATE)")
	public EnvironmentTypesDTO update(
		@Parameter(description = "Environment Types update Details", required = true) @RequestBody EnvironmentTypesDTO itemDTO
	) {

		EnvironmentTypesDTO result = EnvironmentTypesService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Environment Type
	 *
	 * @return ID of removed Environment Type
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Environment Type", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ENVIRONMENT_TYPE_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Environment Type Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = EnvironmentTypesService.delete(itemDTO.getId());

		return result;
	}

}
