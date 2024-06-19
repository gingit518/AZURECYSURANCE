package com.cyberintech.vrisk.server.integration.bigid.client.identitylocation;

import static com.cyberintech.vrisk.server.integration.bigid.client.util.RemoteClientWrapperUtil.wrap;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.client.exception.RemoteClientException;
import com.cyberintech.vrisk.server.integration.bigid.client.identitylocation.vo.IdentityLocationListWrapper;
import com.cyberintech.vrisk.server.integration.bigid.client.identitylocation.vo.IdentityLocationVO;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IdentityLocationClient extends BigIdClient {

	private static String formatFilterSystemDsQuery(String dsName) {
		return String.format("system=\"%s\"", dsName);
	}

	public IdentityLocationClient(RestTemplate restTemplate, BigidConfigurationProperties configurationProperties) {
		super(restTemplate, configurationProperties);
	}

	public List<IdentityLocationVO> getBySystemDatasource(String datasourceName) {
		Map<String, String> filterParams = Map
			.ofEntries(Map.entry("filter", formatFilterSystemDsQuery(datasourceName)));
		return wrap(
			() -> Optional
				.ofNullable(
					getRestTemplate().getForObject(formatRequestUrl("/api/v1/identityLocations?filter={filter}"),
						IdentityLocationListWrapper.class, filterParams))
				.map(IdentityLocationListWrapper::getIdentityLocations)
				.orElseThrow(() -> new RemoteClientException(
					String.format("Identity locations by datasource '%s' are expected.", datasourceName))),
			String.format("get identity locations. by datasource = %s", datasourceName));
	}

}
