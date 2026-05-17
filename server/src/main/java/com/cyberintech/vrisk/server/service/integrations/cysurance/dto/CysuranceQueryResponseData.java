package com.cyberintech.vrisk.server.service.integrations.cysurance.dto;

import lombok.Data;

import java.util.List;

@Data
public class CysuranceQueryResponseData {
	private String reportingPartnerCode;
	private String scope;
	private List<CysuranceQueryResponseDataEntity> entities;
}
