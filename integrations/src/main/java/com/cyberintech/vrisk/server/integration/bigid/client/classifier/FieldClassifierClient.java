package com.cyberintech.vrisk.server.integration.bigid.client.classifier;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.client.classifier.vo.FieldClassifierVO;
import com.cyberintech.vrisk.server.integration.bigid.client.util.RemoteClientWrapperUtil;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FieldClassifierClient extends BigIdClient {

	public FieldClassifierClient(RestTemplate restTemplate, BigidConfigurationProperties configurationProperties) {
		super(restTemplate, configurationProperties);
	}

	public List<FieldClassifierVO> getAll() {
		return RemoteClientWrapperUtil.wrap(
			() -> Optional.ofNullable(getRestTemplate().getForObject(formatRequestUrl("/api/v1/classifications"),
				FieldClassifierVO[].class)).map(Arrays::asList).orElse(Collections.emptyList()),
			"get all field classifiers");
	}

}
