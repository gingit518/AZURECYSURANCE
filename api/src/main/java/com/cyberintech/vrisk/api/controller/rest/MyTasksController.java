package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.TasksFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskBudgetViewDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskEditDTO;
import com.cyberintech.vrisk.server.service.TaskService;
import com.cyberintech.vrisk.server.service.UserService;
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

/**
 * Tasks management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-09
 */
@RestController
@RequestMapping(
	value = MyTasksController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "My Tasks Management Controller"
)
@Tag(name = "My Tasks Management")
public class MyTasksController {

	static final String CONTROLLER_URI = "/api/my-tasks";

	@Autowired
	private TaskService taskService;

	@Autowired
	private UserService userService;

	/**
	 * Get Tasks List for current Risk Model
	 *
	 * @return Tasks List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Tasks List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TASK_READ)")
	public FilteredResponse<TasksFilter, TaskBudgetViewDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<TasksFilter> filteredRequest
	) {

		// Reset filter for My Assignments
		filteredRequest.getFilter().setTaskManagerOrAssigneeId(userService.getCurrentUser().getUserId());

		FilteredResponse<TasksFilter, TaskBudgetViewDTO> result = taskService.getTasksListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Task details
	 *
	 * @return Task Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Task details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TASK_READ)")
	public TaskEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		TaskEditDTO itemDTO = taskService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Task
	 *
	 * @return New Task
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Task", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TASK_CREATE)")
	public TaskEditDTO create(
		@Parameter(description = "Task Details", required = true) @RequestBody TaskEditDTO newItemDTO
	) {

		TaskEditDTO result = taskService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Task
	 *
	 * @return Updated Task
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Task", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TASK_UPDATE)")
	public TaskEditDTO update(
		@Parameter(description = "Tasks update Details", required = true) @RequestBody TaskEditDTO itemDTO
	) {

		TaskEditDTO result = taskService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Task
	 *
	 * @return ID of removed Task
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Task", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).TASK_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Task Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = taskService.delete(itemDTO.getId());

		return result;
	}

}
