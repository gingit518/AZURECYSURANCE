package com.cyberintech.vrisk.server.integration.bigid.client.compliancerule;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.client.compliancerule.vo.ComplianceRuleVO;
import com.cyberintech.vrisk.server.integration.bigid.client.util.RemoteClientWrapperUtil;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ComplianceRuleClient extends BigIdClient {

	public ComplianceRuleClient(RestTemplate restTemplate, BigidConfigurationProperties configurationProperties) {
		super(restTemplate, configurationProperties);
	}

	public List<ComplianceRuleVO> getAll() {
		return RemoteClientWrapperUtil.wrap(
			() -> Optional.ofNullable(getRestTemplate().getForObject(formatRequestUrl("/api/v1/compliance-rules"),
				ComplianceRuleVO[].class)).map(Arrays::asList).orElse(Collections.emptyList()),
			"get all compliance rules");
	}

}
