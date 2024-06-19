package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationEditDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationViewDTO;
import com.cyberintech.vrisk.server.service.DataTypeClassificationService;
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
 * Data Type Classifications management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = DataTypeClassificationController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Data Type Classifications Management Controller"
)
@Tag(name = "Data Type Classifications Management")
public class DataTypeClassificationController {

	static final String CONTROLLER_URI = "/api/data-type-classifications";

	@Autowired
	private DataTypeClassificationService dataTypeClassificationService;

	/**
	 * Get Data Type Classifications List for current Risk Model
	 *
	 * @return Data Type Classifications List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Data Type Classifications List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_CLASS_READ)")
	public FilteredResponse<NameFilter, DataTypeClassificationViewDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, DataTypeClassificationViewDTO> result = dataTypeClassificationService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Data Type Classification details
	 *
	 * @return Data Type Classification Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Data Type Classification details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_CLASS_READ)")
	public DataTypeClassificationEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		DataTypeClassificationEditDTO itemDTO = dataTypeClassificationService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Data Type Classification
	 *
	 * @return New Data Type Classification
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Data Type Classification", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_CLASS_CREATE)")
	public DataTypeClassificationEditDTO create(
		@Parameter(description = "Data Type Classification Details", required = true) @RequestBody DataTypeClassificationEditDTO newItemDTO
	) {

		DataTypeClassificationEditDTO result = dataTypeClassificationService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Data Type Classification
	 *
	 * @return Updated Data Type Classification
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Data Type Classification", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_CLASS_UPDATE)")
	public DataTypeClassificationEditDTO update(
		@Parameter(description = "Data Type Classifications update Details", required = true) @RequestBody DataTypeClassificationEditDTO itemDTO
	) {

		DataTypeClassificationEditDTO result = dataTypeClassificationService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Data Type Classification
	 *
	 * @return ID of removed Data Type Classification
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Data Type Classification", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).DATA_CLASS_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Data Type Classification Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = dataTypeClassificationService.delete(itemDTO.getId());

		return result;
	}

}
