package com.cyberintech.vrisk.server.integration.bigid.client.classifier.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FieldClassifierVO {
	@JsonProperty("_id")
	private String id;
	@JsonProperty("classification_name")
	private String name;
	private boolean enabled;
	private String description;
}
