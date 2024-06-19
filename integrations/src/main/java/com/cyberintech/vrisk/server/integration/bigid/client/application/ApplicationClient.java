package com.cyberintech.vrisk.server.integration.bigid.client.application;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.client.application.vo.ApplicationListWrapper;
import com.cyberintech.vrisk.server.integration.bigid.client.application.vo.ApplicationVO;
import com.cyberintech.vrisk.server.integration.bigid.client.util.RemoteClientWrapperUtil;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ApplicationClient extends BigIdClient {

	public ApplicationClient(RestTemplate restTemplate, BigidConfigurationProperties configurationProperties) {
		super(restTemplate, configurationProperties);
	}

	public List<ApplicationVO> getAll() {
		return RemoteClientWrapperUtil.wrap(
			() -> Optional
				.ofNullable(getRestTemplate().getForObject(formatRequestUrl("/api/v1/applications"),
					ApplicationListWrapper.class))
				.map(ApplicationListWrapper::getApplications).orElse(Collections.emptyList()),
			"get all applications. non pageable API.");
	}

}
