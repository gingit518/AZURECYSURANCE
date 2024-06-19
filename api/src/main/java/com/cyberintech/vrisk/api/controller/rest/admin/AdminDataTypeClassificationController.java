package com.cyberintech.vrisk.api.controller.rest.admin;

import com.cyberintech.vrisk.server.context.ApplicationContextThreadLocal;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.OrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.AdminDataTypeClassificationViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationEditDTO;
import com.cyberintech.vrisk.server.service.DataTypeClassificationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * Data Type Classifications management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = AdminDataTypeClassificationController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Admin Data Type Classifications Management Controller"
)
@Tag(name = "Admin Data Type Classifications Management")
public class AdminDataTypeClassificationController {

	static final String CONTROLLER_URI = "/api/admin/data-type-classifications";

	@Autowired
	private DataTypeClassificationService adminDataTypeClassificationService;

	/**
	 * Get Data Type Classifications List for current Risk Model
	 *
	 * @return Data Type Classifications List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Data Type Classifications List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_DATA_CLASS_READ)")
	public FilteredResponse<OrganizationFilter, AdminDataTypeClassificationViewDTO> getListFiltered(
		@Parameter(description = "Item Filtering", required = true) @RequestBody FilteredRequest<OrganizationFilter> filteredRequest
	) {

		FilteredResponse<OrganizationFilter, AdminDataTypeClassificationViewDTO> result = adminDataTypeClassificationService.getAdminListFiltered(filteredRequest);

		return result;
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
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_DATA_CLASS_CREATE)")
	public DataTypeClassificationEditDTO create(
		@Parameter(description = "Data Type Classification Details", required = true) @RequestBody DataTypeClassificationEditDTO newItemDTO
	) {

		if (newItemDTO.getOrganization() != null) ApplicationContextThreadLocal.getContext().setOrganizationId(newItemDTO.getOrganization().getId());
		DataTypeClassificationEditDTO result = adminDataTypeClassificationService.create(newItemDTO);

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
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_DATA_CLASS_UPDATE)")
	public DataTypeClassificationEditDTO update(
		@Parameter(description = "Data Type Classifications update Details", required = true) @RequestBody DataTypeClassificationEditDTO itemDTO
	) {

		if (itemDTO.getOrganization() != null) ApplicationContextThreadLocal.getContext().setOrganizationId(itemDTO.getOrganization().getId());
		DataTypeClassificationEditDTO result = adminDataTypeClassificationService.update(itemDTO);

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
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ADMIN_DATA_CLASS_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Data Type Classification Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = adminDataTypeClassificationService.delete(itemDTO.getId());

		return result;
	}

}
