package com.cyberintech.vrisk.server.integration.bigid.client.compliancerule.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ComplianceRuleVO {
	private String id;
	private String name;
	private ComplianceRuleCalcVO complianceRuleCalc;
	private ComplianceRuleType type;
	private String category;
	private String owner;
	@JsonProperty("is_enabled")
	private boolean enabled;
}
