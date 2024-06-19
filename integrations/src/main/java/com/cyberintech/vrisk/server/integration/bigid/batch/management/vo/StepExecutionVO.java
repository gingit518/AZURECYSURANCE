package com.cyberintech.vrisk.server.integration.bigid.batch.management.vo;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;

import java.util.Date;

@Data
@RequiredArgsConstructor
public class StepExecutionVO {
	private final String stepName;
	private final BatchStatus status;
	private final int readCount;
	private final int writeCount;
	private final Date startTime;
	private final Date lastUpdated;
	private final Date endTime;

	public StepExecutionVO(StepExecution stepExecution) {
		stepName = stepExecution.getStepName();
		status = stepExecution.getStatus();
		readCount = stepExecution.getReadCount();
		writeCount = stepExecution.getWriteCount();
		startTime = stepExecution.getStartTime();
		lastUpdated = stepExecution.getLastUpdated();
		endTime = stepExecution.getEndTime();
	}
}
