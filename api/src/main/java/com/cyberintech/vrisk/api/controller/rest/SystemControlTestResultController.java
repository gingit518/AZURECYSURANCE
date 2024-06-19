package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.ControlTestResultFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.assessments.SystemControlTestResultViewDTO;
import com.cyberintech.vrisk.server.service.SystemControlTestResultService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * System Control Test Result management controller. Basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.02.02
 */
@RestController
@RequestMapping(
	value = SystemControlTestResultController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "System Control Test Result Management Controller"
)
@Tag(name = "System Control Test Result Management")
public class SystemControlTestResultController {

	static final String CONTROLLER_URI = "/api/system-control-test-results";

	@Autowired
	private SystemControlTestResultService systemControlTestResultService;

	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "System Control Test Result list for current Filters and Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_CONTROL_TEST_RESULT_READ)")
	public FilteredResponse<ControlTestResultFilter, SystemControlTestResultViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<ControlTestResultFilter> filteredRequest
	) {

		FilteredResponse<ControlTestResultFilter, SystemControlTestResultViewDTO> itemDTOs = systemControlTestResultService.getListFiltered(filteredRequest);

		return itemDTOs;
	}

//	/**
//	 * Get System Control Test Result details
//	 *
//	 * @return System Control Test Result Details
//	 */
//	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get System Control Test Result details")
//	@Parameters({
//		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
//	})
//	public SystemControlTestResultEditDTO getDetails(
//		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
//	) {
//
//		SystemControlTestResultEditDTO itemDTO = systemControlTestResultService.getDetails(itemId);
//
//		return itemDTO;
//	}
}
