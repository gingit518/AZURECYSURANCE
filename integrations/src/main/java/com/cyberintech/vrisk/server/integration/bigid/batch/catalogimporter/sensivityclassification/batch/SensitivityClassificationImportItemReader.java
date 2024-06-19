package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.batch;

import static com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants.SENSITIVITY_CLASSIFICATION_IMPORT_STEP_NAME;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch.CatalogDataImportItemReaderBase;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.JobExecutionControlVO;
import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SensitivityClassificationImportItemReader
	extends CatalogDataImportItemReaderBase<SensitivityClassificationDataImporterParamVO, JobExecutionControlVO> {
	private final BigIdClientFactory clientFactory;
	private final Long organizationId;
	private final Long userId;
	private List<SensitivityClassificationDataImporterParamVO> items;

	private boolean initRequired = true;

	public SensitivityClassificationImportItemReader(JobExecutionControlVO controlVO, BigIdClientFactory clientFactory,
		Long organizationId, Long userId) {
		super(controlVO);
		this.clientFactory = clientFactory;
		this.organizationId = organizationId;
		this.userId = userId;
	}

	@Override
	public SensitivityClassificationDataImporterParamVO readItem() {
		if (initRequired) {
			items = clientFactory.createSensitivityClassificationClient(organizationId).getDefault().stream()
				.map(sc -> new SensitivityClassificationDataImporterParamVO(organizationId, userId, sc))
				.collect(Collectors.toList());
			initRequired = false;
		}

		return !items.isEmpty() ? items.remove(0) : null;
	}

	@Override
	protected String getStepName() {
		return SENSITIVITY_CLASSIFICATION_IMPORT_STEP_NAME;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
