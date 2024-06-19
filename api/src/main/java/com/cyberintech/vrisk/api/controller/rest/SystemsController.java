package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.SystemFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemDataExfiltrationDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemEditDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemViewDTO;
import com.cyberintech.vrisk.server.service.SystemsService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Systems management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = SystemsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Systems Management Controller"
)
@Tag(name = "Systems Management")
public class SystemsController {

	static final String CONTROLLER_URI = "/api/systems";

	@Autowired
	private SystemsService systemsService;

	/**
	 * Get Systems List for current Risk Model
	 *
	 * @return Systems List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Systems List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_READ)")
	public FilteredResponse<SystemFilter, SystemViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<SystemFilter> filteredRequest
	) {

		FilteredResponse<SystemFilter, SystemViewDTO> result = systemsService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get System details
	 *
	 * @return System Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get System details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasOneOfPermissions(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_READ, T(com.cyberintech.vrisk.api.config.APIAction).DATA_EXFILTRATION_READ)")
	public SystemEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		SystemEditDTO itemDTO = systemsService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new System
	 *
	 * @return New System
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new System", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_CREATE)")
	public SystemEditDTO create(
		@Parameter(description = "System Details", required = true) @Valid @RequestBody SystemEditDTO newItemDTO
	) {

		SystemEditDTO result = systemsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update number of processed records
	 *
	 * @return Updated Systems
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/update-number-of-records", name = "Update number of processed records for systems", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasOneOfPermissions(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_UPDATE, T(com.cyberintech.vrisk.api.config.APIAction).DATA_EXFILTRATION_UPDATE)")
	public List<SystemViewDTO> updateNumberOfRecordsList(
		@Parameter(description = "List of updated system number of records", required = true) @RequestBody List<SystemViewDTO> itemsList
	) {

		List<SystemViewDTO> result = systemsService.updateNumberOfRecordsList(itemsList);

		return result;
	}

	/**
	 * Update System
	 *
	 * @return Updated System
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing System", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_UPDATE)")
	public SystemEditDTO update(
		@Parameter(description = "Systems update Details", required = true) @Valid @RequestBody SystemEditDTO itemDTO
	) {

		SystemEditDTO result = systemsService.update(itemDTO);

		return result;
	}

	/**
	 * Update System Data Exfiltration
	 *
	 * @return Updated System
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/data-exfiltration", name = "Update Data Exfiltration info for the System", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_UPDATE)")
	public SystemDataExfiltrationDTO updateDataExfiltration(
		@Parameter(description = "Systems update Details", required = true) @Valid @RequestBody SystemDataExfiltrationDTO itemDTO
	) {

		SystemDataExfiltrationDTO result = systemsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes System
	 *
	 * @return ID of removed System
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing System", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_DELETE)")
	public Long delete(
		@Parameter(description = "Simple System Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = systemsService.delete(itemDTO.getId());

		return result;
	}

}
