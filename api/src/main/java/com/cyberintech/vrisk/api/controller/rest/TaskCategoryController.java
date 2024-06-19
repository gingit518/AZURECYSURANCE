package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskCategoryDTO;
import com.cyberintech.vrisk.server.service.TaskCategoryService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;

/**
 * Task Category management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-09
 */
@RestController
@RequestMapping(
	value = TaskCategoryController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Task Category Management Controller"
)
@Tag(name = "Task Category Management")
public class TaskCategoryController {

	static final String CONTROLLER_URI = "/api/task-categories";

	@Autowired
	private TaskCategoryService taskCategoryService;

	/**
	 * Get Task Category List for current Risk Model
	 *
	 * @return Task Category List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Task Category List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, TaskCategoryDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, TaskCategoryDTO> result = taskCategoryService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Task Category details
	 *
	 * @return Task Category Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Task Category details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public TaskCategoryDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		TaskCategoryDTO itemDTO = taskCategoryService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Task Category
	 *
	 * @return New Task Category
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Task Category", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public TaskCategoryDTO create(
		@Parameter(description = "Task Category Details", required = true) @RequestBody TaskCategoryDTO newItemDTO
	) {

		TaskCategoryDTO result = taskCategoryService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Task Category
	 *
	 * @return Updated Task Category
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Task Category", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public TaskCategoryDTO update(
		@Parameter(description = "Task Category update Details", required = true) @RequestBody TaskCategoryDTO itemDTO
	) {

		TaskCategoryDTO result = taskCategoryService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Task Category
	 *
	 * @return ID of removed Task Category
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Task Category", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(
		@Parameter(description = "Simple Task Category Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = taskCategoryService.delete(itemDTO.getId());

		return result;
	}

}
