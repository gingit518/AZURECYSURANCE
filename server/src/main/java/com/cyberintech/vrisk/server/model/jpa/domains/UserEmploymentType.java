package com.cyberintech.vrisk.server.model.jpa.domains;

import org.apache.commons.lang3.StringUtils;

/**
 * User Employment types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-03
 */
public enum UserEmploymentType {
	EMPLOYEE,
	CONTRACTOR;

	/**
	 * Get proper UserEmploymentType from String
	 *
	 * @param typeName
	 * @return
	 */
	public static UserEmploymentType of(String typeName) {
		return of(typeName, EMPLOYEE);
	}

	/**
	 * Get proper UserEmploymentType from String
	 *
	 * @param typeName
	 * @return
	 */
	public static UserEmploymentType of(String typeName, UserEmploymentType defaultType) {
		UserEmploymentType result = defaultType;

		if (StringUtils.isNotEmpty(typeName)) {
			UserEmploymentType tmpTypeValue = UserEmploymentType.valueOf(typeName);
			if (tmpTypeValue != null) {
				result = tmpTypeValue;
			}
		}

		return result;
	}
}
