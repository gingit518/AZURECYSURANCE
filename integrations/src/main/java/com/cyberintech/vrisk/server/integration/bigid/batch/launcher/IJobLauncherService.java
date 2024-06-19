package com.cyberintech.vrisk.server.integration.bigid.batch.launcher;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

public interface IJobLauncherService<P> {
	JobExecution launchJob(P params) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException;

	boolean supports(Class<?> paramsClazz);
}
