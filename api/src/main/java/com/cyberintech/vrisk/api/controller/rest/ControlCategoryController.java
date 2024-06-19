package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.ByFrameworkFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.control_category.ControlCategoryEditDTO;
import com.cyberintech.vrisk.server.model.dto.control_category.ControlCategoryViewDTO;
import com.cyberintech.vrisk.server.service.ControlCategoryService;
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
 * Control Categories management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-09
 */
@RestController
@RequestMapping(
	value = ControlCategoryController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Control Categories Management Controller"
)
@Tag(name = "Control Categories Management")
public class ControlCategoryController {

	static final String CONTROLLER_URI = "/api/control-categories";

	@Autowired
	private ControlCategoryService controlCategoryService;

	/**
	 * Get Control Categories List for current Risk Model
	 *
	 * @return Control Categories List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Control Categories List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_CATEGORY_READ)")
	public FilteredResponse<ByFrameworkFilter, ControlCategoryViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<ByFrameworkFilter> filteredRequest
	) {

		FilteredResponse<ByFrameworkFilter, ControlCategoryViewDTO> result = controlCategoryService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Control Category details
	 *
	 * @return Control Category Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Control Category details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_CATEGORY_READ)")
	public ControlCategoryEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		ControlCategoryEditDTO itemDTO = controlCategoryService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Control Category
	 *
	 * @return New Control Category
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Control Category", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_CATEGORY_CREATE)")
	public ControlCategoryEditDTO create(
		@Parameter(description = "Control Category Details", required = true) @RequestBody ControlCategoryEditDTO newItemDTO
	) {

		ControlCategoryEditDTO result = controlCategoryService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Control Category
	 *
	 * @return Updated Control Category
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Control Category", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_CATEGORY_UPDATE)")
	public ControlCategoryEditDTO update(
		@Parameter(description = "Control Categories update Details", required = true) @RequestBody ControlCategoryEditDTO itemDTO
	) {

		ControlCategoryEditDTO result = controlCategoryService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Control Category
	 *
	 * @return ID of removed Control Category
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Control Category", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_CATEGORY_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Control Category Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = controlCategoryService.delete(itemDTO.getId());

		return result;
	}


}
