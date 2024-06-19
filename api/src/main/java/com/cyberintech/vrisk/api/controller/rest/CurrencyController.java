package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.currency.CurrencyViewDTO;
import com.cyberintech.vrisk.server.service.CurrencyService;
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
 * Currency management controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = CurrencyController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Currency Management Controller"
)
@Tag(name = "Currency Management")
public class CurrencyController {

	static final String CONTROLLER_URI = "/api/currencies";

	@Autowired
	private CurrencyService currencyService;

	/**
	 * Get Currency List for current Filters
	 *
	 * @return Currency List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Currency List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, CurrencyViewDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, CurrencyViewDTO> result = currencyService.getListFiltered(filteredRequest);

		return result;
	}

}
