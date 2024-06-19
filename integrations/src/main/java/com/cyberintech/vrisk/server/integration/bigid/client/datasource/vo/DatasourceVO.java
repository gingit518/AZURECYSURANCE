package com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(of = "name")
public class DatasourceVO {
	@JsonProperty("_id")
	private String idInternal;
	private String id;
	private String name;
	private String type;
	@JsonProperty("owners_v2")
	private List<OwnerVO> owners;
	private String location;
	private List<TagVO> tags;
}
