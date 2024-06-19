package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.policy.PolicyEditDTO;
import com.cyberintech.vrisk.server.model.dto.policy.PolicyReviewDTO;
import com.cyberintech.vrisk.server.model.dto.policy.PolicyViewDTO;
import com.cyberintech.vrisk.server.service.PolicyService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;

/**
 * Policies management Controller. Implements basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-01-09
 */
@RestController
@RequestMapping(
	value = PolicyController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON
)
@Tag(name = "Policy Management")
public class PolicyController {

	static final String CONTROLLER_URI = "/api/policy";

	@Autowired
	private PolicyService policyService;

	/**
	 * Get Policy Items List for current Risk Model
	 *
	 * @return Policy Items List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Policy Items List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).POLICY_READ)")
	public FilteredResponse<NameFilter, PolicyViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, PolicyViewDTO> result = policyService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Policy details
	 *
	 * @return Policy Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Policy details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).POLICY_READ)")
	public PolicyEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		PolicyEditDTO itemDTO = policyService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Policy
	 *
	 * @return New Policy
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Policy", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).POLICY_CREATE)")
	public PolicyEditDTO create(
		@Parameter(description = "Policy Details", required = true) @Valid @RequestBody PolicyEditDTO newItemDTO
	) {

		PolicyEditDTO result = policyService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Policy
	 *
	 * @return Updated Policy
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Policy", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).POLICY_UPDATE)")
	public PolicyEditDTO update(
		@Parameter(description = "Policy update Details", required = true) @Valid @RequestBody PolicyEditDTO itemDTO
	) {

		PolicyEditDTO result = policyService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Policy
	 *
	 * @return ID of removed Policy
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Policy", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).POLICY_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Policy Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = policyService.delete(itemDTO.getId());

		return result;
	}


	/**
	 * Get Policy details
	 *
	 * @return Policy Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/review/{itemId}", name = "Get Policy Review details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).POLICY_ANNUAL_REVIEW)")
	public PolicyReviewDTO getReviewDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		PolicyReviewDTO itemDTO = policyService.getReviewDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Policy
	 *
	 * @return New Policy
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/review", name = "Create new Policy Review", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).POLICY_ANNUAL_REVIEW)")
	public PolicyReviewDTO createReview(
		@Parameter(description = "Policy Details", required = true) @Valid @RequestBody PolicyReviewDTO newItemDTO
	) {
		PolicyReviewDTO result = policyService.createReview(newItemDTO);

		return result;
	}

}
