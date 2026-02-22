package com.cyberintech.vrisk.elastioapi.controller.rest;

import com.cyberintech.vrisk.server.model.data.ElastioOrganizationFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.OrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.organization.ElastioOrganizationViewDTO;
import com.cyberintech.vrisk.server.security.SecurityProfile;
import com.cyberintech.vrisk.server.service.integrations.elastio.ElastioOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;


/**
 * Elastion Integration controller.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2026-02-20
 */
@RestController
@RequestMapping(
	value = ElastioIntegrationController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Elastio Integrations Controller"
)
@Tag(name = "Elastio Integrations Info")
public class ElastioIntegrationController {

	static final String CONTROLLER_URI = "/api/elastio/intergations";

	@Autowired
	private ElastioOrganizationService elastioOrganizationService;

	/**
	 * Return Filtered list of Organizations
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Return Filtered list of Elastio Organizations", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = SecurityProfile.AUTHORIZATION_SCHEME_API_KEY)})
	@PreAuthorize("@apiSecurity.hasRole(T(com.cyberintech.vrisk.server.model.jpa.domains.RoleType).ELASTIO_ADMIN)")
	public FilteredResponse<ElastioOrganizationFilter, ElastioOrganizationViewDTO> filter(@Parameter(description = "Organization Filtering", required = true) @RequestBody FilteredRequest<ElastioOrganizationFilter> filteredRequest) {
		return elastioOrganizationService.getElastioListFiltered(filteredRequest);
	}

	/**
	 * Sync Elastio Organization
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/apply", name = "Sync Elastio Organization data with RiskQ", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = SecurityProfile.AUTHORIZATION_SCHEME_API_KEY)})
	@PreAuthorize("@apiSecurity.hasRole(T(com.cyberintech.vrisk.server.model.jpa.domains.RoleType).ELASTIO_ADMIN)")
	public ElastioOrganizationViewDTO apply(@Parameter(description = "Organization Filtering", required = true) @RequestBody ElastioOrganizationViewDTO organization) {
		return elastioOrganizationService.createElastio(organization);
	}

}
