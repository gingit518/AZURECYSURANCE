package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.*;
import com.cyberintech.vrisk.server.model.dto.external_analytics.ExternalAnalyticsViewDTO;
import com.cyberintech.vrisk.server.model.dto.feeds.*;
import com.cyberintech.vrisk.server.model.dto.user_messages.UserMessageDTO;
import com.cyberintech.vrisk.server.repository.jpa.NewsRepository;
import com.cyberintech.vrisk.server.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

/**
 * Feeds viewer controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-11-30
 */
@RestController
@RequestMapping(
	value = FeedsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Feeds Viewer Controller"
)
@Tag(name = "Feeds Viewer Controller")
public class FeedsController {

	static final String CONTROLLER_URI = "/api/feeds";

	@Autowired
	private CityService cityService;

	@Autowired
	private ExternalAnalyticsService externalAnalyticsService;

	@Autowired
	private NewsService newsService;

	@Autowired
	private PaceCourseService paceCourseService;

	@Autowired
	private WebinarService webinarService;

	@Autowired
	private WhatsNewService whatsNewService;

	@Autowired
	private NewsRepository newsRepository;

	@Autowired
	private TaskService taskService;

	@Autowired
	private UserMessageService userMessageService;


	/**
	 * Get Webinars List for current Filters
	 *
	 * @return Webinars List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/external-analytics", name = "List of latest Webinars")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public List<ExternalAnalyticsViewDTO> getAnalyticsListFiltered() {
		return externalAnalyticsService.getSelfListRandom();
	}


	/**
	 * Get Messages List for current Filters
	 *
	 * @return Messages List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/messages", name = "List of latest user messages")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<FeedsFilter, UserMessageDTO> getMessagesListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<FeedsFilter> filteredRequest
	) {

		if (filteredRequest == null) {
			filteredRequest = new FilteredRequest<>();
		}
		filteredRequest.setSort(BaseSort.of("createdAt", BaseSort.SortOrder.DESC));
		filteredRequest.setSize(12);

		return userMessageService.getListFiltered(filteredRequest);
	}

	/**
	 * Get Feeds List for current Filters
	 *
	 * @return Feeds List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/feeds", name = "List of latest news feeds")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<FeedsFilter, NewsFeedDTO> getFeedsListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<FeedsFilter> filteredRequest
	) {
		filteredRequest = getFeedsFilterFilteredRequest(filteredRequest, 12);

		return newsService.getListFiltered(filteredRequest);
	}

	@NotNull
	private static FilteredRequest<FeedsFilter> getFeedsFilterFilteredRequest(FilteredRequest<FeedsFilter> filteredRequest, int itemsCount) {
		if (filteredRequest == null) {
			filteredRequest = new FilteredRequest<>();
		}
		if (filteredRequest.getFilter() == null) {
			filteredRequest.setFilter(new FeedsFilter());
		}
		filteredRequest.getFilter().setExcludeExpired(true);
		filteredRequest.setSort(BaseSort.of("date", BaseSort.SortOrder.DESC));
		filteredRequest.setSize(itemsCount);
		return filteredRequest;
	}

	/**
	 * Get Tickets List for current Filters
	 *
	 * @return Tickets List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/tickets", name = "List of latest Tickets")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<NameFilter, TicketDTO> getTicketsListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		List<TicketDTO> items = Arrays.asList(
			/*
			TicketDTO.builder().id(1l).code("VRIS-5469").title("Migrate Website").build(),
			TicketDTO.builder().id(2l).code("VRIS-5465").title("Add Advisory Board").build(),
			TicketDTO.builder().id(3l).code("VRIS-5464").title("Scope of Work").build(),
			TicketDTO.builder().id(4l).code("VRIS-5463").title("Guest Access").build(),
			TicketDTO.builder().id(5l).code("VRIS-5459").title("Guest Access  Policy").build()
			*/
		);

		return FilteredResponse.of(filteredRequest).items(items);
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
		filteredRequest = getFeedsFilterFilteredRequest(filteredRequest, 12);

		return webinarService.getListFiltered(filteredRequest);
	}

	/**
	 * Get Whats New List for current Filters
	 *
	 * @return Whats New List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/whats-new", name = "List of latest Whats New")
	@Operation(security = {@SecurityRequirement(name = "bearer-key")})
	public FilteredResponse<FeedsFilter, WhatsNewDTO> getWhatsNewsListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<FeedsFilter> filteredRequest
	) {
		filteredRequest = getFeedsFilterFilteredRequest(filteredRequest, 12);

		return whatsNewService.getListFiltered(filteredRequest);
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
		filteredRequest = getFeedsFilterFilteredRequest(filteredRequest, 12);

		return paceCourseService.getListFiltered(filteredRequest);
	}

}
