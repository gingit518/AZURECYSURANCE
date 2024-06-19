package com.cyberintech.vrisk.server.integration.bigid.client.attributerisk;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.client.attributerisk.vo.AttributeRiskVO;
import com.cyberintech.vrisk.server.integration.bigid.client.attributerisk.vo.AttributeRiskWrapper;
import com.cyberintech.vrisk.server.integration.bigid.client.util.RemoteClientWrapperUtil;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AttributeRiskClient extends BigIdClient {

	public AttributeRiskClient(RestTemplate restTemplate, BigidConfigurationProperties configurationProperties) {
		super(restTemplate, configurationProperties);
	}

	public List<AttributeRiskVO> getAll(String applicationName, String datasource) {
		return RemoteClientWrapperUtil.wrap(
			() -> Optional
				.ofNullable(getRestTemplate().getForObject(formatRequestUrl("/api/v1/attributeRisks?filter={filter}"),
					AttributeRiskWrapper.class,
					Map.of("filter",
						String.format("application = \"%s\" AND system = \"%s\"", applicationName, datasource))))
				.map(AttributeRiskWrapper::getAttributeRisks).orElse(Collections.emptyList()),
			String.format("get attribute risks: application = %s, datasource = %s", applicationName, datasource));
	}

}
