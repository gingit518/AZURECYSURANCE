package com.cyberintech.vrisk.server.security.mfa;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * oAuth2 Multi Factor Authentication Exception
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 */
public class MFACodeException extends OAuth2Exception {

	/**
	 * MFA error Constructor
	 *
	 * @param message
	 */
	public MFACodeException(String message) {
		super(message);
	}

	/**
	 * Get oAuth Error Code
	 *
	 * @return
	 */
	public String getOAuth2ErrorCode() {
		return "mfa_code_failed";
	}

	public int getHttpErrorCode() {
		return 400;
	}
}
