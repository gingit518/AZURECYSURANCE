package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Relation to Requirement Type
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-07-06
 */
public enum RelationToRequirementType {

	ALL_REQUIREMENTS, FRAMEWORKS, REQUIREMENTS;

	/**
	 * Get proper Relation to Requirement Type from String
	 *
	 * @param value
	 * @return RelationToRequirementType
	 */
	public static RelationToRequirementType of(String value) {
		RelationToRequirementType result = null;

		RelationToRequirementType tmpValue = RelationToRequirementType.valueOf(value);
		if (tmpValue != null) {
			result = tmpValue;
		}

		return result;
	}
}
