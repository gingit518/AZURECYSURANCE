package com.cyberintech.vrisk.server.integration.bigid.client.systemuser;

import com.cyberintech.vrisk.server.integration.bigid.client.BigIdClient;
import com.cyberintech.vrisk.server.integration.bigid.client.systemuser.vo.SystemUserBatchResponseVO;
import com.cyberintech.vrisk.server.integration.bigid.client.systemuser.vo.SystemUserVO;
import com.cyberintech.vrisk.server.integration.bigid.client.util.PageScroller;
import com.cyberintech.vrisk.server.integration.bigid.client.util.RemoteClientWrapperUtil;
import com.cyberintech.vrisk.server.integration.bigid.configuration.BigidConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class SystemUserClient extends BigIdClient {

	private static Map<String, Object> unwrapPageRequestParams(PageRequest pageRequest) {
		return Map.ofEntries(
			Map.entry("limit", pageRequest.getPageSize()),
			Map.entry("offset", pageRequest.getOffset()));
	}

	public SystemUserClient(RestTemplate restTemplate, BigidConfigurationProperties configurationProperties) {
		super(restTemplate, configurationProperties);
	}

	public List<SystemUserVO> getAll() {
		return new PageScroller<>(50, this::getPage).scroll();
	}

	public SystemUserBatchResponseVO getPage(PageRequest pageRequest) {
		return RemoteClientWrapperUtil.wrap(
			() -> getRestTemplate().getForObject(
				formatRequestUrl("/api/v1/system-users?format=json&offset={offset}&limit={limit}"),
				SystemUserBatchResponseVO.class,
				unwrapPageRequestParams(pageRequest)),
			String.format("get system users. limit = %s, offset = %s", pageRequest.getPageSize(),
				pageRequest.getOffset()));
	}

}
