package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model_domains.RiskModelDomainEditDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model_domains.RiskModelDomainViewDTO;
import com.cyberintech.vrisk.server.service.RiskModelDomainService;
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
 * Risk Model Вщьфшт management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@RestController
@RequestMapping(
	value = RiskModelDomainController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Risk Model Domains Management Controller"
)
@Tag(name = "Risk Model Domains Management")
public class RiskModelDomainController {

	static final String CONTROLLER_URI = "/api/risk-models/{riskModelId}/domains";

	@Autowired
	private RiskModelDomainService riskModelDomainService;

	/**
	 * Get Risk Model Domains List for current Organization
	 *
	 * @return Risk Models List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Get Risk Model List for current Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_DOMAIN_READ)")
	public List<RiskModelDomainViewDTO> getList(@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId) {

		List<RiskModelDomainViewDTO> result = riskModelDomainService.getListByRiskModel(riskModelId);

		return result;
	}

	/**
	 * Get Risk Model Domain details
	 *
	 * @return Risk Model Domain Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Risk Model Domain details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_DOMAIN_READ)")
	public RiskModelDomainViewDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		RiskModelDomainViewDTO itemDTO = riskModelDomainService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Risk Model Domain
	 *
	 * @return New Risk Model Domain
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Risk Model Domain", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_DOMAIN_CREATE)")
	public RiskModelDomainViewDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Risk Model Domain Details", required = true) @RequestBody RiskModelDomainEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);

		RiskModelDomainViewDTO result = riskModelDomainService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Risk Model Domain
	 *
	 * @return Updated Risk Model Domain
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Risk Model Domain", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_DOMAIN_UPDATE)")
	public RiskModelDomainViewDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "User update Details", required = true) @RequestBody RiskModelDomainEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		RiskModelDomainViewDTO result = riskModelDomainService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Data Domain
	 *
	 * @return ID of removed Data Domain
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Data Domain", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_MODEL_DOMAIN_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Data Domain Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = riskModelDomainService.delete(itemDTO.getId());

		return result;
	}


}
