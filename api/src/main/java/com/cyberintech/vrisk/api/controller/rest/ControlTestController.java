package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.ControlTestFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.ControlTestEditDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.ControlTestViewDTO;
import com.cyberintech.vrisk.server.service.ControlTestService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Control Tests management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-08
 */
@RestController
@RequestMapping(
	value = ControlTestController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Control Tests Management Controller"
)
@Tag(name = "Control Tests Management")
public class ControlTestController {

	static final String CONTROLLER_URI = "/api/control-tests";

	@Autowired
	private ControlTestService controlTestService;

	/**
	 * Get Control Tests List for current Risk Model
	 *
	 * @return Control Tests List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Control Tests List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<ControlTestFilter, ControlTestViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<ControlTestFilter> filteredRequest
	) {

		FilteredResponse<ControlTestFilter, ControlTestViewDTO> result = controlTestService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Save list of Control Tests
	 *
	 * @return New/Updated Control Test
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/save-items", name = "Save list of Control Tests", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<ControlTestEditDTO> saveItems(
		@Parameter(description = "Control Tests list", required = true) @RequestBody List<ControlTestEditDTO> itemsList
	) {

		List<ControlTestEditDTO> result = controlTestService.saveItems(itemsList);

		return result;
	}


	/**
	 * Get Control Test details
	 *
	 * @return Control Test Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Control Test details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ControlTestEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		ControlTestEditDTO itemDTO = controlTestService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Control Test
	 *
	 * @return New Control Test
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Control Test", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ControlTestEditDTO create(
		@Parameter(description = "Control Test Details", required = true) @RequestBody ControlTestEditDTO newItemDTO
	) {

		ControlTestEditDTO result = controlTestService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Control Test
	 *
	 * @return Updated Control Test
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Control Test", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ControlTestEditDTO update(
		@Parameter(description = "Control Tests update Details", required = true) @RequestBody ControlTestEditDTO itemDTO
	) {

		ControlTestEditDTO result = controlTestService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Control Test
	 *
	 * @return ID of removed Control Test
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Control Test", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(
		@Parameter(description = "Simple Control Test Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = controlTestService.delete(itemDTO.getId());

		return result;
	}

}
