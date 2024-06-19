package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.model.data.*;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.feeds.NewsFeedDTO;
import com.cyberintech.vrisk.server.model.dto.feeds.PaceCourseDTO;
import com.cyberintech.vrisk.server.model.dto.feeds.WebinarDTO;
import com.cyberintech.vrisk.server.model.dto.feeds.WhatsNewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.service.*;
import com.cyberintech.vrisk.server.service.admin.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

/**
 * Feeds management controller. Basic user CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-01-23
 */
@RestController
@RequestMapping(
	value = AdminFeedsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Feeds Management Controller"
)
@Tags(value = {@Tag(name = "Admin Feeds Management Controller"), @Tag(name = "Administration")})
public class AdminFeedsController {

	static final String CONTROLLER_URI = "/api/admin/feeds";

	@Autowired
	private NewsService newsService;

	@Autowired
	private PaceCourseService paceCourseService;

	@Autowired
	private WebinarService webinarService;

	@Autowired
	private WhatsNewService whatsNewService;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private AdminUserService adminUserService;


	/**
	 * Get Feeds List for current Filters
	 *
	 * @return Feeds List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/news", name = "List of latest news feeds")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<FeedsFilter, NewsFeedDTO> getFeedsListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<FeedsFilter> filteredRequest
	) {
		return newsService.getListFiltered(filteredRequest);
	}

	/**
	 * Get Feeds List for current Filters
	 *
	 * @return Feeds List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/news/list", name = "List of latest news feeds")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<FeedsFilter, NewsFeedDTO> getFeedsListFilteredResizable(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<FeedsFilter> filteredRequest
	) {
		return newsService.getListFiltered(filteredRequest);
	}

	/**
	 * Upload file with News feeds data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/news/import", name = "Import News feeds data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ImportResultDTO uploadNewsDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {

		if (!adminUserService.hasRole(RoleType.ADMIN) && !adminUserService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			throw new ForbiddenException("You are not allowed to import news!", ApplicationExceptionCodes.FEEDS_MANAGEMENT_FORBIDDEN);
		}
		ImportResultDTO result = newsService.importNewsFromCSVFile(file.getInputStream());

		return result;
	}


	/**
	 * Download CSV document with News Data.
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/news/csv/export", name = "Get News Feeds Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadNewsAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = newsService.getNewsDownloadData();

		// Build HTTP Response
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("News", null, "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Get Webinars List for current Filters
	 *
	 * @return Webinars List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/webinars", name = "List of latest Webinars")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<FeedsFilter, WebinarDTO> getWebinarsListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<FeedsFilter> filteredRequest
	) {
		return webinarService.getListFiltered(filteredRequest);
	}

	/**
	 * Get Webinars List for current Filters
	 *
	 * @return Webinars List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/webinars/list", name = "List of latest Webinars")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<FeedsFilter, WebinarDTO> getWebinarsListFilteredResizeable(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<FeedsFilter> filteredRequest
	) {
		return webinarService.getListFiltered(filteredRequest);
	}

	/**
	 * Upload file with Webinars data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/webinars/import", name = "Import Webinars data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ImportResultDTO uploadWebinarsDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {

		if (!adminUserService.hasRole(RoleType.ADMIN) && !adminUserService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			throw new ForbiddenException("You are not allowed to import webinars!", ApplicationExceptionCodes.FEEDS_MANAGEMENT_FORBIDDEN);
		}
		ImportResultDTO result = webinarService.importWebinarsFromCSVFile(file.getInputStream());

		return result;
	}

	/**
	 * Download CSV document with Webinars Data.
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/webinars/csv/export", name = "Get Webinars Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadWebinarsAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = webinarService.getWebinarsDownloadData();

		// Build HTTP Response
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("Webinars", null, "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Get Pace Course List for current Filters
	 *
	 * @return Pace Course List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/pace-courses", name = "List of latest Whats New")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<FeedsFilter, PaceCourseDTO> getPaceCoursesListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<FeedsFilter> filteredRequest
	) {
		return paceCourseService.getListFiltered(filteredRequest);
	}

	/**
	 * Get Pace Course List for current Filters
	 *
	 * @return Pace Course List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/pace-courses/list", name = "List of latest Whats New")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<FeedsFilter, PaceCourseDTO> getPaceCoursesListFilteredResizeable(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<FeedsFilter> filteredRequest
	) {
		return paceCourseService.getListFiltered(filteredRequest);
	}
	/**
	 * Upload file with Pace Courses data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/pace-courses/import", name = "Import Pace Courses data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ImportResultDTO uploadPaceCoursesDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {

		if (!adminUserService.hasRole(RoleType.ADMIN) && !adminUserService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			throw new ForbiddenException("You are not allowed to import pace courses!", ApplicationExceptionCodes.FEEDS_MANAGEMENT_FORBIDDEN);
		}
		ImportResultDTO result = paceCourseService.importPaceCoursesFromCSVFile(file.getInputStream());

		return result;
	}

	/**
	 * Download CSV document with Pace Courses Data.
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/pace-courses/csv/export", name = "Get Pace Courses Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadPaceCoursesAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = paceCourseService.getPaceCoursesDownloadData();

		// Build HTTP Response
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("PaceCourses", null, "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

	/**
	 * Get What's New List for current Filters
	 *
	 * @return What's New List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/whats-new", name = "List of latest What's New")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<FeedsFilter, WhatsNewDTO> getWhatsNewListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<FeedsFilter> filteredRequest
	) {
		return whatsNewService.getListFiltered(filteredRequest);
	}

	/**
	 * Get What's New List for current Filters
	 *
	 * @return What's New List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/whats-new/list", name = "List of latest What's New")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<FeedsFilter, WhatsNewDTO> getWhatsNewListFilteredResizeable(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<FeedsFilter> filteredRequest
	) {
		return whatsNewService.getListFiltered(filteredRequest);
	}

	/**
	 * Upload file with What's New data in CSV format
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/whats-new/import", name = "Import Whats New data from CSV template")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ImportResultDTO uploadWhatsNewDataAsCSV(@RequestParam("file") MultipartFile file) throws IOException {

		if (!adminUserService.hasRole(RoleType.ADMIN) && !adminUserService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			throw new ForbiddenException("You are not allowed to import whats new!", ApplicationExceptionCodes.FEEDS_MANAGEMENT_FORBIDDEN);
		}
		ImportResultDTO result = whatsNewService.importWhatsNewFromCSVFile(file.getInputStream());

		return result;
	}

	/**
	 * Download CSV document with What's New Data.
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/whats-new/csv/export", name = "Get Whats New Data in CSV")
	@Produces("application/vnd.ms-excel")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public void downloadWhatsNewAsCSV(HttpServletResponse response) throws IOException {
		// Build Download Template
		ByteArrayInputStream byteArrayInputStream = whatsNewService.getWhatsNewDownloadData();

		// Build HTTP Response
		byte[] fileBytes = byteArrayInputStream.readAllBytes();
		String fileName = applicationProperties.buildExportFileName("WhatsNew", null, "csv");
		response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", fileName));
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(fileBytes, 0, fileBytes.length);
	}

}


