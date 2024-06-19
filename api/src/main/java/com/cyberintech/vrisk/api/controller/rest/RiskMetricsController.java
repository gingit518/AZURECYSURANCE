package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_metrics.RiskMetricsViewDTO;
import com.cyberintech.vrisk.server.service.RiskMetricsService;
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
 * Risk Metrics management controller. Basic risk model CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-02-14
 */
@RestController
@RequestMapping(
	value = RiskMetricsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Risk Metrics Management Controller"
)
@Tag(name = "Risk Metrics Management")
public class RiskMetricsController {

	static final String CONTROLLER_URI = "/api/risk-models/{riskModelId}/risk-metrics";

	@Autowired
	private RiskMetricsService riskMetricsService;

	/**
	 * Get Risk Metrics List for current Organization
	 *
	 * @return Risk Metrics List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Risk Metrics List for current Organization and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_METRIC_READ)")
	public List<RiskMetricsViewDTO> getList(@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId) {

		List<RiskMetricsViewDTO> result = riskMetricsService.getListByRiskModel(riskModelId);

		return result;
	}

	/**
	 * Get Risk Metrics List for current Risk Model
	 *
	 * @return Risk Metrics List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Risk Metrics List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_METRIC_READ)")
	public FilteredResponse<NameFilter, RiskMetricsViewDTO> getListFiltered(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, RiskMetricsViewDTO> result = riskMetricsService.getListFiltered(riskModelId, filteredRequest);

		return result;
	}

	/**
	 * Get Risk Metric details
	 *
	 * @return Risk Metric Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Quant Metric details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_METRIC_READ)")
	public RiskMetricsViewDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		RiskMetricsViewDTO itemDTO = riskMetricsService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Risk Metric
	 *
	 * @return New Risk Metric
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Quant Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_METRIC_CREATE)")
	public RiskMetricsViewDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Risk Metric Details", required = true) @RequestBody RiskMetricsViewDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);

		RiskMetricsViewDTO result = riskMetricsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Risk Metric
	 *
	 * @return Updated Risk Metric
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Quant Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_METRIC_UPDATE)")
	public RiskMetricsViewDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "User update Details", required = true) @RequestBody RiskMetricsViewDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		RiskMetricsViewDTO result = riskMetricsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Risk Metric
	 *
	 * @return ID of removed Risk Metric
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Risk Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_METRIC_DELETE)")
	public Long delete(@Parameter(description = "Simple Risk Metric Details", required = true) @RequestBody ItemViewDTO itemDTO) {

		Long result = itemDTO.getId();

		riskMetricsService.delete(itemDTO.getId());

		return result;
	}
}
