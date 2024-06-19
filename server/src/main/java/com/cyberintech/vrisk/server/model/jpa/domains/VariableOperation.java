package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Variable Function
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-16
 */
public enum VariableOperation {
	PLUS, MINUS, MULTIPLY, DIVIDE, OPEN_BRACKET, CLOSE_BRACKET, MIN, MAX, COMMA, ABS, MEDIAN, AVERAGE, MODE, SQRT, SUM, RAND, NONE, POWER, EXPONENT, LOG, HYPERBOLA;

	/**
	 * Get proper VendorType from String
	 *
	 * @param value
	 * @return
	 */
	public static VariableOperation of(String value) {
		VariableOperation result = NONE;

		VariableOperation tmpValue = VariableOperation.valueOf(value);
		if (tmpValue != null) {
			result = tmpValue;
		}

		return result;
	}
}
