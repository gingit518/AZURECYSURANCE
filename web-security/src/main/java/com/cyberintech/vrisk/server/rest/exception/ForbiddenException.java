package com.cyberintech.vrisk.server.rest.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * General exception to be treated as FORBIDDEN.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-19
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenException extends ServerException {

	@Getter
	private String username;

	public ForbiddenException(String message) {
		super(message, 403);
	}

	public ForbiddenException(String message, String username) {
		super(message, 403);

		this.username = username;
	}

	/**
	 * Full API exception constructor
	 *
	 * @param message
	 * @param code
	 */
	public ForbiddenException(String message, int code) {
		super(message, code);
	}

}
