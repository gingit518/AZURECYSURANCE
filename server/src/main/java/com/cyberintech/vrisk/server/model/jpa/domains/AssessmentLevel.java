package com.cyberintech.vrisk.server.model.jpa.domains;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Predefined Assessment Levels
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.02.04
 */
@Getter
@ToString(of = {"id", "name"})
public enum AssessmentLevel {

	/**
	 * Organization assessment level
	 */
	ORG(1L, "Org", "Org"),

	/**
	 * System assessment level
	 */
	SYSTEM(2L, "System", "System"),

	/**
	 * Process assessment level
	 */
	PROCESS(3L, "Process", "Process"),

	/**
	 * Technology assessment level
	 */
	TECHNOLOGY(4L, "Technology", "Technology");

	private final Long id;

	private final String name;

	private final String description;

	private AssessmentLevel(long id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	/**
	 * Returns Assessment Level by ID
	 *
	 * @param id
	 * @return AssessmentLevel
	 */
	public static AssessmentLevel of(Long id) {
		AssessmentLevel result = null;

		if(id != null) {
			switch (id.intValue()) {
				case 1:
					result = ORG;
					break;
				case 2:
					result = SYSTEM;
					break;
				case 3:
					result = PROCESS;
					break;
				case 4:
					result = TECHNOLOGY;
					break;
			}
		}

		return result;
	}
}
