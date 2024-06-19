package com.cyberintech.vrisk.server.security.mfa;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * oAuth2 Multi Factor Authentication Exception
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 */
public class MFATOTPNotSetException extends OAuth2Exception {

	/**
	 * MFA error Constructor
	 *
	 * @param mfaToken
	 */
	public MFATOTPNotSetException(String mfaToken, String mfaRefreshToken, String qrCodeUrl) {
		super("Multi-factor authentication required");
        this.addAdditionalInformation("mfa_token", mfaToken);
        this.addAdditionalInformation("mfa_refresh_token", mfaRefreshToken);
        this.addAdditionalInformation("totp_qr_code_url", qrCodeUrl);
	}

	/**
	 * Get oAuth Error Code
	 *
	 * @return
	 */
	public String getOAuth2ErrorCode() {
		return "mfa_totp_not_set";
	}

	public int getHttpErrorCode() {
		return 403;
	}
}
