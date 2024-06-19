package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Language Direction Type Relation
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-04-20
 */
public enum LanguageDirection {
	LTR, RTL;

	/**
	 * Get proper Language Direction from String
	 *
	 * @param value
	 * @return LanguageDirection
	 */
	public static LanguageDirection of(String value) {
		LanguageDirection result = null;

		LanguageDirection tmpValue = LanguageDirection.valueOf(value);
		if (tmpValue != null) {
			result = tmpValue;
		}

		return result;
	}
}
