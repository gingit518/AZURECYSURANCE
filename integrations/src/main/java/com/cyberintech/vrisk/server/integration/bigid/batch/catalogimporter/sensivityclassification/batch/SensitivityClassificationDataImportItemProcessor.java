package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.DataImportItemProcessorBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.SensitivityClassificationDataImporter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.SensitivityClassificationDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.exeption.CatalogDataItemProcessorValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Slf4j
public class SensitivityClassificationDataImportItemProcessor
	extends DataImportItemProcessorBase<SensitivityClassificationDataImporterParamVO, SensitivityClassificationDataImporterResultVO,
	SensitivityClassificationDataImporter, SensitivityClassificationDataImporterFactory> {

	public SensitivityClassificationDataImportItemProcessor(SensitivityClassificationDataImporterFactory factory) {
		super(factory);
	}

	@Override
	protected void validateOrFail(SensitivityClassificationDataImporterParamVO data) {
		if (data.getScConfigVO() == null) {
			throw new CatalogDataItemProcessorValidationException("Can not process null sensitivity classification.");
		}

		if (StringUtils.isBlank(data.getScConfigVO().getName())) {
			throw new CatalogDataItemProcessorValidationException("Can not process sensitivity classification with blank name.");
		}

		if (CollectionUtils.isEmpty(data.getScConfigVO().getClassifications())) {
			throw new CatalogDataItemProcessorValidationException("Can not process empty sensitivity classification list.");
		}
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
