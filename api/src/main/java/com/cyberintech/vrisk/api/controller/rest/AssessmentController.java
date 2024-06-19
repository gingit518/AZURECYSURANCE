package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.AssessmentFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentEditDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentViewDTO;
import com.cyberintech.vrisk.server.service.AssessmentService;
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
 * Assessments management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-05
 */
@RestController
@RequestMapping(
	value = AssessmentController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Assessments Management Controller"
)
@Tag(name = "Assessments Management")
public class AssessmentController {

	static final String CONTROLLER_URI = "/api/assessments";

	@Autowired
	private AssessmentService assessmentService;

	/**
	 * Get Assessments List for current Risk Model
	 *
	 * @return Assessments List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Assessments List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_READ)")
	public FilteredResponse<AssessmentFilter, AssessmentViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<AssessmentFilter> filteredRequest
	) {

		FilteredResponse<AssessmentFilter, AssessmentViewDTO> result = assessmentService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Assessment details
	 *
	 * @return Assessment Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Assessment details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_READ)")
	public AssessmentEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		AssessmentEditDTO itemDTO = assessmentService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Assessment
	 *
	 * @return New Assessment
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Assessment", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_CREATE)")
	public AssessmentEditDTO create(
		@Parameter(description = "Assessment Details", required = true) @RequestBody AssessmentEditDTO newItemDTO
	) {

		AssessmentEditDTO result = assessmentService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Assessment
	 *
	 * @return Updated Assessment
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Assessment", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_UPDATE)")
	public AssessmentEditDTO update(
		@Parameter(description = "Assessments update Details", required = true) @RequestBody AssessmentEditDTO itemDTO
	) {

		AssessmentEditDTO result = assessmentService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Assessment
	 *
	 * @return ID of removed Assessment
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Assessment", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Assessment Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = assessmentService.delete(itemDTO.getId());

		return result;
	}

}
