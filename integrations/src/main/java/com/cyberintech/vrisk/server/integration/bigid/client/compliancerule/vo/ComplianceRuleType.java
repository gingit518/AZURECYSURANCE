package com.cyberintech.vrisk.server.integration.bigid.client.compliancerule.vo;

import com.cyberintech.vrisk.server.integration.bigid.client.exception.EnumDeserializationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
//MOVE TO DB
public enum ComplianceRuleType {
	ACCESS_GOVERANCE("access_governance"), CATALOG("catalog"), PRIVACY("privacy"), UNKNOWN(null);

	private final String code;

	ComplianceRuleType(String code) {
		this.code = code;
	}

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	public static ComplianceRuleType create(String str) {
		if (StringUtils.isBlank(str)) {
			return UNKNOWN;
		}

		for (ComplianceRuleType policyType : values()) {
			if (StringUtils.equalsAny(str, policyType.getCode())) {
				return policyType;
			}
		}

		throw new EnumDeserializationException(String.format("Can not deserialize ComplianceRuleType from %s", str));
	}

	public String getCode() {
		return code;
	}
}
