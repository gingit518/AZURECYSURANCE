package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.agreements.OrganizationAgreementEditDTO;
import com.cyberintech.vrisk.server.model.dto.agreements.OrganizationAgreementViewDTO;
import com.cyberintech.vrisk.server.service.OrganizationAgreementService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;

/**
 * Organization Agreements management Controller. Implements basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-01-17
 */
@RestController
@RequestMapping(
	value = OrganizationAgreementController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON
)
@Tag(name = "Admin Organization Agreements Management")
public class OrganizationAgreementController {

	static final String CONTROLLER_URI = "/api/organization-agreement";

	@Autowired
	private OrganizationAgreementService organizationAgreementService;

	/**
	 * Get Organization Agreement Items List for current Risk Model
	 *
	 * @return Organization Agreement Items List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Organization Agreement Items List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, OrganizationAgreementViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, OrganizationAgreementViewDTO> result = organizationAgreementService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Organization Agreement details
	 *
	 * @return Organization Agreement Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Organization Agreement details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public OrganizationAgreementEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		OrganizationAgreementEditDTO itemDTO = organizationAgreementService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Organization Agreement
	 *
	 * @return New Organization Agreement
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Organization Agreement", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public OrganizationAgreementEditDTO create(
		@Parameter(description = "Organization Agreement Details", required = true) @RequestBody OrganizationAgreementEditDTO newItemDTO
	) {

		OrganizationAgreementEditDTO result = organizationAgreementService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Organization Agreement
	 *
	 * @return Updated Organization Agreement
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Organization Agreement", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public OrganizationAgreementEditDTO update(
		@Parameter(description = "Organization Agreement update Details", required = true) @RequestBody OrganizationAgreementEditDTO itemDTO
	) {

		OrganizationAgreementEditDTO result = organizationAgreementService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Organization Agreement
	 *
	 * @return ID of removed item
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Organization Agreement", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(
		@Parameter(description = "Simple Organization Agreement Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = organizationAgreementService.delete(itemDTO.getId());

		return result;
	}

}
