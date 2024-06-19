package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.ByFrameworkFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.control_subcategory.ControlSubcategoryEditDTO;
import com.cyberintech.vrisk.server.model.dto.control_subcategory.ControlSubcategoryViewDTO;
import com.cyberintech.vrisk.server.service.ControlSubcategoryService;
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
 * Control Subcategories management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-09
 */
@RestController
@RequestMapping(
	value = ControlSubcategoryController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Control Subcategories Management Controller"
)
@Tag(name = "Control Subcategories Management")
public class ControlSubcategoryController {

	static final String CONTROLLER_URI = "/api/control-subcategories";

	@Autowired
	private ControlSubcategoryService controlSubcategoryService;

	/**
	 * Get Control Subcategories List for current Risk Model
	 *
	 * @return Control Subcategories List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Control Subcategories List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_SUBCATEGORY_READ)")
	public FilteredResponse<ByFrameworkFilter, ControlSubcategoryViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<ByFrameworkFilter> filteredRequest
	) {

		FilteredResponse<ByFrameworkFilter, ControlSubcategoryViewDTO> result = controlSubcategoryService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Control Subcategory details
	 *
	 * @return Control Subcategory Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Control Subcategory details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_SUBCATEGORY_READ)")
	public ControlSubcategoryEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		ControlSubcategoryEditDTO itemDTO = controlSubcategoryService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Control Subcategory
	 *
	 * @return New Control Subcategory
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Control Subcategory", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_SUBCATEGORY_CREATE)")
	public ControlSubcategoryEditDTO create(
		@Parameter(description = "Control Subcategory Details", required = true) @RequestBody ControlSubcategoryEditDTO newItemDTO
	) {

		ControlSubcategoryEditDTO result = controlSubcategoryService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Control Subcategory
	 *
	 * @return Updated Control Subcategory
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Control Subcategory", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_SUBCATEGORY_UPDATE)")
	public ControlSubcategoryEditDTO update(
		@Parameter(description = "Control Subcategories update Details", required = true) @RequestBody ControlSubcategoryEditDTO itemDTO
	) {

		ControlSubcategoryEditDTO result = controlSubcategoryService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Control Subcategory
	 *
	 * @return ID of removed Control Subcategory
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Control Subcategory", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTROL_SUBCATEGORY_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Control Subcategory Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = controlSubcategoryService.delete(itemDTO.getId());

		return result;
	}

}
