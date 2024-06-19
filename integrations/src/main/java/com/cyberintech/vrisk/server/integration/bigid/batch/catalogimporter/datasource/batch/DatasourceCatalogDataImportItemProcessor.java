package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.DataImportItemProcessorBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.DatasourceCatalogDataImporter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.DatasourceCatalogDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.exeption.CatalogDataItemProcessorValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Slf4j
public class DatasourceCatalogDataImportItemProcessor
	extends DataImportItemProcessorBase<DatasourceCatalogDataImporterParamVO, DatasourceCatalogDataImporterResultVO,
	DatasourceCatalogDataImporter, DatasourceCatalogDataImporterFactory> {

	public DatasourceCatalogDataImportItemProcessor(DatasourceCatalogDataImporterFactory factory) {
		super(factory);
	}

	protected void validateOrFail(DatasourceCatalogDataImporterParamVO data) {
		if (data.getDatasourceVO() == null) {
			throw new CatalogDataItemProcessorValidationException("Can not process null datasource.");
		}

		if (StringUtils.isBlank(data.getDatasourceVO().getName())) {
			throw new CatalogDataItemProcessorValidationException("Can not process datasource with blank name.");
		}

		if (data.getDatasourceVO().getType() == null || StringUtils.isBlank(data.getDatasourceVO().getType())) {
			throw new CatalogDataItemProcessorValidationException("Can not process datasource with blank type.");
		}
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
