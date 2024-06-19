package com.cyberintech.vrisk.server.integration.bigid.client.systemlocation.vo;

import lombok.Data;

import java.util.List;

@Data
public class SystemLocationVO {
	private String id;
	private String name;
	private long count;
	private long average;
	private List<String> systems;
}
