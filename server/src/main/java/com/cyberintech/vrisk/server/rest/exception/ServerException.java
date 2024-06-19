package com.cyberintech.vrisk.server.rest.exception;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * General exception to be treated as SERVER_EXCEPTION.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-10-17
 */
@Setter
@Getter
@NoArgsConstructor
public class ServerException extends RuntimeException {

	/**
	 * Exception code
	 */
	private int code;

	public ServerException(String message) {
		super(message);
	}

	public ServerException(String message, int code) {
		super(message);

		this.code = code;
	}

	@JsonIgnore
	@Override
	public StackTraceElement[] getStackTrace() {
		return super.getStackTrace();
	}

}
