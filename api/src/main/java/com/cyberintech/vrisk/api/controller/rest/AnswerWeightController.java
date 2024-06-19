package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.qual_metrics.AnswerWeightDTO;
import com.cyberintech.vrisk.server.service.AnswerWeightService;
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
 * Simple Answer Weights controller. Used for Answer Weights Listing
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-06
 */
@RestController
@RequestMapping(
	value = AnswerWeightController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Answer Weights Management Controller"
)
@Tag(name = "Answer Weights Viewer")
public class AnswerWeightController {

	static final String CONTROLLER_URI = "/api/answer-weights";

	@Autowired
	private AnswerWeightService answerWeightService;

	/**
	 * Get Answer Weights List
	 *
	 * @return Answer Weights List
	 */
	@Produces(MediaType.APPLICATION_JSON)
	@RequestMapping(method = RequestMethod.GET, value = "", name = "Get Answer Weights List")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<AnswerWeightDTO> getList() {

		List<AnswerWeightDTO> result = answerWeightService.getList();

		return result;
	}

}
