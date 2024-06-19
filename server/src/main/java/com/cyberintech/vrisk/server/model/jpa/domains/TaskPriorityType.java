package com.cyberintech.vrisk.server.model.jpa.domains;

import org.apache.commons.lang3.StringUtils;

/**
 * Task Priority types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-09
 */
public enum TaskPriorityType {
	LOW
	, MEDIUM
	, HIGH
	, URGENT
	;

	/**
	 * Get proper Task Priority from String
	 *
	 * @param typeName
	 * @return
	 */
	public static TaskPriorityType of(String typeName) {
		return of(typeName, MEDIUM);
	}

	/**
	 * Get proper Task Priority from String
	 *
	 * @param typeName
	 * @return
	 */
	public static TaskPriorityType of(String typeName, TaskPriorityType defaultType) {
		TaskPriorityType result = defaultType;

		if (StringUtils.isNotEmpty(typeName)) {
			TaskPriorityType tmpTypeValue = TaskPriorityType.valueOf(typeName);
			if (tmpTypeValue != null) {
				result = tmpTypeValue;
			}
		}

		return result;
	}
}
