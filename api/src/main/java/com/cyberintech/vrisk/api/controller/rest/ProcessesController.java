package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.ProcessFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessEditDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessViewDTO;
import com.cyberintech.vrisk.server.service.ProcessService;
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
 * Process management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = ProcessesController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Process Management Controller"
)
@Tag(name = "Process Management")
public class ProcessesController {

	static final String CONTROLLER_URI = "/api/processes";

	@Autowired
	private ProcessService processService;

	/**
	 * Get Process List for current Risk Model
	 *
	 * @return Process List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Process List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasOneOfPermissions(T(com.cyberintech.vrisk.api.config.APIAction).PROCESS_READ, T(com.cyberintech.vrisk.api.config.APIAction).BUSINESS_INTERRUPTION_READ, T(com.cyberintech.vrisk.api.config.APIAction).REGULATORY_EXPOSURE_READ)")
	public FilteredResponse<ProcessFilter, ProcessViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<ProcessFilter> filteredRequest
	) {

		FilteredResponse<ProcessFilter, ProcessViewDTO> result = processService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Process details
	 *
	 * @return Process Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Process details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).PROCESS_READ)")
	public ProcessEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		ProcessEditDTO itemDTO = processService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Process
	 *
	 * @return New Process
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Process", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).PROCESS_CREATE)")
	public ProcessEditDTO create(
		@Parameter(description = "Process Details", required = true) @RequestBody ProcessEditDTO newItemDTO
	) {

		ProcessEditDTO result = processService.create(newItemDTO);

		return result;
	}

	/**
	 * Update process revenues
	 *
	 * @return Updated Processes
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/update-process-revenue", name = "Update process revenues", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasOneOfPermissions(T(com.cyberintech.vrisk.api.config.APIAction).PROCESS_UPDATE, T(com.cyberintech.vrisk.api.config.APIAction).BUSINESS_INTERRUPTION_UPDATE)")
	public List<ProcessViewDTO> updateNumberOfRecordsList(
		@Parameter(description = "List of updated process revenue records", required = true) @RequestBody List<ProcessViewDTO> itemsList
	) {

		List<ProcessViewDTO> result = processService.updateProcessRevenue(itemsList);

		return result;
	}

	/**
	 * Update Process
	 *
	 * @return Updated Process
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Process", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).PROCESS_UPDATE)")
	public ProcessEditDTO update(
		@Parameter(description = "Process update Details", required = true) @RequestBody ProcessEditDTO itemDTO
	) {

		ProcessEditDTO result = processService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Process
	 *
	 * @return ID of removed Process
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Process", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).PROCESS_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Process Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = processService.delete(itemDTO.getId());

		return result;
	}

}
