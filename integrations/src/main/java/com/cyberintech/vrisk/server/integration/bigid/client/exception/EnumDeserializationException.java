package com.cyberintech.vrisk.server.integration.bigid.client.exception;

import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;

public class EnumDeserializationException extends InternalServerErrorException {
	public EnumDeserializationException(String message) {
		super(message);
	}
}
