package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Basic Permission Types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-01
 */
public enum PermissionType {

	  DASHBOARD_MY_ASSIGNMENTS("dashboard_my_assignments")
	, DASHBOARD_VENDOR("dashboard_vendor")
	, DASHBOARD_VENDOR_STATUS("dashboard_vendor_status")
	, DASHBOARD_VENDOR_SELF_ASSESSMENT("dashboard_vendor_self_assessment")
	, DASHBOARD_VENDOR_AND_SYSTEM("dashboard_vendor_and_systems")
	, DASHBOARD_ORGANIZATION("dashboard_organization")
	, DASHBOARD_PRIVACY("dashboard_privacy")
	, DASHBOARD_CLOUD("dashboard_cloud")
	, DASHBOARD_M_AND_A("dashboard_m_and_a")
	, DASHBOARD_CROWN_JEWEL_ASSET("dashboard_crown_jewel_asset")
	, DASHBOARD_PRIVACY_RISK("dashboard_privacy_risk")
	, DASHBOARD_CYBERSECURITY_TOOL_ROI("dashboard_cybersecurity_tool_roi")
	, DASHBOARD_BUDGETING("dashboard_budgeting")
	, DASHBOARD_BUDGET_SCENARIO_ANALYSIS("dashboard_budget_scenario_analysis")
	, DASHBOARD_ADMIN("dashboard_admin")
	, DASHBOARD_QUESTION_STATUS("dashboard_question_status")
	, DASHBOARD_VENDOR_QUESTION_STATUS("dashboard_vendor_question_status")
	, DASHBOARD_ASSIGNMENT_STATUS("dashboard_assignment_status")
	, DASHBOARD_QUESTION_STATUS_REPORT("dashboard_question_status_report")
	, DASHBOARD_SYSTEM_QUESTION_STATUS("dashboard_question_status_report_for_systems")
	, DASHBOARD_BUSINESS_UNIT_QUESTION_STATUS("dashboard_business_unit_question_status")
	, DASHBOARD_CISO_DIGITAL_ASSET("dashboard_ciso_digital_asset")
	, DASHBOARD_RESIDUAL_CYBER_RISK("dashboard_residual_cyber_risk")
	, DASHBOARD_TEST_ITEM("dashboard_test_item")
	, ORGANIZATION_AGGREGATE_INSURANCE_LIMIT("organization_aggregate_insurance_limit")
	, ORGANIZATION_AVERAGE_REVENUE("organization_average_revenue")
	, ORGANIZATION_CYBER_INSURANCE_DEDUCTIBLE("organization_cyber_insurance_deductible")
	, ORGANIZATION_MARKET_CAPITALIZATION("organization_market_capitalization")
	, ORGANIZATION_RECORD_PRICE_LIMIT("organization_record_price_limit")
	, ORGANIZATION_RISK_SCORING_READ("organization_risk_scoring_read")
	, ORGANIZATION_SUPPORTED_LANGUAGES_UPDATE("organization_supported_languages_update")
	, DASHBOARD_PHI_DATA("dashboard_phi_data")
	, DASHBOARD_ETL_SYSTEMS_EXPOSURES("dashboard_etl_systems_exposures")
	, DASHBOARD_IOT_EXPOSURES("dashboard_iot_exposures")
	, DASHBOARD_UNINSURABLE_EXPOSURES("dashboard_uninsurable_exposures")
	, DASHBOARD_ASSESSMENT("dashboard_assessment")
	, DASHBOARD_TASK("dashboard_task")
	, DASHBOARD_RESOURCES("dashboard_resources")
	, DASHBOARD_ORGANIZATION_SYSTEM_GDPR("dashboard_organization_system_gdpr")
	, DASHBOARD_RISK_REGISTER("dashboard_risk_register")
	, DASHBOARD_ASSESSMENT_FINDING("dashboard_assessment_findings")
	, DASHBOARD_DATA_EXFILTRATION("dashboard_data_exfiltrations")
	, DASHBOARD_CFO_CROWN_JEWEL_ASSET("dashboard_cfo_crown_jewel_asset")
	, DASHBOARD_AUDIT_ASSESSMENT_AUDIT("dashboard_audit_assessment_audit")
	, DASHBOARD_CFO_CYBERSECURITY_TOOL_ROI("dashboard_cfo_cybersecurity_tool_roi")
	, DASHBOARD_CFO_M_AND_A("dashboard_cfo_m_and_a")
	, DASHBOARD_CFO_ORGANIZATION("dashboard_cfo_organization")
	, DASHBOARD_CFO_VENDOR("dashboard_cfo_vendor")
	, DASHBOARD_PRIVACY_IMPACT_ASSESSMENT("privacy_impact_assessment_read")
	, DASHBOARD_ELASTIO("dashboard_elastio")
	, DASHBOARD_CYSURANCE("dashboard_cysurance")
	;

	private final String _permission;

	private PermissionType(String permission) {
		this._permission = permission;
	}

	@Override
	public String toString() {
		return this.getPermission();
	}

	/**
	 * Get Permission Code Definition
	 *
	 * @return
	 */
	public String getPermission() {
		return _permission;
	}

}
