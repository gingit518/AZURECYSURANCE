package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.CsvImportItemWriterBase;
import com.cyberintech.vrisk.server.service.storage.StorageDocumentsService;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;

@Slf4j
public class DatasourceCatalogDataImportItemWriter extends CsvImportItemWriterBase<DatasourceCatalogDataImporterResultVO> {

	public DatasourceCatalogDataImportItemWriter(StorageDocumentsService s3UploadHelper) {
		super(s3UploadHelper);
	}

	@Override
	@SneakyThrows
	protected void writeAsCsvRow(CSVPrinter csvPrinter, DatasourceCatalogDataImporterResultVO item) {
		csvPrinter.printRecord(
			item.getOrganizationId(),
			item.getUserImporterId(),
			item.getDatasourceVO().getIdInternal(),
			item.getDatasourceVO().getName(),
			item.getTechnologyCategoryId(),
			item.getDatasourceVO().getType(),
			item.getTechnologyCategoryAction(),
			item.getTechnologyId(),
			item.getDatasourceVO().getName(),
			item.getTechnologyAction(),
			item.getTechnologyItOwnerId(),
			item.getTechnologyItOwnerEmail(),
			item.getTechnologyItOwnerAction(),
			item.getTechnologyBusinessOwnerId(),
			item.getTechnologyBusinessOwnerEmail(),
			item.getTechnologyBusinessOwnerAction(),
			item.getInfoSecFocalId(),
			item.getInfoSecFocalEmail(),
			item.getInfoSecFocalAction(),
			item.getParentOrganizationId(),
			item.getParentOrganizationAction(),
			item.getSubOrganizationId(),
			item.getSubOrganizationAction(),
			item.getStatus(),
			item.getErrorMessage()
		);
	}

	@Override
	protected String[] getCsvHeaders() {
		return new String[]{
			"Organization Id",
			"User Importer Id",
			"BI Datasource Id",
			"BI Datasource Name",
			"Technology Category Id",
			"Technology Category Name",
			"Technology Category Import Action",
			"Technology Id",
			"Technology Name",
			"Technology Import Action",
			"Technology IT Owner Id",
			"Technology IT Owner Email",
			"Technology IT Owner Import Action",
			"Technology Business Owner Id",
			"Technology Business Owner Email",
			"Technology Business Owner Action",
			"Technology/System Info Sec Person Id",
			"Technology/System Info Sec Person Email",
			"Technology/System Info Sec Person Import Action",
			"Technology Organization Owner Id. Parent",
			"Technology Organization Owner Import Action",
			"Technology Organization Owner Id. Sub",
			"Technology Organization Owner Import Action",
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
		return BatchConstants.DATASOURCE_IMPORT_STEP_NAME;
	}
}
