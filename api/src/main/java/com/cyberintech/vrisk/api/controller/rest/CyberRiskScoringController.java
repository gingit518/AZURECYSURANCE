package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.qualitative_question.ReassignScoringToUserDTO;
import com.cyberintech.vrisk.server.service.CyberRiskScoringService;
import com.cyberintech.vrisk.server.service.OrganizationService;
import com.cyberintech.vrisk.server.service.QualitativeQuestionService;
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
 * Cyber Risk Scoring controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-05-14
 */
@RestController
@RequestMapping(
	value = CyberRiskScoringController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Cyber Risk Scoring Controller"
)
@Tag(name = "Cyber Risk Scoring")
public class CyberRiskScoringController {

	static final String CONTROLLER_URI = "/api/risk-model/{riskModelId}/cyber-risk-scoring";

	@Autowired
	private QualitativeQuestionService qualitativeQuestionService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private CyberRiskScoringService cyberRiskScoringService;

	/**
	 * Reassign Cyber Risk Scoring Item to another user
	 *
	 * @return Result Status
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/reassign-to-user", name = "Reassign Cyber Risk Scoring Item to another user", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).RISK_SCORING_UPDATE)")
	public Boolean reassignToUser(
		@Parameter(example = "101") @PathVariable("riskModelId") @NotNull @Size(min = 1) Long riskModelId,
		@Parameter(description = "Reassign Scoring Details", required = true) @RequestBody ReassignScoringToUserDTO reassignScoringToUserDTO
	) {
		Boolean result = cyberRiskScoringService.reassignToUser(riskModelId, reassignScoringToUserDTO);

		return result;
	}

}
