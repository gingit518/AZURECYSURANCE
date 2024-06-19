package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.batch;

import static com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants.DATASOURCE_IMPORT_STEP_NAME;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.CatalogDataImportItemReaderBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.JobExecutionControlVO;
import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DatasourceCatalogDataImportItemReader
	extends CatalogDataImportItemReaderBase<DatasourceCatalogDataImporterParamVO, JobExecutionControlVO> {
	private static final int LIMIT = 50;
	private final BigIdClientFactory clientFactory;
	private final Long organizationId;
	private final Long userId;
	private List<DatasourceCatalogDataImporterParamVO> items;
	private int skip = 0;

	public DatasourceCatalogDataImportItemReader(JobExecutionControlVO controlVO,
		BigIdClientFactory clientFactory, Long organizationId, Long userId) {
		super(controlVO);
		this.clientFactory = clientFactory;
		this.organizationId = organizationId;
		this.userId = userId;
	}

	@Override
	public DatasourceCatalogDataImporterParamVO readItem() {
		if (CollectionUtils.isEmpty(items)) {
			items = clientFactory.createDatasourceClient(organizationId).getChunkWithTags(skip, LIMIT).stream()
				.map(ds -> new DatasourceCatalogDataImporterParamVO(organizationId, userId, ds))
				.collect(Collectors.toList());
			skip = skip + LIMIT;
		}
		return !items.isEmpty() ? items.remove(0) : null;
	}

	@Override
	protected String getStepName() {
		return DATASOURCE_IMPORT_STEP_NAME;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
