package com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DatasourceVOWrapper {
	@JsonProperty("ds_connection")
	private DatasourceVO datasourceVO;
}
