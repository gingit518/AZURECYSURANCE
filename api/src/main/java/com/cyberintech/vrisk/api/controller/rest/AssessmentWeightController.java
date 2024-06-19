package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.ByParentFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentWeightViewDTO;
import com.cyberintech.vrisk.server.service.AssessmentWeightService;
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
 * Assessment Weights management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-09
 */
@RestController
@RequestMapping(
	value = AssessmentWeightController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Assessment Weights Management Controller"
)
@Tag(name = "Assessment Weights Management")
public class AssessmentWeightController {

	static final String CONTROLLER_URI = "/api/assessment-weights";

	@Autowired
	private AssessmentWeightService assessmentWeightService;

	/**
	 * Get Assessment Weights List for current Risk Model
	 *
	 * @return Assessment Weights List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Assessment Weights List for current Filters")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public FilteredResponse<ByParentFilter, AssessmentWeightViewDTO> getListFiltered(
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<ByParentFilter> filteredRequest
	) {

		FilteredResponse<ByParentFilter, AssessmentWeightViewDTO> result = assessmentWeightService.getListFiltered(filteredRequest);

		return result;
	}

}
