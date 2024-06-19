package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.SystemFilter;
import com.cyberintech.vrisk.server.model.data.TechnologyAssetFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.TechnologyAssetEditDTO;
import com.cyberintech.vrisk.server.model.dto.systems.TechnologyAssetViewDTO;
import com.cyberintech.vrisk.server.service.TechnologyAssetsService;
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

/**
 * TechnologyAssets management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = TechnologyAssetsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "TechnologyAssets Management Controller"
)
@Tag(name = "TechnologyAssets Management")
public class TechnologyAssetsController {

	static final String CONTROLLER_URI = "/api/technology-assets";

	@Autowired
	private TechnologyAssetsService technologyAssetsService;

	/**
	 * Get TechnologyAssets List for current Risk Model
	 *
	 * @return TechnologyAssets List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "TechnologyAssets List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_READ)")
	public FilteredResponse<TechnologyAssetFilter, TechnologyAssetViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<TechnologyAssetFilter> filteredRequest
	) {
		// systemStatus, ipAddress, technologyCategory, technology, manufacturer, assetOwner, endOfLife, location

		FilteredResponse<TechnologyAssetFilter, TechnologyAssetViewDTO> result = technologyAssetsService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get TechnologyAsset details
	 *
	 * @return TechnologyAsset Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get TechnologyAsset details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasOneOfPermissions(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_READ, T(com.cyberintech.vrisk.api.config.APIAction).DATA_EXFILTRATION_READ)")
	public TechnologyAssetEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		TechnologyAssetEditDTO itemDTO = technologyAssetsService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new TechnologyAsset
	 *
	 * @return New TechnologyAsset
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new TechnologyAsset", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_CREATE)")
	public TechnologyAssetEditDTO create(
		@Parameter(description = "TechnologyAsset Details", required = true) @Valid @RequestBody TechnologyAssetEditDTO newItemDTO
	) {

		TechnologyAssetEditDTO result = technologyAssetsService.create(newItemDTO);

		return result;
	}

	/**
	 * Update TechnologyAsset
	 *
	 * @return Updated TechnologyAsset
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing TechnologyAsset", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_UPDATE)")
	public TechnologyAssetEditDTO update(
		@Parameter(description = "TechnologyAssets update Details", required = true) @Valid @RequestBody TechnologyAssetEditDTO itemDTO
	) {

		TechnologyAssetEditDTO result = technologyAssetsService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes TechnologyAsset
	 *
	 * @return ID of removed TechnologyAsset
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing TechnologyAsset", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SYSTEM_DELETE)")
	public Long delete(
		@Parameter(description = "Simple TechnologyAsset Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = technologyAssetsService.delete(itemDTO.getId());

		return result;
	}

}
