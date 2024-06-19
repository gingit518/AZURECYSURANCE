package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.budget.FixedOperationalCostDTO;
import com.cyberintech.vrisk.server.service.FixedOperationalCostService;
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
 * Fixed Operational Costs management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-04
 */
@RestController
@RequestMapping(
	value = FixedOperationalCostController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Budgeting. Fixed Operational Costs Management Controller."
)
@Tag(name = "Fixed Operational Costs Management Controller.")
public class FixedOperationalCostController {

	static final String CONTROLLER_URI = "/api/budget/fixed-operational-costs";

	@Autowired
	private FixedOperationalCostService fixedOperationalCostService;

	/**
	 * Get Fixed Operational Costs List for current Risk Model
	 *
	 * @return Fixed Operational Costs List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Fixed Operational Costs List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).FIXED_OPERATIONAL_COST_READ)")
	public FilteredResponse<NameFilter, FixedOperationalCostDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, FixedOperationalCostDTO> result = fixedOperationalCostService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Fixed Operational Costs details
	 *
	 * @return Fixed Operational Costs Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Fixed Operational Costs details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).FIXED_OPERATIONAL_COST_READ)")
	public FixedOperationalCostDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		FixedOperationalCostDTO itemDTO = fixedOperationalCostService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Fixed Operational Costs
	 *
	 * @return New Fixed Operational Costs
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Fixed Operational Costs", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).FIXED_OPERATIONAL_COST_CREATE)")
	public FixedOperationalCostDTO create(
		@Parameter(description = "Fixed Operational Costs Details", required = true) @RequestBody FixedOperationalCostDTO newItemDTO
	) {

		FixedOperationalCostDTO result = fixedOperationalCostService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Fixed Operational Costs
	 *
	 * @return Updated Fixed Operational Costs
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Fixed Operational Costs", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).FIXED_OPERATIONAL_COST_UPDATE)")
	public FixedOperationalCostDTO update(
		@Parameter(description = "Fixed Operational Costs update Details", required = true) @RequestBody FixedOperationalCostDTO itemDTO
	) {

		FixedOperationalCostDTO result = fixedOperationalCostService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Fixed Operational Costs
	 *
	 * @return ID of removed Fixed Operational Costs
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Fixed Operational Costs", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).FIXED_OPERATIONAL_COST_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Fixed Operational Costs Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = fixedOperationalCostService.delete(itemDTO.getId());

		return result;
	}

}
