package com.cyberintech.vrisk.server.service.integrations.cysurance.dto;

import lombok.Data;

import java.util.List;

@Data
public class CysuranceQueryResponseDataEntity {
	private String riskEntityIdentifier;
	private String riskEntityType;
	private List<CysuranceQueryResponseDataEntityRating> ratings;
}
