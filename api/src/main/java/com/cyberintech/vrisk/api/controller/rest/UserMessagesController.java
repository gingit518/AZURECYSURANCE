package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.user_messages.UserMessageDTO;
import com.cyberintech.vrisk.server.service.UserMessageService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.MediaType;

/**
 * User messages controller. Basic user messages CRUD.
 *
 * @author   Oleh Dmytrenko <odmytrenko@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-01-11
 */
@RestController
@RequestMapping(
	value = UserMessagesController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "User Messages Management Controller"
)
@Tag(name = "User Messages")
public class UserMessagesController {

	static  final  String CONTROLLER_URI = "/api/user-messages";

	@Autowired
	private UserMessageService userMessageService;

	/**
	 * Get User Message details
	 *
	 * @return User Message Details
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{itemId}", name = "Get User Message details")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public UserMessageDTO getDetails(@PathVariable("itemId") @NotNull @Size(min = 1) Long itemId) {

		UserMessageDTO itemDTO = userMessageService.getDetails(itemId);

		return itemDTO;
	}

	/**
	 * Create new User Message
	 *
	 * @return New User Message
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", name = "Create new User Message", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public UserMessageDTO create(@Parameter(description = "User Message Details", required = true) @RequestBody @Valid UserMessageDTO newItemDTO) {

		UserMessageDTO result = userMessageService.create(newItemDTO);

		return result;
	}

	/**
	 * Receive User Message
	 *
	 * @return Received User Message
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/receive", name = "Receive existing User Message", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public UserMessageDTO receive(@Parameter(description = "User message Details", required = true) @RequestBody UserMessageDTO itemDTO) {

		UserMessageDTO result = userMessageService.receiveMessage(itemDTO.getId());

		return result;
	}

	/**
	 * Read User Message
	 *
	 * @return Read User Message
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/read", name = "Read existing User Message", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
//	@PreAuthorize("@apiSecurity.hasPermission(T(com.cyberintech.vrisk.api.config.APIAction).USER_UPDATE)")
	public UserMessageDTO read(@Parameter(description = "User message Details", required = true) @RequestBody UserMessageDTO itemDTO) {

		UserMessageDTO result = userMessageService.readMessage(itemDTO.getId());

		return result;
	}

	/**
	 * Deletes User Message
	 *
	 * @return ID of removed User Message
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "", name = "Delete existing User Message", consumes = {MediaType.APPLICATION_JSON})
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Long delete(@Parameter(description = "Simple User Message Details", required = true) @RequestBody ItemViewDTO itemDTO) {

		Long result = userMessageService.delete(itemDTO.getId());

		return result;
	}

}
