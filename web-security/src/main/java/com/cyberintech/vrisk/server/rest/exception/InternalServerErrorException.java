package com.cyberintech.vrisk.server.rest.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
@AllArgsConstructor
public class InternalServerErrorException extends ServerException {

	private static final long serialVersionUID = -1335175860454511912L;

	public InternalServerErrorException(String message) {
		super(message, 500);
	}

	/**
	 * Full API exception constructor
	 *
	 * @param message
	 * @param code
	 */
	public InternalServerErrorException(String message, int code) {
		super(message, code);
	}

}
