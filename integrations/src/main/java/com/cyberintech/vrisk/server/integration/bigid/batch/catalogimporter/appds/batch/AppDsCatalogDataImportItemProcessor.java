package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.AppDsConnectionCatalogDataImporter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.AppDsConnectionCatalogDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.DataImportItemProcessorBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.exeption.CatalogDataItemProcessorValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Slf4j
public class AppDsCatalogDataImportItemProcessor
	extends DataImportItemProcessorBase<AppDsConnectionCatalogDataImporterParamVO, AppDsConnectionCatalogDataImporterResultVO,
	AppDsConnectionCatalogDataImporter, AppDsConnectionCatalogDataImporterFactory> {

	public AppDsCatalogDataImportItemProcessor(AppDsConnectionCatalogDataImporterFactory factory) {
		super(factory);
	}

	@Override
	protected void validateOrFail(AppDsConnectionCatalogDataImporterParamVO data) {
		if (data.getApplicationVO() == null) {
			throw new CatalogDataItemProcessorValidationException("Can not process null application.");
		}

		if (StringUtils.isBlank(data.getApplicationVO().getName())) {
			throw new CatalogDataItemProcessorValidationException("Can not process application with blank name.");
		}

		if (data.getDatasourceVO() == null) {
			throw new CatalogDataItemProcessorValidationException("Can not process null datasource.");
		}

		if (StringUtils.isBlank(data.getDatasourceVO().getName())) {
			throw new CatalogDataItemProcessorValidationException("Can not process datasource with blank name.");
		}
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
