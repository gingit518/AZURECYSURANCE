package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Language Constant Scope Type
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-06-25
 */
public enum LanguageConstantScopeType {

	UI, ADMIN, SERVER, HINTS, ANDROID_CLIENT, IOS_CLIENT, HELP, MENU, DASHBOARD;

	/**
	 * Get proper Language Constant Type from String
	 *
	 * @param value
	 * @return LanguageConstantScopeType
	 */
	public static LanguageConstantScopeType of(String value) {
		LanguageConstantScopeType result = null;

		LanguageConstantScopeType tmpValue = LanguageConstantScopeType.valueOf(value);
		if (tmpValue != null) {
			result = tmpValue;
		}

		return result;
	}
}
