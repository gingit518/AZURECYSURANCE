package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.ProjectDTO;
import com.cyberintech.vrisk.server.service.ProjectService;
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
 * Projects management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-09
 */
@RestController
@RequestMapping(
	value = ProjectController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Projects Management Controller"
)
@Tag(name = "Projects Management")
public class ProjectController {

	static final String CONTROLLER_URI = "/api/projects";

	@Autowired
	private ProjectService projectService;

	/**
	 * Get Projects List for current Risk Model
	 *
	 * @return Projects List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Projects List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, ProjectDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, ProjectDTO> result = projectService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Project details
	 *
	 * @return Project Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Project details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ProjectDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		ProjectDTO itemDTO = projectService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Project
	 *
	 * @return New Project
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Project", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ProjectDTO create(
		@Parameter(description = "Project Details", required = true) @RequestBody ProjectDTO newItemDTO
	) {

		ProjectDTO result = projectService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Project
	 *
	 * @return Updated Project
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Project", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public ProjectDTO update(
		@Parameter(description = "Project update Details", required = true) @RequestBody ProjectDTO itemDTO
	) {

		ProjectDTO result = projectService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Project
	 *
	 * @return ID of removed Project
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Project", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(
		@Parameter(description = "Project Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = projectService.delete(itemDTO.getId());

		return result;
	}

}
