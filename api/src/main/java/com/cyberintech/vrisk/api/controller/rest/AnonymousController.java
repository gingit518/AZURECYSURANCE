package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.user.ForgetPasswordDTO;
import com.cyberintech.vrisk.server.model.dto.user.ResetPasswordDTO;
import com.cyberintech.vrisk.server.service.UserPasswordResetLinksService;
import com.cyberintech.vrisk.server.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;


/**
 * Anonymous Users Controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-25
 */
@RestController
@RequestMapping(
	value = AnonymousController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "Anonymous Users Controller"
)
@Tag(name = "Anonymous Users")
public class AnonymousController {

	static final String CONTROLLER_URI = "/api/anonymous";

	@Autowired
	private UserService userService;

	@Autowired
	private UserPasswordResetLinksService userPasswordResetLinksService;

	/**
	 * Send "Forget password" request
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/reset-password/send-reset-email", name = "Send forget password email", consumes = {MediaType.APPLICATION_JSON})
	@ApiResponses(value = {
		@ApiResponse(responseCode = "404", description = "[ApplicationExceptionCodes.USER_WITH_EMAIL_NOT_EXISTS]: User with this email is not found [{0}]"),
		@ApiResponse(responseCode = "500", description = "[ApplicationExceptionCodes.RESET_PASSWORD_LINK_EMAIL_FAILED]: Failed to send reset password email to [{0}]")
	})
	public boolean sendResetPasswordEmail(@Parameter(description = "User Details to reset password", required = true) @RequestBody ForgetPasswordDTO forgetPasswordDTO) {
		boolean result = false;

		userService.sendResetPasswordEmail(forgetPasswordDTO);

		result = true;

		return result;
	}

	/**
	 * Verify reset password code
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/reset-password/verify-code/{resetCode}", name = "Verify user code for reset password")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "1324", description = "[HTTP-400], [ApplicationExceptionCodes.RESET_PASSWORD_LINK_NOT_EXISTS], Reset password link not found [{0}]"),
		@ApiResponse(responseCode = "1325", description = "[HTTP-400], [ApplicationExceptionCodes.RESET_PASSWORD_LINK_EXPIRED], Reset password link expired [{0}]"),
		@ApiResponse(responseCode = "1326", description = "[HTTP-400], [ApplicationExceptionCodes.RESET_PASSWORD_LINK_ALREADY_APPLIED], Reset password link already applied [{0}]")
	})
	public boolean verifyResetPasswordEmail(@Parameter(description = "Reset user password code", example = "1ecb3d9c-1ebf-4152-8ae9-97d463beee6d", required = true) @PathVariable(name = "resetCode") String resetCode) {
		boolean result = true;

		userPasswordResetLinksService.verifyLinkByCode(resetCode);

		return result;
	}

	/**
	 * Apply new password by reset code
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/reset-password/apply", name = "Apply new password based on reset password code", consumes = {MediaType.APPLICATION_JSON})
	@ApiResponses(value = {
		@ApiResponse(responseCode = "ApplicationExceptionCodes.RESET_PASSWORD_LINK_NOT_EXISTS", description = "[HTTP-404], [ApplicationExceptionCodes.RESET_PASSWORD_LINK_NOT_EXISTS], Reset password link not found [{0}]"),
		@ApiResponse(responseCode = "ApplicationExceptionCodes.RESET_PASSWORD_LINK_EXPIRED", description = "[HTTP-400], [ApplicationExceptionCodes.RESET_PASSWORD_LINK_EXPIRED], Reset password link expired [{0}]"),
		@ApiResponse(responseCode = "ApplicationExceptionCodes.RESET_PASSWORD_LINK_ALREADY_APPLIED", description = "[HTTP-400], [ApplicationExceptionCodes.RESET_PASSWORD_LINK_ALREADY_APPLIED], Reset password link already applied [{0}]"),
		@ApiResponse(responseCode = "ApplicationExceptionCodes.RESET_PASSWORD_TOO_WEAK", description = "[HTTP-400], [ApplicationExceptionCodes.RESET_PASSWORD_TOO_WEAK], Reset password is too weak")
	})
	public boolean changePassword(@Parameter(description = "User Details to reset password", required = true) @RequestBody ResetPasswordDTO resetPasswordDTO) {
		boolean result = true;

		userPasswordResetLinksService.applyPassword(resetPasswordDTO.getCode(), resetPasswordDTO.getPassword());

		return result;
	}

}
