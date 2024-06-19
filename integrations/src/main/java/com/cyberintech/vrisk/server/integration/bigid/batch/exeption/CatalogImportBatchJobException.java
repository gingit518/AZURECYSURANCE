package com.cyberintech.vrisk.server.integration.bigid.batch.exeption;

import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;

public class CatalogImportBatchJobException extends InternalServerErrorException {
	public CatalogImportBatchJobException(String message) {
		super(message);
	}
}
