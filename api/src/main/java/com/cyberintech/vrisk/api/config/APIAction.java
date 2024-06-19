package com.cyberintech.vrisk.api.config;

/**
 * Permission codes
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-30
 */
public class APIAction {
	public static final String RISK_MODEL_READ = "risk_model_read";
	public static final String RISK_MODEL_CREATE = "risk_model_create";
	public static final String RISK_MODEL_UPDATE = "risk_model_update";
	public static final String RISK_MODEL_DELETE = "risk_model_delete";
	public static final String VENDOR_READ = "vendor_read";
	public static final String VENDOR_CREATE = "vendor_create";
	public static final String VENDOR_UPDATE = "vendor_update";
//	public static final String VENDOR_DELETE = "vendor_delete";
	public static final String VENDOR_INTERNAL_QUESTION = "vendor_internal_question";
	public static final String USER_READ = "user_read";
	public static final String USER_CREATE = "user_create";
	public static final String USER_UPDATE = "user_update";
	public static final String USER_DELETE = "user_delete";
	public static final String IMPORT_ASSESSMENT_FRAMEWORK = "import_assessment_framework";
	public static final String IMPORT_BUSINESS_UNIT = "import_business_unit";
	public static final String IMPORT_CONTROL_GUIDELINE = "import_control_guideline";
	public static final String IMPORT_GDPR_ARTICLE = "import_gdpr_article";
	public static final String IMPORT_GDPR_EVIDENCE_ARTICLES = "import_gdpr_evidence_articles";
	public static final String IMPORT_QUALITATIVE_QUESTION = "import_qualitative_question";
	public static final String IMPORT_QUALITATIVE_QUESTION_ANSWERS = "import_qualitative_question_answers";
	public static final String IMPORT_PROCESS = "import_process";
	public static final String IMPORT_SECURITY_REQUIREMENTS = "import_security_requirements";
	public static final String IMPORT_SUBSIDIARY = "import_subsidiary";
	public static final String IMPORT_SYSTEM_RISK = "import_system_risk";
	public static final String IMPORT_TECHNOLOGY = "import_technology";
	public static final String IMPORT_USER = "import_user";
	public static final String IMPORT_VENDOR = "import_vendor";
	public static final String IMPORT_QUANT_METRICS = "quantification_metric_import";

	public static final String ADMIN_ORGANIZATION_READ = "admin_organization_read";
	public static final String ADMIN_ORGANIZATION_UPDATE = "admin_organization_update";

	public static final String ADMIN_ORGANIZATION_CREATE = "admin_organization_create";
	public static final String ADMIN_ORGANIZATION_DELETE = "admin_organization_delete";


	public static final String ORGANIZATION_READ = "organization_read";
	public static final String ORGANIZATION_UPDATE = "organization_update";
	public static final String ORGANIZATION_RECORD_PRICE_LIMIT = "organization_record_price_limit";
	public static final String ORGANIZATION_AGGREGATE_INSURANCE_LIMIT = "organization_aggregate_insurance_limit";
	public static final String ORGANIZATION_CYBER_INSURANCE_DEDUCTIBLE = "organization_cyber_insurance_deductible";
	public static final String ORGANIZATION_AVERAGE_REVENUE = "organization_average_revenue";
	public static final String ORGANIZATION_MARKET_CAPITALIZATION = "organization_market_capitalization";
	public static final String ORGANIZATION_RISK_SCORING_READ = "organization_risk_scoring_read";
	public static final String ORGANIZATION_CYBER_INSURANCE_INFORMATION = "organization_cyber_insurance_information";
	public static final String ORGANIZATION_LOAD_TEST_DATA = "organization_load_test_data";
	public static final String REGULATORY_EXPOSURE_UPDATE = "regulatory_exposure_update";

	public static final String SUBSIDIARY_READ = "subsidiary_read";
	public static final String SUBSIDIARY_CREATE = "subsidiary_create";
	public static final String SUBSIDIARY_UPDATE = "subsidiary_update";
	public static final String SUBSIDIARY_DELETE = "subsidiary_delete";
	public static final String SUBSIDIARY_EXPORT = "export_subsidiary";

