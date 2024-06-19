package com.cyberintech.vrisk.server.integration.bigid.client;

import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.springframework.web.client.RestTemplate;

public class BigIdClient {

	private final RestTemplate restTemplate;
	private final BigidConfigurationProperties configurationProperties;

	public BigIdClient(RestTemplate restTemplate, BigidConfigurationProperties configurationProperties) {
		this.restTemplate = restTemplate;
		this.configurationProperties = configurationProperties;
	}

	protected String formatRequestUrl(String request) {
		return configurationProperties.getBaseServiceUrl() + request;
	}

	public BigidConfigurationProperties getConfigurationProperties() {
		return configurationProperties;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

}
