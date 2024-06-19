package com.cyberintech.vrisk.server.integration.bigid.batch.common;

import com.cyberintech.vrisk.server.integration.bigid.batch.helper.MDCBatchLoggingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MDCContextJobExecutionListener implements JobExecutionListener {

	private final MDCBatchLoggingHelper mdcBatchHelper;

	@Override
	public void beforeJob(JobExecution jobExecution) {
		mdcBatchHelper.setJobExecutionData(jobExecution);
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		mdcBatchHelper.clearJobExecutionData();
	}
}
