package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantsEditDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantsViewDTO;
import com.cyberintech.vrisk.server.service.QuantsService;
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
 * Quants management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-17
 */
@RestController
@RequestMapping(
	value = QuantsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Quants Management Controller"
)
@Tag(name = "Quants Management")
public class QuantsController {

	static final String CONTROLLER_URI = "/api/risk-models/{riskModelId}/quants";

	@Autowired
	private QuantsService quantsService;

	/**
	 * Get Quants List for current Organization
	 *
	 * @return Quants List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Quants List for current Organization and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUANTS_READ)")
	public List<QuantsViewDTO> getList(@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId) {

		List<QuantsViewDTO> result = quantsService.getList();

		return result;
	}

	/**
	 * Get Quants List for current Risk Model
	 *
	 * @return Quants List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Quants List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUANTS_READ)")
	public FilteredResponse<NameFilter, QuantsViewDTO> getListFiltered(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, QuantsViewDTO> result = quantsService.getListFiltered(riskModelId, filteredRequest);

		return result;
	}

	/**
	 * Get Quant details
	 *
	 * @return Quant Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Quant details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUANTS_READ)")
	public QuantsEditDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		QuantsEditDTO itemDTO = quantsService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Quant
	 *
	 * @return New Quant
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Quant", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public QuantsEditDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Quant Details", required = true) @RequestBody QuantsEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);

		QuantsEditDTO result = quantsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Quant
	 *
	 * @return Updated Quant
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Quant", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public QuantsEditDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "User update Details", required = true) @RequestBody QuantsEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		QuantsEditDTO result = quantsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Quant Metric
	 *
	 * @return ID of removed Quant Metric
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Quant", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public Long delete(@Parameter(description = "Simple Quant Metric Details", required = true) @RequestBody ItemViewDTO itemDTO) {

		Long result = itemDTO.getId();

		quantsService.delete(itemDTO.getId());

		return result;
	}

}