	public static final String BUSINESS_UNIT_READ = "business_unit_read";
	public static final String BUSINESS_UNIT_CREATE = "business_unit_create";
	public static final String BUSINESS_UNIT_UPDATE = "business_unit_update";
	public static final String BUSINESS_UNIT_DELETE = "business_unit_delete";
	public static final String BUSINESS_UNIT_DOWNLOAD = "export_business_unit";
	public static final String BUSINESS_UNIT_DOWNLOAD_TEMPLATE = "export_business_unit_template";
	public static final String BUSINESS_UNIT_UPLOAD = "import_business_unit";

	public static final String ADMIN_BUSINESS_UNIT_READ = "admin_business_unit_read";
	public static final String ADMIN_BUSINESS_UNIT_CREATE = "admin_business_unit_create";
	public static final String ADMIN_BUSINESS_UNIT_UPDATE = "admin_business_unit_update";
	public static final String ADMIN_BUSINESS_UNIT_DELETE = "admin_business_unit_delete";

	public static final String ASSET_CLASS_READ = "asset_class_read";
	public static final String ASSET_CLASS_CREATE = "asset_class_create";
	public static final String ASSET_CLASS_UPDATE = "asset_class_update";
	public static final String ASSET_CLASS_DELETE = "asset_class_delete";

	public static final String DATA_CLASS_READ = "data_class_read";
	public static final String DATA_CLASS_CREATE = "data_class_create";
	public static final String DATA_CLASS_UPDATE = "data_class_update";
	public static final String DATA_CLASS_DELETE = "data_class_delete";

	public static final String DATA_DOMAIN_READ = "data_domain_read";
	public static final String DATA_DOMAIN_CREATE = "data_domain_create";
	public static final String DATA_DOMAIN_UPDATE = "data_domain_update";
	public static final String DATA_DOMAIN_DELETE = "data_domain_delete";

	public static final String DATA_FIELD_READ = "data_field_read";
	public static final String DATA_FIELD_CREATE = "data_field_create";
	public static final String DATA_FIELD_UPDATE = "data_field_update";
	public static final String DATA_FIELD_DELETE = "data_field_delete";

	public static final String ENVIRONMENT_TYPE_READ = "environment_type_read";
	public static final String ENVIRONMENT_TYPE_CREATE = "environment_type_create";
	public static final String ENVIRONMENT_TYPE_UPDATE = "environment_type_update";
	public static final String ENVIRONMENT_TYPE_DELETE = "environment_type_delete";

	public static final String ADMIN_DATA_CLASS_READ = "admin_data_class_read";
	public static final String ADMIN_DATA_CLASS_CREATE = "admin_data_class_create";
	public static final String ADMIN_DATA_CLASS_UPDATE = "admin_data_class_update";
	public static final String ADMIN_DATA_CLASS_DELETE = "admin_data_class_delete";

	public static final String SYSTEM_READ = "system_read";
	public static final String SYSTEM_CREATE = "system_create";
	public static final String SYSTEM_UPDATE = "system_update";
	public static final String SYSTEM_DELETE = "system_delete";
	public static final String DATA_EXFILTRATION_READ = "data_exfiltration_read";
	public static final String DATA_EXFILTRATION_UPDATE = "data_exfiltration_update";
	public static final String REGULATORY_EXPOSURE_READ = "regulatory_exposure_read";

	public static final String PROCESS_READ = "process_read";
	public static final String PROCESS_CREATE = "process_create";
	public static final String PROCESS_UPDATE = "process_update";
	public static final String PROCESS_DELETE = "process_delete";
	public static final String PROCESS_EXPORT = "export_process";
	public static final String PROCESS_EXPORT_TEMPLATE = "export_process_template";
	public static final String PROCESS_UPLOAD = "import_process";

