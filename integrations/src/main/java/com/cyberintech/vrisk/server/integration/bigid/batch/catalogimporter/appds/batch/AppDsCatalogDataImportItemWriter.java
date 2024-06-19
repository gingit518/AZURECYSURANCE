package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.CsvImportItemWriterBase;
import com.cyberintech.vrisk.server.service.storage.StorageDocumentsService;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;

@Slf4j
public class AppDsCatalogDataImportItemWriter extends CsvImportItemWriterBase<AppDsConnectionCatalogDataImporterResultVO> {

	public AppDsCatalogDataImportItemWriter(StorageDocumentsService s3UploadHelper) {
		super(s3UploadHelper);
	}

	@Override
	@SneakyThrows
	protected void writeAsCsvRow(CSVPrinter csvPrinter, AppDsConnectionCatalogDataImporterResultVO item) {
		csvPrinter.printRecord(
			item.getOrganizationId(),
			item.getUserImporterId(),
			item.getApplicationVO().getIdInternal(),
			item.getApplicationVO().getName(),
			item.getDatasourceVO().getIdInternal(),
			item.getDatasourceVO().getName(),
			item.getSystemId(),
			item.getTechnologyId(),
			item.getSystemAction(),
			item.getStatus(),
			item.getErrorMessage()
		);
	}

	@Override
	protected String[] getCsvHeaders() {
		return new String[]{
			"Organization Id",
			"User Importer Id",
			"BI Application Id",
			"BI Application Name",
			"BI Datasource Id",
			"BI Datasource Name",
			"System Id",
			"Technology Id",
			"Connection Action",
			"Import Status",
			"Import Error message"
		};
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected String getAssetName() {
		return BatchConstants.APP_DS_IMPORT_STEP_NAME;
	}
}
