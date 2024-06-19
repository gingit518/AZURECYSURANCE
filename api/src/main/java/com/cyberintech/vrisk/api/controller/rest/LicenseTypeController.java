package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.budget.LicenseTypeDTO;
import com.cyberintech.vrisk.server.service.LicenseTypeService;
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
 * License Type management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@RestController
@RequestMapping(
	value = LicenseTypeController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "License Type Management Controller"
)
@Tag(name = "License Type Management")
public class LicenseTypeController {

	static final String CONTROLLER_URI = "/api/license-type";

	@Autowired
	private LicenseTypeService licenseTypeService;

	/**
	 * Get License Type List for current Risk Model
	 *
	 * @return License Type List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "License Type List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, LicenseTypeDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, LicenseTypeDTO> result = licenseTypeService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get License Type details
	 *
	 * @return License Type Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get License Type details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public LicenseTypeDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		LicenseTypeDTO itemDTO = licenseTypeService.getDetails(itemId);

		return itemDTO;
	}


}
