package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.common.batch;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.JobExecutionControlVO;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.batch.item.ItemReader;

import java.util.Set;

public abstract class CatalogDataImportItemReaderBase<T, C extends JobExecutionControlVO> implements ItemReader<T> {
	protected final C controlVO;

	protected CatalogDataImportItemReaderBase(C controlVO) {
		this.controlVO = controlVO;
	}

	public T read() {
		if (!isEnabled()) {
			getLogger().info("{} step reader is disabled. No items will be processed.", getStepName());
			return null;
		}

		return readItem();
	}

	protected boolean isEnabled() {
		if (controlVO == null) {
			return true;
		}

		if (CollectionUtils.isEmpty(controlVO.getEnabledSteps())) {
			return true;
		}

		Set<String> enabledSteps = controlVO.getEnabledSteps();
		return enabledSteps.contains(getStepName());
	}

	protected abstract T readItem();

	protected abstract String getStepName();

	protected abstract Logger getLogger();
}
