package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Idp Type Relation
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-05-13
 */
public enum IdpType {

	GOOGLE, MICROSOFT;

	/**
	 * Get proper Idp Type from String
	 *
	 * @param value
	 * @return IdpType
	 */
	public static IdpType of(String value) {
		IdpType result = null;

		IdpType tmpValue = IdpType.valueOf(value);
		if (tmpValue != null) {
			result = tmpValue;
		}

		return result;
	}
}
