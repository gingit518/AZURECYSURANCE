package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.qual_metrics.MetricDomainViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.MetricDomain;
import com.cyberintech.vrisk.server.model.jpa.domains.VendorType;
import com.cyberintech.vrisk.server.service.MetricDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple Metric Domains controller. Used for Metric Domains Listing
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-06
 */
@RestController
@RequestMapping(
	value = MetricDomainController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Metric Domains Management Controller"
)
@Tag(name = "Metric Domains Viewer")
public class MetricDomainController {

	static final String CONTROLLER_URI = "/api/metric-domains";

	@Autowired
	private MetricDomainService metricDomainService;

	/**
	 * Get Metric Domains List
	 *
	 * @return Metric Domains List
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Get Metric Domains List")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<MetricDomainViewDTO> getList() {

		List<MetricDomainViewDTO> metricDomains = metricDomainService.getList();

		List<MetricDomainViewDTO> result = metricDomains;

		return result;
	}

	/**
	 * Get Metric Domains List
	 *
	 * @return Metric Domains List
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(method = RequestMethod.GET, value = "/filtered", name = "Get Metric Domains List")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<MetricDomainViewDTO> getListFiltered() {

		List<MetricDomainViewDTO> metricDomains = metricDomainService.getList();

		List<MetricDomainViewDTO> result = metricDomains.stream().filter(metricDomainViewDTO -> !MetricDomain.CYBERSECURITY_MATURITY.getId().equals(metricDomainViewDTO.getId())).collect(Collectors.toList());

		return result;
	}

	/**
	 * Get List of Metric Domains which are not empty
	 *
	 * @return Get List of Metric Domains which are not empty
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(method = RequestMethod.GET, value = "/list/risk-model/{riskModelId}/{metricType}", name = "Get List of Metric Domains which are not empty")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public List<MetricDomainViewDTO> getListByTypeAndNotEmpty(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@PathVariable("metricType") @NotNull @Size(min = 1) VendorType metricType,
		@QueryParam("area") String area
	) {

		List<MetricDomainViewDTO> result = metricDomainService.getListByTypeAndNotEmpty(metricType, riskModelId, area);

		// List<MetricDomainViewDTO> result = metricDomains.stream().filter(metricDomainViewDTO -> !MetricDomain.CYBERSECURITY_MATURITY.getId().equals(metricDomainViewDTO.getId())).collect(Collectors.toList());

		return result;
	}

	/**
	 * Get List of Metric Domains which are not empty
	 *
	 * @return Get List of Metric Domains which are not empty
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(method = RequestMethod.GET, value = "/list-pages/risk-model/{riskModelId}/{metricType}", name = "Get List of Metric Domains pages which are not empty")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public List<String> getListPagesByTypeAndNotEmpty(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@PathVariable("metricType") @NotNull @Size(min = 1) VendorType metricType
	) {
		List<String> result = metricDomainService.getAllCodesByTypeAndNotEmpty(metricType, riskModelId);

		return result;
	}

}
