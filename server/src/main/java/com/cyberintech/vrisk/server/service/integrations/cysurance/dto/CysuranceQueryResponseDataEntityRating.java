package com.cyberintech.vrisk.server.service.integrations.cysurance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CysuranceQueryResponseDataEntityRating {
	@JsonProperty("factor_code")
	private String factorCode;

	@JsonProperty("category_code")
	private String categoryCode;

	@JsonProperty("factor_name")
	private String factorName;

	@JsonProperty("value_type")
	private String valueType;

	@JsonProperty("value")
	private Object value;

	@JsonProperty("measured_at")
	private String measuredAt;

	@JsonProperty("reported_by")
	private String reportedBy;

	@JsonProperty("received_at")
	private String receivedAt;

	@JsonProperty("confidence")
	private Double confidence;
}
