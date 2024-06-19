package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.associate_models.AssociateModelEditDTO;
import com.cyberintech.vrisk.server.model.dto.associate_models.AssociateModelViewDTO;
import com.cyberintech.vrisk.server.service.AssociateModelService;
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
 * Associate Models management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@RestController
@RequestMapping(
	value = AssociateModelsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Associate Models Management Controller"
)
@Tag(name = "Associate Models Management")
public class AssociateModelsController {

	static final String CONTROLLER_URI = "/api/risk-model/{riskModelId}/associate-models";

	@Autowired
	private AssociateModelService associateModelService;

	/**
	 * Get Associate Models List for current Risk Model
	 *
	 * @return Associate Models List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Associate Models List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSOCIATE_MODEL_READ)")
	public FilteredResponse<NameFilter, AssociateModelViewDTO> getListFiltered(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, AssociateModelViewDTO> result = associateModelService.getListFiltered(riskModelId, filteredRequest);

		return result;
	}

	/**
	 * Get Associate Model details
	 *
	 * @return Associate Model Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Associate Model details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSOCIATE_MODEL_READ)")
	public AssociateModelEditDTO getDetails(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		AssociateModelEditDTO itemDTO = associateModelService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Associate Model
	 *
	 * @return New Associate Model
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Associate Model", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSOCIATE_MODEL_CREATE)")
	public AssociateModelEditDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Associate Model Details", required = true) @RequestBody AssociateModelEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);

		AssociateModelEditDTO result = associateModelService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Associate Model
	 *
	 * @return Updated Associate Model
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Associate Model", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSOCIATE_MODEL_UPDATE)")
	public AssociateModelEditDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Associate Models update Details", required = true) @RequestBody AssociateModelEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		AssociateModelEditDTO result = associateModelService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Associate Model
	 *
	 * @return ID of removed Associate Model
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Associate Model", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSOCIATE_MODEL_DELETE)")
	public Long delete(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Simple Associate Model Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = associateModelService.delete(itemDTO.getId());

		return result;
	}

}
