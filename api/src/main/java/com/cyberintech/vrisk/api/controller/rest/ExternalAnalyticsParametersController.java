package com.cyberintech.vrisk.api.controller.rest;

import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsParameterType;
import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * External Analytics Parameters Repository
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
@RestController
@RequestMapping(
	value = ExternalAnalyticsParametersController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON,
	name = "External Analytics Parameters Controller"
)
@Tag(name = "External Analytics Parameters Controller")
public class ExternalAnalyticsParametersController {

	static final String CONTROLLER_URI = "/api/external-analytics-parameters";

	/**
	 * Get City List for current Filters
	 *
	 * @return City List
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/defaults", name = "Mapping of default parameters for External Analytics")
	@Parameters({
		@Parameter(name = "authorization", description = "oAuth Access token for API calls", example = "Bearer DF0310", required = true, in = ParameterIn.HEADER)
	})
	public Map<String, Map<String, String>> getDefaults() {

		Map<String, Map<String, String>> result = new HashMap<>();

		Map<String, String> qLik = new HashMap<>();
		qLik.put(ExternalAnalyticsParameterType.USER_EMAIL.name(), "ekalosha@dfusiontech.com");
		qLik.put(ExternalAnalyticsParameterType.USER_NAME.name(), "Eugene Kalosha");
		qLik.put(ExternalAnalyticsParameterType.USER_GROUPS.name(), "[\"Developer\"]");
		qLik.put(ExternalAnalyticsParameterType.TENANT_DOMAIN.name(), "cit.us.qlikcloud.com");
		qLik.put(ExternalAnalyticsParameterType.WEB_INTEGRATION_ID.name(), "Tkfct_v1d3I5DGIf8JED5sb3UDTX9eX3");
		qLik.put(ExternalAnalyticsParameterType.APPLICATION_ID.name(), "ac403e95-244b-4759-8768-2f9e1feaebc4");
		qLik.put(ExternalAnalyticsParameterType.ISSUER.name(), "cit.us.qlikcloud.com");
		qLik.put(ExternalAnalyticsParameterType.API_KEY_ID.name(), "0b4d38e0-c26c-417a-9393-24679103b0dd");
		qLik.put(ExternalAnalyticsParameterType.OBJECT_ID.name(), "aBrBPNq");
		qLik.put(ExternalAnalyticsParameterType.EMBED_URL.name(), "https://cit.us.qlikcloud.com/single/?appid=ac403e95-244b-4759-8768-2f9e1feaebc4&obj=ktwJYQg&opt=ctxmenu,currsel");
		qLik.put(ExternalAnalyticsParameterType.WIDTH.name(), "100%");
		qLik.put(ExternalAnalyticsParameterType.HEIGHT.name(), "768px");

		Map<String, String> powerBI = new HashMap<>();
		powerBI.put(ExternalAnalyticsParameterType.EMBED_URL.name(), "https://app.powerbi.com/reportEmbed?reportId=8c690fe4-b327-486a-9b7d-1fa7d8fc1d7e&autoAuth=true&ctid=fc371555-8bd2-4c98-9e42-e82be54ddb36");
		powerBI.put(ExternalAnalyticsParameterType.POWERBI_CLIENT_ID.name(), "");
		powerBI.put(ExternalAnalyticsParameterType.POWERBI_WORKSPACE_ID.name(), "");
		powerBI.put(ExternalAnalyticsParameterType.POWERBI_REPORT_ID.name(), "");
		powerBI.put(ExternalAnalyticsParameterType.POWERBI_PAGE_NAME.name(), "ALL");
		powerBI.put(ExternalAnalyticsParameterType.WIDTH.name(), "100%");
		powerBI.put(ExternalAnalyticsParameterType.HEIGHT.name(), "768px");


		Map<String, String> dashboard = new HashMap<>();
		dashboard.put(ExternalAnalyticsParameterType.DASHBOARD_REPORT_ID.name(), "1");
		dashboard.put(ExternalAnalyticsParameterType.DASHBOARD_SECTION_NAME.name(), "");

		result.put(ExternalAnalyticsType.QLIK.name(), qLik);
		result.put(ExternalAnalyticsType.POWER_BI.name(), powerBI);
		result.put(ExternalAnalyticsType.DASHBOARD.name(), dashboard);

		return result;
	}

}