	public static final String BUSINESS_INTERRUPTION_READ = "business_interruption_read";
	public static final String BUSINESS_INTERRUPTION_UPDATE = "business_interruption_update";

	public static final String TECHNOLOGY_CATEGORY_READ = "technology_category_read";
	public static final String TECHNOLOGY_CATEGORY_CREATE = "technology_category_create";
	public static final String TECHNOLOGY_CATEGORY_UPDATE = "technology_category_update";
	public static final String TECHNOLOGY_CATEGORY_DELETE = "technology_category_delete";

	public static final String ADMIN_TECHNOLOGY_CATEGORY_READ = "admin_technology_category_read";
	public static final String ADMIN_TECHNOLOGY_CATEGORY_CREATE = "admin_technology_category_create";
	public static final String ADMIN_TECHNOLOGY_CATEGORY_UPDATE = "admin_technology_category_update";
	public static final String ADMIN_TECHNOLOGY_CATEGORY_DELETE = "admin_technology_category_delete";

	public static final String TECHNOLOGY_READ = "technology_read";
	public static final String TECHNOLOGY_CREATE = "technology_create";
	public static final String TECHNOLOGY_UPDATE = "technology_update";
	public static final String TECHNOLOGY_DELETE = "technology_delete";
	public static final String TECHNOLOGY_EXPORT = "export_technology";
	public static final String TECHNOLOGY_EXPORT_TEMPLATE = "export_technology_template";

	public static final String RISK_SCORING_READ = "risk_scoring_read";
	public static final String RISK_SCORING_UPDATE = "risk_scoring_question";

	public static final String RISK_MODEL_DOMAIN_READ = "domain_read";
	public static final String RISK_MODEL_DOMAIN_CREATE = "domain_create";

	public static final String RISK_MODEL_DOMAIN_UPDATE = "domain_update";
	public static final String RISK_MODEL_DOMAIN_DELETE = "domain_delete";

	public static final String CATEGORY_DOMAIN_READ = "category_read";
	public static final String CATEGORY_DOMAIN_CREATE = "category_create";
	public static final String CATEGORY_DOMAIN_UPDATE = "category_update";

	public static final String QUANTS_READ = "quantification_model_read";
	public static final String QUANTS_CREATE = "quantification_model_create";
	public static final String QUANTS_UPDATE = "quantification_model_update";
	public static final String QUANTS_DELETE = "quantification_model_delete";

	public static final String QUANT_METRIC_READ = "quantification_metric_read";
	public static final String QUANT_METRIC_CREATE = "quantification_metric_create";
	public static final String QUANT_METRIC_UPDATE = "quantification_metric_update";
	public static final String QUANT_METRIC_DELETE = "quantification_metric_delete";
	public static final String QUANT_METRIC_EXPORT = "quantification_metric_export";

	public static final String QUAL_METRIC_READ = "qualitative_metric_read";
	public static final String QUAL_METRIC_CREATE = "qualitative_metric_create";
	public static final String QUAL_METRIC_UPDATE = "qualitative_metric_update";

	public static final String QUAL_QUESTION_READ = "qualitative_question_read";
	public static final String QUAL_QUESTION_CREATE = "qualitative_question_create";
	public static final String QUAL_QUESTION_UPDATE = "qualitative_question_update";
	public static final String QUAL_QUESTION_DELETE = "qualitative_question_delete";
	public static final String EXPORT_QUALITATIVE_QUESTION = "export_qualitative_question";
	public static final String EXPORT_QUALITATIVE_QUESTION_ANSWERS = "export_qualitative_question_answers";
	public static final String METRIC_RESULT_ANSWER_READ = "risk_answer_read";
	public static final String METRIC_RESULT_ANSWER_UPDATE = "risk_answer_update";

	public static final String ASSOCIATE_MODEL_READ = "associate_model_read";
	public static final String ASSOCIATE_MODEL_CREATE = "associate_model_create";
	public static final String ASSOCIATE_MODEL_UPDATE = "associate_model_update";
	public static final String ASSOCIATE_MODEL_DELETE = "associate_model_delete";

