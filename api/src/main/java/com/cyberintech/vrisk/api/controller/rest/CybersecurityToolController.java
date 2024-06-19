package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.budget.CybersecurityToolDTO;
import com.cyberintech.vrisk.server.service.CybersecurityToolService;
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
 * Cybersecurity Tools management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-10
 */
@RestController
@RequestMapping(
	value = CybersecurityToolController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Cybersecurity Tools Management Controller"
)
@Tag(name = "Cybersecurity Tools Management")
public class CybersecurityToolController {

	static final String CONTROLLER_URI = "/api/budget/cybersecurity-tools";

	@Autowired
	private CybersecurityToolService cybersecurityToolService;

	/**
	 * Get Cybersecurity Tools List for current Risk Model
	 *
	 * @return Cybersecurity Tools List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Cybersecurity Tools List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CYBER_SECURITY_TOOL_READ)")
	public FilteredResponse<NameFilter, CybersecurityToolDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, CybersecurityToolDTO> result = cybersecurityToolService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Cybersecurity Tool details
	 *
	 * @return Cybersecurity Tool Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Cybersecurity Tool details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CYBER_SECURITY_TOOL_READ)")
	public CybersecurityToolDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		CybersecurityToolDTO itemDTO = cybersecurityToolService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Cybersecurity Tool
	 *
	 * @return New Cybersecurity Tool
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Cybersecurity Tool", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CYBER_SECURITY_TOOL_CREATE)")
	public CybersecurityToolDTO create(
		@Parameter(description = "Cybersecurity Tool Details", required = true) @RequestBody CybersecurityToolDTO newItemDTO
	) {

		CybersecurityToolDTO result = cybersecurityToolService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Cybersecurity Tool
	 *
	 * @return Updated Cybersecurity Tool
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Cybersecurity Tool", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CYBER_SECURITY_TOOL_UPDATE)")
	public CybersecurityToolDTO update(
		@Parameter(description = "Cybersecurity Tools update Details", required = true) @RequestBody CybersecurityToolDTO itemDTO
	) {

		CybersecurityToolDTO result = cybersecurityToolService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Cybersecurity Tool
	 *
	 * @return ID of removed Cybersecurity Tool
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Cybersecurity Tool", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CYBER_SECURITY_TOOL_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Cybersecurity Tool Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = cybersecurityToolService.delete(itemDTO.getId());

		return result;
	}

}
