package com.cyberintech.vrisk.server.integration.bigid.batch.management.vo;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;

import java.util.Date;

@Data
@RequiredArgsConstructor
public class JobExecutionVO {
	private final Long jobExecutionId;
	private final BatchStatus status;
	private final Date startTime;
	private final Date lastUpdated;
	private final Date endTime;

	public JobExecutionVO(JobExecution jobExecution) {
		jobExecutionId = jobExecution.getId();
		status = jobExecution.getStatus();
		startTime = jobExecution.getStartTime();
		lastUpdated = jobExecution.getLastUpdated();
		endTime = jobExecution.getEndTime();
	}
}
