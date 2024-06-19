package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.IndustryDTO;
import com.cyberintech.vrisk.server.model.dto.organization.IndustryRefDTO;
import com.cyberintech.vrisk.server.service.IndustryService;
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
 * Industries management controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-08
 */
@RestController
@RequestMapping(
	value = IndustryController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Industries Management Controller"
)
@Tag(name = "Industries Management")
public class IndustryController {

	static final String CONTROLLER_URI = "/api/industries";

	@Autowired
	private IndustryService industryService;

	/**
	 * Get Industries List for current Filters
	 *
	 * @return Industries List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Industries List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, IndustryRefDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, IndustryRefDTO> result = industryService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Industries List for current Filters
	 *
	 * @return Industries List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter-extended", name = "Industries List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, IndustryDTO> getListFilteredWithParents(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, IndustryDTO> result = industryService.getListFilteredWithParents(filteredRequest);

		return result;
	}

	/**
	 * Get Industry details
	 *
	 * @return Industry Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Industry details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).INDUSTRY_READ)")
	public IndustryDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {
		IndustryDTO itemDTO = industryService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Industry
	 *
	 * @return New Industry
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Industry", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).HINT_CREATE)")
	public IndustryDTO create(
		@Parameter(description = "Industry Details", required = true) @RequestBody IndustryDTO newItemDTO
	) {
		IndustryDTO result = industryService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Industry
	 *
	 * @return Updated Industry
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Industry", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).HINT_UPDATE)")
	public IndustryDTO update(
		@Parameter(description = "Industries update Details", required = true) @RequestBody IndustryDTO itemDTO
	) {
		IndustryDTO result = industryService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Industry
	 *
	 * @return ID of removed Industry
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Industry", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).INDUSTRY_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Industry Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {
		Long result = industryService.delete(itemDTO.getId());

		return result;
	}

}
