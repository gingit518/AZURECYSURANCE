package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.budget.FixedCapitalCostDTO;
import com.cyberintech.vrisk.server.service.FixedCapitalCostService;
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
 * Fixed Capital Costs management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-04
 */
@RestController
@RequestMapping(
	value = FixedCapitalCostController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Budgeting. Fixed Capital Costs Management Controller."
)
@Tag(name = "Fixed Capital Costs Management Controller.")
public class FixedCapitalCostController {

	static final String CONTROLLER_URI = "/api/budget/fixed-capital-costs";

	@Autowired
	private FixedCapitalCostService fixedCapitalCostService;

	/**
	 * Get Fixed Capital Costs List for current Risk Model
	 *
	 * @return Fixed Capital Costs List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Fixed Capital Costs List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).FIXED_CAPITAL_COST_READ)")
	public FilteredResponse<NameFilter, FixedCapitalCostDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, FixedCapitalCostDTO> result = fixedCapitalCostService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Fixed Capital Costs details
	 *
	 * @return Fixed Capital Costs Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Fixed Capital Costs details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).FIXED_CAPITAL_COST_READ)")
	public FixedCapitalCostDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		FixedCapitalCostDTO itemDTO = fixedCapitalCostService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Fixed Capital Costs
	 *
	 * @return New Fixed Capital Costs
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Fixed Capital Costs", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).FIXED_CAPITAL_COST_CREATE)")
	public FixedCapitalCostDTO create(
		@Parameter(description = "Fixed Capital Costs Details", required = true) @RequestBody FixedCapitalCostDTO newItemDTO
	) {

		FixedCapitalCostDTO result = fixedCapitalCostService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Fixed Capital Costs
	 *
	 * @return Updated Fixed Capital Costs
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Fixed Capital Costs", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).FIXED_CAPITAL_COST_UPDATE)")
	public FixedCapitalCostDTO update(
		@Parameter(description = "Fixed Capital Costs update Details", required = true) @RequestBody FixedCapitalCostDTO itemDTO
	) {

		FixedCapitalCostDTO result = fixedCapitalCostService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Fixed Capital Costs
	 *
	 * @return ID of removed Fixed Capital Costs
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Fixed Capital Costs", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).FIXED_CAPITAL_COST_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Fixed Capital Costs Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = fixedCapitalCostService.delete(itemDTO.getId());

		return result;
	}

}
