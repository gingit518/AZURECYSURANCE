package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.agreements.UserAgreementEditDTO;
import com.cyberintech.vrisk.server.model.dto.agreements.UserAgreementViewDTO;
import com.cyberintech.vrisk.server.service.UserAgreementService;
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
import java.util.List;

/**
 * Organization Agreements management Controller. Implements basic CRUD.
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-01-20
 */
@RestController
@RequestMapping(
	value = UserAgreementController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON
)
@Tag(name = "User Agreements Management")
public class UserAgreementController {

	static final String CONTROLLER_URI = "/api/user-agreement";

	@Autowired
	private UserAgreementService userAgreementService;

	/**
	 * Get User Agreement Items which aren't answered by current User yet
	 *
	 * @return User Agreement Items
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/unanswered", name = "Unanswered User Agreement Items List for current User and Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<UserAgreementViewDTO> getListOfNotAnsweredUserAgreements() {

		List<UserAgreementViewDTO> result = userAgreementService.getListOfUnansweredUserAgreements();

		return result;
	}

	/**
	 * Save List of User Agreement Items
	 *
	 * @return User Agreement Items
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/unanswered", name = "Save User Agreement Items List for Current User and Organization")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public List<UserAgreementEditDTO> saveListOfAnsweredUserAgreements(
		@Parameter(description = "User Agreement Items save Details", required = true) @RequestBody List<UserAgreementEditDTO> itemsDTOs
	) {

		List<UserAgreementEditDTO> result = userAgreementService.saveListOfAnsweredUserAgreements(itemsDTOs);

		return result;
	}
}
