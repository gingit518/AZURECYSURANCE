package com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo;

import com.cyberintech.vrisk.server.integration.bigid.client.common.IBatchResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Data
public class DatasourceBatchResponseVO implements IBatchResponse<DatasourceVO> {
	@JsonProperty("ds_connections")
	private List<DatasourceVO> results;

	@Override
	public long getTotalRowsCounter() {
		return CollectionUtils.size(results);
	}
}
