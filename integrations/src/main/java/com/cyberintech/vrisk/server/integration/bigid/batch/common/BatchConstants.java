package com.cyberintech.vrisk.server.integration.bigid.batch.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BatchConstants {
	public static final String CATALOG_IMPORT_JOB_NAME = "big-id-catalog-data-import";
	public static final String JOB_ID_PARAM = "job-id";
	public static final String APPLICATION_IMPORT_STEP_NAME = "application-catalog-data-import-step";
	public static final String DATASOURCE_IMPORT_STEP_NAME = "datasource-catalog-data-import-step";
	public static final String APP_DS_IMPORT_STEP_NAME = "app-ds-catalog-data-import-step";
	public static final String COMPLIANCE_RULE_IMPORT_STEP_NAME = "compliance-rule-catalog-data-import-step";
	public static final String FIELD_CLASSIFIER_IMPORT_STEP_NAME = "field-classifier-catalog-data-import-step";
	public static final String SENSITIVITY_CLASSIFICATION_IMPORT_STEP_NAME = "sensitivity-classification-catalog-data-import-step";
	public static final String ORGANIZATION_ID_JOB_PARAM = "organization-id";
	public static final String USER_ID_JOB_PARAM = "user-id";
	public static final String CATALOG_IMPORT_JOB_CONTROL = "job-execution-control";
	public static final String CHUNK_COUNTER_PARAM = "chunk-counter";
}
