package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.MDCBatchLoggingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MDCContextSensivityClassificationItemProcessingListener
	implements ItemProcessListener<SensitivityClassificationDataImporterParamVO, SensitivityClassificationDataImporterResultVO> {

	private final MDCBatchLoggingHelper mdcBatchLoggingHelper;


	@Override
	public void beforeProcess(SensitivityClassificationDataImporterParamVO item) {
		mdcBatchLoggingHelper.setSensitivityClassificationImportData(item);
	}

	@Override
	public void afterProcess(SensitivityClassificationDataImporterParamVO item, SensitivityClassificationDataImporterResultVO result) {
		mdcBatchLoggingHelper.clearSensitivityClassificationImportData();
	}

	@Override
	public void onProcessError(SensitivityClassificationDataImporterParamVO item, Exception e) {
		mdcBatchLoggingHelper.clearSensitivityClassificationImportData();
	}
}