	public static final String ASSESSMENT_READ = "assessment_read";
	public static final String ASSESSMENT_CREATE = "assessment_create";
	public static final String ASSESSMENT_UPDATE = "assessment_update";
	public static final String ASSESSMENT_DELETE = "assessment_delete";
	public static final String ASSESSMENT_FRAMEWORK_EXPORT = "export_assessment_framework";

	public static final String ASSESSMENT_TYPE_READ = "assessment_type_read";
	public static final String ASSESSMENT_TYPE_CREATE = "assessment_type_create";
	public static final String ASSESSMENT_TYPE_UPDATE = "assessment_type_update";
	public static final String ASSESSMENT_TYPE_DELETE = "assessment_type_delete";

	public static final String CONTROL_FUNCTION_READ = "nist_csf_category_read";
	public static final String CONTROL_FUNCTION_CREATE = "nist_csf_category_create";
	public static final String CONTROL_FUNCTION_UPDATE = "nist_csf_category_update";
	public static final String CONTROL_FUNCTION_DELETE = "nist_csf_category_delete";

	public static final String CONTROL_MATURITY_READ = "control_maturity_read";
	public static final String CONTROL_MATURITY_CREATE = "control_maturity_create";
	public static final String CONTROL_MATURITY_UPDATE = "control_maturity_update";
	public static final String CONTROL_MATURITY_DELETE = "control_maturity_delete";

	public static final String CONTROL_CATEGORY_READ = "control_test_read";
	public static final String CONTROL_CATEGORY_CREATE = "control_test_create";
	public static final String CONTROL_CATEGORY_UPDATE = "control_test_update";
	public static final String CONTROL_CATEGORY_DELETE = "control_test_delete";

	public static final String CONTROL_SUBCATEGORY_READ = "control_guideline_read";
	public static final String CONTROL_SUBCATEGORY_CREATE = "control_guideline_create";
	public static final String CONTROL_SUBCATEGORY_UPDATE = "control_guideline_update";
	public static final String CONTROL_SUBCATEGORY_DELETE = "control_guideline_delete";
	public static final String CONTROL_SUBCATEGORY_EXPORT = "export_control_guideline";

	public static final String ASSESSMENT_FINDING_READ = "assessment_finding_read";
	public static final String ASSESSMENT_FINDING_CREATE = "assessment_finding_create";
	public static final String ASSESSMENT_FINDING_UPDATE = "assessment_finding_update";
	public static final String ASSESSMENT_FINDING_DELETE = "assessment_finding_delete";

	public static final String VENDOR_QUAL_QUESTION = "vendor_scoring_question";
	public static final String VENDOR_EXPORT = "export_vendor";

	public static final String CLOUD_SCORING_READ = "cloud_scoring_read";
	public static final String CLOUD_SCORING_QUESTION = "cloud_scoring_question";

	public static final String ASSOCIATE_VENDOR_READ = "associate_system_read";
	public static final String ASSOCIATE_VENDOR_UPDATE = "associate_system_update";

	public static final String GDPR_ORGANIZATION_COMPLIANCE_READ = "gdpr_organization_compliance_read";
	public static final String GDPR_ORGANIZATION_COMPLIANCE_SCORING_QUESTION = "gdpr_organization_compliance_scoring_question";
	public static final String GDPR_ORGANIZATION_COMPLIANCE_UPDATE = "gdpr_organization_compliance_update";

	public static final String GDPR_SYSTEM_COMPLIANCE_READ = "gdpr_system_compliance_read";
	public static final String GDPR_SYSTEM_ARTICLE_COMPLIANCE_READ = "gdpr_system_article_compliance_read";
	public static final String GDPR_SYSTEM_ARTICLE_COMPLIANCE_UPDATE = "gdpr_system_article_compliance_update";
	public static final String GDPR_SYSTEM_COMPLIANCE_SCORING_QUESTION = "gdpr_system_compliance_scoring_question";
	public static final String GDPR_EVIDENCE_DOCUMENT_READ = "gdpr_evidence_document_read";
	public static final String GDPR_EVIDENCE_DOCUMENT_UPDATE = "gdpr_evidence_document_update";

