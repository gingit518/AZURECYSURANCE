package org.springframework.security.oauth2.common;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Ryan Heaton
 */
public class DefaultExpiringOAuth2RefreshToken implements Serializable, OAuth2RefreshToken {

	private static final long serialVersionUID = 3449554332764129719L;

	private Date expiration;

	private String value;

	public DefaultExpiringOAuth2RefreshToken() {
		;
	}

	public DefaultExpiringOAuth2RefreshToken(String value) {
		this.value = value;
	}

	/**
	 * @param value
	 */
	public DefaultExpiringOAuth2RefreshToken(String value, Date expiration) {
		this.value = value;
		this.expiration = expiration;
	}

	/**
	 * The instant the token expires.
	 *
	 * @return The instant the token expires.
	 */
	public Date getExpiration() {
		return expiration;
	}

	@Override
	public String getValue() {
		return value;
	}

}
