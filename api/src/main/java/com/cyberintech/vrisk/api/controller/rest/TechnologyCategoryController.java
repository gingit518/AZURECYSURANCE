package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryEditDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryViewDTO;
import com.cyberintech.vrisk.server.service.TechnologyCategoryService;
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
 * Technology Categories management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = TechnologyCategoryController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Technology Categories Management Controller"
)
@Tag(name = "Technology Categories Management")
public class TechnologyCategoryController {

	static final String CONTROLLER_URI = "/api/technology-categories";

	@Autowired
	private TechnologyCategoryService technologyCategoryService;

	/**
	 * Get Technology Categories List for current Risk Model
	 *
	 * @return Technology Categories List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Technology Categories List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_READ)")
	public FilteredResponse<NameFilter, TechnologyCategoryViewDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, TechnologyCategoryViewDTO> result = technologyCategoryService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Technology Category details
	 *
	 * @return Technology Category Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Technology Category details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_READ)")
	public TechnologyCategoryEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		TechnologyCategoryEditDTO itemDTO = technologyCategoryService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Technology Category
	 *
	 * @return New Technology Category
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Technology Category", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_CREATE)")
	public TechnologyCategoryEditDTO create(
		@Parameter(description = "Technology Category Details", required = true) @RequestBody TechnologyCategoryEditDTO newItemDTO
	) {

		TechnologyCategoryEditDTO result = technologyCategoryService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Technology Category
	 *
	 * @return Updated Technology Category
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Technology Category", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_UPDATE)")
	public TechnologyCategoryEditDTO update(
		@Parameter(description = "Technology Categories update Details", required = true) @RequestBody TechnologyCategoryEditDTO itemDTO
	) {

		TechnologyCategoryEditDTO result = technologyCategoryService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Technology Category
	 *
	 * @return ID of removed Technology Category
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Technology Category", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Technology Category Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = technologyCategoryService.delete(itemDTO.getId());

		return result;
	}

}
