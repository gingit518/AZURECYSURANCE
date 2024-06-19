package com.cyberintech.vrisk.server.model.jpa.domains;

import org.apache.commons.lang3.StringUtils;

/**
 * System types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-09-10
 */
public enum SystemType {
	COTS, CUSTOM_COTS, HOME_GROWN, MOBILE;

	/**
	 * Get proper SystemType from String
	 *
	 * @param questionTypeName
	 * @return
	 */
	public static SystemType of(String questionTypeName) {
		return of(questionTypeName, null);
	}

	/**
	 * Get proper SystemType from String
	 *
	 * @param questionTypeName
	 * @return
	 */
	public static SystemType of(String questionTypeName, SystemType defaultSystemType) {
		SystemType result = defaultSystemType;

		if (StringUtils.isNotEmpty(questionTypeName)) {
			SystemType tmpSystemType = SystemType.valueOf(questionTypeName);
			if (tmpSystemType != null) {
				result = tmpSystemType;
			}
		}

		return result;
	}
}
