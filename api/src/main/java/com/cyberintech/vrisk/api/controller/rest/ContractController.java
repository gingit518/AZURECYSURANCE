package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.contract.ContractDTO;
import com.cyberintech.vrisk.server.service.ContractService;
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
 * Contract management Controller. Implements basic CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-05-11
 */

@RestController
@RequestMapping(
	value = ContractController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Vendor Contract Management Controller"
)
@Tag(name = "Vendor Contract Management")
public class ContractController {

	static final String CONTROLLER_URI = "/api/vendor-contract";

	@Autowired
	private ContractService contractServise;


	/**
	 * Get Vendor Contract Items List
	 *
	 * @return Vendor Contract Items List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Vendor Contract Items List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTRACT_READ)")
	public FilteredResponse<NameFilter, ContractDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {
		return contractServise.getListFiltered(filteredRequest);
	}

	/**
	 * Get Vendor Contract details
	 *
	 * @return Vendor Contract Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Vendor Contract details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTRACT_READ)")
	public ContractDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {
		return contractServise.getDetails(itemId);
	}

	/**
	 * Create new Vendor Contract
	 *
	 * @return New Vendor Contract
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Vendor Contract", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTRACT_CREATE)")
	public ContractDTO create(
		@Parameter(description = "Vendor Contract Details", required = true) @Valid @RequestBody ContractDTO newItemDTO
	) {
		return contractServise.create(newItemDTO);
	}

	/**
	 * Update Vendor Contract
	 *
	 * @return Updated Vendor Contract
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Vendor Contract", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTRACT_UPDATE)")
	public ContractDTO update(
		@Parameter(description = "Vendor Contract update Details", required = true) @Valid @RequestBody ContractDTO itemDTO
	) {
		return contractServise.update(itemDTO);
	}

	/**
	 * Deletes Vendor Contract
	 *
	 * @return ID of removed Vendor Contract
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Vendor Contract", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CONTRACT_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Vendor Contract Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {
		return contractServise.delete(itemDTO.getId());
	}
}