	public static final String GDPR_ARTICLE_EXPORT = "export_gdpr_article";

	public static final String CYBER_SECURITY_TOOL_READ = "security_tool_read";
	public static final String CYBER_SECURITY_TOOL_CREATE = "security_tool_create";
	public static final String CYBER_SECURITY_TOOL_UPDATE = "security_tool_update";
	public static final String CYBER_SECURITY_TOOL_DELETE = "security_tool_delete";

	public static final String CYBER_ROLE_READ = "cyber_role_read";
	public static final String CYBER_ROLE_CREATE = "cyber_role_create";
	public static final String CYBER_ROLE_UPDATE = "cyber_role_update";
	public static final String CYBER_ROLE_DELETE = "cyber_role_delete";

	public static final String FIXED_OPERATIONAL_COST_READ = "operational_cost_read";
	public static final String FIXED_OPERATIONAL_COST_CREATE = "operational_cost_create";
	public static final String FIXED_OPERATIONAL_COST_UPDATE = "operational_cost_update";
	public static final String FIXED_OPERATIONAL_COST_DELETE = "operational_cost_delete";

	public static final String FIXED_CAPITAL_COST_READ = "capital_cost_read";
	public static final String FIXED_CAPITAL_COST_CREATE = "capital_cost_create";
	public static final String FIXED_CAPITAL_COST_UPDATE = "capital_cost_update";
	public static final String FIXED_CAPITAL_COST_DELETE = "capital_cost_delete";

	public static final String VARIABLE_COST_READ = "variable_cost_read";
	public static final String VARIABLE_COST_CREATE = "variable_cost_create";
	public static final String VARIABLE_COST_UPDATE = "variable_cost_update";
	public static final String VARIABLE_COST_DELETE = "variable_cost_delete";

	public static final String USER_SETTING_READ = "user_settings_read";
	public static final String USER_SETTING_UPDATE = "user_settings_update";
	public static final String USER_EXPORT = "export_user";

	public static final String SYSTEM_RISK_EXPORT = "export_system_risk";
	public static final String SYSTEM_RISK_REDUCTION_READ = "system_risk_reduction_read";
	public static final String SYSTEM_RISK_REDUCTION_UPDATE = "system_risk_reduction_update";

	public static final String DASHBOARD_QUESTION_STATUS = "dashboard_question_status";
	public static final String DASHBOARD_ASSIGNMENT_STATUS = "dashboard_assignment_status";
	public static final String DASHBOARD_QUESTION_STATUS_REPORT = "dashboard_question_status_report";
	public static final String DASHBOARD_RESIDUAL_CYBER_RISK = "dashboard_residual_cyber_risk";
	public static final String DASHBOARD_BUSINESS_UNIT_QUESTION_STATUS = "dashboard_business_unit_question_status";

	public static final String AUDIT_LOG_READ = "audit_log_read";
	public static final String AUDIT_LOG_DETAILS = "audit_log_details";

	public static final String TASK_READ = "task_read";
	public static final String TASK_CREATE = "task_create";
	public static final String TASK_UPDATE = "task_update";
	public static final String TASK_DELETE = "task_delete";

	public static final String RISK_METRIC_READ = "risk_metric_read";
	public static final String RISK_METRIC_CREATE = "risk_metric_create";
	public static final String RISK_METRIC_UPDATE = "risk_metric_update";
	public static final String RISK_METRIC_DELETE = "risk_metric_delete";

	public static final String ORGANIZATION_CONTROL_TEST_RESULT_DOWNLOAD = "organization_control_test_result_download";

