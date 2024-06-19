package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentLevelRefDTO;
import com.cyberintech.vrisk.server.service.AssessmentLevelService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * Assessment Levels management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-10
 */
@RestController
@RequestMapping(
	value = AssessmentLevelController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Assessment Levels Management Controller"
)
@Tag(name = "Assessment Levels Management")
public class AssessmentLevelController {

	static final String CONTROLLER_URI = "/api/assessment-levels";

	@Autowired
	private AssessmentLevelService assessmentLevelService;

	/**
	 * Get Assessment Levels List for current Risk Model
	 *
	 * @return Assessment Levels List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Assessment Levels List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<NameFilter, AssessmentLevelRefDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<NameFilter> filteredRequest
	) {

		FilteredResponse<NameFilter, AssessmentLevelRefDTO> result = assessmentLevelService.getListFiltered(filteredRequest);

		return result;
	}

}
