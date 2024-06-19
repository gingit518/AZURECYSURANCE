package com.cyberintech.vrisk.server.integration.bigid.client.sensitivityclassification.vo;

import lombok.Data;

import java.util.List;

@Data
public class SCConfigVO {
	private List<SCConfigClassificationVO> classifications;
	private String name;
	private String description;
	private String status;
	private String id;
}
