package com.cyberintech.vrisk.server.integration.bigid.client.common.datacatalog;

import com.cyberintech.vrisk.server.integration.bigid.client.exception.EnumDeserializationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.StringUtils;

public enum OpenAccess {
	YES("Yes"),
	NO("No"),
	UNKNOWN(StringUtils.EMPTY);

	private final String code;

	OpenAccess(String code) {
		this.code = code;
	}

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	public static OpenAccess create(String str) {
		if (str == null) {
			return null;
		}

		for (OpenAccess ownerType : values()) {
			if (StringUtils.equalsAny(str, ownerType.getCode(), ownerType.name())) {
				return ownerType;
			}
		}

		throw new EnumDeserializationException(String.format("Can not deserialize OpenAccess from %s", str));
	}

	public String getCode() {
		return code;
	}
}
