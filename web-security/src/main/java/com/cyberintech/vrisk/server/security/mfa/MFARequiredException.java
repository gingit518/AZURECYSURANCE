package com.cyberintech.vrisk.server.security.mfa;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * oAuth2 Multi Factor Authentication Exception
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 */
public class MFARequiredException extends OAuth2Exception {

	/**
	 * MFA error Constructor
	 *
	 * @param mfaToken
	 */
	public MFARequiredException(String mfaToken, String mfaRefreshToken, String mfaType) {
		super("Multi-factor authentication required");
        this.addAdditionalInformation("mfa_token", mfaToken);
        this.addAdditionalInformation("mfa_refresh_token", mfaRefreshToken);
        this.addAdditionalInformation("mfa_type", mfaType);
	}

	/**
	 * Get oAuth Error Code
	 *
	 * @return
	 */
	public String getOAuth2ErrorCode() {
		return "mfa_required";
	}

	public int getHttpErrorCode() {
		return 403;
	}
}
