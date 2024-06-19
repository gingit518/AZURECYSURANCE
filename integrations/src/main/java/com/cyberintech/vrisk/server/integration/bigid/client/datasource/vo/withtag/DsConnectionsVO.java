package com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.withtag;

import com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo.DatasourceVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DsConnectionsVO {
	@JsonProperty("ds_connections")
	private List<DatasourceVO> datasources;
}
