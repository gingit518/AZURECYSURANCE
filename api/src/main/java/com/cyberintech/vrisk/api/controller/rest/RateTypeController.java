package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.budget.RateTypeDTO;
import com.cyberintech.vrisk.server.service.RateTypeService;
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
 * Rate Type management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@RestController
@RequestMapping(
	value = RateTypeController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Rate Type Management Controller"
)
@Tag(name = "Rate Type Management")
public class RateTypeController {

	static final String CONTROLLER_URI = "/api/rate-type";

	@Autowired
	private RateTypeService rateTypeService;

	/**
	 * Get Rate Type List for current Risk Model
	 *
	 * @return Rate Type List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Rate Type List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, RateTypeDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, RateTypeDTO> result = rateTypeService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Rate Type details
	 *
	 * @return Rate Type Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Rate Type details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public RateTypeDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		RateTypeDTO itemDTO = rateTypeService.getDetails(itemId);

		return itemDTO;
	}


}
