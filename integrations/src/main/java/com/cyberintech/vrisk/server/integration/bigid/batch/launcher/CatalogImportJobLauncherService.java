package com.cyberintech.vrisk.server.integration.bigid.batch.launcher;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.JobType;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.CatalogImportJobParamsVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class CatalogImportJobLauncherService extends AbstractJobLaucherService<CatalogImportJobParamsVO> {

	private final Job catalogImportJob;

	public CatalogImportJobLauncherService(JsonHelper jsonHelper, JobRepository jobRepository, Job catalogImportJob) {
		super(jsonHelper, jobRepository);
		this.catalogImportJob = catalogImportJob;
	}

	@Override
	protected JobParameters getJobParameters(CatalogImportJobParamsVO params) {
		return new JobParametersBuilder()
			.addString(BatchConstants.JOB_ID_PARAM, String.valueOf(UUID.randomUUID()))
			.addLong(BatchConstants.ORGANIZATION_ID_JOB_PARAM, params.getOrganizationId(), true)
			.addLong(BatchConstants.USER_ID_JOB_PARAM, params.getUserId())
			.addString(BatchConstants.CATALOG_IMPORT_JOB_CONTROL, jsonHelper.toJson(params.getExecutionControl()))
			.toJobParameters();
	}

	@Override
	public Job getJob() {
		return catalogImportJob;
	}

	@Override
	protected JobType getJobType() {
		return JobType.IMPORT_CATALOG;
	}

	@Override
	public boolean supports(Class<?> paramsClazz) {
		return paramsClazz.isAssignableFrom(CatalogImportJobParamsVO.class);
	}
}
