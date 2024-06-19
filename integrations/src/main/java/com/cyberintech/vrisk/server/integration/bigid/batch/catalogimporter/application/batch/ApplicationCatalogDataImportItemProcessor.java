package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.ApplicationCatalogDataImporter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.ApplicationCatalogDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.DataImportItemProcessorBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.exeption.CatalogDataItemProcessorValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Slf4j
public class ApplicationCatalogDataImportItemProcessor
	extends DataImportItemProcessorBase<ApplicationCatalogDataImporterParamVO, ApplicationCatalogDataImporterResultVO,
	ApplicationCatalogDataImporter, ApplicationCatalogDataImporterFactory> {

	public ApplicationCatalogDataImportItemProcessor(ApplicationCatalogDataImporterFactory factory) {
		super(factory);
	}

	@Override
	protected void validateOrFail(ApplicationCatalogDataImporterParamVO data) {
		if (data.getApplicationVO() == null) {
			throw new CatalogDataItemProcessorValidationException("Can not process null application.");
		}

		if (StringUtils.isBlank(data.getApplicationVO().getName())) {
			throw new CatalogDataItemProcessorValidationException("Can not process application with blank name.");
		}
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
