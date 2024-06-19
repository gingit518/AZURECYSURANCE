package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

/**
 * Predefined Data Type Domains
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-04-09
 */
@Getter
@ToString(of = {"id", "name"})
public enum DataTypeDomain {

	/**
	 * All EU Citizen data
	 */
	PRIVACY(101L, "Privacy"),

	/**
	 * All personal identifiable Information
	 */
	PII(102L, "PII"),

	/**
	 * All patient and medical data
	 */
	HEALTHCARE(103L, "Healthcare"),

	/**
	 * Intellectual Property
	 */
	INTELLECTUAL_PROPERTY(104L, "IP"),

	/**
	 * Customer information
	 */
	CUSTOMER_RELATED(105L, "Customer Related"),

	/**
	 * Partner information
	 */
	PARTNER_RELATED(106L, "Partner Related"),

	/**
	 * Credit card data including card number, card pin, card security code
	 */
	CREDIT_CARD(107L, "Credit Card"),

	/**
	 * Any data provided to or received from the federal government
	 */
	FEDERAL(108L, "Federal"),

	/**
	 * Patent data
	 */
	PATENT(109L, "Patent"),

	/**
	 * Any type of formula data
	 */
	FORMULA(110L, "Formula"),

	/**
	 * Any 3rd party data
	 */
	VENDOR(111L, "Vendor"),

	/**
	 * Any business data
	 */
	BUSINESS_DATA(112L, "Business Data"),

	/**
	 * Any financial data
	 */
	FINANCIAL(113L, "Financial"),

	/**
	 * Any employee related data
	 */
	EMPLOYEE(114L, "Employee");

	private final Long id;

	private final String name;

	private DataTypeDomain(Long domainId, String domainName) {
		this.id = domainId;
		this.name = domainName;
	}

	/**
	 * Returns Qual By ID
	 *
	 * @param id
	 * @return
	 */
	public static DataTypeDomain of(Long id) {
		DataTypeDomain result = null;

		if (id != null) {
			switch (id.intValue()) {
				case 101:
					return PRIVACY;
				case 102:
					return PII;
				case 103:
					return HEALTHCARE;
				case 104:
					return INTELLECTUAL_PROPERTY;
				case 105:
					return CUSTOMER_RELATED;
				case 106:
					return PARTNER_RELATED;
				case 107:
					return CREDIT_CARD;
				case 108:
					return FEDERAL;
				case 109:
					return PATENT;
				case 110:
					return FORMULA;
				case 111:
					return VENDOR;
				case 112:
					return BUSINESS_DATA;
				case 113:
					return FINANCIAL;
				case 114:
					return EMPLOYEE;
			}
		}

		return result;
	}

}
