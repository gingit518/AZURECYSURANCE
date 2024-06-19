package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.MDCBatchLoggingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MDCContextAppDsConnectionItemProcessingListener implements ItemProcessListener<AppDsConnectionCatalogDataImporterParamVO, AppDsConnectionCatalogDataImporterResultVO> {

	private final MDCBatchLoggingHelper mdcBatchLoggingHelper;

	@Override
	public void beforeProcess(AppDsConnectionCatalogDataImporterParamVO item) {
		mdcBatchLoggingHelper.setAppDsImportData(item);
	}

	@Override
	public void afterProcess(AppDsConnectionCatalogDataImporterParamVO item, AppDsConnectionCatalogDataImporterResultVO result) {
		mdcBatchLoggingHelper.clearAppDsImportData();
	}

	@Override
	public void onProcessError(AppDsConnectionCatalogDataImporterParamVO item, Exception e) {
		mdcBatchLoggingHelper.clearAppDsImportData();
	}
}
