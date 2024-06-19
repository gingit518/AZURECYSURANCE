package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.SubsidiaryOrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.VendorEditDTO;
import com.cyberintech.vrisk.server.model.dto.organization.VendorViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.VendorService;
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
 * Vendors controller. Basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-14
 */
@RestController
@RequestMapping(
	value = VendorController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Vendors Management Controller"
)
@Tag(name = "Vendors Management")
public class VendorController {

	static final String CONTROLLER_URI = "/api/vendors";

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private VendorService vendorService;

	/**
	 * Return Filtered list of Vendors
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Return Filtered list of Vendors", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasOneOfPermissions(T(com.cyberintech.vrisk.api.config.APIAction).VENDOR_READ, T(com.cyberintech.vrisk.api.config.APIAction).CLOUD_SCORING_READ)")
	public FilteredResponse<SubsidiaryOrganizationFilter, VendorViewDTO> filter(@Parameter(description = "Vendor Filtering", required = true) @RequestBody FilteredRequest<SubsidiaryOrganizationFilter> filteredRequest) {

		FilteredResponse<SubsidiaryOrganizationFilter, VendorViewDTO> result = organizationService.getOrganizationListFiltered(OrganizationType.Vendor, filteredRequest, VendorViewDTO.class);

		return result;
	}

	/**
	 * Get Vendor details
	 *
	 * @return Vendor Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Vendor details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VENDOR_UPDATE)")
	public VendorEditDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		VendorEditDTO itemDTO = vendorService.getVendorDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Vendor
	 *
	 * @return New Vendor
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Vendor", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VENDOR_CREATE)")
	public VendorEditDTO create(@Parameter(description = "Vendor Details", required = true) @RequestBody VendorEditDTO newItemDTO) {

		VendorEditDTO result = vendorService.createVendor(newItemDTO);

		return result;
	}

	/**
	 * Update Vendor
	 *
	 * @return New Vendor
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Vendor", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).VENDOR_UPDATE)")
	public VendorEditDTO update(@Parameter(description = "Vendor update Details", required = true) @RequestBody VendorEditDTO itemDTO) {

		VendorEditDTO result = vendorService.updateVendor(itemDTO);

		return result;
	}

	/**
	 * Deletes Vendor
	 *
	 * @return ID of removed Vendor
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Vendor", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(@Parameter(description = "Vendor Reference", required = true) @RequestBody ItemViewDTO itemDTO) {

		Long result = itemDTO.getId();

		vendorService.deleteVendorById(itemDTO.getId());

		return result;
	}

}
