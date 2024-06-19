package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.SubsidiaryOrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationEditDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.service.OrganizationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;


/**
 * Organizations management controller. Basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@RestController
@RequestMapping(
	value = OrganizationController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Organizations Management Controller"
)
@Tag(name = "Organization Viewer")
public class OrganizationController {

	static final String CONTROLLER_URI = "/api/organizations";

	@Autowired
	private OrganizationService organizationService;

	/**
	 * Get Organization details for Current User
	 *
	 * @return User Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/self", name = "Get Organization details for current user")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public OrganizationEditDTO getSelf() {
		Organizations entity = organizationService.getCurrentOrganizationEntity();
		OrganizationEditDTO itemDTO = new OrganizationEditDTO(entity);

		return itemDTO;
	}

	/**
	 * Filter Vendors list by name and return Paged Response
	 *
	 * @return Organizations List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter-vendors", name = "Filter Vendors list by name and return Paged Response")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ORGANIZATION_READ)")
	public FilteredResponse<SubsidiaryOrganizationFilter, OrganizationViewDTO> getListFiltered(
		@Parameter(description = "Name Filtering", required = true) @RequestBody FilteredRequest<SubsidiaryOrganizationFilter> filteredRequest
	) {

		FilteredResponse<SubsidiaryOrganizationFilter, OrganizationViewDTO> result = organizationService.getOrganizationListFiltered(OrganizationType.Vendor, filteredRequest);

		return result;
	}

	/**
	 * Filter Vendors list by name and return Paged Response
	 *
	 * @return Organizations List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter-subsidiaries", name = "Filter Vendors list by name and return Paged Response")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SUBSIDIARY_READ)")
	public FilteredResponse<SubsidiaryOrganizationFilter, OrganizationViewDTO> getListFilteredSubsidiaries(
		@Parameter(description = "Name Filtering", required = true) @RequestBody FilteredRequest<SubsidiaryOrganizationFilter> filteredRequest
	) {

		FilteredResponse<SubsidiaryOrganizationFilter, OrganizationViewDTO> result = organizationService.getOrganizationListFiltered(OrganizationType.Subsidiary, filteredRequest);

		return result;
	}

	/**
	 * Update current Organization Details
	 *
	 * @return Organization
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/update-organization", name = "Update allowed Details for current Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasOneOfPermissions(T(com.cyberintech.vrisk.api.config.APIAction).ORGANIZATION_UPDATE, T(com.cyberintech.vrisk.api.config.APIAction).REGULATORY_EXPOSURE_UPDATE)")
	public OrganizationEditDTO updateCurrentOrganization(
		@Parameter(description = "Current organization Details", required = true) @RequestBody OrganizationEditDTO organizationEditDTO
		) {

		OrganizationEditDTO result = organizationService.updateCurrentOrganization(organizationEditDTO);

		return result;
	}

	/**
	 * Update current Organization Details
	 *
	 * @return Organization
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/update-cyber-insurance-info", name = "Update allowed Cyber Insurance Information for current Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasOneOfPermissions(T(com.cyberintech.vrisk.api.config.APIAction).ORGANIZATION_CYBER_INSURANCE_INFORMATION)")
	public OrganizationEditDTO updateCurrentOrganizationCyberInfo(
		@Parameter(description = "Current organization Details", required = true) @RequestBody OrganizationEditDTO organizationEditDTO
		) {

		OrganizationEditDTO result = organizationService.updateCurrentOrganizationCyberInfo(organizationEditDTO);

		return result;
	}

}
