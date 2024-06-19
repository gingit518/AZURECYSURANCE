package com.cyberintech.vrisk.server.integration.bigid.client.datacatalog;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.client.datacatalog.vo.DataCatalogBatchResponseVO;
import com.cyberintech.vrisk.server.integration.bigid.client.datacatalog.vo.DataCatalogVO;
import com.cyberintech.vrisk.server.integration.bigid.client.util.PageScroller;
import com.cyberintech.vrisk.server.integration.bigid.client.util.RemoteClientWrapperUtil;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCatalogClient extends BigIdClient {

	private static Map<String, Object> unwrapPageRequestParams(PageRequest pageRequest, Map<String, Object> params) {
		Map<String, Object> pageableParams = Map.ofEntries(
			Map.entry("limit", pageRequest.getPageSize()),
			Map.entry("offset", pageRequest.getOffset()));
		HashMap<String, Object> result = new HashMap<>(params);
		result.putAll(pageableParams);
		return result;
	}

	public DataCatalogClient(RestTemplate restTemplate, BigidConfigurationProperties configurationProperties) {
		super(restTemplate, configurationProperties);
	}

	public List<DataCatalogVO> getAll() {
		return new PageScroller<>(50, this::getPage).scroll();
	}

	public DataCatalogBatchResponseVO getPage(PageRequest pageRequest) {
		return RemoteClientWrapperUtil.wrap(
			() -> getRestTemplate().getForObject(
				formatRequestUrl("/api/v1/data-catalog?format=json&offset={offset}&limit={limit}&filter={filter}"),
				DataCatalogBatchResponseVO.class,
				unwrapPageRequestParams(pageRequest, Collections.emptyMap())),
			String.format("get data catalogs. pageable. Page size = %s, offset = %s", pageRequest.getPageSize(),
				pageRequest.getOffset()));
	}

	public DataCatalogBatchResponseVO getPage(PageRequest pageRequest, Map<String, Object> params) {
		return RemoteClientWrapperUtil.wrap(
			() -> getRestTemplate().getForObject(
				formatRequestUrl("/api/v1/data-catalog?format=json&offset={offset}&limit={limit}&filter={filter}"),
				DataCatalogBatchResponseVO.class,
				unwrapPageRequestParams(pageRequest, params)),
			String.format("get data catalogs. pageable. params = %s", params));
	}

	public List<DataCatalogVO> getAllByFilter(String filter) {
		return new PageScroller<>(50, pr -> getPage(pr, Map.of("filter", filter))).scroll();
	}

	public List<DataCatalogVO> getAllByApplication(String application) {
		return getAllByFilter(String.format("application in (\"%s\")", application));
	}
}
