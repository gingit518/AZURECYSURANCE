package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;

/**
 * Variable Type
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-31
 */
public enum VariableType {
	CONSTANT(1L)
	, PROCESS_REVENUE(2l)
	, SYSTEM_NUMBER_OF_REC(3L)
	, ORGANIZATION_REVENUE(4L)
	, LIKELIHOOD(5L)
	, IMPACT(6L)
	, AMPLIFIED_REPUTATION(7L)
	, AMPLIFIED_OPERATIONAL(8L)
	, AMPLIFIED_LEGAL(9L)
	, RISK_METRIC_CONSTANT(10L)
	, CONFIDENTIALITY(11L)
	, INTEGRITY(12L)
	, MARKET_CAPITALIZATION(13L)
	, RTO(14L)
	, RRTO(15L)
	, RISK_MODEL_CONSTANT(16L)
	, QUANT_METRIC(17L)
	, ORGANIZATION_RECORD_PRICE(18L)
	, MITIGATION_RISK(19L)
	, SYSTEM_COST_TO_RESTORE(23L)
	, EBIDTA(24L)
	, DEBT(25L)
	, PENSION_DEBT(26L)
	, GROSS_RISK_BEARING_CAPACITY(27L)
	, REVENUE(28L)
	;

	@Getter
	private final Long id;

	private VariableType(Long id) {
		this.id = id;
	}

	/**
	 * Get proper ValueSource from String
	 *
	 * @param value
	 * @return
	 */
	public static VariableType of(String value) {
		VariableType result = CONSTANT;

		VariableType tmpValue = VariableType.valueOf(value);
		if (tmpValue != null) {
			result = tmpValue;
		}

		return result;
	}
}
