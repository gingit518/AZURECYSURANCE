package com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cyberintech.vrisk.server.integration.bigid.client.BaseClientTest;
import com.cyberintech.vrisk.server.integration.bigid.client.auth.BigIdAuthRequestInterceptor;
import com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification.vo.SCConfigVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class SensitivityClassificationClientTest extends BaseClientTest {

	private SensitivityClassificationClient client;

	@BeforeEach
	void setup() {
		RestTemplate bigIdRestTemplate = new RestTemplate();
		List<ClientHttpRequestInterceptor> interceptors = bigIdRestTemplate.getInterceptors();
		interceptors.add(new BigIdAuthRequestInterceptor(getAuthorizationToken()));

		this.client = new SensitivityClassificationClient(bigIdRestTemplate, getConfigurationProperties());
	}

	@Test
	void getAll() {
		List<SCConfigVO> classifications = this.client.getDefault();
		assertNotNull(classifications);
		assertFalse(classifications.isEmpty());
	}

}
