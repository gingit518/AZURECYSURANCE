package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemToolRiskReductionDTO;
import com.cyberintech.vrisk.server.service.SystemToolRiskReductionService;
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
import java.util.ArrayList;
import java.util.List;

/**
 * System Tool Risk Reduction management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = SystemToolRiskReductionServiceController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "System Tool Risk Reduction Management Controller"
)
@Tag(name = "System Tool Risk Reduction Management")
public class SystemToolRiskReductionServiceController {

	static final String CONTROLLER_URI = "/api/system-tool-risk-reduction";

	@Autowired
	private SystemToolRiskReductionService systemToolRiskReductionService;

	/**
	 * Get System Tool Risk Reduction List for current Risk Model
	 *
	 * @return System Tool Risk Reduction List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "System Tool Risk Reduction List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_RISK_REDUCTION_READ)")
	public FilteredResponse<NameFilter, SystemToolRiskReductionDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, SystemToolRiskReductionDTO> result = systemToolRiskReductionService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get System Tool Risk Reduction details
	 *
	 * @return System Tool Risk Reduction Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get System Tool Risk Reduction details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_RISK_REDUCTION_READ)")
	public SystemToolRiskReductionDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		SystemToolRiskReductionDTO itemDTO = systemToolRiskReductionService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new System Tool Risk Reduction
	 *
	 * @return New System Tool Risk Reduction
	 */
	@RequestMapping(method = RequestMethod.POST, value = "save", name = "Synchronize the list of System Tool Risk Reduction", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_RISK_REDUCTION_UPDATE)")
	public List<SystemToolRiskReductionDTO> saveList(
		@Parameter(description = "System Tool Risk Reduction Details", required = true) @RequestBody List<SystemToolRiskReductionDTO> itemsList
	) {

		List<SystemToolRiskReductionDTO> result = new ArrayList<>();

		for (SystemToolRiskReductionDTO item : itemsList) {
			if (item.getToolPrice() != null || item.getRiskReduction() != null || item.getRiskReductionPercent() != null) {
				SystemToolRiskReductionDTO saveResult = (item.getId() != null) ? systemToolRiskReductionService.update(item) : systemToolRiskReductionService.create(item);
				result.add(saveResult);
			}
		}

		return result;
	}

	/**
	 * Create new System Tool Risk Reduction
	 *
	 * @return New System Tool Risk Reduction
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new System Tool Risk Reduction", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public SystemToolRiskReductionDTO create(
		@Parameter(description = "System Tool Risk Reduction Details", required = true) @RequestBody SystemToolRiskReductionDTO newItemDTO
	) {

		SystemToolRiskReductionDTO result = systemToolRiskReductionService.create(newItemDTO);

		return result;
	}

	/**
	 * Update System Tool Risk Reduction
	 *
	 * @return Updated System Tool Risk Reduction
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing System Tool Risk Reduction", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_RISK_REDUCTION_UPDATE)")
	public SystemToolRiskReductionDTO update(
		@Parameter(description = "System Tool Risk Reduction update Details", required = true) @RequestBody SystemToolRiskReductionDTO itemDTO
	) {

		SystemToolRiskReductionDTO result = systemToolRiskReductionService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes System Tool Risk Reduction
	 *
	 * @return ID of removed System Tool Risk Reduction
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing System Tool Risk Reduction", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(
		@Parameter(description = "Simple System Tool Risk Reduction Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = systemToolRiskReductionService.delete(itemDTO.getId());

		return result;
	}

}
