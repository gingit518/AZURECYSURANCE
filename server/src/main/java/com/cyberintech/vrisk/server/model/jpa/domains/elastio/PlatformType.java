package com.cyberintech.vrisk.server.model.jpa.domains.elastio;

import org.apache.commons.lang3.StringUtils;

/**
 * Platform types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2026-03-01
 */
public enum PlatformType {
	AWS
	, Azure
	, GCP
	, RDS
	;

	/**
	 * Get proper VendorType from String
	 *
	 * @param questionTypeName
	 * @return
	 */
	public static PlatformType of(String questionTypeName) {
		return of(questionTypeName, null);
	}

	/**
	 * Get proper VendorType from String
	 *
	 * @param questionTypeName
	 * @return
	 */
	public static PlatformType of(String questionTypeName, PlatformType defaultVendorType) {
		PlatformType result = defaultVendorType;

		if (StringUtils.isNotEmpty(questionTypeName)) {
			result = PlatformType.valueOf(questionTypeName);
		}

		return result;
	}

}
