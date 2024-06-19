package com.cyberintech.vrisk.api.controller.rest.integrations;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationEditDTO;
import com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.ZoomInfoCache;
import com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.ZoomInfoService;
import com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo.dto.*;
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
import java.util.List;

/**
 * ZoomInfo API integrations controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-02-23
 */
@RestController
@RequestMapping(
	value = ZoomInfoIntegrationsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Integrations - ZoomInfo Controller"
)
@Tag(name = "Integrations - ZoomInfo Controller")
public class ZoomInfoIntegrationsController {

	static final String CONTROLLER_URI = "/api/integrations/zoominfo";

	@Autowired
	private ZoomInfoCache zoomInfoCache;

	@Autowired
	private ZoomInfoService zoomInfoService;

	/**
	 * Search across Zoom Info organizations
	 *
	 * @return Organizations List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/organizations/search", name = "Search across Zoom Info organizations")
//	@Parameters({
//		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
//	})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<OrganizationSearchFilter, OrganizationSearchItem> getListFiltered(
		@Parameter(name = "Item Filtering", required = true) @RequestBody FilteredRequest<OrganizationSearchFilter> filteredRequest
	) {

		FilteredResponse<OrganizationSearchFilter, OrganizationSearchItem> result = zoomInfoService.searchOrganizations(filteredRequest);

		return result;
	}

	/**
	 * Enrich Zoom Info organizations using simple search pattern
	 *
	 * @return Organizations List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/organizations/enrich", name = "Search across Zoom Info organizations")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public OrganizationEnrichResponse enrichOrganization(
		@Parameter(name = "Item Filtering", required = true) @RequestBody FilteredRequest<OrganizationSearchFilter> filteredRequest
	) {

		OrganizationEnrichResponse result = zoomInfoService.enrichOrganizations(filteredRequest);

		return result;
	}

	/**
	 * Enrich First Zoom Info organization
	 *
	 * @return Organization Info
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/organizations/enrich/first", name = "Search across Zoom Info organizations")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public OrganizationEnrichItem enrichOrganization(
		@Parameter(name = "Item Filtering", required = true) @RequestBody OrganizationSearchFilter filteredRequest
	) {

		OrganizationEnrichItem result = zoomInfoService.enrichOrganizations(filteredRequest);

		return result;
	}

	/**
	 * Obtain Zoom Info organizations hierarchy
	 *
	 * @return Organization hierarchy Info
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/organizations/hierarchy", name = "Obtain Zoom Info organizations hierarchy")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public OrganizationHierarchyEnrichItem enrichOrganizationHierarchy(
		@Parameter(name = "Item Filtering", required = true) @RequestBody OrganizationSearchFilter filteredRequest
	) {

		OrganizationHierarchyEnrichItem result = zoomInfoService.enrichOrganizationHierarchy(filteredRequest);

		return result;
	}

	/**
	 * Search OrgChart across Zoom Info organizations
	 *
	 * @return Organization OrgChart Info
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/organizations/orgchart/{zoomInfoId}", name = "Search OrgChart across Zoom Info organizations")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<OrgChartEnrichItem> enrichOrgChart(
		@PathVariable("zoomInfoId") @NotNull @Size(min = 1) String zoomInfoId
	) {
		List<OrgChartEnrichItem> result = zoomInfoService.enrichOrgChart(zoomInfoId);

		return result;
	}

	/**
	 * Apply Zoom Info integration for organization and load all the data
	 *
	 * @return Status OK
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/organizations/apply/{organizationId}", name = "Apply Zoom Info integration for organization and load all the data")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public OrganizationEditDTO applyIntegration(
		@PathVariable("organizationId") @NotNull @Size(min = 1) Long organizationId,
		@Parameter(name = "Organization Edit DTO", required = false) @RequestBody OrganizationEditDTO organizationDTO
	) {
		return zoomInfoService.applyIntegration(organizationId, organizationDTO);
	}

	/**
	 * Load all info for Zoom Info organization
	 *
	 * @return Organization Info
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/organizations/data/{zoomInfoId}", name = "Search across Zoom Info organizations")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public OrganizationZoomInfoExtendedDetails loadOrganizationZoomInfoExtended(@PathVariable("zoomInfoId") @NotNull @Size(min = 1) Long zoomInfoId) {
		OrganizationZoomInfoExtendedDetails result = zoomInfoService.getOrganizationZoomInfoExtended(zoomInfoId);

		return result;
	}

	/**
	 * Save verified data for Zoom Info organization
	 *
	 * @return Status OK
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/organizations/data", name = "Save verified data for Zoom Info organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public String saveCache(@PathVariable("zoomInfoId") @NotNull @Size(min = 1) OrganizationZoomInfoExtendedDetails zoomInfoExtendedDetails) {

		zoomInfoCache.putOrganization(zoomInfoExtendedDetails);

		return "OK";
	}

	/**
	 * Removes all info for Zoom Info organization from Cache
	 *
	 * @return Status OK
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/organizations/data/{zoomInfoId}", name = "Remove cache for Zoom Info organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public String deleteCache(@PathVariable("zoomInfoId") @NotNull @Size(min = 1) Long zoomInfoId) {

		zoomInfoCache.removeOrganization(zoomInfoId);

		return "OK";
	}

}
