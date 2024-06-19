package com.cyberintech.vrisk.server.integration.bigid.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.cyberintech.vrisk.server.integration.bigid.client.auth.AuthenticationClient;
import com.cyberintech.vrisk.server.integration.bigid.client.auth.AuthorizationToken;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;

import java.io.InputStream;
import java.util.Properties;

public class BaseClientTest {

	private BigidConfigurationProperties configurationProperties;

	private String authorizationToken = null;

	public String getAuthorizationToken() {
		if (authorizationToken == null) {
			AuthenticationClient client = new AuthenticationClient(getConfigurationProperties());
			AuthorizationToken token = client.getToken();

			assertNotNull(token, "Authorization token should be obtained from BigID");
			this.authorizationToken = token.getValue();
		}
		return authorizationToken;
	}

	public BigidConfigurationProperties getConfigurationProperties() {
		if (configurationProperties == null) {
			loadConfigurationProperties();
		}
		return configurationProperties;
	}

	private void loadConfigurationProperties() {
		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.properties")) {
			Properties properties = new Properties();
			properties.load(inputStream);

			BigidConfigurationProperties configurationProperties = new BigidConfigurationProperties();
			configurationProperties.setBaseServiceUrl(properties.getProperty("bigid.base_url"));
			configurationProperties.setTokenName(properties.getProperty("bigid.clientId"));
			configurationProperties.setTokenValue(properties.getProperty("bigid.clientSecret"));
			this.configurationProperties = configurationProperties;

		} catch (Exception e) {
			fail("Configuration properties not loaded");
		}
	}

}
