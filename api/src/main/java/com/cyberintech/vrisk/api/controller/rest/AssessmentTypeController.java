package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentTypeEditDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentTypeViewDTO;
import com.cyberintech.vrisk.server.service.AssessmentTypeService;
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
 * Assessment Types management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@RestController
@RequestMapping(
	value = AssessmentTypeController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Assessment Types Management Controller"
)
@Tag(name = "Assessment Types Management")
public class AssessmentTypeController {

	static final String CONTROLLER_URI = "/api/assessment-types";

	@Autowired
	private AssessmentTypeService assessmentTypeService;

	/**
	 * Get Assessment Types List for current Risk Model
	 *
	 * @return Assessment Types List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Assessment Types List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_TYPE_READ)")
	public FilteredResponse<NameFilter, AssessmentTypeViewDTO> getListFiltered(
		@Parameter(name = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, AssessmentTypeViewDTO> result = assessmentTypeService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Assessment Type details
	 *
	 * @return Assessment Type Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Assessment Type details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_TYPE_READ)")
	public AssessmentTypeEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		AssessmentTypeEditDTO itemDTO = assessmentTypeService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Assessment Type
	 *
	 * @return New Assessment Type
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Assessment Type", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_TYPE_CREATE)")
	public AssessmentTypeEditDTO create(
		@Parameter(name = "Assessment Type Details", required = true) @RequestBody AssessmentTypeEditDTO newItemDTO
	) {

		AssessmentTypeEditDTO result = assessmentTypeService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Assessment Type
	 *
	 * @return Updated Assessment Type
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Assessment Type", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_TYPE_UPDATE)")
	public AssessmentTypeEditDTO update(
		@Parameter(name = "Assessment Types update Details", required = true) @RequestBody AssessmentTypeEditDTO itemDTO
	) {

		AssessmentTypeEditDTO result = assessmentTypeService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Assessment Type
	 *
	 * @return ID of removed Assessment Type
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Assessment Type", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_TYPE_DELETE)")
	public Long delete(
		@Parameter(name = "Simple Assessment Type Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = assessmentTypeService.delete(itemDTO.getId());

		return result;
	}

}
