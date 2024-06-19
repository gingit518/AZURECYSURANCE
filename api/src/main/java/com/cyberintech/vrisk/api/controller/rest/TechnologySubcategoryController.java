package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.ByParentFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologySubcategoryDTO;
import com.cyberintech.vrisk.server.service.TechnologySubcategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;

/**
 * Technology Subcategories management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-03
 */
@RestController
@RequestMapping(
	value = TechnologySubcategoryController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Technology Subcategories Management Controller"
)
@Tag(name = "Technology Subcategories Management")
public class TechnologySubcategoryController {

	static final String CONTROLLER_URI = "/api/technology-subcategories";

	@Autowired
	private TechnologySubcategoryService technologySubcategoryService;

	/**
	 * Get Technology Subcategories List for current Risk Model
	 *
	 * @return Technology Subcategories List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Technology Subcategories List for current Filters and Risk Model")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_READ)")
	public FilteredResponse<ByParentFilter, TechnologySubcategoryDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<ByParentFilter> filteredRequest
	) {

		FilteredResponse<ByParentFilter, TechnologySubcategoryDTO> result = technologySubcategoryService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Technology Subcategory details
	 *
	 * @return Technology Subcategory Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Technology Subcategory details")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_READ)")
	public TechnologySubcategoryDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		TechnologySubcategoryDTO itemDTO = technologySubcategoryService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Technology Subcategory
	 *
	 * @return New Technology Subcategory
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Technology Subcategory", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_CREATE)")
	public TechnologySubcategoryDTO create(
		@Parameter(description = "Technology Subcategory Details", required = true) @RequestBody TechnologySubcategoryDTO newItemDTO
	) {

		TechnologySubcategoryDTO result = technologySubcategoryService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Technology Subcategory
	 *
	 * @return Updated Technology Subcategory
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Technology Subcategory", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_UPDATE)")
	public TechnologySubcategoryDTO update(
		@Parameter(description = "Technology Subcategories update Details", required = true) @RequestBody TechnologySubcategoryDTO itemDTO
	) {

		TechnologySubcategoryDTO result = technologySubcategoryService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Technology Subcategory
	 *
	 * @return ID of removed Technology Subcategory
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Technology Subcategory", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Technology Subcategory Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = technologySubcategoryService.delete(itemDTO.getId());

		return result;
	}

}
