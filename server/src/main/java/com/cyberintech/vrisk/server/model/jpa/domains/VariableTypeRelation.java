package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Variable Type Relation
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-02-12
 */
public enum VariableTypeRelation {
	QUANT_METRIC , RISK_METRIC;

	/**
	 * Get proper VariableTypeRelation from String
	 *
	 * @param value
	 * @return VariableTypeRelation
	 */
	public static VariableTypeRelation of(String value) {
		VariableTypeRelation result = null;

		VariableTypeRelation tmpValue = VariableTypeRelation.valueOf(value);
		if (tmpValue != null) {
			result = tmpValue;
		}

		return result;
	}
}
