package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model_constants.RiskModelConstantEditDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model_constants.RiskModelConstantViewDTO;
import com.cyberintech.vrisk.server.service.RiskModelConstantService;
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
 * Risk Model Constants management controller. Basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-10-28
 */
@RestController
@RequestMapping(
	value = RiskModelConstantController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Risk Model Constants Management Controller"
)
@Tag(name = "Risk Model Constants Controller")
public class RiskModelConstantController {

	static final String CONTROLLER_URI = "/api/risk-models/{riskModelId}/constants";

	@Autowired
	private RiskModelConstantService riskModelConstantService;

	/**
	 * Get Risk Model Constants List for current Risk Model
	 *
	 * @param riskModelId
	 * @param filteredRequest
	 * @return Risk Model Constants List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Get Risk Model Constants List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_CONSTANT_READ)")
	public FilteredResponse<NameFilter, RiskModelConstantViewDTO> getListFiltered(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, RiskModelConstantViewDTO> result = riskModelConstantService.getListFiltered(riskModelId, filteredRequest);

		return result;
	}

	/**
	 * Get Risk Model Constant details
	 *
	 * @param itemId
	 * @return Risk Model Constant Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Risk Model Constant details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_CONSTANT_READ)")
	public RiskModelConstantEditDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		RiskModelConstantEditDTO itemDTO = riskModelConstantService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Risk Model Constant
	 *
	 * @param riskModelId
	 * @param newItemDTO
	 * @return New Risk Model Constant
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Risk Model Constant", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_CONSTANT_CREATE)")
	public RiskModelConstantEditDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Risk Model Constant Details", required = true) @RequestBody RiskModelConstantEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);

		RiskModelConstantEditDTO result = riskModelConstantService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Risk Model Constant
	 *
	 * @param riskModelId
	 * @param itemDTO
	 * @return Updated Risk Model Constant
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Risk Model Constant", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_CONSTANT_UPDATE)")
	public RiskModelConstantEditDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Risk Model Constant update Details", required = true) @RequestBody RiskModelConstantEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		RiskModelConstantEditDTO result = riskModelConstantService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Risk Model Constant
	 *
	 * @param itemDTO
	 * @return ID of removed Risk Model Constant
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Risk Model Constant", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_CONSTANT_DELETE)")
	public Long delete(@Parameter(description = "Simple Risk Model Constant Details", required = true) @RequestBody ItemViewDTO itemDTO) {

		Long result = riskModelConstantService.delete(itemDTO.getId());

		return result;
	}
}
