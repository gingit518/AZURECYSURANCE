package com.cyberintech.vrisk.server.service.integrations.cysurance.dto;

import lombok.Data;

@Data
public class CysuranceQueryResponse {
	private String success;
	private Integer status;
	private String code;
	private String message;
	private CysuranceQueryResponseData data;
}
