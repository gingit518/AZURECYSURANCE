package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.OrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationDemoDataConfigDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationEditDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationViewDTO;
import com.cyberintech.vrisk.server.service.admin.AdminOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import java.util.List;


/**
 * Organizations controller. Basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-27
 */
@RestController
@RequestMapping(
	value = AdminOrganizationController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Organizations Management Controller"
)
@Tags(value = {@Tag(name = "Admin Organizations Management"), @Tag(name = "Administration")})
public class AdminOrganizationController {

	static final String CONTROLLER_URI = "/api/admin/organizations";

	@Autowired
	private AdminOrganizationService adminOrganizationService;

	/**
	 * Get Organizations List
	 *
	 * @return Organizations List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Get Organizations List")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_ORGANIZATION_READ)")
	public List<OrganizationViewDTO> getList() {

		// List<OrganizationViewDTO> result = adminOrganizationService.getList();
		List<OrganizationViewDTO> result = null;

		return result;
	}

	/**
	 * Return Filtered list of Organizations
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Return Filtered list of Organizations", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_ORGANIZATION_READ)")
	public FilteredResponse<OrganizationFilter, OrganizationViewDTO> filter(@Parameter(description = "Organization Filtering", required = true) @RequestBody FilteredRequest<OrganizationFilter> filteredRequest) {

		FilteredResponse<OrganizationFilter, OrganizationViewDTO> result = adminOrganizationService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Load test data to organization
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/load-demo-data", name = "Loads Demo data to Organizations", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ORGANIZATION_LOAD_TEST_DATA)")
	public ImportResultDTO applyDemoData(@Parameter(description = "Organization Filtering", required = true) @RequestBody OrganizationDemoDataConfigDTO organizationDemoDataConfig) {
		return adminOrganizationService.loadDemoData(organizationDemoDataConfig);
	}

	/**
	 * Get Organization details
	 *
	 * @return Organization Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Organization details")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_ORGANIZATION_READ)")
	public OrganizationEditDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		OrganizationEditDTO itemDTO = adminOrganizationService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Organization
	 *
	 * @return New Organization
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Organization", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_ORGANIZATION_CREATE)")
	public OrganizationEditDTO create(@Parameter(description = "Organization Details", required = true) @RequestBody OrganizationEditDTO newItemDTO) {

		OrganizationEditDTO result = adminOrganizationService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Organization
	 *
	 * @return New Organization
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Organization", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_ORGANIZATION_UPDATE)")
	public OrganizationEditDTO update(@Parameter(description = "Organization update Details", required = true) @RequestBody OrganizationEditDTO itemDTO) {

		OrganizationEditDTO result = adminOrganizationService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Organization
	 *
	 * @return ID of removed Organization
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Organization", consumes = {MediaType.APPLICATION_JSON})
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_ORGANIZATION_DELETE)")
	public Long delete(@Parameter(description = "Simple Organization Details", required = true) @RequestBody ItemViewDTO itemDTO) {

		Long result = itemDTO.getId();

		OrganizationEditDTO deleteResult = adminOrganizationService.deleteOrganizationById(itemDTO.getId());
		if (deleteResult != null) {
			result = deleteResult.getId();
		}

		return result;
	}

}
