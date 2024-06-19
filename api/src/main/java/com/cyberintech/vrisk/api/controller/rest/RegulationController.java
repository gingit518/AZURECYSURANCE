package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.regulations.RegulationDTO;
import com.cyberintech.vrisk.server.service.RegulationService;
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
import java.util.List;

/**
 * Regulations controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
@RestController
@RequestMapping(
	value = RegulationController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Regulation Management Controller"
)
@Tag(name = "Regulation Management")
public class RegulationController {

	static final String CONTROLLER_URI = "/api/regulations";

	@Autowired
	private RegulationService regulationService;

	/**
	 * Get Regulations List
	 *
	 * @return Regulations List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Rate Type List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<RegulationDTO> getList() {

		List<RegulationDTO> result = regulationService.getList();

		return result;
	}

	/**
	 * Get Regulations List for current Risk Model
	 *
	 * @return Regulations List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Regulations List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, RegulationDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {
		return regulationService.getListFiltered(filteredRequest);
	}

	/**
	 * Get Regulation details
	 *
	 * @return Regulation Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Regulation details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).REGULATION_READ)")
	public RegulationDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		RegulationDTO itemDTO = regulationService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Regulation
	 *
	 * @return New Regulation
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Regulation", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).REGULATION_CREATE)")
	public RegulationDTO create(
		@Parameter(description = "Regulation Details", required = true) @RequestBody RegulationDTO newItemDTO
	) {

		RegulationDTO result = regulationService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Regulation
	 *
	 * @return Updated Regulation
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update Regulation", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).REGULATION_UPDATE)")
	public RegulationDTO update(
		@Parameter(description = "Regulation update Details", required = true) @RequestBody RegulationDTO itemDTO
	) {

		RegulationDTO result = regulationService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Regulation
	 *
	 * @return ID of removed Regulation
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Regulation", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).REGULATION_DELETE)")
	public Long delete(
		@Parameter(description = "Regulation Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = regulationService.delete(itemDTO.getId());

		return result;
	}
}
