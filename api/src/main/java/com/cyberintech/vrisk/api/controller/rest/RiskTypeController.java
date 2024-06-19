package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.risk_type.RiskTypeEditDTO;
import com.cyberintech.vrisk.server.model.dto.risk_type.RiskTypeViewDTO;
import com.cyberintech.vrisk.server.service.CategoryDomainService;
import com.cyberintech.vrisk.server.service.RiskTypeService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Risk Types management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@RestController
@RequestMapping(
	value = RiskTypeController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Risk Types Management Controller"
)
@Tag(name = "Risk Types Management")
public class RiskTypeController {

	static final String CONTROLLER_URI = "/api/risk-types";

	@Autowired
	private CategoryDomainService categoryDomainService;

	@Autowired
	private RiskTypeService riskTypeService;

	/**
	 * Get Risk Types List for current Category Domain
	 *
	 * @return Risk Types List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/category-domain/{categoryDomainId}", name = "Risk Types List for current Organization and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<RiskTypeViewDTO> getList(@PathVariable("categoryDomainId") @NotNull @Size(min = 1) Long categoryDomainId) {

		List<RiskTypeViewDTO> result = riskTypeService.getListByCategoryDomain(categoryDomainId);

		return result;
	}

	/**
	 * Get Risk Types List for current Risk Model
	 *
	 * @return Risk Types List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/risk-model/{riskModelId}", name = "Risk Types List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, RiskTypeViewDTO> getListFiltered(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Name Filtering", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, RiskTypeViewDTO> result = riskTypeService.getListFiltered(riskModelId, filteredRequest);

		return result;
	}

	/**
	 * Get Risk Type details
	 *
	 * @return Risk Type Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Risk Type details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public RiskTypeViewDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		RiskTypeViewDTO itemDTO = riskTypeService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Risk Type
	 *
	 * @return New Risk Type
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Risk Type", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public RiskTypeViewDTO create(
		@PathVariable("categoryDomainId") @NotNull @Size(min = 1) Long categoryDomainId,
		@Parameter(description = "Risk Type Details", required = true) @RequestBody RiskTypeEditDTO newItemDTO
	) {

		RiskTypeViewDTO result = riskTypeService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Risk Type
	 *
	 * @return Updated Risk Type
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Risk Type", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public RiskTypeViewDTO update(
		@PathVariable("categoryDomainId") @NotNull @Size(min = 1) Long categoryDomainId,
		@Parameter(description = "User update Details", required = true) @RequestBody RiskTypeEditDTO itemDTO
	) {

		RiskTypeViewDTO result = riskTypeService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Risk Type
	 *
	 * @return ID of removed Risk Type
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Risk Type", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(@Parameter(description = "Simple Risk Type Details", required = true) @RequestBody RiskTypeEditDTO itemDTO) {

		Long result = riskTypeService.delete(itemDTO.getId());

		return result;
	}

}
