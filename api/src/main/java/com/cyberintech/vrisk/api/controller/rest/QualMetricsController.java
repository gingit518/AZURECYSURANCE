package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.MetricResultDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualMetricsEditDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualMetricsViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.MetricDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.model.jpa.entity.MetricDomains;
import com.cyberintech.vrisk.server.service.QualMetricsService;
import com.cyberintech.vrisk.server.service.dashboards.MetricResult;
import com.cyberintech.vrisk.server.service.dashboards.ScoringQuestionsDashboardService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Qual Metrics management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-20
 */
@RestController
@RequestMapping(
	value = QualMetricsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Qual Metrics Management Controller"
)
@Tag(name = "Qual Metrics Management")
public class QualMetricsController {

	static final String CONTROLLER_URI = "/api/risk-models/{riskModelId}/qual-metrics";

	@Autowired
	private QualMetricsService qualMetricsService;

	@Autowired
	private ScoringQuestionsDashboardService scoringQuestionsDashboardService;

	/**
	 * Get Qual Metrics List for current Organization
	 *
	 * @return Qual Metrics List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Qual Metrics List for current Organization and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_METRIC_READ)")
	public List<QualMetricsViewDTO> getList(@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId) {

		List<QualMetricsViewDTO> result = qualMetricsService.getListByRiskModel(riskModelId);

		return result;
	}

	/**
	 * Get Risk Model Domain details
	 *
	 * @return Risk Model Domain Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/calculate/organization", name = "Get Risk Model Domain details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_METRIC_READ)")
	public Map<String, MetricResultDTO> calculateOrganizationMetrics(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId
		// , @PathVariable("metricDomain") @NotNull @Size(min = 1) MetricDomain metricDomain
	) {

		Map<String, MetricResultDTO> resultMap = new HashMap<>();

		Map<MetricDomains, MetricResult> organizationScoringQualDataMap = scoringQuestionsDashboardService.getOrganizationScoringData(riskModelId, Arrays.asList(VendorType.Organization));
		for (Map.Entry<MetricDomains, MetricResult> organizationScoringQual : organizationScoringQualDataMap.entrySet()) {
			MetricResultDTO metricResult = MetricResultDTO.of(organizationScoringQual.getValue());
			if (MetricDomain.FFIEC_CAT_INHERENT_RISK.getCode().equalsIgnoreCase(organizationScoringQual.getKey().getCode())
				|| MetricDomain.CYBERSECURITY_MATURITY.getCode().equalsIgnoreCase(organizationScoringQual.getKey().getCode())) {
				metricResult.setHideCalculation(true);
			}
			resultMap.put(organizationScoringQual.getKey().getCode(), metricResult);
		}

		return resultMap;
	}

	/**
	 * Get Risk Model Domain details
	 *
	 * @return Risk Model Domain Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Risk Model Domain details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_METRIC_READ)")
	public QualMetricsViewDTO getDetails(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		QualMetricsViewDTO itemDTO = qualMetricsService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Risk Model Domain
	 *
	 * @return New Risk Model Domain
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Risk Model Domain", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_METRIC_CREATE)")
	public QualMetricsViewDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Risk Model Domain Details", required = true) @RequestBody QualMetricsEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);

		QualMetricsViewDTO result = qualMetricsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Risk Model Domain
	 *
	 * @return Updated Risk Model Domain
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Risk Model Domain", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_METRIC_UPDATE)")
	public QualMetricsViewDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "User update Details", required = true) @RequestBody QualMetricsEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		QualMetricsViewDTO result = qualMetricsService.update(itemDTO);

		return result;
	}


	/**
	 * Deletes Qualitative metric
	 *
	 * @return ID of removed Qualitative metric
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Qualitative metric", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).QUAL_METRIC_UPDATE)")
	public Long delete(
		@Parameter(description = "Item Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = qualMetricsService.delete(itemDTO.getId());

		return result;
	}

}
