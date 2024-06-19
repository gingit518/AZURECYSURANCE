package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.TechnologyFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyEditDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyViewDTO;
import com.cyberintech.vrisk.server.service.TechnologyService;
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
 * Technology management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = TechnologyController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Technology Management Controller"
)
@Tag(name = "Technology Management")
public class TechnologyController {

	static final String CONTROLLER_URI = "/api/technologies";

	@Autowired
	private TechnologyService technologyService;

	/**
	 * Get Technology List for current Risk Model
	 *
	 * @return Technology List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Technology List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	// @PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_READ)")
	public FilteredResponse<TechnologyFilter, TechnologyViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<TechnologyFilter> filteredRequest
	) {

		FilteredResponse<TechnologyFilter, TechnologyViewDTO> result = technologyService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Technology details
	 *
	 * @return Technology Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Technology details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_READ)")
	public TechnologyEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		TechnologyEditDTO itemDTO = technologyService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Technology
	 *
	 * @return New Technology
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Technology", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_CREATE)")
	public TechnologyEditDTO create(
		@Parameter(description = "Technology Details", required = true) @RequestBody TechnologyEditDTO newItemDTO
	) {

		TechnologyEditDTO result = technologyService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Technology
	 *
	 * @return Updated Technology
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Technology", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_UPDATE)")
	public TechnologyEditDTO update(
		@Parameter(description = "Technology update Details", required = true) @RequestBody TechnologyEditDTO itemDTO
	) {

		TechnologyEditDTO result = technologyService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Technology
	 *
	 * @return ID of removed Technology
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Technology", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TECHNOLOGY_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Technology Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = technologyService.delete(itemDTO.getId());

		return result;
	}

}
