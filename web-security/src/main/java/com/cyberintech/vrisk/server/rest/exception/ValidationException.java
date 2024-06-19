package com.cyberintech.vrisk.server.rest.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

/**
 * Validation exception to be treated as BAD_REQUEST.
 *
 * @author   Andrii Iakovenko
 * @version  0.1.1
 * @since    2022-07-11
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationException extends ServerException {

	private Map<String, String> messages;

	public ValidationException(Map<String, String> messages) {
		super("Validation failed", 400);
		this.messages = messages;
	}

	public Map<String, String> getMessages() {
		return messages;
	}

}
