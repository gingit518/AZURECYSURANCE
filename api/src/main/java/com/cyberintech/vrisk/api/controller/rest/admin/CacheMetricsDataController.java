package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.model.data.CacheMetricsFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.risk_model.CacheMetricsDataDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.QuantsService;
import com.cyberintech.vrisk.server.service.RiskModelCalculationsService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

/**
 * Quants management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-17
 */
@RestController
@RequestMapping(
	value = CacheMetricsDataController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Cache Metrics Data Controller"
)
@Tag(name = "Admin Cache Metrics Data Controller")
public class CacheMetricsDataController {

	static final String CONTROLLER_URI = "/api/admin/cache-metrics-data";

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private QuantsService quantsService;

	@Autowired
	private RiskModelCalculationsService riskModelCalculationsService;

	/**
	 * Rebuild all quants metrics cache
	 *
	 * @return OK status
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/rebuild-metrics-cache", name = "Rebuild all Quants metrics cache")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public String rebuildAllMetricsCache() {

		riskModelCalculationsService.rebuildAllMetricsCacheAsync();

		return "OK";
	}

	/**
	 * Rebuild metrics cache for Risk Model
	 *
	 * @return Risk Model Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/rebuild-metrics-cache/{itemId}", name = "Rebuild metrics cache for Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
//	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_RECALCULATE_CACHE)")
	public Boolean rebuildMetricsCacheForRiskModel(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {
		return riskModelCalculationsService.rebuildMetricsCache(itemId);
	}

	/**
	 * Get Quants List
	 *
	 * @return Quants List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/export/csv", name = "Cache Metrics Data List for current Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = false, in = ParameterIn.HEADER)
		, @Parameter(name = "api-key", description = "API key Access token", example = "", required = false, in = ParameterIn.HEADER)
	})
	@Produces("application/vnd.ms-excel")
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_RECALCULATE_CACHE)")
	public void exportAsCSV(
		HttpServletResponse response
	) throws IOException {

		// Get data to Download
		ByteArrayInputStream byteArrayInputStream = riskModelCalculationsService.getDownloadData();

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("CacheMetrics", organization.getName(), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Get Quants List
	 *
	 * @return Quants List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Cache Metrics Data List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public FilteredResponse<CacheMetricsFilter, CacheMetricsDataDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<CacheMetricsFilter> filteredRequest
	) {

		FilteredResponse<CacheMetricsFilter, CacheMetricsDataDTO> result = riskModelCalculationsService.getCacheMetricsListFiltered(filteredRequest);

		return result;
	}


	/**
	 * Get Cache Metric details
	 *
	 * @return Cache Metric details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Cache Metric details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.isSuperAdmin()")
	public CacheMetricsDataDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		CacheMetricsDataDTO itemDTO = riskModelCalculationsService.getDetails(itemId);

		return itemDTO;
	}
}
