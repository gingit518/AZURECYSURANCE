package com.cyberintech.vrisk.api.config;

import com.cyberintech.vrisk.server.rest.exception.*;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@Autowired
	private ClientMessage clientMessage;

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiError> badRequestException(BadRequestException exception) {
		log.warn(exception.getMessage(), exception);
		return buildResponse(HttpStatus.BAD_REQUEST, new ApiError(exception));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> conflictException(ConflictException exception) {
		log.warn(exception.getMessage(), exception);
		return buildResponse(HttpStatus.CONFLICT, new ApiError(exception));
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ApiError> forbiddenException(ForbiddenException exception) {
		String errorMessage = MessageFormat.format("User [{0}] tried to execute forbidden action. Message: [{1}]",
			exception.getUsername(), exception.getMessage());
		log.warn(errorMessage);
		return buildResponse(HttpStatus.FORBIDDEN, new ApiError(exception));
	}

	@ExceptionHandler(ItemNotFoundException.class)
	public ResponseEntity<ApiError> itemNotFoundException(ItemNotFoundException exception) {
		log.warn(exception.getMessage());
		return buildResponse(HttpStatus.NOT_FOUND, new ApiError(exception));
	}

	// 404
	@ExceptionHandler({ NoHandlerFoundException.class })
	public ResponseEntity<ApiError> notFound(final NoHandlerFoundException exception) {
		return buildResponse(HttpStatus.NOT_FOUND, new ApiError(404, "Page not found"));
	}

	@ExceptionHandler(ServerException.class)
	public ResponseEntity<ApiError> genericServerException(ServerException exception) {
		log.warn(exception.getMessage(), exception);

		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, new ApiError(500, "Internal server error"));
	}

	@ExceptionHandler(NotAuthenticatedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<ApiError> genericServerExceptionException(NotAuthenticatedException exception) {
		log.warn(exception.getMessage(), exception);
		return buildResponse(HttpStatus.UNAUTHORIZED, new ApiError(exception));
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ValidationError> validationException(BindException exception) {
		log.debug(exception.getMessage());
		Map<String, String> messages = new HashMap<>();
		exception.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			messages.put(fieldName, clientMessage.getMessage(errorMessage));
		});

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
			.body(new ValidationError(400, "Validation failed", messages));
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ValidationError> validationException(ValidationException exception) {
		log.debug(exception.getMessage(), exception);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
			.body(new ValidationError(exception, exception.getMessages()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> unknownException(Exception exception) {
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
	private static class ApiError {
		private int code;
		private String message;

		ApiError(ServerException e) {
			this.code = e.getCode();
			this.message = e.getMessage();
		}
	}

	@AllArgsConstructor
	@Data
	private static class ValidationError {

		private int code;
		private String message;

		private Map<String, String> messages;

		ValidationError(ServerException e, Map<String, String> messages) {
			this.code = e.getCode();
			this.message = e.getMessage();
			this.messages = messages;
		}
	}

}
