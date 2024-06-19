package com.cyberintech.vrisk.server.integration.bigid.client.datasource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cyberintech.vrisk.server.integration.bigid.client.BaseClientTest;
import com.cyberintech.vrisk.server.integration.bigid.client.auth.BigIdAuthRequestInterceptor;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class DatasourceClientTest extends BaseClientTest {

	private DatasourceClient datasourceClient;

	@BeforeEach
	void setup() {
		RestTemplate bigIdRestTemplate = new RestTemplate();
		List<ClientHttpRequestInterceptor> interceptors = bigIdRestTemplate.getInterceptors();
		interceptors.add(new BigIdAuthRequestInterceptor(getAuthorizationToken()));

		this.datasourceClient = new DatasourceClient(bigIdRestTemplate, getConfigurationProperties());
	}

	@Test
	void getAll() {
		List<DatasourceVO> datasources = this.datasourceClient.getAll();
		assertNotNull(datasources);
		assertFalse(datasources.isEmpty());
	}

}
