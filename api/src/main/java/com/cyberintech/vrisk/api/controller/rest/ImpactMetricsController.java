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
 * Impact Metrics management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-20
 */
@RestController
@RequestMapping(
	value = ImpactMetricsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Impact Metrics Management Controller"
)
@Tag(name = "Impact Metrics Management")
public class ImpactMetricsController {

	static final String CONTROLLER_URI = "/api/risk-models/{riskModelId}/impact-metrics";

	@Autowired
	private MetricRisksService impactMetricsService;

	/**
	 * Get Impact Metrics List for current Organization
	 *
	 * @return Impact Metrics List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Impact Metrics List for current Organization and Risk Model")
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

		List<Long> impact = Arrays.asList(MetricDomain.IMPACT.getId());
		FilteredResponse<BaseFilter, MetricRisksViewDTO> result = impactMetricsService.getListByRiskModel(riskModelId, impact, pageable);

		return result;
	}

	/**
	 * Get Impact Metric details
	 *
	 * @return Impact Metric Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Impact Metric details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public MetricRisksViewDTO getDetails(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		MetricRisksViewDTO itemDTO = impactMetricsService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Impact Metric
	 *
	 * @return New Impact Metric
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Impact Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public MetricRisksViewDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Impact Metric Details", required = true) @RequestBody MetricRisksEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);
		newItemDTO.setMetricDomainId(MetricDomain.IMPACT.getId());
		MetricRisksViewDTO result = impactMetricsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Impact Metric
	 *
	 * @return Updated Impact Metric
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Impact Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public MetricRisksViewDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Impact Metric Details", required = true) @RequestBody MetricRisksEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		MetricRisksViewDTO result = impactMetricsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Impact Metric
	 *
	 * @return ID of removed Impact Metric
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Impact Metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(@Parameter(description = "Impact Metric Details", required = true) @RequestBody MetricRisksEditDTO itemDTO) {

		Long result = impactMetricsService.delete(itemDTO.getId());

		return result;
	}

}
