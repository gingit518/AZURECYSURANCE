package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.category_domain.CategoryDomainDetailsDTO;
import com.cyberintech.vrisk.server.model.dto.category_domain.CategoryDomainEditDTO;
import com.cyberintech.vrisk.server.model.dto.category_domain.CategoryDomainViewDTO;
import com.cyberintech.vrisk.server.service.CategoryDomainService;
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
 * Risk Category Domains management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@RestController
@RequestMapping(
	value = CategoryDomainController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Risk Category Domains Management Controller"
)
@Tag(name = "Risk Category Domains Management")
public class CategoryDomainController {

	static final String CONTROLLER_URI = "/api/risk-models/{riskModelId}/categories";

	@Autowired
	private CategoryDomainService categoryDomainService;

	/**
	 * Get Risk Category Domains List for current Organization
	 *
	 * @return Risk Category Domains List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Risk Category Domains List for current Organization and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CATEGORY_DOMAIN_READ)")
	public List<CategoryDomainDetailsDTO> getList(@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId) {

		List<CategoryDomainDetailsDTO> result = categoryDomainService.getListByRiskModel(riskModelId);

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
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CATEGORY_DOMAIN_READ)")
	public CategoryDomainViewDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId, @Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId) {

		CategoryDomainViewDTO itemDTO = categoryDomainService.getDetails(itemId);

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
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CATEGORY_DOMAIN_CREATE)")
	public CategoryDomainViewDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Risk Model Domain Details", required = true) @RequestBody CategoryDomainEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);

		CategoryDomainViewDTO result = categoryDomainService.create(newItemDTO);

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
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).CATEGORY_DOMAIN_UPDATE)")
	public CategoryDomainViewDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "User update Details", required = true) @RequestBody CategoryDomainEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		CategoryDomainViewDTO result = categoryDomainService.update(itemDTO);

		return result;
	}

}
