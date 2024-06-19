package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.BaseFilter;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.metric_risk.MetricRisksEditDTO;
import com.cyberintech.vrisk.server.model.dto.metric_risk.MetricRisksViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.MetricDomain;
import com.cyberintech.vrisk.server.service.MetricRisksService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

/**
 * Amplification Metrics management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-20
 */
@RestController
@RequestMapping(
	value = AmplificationMetricsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Amplification Metrics Management Controller"
)
@Tag(name = "Amplification Metrics Management")
public class AmplificationMetricsController {

	static final String CONTROLLER_URI = "/api/risk-models/{riskModelId}/amplification-metrics";

	@Autowired
	private MetricRisksService amplificationMetricsService;

	/**
	 * Get Amplification Metrics List for current Organization
	 *
	 * @return Amplification Metrics List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Amplification Metrics List for current Organization and Risk Model")
	@Parameters({
		@Parameter(name = "size", description = "Size of the Page", example = "10", required = true, in = ParameterIn.QUERY),
		@Parameter(name = "page", description = "Number of current page", example = "0", required = true, in = ParameterIn.QUERY),
		@Parameter(name = "sort", description = "Sort parameters of the result", example = "id,DESC", required = false, in = ParameterIn.QUERY),
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<BaseFilter, MetricRisksViewDTO> getListPaged(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(hidden = true) Pageable pageable
	) {

		List<Long> amplification = Arrays.asList(MetricDomain.AMPLIFIED_REPUTATION.getId(), MetricDomain.AMPLIFIED_OPERATIONAL.getId(), MetricDomain.AMPLIFIED_LEGAL.getId());
		FilteredResponse<BaseFilter, MetricRisksViewDTO> result = amplificationMetricsService.getListByRiskModel(riskModelId, amplification, pageable);

		return result;
	}

	/**
	 * Get Amplification Metric details
	 *
	 * @return Amplification Metric Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Amplification Metric details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public MetricRisksViewDTO getDetails(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		MetricRisksViewDTO itemDTO = amplificationMetricsService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Amplification Metric
	 *
	 * @return New Amplification Metric
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Amplification Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public MetricRisksViewDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Amplification Metric Details", required = true) @RequestBody MetricRisksEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);
		MetricRisksViewDTO result = amplificationMetricsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Amplification Metric
	 *
	 * @return Updated Amplification Metric
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Amplification Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public MetricRisksViewDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Amplification Metric Details", required = true) @RequestBody MetricRisksEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		MetricRisksViewDTO result = amplificationMetricsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Amplification Metric
	 *
	 * @return ID of removed Amplification Metric
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Amplification Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(@Parameter(description = "Amplification Metric Details", required = true) @RequestBody MetricRisksEditDTO itemDTO) {

		Long result = amplificationMetricsService.delete(itemDTO.getId());

		return result;
	}

}
