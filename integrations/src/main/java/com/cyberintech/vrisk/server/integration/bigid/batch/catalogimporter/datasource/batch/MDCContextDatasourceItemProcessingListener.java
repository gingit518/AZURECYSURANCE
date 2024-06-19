package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.MDCBatchLoggingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MDCContextDatasourceItemProcessingListener
	implements ItemProcessListener<DatasourceCatalogDataImporterParamVO, DatasourceCatalogDataImporterResultVO> {

	private final MDCBatchLoggingHelper mdcBatchLoggingHelper;


	@Override
	public void beforeProcess(DatasourceCatalogDataImporterParamVO item) {
		mdcBatchLoggingHelper.setDatasourceImportData(item);
	}

	@Override
	public void afterProcess(DatasourceCatalogDataImporterParamVO item, DatasourceCatalogDataImporterResultVO result) {
		mdcBatchLoggingHelper.clearDatasourceImportData();
	}

	@Override
	public void onProcessError(DatasourceCatalogDataImporterParamVO item, Exception e) {
		mdcBatchLoggingHelper.clearDatasourceImportData();
	}
}
