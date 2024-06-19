package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.budget.CostTypeDTO;
import com.cyberintech.vrisk.server.service.CostTypeService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;

/**
 * Cost Type management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@RestController
@RequestMapping(
	value = CostTypeController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Cost Type Management Controller"
)
@Tag(name = "Cost Type Management")
public class CostTypeController {

	static final String CONTROLLER_URI = "/api/cost-type";

	@Autowired
	private CostTypeService costTypeService;

	/**
	 * Get Cost Type List for current Risk Model
	 *
	 * @return Cost Type List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Cost Type List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, CostTypeDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, CostTypeDTO> result = costTypeService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Cost Type details
	 *
	 * @return Cost Type Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Cost Type details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public CostTypeDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		CostTypeDTO itemDTO = costTypeService.getDetails(itemId);

		return itemDTO;
	}


}
