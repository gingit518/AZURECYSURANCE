package com.cyberintech.vrisk.server.integration.bigid.client.systemuser.vo;

import com.cyberintech.vrisk.server.integration.bigid.client.common.IBatchResponse;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
public class SystemUserBatchResponseVO implements IBatchResponse<SystemUserVO> {
	private SystemUserListWrapper data;
	private long totalCount;

	@Override
	public List<SystemUserVO> getResults() {
		return Optional.ofNullable(data)
			.map(SystemUserListWrapper::getUsers)
			.filter(CollectionUtils::isNotEmpty)
			.map(Collections::unmodifiableList)
			.orElse(Collections.emptyList());
	}

	@Override
	public long getTotalRowsCounter() {
		return totalCount;
	}
}
