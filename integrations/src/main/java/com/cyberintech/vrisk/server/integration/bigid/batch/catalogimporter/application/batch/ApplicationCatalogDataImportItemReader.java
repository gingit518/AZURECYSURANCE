package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.batch;

import static com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants.APPLICATION_IMPORT_STEP_NAME;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.CatalogDataImportItemReaderBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.JobExecutionControlVO;
import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ApplicationCatalogDataImportItemReader
	extends CatalogDataImportItemReaderBase<ApplicationCatalogDataImporterParamVO, JobExecutionControlVO> {
	private final BigIdClientFactory clientFactory;
	private final Long organizationId;
	private final Long userId;
	private final List<ApplicationCatalogDataImporterParamVO> items = new LinkedList<>();
	private boolean initRequired = true;

	public ApplicationCatalogDataImportItemReader(JobExecutionControlVO controlVO,
		BigIdClientFactory clientFactory,
		Long organizationId, Long userId) {
		super(controlVO);
		this.clientFactory = clientFactory;
		this.organizationId = organizationId;
		this.userId = userId;
	}

	@Override
	public ApplicationCatalogDataImporterParamVO readItem() {
		if (initRequired) {
			clientFactory.createApplicationClient(organizationId).getAll().stream()
				.map(app -> new ApplicationCatalogDataImporterParamVO(organizationId, userId, app))
				.forEach(items::add);
			initRequired = false;
		}
		return !items.isEmpty() ? items.remove(0) : null;
	}

	@Override
	protected String getStepName() {
		return APPLICATION_IMPORT_STEP_NAME;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
