package com.cyberintech.vrisk.server.integration.bigid.client.attributerisk.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AttributeRiskWrapper {
	@JsonProperty("attribute_risks")
	private List<AttributeRiskVO> attributeRisks;
}
