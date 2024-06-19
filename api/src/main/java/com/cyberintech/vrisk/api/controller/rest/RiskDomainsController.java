package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.risk_domains.RiskDomainViewDTO;
import com.cyberintech.vrisk.server.service.RiskDomainService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Risk Domains controller. Used primarily for Listing
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@RestController
@RequestMapping(
	value = RiskDomainsController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Roles Management Controller"
)
@Tag(name = "Risk Domains Vocabulary Viewer")
public class RiskDomainsController {

	static final String CONTROLLER_URI = "/api/risk-domains";

	@Autowired
	private RiskDomainService riskDomainService;

	/**
	 * Get Risk Domains List
	 *
	 * @return Roles List
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Get all Risk Domains List")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<RiskDomainViewDTO> getList() {
		List<RiskDomainViewDTO> items = riskDomainService.getList();

		return items;
	}

	/**
	 * Get Risk Domain details
	 *
	 * @return Risk Domains Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Risk Domain details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public RiskDomainViewDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		RiskDomainViewDTO itemDTO = riskDomainService.getDetails(itemId);

		return itemDTO;
	}

}
