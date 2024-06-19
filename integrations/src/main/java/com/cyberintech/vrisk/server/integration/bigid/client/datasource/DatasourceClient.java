package com.cyberintech.vrisk.server.integration.bigid.client.datasource;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceBatchResponseVO;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceVO;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceVOWrapper;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.withtag.DatasourceWithTagsBatchResponseVO;
import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.withtag.DsConnectionsVO;
import com.cyberintech.vrisk.server.integration.bigid.client.exception.RemoteClientException;
import com.cyberintech.vrisk.server.integration.bigid.client.util.RemoteClientWrapperUtil;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class DatasourceClient extends BigIdClient {

	public DatasourceClient(RestTemplate restTemplate,
		BigidConfigurationProperties configurationProperties) {
		super(restTemplate, configurationProperties);
	}

	public List<DatasourceVO> getAll() {
		return RemoteClientWrapperUtil.wrap(
			() -> Optional
				.ofNullable(getRestTemplate().getForObject(formatRequestUrl("/api/v1/ds_connections?format=json"),
					DatasourceBatchResponseVO.class))
				.map(DatasourceBatchResponseVO::getResults)
				.orElseThrow(() -> new RemoteClientException("Datasource list expected.")),
			"get data sources. non-pageable.");
	}

	public DatasourceVO getByName(String datasourceName) {
		return RemoteClientWrapperUtil
			.wrap(
				() -> Optional
					.ofNullable(getRestTemplate().getForObject(
						formatRequestUrl(
							"/api/v1/ds_connections/{dsname}?format=json&shouldKeepDisabledCustomFields=true"),
						DatasourceVOWrapper.class, Map.ofEntries(Map.entry("dsname", datasourceName))))
					.map(DatasourceVOWrapper::getDatasourceVO)
					.orElseThrow(
						() -> new RemoteClientException(String.format("Datasource '%s' expected.", datasourceName))),
				String.format("get data sources. by name = %s", datasourceName));
	}

	public List<DatasourceVO> getChunkWithTags(int offset, int limit) {
		return RemoteClientWrapperUtil.wrap(
			() -> Optional
				.ofNullable(getRestTemplate().getForObject(
					formatRequestUrl("/api/v1/ds-connections?format=json&skip={skip}&limit={limit}"),
					DatasourceWithTagsBatchResponseVO.class,
					Map.ofEntries(
						Map.entry("skip", offset),
						Map.entry("limit", limit))))
				.map(DatasourceWithTagsBatchResponseVO::getResults)
				.map(DsConnectionsVO::getDatasources)
				.orElseThrow(() -> new RemoteClientException("Datasource list is expected.")),
			String.format("get datasources. chunk. offset = %s, limit = %s", offset, limit));
	}
}
