package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.external_analytics.ExternalAnalyticsAccessDTO;
import com.cyberintech.vrisk.server.model.dto.external_analytics.ExternalAnalyticsDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import com.cyberintech.vrisk.server.service.ExternalAnalyticsService;
import com.cyberintech.vrisk.server.service.dashboards.AnalyticDashboardsService;
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
 * External Analytics management controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
@RestController
@RequestMapping(
	value = ExternalAnalyticsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "External Analytics Management Controller"
)
@Tag(name = "External Analytics Management")
public class ExternalAnalyticsController {

	static final String CONTROLLER_URI = "/api/external-analytics";

	@Autowired
	private ExternalAnalyticsService externalAnalyticsService;

	@Autowired
	private AnalyticDashboardsService analyticDashboardsService;

	/**
	 * Get External Analytics List
	 *
	 * @return External Analytics List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Rate Type List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<ExternalAnalyticsDTO> getList() {

		List<ExternalAnalyticsDTO> result = externalAnalyticsService.getList();

		return result;
	}

	/**
	 * Get External Analytics List for current Risk Model
	 *
	 * @return External Analytics List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "External Analytics List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).EXTERNAL_ANALYTICS_READ)")
	public FilteredResponse<NameFilter, ExternalAnalyticsDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {
		return externalAnalyticsService.getListFiltered(filteredRequest);
	}

	/**
	 * Get External Analytics access details
	 *
	 * @return External Analytics access Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/access-details/{analyticsType}", name = "Get External Analytics access details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).EXTERNAL_ANALYTICS_READ)")
	public ExternalAnalyticsAccessDTO getAccessDetails(
		@PathVariable("analyticsType") @NotNull @Size(min = 1) ExternalAnalyticsType analyticsType
	) {

		ExternalAnalyticsAccessDTO itemDTO = null;

		return itemDTO;
	}

	/**
	 * Get External Analytics details
	 *
	 * @return External Analytics Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get External Analytics details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).EXTERNAL_ANALYTICS_READ)")
	public ExternalAnalyticsDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		ExternalAnalyticsDTO itemDTO = externalAnalyticsService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new External Analytics
	 *
	 * @return New External Analytics
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new External Analytics", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).EXTERNAL_ANALYTICS_CREATE)")
	public ExternalAnalyticsDTO create(
		@Parameter(description = "External Analytics Details", required = true) @RequestBody ExternalAnalyticsDTO newItemDTO
	) {

		ExternalAnalyticsDTO result = externalAnalyticsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update External Analytics
	 *
	 * @return Updated External Analytics
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update External Analytics", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).EXTERNAL_ANALYTICS_UPDATE)")
	public ExternalAnalyticsDTO update(
		@Parameter(description = "External Analytics update Details", required = true) @RequestBody ExternalAnalyticsDTO itemDTO
	) {

		ExternalAnalyticsDTO result = externalAnalyticsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes External Analytics
	 *
	 * @return ID of removed External Analytics
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing External Analytics", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).EXTERNAL_ANALYTICS_DELETE)")
	public Long delete(
		@Parameter(description = "External Analytics Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = externalAnalyticsService.delete(itemDTO.getId());

		return result;
	}
}
