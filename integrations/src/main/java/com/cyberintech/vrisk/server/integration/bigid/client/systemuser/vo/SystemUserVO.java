package com.cyberintech.vrisk.server.integration.bigid.client.systemuser.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SystemUserVO {
	@JsonProperty("_id")
	private String idInternal;
	private String id;
	private String name;
	private String firstName;
	private String lastName;
	private String origin;
	private String email;
	private boolean admin;
	@JsonProperty("__v")
	private String version;
	private List<String> roleIds;
}
