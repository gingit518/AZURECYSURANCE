package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.CsvImportItemWriterBase;
import com.cyberintech.vrisk.server.service.storage.StorageDocumentsService;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;

@Slf4j
public class ComplianceRuleDataImportItemWriter extends CsvImportItemWriterBase<ComplianceRuleImporterResultVO> {

	public ComplianceRuleDataImportItemWriter(StorageDocumentsService s3UploadHelper) {
		super(s3UploadHelper);
	}

	@Override
	@SneakyThrows
	protected void writeAsCsvRow(CSVPrinter csvPrinter, ComplianceRuleImporterResultVO item) {
		csvPrinter.printRecord(
			item.getOrganizationId(),
			item.getUserImporterId(),
			item.getComplianceRuleVO().getId(),
			item.getComplianceRuleVO().getName(),
			item.getDataTypeClassificationId(),
			item.getComplianceRuleVO().getName(),
			item.getDataTypeClassificationAction(),
			item.getOwnerId(),
			item.getComplianceRuleVO().getOwner(),
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
			"BI Compliance Rule id",
			"BI Compliance Rule name",
			"Data Type Classification id",
			"Data Type Classification",
			"Compliance Rule Import Action",
			"Owner Id",
			"Owner email",
			"Owner Import Action",
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
		return BatchConstants.COMPLIANCE_RULE_IMPORT_STEP_NAME;
	}
}
