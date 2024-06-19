package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.risk_model.RiskModelViewDTO;
import com.cyberintech.vrisk.server.service.RiskModelCalculationsService;
import com.cyberintech.vrisk.server.service.RiskModelService;
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
 * Risk Model management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@RestController
@RequestMapping(
	value = RiskModelController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Risk Models Management Controller"
)
@Tag(name = "Risk Models Management")
public class RiskModelController {

	static final String CONTROLLER_URI = "/api/risk-models";

	@Autowired
	private RiskModelService riskModelService;

	@Autowired
	private RiskModelCalculationsService riskModelCalculationsService;

	/**
	 * Get Risk Model List for current Organization
	 *
	 * @return Risk Models List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Get Risk Model List for current Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<RiskModelViewDTO> getList() {

		List<RiskModelViewDTO> result = riskModelService.getList();

		return result;
	}

	/**
	 * Get default Risk Model details
	 *
	 * @return Risk Model Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/default", name = "Get default Risk Model details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public RiskModelViewDTO getDefault() {

		RiskModelViewDTO itemDTO = null;

		return itemDTO;
	}

	/**
	 * Get Risk Model details
	 *
	 * @return Risk Model Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Risk Model details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public RiskModelViewDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		RiskModelViewDTO itemDTO = riskModelService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Rebuild metrics cache for Risk Model
	 *
	 * @return Risk Model Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/rebuild-metrics-cache/{itemId}", name = "Rebuild metrics cache for Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_RECALCULATE_CACHE)")
	public Boolean rebuildMetricsCache(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {
		return riskModelCalculationsService.rebuildMetricsCache(itemId);
	}

	/**
	 * Create new Risk Model
	 *
	 * @return New Risk Model
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Risk Model", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_CREATE)")
	public RiskModelViewDTO create(@Parameter(description = "Risk Model Details", required = true) @RequestBody RiskModelViewDTO newItemDTO) {

		RiskModelViewDTO result = riskModelService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Risk Model
	 *
	 * @return Updated Risk Model
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Risk Model", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_UPDATE)")
	public RiskModelViewDTO update(@Parameter(description = "Risk Model update Details", required = true) @RequestBody RiskModelViewDTO itemDTO) {

		RiskModelViewDTO result = riskModelService.update(itemDTO);

		return result;
	}

	/**
	 * Delete Risk Model
	 *
	 * @return Delete Risk Model
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Risk Model", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_DELETE)")
	public RiskModelViewDTO delete(@Parameter(description = "Risk Model Details", required = true) @RequestBody RiskModelViewDTO itemDTO) {

		RiskModelViewDTO result = riskModelService.delete(itemDTO);

		return result;
	}

}
