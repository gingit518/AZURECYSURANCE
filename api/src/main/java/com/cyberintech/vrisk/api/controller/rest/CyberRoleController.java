package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.budget.CyberRoleDTO;
import com.cyberintech.vrisk.server.service.CyberRoleService;
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
 * Cyber Roles management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-10
 */
@RestController
@RequestMapping(
	value = CyberRoleController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Cyber Roles Management Controller"
)
@Tag(name = "Cyber Roles Management")
public class CyberRoleController {

	static final String CONTROLLER_URI = "/api/budget/cyber-roles";

	@Autowired
	private CyberRoleService cyberRoleService;

	/**
	 * Get Cyber Roles List for current Risk Model
	 *
	 * @return Cyber Roles List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Cyber Roles List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CYBER_ROLE_READ)")
	public FilteredResponse<NameFilter, CyberRoleDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, CyberRoleDTO> result = cyberRoleService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Cyber Role details
	 *
	 * @return Cyber Role Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Cyber Role details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CYBER_ROLE_READ)")
	public CyberRoleDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		CyberRoleDTO itemDTO = cyberRoleService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Cyber Role
	 *
	 * @return New Cyber Role
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Cyber Role", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CYBER_ROLE_CREATE)")
	public CyberRoleDTO create(
		@Parameter(description = "Cyber Role Details", required = true) @RequestBody CyberRoleDTO newItemDTO
	) {

		CyberRoleDTO result = cyberRoleService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Cyber Role
	 *
	 * @return Updated Cyber Role
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Cyber Role", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CYBER_ROLE_UPDATE)")
	public CyberRoleDTO update(
		@Parameter(description = "Cyber Roles update Details", required = true) @RequestBody CyberRoleDTO itemDTO
	) {

		CyberRoleDTO result = cyberRoleService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Cyber Role
	 *
	 * @return ID of removed Cyber Role
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Cyber Role", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CYBER_ROLE_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Cyber Role Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = cyberRoleService.delete(itemDTO.getId());

		return result;
	}

}
