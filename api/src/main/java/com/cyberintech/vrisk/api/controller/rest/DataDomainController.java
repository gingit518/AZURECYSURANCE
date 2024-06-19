package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.datadomains.DataDomainsDTO;
import com.cyberintech.vrisk.server.service.DataDomainsService;
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
 * Data Domain management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-16
 */
@RestController
@RequestMapping(
	value = DataDomainController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Data Domains Management Controller"
)
@Tag(name = "Data Domains Management")
public class DataDomainController {

	static final String CONTROLLER_URI = "/api/data-domains";

	@Autowired
	private DataDomainsService dataDomainsService;

	/**
	 * Get Data Domains List for current Risk Model
	 *
	 * @return Data Domains List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Data Domains List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_DOMAIN_READ)")
	public FilteredResponse<NameFilter, DataDomainsDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, DataDomainsDTO> result = dataDomainsService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Data Domain details
	 *
	 * @return Data Domain Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Data Domain details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_DOMAIN_READ)")
	public DataDomainsDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		DataDomainsDTO itemDTO = dataDomainsService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Data Domain
	 *
	 * @return New Data Domain
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Data Domain", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_DOMAIN_CREATE)")
	public DataDomainsDTO create(
		@Parameter(description = "Data Domain Details", required = true) @RequestBody DataDomainsDTO newItemDTO
	) {

		DataDomainsDTO result = dataDomainsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Data Domain
	 *
	 * @return Updated Data Domain
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Data Domain", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_DOMAIN_UPDATE)")
	public DataDomainsDTO update(
		@Parameter(description = "Data Domains update Details", required = true) @RequestBody DataDomainsDTO itemDTO
	) {

		DataDomainsDTO result = dataDomainsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Data Domain
	 *
	 * @return ID of removed Data Domain
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Data Domain", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_DOMAIN_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Data Domain Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = dataDomainsService.delete(itemDTO.getId());

		return result;
	}

}
