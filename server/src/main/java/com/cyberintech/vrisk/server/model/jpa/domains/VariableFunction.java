package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Variable Function
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-16
 */
public enum VariableFunction {
	MAX, MIN, ABS, NONE;

	/**
	 * Get proper VendorType from String
	 *
	 * @param value
	 * @return
	 */
	public static VariableFunction of(String value) {
		VariableFunction result = NONE;

		VariableFunction tmpValue = VariableFunction.valueOf(value);
		if (tmpValue != null) {
			result = tmpValue;
		}

		return result;
	}
}