	public static final String SYSTEM_CONTROL_TEST_RESULT_READ = "system_control_test_result_read";
	public static final String SYSTEM_CONTROL_TEST_RESULT_UPDATE = "system_control_test_result_update";
	public static final String SYSTEM_CONTROL_TEST_RESULT_DRILLDOWN = "system_control_test_result_drilldown";
	public static final String SYSTEM_CONTROL_TEST_RESULT_DOWNLOAD = "system_control_test_result_download";

	public static final String SECURITY_REQUIREMENT_READ = "security_requirement_read";
	public static final String SECURITY_REQUIREMENT_CREATE = "security_requirement_create";
	public static final String SECURITY_REQUIREMENT_UPDATE = "security_requirement_update";
	public static final String SECURITY_REQUIREMENT_DELETE = "security_requirement_delete";
	public static final String SECURITY_REQUIREMENT_EXPORT = "export_security_requirements";

	public static final String PRIVACY_IMPACT_ASSESSMENT_READ = "privacy_impact_assessment_read"; // duplicates Dashboard Permission Type - DASHBOARD_PRIVACY_RISK

	public static final String CYBERSECURITY_MATURITY_READ = "cybersecurity_maturity_read";

	public static final String POLICY_READ = "policy_read";
	public static final String POLICY_CREATE = "policy_create";
	public static final String POLICY_UPDATE = "policy_update";
	public static final String POLICY_DELETE = "policy_delete";
	public static final String POLICY_ANNUAL_REVIEW = "policy_annual_review";
	public static final String POLICY_APPROVAL = "policy_approval";

	public static final String CONTRACT_READ = "contract_read";
	public static final String CONTRACT_CREATE = "contract_create";
	public static final String CONTRACT_UPDATE = "contract_update";
	public static final String CONTRACT_DELETE = "contract_delete";

	public static final String VULNERABILITY_READ = "vulnerability_read";
	public static final String VULNERABILITY_CREATE = "vulnerability_create";
	public static final String VULNERABILITY_UPDATE = "vulnerability_update";
	public static final String VULNERABILITY_DELETE = "vulnerability_delete";

	public static final String RISK_MODEL_CONSTANT_READ = "risk_model_constant_read";
	public static final String RISK_MODEL_CONSTANT_CREATE = "risk_model_constant_create";
	public static final String RISK_MODEL_CONSTANT_UPDATE = "risk_model_constant_update";
	public static final String RISK_MODEL_CONSTANT_DELETE = "risk_model_constant_delete";

	public static final String REGULATION_READ = "regulation_read";
	public static final String REGULATION_CREATE = "regulation_create";
	public static final String REGULATION_UPDATE = "regulation_update";
	public static final String REGULATION_DELETE = "regulation_delete";

	public static final String EXTERNAL_ANALYTICS_READ = "external_analytics_read";
	public static final String EXTERNAL_ANALYTICS_CREATE = "external_analytics_create";
	public static final String EXTERNAL_ANALYTICS_UPDATE = "external_analytics_update";
	public static final String EXTERNAL_ANALYTICS_DELETE = "external_analytics_delete";

	public static final String RISK_MODEL_RECALCULATE_CACHE = "risk_model_recalculate_cache";

	public static final String HINT_READ = "hint_read";
	public static final String HINT_CREATE = "hint_create";
	public static final String HINT_UPDATE = "hint_update";
	public static final String HINT_DELETE = "hint_delete";
	public static final String HINTS_EXPORT = "hint_export";
	public static final String HINTS_IMPORT = "hint_import";

	public static final String MENU_READ = "menu_read";
	public static final String MENU_CREATE = "menu_create";
	public static final String MENU_UPDATE = "menu_update";
	public static final String MENU_DELETE = "menu_delete";

	public static final String INDUSTRY_READ = "industry_read";
	public static final String INDUSTRY_CREATE = "industry_create";
	public static final String INDUSTRY_UPDATE = "industry_update";
	public static final String INDUSTRY_DELETE = "industry_delete";
	public static final String INDUSTRY_EXPORT = "industry_export";
	public static final String INDUSTRY_IMPORT = "industry_import";

}
