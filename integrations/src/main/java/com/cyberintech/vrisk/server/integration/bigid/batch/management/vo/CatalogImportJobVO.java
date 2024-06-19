package com.cyberintech.vrisk.server.integration.bigid.batch.management.vo;

import com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.util.Comparator;
import java.util.Optional;

@Data
@RequiredArgsConstructor
public class CatalogImportJobVO {
	private final JobExecutionVO jobExecution;
	private final StepExecutionVO fieldClassifierStepExecution;
	private final StepExecutionVO complianceRuleStepExecution;
	private final StepExecutionVO applicationStepExecution;
	private final StepExecutionVO datasourceStepExecution;
	private final StepExecutionVO appDsStepExecution;

	public CatalogImportJobVO(JobExecution jobExecution) {
		this.jobExecution = new JobExecutionVO(jobExecution);
		Optional<StepExecution> currentFieldClassifierStepExecution = jobExecution.getStepExecutions().stream()
			.filter(se -> BatchConstants.FIELD_CLASSIFIER_IMPORT_STEP_NAME.equals(se.getStepName()))
			.max(Comparator.comparing(StepExecution::getLastUpdated));
		fieldClassifierStepExecution = currentFieldClassifierStepExecution.map(StepExecutionVO::new).orElse(null);

		Optional<StepExecution> currentComplianceRuleStepExecution = jobExecution.getStepExecutions().stream()
			.filter(se -> BatchConstants.COMPLIANCE_RULE_IMPORT_STEP_NAME.equals(se.getStepName()))
			.max(Comparator.comparing(StepExecution::getLastUpdated));
		complianceRuleStepExecution = currentComplianceRuleStepExecution.map(StepExecutionVO::new).orElse(null);

		Optional<StepExecution> currentApplicationStepExecution = jobExecution.getStepExecutions().stream()
			.filter(se -> BatchConstants.APPLICATION_IMPORT_STEP_NAME.equals(se.getStepName()))
			.max(Comparator.comparing(StepExecution::getLastUpdated));
		applicationStepExecution = currentApplicationStepExecution.map(StepExecutionVO::new).orElse(null);

		Optional<StepExecution> currentDatasourceStepExecution = jobExecution.getStepExecutions().stream()
			.filter(se -> BatchConstants.DATASOURCE_IMPORT_STEP_NAME.equals(se.getStepName()))
			.max(Comparator.comparing(StepExecution::getLastUpdated));
		datasourceStepExecution = currentDatasourceStepExecution.map(StepExecutionVO::new).orElse(null);

		Optional<StepExecution> currentAppDsStepExecution = jobExecution.getStepExecutions().stream()
			.filter(se -> BatchConstants.APP_DS_IMPORT_STEP_NAME.equals(se.getStepName()))
			.max(Comparator.comparing(StepExecution::getLastUpdated));
		appDsStepExecution = currentAppDsStepExecution.map(StepExecutionVO::new).orElse(null);
	}
}
