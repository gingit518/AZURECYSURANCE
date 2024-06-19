package com.cyberintech.vrisk.server.service.dashboards;

import java.text.MessageFormat;

/**
 * Dashboards Configuration
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-02
 */
public class DashboardsConfig {

	public static final Long DASHBOARD_VENDOR = 1L;
	public static final Long DASHBOARD_ORGANIZATION = 2L;
	public static final Long DASHBOARD_ADMIN = 3L;
	public static final Long DASHBOARD_PRIVACY = 4L;
	public static final Long DASHBOARD_CLOUD = 5L;
	/** Executive M & A */
	public static final Long DASHBOARD_M_AND_A = 6L;
	public static final Long DASHBOARD_MY_ASSIGNMENTS = 41L;
	public static final Long DASHBOARD_QUESTION_STATUS = 35L;
	public static final Long DASHBOARD_VENDOR_QUESTION_STATUS = 35002L;
	public static final Long DASHBOARD_ASSIGNMENTS_STATUS = 36L;
	/** CISO Crown Jewel Asset */
	public static final Long DASHBOARD_CROWN_JEWEL_ASSET = 101L;
	public static final Long DASHBOARD_PRIVACY_RISK = 102L;
	/** CISO Cybersecurity Tool ROI */
	public static final Long DASHBOARD_CYBERSECURITY_TOOL_ROI = 103L;
	public static final Long DASHBOARD_BUDGETING = 104L;
	public static final Long DASHBOARD_BUDGET_SCENARIO_ANALYSIS = 105L;
	public static final Long DASHBOARD_RESIDUAL_CYBER_RISK = 115L;
	public static final Long DASHBOARD_PHI_DATA = 120L;
	public static final Long DASHBOARD_ETL_SYSTEMS_EXPOSURES = 121L;
	public static final Long DASHBOARD_IOT_EXPOSURES = 122L;
	public static final Long DASHBOARD_UNINSURABLE_EXPOSURES = 123L;
	public static final Long DASHBOARD_ASSESSMENTS = 124L;
	public static final Long DASHBOARD_ASSESSMENTS_AUDIT = 1241L;
	public static final Long DASHBOARD_TASK = 125L;
	public static final Long DASHBOARD_RESOURCES = 126L;
	public static final Long DASHBOARD_ORGANIZATION_SYSTEM_GDPR = 127L;
	public static final Long DASHBOARD_RISK_REGISTER = 128L;
	public static final Long DASHBOARD_ASSESSMENT_FINDING = 129L;
	public static final Long DASHBOARD_VENDOR_STATUS = 135L;
	public static final Long DASHBOARD_DATA_EXFILTRATION = 137L;
	public static final Long DASHBOARD_VENDOR_SELF_ASSESSMENT = 1001L;
	public static final Long DASHBOARD_VENDOR_AND_SYSTEM = 1002L;
	/** Executive Cyber Insurance */
	public static final Long DASHBOARD_CYBER_INSURANCE = 2000L;
	/** CFO Crown Jewel Asset*/
	public static final Long DASHBOARD_CFO_CROWN_JEWEL_ASSET = 2001L;
	/** CFO Cybersecurity Tool ROI */
	public static final Long DASHBOARD_CFO_CYBERSECURITY_TOOL_ROI = 2002L;
	/** CFO M & A */
	public static final Long DASHBOARD_CFO_M_AND_A = 2003L;
	/** CFO Cyber Insurance */
	public static final Long DASHBOARD_CFO_CYBER_INSURANCE = 2004L;
	/** CFO Vendor */
	public static final Long DASHBOARD_CFO_VENDOR = 2005L;
	public static final Long DASHBOARD_PRIVACY_IMPACT_ASSESSMENT = 2006L;

	public static final Long FFIEC_CAT_CYBER_MATURITY = 16647L;

	public static final Long FFIEC_CAT_INHERENT_RISK = 16648L;
//	public static final Long DASHBOARD_TEST_ITEM = 1102L;

	public static final String CROWN_JEWEL_REPORT = "__CROWN_JEWEL_REPORT";
	public static final String PRIVACY_RISK_REPORT = "__PRIVACY_RISK_REPORT";
	public static final String DASHBOARD_VENDOR_STATUS_REPORT = "__DASHBOARD_VENDOR_STATUS_REPORT";
	public static final String DASHBOARD_VENDOR_AND_SYSTEM_REPORT = "__DASHBOARD_VENDOR_AND_SYSTEM_REPORT";
	public static final String DASHBOARD_EXPOSURE_RISK_REPORT = "__DASHBOARD_EXPOSURE_RISK_REPORT";

	public static final String SCORING_QUESTIONS_URL_TEMPLATE = "/private/cyber-risk-scoring/questions/{0,number,#}?metric={1}";
	public static final String CLOUD_SCORING_QUESTIONS_URL_TEMPLATE = "/private/cloud-scoring/questions/scoring/{0,number,#}?metric={1}";
	public static final String VENDOR_SCORING_QUESTIONS_URL_TEMPLATE = "/private/vendors/questions/scoring/{0,number,#}?metric={1}";

	/**
	 * Build URL Template for Scoring metric Questions page
	 *
	 * @param systemId
	 * @param metric
	 * @return
	 */
	public static String buildSystemRiskQualQuestionsUrl(Long systemId, String metric) {
		return MessageFormat.format(SCORING_QUESTIONS_URL_TEMPLATE, systemId, metric);
	}

	/**
	 * Build URL Template for Scoring metric Questions page
	 *
	 * @param vendorId
	 * @param metric
	 * @return
	 */
	public static String buildCloudRiskQualQuestionsUrl(Long vendorId, String metric) {
		return MessageFormat.format(CLOUD_SCORING_QUESTIONS_URL_TEMPLATE, vendorId, metric);
	}

	/**
	 * Build URL Template for Scoring metric Questions page
	 *
	 * @param vendorId
	 * @param metric
	 * @return
	 */
	public static String buildVendorRiskQualQuestionsUrl(Long vendorId, String metric) {
		return MessageFormat.format(VENDOR_SCORING_QUESTIONS_URL_TEMPLATE, vendorId, metric);
	}

}
