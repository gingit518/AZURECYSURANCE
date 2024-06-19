package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.QuestionFilter;
import com.cyberintech.vrisk.server.model.dto.answers.MetricResultAnswerEditDTO;
import com.cyberintech.vrisk.server.model.dto.answers.MetricResultAnswerViewDTO;
import com.cyberintech.vrisk.server.service.MetricResultAnswersService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Metric Result Answers management controller. Basic risk model CRUD.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-20
 */
@RestController
@RequestMapping(
	value = MetricResultAnswersController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Metric Result Answers Management Controller"
)
@Tag(name = "Metric Result Answers Management")
public class MetricResultAnswersController {

	static final String CONTROLLER_URI = "/api/risk-models/{riskModelId}/answers";

	@Autowired
	private MetricResultAnswersService metricResultAnswersService;

	/**
	 * Get Metric Result Answers List for current Organization
	 *
	 * @return Metric Result Answers List
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/filter", name = "Metric Result Answers List for current Organization and Risk Model")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).METRIC_RESULT_ANSWER_READ)")
	public FilteredResponse<QuestionFilter, MetricResultAnswerViewDTO> getListFiltered(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Data Filtering Object", required = true) @RequestBody FilteredRequest<QuestionFilter> filteredRequest
	) {

		FilteredResponse<QuestionFilter, MetricResultAnswerViewDTO> result = metricResultAnswersService.getListByRiskModel(riskModelId, filteredRequest);

		return result;
	}

	/**
	 * Get Metric Result Answer details
	 *
	 * @return Metric Result Answer Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get Metric Result Answer details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).METRIC_RESULT_ANSWER_READ)")
	public MetricResultAnswerViewDTO getDetails(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId
	) {

		MetricResultAnswerViewDTO itemDTO = metricResultAnswersService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Save list of Metric Result Answers
	 *
	 * @return New/Updated Metric Result Answer
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/save-items", name = "Save list of Metric Result Answers", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<MetricResultAnswerViewDTO> saveItems(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Metric Result Answers list", required = true) @RequestBody List<MetricResultAnswerEditDTO> itemsList
	) {

		Optional.ofNullable(itemsList).orElse(new ArrayList<>()).stream().forEach(metricResultAnswerEditDTO -> metricResultAnswerEditDTO.setRiskModelId(riskModelId));
		List<MetricResultAnswerViewDTO> result = metricResultAnswersService.saveItems(itemsList);

		return result;
	}

	/**
	 * Create new Metric Result Answer
	 *
	 * @return New Metric Result Answer
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new Metric Result Answer", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public MetricResultAnswerViewDTO create(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Metric Result Answer Details", required = true) @RequestBody MetricResultAnswerEditDTO newItemDTO
	) {

		newItemDTO.setRiskModelId(riskModelId);
		MetricResultAnswerViewDTO result = metricResultAnswersService.create(newItemDTO);

		return result;
	}

	/**
	 * Update Metric Result Answer
	 *
	 * @return Updated Metric Result Answer
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "", name = "Update existing Metric Result Answer", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).METRIC_RESULT_ANSWER_UPDATE)")
	public MetricResultAnswerViewDTO update(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Metric Result Answer Details", required = true) @RequestBody MetricResultAnswerEditDTO itemDTO
	) {

		itemDTO.setRiskModelId(riskModelId);

		MetricResultAnswerViewDTO result = metricResultAnswersService.update(itemDTO);

		return result;
	}

	/**
	 * Deletes Metric Result Answer
	 *
	 * @return ID of removed Metric Result Answer
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing Metric Result Answer", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(@Parameter(description = "Metric Result Answer Details", required = true) @RequestBody MetricResultAnswerEditDTO itemDTO) {

		Long result = metricResultAnswersService.delete(itemDTO.getId());

		return result;
	}

}
