package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.FieldClassifierDataImporter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.FieldClassifierDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo.FieldClassifierDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo.FieldClassifierDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.DataImportItemProcessorBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.exeption.CatalogDataItemProcessorValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Slf4j
public class FieldClassifierDataImportProcessor
	extends DataImportItemProcessorBase<FieldClassifierDataImporterParamVO, FieldClassifierDataImporterResultVO,
	FieldClassifierDataImporter, FieldClassifierDataImporterFactory> {

	public FieldClassifierDataImportProcessor(FieldClassifierDataImporterFactory factory) {
		super(factory);
	}

	@Override
	protected void validateOrFail(FieldClassifierDataImporterParamVO data) {
		if (data.getClassifierVO() == null) {
			throw new CatalogDataItemProcessorValidationException("Can not process null field classifier.");
		}

		if (StringUtils.isBlank(data.getClassifierVO().getName())) {
			throw new CatalogDataItemProcessorValidationException("Can not process classifier with blank name.");
		}
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
