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
 * Likelihood Metrics management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-20
 */
@RestController
@RequestMapping(
	value = LikelihoodMetricsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Likelihood Metrics Management Controller"
)
@Tag(name = "Likelihood Metrics Management")
public class LikelihoodMetricsController {

	static final String CONTROLLER_URI = "/api/risk-models/{riskModelId}/likelihood-metrics";

	@Autowired
	private MetricRisksService likelihoodMetricsService;

	/**
	 * Get Likelihood Metrics List for current Organization
	 *
	 * @return Likelihood Metrics List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Likelihood Metrics List for current Organization and Risk Model")
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

		List<Long> likelihood = Arrays.asList(MetricDomain.LIKELIHOOD.getId());
		FilteredResponse<BaseFilter, MetricRisksViewDTO> result = likelihoodMetricsService.getListByRiskModel(riskModelId, likelihood, pageable);

		return result;
	}

	/**
	 * Get Likelihood Metric details
	 *
	 * @return Likelihood Metric Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Likelihood Metric details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public MetricRisksViewDTO getDetails(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		MetricRisksViewDTO itemDTO = likelihoodMetricsService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Likelihood Metric
	 *
	 * @return New Likelihood Metric
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Likelihood Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public MetricRisksViewDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Likelihood Metric Details", required = true) @RequestBody MetricRisksEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);
		newItemDTO.setMetricDomainId(MetricDomain.LIKELIHOOD.getId());
		MetricRisksViewDTO result = likelihoodMetricsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Likelihood Metric
	 *
	 * @return Updated Likelihood Metric
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Likelihood Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public MetricRisksViewDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Likelihood Metric Details", required = true) @RequestBody MetricRisksEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		MetricRisksViewDTO result = likelihoodMetricsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Likelihood Metric
	 *
	 * @return ID of removed Likelihood Metric
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Likelihood Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(@Parameter(description = "Likelihood Metric Details", required = true) @RequestBody MetricRisksEditDTO itemDTO) {

		Long result = likelihoodMetricsService.delete(itemDTO.getId());

		return result;
	}

}
