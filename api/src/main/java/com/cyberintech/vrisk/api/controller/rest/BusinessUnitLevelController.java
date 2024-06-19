package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnitLevels;
import com.cyberintech.vrisk.server.repository.jpa.BusinessUnitRepository;
import com.cyberintech.vrisk.server.service.BusinessUnitLevelService;
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
import javax.ws.rs.core.MediaType;
import java.text.MessageFormat;
import java.util.List;

@RestController
@RequestMapping(
	value = BusinessUnitLevelController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Business Unit Levels Management Controller"
)
@Tag(name = "Business Unit Levels Management")
public class BusinessUnitLevelController {

	static final String CONTROLLER_URI = "/api/business-unit-levels";

	@Autowired
	private BusinessUnitLevelService businessUnitLevelService;

	@Autowired
	private BusinessUnitRepository businessUnitRepository;

	/**
	 * Rebuild Business Unit Levels for all business units
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/rebuild", name = "Rebuild Business Unit Levels")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public String rebuildBusinessUnitLevelsForAllBusinessUnits() {

		List<BusinessUnitLevels> builtItems = businessUnitLevelService.rebuildBusinessUnitLevels();

		String result = MessageFormat.format("{0} Business Unit Levels built for existed Business Units.", builtItems.size());

		return result;
	}

	/**
	 * Rebuild Business Unit Levels for all business units
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/build/{itemId}", name = "Rebuild Business Unit Levels")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public String buildBusinessUnitLevelsForBusinessUnit(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		List<BusinessUnitLevels> builtItems = businessUnitLevelService.buildBusinessUnitLevelsBottom(businessUnitRepository.findById(itemId).get());

		String result = MessageFormat.format("{0} Business Unit Levels built for existed Business Units.", builtItems.size());

		return result;
	}
}
