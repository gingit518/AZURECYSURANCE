package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantMetricsEditDTO;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantMetricsViewDTO;
import com.cyberintech.vrisk.server.service.QuantMetricsService;
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
 * Quant Metrics management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-20
 */
@RestController
@RequestMapping(
	value = QuantMetricsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Quant Metrics Management Controller"
)
@Tag(name = "Quant Metrics Management")
public class QuantMetricsController {

	static final String CONTROLLER_URI = "/api/risk-models/{riskModelId}/quant-metrics";

	@Autowired
	private QuantMetricsService quantMetricsService;

	/**
	 * Get Quant Metrics List for current Organization
	 *
	 * @return Quant Metrics List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Quant Metrics List for current Organization and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUANT_METRIC_READ)")
	public List<QuantMetricsViewDTO> getList(@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId) {

		List<QuantMetricsViewDTO> result = quantMetricsService.getListByRiskModel(riskModelId);

		return result;
	}

	/**
	 * Get Quant Metrics List for current Risk Model
	 *
	 * @return Quant Metrics List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Quant Metrics List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUANT_METRIC_READ)")
	public FilteredResponse<NameFilter, QuantMetricsViewDTO> getListFiltered(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, QuantMetricsViewDTO> result = quantMetricsService.getListFiltered(riskModelId, filteredRequest);

		return result;
	}

	/**
	 * Get Quant Metric details
	 *
	 * @return Quant Metric Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Quant Metric details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUANT_METRIC_READ)")
	public QuantMetricsEditDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		QuantMetricsEditDTO itemDTO = quantMetricsService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Quant Metric
	 *
	 * @return New Quant Metric
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Quant Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUANT_METRIC_CREATE)")
	public QuantMetricsEditDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Quant Metric Details", required = true) @RequestBody QuantMetricsEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);

		QuantMetricsEditDTO result = quantMetricsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Quant Metric
	 *
	 * @return Updated Quant Metric
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Quant Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUANT_METRIC_UPDATE)")
	public QuantMetricsEditDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "User update Details", required = true) @RequestBody QuantMetricsEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		QuantMetricsEditDTO result = quantMetricsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Quant Metric
	 *
	 * @return ID of removed Quant Metric
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Quant Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUANT_METRIC_DELETE)")
	public Long delete(@Parameter(description = "Simple Quant Metric Details", required = true) @RequestBody ItemViewDTO itemDTO) {

		Long result = itemDTO.getId();

		quantMetricsService.delete(itemDTO.getId());

		return result;
	}
}
