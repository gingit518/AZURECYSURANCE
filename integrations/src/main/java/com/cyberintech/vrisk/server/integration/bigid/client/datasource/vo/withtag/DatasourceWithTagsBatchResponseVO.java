package com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.withtag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DatasourceWithTagsBatchResponseVO {
	@JsonProperty("data")
	private DsConnectionsVO results;
}
