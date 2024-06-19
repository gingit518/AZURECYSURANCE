package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo.FieldClassifierDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo.FieldClassifierDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.MDCBatchLoggingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MDCContextFieldClassifierItemProcessingListener
	implements ItemProcessListener<FieldClassifierDataImporterParamVO, FieldClassifierDataImporterResultVO> {

	private final MDCBatchLoggingHelper mdcBatchLoggingHelper;

	@Override
	public void beforeProcess(FieldClassifierDataImporterParamVO item) {
		mdcBatchLoggingHelper.setFieldClassifierImportData(item);
	}

	@Override
	public void afterProcess(FieldClassifierDataImporterParamVO item, FieldClassifierDataImporterResultVO result) {
		mdcBatchLoggingHelper.clearFieldClassifierImportData();
	}

	@Override
	public void onProcessError(FieldClassifierDataImporterParamVO item, Exception e) {
		mdcBatchLoggingHelper.clearFieldClassifierImportData();
	}
}
