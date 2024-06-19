package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentFindingEditDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentFindingViewDTO;
import com.cyberintech.vrisk.server.service.AssessmentFindingService;
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
 * Assessment Findings management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-14
 */
@RestController
@RequestMapping(
	value = AssessmentFindingController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Assessment Findings Management Controller"
)
@Tag(name = "Assessment Findings Management")
public class AssessmentFindingController {

	static final String CONTROLLER_URI = "/api/assessment-findings";

	@Autowired
	private AssessmentFindingService assessmentFindingService;

	/**
	 * Get Assessment Findings List for current Risk Model
	 *
	 * @return Assessment Findings List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Assessment Findings List for current Filters and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_FINDING_READ)")
	public FilteredResponse<NameFilter, AssessmentFindingViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, AssessmentFindingViewDTO> result = assessmentFindingService.getListFiltered(filteredRequest);

		return result;
	}

	/**
	 * Get Assessment Finding details
	 *
	 * @return Assessment Finding Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Assessment Finding details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_FINDING_READ)")
	public AssessmentFindingEditDTO getDetails(
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		AssessmentFindingEditDTO itemDTO = assessmentFindingService.getDetails(itemId);

		return itemDTO;
	}


	/**
	 * Create new Assessment Finding
	 *
	 * @return New Assessment Finding
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Assessment Finding", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_FINDING_CREATE)")
	public AssessmentFindingEditDTO create(
		@Parameter(description = "Assessment Finding Details", required = true) @RequestBody AssessmentFindingEditDTO newItemDTO
	) {

		AssessmentFindingEditDTO result = assessmentFindingService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Assessment Finding
	 *
	 * @return Updated Assessment Finding
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Assessment Finding", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_FINDING_UPDATE)")
	public AssessmentFindingEditDTO update(
		@Parameter(description = "Assessment Findings update Details", required = true) @RequestBody AssessmentFindingEditDTO itemDTO
	) {

		AssessmentFindingEditDTO result = assessmentFindingService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Assessment Finding
	 *
	 * @return ID of removed Assessment Finding
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Assessment Finding", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).ASSESSMENT_FINDING_DELETE)")
	public Long delete(
		@Parameter(description = "Simple Assessment Finding Details", required = true) @RequestBody ItemViewDTO itemDTO
	) {

		Long result = assessmentFindingService.delete(itemDTO.getId());

		return result;
	}

}
