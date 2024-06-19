package com.cyberintech.vrisk.server.integration.bigid.client;

import com.cyberintech.vrisk.server.integration.bigid.client.application.ApplicationClient;
import com.cyberintech.vrisk.server.integration.bigid.client.attributerisk.AttributeRiskClient;
import com.cyberintech.vrisk.server.integration.bigid.client.auth.AuthenticationClient;
import com.cyberintech.vrisk.server.integration.bigid.client.auth.BigIdAuthRequestInterceptor;
import com.cyberintech.vrisk.server.integration.bigid.client.auth.BigIdAuthorizationTokenProvider;
import com.cyberintech.vrisk.server.integration.bigid.client.classifier.FieldClassifierClient;
import com.cyberintech.vrisk.server.integration.bigid.client.compliancerule.ComplianceRuleClient;
import com.cyberintech.vrisk.server.integration.bigid.client.datacatalog.DataCatalogClient;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.DatasourceClient;
import com.cyberintech.vrisk.server.integration.bigid.client.identitylocation.IdentityLocationClient;
import com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.ObjectDetailsClient;
import com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification.SensitivityClassificationClient;
import com.cyberintech.vrisk.server.integration.bigid.client.systemlocation.SystemLocationClient;
import com.cyberintech.vrisk.server.integration.bigid.client.systemuser.SystemUserClient;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BigIdClientFactory {

	@Autowired
	private BigIdAuthorizationTokenProvider authorizationTokenProvider;

	@Autowired
	private BigIdConfigurationProvider configurationProvider;

	public ApplicationClient createApplicationClient(Long organizationId) {
		return new ApplicationClient(createRestTemplate(organizationId), getConfigurationProperties(organizationId));
	}

	public AttributeRiskClient createAttributeRiskClient(Long organizationId) {
		return new AttributeRiskClient(createRestTemplate(organizationId), getConfigurationProperties(organizationId));
	}

	public AuthenticationClient createAuthenticationClient(Long organizationId) {
		return new AuthenticationClient(getConfigurationProperties(organizationId));
	}

	public ComplianceRuleClient createComplianceRuleClient(Long organizationId) {
		return new ComplianceRuleClient(createRestTemplate(organizationId), getConfigurationProperties(organizationId));
	}

	public DataCatalogClient createDataCatalogClient(Long organizationId) {
		return new DataCatalogClient(createRestTemplate(organizationId), getConfigurationProperties(organizationId));
	}

	public DatasourceClient createDatasourceClient(Long organizationId) {
		return new DatasourceClient(createRestTemplate(organizationId), getConfigurationProperties(organizationId));
	}

	public FieldClassifierClient createFieldClassifierClient(Long organizationId) {
		return new FieldClassifierClient(createRestTemplate(organizationId),
			getConfigurationProperties(organizationId));
	}

	public IdentityLocationClient createIdentityLocationClient(Long organizationId) {
		return new IdentityLocationClient(createRestTemplate(organizationId),
			getConfigurationProperties(organizationId));
	}

	public ObjectDetailsClient createObjectDetailsClient(Long organizationId) {
		return new ObjectDetailsClient(createRestTemplate(organizationId), getConfigurationProperties(organizationId));
	}

	public RestTemplate createRestTemplate(Long organizationId) {
		RestTemplate restTemplate = new RestTemplateBuilder()
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.interceptors(new BigIdAuthRequestInterceptor(this.authorizationTokenProvider.getToken(organizationId)))
			.build();
		return restTemplate;
	}

	public SensitivityClassificationClient createSensitivityClassificationClient(Long organizationId) {
		return new SensitivityClassificationClient(createRestTemplate(organizationId),
			getConfigurationProperties(organizationId));
	}

	public SystemLocationClient createSystemLocationClient(Long organizationId) {
		return new SystemLocationClient(createRestTemplate(organizationId), getConfigurationProperties(organizationId));
	}

	public SystemUserClient createSystemUserClient(Long organizationId) {
		return new SystemUserClient(createRestTemplate(organizationId), getConfigurationProperties(organizationId));
	}

	private BigidConfigurationProperties getConfigurationProperties(Long organizationId) {
		return configurationProvider.getConfigurationProperties(organizationId);
	}

}
