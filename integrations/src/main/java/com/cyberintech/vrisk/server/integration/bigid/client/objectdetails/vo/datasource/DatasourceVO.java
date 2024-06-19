package com.cyberintech.vrisk.server.integration.bigid.client.objectdetails.vo.datasource;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DatasourceVO {
	@JsonProperty("_id")
	private String idInternal;
	private String id;
	private String name;
	private List<String> owners;
	private String location;
	private String type;
}
