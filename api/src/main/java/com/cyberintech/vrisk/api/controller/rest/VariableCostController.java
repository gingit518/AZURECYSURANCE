package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.budget.VariableCostDTO;
import com.cyberintech.vrisk.server.service.VariableCostService;
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
 * Variable Costs management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-04
 */
@RestController
@RequestMapping(
	value = VariableCostController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Budgeting. Variable Costs Management Controller."
)
@Tag(name = "Variable Costs Management Controller.")
public class VariableCostController {

	static final String CONTROLLER_URI = "/api/budget/variable-costs";

	@Autowired
	private VariableCostService variableCostService;

	/**
	 * Get Variable Costs List for current Risk Model
	 *
	 * @return Variable Costs List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Variable Costs List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VARIABLE_COST_READ)")
	public FilteredResponse<NameFilter, VariableCostDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, VariableCostDTO> result = variableCostService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Variable Costs details
	 *
	 * @return Variable Costs Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Variable Costs details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VARIABLE_COST_READ)")
	public VariableCostDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		VariableCostDTO itemDTO = variableCostService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Variable Costs
	 *
	 * @return New Variable Costs
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Variable Costs", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VARIABLE_COST_CREATE)")
	public VariableCostDTO create(
		@Parameter(description = "Variable Costs Details", required = true) @RequestBody VariableCostDTO newItemDTO
	) {

		VariableCostDTO result = variableCostService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Variable Costs
	 *
	 * @return Updated Variable Costs
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Variable Costs", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VARIABLE_COST_UPDATE)")
	public VariableCostDTO update(
		@Parameter(description = "Variable Costs update Details", required = true) @RequestBody VariableCostDTO itemDTO
	) {

		VariableCostDTO result = variableCostService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Variable Costs
	 *
	 * @return ID of removed Variable Costs
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Variable Costs", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VARIABLE_COST_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Variable Costs Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = variableCostService.delete(itemDTO.getId());

		return result;
	}

}
