package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationEditDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationViewDTO;
import com.cyberintech.vrisk.server.service.DataAssetClassificationService;
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
 * Data Asset Classifications management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = DataAssetClassificationController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Data Asset Classifications Management Controller"
)
@Tag(name = "Data Asset Classifications Management")
public class DataAssetClassificationController {

	static final String CONTROLLER_URI = "/api/data-asset-classifications";

	@Autowired
	private DataAssetClassificationService dataAssetClassificationService;

	/**
	 * Get Data Asset Classifications List for current Risk Model
	 *
	 * @return Data Asset Classifications List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Data Asset Classifications List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSET_CLASS_READ)")
	public FilteredResponse<NameFilter, DataAssetClassificationViewDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, DataAssetClassificationViewDTO> result = dataAssetClassificationService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Data Asset Classification details
	 *
	 * @return Data Asset Classification Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Data Asset Classification details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSET_CLASS_READ)")
	public DataAssetClassificationEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		DataAssetClassificationEditDTO itemDTO = dataAssetClassificationService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Data Asset Classification
	 *
	 * @return New Data Asset Classification
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Data Asset Classification", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSET_CLASS_CREATE)")
	public DataAssetClassificationEditDTO create(
		@Parameter(description = "Data Asset Classification Details", required = true) @RequestBody DataAssetClassificationEditDTO newItemDTO
	) {

		DataAssetClassificationEditDTO result = dataAssetClassificationService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Data Asset Classification
	 *
	 * @return Updated Data Asset Classification
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Data Asset Classification", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSET_CLASS_UPDATE)")
	public DataAssetClassificationEditDTO update(
		@Parameter(description = "Data Asset Classifications update Details", required = true) @RequestBody DataAssetClassificationEditDTO itemDTO
	) {

		DataAssetClassificationEditDTO result = dataAssetClassificationService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Data Asset Classification
	 *
	 * @return ID of removed Data Asset Classification
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Data Asset Classification", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSET_CLASS_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Data Asset Classification Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = dataAssetClassificationService.delete(itemDTO.getId());

		return result;
	}

}
