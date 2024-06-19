package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.CsvImportItemWriterBase;
import com.cyberintech.vrisk.server.service.storage.StorageDocumentsService;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;

@Slf4j
public class ApplicationCatalogDataImportItemWriter extends CsvImportItemWriterBase<ApplicationCatalogDataImporterResultVO> {

	public ApplicationCatalogDataImportItemWriter(StorageDocumentsService s3UploadHelper) {
		super(s3UploadHelper);
	}

	@Override
	@SneakyThrows
	protected void writeAsCsvRow(CSVPrinter csvPrinter, ApplicationCatalogDataImporterResultVO item) {
		csvPrinter.printRecord(
			item.getOrganizationId(),
			item.getUserImporterId(),
			item.getApplicationVO().getIdInternal(),
			item.getApplicationVO().getName(),
			item.getSystemId(),
			item.getApplicationVO().getName(),
			item.getSystemAction(),
			item.getOwnerId(),
			item.getOwnerName(),
			item.getOwnerEmail(),
			item.getOwnerAction(),
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
			"System Id",
			"System Name",
			"System Import Action",
			"System Owner id",
			"System Owner name",
			"System Owner email",
			"System Owner Import Action",
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
		return BatchConstants.APPLICATION_IMPORT_STEP_NAME;
	}

}
