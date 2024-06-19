package com.cyberintech.vrisk.server.model.jpa.domains;

import org.apache.commons.lang3.StringUtils;

/**
 * System deployment types.
 *
 * @author  Andrii Iakovenko
 * @version 0.0.1
 * @since   2022-07-12
 */
public enum DeploymentType {
	ON_PREMISE, CLOUD, HYBRID;

	/**
	 * Get proper DeploymentType from String
	 *
	 * @param typeName
	 * @return
	 */
	public static DeploymentType of(String typeName) {
		return of(typeName, null);
	}

	/**
	 * Get proper DeploymentType from String
	 *
	 * @param typeName
	 * @return
	 */
	public static DeploymentType of(String typeName, DeploymentType defaultType) {
		DeploymentType result = defaultType;

		if (StringUtils.isNotEmpty(typeName)) {
			DeploymentType tmpType = DeploymentType.valueOf(typeName);
			if (tmpType != null) {
				result = tmpType;
			}
		}

		return result;
	}
}
