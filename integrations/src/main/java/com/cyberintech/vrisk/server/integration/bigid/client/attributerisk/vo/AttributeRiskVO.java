package com.cyberintech.vrisk.server.integration.bigid.client.attributerisk.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AttributeRiskVO {
	private String id;
	@JsonProperty("glossary_id")
	private String glossaryId;
	@JsonProperty("original_names")
	private List<String> originalNames;
	@JsonProperty("short_name")
	private String shortName;
	private String name;
	private long count;
	private long avg;
	@JsonProperty("id_score")
	private long idScore;
	@JsonProperty("min_score")
	private long minScore;
	@JsonProperty("max_score")
	private long maxScore;
}
