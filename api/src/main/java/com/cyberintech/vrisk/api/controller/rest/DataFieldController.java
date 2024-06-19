package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataFieldsDTO;
import com.cyberintech.vrisk.server.service.DataFieldsService;
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
 * Data Field management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-16
 */
@RestController
@RequestMapping(
	value = DataFieldController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Data Fields Management Controller"
)
@Tag(name = "Data Fields Management")
public class DataFieldController {

	static final String CONTROLLER_URI = "/api/data-fields";

	@Autowired
	private DataFieldsService dataFieldsService;

	/**
	 * Get Data Fields List for current Risk Model
	 *
	 * @return Data Fields List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Data Fields List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_FIELD_READ)")
	public FilteredResponse<NameFilter, DataFieldsDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, DataFieldsDTO> result = dataFieldsService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Data Field details
	 *
	 * @return Data Field Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Data Field details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_FIELD_READ)")
	public DataFieldsDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		DataFieldsDTO itemDTO = dataFieldsService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Data Field
	 *
	 * @return New Data Field
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Data Field", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_FIELD_CREATE)")
	public DataFieldsDTO create(
		@Parameter(description = "Data Field Details", required = true) @RequestBody DataFieldsDTO newItemDTO
	) {

		DataFieldsDTO result = dataFieldsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Data Field
	 *
	 * @return Updated Data Field
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Data Field", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_FIELD_UPDATE)")
	public DataFieldsDTO update(
		@Parameter(description = "Data Fields update Details", required = true) @RequestBody DataFieldsDTO itemDTO
	) {

		DataFieldsDTO result = dataFieldsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Data Field
	 *
	 * @return ID of removed Data Field
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Data Field", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_FIELD_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Data Field Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = dataFieldsService.delete(itemDTO.getId());

		return result;
	}

}
