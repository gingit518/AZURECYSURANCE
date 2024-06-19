package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.SecurityRequirementFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityRequirementDTO;
import com.cyberintech.vrisk.server.model.dto.control_subcategory.ControlSubcategoryMappingViewDTO;
import com.cyberintech.vrisk.server.service.SecurityRequirementService;
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
 * Security Requirements management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-01-23
 */
@RestController
@RequestMapping(
	value = SecurityRequirementController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Security Requirements Management Controller"
)
@Tag(name = "Security Requirements Management")
public class SecurityRequirementController {

	static final String CONTROLLER_URI = "/api/security-requirements";

	@Autowired
	private SecurityRequirementService securityRequirementService;

	/**
	 * Get Security Requirements List for current Risk Model
	 *
	 * @return Security Requirements List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Security Requirements List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SECURITY_REQUIREMENT_READ)")
	public FilteredResponse<SecurityRequirementFilter, SecurityRequirementDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<SecurityRequirementFilter> filteredRequest
	) {

		FilteredResponse<SecurityRequirementFilter, SecurityRequirementDTO> result = securityRequirementService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Security Requirement details
	 *
	 * @return Security Requirement Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Security Requirement details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SECURITY_REQUIREMENT_READ)")
	public SecurityRequirementDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		SecurityRequirementDTO result = securityRequirementService.getDetails(itemId);

		return result;
	}

	/**
	 * Get Mapping between Security Requirement and Security Frameworks
	 *
	 * @return Security Requirement Mapping
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/mapping/{itemId}", name = "Get Mapping between Security Requirement and Security Frameworks")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<ControlSubcategoryMappingViewDTO> getMappings(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		List<ControlSubcategoryMappingViewDTO> result = securityRequirementService.getMappings(itemId);

		return result;
	}

	/**
	 * Save Mapping between Security Requirement and Security Frameworks
	 *
	 * @return Security Requirement Mapping
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/mapping/{itemId}", name = "Save Mapping between Security Requirement and Security Frameworks")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<ControlSubcategoryMappingViewDTO> saveMappings(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId,
		@Parameter(description = "Mappings between Security Requirement and Security Frameworks", required = true) @RequestBody List<ControlSubcategoryMappingViewDTO> mappingItems
	) {

		List<ControlSubcategoryMappingViewDTO> result = securityRequirementService.saveMappings(itemId, mappingItems);

		return result;
	}

	/**
	 * Create new Security Requirement
	 *
	 * @return New Security Requirement
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Security Requirement", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SECURITY_REQUIREMENT_CREATE)")
	public SecurityRequirementDTO create(
		@Parameter(description = "Security Requirement Details", required = true) @RequestBody SecurityRequirementDTO newItemDTO
	) {

		SecurityRequirementDTO result = securityRequirementService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Security Requirement
	 *
	 * @return Updated Security Requirement
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Security Requirement", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SECURITY_REQUIREMENT_UPDATE)")
	public SecurityRequirementDTO update(
		@Parameter(description = "Security Requirements update Details", required = true) @RequestBody SecurityRequirementDTO itemDTO
	) {

		SecurityRequirementDTO result = securityRequirementService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Security Requirement
	 *
	 * @return ID of removed Security Requirement
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Security Requirement", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).SECURITY_REQUIREMENT_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Security Requirement Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = securityRequirementService.delete(itemDTO.getId());
		return result;
	}

}
