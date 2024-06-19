package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.ByParentFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyClassTypeDTO;
import com.cyberintech.vrisk.server.service.TechnologyClassTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;

/**
 * Technology Class Types management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-03
 */
@RestController
@RequestMapping(
	value = TechnologyClassTypeController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Technology Class Types Management Controller"
)
@Tag(name = "Technology Class Types Management")
public class TechnologyClassTypeController {

	static final String CONTROLLER_URI = "/api/technology-class-types";

	@Autowired
	private TechnologyClassTypeService technologyClassTypeService;

	/**
	 * Get Technology Class Types List for current Risk Model
	 *
	 * @return Technology Class Types List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Technology Class Types List for current Filters and Risk Model")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_READ)")
	public FilteredResponse<ByParentFilter, TechnologyClassTypeDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<ByParentFilter> filteredRequest
	) {

		FilteredResponse<ByParentFilter, TechnologyClassTypeDTO> result = technologyClassTypeService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Technology Class Type details
	 *
	 * @return Technology Class Type Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Technology Class Type details")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_READ)")
	public TechnologyClassTypeDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		TechnologyClassTypeDTO itemDTO = technologyClassTypeService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Technology Class Type
	 *
	 * @return New Technology Class Type
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Technology Class Type", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_CREATE)")
	public TechnologyClassTypeDTO create(
		@Parameter(description = "Technology Class Type Details", required = true) @RequestBody TechnologyClassTypeDTO newItemDTO
	) {

		TechnologyClassTypeDTO result = technologyClassTypeService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Technology Class Type
	 *
	 * @return Updated Technology Class Type
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Technology Class Type", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_UPDATE)")
	public TechnologyClassTypeDTO update(
		@Parameter(description = "Technology Class Types update Details", required = true) @RequestBody TechnologyClassTypeDTO itemDTO
	) {

		TechnologyClassTypeDTO result = technologyClassTypeService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Technology Class Type
	 *
	 * @return ID of removed Technology Class Type
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Technology Class Type", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CATEGORY_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Technology Class Type Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = technologyClassTypeService.delete(itemDTO.getId());

		return result;
	}

}
