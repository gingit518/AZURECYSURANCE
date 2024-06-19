package com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo;

import lombok.Data;

@Data
public class OwnerVO {
	private String id;
	private String email;
	private OwnerType type;
}
