package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.SubsidiaryOrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationEditDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.service.OrganizationService;
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
 * Subsidiary organization controller. Basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-03-20
 */
@RestController
@RequestMapping(
	value = SubsidiaryOrganizationController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Subsidiary Organizations Management Controller"
)
@Tag(name = "Subsidiary Organizations Management")
public class SubsidiaryOrganizationController {

	static final String CONTROLLER_URI = "/api/subsidiary-organizations";

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Return Filtered list of Subsidiary organizations
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Return Filtered list of Subsidiary Organizations", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SUBSIDIARY_READ)")
	public FilteredResponse<SubsidiaryOrganizationFilter, OrganizationViewDTO> filter(@Parameter(description = "Subsidiary Filtering", required = true) @RequestBody FilteredRequest<SubsidiaryOrganizationFilter> filteredRequest) {

		FilteredResponse<SubsidiaryOrganizationFilter, OrganizationViewDTO> result = organizationService.getOrganizationListFiltered(OrganizationType.Subsidiary, filteredRequest);

		return result;
	}

	/**
	 * Get Subsidiary details
	 *
	 * @return Subsidiary Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Subsidiary details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SUBSIDIARY_READ)")
	public OrganizationEditDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		OrganizationEditDTO itemDTO = organizationService.getSubsidiaryDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Subsidiary
	 *
	 * @return New Subsidiary
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Subsidiary", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SUBSIDIARY_CREATE)")
	public OrganizationEditDTO create(@Parameter(description = "Subsidiary Details", required = true) @RequestBody OrganizationEditDTO newItemDTO) {

		OrganizationEditDTO result = organizationService.createSubsidiary(newItemDTO);

		return result;
	}

	/**
	 * Update Subsidiary
	 *
	 * @return New Subsidiary
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Subsidiary", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SUBSIDIARY_UPDATE)")
	public OrganizationEditDTO update(@Parameter(description = "Subsidiary update Details", required = true) @RequestBody OrganizationEditDTO itemDTO) {

		OrganizationEditDTO result = organizationService.updateSubsidiary(itemDTO);

		return result;
	}

	/**
	 * Deletes Subsidiary
	 *
	 * @return ID of removed Subsidiary
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Subsidiary", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SUBSIDIARY_DELETE)")
	public Long delete(@Parameter(description = "Simple Subsidiary Details", required = true) @RequestBody ItemViewDTO itemDTO) {

		Long result = itemDTO.getId();

		organizationService.deleteSubsidiaryById(itemDTO.getId());

		return result;
	}

}
