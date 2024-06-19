package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.associate_vendors.AssociateVendorEditDTO;
import com.cyberintech.vrisk.server.repository.results.AssociateVendorResult;
import com.cyberintech.vrisk.server.service.AssociateVendorService;
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
 * Associate Vendors management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = AssociateVendorController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Associate Vendors Management Controller"
)
@Tag(name = "Associate Vendors Management")
public class AssociateVendorController {

	static final String CONTROLLER_URI = "/api/associate-vendors";

	@Autowired
	private AssociateVendorService associateVendorService;

	/**
	 * Get Associate Vendors List for current Risk Model
	 *
	 * @return Associate Vendors List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Associate Vendors List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSOCIATE_VENDOR_READ)")
	public FilteredResponse<NameFilter, AssociateVendorResult> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, AssociateVendorResult> result = associateVendorService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Associate Vendor details
	 *
	 * @return Associate Vendor Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Associate Vendor details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSOCIATE_VENDOR_READ)")
	public AssociateVendorEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		AssociateVendorEditDTO itemDTO = associateVendorService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Associate Vendor
	 *
	 * @return New Associate Vendor
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Associate Vendor", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public AssociateVendorEditDTO create(
		@Parameter(description = "Associate Vendor Details", required = true) @RequestBody AssociateVendorEditDTO newItemDTO
	) {

		AssociateVendorEditDTO result = associateVendorService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Associate Vendor
	 *
	 * @return Updated Associate Vendor
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Associate Vendor", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSOCIATE_VENDOR_UPDATE)")
	public AssociateVendorEditDTO update(
		@Parameter(description = "Associate Vendors update Details", required = true) @RequestBody AssociateVendorEditDTO itemDTO
	) {

		AssociateVendorEditDTO result = associateVendorService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Associate Vendor
	 *
	 * @return ID of removed Associate Vendor
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Associate Vendor", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(
		@Parameter(description = "Simple Associate Vendor Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = associateVendorService.delete(itemDTO.getId());

		return result;
	}

}
