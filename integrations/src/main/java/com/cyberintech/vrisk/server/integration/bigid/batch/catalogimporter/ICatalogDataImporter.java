package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter;


public interface ICatalogDataImporter<R> {

	R process();

	boolean supports(Object data);
}
