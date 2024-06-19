package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.LocationFilter;
import com.cyberintech.vrisk.server.model.dto.state.StateViewDTO;
import com.cyberintech.vrisk.server.service.StateService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * State management controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = StateController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "State Management Controller"
)
@Tag(name = "State Management")
public class StateController {

	static final String CONTROLLER_URI = "/api/states";

	@Autowired
	private StateService stateService;

	/**
	 * Get State List for current Filters
	 *
	 * @return State List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "State List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<LocationFilter, StateViewDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<LocationFilter> filteredRequest
	) {

		FilteredResponse<LocationFilter, StateViewDTO> result = stateService.getListFiltered(filteredRequest);

		return result;
	}

}
