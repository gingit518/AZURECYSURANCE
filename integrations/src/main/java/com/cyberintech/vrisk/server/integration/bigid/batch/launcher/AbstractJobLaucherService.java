package com.cyberintech.vrisk.server.integration.bigid.batch.launcher;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.JobType;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.JsonHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractJobLaucherService<P> implements IJobLauncherService<P> {
	protected final JsonHelper jsonHelper;
	private final JobRepository jobRepository;

	@Override
	public JobExecution launchJob(P params) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
		JobParametersInvalidException, JobRestartException {
		log.info("Starting {} job with params: {}.", getJobType(), params);

		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());

		return jobLauncher.run(getJob(), getJobParameters(params));
	}

	protected abstract JobParameters getJobParameters(P params);

	public abstract Job getJob();

	protected abstract JobType getJobType();
}
