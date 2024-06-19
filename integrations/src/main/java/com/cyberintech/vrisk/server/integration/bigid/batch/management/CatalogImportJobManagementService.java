package com.cyberintech.vrisk.server.integration.bigid.batch.management;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.JobType;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogImportJobParamsVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.JobExecutionControlVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.exeption.CatalogImportBatchJobException;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.JsonHelper;
import com.cyberintech.vrisk.server.integration.bigid.batch.launcher.IJobLauncherService;
import com.cyberintech.vrisk.server.integration.bigid.batch.management.vo.CatalogImportJobVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.management.vo.CatalogImportLaunchJobResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants.CATALOG_IMPORT_JOB_CONTROL;
import static com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants.CATALOG_IMPORT_JOB_NAME;
import static com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants.ORGANIZATION_ID_JOB_PARAM;
import static com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants.USER_ID_JOB_PARAM;

@Service
@Slf4j
@RequiredArgsConstructor
public class CatalogImportJobManagementService {

	private final IJobLauncherService<CatalogImportJobParamsVO> catalogImportJobLauncherService;
	private final JobExplorer jobExplorer;
	private final JsonHelper jsonHelper;

	public CatalogImportLaunchJobResponseVO launch(CatalogImportJobParamsVO params) {
		CatalogImportLaunchJobResponseVO jobResponse = getRunningJobStatus(params.getOrganizationId());
		if (jobResponse.getJob() != null) {
			throw new CatalogImportBatchJobException(String.format("Catalog import job is running for organization with %s id.",
				params.getOrganizationId())
			);
		}
		try {
			return new CatalogImportLaunchJobResponseVO(params,
				new CatalogImportJobVO(catalogImportJobLauncherService.launchJob(params)));
		} catch (Exception ex) {
			log.error("{} Job launch failed for params = {}.", getJobType(), params);
			throw new CatalogImportBatchJobException(String.format("%s job launch failed.", getJobType()));
		}
	}

	public CatalogImportLaunchJobResponseVO getJobStatus(Long jobId) {
		JobExecution jobExecution = jobExplorer.getJobExecution(jobId);
		if (jobExecution == null) {
			return new CatalogImportLaunchJobResponseVO(null, null);
		}
		return new CatalogImportLaunchJobResponseVO(new CatalogImportJobParamsVO(
			jobExecution.getJobParameters().getLong(ORGANIZATION_ID_JOB_PARAM),
			jobExecution.getJobParameters().getLong(USER_ID_JOB_PARAM),
			jsonHelper.fromJson(jobExecution.getJobParameters().getString(CATALOG_IMPORT_JOB_CONTROL), JobExecutionControlVO.class)),
			new CatalogImportJobVO(jobExecution));
	}

	private JobType getJobType() {
		return JobType.IMPORT_CATALOG;
	}

	private CatalogImportLaunchJobResponseVO getRunningJobStatus(Long organizationId) {
		log.debug("Getting job status");
		JobExecution jobExecution = getRunningJobExecution(CATALOG_IMPORT_JOB_NAME, organizationId);
		if (jobExecution == null) {
			return new CatalogImportLaunchJobResponseVO(new CatalogImportJobParamsVO(organizationId, null, null),
				null);
		}
		return new CatalogImportLaunchJobResponseVO(new CatalogImportJobParamsVO(
			organizationId,
			jobExecution.getJobParameters().getLong(USER_ID_JOB_PARAM),
			jsonHelper.fromJson(jobExecution.getJobParameters().getString(CATALOG_IMPORT_JOB_CONTROL),
				JobExecutionControlVO.class)
		), new CatalogImportJobVO(jobExecution));
	}

	private JobExecution getRunningJobExecution(String jobName, Long organizationId) {
		Set<JobExecution> jobExecutions = jobExplorer.findRunningJobExecutions(jobName);

		return jobExecutions.stream()
			.filter(jobExecution -> organizationId.equals(jobExecution.getJobParameters().getLong(ORGANIZATION_ID_JOB_PARAM)))
			.findFirst()
			.orElse(null);
	}

	public Page<CatalogImportLaunchJobResponseVO> getRecentJobInstances(Pageable pageable) {
		List<JobInstance> jobInstances = jobExplorer.findJobInstancesByJobName(CATALOG_IMPORT_JOB_NAME,
			Math.toIntExact(pageable.getOffset()), pageable.getPageSize());
		return PageableExecutionUtils.getPage(jobInstances.stream().map(ji -> getJobStatus(ji.getId())).collect(Collectors.toList()),
			pageable, () -> getJobInstanceCount(CATALOG_IMPORT_JOB_NAME));
	}

	private int getJobInstanceCount(String jobName) {
		try {
			return jobExplorer.getJobInstanceCount(jobName);
		} catch (NoSuchJobException e) {
			return 0;
		}
	}

}
