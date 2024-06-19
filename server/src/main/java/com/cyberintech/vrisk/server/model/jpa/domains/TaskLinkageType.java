package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

/**
 * Predefined Task Linkage Types
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.04.30
 */
@Getter
@ToString(of = {"id", "name"})
public enum TaskLinkageType {

	/**
	 * Assessment Linkage Type
	 */
	ASSESSMENT(1L, "Assessment"),

	/**
	 * Assessment Finding Linkage Type
	 */
	FINDING(2L, "Assessment Finding"),

	/**
	 * Vulnerability Linkage Type
	 */
	VULNERABILITY(3L, "Vulnerability");

	private final Long id;

	private final String name;

	private TaskLinkageType(long id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Returns Task Linkage Type by ID
	 *
	 * @param id
	 * @return TaskLinkageType
	 */
	public static TaskLinkageType of(Long id) {
		TaskLinkageType result = null;

		if (id != null) {
			switch (id.intValue()) {
				case 1:
					result = ASSESSMENT;
					break;
				case 2:
					result = FINDING;
					break;
				case 3:
					result = VULNERABILITY;
					break;
			}
		}

		return result;
	}
}
