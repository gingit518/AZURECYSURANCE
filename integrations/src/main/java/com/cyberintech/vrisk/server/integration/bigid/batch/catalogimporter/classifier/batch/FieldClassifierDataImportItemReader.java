package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.batch;

import static com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants.FIELD_CLASSIFIER_IMPORT_STEP_NAME;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo.FieldClassifierDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.CatalogDataImportItemReaderBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.JobExecutionControlVO;
import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class FieldClassifierDataImportItemReader
	extends CatalogDataImportItemReaderBase<FieldClassifierDataImporterParamVO, JobExecutionControlVO> {
	private final BigIdClientFactory clientFactory;
	private final Long organizationId;
	private final Long userId;
	private final List<FieldClassifierDataImporterParamVO> items = new LinkedList<>();
	private boolean initRequired = true;

	public FieldClassifierDataImportItemReader(JobExecutionControlVO controlVO,
		BigIdClientFactory clientFactory,
		Long organizationId, Long userId) {
		super(controlVO);
		this.clientFactory = clientFactory;
		this.organizationId = organizationId;
		this.userId = userId;
	}

	@Override
	public FieldClassifierDataImporterParamVO readItem() {
		if (initRequired) {
			clientFactory.createFieldClassifierClient(organizationId).getAll().stream()
				.map(cr -> new FieldClassifierDataImporterParamVO(organizationId, userId, cr))
				.forEach(items::add);
			initRequired = false;
		}
		return !items.isEmpty() ? items.remove(0) : null;
	}

	@Override
	protected String getStepName() {
		return FIELD_CLASSIFIER_IMPORT_STEP_NAME;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
