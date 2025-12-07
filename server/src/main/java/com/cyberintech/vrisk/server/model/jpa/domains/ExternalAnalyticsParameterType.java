package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * External Analytics Qlik Parameters List
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
public enum ExternalAnalyticsParameterType {
	USER_EMAIL
	, USER_NAME
	, USER_GROUPS
	, TENANT_DOMAIN
	, WEB_INTEGRATION_ID
	, APPLICATION_ID
	, ISSUER
	, API_KEY_ID
	, OBJECT_ID
	, EMBED_URL
	, HEIGHT
	, WIDTH

	, POWERBI_CLIENT_ID
	, POWERBI_WORKSPACE_ID
	, POWERBI_REPORT_ID
	, POWERBI_PAGE_NAME

	, DASHBOARD_REPORT_ID
	, DASHBOARD_SECTION_NAME
	, DASHBOARD_CONFIG_JSON
}
