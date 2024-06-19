package com.cyberintech.vrisk.server.rest;

import com.cyberintech.vrisk.server.rest.exception.*;
import lombok.AllArgsConstructor;
import lombok.Data;
//import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.text.MessageFormat;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	public GlobalExceptionHandler() {
		log.info("GlobalExceptionHandler instantiated");
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiError> handleBadRequestException(BadRequestException exception) {
		log.warn(exception.getMessage(), exception);
		return buildResponse(HttpStatus.BAD_REQUEST, new ApiError(400, "Bad request"));
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ApiError> handleForbiddenException(ForbiddenException exception) {
		String errorMessage = MessageFormat.format("User [{0}] tried to execute forbidden action. Message: [{1}]",
			exception.getUsername(), exception.getMessage());
		log.warn(errorMessage);
		return buildResponse(HttpStatus.FORBIDDEN, new ApiError(403, exception.getMessage()));
	}

	@ExceptionHandler(ItemNotFoundException.class)
	public ResponseEntity<ApiError> handleItemNotFoundException(ItemNotFoundException exception) {
		log.warn(exception.getMessage(), exception);
		return buildResponse(HttpStatus.NOT_FOUND, new ApiError(404, "Item not found"));
	}

	@ExceptionHandler({ NoHandlerFoundException.class })
	public ResponseEntity<ApiError> handleNoHandlerFound(final NoHandlerFoundException ex) {
		return buildResponse(HttpStatus.NOT_FOUND, new ApiError(404, "Page not found"));
	}

	@ExceptionHandler(ServerException.class)
	public ResponseEntity<ApiError> handleServerException(ServerException exception) {
		log.warn(exception.getMessage(), exception);

		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, new ApiError(500, "Internal server error"));
	}

	@ExceptionHandler(NotAuthenticatedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<ApiError> handleNotAuthenticatedException(NotAuthenticatedException exception) {
		log.warn(exception.getMessage(), exception);
		return buildResponse(HttpStatus.UNAUTHORIZED, new ApiError(401, "Not authorized"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnknownException(Exception exception) {
		ApiError apiError = new ApiError(500,
			"Something went wrong. Our engineers are working to localize the issue. Please try again later or contact support.");
		log.warn(exception.getMessage(), exception);
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, apiError);
	}

	private ResponseEntity<ApiError> buildResponse(HttpStatus status, ApiError apiError) {
		return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(apiError);
	}

	@AllArgsConstructor
	@Data
	public static class ApiError {
		private int code;
		private String message;

		ApiError(ServerException e) {
			this.code = e.getCode();
			this.message = e.getMessage();
		}
	}
}
