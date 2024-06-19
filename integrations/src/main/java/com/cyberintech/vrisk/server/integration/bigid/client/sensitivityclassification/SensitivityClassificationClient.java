package com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification.vo.SCConfigVO;
import com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification.vo.SCConfigsListWrapperVO;
import com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification.vo.SensitivityClassificationResponseVO;
import com.cyberintech.vrisk.server.integration.bigid.client.util.RemoteClientWrapperUtil;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SensitivityClassificationClient extends BigIdClient {

	public SensitivityClassificationClient(RestTemplate restTemplate,
		BigidConfigurationProperties configurationProperties) {
		super(restTemplate, configurationProperties);
	}

	public List<SCConfigVO> getDefault() {
		return RemoteClientWrapperUtil.wrap(
			() -> Optional.ofNullable(getRestTemplate().getForObject(
				formatRequestUrl(
					"/api/v1/aci/sc/configs?skip={skip}&limit={limit}&requireTotalCount=true&filter={filter}"),
				SensitivityClassificationResponseVO.class,
				Map.ofEntries(
					Map.entry("skip", 0),
					Map.entry("limit", 100),
					Map.entry("filter", "[{\"field\":\"name\",\"value\":\"default\",\"operator\":\"equal\"}]"))))
				.map(SensitivityClassificationResponseVO::getData)
				.map(SCConfigsListWrapperVO::getScConfigs)
				.orElse(Collections.emptyList()),
			"get default Sensitivity Classification config");
	}

}
