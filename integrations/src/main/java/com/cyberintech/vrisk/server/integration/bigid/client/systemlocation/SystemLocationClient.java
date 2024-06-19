package com.cyberintech.vrisk.server.integration.bigid.client.systemlocation;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.client.exception.RemoteClientException;
import com.cyberintech.vrisk.server.integration.bigid.client.systemlocation.vo.SystemLocationListWrapper;
import com.cyberintech.vrisk.server.integration.bigid.client.systemlocation.vo.SystemLocationVO;
import com.cyberintech.vrisk.server.integration.bigid.client.util.RemoteClientWrapperUtil;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SystemLocationClient extends BigIdClient {

	private static String formatFilterSystemDsQuery(String dsName) {
		return String.format("system=\"%s\"", dsName);
	}

	public SystemLocationClient(RestTemplate restTemplate, BigidConfigurationProperties configurationProperties) {
		super(restTemplate, configurationProperties);
	}

	public List<SystemLocationVO> getBySystemDatasource(String datasourceName) {
		return RemoteClientWrapperUtil.wrap(
			() -> Optional
				.ofNullable(getRestTemplate().getForObject(formatRequestUrl("/api/v1/systemLocations?filter={filter}"),
					SystemLocationListWrapper.class,
					Map.ofEntries(Map.entry("filter", formatFilterSystemDsQuery(datasourceName)))))
				.map(SystemLocationListWrapper::getSystemLocations)
				.orElseThrow(() -> new RemoteClientException(
					String.format("System locations by '%s' datasource name are expected.", datasourceName))),
			String.format("get system locations(pii distribution) by datasource = %s", datasourceName));
	}
}
