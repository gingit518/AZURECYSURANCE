package com.cyberintech.vrisk.server.model.jpa.domains;

import org.apache.commons.lang3.StringUtils;

/**
 * System statuses
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
public enum SystemStatus {
	ACTIVE, PENDING, DISABLED;

	/**
	 * Return status from string
	 *
	 * @param name
	 * @return
	 */
	public static SystemStatus ofString(String name) {
		if (StringUtils.isNotEmpty(name)) {
			if (name.equalsIgnoreCase(ACTIVE.name())) {
				return ACTIVE;
			} else if (name.equalsIgnoreCase(PENDING.name())) {
				return PENDING;
			} else if (name.equalsIgnoreCase(DISABLED.name())) {
				return DISABLED;
			}
		}

		return null;
	}
}
