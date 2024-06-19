package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.GDPRFilter;
import com.cyberintech.vrisk.server.model.dto.gdpr.*;
import com.cyberintech.vrisk.server.service.GDPRArticleStatusService;
import com.cyberintech.vrisk.server.service.GDPRItemsService;
import com.cyberintech.vrisk.server.service.GDPRSystemArticleStatusService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * GDPR management controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-25
 */
@RestController
@RequestMapping(
	value = GDPRController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "GDPR Management Controller"
)
@Tag(name = "GDPR Management")
public class GDPRController {

	static final String CONTROLLER_URI = "/api/gdpr";

	@Autowired
	private GDPRItemsService gdprItemsService;

	@Autowired
	private GDPRSystemArticleStatusService gdprSystemArticleStatusService;

	@Autowired
	private GDPRArticleStatusService gdprArticleStatusService;

	/**
	 * Get chapters list for current Filters
	 *
	 * @return items list
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/chapter/filter", name = "GDPR Chapters for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<GDPRFilter, GDPRArticleChapterDTO> getChaptersListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<GDPRFilter> filteredRequest
	) {

		FilteredResponse<GDPRFilter, GDPRArticleChapterDTO> result = gdprItemsService.getGDPRArticleChapterListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get sections for current Filters
	 *
	 * @return items list
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/section/filter", name = "GDPR Chapter Sections for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<GDPRFilter, GDPRArticleChapterSectionDTO> getSectionsListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<GDPRFilter> filteredRequest
	) {

		FilteredResponse<GDPRFilter, GDPRArticleChapterSectionDTO> result = gdprItemsService.getGDPRArticleChapterSectionListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get articles for current Filters
	 *
	 * @return items list
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/articles/filter", name = "GDPR Articles for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<GDPRFilter, GDPRArticleItemDTO> getArticlesListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<GDPRFilter> filteredRequest
	) {

		FilteredResponse<GDPRFilter, GDPRArticleItemDTO> result = gdprItemsService.getGDPRArticleItemsListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get System status for current Filters
	 *
	 * @return items list
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/system/status/filter", name = "GDPR System Status for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).GDPR_SYSTEM_COMPLIANCE_READ)")
	public FilteredResponse<GDPRFilter, GDPRSystemStatusDTO> getGDPRSystemStatusListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<GDPRFilter> filteredRequest
	) {

		FilteredResponse<GDPRFilter, GDPRSystemStatusDTO> result = gdprSystemArticleStatusService.getGDPRSystemStatusListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get System Article status for current Filters
	 *
	 * @return items list
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/system-article/status/filter", name = "GDPR System Article Status for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).GDPR_SYSTEM_ARTICLE_COMPLIANCE_READ)")
	public FilteredResponse<GDPRFilter, GDPRSystemArticleStatusDTO> getGDPRSystemArticleStatusListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<GDPRFilter> filteredRequest
	) {

		FilteredResponse<GDPRFilter, GDPRSystemArticleStatusDTO> result = gdprSystemArticleStatusService.getGDPRSystemArticleStatusListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get System Article status Log for current Filters
	 *
	 * @return items list
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/system-article/status-log/filter", name = "GDPR System Article Status Log for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<GDPRFilter, GDPRSystemArticleStatusLogDTO> getGDPRSystemArticleStatusLogListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<GDPRFilter> filteredRequest
	) {

		FilteredResponse<GDPRFilter, GDPRSystemArticleStatusLogDTO> result = gdprSystemArticleStatusService.getGDPRSystemArticleStatusLogListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Check GDPR System Article Status by GDPR parameters
	 *
	 * @return Article Status Item
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/system-article/status/check", name = "Check GDPR System Article Status by GDPR parameters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public GDPRSystemArticleStatusDTO checkArticleStatus(@Parameter(description = "System Article Status Search Request", required = true) @RequestBody GDPRSystemArticleStatusSearchDTO searchRequest) {
		GDPRSystemArticleStatusDTO result = gdprSystemArticleStatusService.checkArticleStatus(searchRequest);

		return result;
	}

	/**
	 * Get Organization Article status for current Filters
	 *
	 * @return items list
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/organization-article/status/filter", name = "GDPR Organization Article Status for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).GDPR_ORGANIZATION_COMPLIANCE_READ)")
	public FilteredResponse<GDPRFilter, GDPRArticleStatusDTO> getGDPRArticleStatusListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<GDPRFilter> filteredRequest
	) {

		FilteredResponse<GDPRFilter, GDPRArticleStatusDTO> result = gdprArticleStatusService.getGDPRArticleStatusListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Organization Article status Log for current Filters
	 *
	 * @return items list
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/organization-article/status-log/filter", name = "GDPR Organization Article Status Log for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<GDPRFilter, GDPRArticleStatusLogDTO> getGDPRArticleStatusLogListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<GDPRFilter> filteredRequest
	) {

		FilteredResponse<GDPRFilter, GDPRArticleStatusLogDTO> result = gdprArticleStatusService.getGDPRArticleStatusLogListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get GDPR Organization Status for Current Organization
	 *
	 * @return Status Item
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/organization/status", name = "Get GDPR Organization Status for Current Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public GDPROrganizationStatusDTO getGDPROrganizationStatus() {
		GDPROrganizationStatusDTO result = gdprArticleStatusService.getCurrentGDPROrganizationStatus();

		return result;
	}

	/**
	 * Save GDPR Organization Article Status
	 *
	 * @return Article Status Item
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/organization-articles", name = "Save GDPR Organization Article Status")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).GDPR_ORGANIZATION_COMPLIANCE_UPDATE)")
	public GDPRArticleStatusDTO saveArticleStatus(@Parameter(description = "Organization Article Status Request", required = true) @RequestBody GDPRArticleStatusDTO articleStatus) {
		GDPRArticleStatusDTO result = gdprArticleStatusService.saveArticleStatus(articleStatus);

		return result;
	}

	/**
	 * Check GDPR Organization Article Status by GDPR parameters
	 *
	 * @return Article Status Item
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/organization-article/status/check", name = "Check GDPR Organization Article Status by GDPR parameters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public GDPRArticleStatusDTO checkArticleStatus(@Parameter(description = "Organization Article Status Search Request", required = true) @RequestBody GDPRArticleStatusSearchDTO searchRequest) {
		GDPRArticleStatusDTO result = gdprArticleStatusService.checkArticleStatus(searchRequest);

		return result;
	}

}
