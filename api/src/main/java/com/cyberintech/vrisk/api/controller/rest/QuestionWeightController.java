package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.qual_metrics.QuestionWeightDTO;
import com.cyberintech.vrisk.server.service.QuestionWeightService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Simple Question Weights controller. Used for Question Weights Listing
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-06
 */
@RestController
@RequestMapping(
	value = QuestionWeightController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Question Weights Management Controller"
)
@Tag(name = "Question Weights Viewer")
public class QuestionWeightController {

	static final String CONTROLLER_URI = "/api/question-weights";

	@Autowired
	private QuestionWeightService questionWeightService;

	/**
	 * Get Question Weights List
	 *
	 * @return Question Weights List
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Get Question Weights List")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<QuestionWeightDTO> getList() {

		List<QuestionWeightDTO> result = questionWeightService.getList();

		return result;
	}

}
