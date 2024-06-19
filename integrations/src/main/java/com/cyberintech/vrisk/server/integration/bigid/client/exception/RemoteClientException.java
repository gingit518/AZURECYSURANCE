package com.cyberintech.vrisk.server.integration.bigid.client.exception;

import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;

public class RemoteClientException extends InternalServerErrorException {
	public RemoteClientException(String message) {
		super(message);
	}


}
