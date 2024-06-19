package com.cyberintech.vrisk.server.model.jpa.domains;

import org.apache.commons.lang3.StringUtils;

/**
 * User Rate types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-03
 */
public enum UserRateType {
	HOURLY
	, DAILY
	, WEEKLY
	, MONTHLY
	, ANNUAL
	;

	/**
	 * Get proper UserRateType from String
	 *
	 * @param typeName
	 * @return
	 */
	public static UserRateType of(String typeName) {
		return of(typeName, ANNUAL);
	}

	/**
	 * Get proper UserRateType from String
	 *
	 * @param typeName
	 * @return
	 */
	public static UserRateType of(String typeName, UserRateType defaultType) {
		UserRateType result = defaultType;

		if (StringUtils.isNotEmpty(typeName)) {
			UserRateType tmpTypeValue = UserRateType.valueOf(typeName);
			if (tmpTypeValue != null) {
				result = tmpTypeValue;
			}
		}

		return result;
	}
}
