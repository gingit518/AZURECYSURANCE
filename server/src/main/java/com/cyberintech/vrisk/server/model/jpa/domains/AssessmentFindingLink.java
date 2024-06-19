package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Assessment Finding Link Type Relation
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-03-12
 */
public enum AssessmentFindingLink {
	NONE, ASSESSMENT, REQUIREMENT;

	/**
	 * Get proper Assessment Finding Link from String
	 *
	 * @param value
	 * @return AssessmentFindingLink
	 */
	public static AssessmentFindingLink of(String value) {
		AssessmentFindingLink result = null;

		AssessmentFindingLink tmpValue = AssessmentFindingLink.valueOf(value);
		if (tmpValue != null) {
			result = tmpValue;
		}

		return result;
	}
}
