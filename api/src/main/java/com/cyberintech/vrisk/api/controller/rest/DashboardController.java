package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.dashboards.DashboardService;
import com.cyberintech.vrisk.server.service.dashboards.powerbi.AzureADService;
import com.cyberintech.vrisk.server.service.dashboards.powerbi.PowerBIConfig;
import com.cyberintech.vrisk.server.service.dashboards.powerbi.PowerBIService;
import com.cyberintech.vrisk.server.service.dashboards.powerbi.model.EmbedConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Dashboards management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-05
 */
@RestController
@RequestMapping(
	value = DashboardController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Dashboards Management Controller"
)
@Tag(name = "Dashboards Management")
public class DashboardController {

	static final String CONTROLLER_URI = "/api/dashboards";

//	@Autowired
//	private BusinessUnitService businessUnitService;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private AzureADService azureADService;

	@Autowired
	private PowerBIService powerBIService;

	@Autowired
	private PowerBIConfig powerBIConfig;

	/**
	 * Get Dashboards List for current Risk Model
	 *
	 * @return Dashboards List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Dashboards List for current Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<DashboardRefDTO> getListFiltered(
		@Parameter(example = "101") @RequestHeader("risk-model-id") @NotNull @Size(min = 1) Long riskModelId
	) {

		List<DashboardRefDTO> result = dashboardService.getList();

		return result;
	}

	/**downloadAsCSV
	 * Download Questions Status Excel for Dashboard and current Risk Model
	 *
	 * @return Excel Document
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/download/questions-status", name = "Download Question Status Report")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadQuestionsStatus(
		HttpServletResponse response,
		@Parameter(example = "101") @RequestParam("riskModelId") @NotNull @Size(min = 1) Long riskModelId
	) throws IOException {
		// Build Download Template
		ByteArrayOutputStream byteArrayOutputStream = dashboardService.getQuestionStatusReport(riskModelId);

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		String fileName = applicationProperties.buildExportFileName("Report_QuestionStatus", organization.getName(), new SimpleDateFormat("yyyyMMdd").format(new Date()), "xlsx");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		byteArrayOutputStream.writeTo(outputStream);
		// outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Download Questions Status Excel for Dashboard, Search State and current Risk Model
	 *
	 * @return Excel Document
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/download/report", name = "Download Question Status Report")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadQuestionsStatusSearch(
		HttpServletResponse response,
		@Parameter(example = "101") @RequestParam("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(example = "") @RequestParam("dashboardRefUUID") @NotNull @Size(min = 1) String dashboardRefUUID
	) throws IOException {
		// Build Download Template
		ByteArrayOutputStream byteArrayOutputStream = dashboardService.getReportContent(riskModelId, dashboardRefUUID);

		// Build HTTP Response
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		String fileName = applicationProperties.buildExportFileName("Report", organization.getName(), new SimpleDateFormat("yyyyMMdd").format(new Date()), "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		byteArrayOutputStream.writeTo(outputStream);
	}

	/**
	 * Get Dashboard details
	 *
	 * @return Dashboard Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Dashboard details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public DashboardDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId,
		@Parameter(example = "101") @RequestHeader("risk-model-id") @NotNull @Size(min = 1) Long riskModelId
	) {

		DashboardDTO itemDTO = dashboardService.getDashboardDetails(itemId, riskModelId, null);

		return itemDTO;
	}

	/**
	 * Get Dashboard details filtered
	 *
	 * @return Dashboard Details
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/{itemId}/filter", name = "Get Dashboard details filtered")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public DashboardDTO getDetailsFiltered(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId,
		@Parameter(description = "Dashboard State Object", required = true) @RequestBody DashboardStateDTO dashboardState,
		@Parameter(example = "101") @RequestHeader("risk-model-id") @NotNull @Size(min = 1) Long riskModelId
	) {

		DashboardDTO itemDTO = dashboardService.getDashboardDetails(itemId, riskModelId, dashboardState);

		return itemDTO;
	}

	/**
	 * Get Dashboard details
	 *
	 * @return Dashboard Details
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/get-drilldown-dashboard", name = "Get Dashboard details for Drilldown")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public DashboardDTO getDetailsForDrilldown(
		@Parameter(example = "101") @RequestHeader("risk-model-id") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Dashboard Drilldown Object", required = true) @RequestBody DashboardDataItemDrilldownDTO drilldown
	) {

		DashboardDTO itemDTO = dashboardService.getDashboardDetails(drilldown, riskModelId);

		return itemDTO;
	}

	/**
	 * Save Dashboard items data
	 *
	 * @return Dashboard items data
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/save-items-data", name = "Get Dashboard details for Drilldown")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<DashboardItemDTO> saveDashboardData(
		@Parameter(example = "101") @RequestHeader("risk-model-id") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Dashboard items list to save", required = true) @RequestBody List<DashboardItemDTO> items
	) {
		List<DashboardItemDTO> result = dashboardService.saveDashboardData(riskModelId, items);

		return result;
	}

	/**
	 * Get Power BI Token Details
	 *
	 * @return Dashboard items data
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/powerbi/embed-config", name = "Get Power BI Token Details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public EmbedConfig getDashboardDataToken(
		@Parameter(example = "101") @RequestHeader("risk-model-id") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Dashboard items list to save", required = true) @RequestBody DashboardIFrameItemDTO dashboardDetails
	) throws MalformedURLException, ExecutionException, InterruptedException, JsonProcessingException {

		String accessToken = azureADService.getAccessToken(dashboardDetails.getAccessDetails().getApplicationId());
		EmbedConfig embedConfig = powerBIService.getEmbedConfig(accessToken, dashboardDetails.getAccessDetails().getWorkspaceId(), dashboardDetails.getAccessDetails().getReportId());

		return embedConfig;
	}

}
