package org.springframework.security.oauth2.common;

import com.fasterxml.jackson.annotation.JsonValue;

public interface OAuth2RefreshToken {

	@JsonValue
	String getValue();
}
