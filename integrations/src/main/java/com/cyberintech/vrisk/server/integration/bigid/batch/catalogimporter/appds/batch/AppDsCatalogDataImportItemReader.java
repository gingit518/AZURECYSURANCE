package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.CatalogDataImportItemReaderBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.JobExecutionControlVO;
import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClientFactory;
import com.cyberintech.vrisk.server.integration.bigid.client.application.vo.ApplicationVO;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AppDsCatalogDataImportItemReader
	extends CatalogDataImportItemReaderBase<AppDsConnectionCatalogDataImporterParamVO, JobExecutionControlVO> {
	private final BigIdClientFactory clientFactory;
	private final Long organizationId;
	private final Long userId;
	private final List<AppDsConnectionCatalogDataImporterParamVO> items = new LinkedList<>();
	private boolean needToFetch = true;

	public AppDsCatalogDataImportItemReader(JobExecutionControlVO controlVO, BigIdClientFactory clientFactory, Long organizationId, Long userId) {
		super(controlVO);
		this.clientFactory = clientFactory;
		this.organizationId = organizationId;
		this.userId = userId;
	}

	@Override
	public AppDsConnectionCatalogDataImporterParamVO readItem() {
		if (needToFetch) {
			List<ApplicationVO> applications = clientFactory.createApplicationClient(organizationId).getAll();
			Map<String, List<ApplicationVO>> dsAppListIndex = applications.stream()
				.filter(a -> StringUtils.isNotBlank(a.getTargetDataSource()))
				.filter(a -> !StringUtils.equalsIgnoreCase(a.getTargetDataSource(), "none"))
				.collect(Collectors.groupingBy(ApplicationVO::getTargetDataSource));

			dsAppListIndex.keySet().forEach(dsName -> {
				List<ApplicationVO> apps = dsAppListIndex.get(dsName);
				if (CollectionUtils.isNotEmpty(apps)) {
					DatasourceVO datasource = clientFactory.createDatasourceClient(organizationId).getByName(dsName);
					apps.stream().map(app -> new AppDsConnectionCatalogDataImporterParamVO(organizationId, userId, datasource, app))
						.forEach(items::add);
				}
			});
			needToFetch = false;
		}
		return !items.isEmpty() ? items.remove(0) : null;
	}

	@Override
	protected String getStepName() {
		return BatchConstants.APP_DS_IMPORT_STEP_NAME;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
