package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Two Factor Type Relation
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.0
 * @since    2023-09-10
 */
public enum TwoFactorType {

	NONE, PHONE, TOTP;

	/**
	 * Get proper Two Factor Type from String
	 *
	 * @param value
	 * @return Two FactorType
	 */
	public static TwoFactorType of(String value) {
		TwoFactorType result = null;

		TwoFactorType tmpValue = TwoFactorType.valueOf(value);
		if (tmpValue != null) {
			result = tmpValue;
		}

		return result;
	}
}
