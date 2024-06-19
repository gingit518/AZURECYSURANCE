package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Predefined Metric Domains
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-10
 */
@Getter
@ToString(of = {"code"})
public enum MetricDomain {

	/**
	 * Probability an attack will cause damage.
	 */
	LIKELIHOOD(1l, "LIKELIHOOD"),

	/**
	 * The extent an attack will cause damage.
	 */
	IMPACT(2l, "IMPACT"),

	/**
	 * The increase in risk based on reputational factors.
	 */
	AMPLIFIED_REPUTATION(3l, "AMPLIFIED_REPUTATION"),

	/**
	 * The increase in risk based on operational factors.
	 */
	AMPLIFIED_OPERATIONAL(4l, "AMPLIFIED_OPERATIONAL"),

	/**
	 * The increase in risk based on legal factors.
	 */
	AMPLIFIED_LEGAL(5l, "AMPLIFIED_LEGAL"),

	/**
	 * TODO: 17.02.20  provide description
	 */
	CONFIDENTIALITY(6l, "CONFIDENTIALITY")


	/**
	 * TODO: 17.02.20  provide description
	 */
	, INTEGRITY(7l, "INTEGRITY")

	/**
	 * Cybersecurity Maturity
	 */
	, CYBERSECURITY_MATURITY(8l,"CYBERSECURITY_MATURITY")

	/**
	 * FFIEC CAT Inherent Risk
	 */
	, FFIEC_CAT_INHERENT_RISK(9l,"FFIEC_CAT_INHERENT_RISK")

	/**
	 * FFIEC CAT Cyber Org Maturity Level
	 */
	, FFIEC_CAT_CYBER_ORG_MATURITY(10l,"FFIEC_CAT_CYBER_ORG_MATURITY")

	;

	/**
	 * Define map of all the items in this Enum
	 */
	public static Map<Long, MetricDomain> ALL_ITEMS_MAP = Arrays.stream(MetricDomain.values()).collect(Collectors.toMap(MetricDomain::getId, itemType -> itemType));

	private final Long id;

	private final String code;

	private MetricDomain(Long domainId, String domainCode) {
		this.id = domainId;
		this.code = domainCode;
	}

	/**
	 * Returns Metric Domain By ID
	 *
	 * @param id
	 * @return
	 */
	public static MetricDomain of(Long id) {
		MetricDomain result = null;

		if (id != null) {
			result = ALL_ITEMS_MAP.get(id);
		}

		return result;
	}

}
