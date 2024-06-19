package com.cyberintech.vrisk.server.integration.bigid.batch.helper;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo.FieldClassifierDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.MDCConstants;
import org.slf4j.MDC;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Component;

@Component
public class MDCBatchLoggingHelper {
	public void setJobExecutionData(JobExecution jobExecution) {
		MDC.put(MDCConstants.BATCH_JOB_ID, String.valueOf(jobExecution.getJobId()));
		MDC.put(MDCConstants.BATCH_JOB_NAME, jobExecution.getJobInstance().getJobName());
		MDC.put(MDCConstants.BATCH_JOB_ORG_ID, jobExecution.getJobParameters().getString(BatchConstants.ORGANIZATION_ID_JOB_PARAM));
		MDC.put(MDCConstants.BATCH_JOB_USER_ID, jobExecution.getJobParameters().getString(BatchConstants.USER_ID_JOB_PARAM));
	}

	public void clearJobExecutionData() {
		MDC.remove(MDCConstants.BATCH_JOB_ID);
		MDC.remove(MDCConstants.BATCH_JOB_NAME);
		MDC.remove(MDCConstants.BATCH_JOB_ORG_ID);
		MDC.remove(MDCConstants.BATCH_JOB_USER_ID);
	}

	public void setApplicationImportData(ApplicationCatalogDataImporterParamVO item) {
		MDC.put(MDCConstants.BATCH_APP_NAME, item.getApplicationVO().getName());
	}

	public void clearApplicationImportData() {
		MDC.remove(MDCConstants.BATCH_APP_NAME);
	}

	public void setDatasourceImportData(DatasourceCatalogDataImporterParamVO item) {
		MDC.put(MDCConstants.BATCH_DS_NAME, item.getDatasourceVO().getName());
		MDC.put(MDCConstants.BATCH_DS_TYPE, item.getDatasourceVO().getType());
	}

	public void clearDatasourceImportData() {
		MDC.remove(MDCConstants.BATCH_DS_NAME);
		MDC.remove(MDCConstants.BATCH_DS_TYPE);
	}

	public void setAppDsImportData(AppDsConnectionCatalogDataImporterParamVO item) {
		MDC.put(MDCConstants.BATCH_APP_NAME, item.getApplicationVO().getAppAccountName());
		MDC.put(MDCConstants.BATCH_DS_NAME, item.getDatasourceVO().getName());
	}

	public void clearAppDsImportData() {
		MDC.remove(MDCConstants.BATCH_APP_NAME);
		MDC.remove(MDCConstants.BATCH_DS_NAME);
	}

	public void setFieldClassifierImportData(FieldClassifierDataImporterParamVO item) {
		MDC.put(MDCConstants.BATCH_FIELD_CLASSIFIER_NAME, item.getClassifierVO().getName());
	}

	public void clearFieldClassifierImportData() {
		MDC.remove(MDCConstants.BATCH_FIELD_CLASSIFIER_NAME);
	}

	public void setComplianceRuleImportData(ComplianceRuleImporterParamVO item) {
		MDC.put(MDCConstants.BATCH_COMPLIANCE_RULE_NAME, item.getComplianceRuleVO().getName());
	}

	public void clearComplianceRuleImportData() {
		MDC.remove(MDCConstants.BATCH_COMPLIANCE_RULE_NAME);
	}


	public void setSensitivityClassificationImportData(SensitivityClassificationDataImporterParamVO item) {
		MDC.put(MDCConstants.BATCH_SENSITIVITY_CLASSIFICATION_NAME, item.getScConfigVO().getName());
	}

	public void clearSensitivityClassificationImportData() {
		MDC.remove(MDCConstants.BATCH_SENSITIVITY_CLASSIFICATION_NAME);
	}
}
