package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.context.ApplicationContextThreadLocal;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.OrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.AdminTechnologyCategoryDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryEditDTO;
import com.cyberintech.vrisk.server.service.TechnologyCategoryService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;

/**
 * Technology Category management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = AdminTechnologyCategoryController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Technology Category Management Controller"
)
@Tag(name = "Admin Technology Category Management")
public class AdminTechnologyCategoryController {

	static final String CONTROLLER_URI = "/api/admin/technology-categories";

	@Autowired
	private TechnologyCategoryService technologyCategoryService;

	@Autowired
	private ModelMapper modelMapper;

	/**
	 * Get Technology Category List for current Risk Model
	 *
	 * @return Technology Category List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Technology Category List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_TECHNOLOGY_CATEGORY_READ)")
	public FilteredResponse<OrganizationFilter, AdminTechnologyCategoryDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<OrganizationFilter> filteredRequest
	) {
		FilteredResponse<OrganizationFilter, AdminTechnologyCategoryDTO> result = technologyCategoryService.getAdminListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Technology Category details
	 *
	 * @return Technology Category Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Technology Category details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_TECHNOLOGY_CATEGORY_READ)")
	public AdminTechnologyCategoryDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		AdminTechnologyCategoryDTO itemDTO = technologyCategoryService.getAdminDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new Data Type Classification
	 *
	 * @return New Data Type Classification
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Data Type Classification", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_TECHNOLOGY_CATEGORY_CREATE)")
	public AdminTechnologyCategoryDTO create(
		@Parameter(description = "Data Type Classification Details", required = true) @RequestBody AdminTechnologyCategoryDTO newItemDTO
	) {

		if (newItemDTO.getOrganization() != null) ApplicationContextThreadLocal.getContext().setOrganizationId(newItemDTO.getOrganization().getId());
		TechnologyCategoryEditDTO item = technologyCategoryService.create(newItemDTO);
		AdminTechnologyCategoryDTO result = modelMapper.map(item, AdminTechnologyCategoryDTO.class);

		return result;
	}

	/**
	 * Update Data Type Classification
	 *
	 * @return Updated Data Type Classification
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Data Type Classification", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_TECHNOLOGY_CATEGORY_UPDATE)")
	public AdminTechnologyCategoryDTO update(
		@Parameter(description = "Technology Category update Details", required = true) @RequestBody AdminTechnologyCategoryDTO itemDTO
	) {

		if (itemDTO.getOrganization() != null) ApplicationContextThreadLocal.getContext().setOrganizationId(itemDTO.getOrganization().getId());
		TechnologyCategoryEditDTO item = technologyCategoryService.update(itemDTO);
		AdminTechnologyCategoryDTO result = modelMapper.map(item, AdminTechnologyCategoryDTO.class);

		return result;
	}

	/**
	 * Deletes Data Type Classification
	 *
	 * @return ID of removed Data Type Classification
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Data Type Classification", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_TECHNOLOGY_CATEGORY_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Data Type Classification Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = technologyCategoryService.delete(itemDTO.getId());

		return result;
	}

}
