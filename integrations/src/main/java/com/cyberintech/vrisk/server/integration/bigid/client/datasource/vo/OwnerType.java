package com.cyberintech.vrisk.server.integration.bigid.client.datasource.vo;

import com.cyberintech.vrisk.server.integration.bigid.client.exception.EnumDeserializationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum OwnerType {
	IT("it"),
	BUSINESS("business");

	private final String code;

	OwnerType(String code) {
		this.code = code;
	}

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	public static OwnerType create(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}

		for (OwnerType ownerType : values()) {
			if (StringUtils.equalsAny(str, ownerType.getCode(), ownerType.name())) {
				return ownerType;
			}
		}

		throw new EnumDeserializationException(String.format("Can not deserialize OwnerType from %s", str));
	}

	@JsonValue
	public String getCode() {
		return code;
	}
}
