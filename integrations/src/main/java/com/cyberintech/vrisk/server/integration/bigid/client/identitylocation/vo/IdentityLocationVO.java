package com.cyberintech.vrisk.server.integration.bigid.client.identitylocation.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IdentityLocationVO {
	private String id;
	private String name;
	private long count;
	@JsonProperty("avg")
	private long average;
}
