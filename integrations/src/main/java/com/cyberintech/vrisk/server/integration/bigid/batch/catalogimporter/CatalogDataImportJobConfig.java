package com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter;

import static com.cyberintech.vrisk.server.integration.bigid.batch.common.BatchConstants.*;

import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.AppDsConnectionCatalogDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.batch.AppDsCatalogDataImportItemProcessor;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.batch.AppDsCatalogDataImportItemReader;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.batch.AppDsCatalogDataImportItemWriter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.batch.MDCContextAppDsConnectionItemProcessingListener;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.appds.vo.AppDsConnectionCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.ApplicationCatalogDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.batch.ApplicationCatalogDataImportItemProcessor;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.batch.ApplicationCatalogDataImportItemReader;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.batch.ApplicationCatalogDataImportItemWriter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.batch.MDCContextApplicationItemProcessingListener;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.application.vo.ApplicationCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.FieldClassifierDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.batch.FieldClassifierDataImportItemReader;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.batch.FieldClassifierDataImportItemWriter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.batch.FieldClassifierDataImportProcessor;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.batch.MDCContextFieldClassifierItemProcessingListener;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo.FieldClassifierDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.classifier.vo.FieldClassifierDataImporterResultVO;
import com.cyberintech.vrisk.server.service.storage.StorageDocumentsService;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.ComplianceRuleDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.batch.ComplianceRuleCatalogDataImportItemReader;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.batch.ComplianceRuleDataImportItemProcessor;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.batch.ComplianceRuleDataImportItemWriter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.batch.MDCContextComplianceRuleItemProcessingListener;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.compliancerule.vo.ComplianceRuleImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.DatasourceCatalogDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.batch.DatasourceCatalogDataImportItemProcessor;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.batch.DatasourceCatalogDataImportItemReader;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.batch.DatasourceCatalogDataImportItemWriter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.batch.MDCContextDatasourceItemProcessingListener;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.datasource.vo.DatasourceCatalogDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.SensitivityClassificationDataImporterFactory;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.batch.MDCContextSensivityClassificationItemProcessingListener;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.batch.SensitivityClassificationDataImportItemProcessor;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.batch.SensitivityClassificationDataImportItemWriter;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.batch.SensitivityClassificationImportItemReader;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterParamVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.catalogimporter.sensivityclassification.vo.SensitivityClassificationDataImporterResultVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.MDCContextJobExecutionListener;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.SecurityContextAwareChunkListener;
import com.cyberintech.vrisk.server.integration.bigid.batch.common.vo.JobExecutionControlVO;
import com.cyberintech.vrisk.server.integration.bigid.batch.exeption.CatalogDataItemProcessorValidationException;
import com.cyberintech.vrisk.server.integration.bigid.batch.helper.JsonHelper;
import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClientFactory;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.DatasourceClient;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CatalogDataImportJobConfig {
	private final JobBuilderFactory jobBuilders;
	private final StepBuilderFactory stepBuilders;
	private final JsonHelper jsonHelper;
	@Value("${intgration.bigid.catalog-import.application.chunk-size:50}")
	private int applicationChunkSize;
	@Value("${intgration.bigid.catalog-import.datasource.chunk-size:50}")
	private int datasourceChunkSize;
	@Value("${intgration.bigid.catalog-import.app-ds.chunk-size:50}")
	private int appDsChunkSize;
	@Value("${intgration.bigid.catalog-import.field-classifier.chunk-size:50}")
	private int fieldClassifierChunkSize;
	@Value("${intgration.bigid.catalog-import.compliance-rule.chunk-size:50}")
	private int complianceRuleChunkSize;

	@Bean
	Job catalogDataImportJob(Step sensitivityClassificationImportStep,
							 Step fieldClassifierImportStep,
							 Step complianceRuleImportStep,
							 Step applicationImportStep,
							 Step datasourceImportStep,
							 Step appDsImportStep,
							 MDCContextJobExecutionListener mdcContextJobListener) {
		return jobBuilders.get(CATALOG_IMPORT_JOB_NAME)
			.preventRestart()
			.start(sensitivityClassificationImportStep)
			.next(fieldClassifierImportStep)
			.next(complianceRuleImportStep)
			.next(applicationImportStep)
			.next(datasourceImportStep)
			.next(appDsImportStep)
			.listener(mdcContextJobListener)
			.build();
	}

	@Bean
	Step applicationImportStep(ApplicationCatalogDataImportItemReader applicationContextItemReader,
							   ApplicationCatalogDataImportItemProcessor applicationCatalogDataImportItemProcessor,
							   ApplicationCatalogDataImportItemWriter applicationCatalogDataImportItemWriter,
							   ChunkListener securityContextStepExecutionListener,
							   MDCContextApplicationItemProcessingListener mdcContextApplicationItemProcessingListener) {
		return stepBuilders.get(APPLICATION_IMPORT_STEP_NAME)
			.<ApplicationCatalogDataImporterParamVO, ApplicationCatalogDataImporterResultVO>chunk(applicationChunkSize)
			.reader(applicationContextItemReader)
			.processor(applicationCatalogDataImportItemProcessor)
			.writer(applicationCatalogDataImportItemWriter)
			.faultTolerant()
			.skip(CatalogDataItemProcessorValidationException.class)
			.listener(securityContextStepExecutionListener)
			.listener(mdcContextApplicationItemProcessingListener)
			.build();
	}

	@Bean
	@StepScope
	ApplicationCatalogDataImportItemReader applicationImportContextItemReader(
		BigIdClientFactory bigIdClientFactory,
		@Value("#{jobParameters['" + ORGANIZATION_ID_JOB_PARAM + "']}") Long organizationId,
		@Value("#{jobParameters['" + USER_ID_JOB_PARAM + "']}") Long userId,
		@Value("#{jobParameters['" + CATALOG_IMPORT_JOB_CONTROL + "']}") String jobControl
	) {
		return new ApplicationCatalogDataImportItemReader(
			jsonHelper.fromJson(jobControl, JobExecutionControlVO.class),
			bigIdClientFactory, organizationId, userId);
	}

	@Bean
	@StepScope
	ApplicationCatalogDataImportItemProcessor applicationCatalogDataImportItemProcessor(
		ApplicationCatalogDataImporterFactory applicationCatalogDataImporterFactory
	) {
		return new ApplicationCatalogDataImportItemProcessor(applicationCatalogDataImporterFactory);
	}

	@Bean
	@StepScope
	ApplicationCatalogDataImportItemWriter applicationCatalogDataImportItemWriter(StorageDocumentsService s3UploadHelper) {
		return new ApplicationCatalogDataImportItemWriter(s3UploadHelper);
	}

	@Bean
	Step datasourceImportStep(DatasourceCatalogDataImportItemReader datasourceCatalogDataImportItemReader,
							  DatasourceCatalogDataImportItemProcessor datasourceCatalogDataImportItemProcessor,
							  DatasourceCatalogDataImportItemWriter datasourceCatalogDataImportItemWriter,
							  ChunkListener securityContextStepExecutionListener,
							  MDCContextDatasourceItemProcessingListener mdcContextDatasourceItemProcessingListener) {
		return stepBuilders.get(DATASOURCE_IMPORT_STEP_NAME)
			.<DatasourceCatalogDataImporterParamVO, DatasourceCatalogDataImporterResultVO>chunk(datasourceChunkSize)
			.reader(datasourceCatalogDataImportItemReader)
			.processor(datasourceCatalogDataImportItemProcessor)
			.writer(datasourceCatalogDataImportItemWriter)
			.faultTolerant()
			.skip(CatalogDataItemProcessorValidationException.class)
			.listener(securityContextStepExecutionListener)
			.listener(mdcContextDatasourceItemProcessingListener)
			.build();
	}

	@Bean
	@StepScope
	DatasourceCatalogDataImportItemReader datasourceImportContextItemReader(
		BigIdClientFactory bigIdClientFactory,
		@Value("#{jobParameters['" + ORGANIZATION_ID_JOB_PARAM + "']}") Long organizationId,
		@Value("#{jobParameters['" + USER_ID_JOB_PARAM + "']}") Long userId,
		@Value("#{jobParameters['" + CATALOG_IMPORT_JOB_CONTROL + "']}") String jobControl
	) {
		return new DatasourceCatalogDataImportItemReader(
			jsonHelper.fromJson(jobControl, JobExecutionControlVO.class),
			bigIdClientFactory, organizationId, userId);
	}

	@Bean
	@StepScope
	DatasourceCatalogDataImportItemProcessor datasourceImportContextItemProcessor(
		DatasourceCatalogDataImporterFactory datasourceCatalogImportProcessorFactory
	) {
		return new DatasourceCatalogDataImportItemProcessor(datasourceCatalogImportProcessorFactory);
	}

	@Bean
	@StepScope
	DatasourceCatalogDataImportItemWriter datasourceCatalogDataImportItemWriter(StorageDocumentsService s3UploadHelper) {
		return new DatasourceCatalogDataImportItemWriter(s3UploadHelper);
	}

	@Bean
	Step appDsImportStep(AppDsCatalogDataImportItemReader appDsImportContextItemReader,
						 AppDsCatalogDataImportItemProcessor appDsImportContextItemProcessor,
						 AppDsCatalogDataImportItemWriter appDsCatalogDataImportItemWriter,
						 ChunkListener securityContextStepExecutionListener,
						 MDCContextAppDsConnectionItemProcessingListener mdcContextAppDsConnectionItemProcessingListener) {
		return stepBuilders.get(APP_DS_IMPORT_STEP_NAME)
			.<AppDsConnectionCatalogDataImporterParamVO, AppDsConnectionCatalogDataImporterResultVO>chunk(appDsChunkSize)
			.reader(appDsImportContextItemReader)
			.processor(appDsImportContextItemProcessor)
			.writer(appDsCatalogDataImportItemWriter)
			.faultTolerant()
			.skip(CatalogDataItemProcessorValidationException.class)
			.listener(securityContextStepExecutionListener)
			.listener(mdcContextAppDsConnectionItemProcessingListener)
			.build();
	}

	@Bean
	@StepScope
	AppDsCatalogDataImportItemReader appDsImportContextItemReader(
		BigIdClientFactory bigIdClientFactory,
		DatasourceClient datasourceClient,
		@Value("#{jobParameters['" + ORGANIZATION_ID_JOB_PARAM + "']}") Long organizationId,
		@Value("#{jobParameters['" + USER_ID_JOB_PARAM + "']}") Long userId,
		@Value("#{jobParameters['" + CATALOG_IMPORT_JOB_CONTROL + "']}") String jobControl
	) {
		return new AppDsCatalogDataImportItemReader(
			jsonHelper.fromJson(jobControl, JobExecutionControlVO.class),
			bigIdClientFactory, organizationId, userId);
	}

	@Bean
	@StepScope
	AppDsCatalogDataImportItemProcessor appDsImportContextItemProcessor(
		AppDsConnectionCatalogDataImporterFactory appDsConnectionCatalogDataImporterFactory
	) {
		return new AppDsCatalogDataImportItemProcessor(appDsConnectionCatalogDataImporterFactory);
	}

	@Bean
	@StepScope
	AppDsCatalogDataImportItemWriter appDsCatalogDataImportItemWriter(StorageDocumentsService s3UploadHelper) {
		return new AppDsCatalogDataImportItemWriter(s3UploadHelper);
	}

	@Bean
	@StepScope
	ChunkListener securityContextStepExecutionListener(
		@Value("#{jobParameters['" + USER_ID_JOB_PARAM + "']}") Long userId,
		UserRepository userRepository) {
		return new SecurityContextAwareChunkListener(userRepository, userId);
	}

	@Bean
	Step fieldClassifierImportStep(FieldClassifierDataImportItemReader fieldClassifierDataImportItemReader,
								   FieldClassifierDataImportProcessor fieldClassifierDataImportProcessor,
								   FieldClassifierDataImportItemWriter fieldClassifierDataImportItemWriter,
								   ChunkListener securityContextStepExecutionListener,
								   MDCContextFieldClassifierItemProcessingListener mdcContextFieldClassifierItemProcessingListener) {
		return stepBuilders.get(FIELD_CLASSIFIER_IMPORT_STEP_NAME)
			.<FieldClassifierDataImporterParamVO, FieldClassifierDataImporterResultVO>chunk(fieldClassifierChunkSize)
			.reader(fieldClassifierDataImportItemReader)
			.processor(fieldClassifierDataImportProcessor)
			.writer(fieldClassifierDataImportItemWriter)
			.faultTolerant()
			.skip(CatalogDataItemProcessorValidationException.class)
			.listener(securityContextStepExecutionListener)
			.listener(mdcContextFieldClassifierItemProcessingListener)
			.build();
	}

	@Bean
	@StepScope
	FieldClassifierDataImportItemReader fieldClassifierDataImportItemReader(
		BigIdClientFactory bigIdClientFactory,
		@Value("#{jobParameters['" + ORGANIZATION_ID_JOB_PARAM + "']}") Long organizationId,
		@Value("#{jobParameters['" + USER_ID_JOB_PARAM + "']}") Long userId,
		@Value("#{jobParameters['" + CATALOG_IMPORT_JOB_CONTROL + "']}") String jobControl
	) {
		return new FieldClassifierDataImportItemReader(
			jsonHelper.fromJson(jobControl, JobExecutionControlVO.class),
			bigIdClientFactory, organizationId, userId);
	}

	@Bean
	@StepScope
	FieldClassifierDataImportProcessor fieldClassifierDataImportProcessor(
		FieldClassifierDataImporterFactory fieldClassifierDataImporterFactory
	) {
		return new FieldClassifierDataImportProcessor(fieldClassifierDataImporterFactory);
	}

	@Bean
	@StepScope
	FieldClassifierDataImportItemWriter fieldClassifierDataImportItemWriter(StorageDocumentsService s3UploadHelper) {
		return new FieldClassifierDataImportItemWriter(s3UploadHelper);
	}

	@Bean
	Step complianceRuleImportStep(ComplianceRuleCatalogDataImportItemReader complianceRuleCatalogDataImportItemReader,
								  ComplianceRuleDataImportItemProcessor complianceRuleDataImportItemProcessor,
								  ComplianceRuleDataImportItemWriter complianceRuleDataImportItemWriter,
								  ChunkListener securityContextStepExecutionListener,
								  MDCContextComplianceRuleItemProcessingListener mdcContextComplianceRuleItemProcessingListener) {
		return stepBuilders.get(COMPLIANCE_RULE_IMPORT_STEP_NAME)
			.<ComplianceRuleImporterParamVO, ComplianceRuleImporterResultVO>chunk(complianceRuleChunkSize)
			.reader(complianceRuleCatalogDataImportItemReader)
			.processor(complianceRuleDataImportItemProcessor)
			.writer(complianceRuleDataImportItemWriter)
			.faultTolerant()
			.skip(CatalogDataItemProcessorValidationException.class)
			.listener(securityContextStepExecutionListener)
			.listener(mdcContextComplianceRuleItemProcessingListener)
			.build();
	}

	@Bean
	@StepScope
	ComplianceRuleCatalogDataImportItemReader complianceRuleCatalogDataImportItemReader(
		BigIdClientFactory bigIdClientFactory,
		@Value("#{jobParameters['" + ORGANIZATION_ID_JOB_PARAM + "']}") Long organizationId,
		@Value("#{jobParameters['" + USER_ID_JOB_PARAM + "']}") Long userId,
		@Value("#{jobParameters['" + CATALOG_IMPORT_JOB_CONTROL + "']}") String jobControl
	) {
		return new ComplianceRuleCatalogDataImportItemReader(
			jsonHelper.fromJson(jobControl, JobExecutionControlVO.class),
			bigIdClientFactory, organizationId, userId);
	}

	@Bean
	@StepScope
	ComplianceRuleDataImportItemProcessor complianceRuleDataImportItemProcessor(
		ComplianceRuleDataImporterFactory complianceRuleImporterFactory
	) {
		return new ComplianceRuleDataImportItemProcessor(complianceRuleImporterFactory);
	}

	@Bean
	@StepScope
	ComplianceRuleDataImportItemWriter complianceRuleDataImportItemWriter(StorageDocumentsService s3UploadHelper) {
		return new ComplianceRuleDataImportItemWriter(s3UploadHelper);
	}

	@Bean
	Step sensitivityClassificationImportStep(SensitivityClassificationImportItemReader sensitivityClassificationImportItemReader,
											 SensitivityClassificationDataImportItemProcessor sensitivityClassificationDataImportItemProcessor,
											 SensitivityClassificationDataImportItemWriter sensitivityClassificationDataImportItemWriter,
											 ChunkListener securityContextStepExecutionListener,
											 MDCContextSensivityClassificationItemProcessingListener mdcContextSensivityClassificationItemProcessingListener) {
		return stepBuilders.get(SENSITIVITY_CLASSIFICATION_IMPORT_STEP_NAME)
			.<SensitivityClassificationDataImporterParamVO, SensitivityClassificationDataImporterResultVO>chunk(fieldClassifierChunkSize)
			.reader(sensitivityClassificationImportItemReader)
			.processor(sensitivityClassificationDataImportItemProcessor)
			.writer(sensitivityClassificationDataImportItemWriter)
			.faultTolerant()
			.skip(CatalogDataItemProcessorValidationException.class)
			.listener(securityContextStepExecutionListener)
			.listener(mdcContextSensivityClassificationItemProcessingListener)
			.build();
	}

	@Bean
	@StepScope
	SensitivityClassificationImportItemReader sensitivityClassificationImportItemReader(
		BigIdClientFactory bigIdClientFactory,
		@Value("#{jobParameters['" + ORGANIZATION_ID_JOB_PARAM + "']}") Long organizationId,
		@Value("#{jobParameters['" + USER_ID_JOB_PARAM + "']}") Long userId,
		@Value("#{jobParameters['" + CATALOG_IMPORT_JOB_CONTROL + "']}") String jobControl
	) {
		return new SensitivityClassificationImportItemReader(
			jsonHelper.fromJson(jobControl, JobExecutionControlVO.class),
			bigIdClientFactory, organizationId, userId);
	}

	@Bean
	@StepScope
	SensitivityClassificationDataImportItemProcessor sensitivityClassificationDataImportItemProcessor(
		SensitivityClassificationDataImporterFactory sensitivityClassificationDataImporterFactory) {
		return new SensitivityClassificationDataImportItemProcessor(sensitivityClassificationDataImporterFactory);
	}

	@Bean
	@StepScope
	SensitivityClassificationDataImportItemWriter sensitivityClassificationDataImportItemWriter(StorageDocumentsService s3UploadHelper) {
		return new SensitivityClassificationDataImportItemWriter(s3UploadHelper);
	}

}
