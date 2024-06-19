package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.CsvImportItemWriterBase;
import com.cyberintech.vrisk.server.service.storage.StorageDocumentsService;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.ImportAction;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;

import java.io.IOException;

@Slf4j
public class SensitivityClassificationDataImportItemWriter extends CsvImportItemWriterBase<SensitivityClassificationDataImporterResultVO> {

	public SensitivityClassificationDataImportItemWriter(StorageDocumentsService s3UploadHelper) {
		super(s3UploadHelper);
	}

	private static void writeDataAssetClassifier(CSVPrinter printer, SensitivityClassificationDataImporterResultVO item, DataAssetClassificationRefDTO syncedDataAssetClassifier, ImportAction importAction) throws IOException {
		printer.printRecord(
			item.getOrganizationId(),
			item.getUserImporterId(),
			item.getScConfigVO().getId(),
			item.getScConfigVO().getName(),
			syncedDataAssetClassifier.getName(),
			syncedDataAssetClassifier.getId(),
			syncedDataAssetClassifier.getName(),
			importAction,
			item.getStatus(),
			item.getErrorMessage()
		);
	}

	@Override
	@SneakyThrows
	protected void writeAsCsvRow(CSVPrinter csvPrinter, SensitivityClassificationDataImporterResultVO item) {
		for (DataAssetClassificationRefDTO syncedDataAssetClassifier : item.getSyncedDataAssetClassifiers()) {
			writeDataAssetClassifier(csvPrinter, item, syncedDataAssetClassifier, ImportAction.SYNCED);
		}
		for (DataAssetClassificationRefDTO createdDataAssetClassifier : item.getCreatedDataAssetClassifiers()) {
			writeDataAssetClassifier(csvPrinter, item, createdDataAssetClassifier, ImportAction.CREATED);
		}
	}

	@Override
	protected String[] getCsvHeaders() {
		return new String[]{
			"Organization Id",
			"User Importer Id",
			"BI Sensitivity Classification Id",
			"BI Sensitivity Classification Name",
			"BI Classification Name",
			"Data Asset Classifier Id",
			"Data Asset Classifier Name",
			"Sensitivity Classification Import Action",
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
		return BatchConstants.SENSITIVITY_CLASSIFICATION_IMPORT_STEP_NAME;
	}
}
