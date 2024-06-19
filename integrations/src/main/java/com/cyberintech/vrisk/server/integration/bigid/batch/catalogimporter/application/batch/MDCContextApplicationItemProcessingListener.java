package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.MDCBatchLoggingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MDCContextApplicationItemProcessingListener
	implements ItemProcessListener<ApplicationCatalogDataImporterParamVO, ApplicationCatalogDataImporterResultVO> {

	private final MDCBatchLoggingHelper mdcBatchLoggingHelper;

	@Override
	public void beforeProcess(ApplicationCatalogDataImporterParamVO item) {
		mdcBatchLoggingHelper.setApplicationImportData(item);
	}

	@Override
	public void afterProcess(ApplicationCatalogDataImporterParamVO item, ApplicationCatalogDataImporterResultVO result) {
		mdcBatchLoggingHelper.clearApplicationImportData();

	}

	@Override
	public void onProcessError(ApplicationCatalogDataImporterParamVO item, Exception e) {
		mdcBatchLoggingHelper.clearApplicationImportData();
	}
}
