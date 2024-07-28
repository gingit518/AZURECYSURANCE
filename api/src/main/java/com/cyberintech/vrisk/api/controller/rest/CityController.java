package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.LocationFilter;
import com.cyberintech.vrisk.server.model.dto.city.CityEditDTO;
import com.cyberintech.vrisk.server.model.dto.city.CityViewDTO;
import com.cyberintech.vrisk.server.service.CityService;
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
 * City management controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = CityController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "City Management Controller"
)
@Tag(name = "City Management")
public class CityController {

	static final String CONTROLLER_URI = "/api/cities";

	@Autowired
	private CityService cityService;

	/**
	 * Get City List for current Filters
	 *
	 * @return City List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "City List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<LocationFilter, CityViewDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<LocationFilter> filteredRequest
	) {

		FilteredResponse<LocationFilter, CityViewDTO> result = cityService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Create City
	 *
	 * @return City
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/safely-create", name = "Safely create City item")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public CityEditDTO safelyCreate(@Parameter(description = "Item Filtering", required = true) @RequestBody CityEditDTO city) {

		CityEditDTO result = cityService.safelyCreate(city);

		return result;
	}

}
