package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

/**
 * Predefined Asset Classes Domains
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.2.0
 * @since    2019-03-12
 */
@Getter
@ToString(of = {"id", "name"})
public enum AssetClass {

	/**
	 * Crown jewel assets refer to a company's most prized or valuable assets in terms of its profitability and future prospects.
	 * A failure of this type of system results in the company going out of business. Examples include:
	 * Safety-Critical Systems - A system whose failure may result in injury, loss of life or serious environmental damage.
	 * An example of a safety-critical system is a control system for a chemical manufacturing plant.
	 * Mission-Critical Systems - A system whose failure may result in the failure of some goal-directed activity.
	 * An example of a mission-critical system is a navigational system for a spacecraft.
	 * Transactional Systems - A system whose failure may result in the failure of some goal-directed activity.
	 * An example is a transactional system is one that processes privacy data when the company sells trust. (i.e. Equifax)
	 */
	CROWN_JEWEL(101L, "Crown Jewel"),

	/**
	 * A business-critical system is a system whose failure may result in very high costs for the business using that system.
	 * An example of a business-critical system is the customer accounting system in a bank.
	 */
	BUSINESS_CRITICAL(102L, "Business Critical"),

	/**
	 * A business crucial system is a system whose failure is not critical but has significant impact.
	 */
	BUSINESS_CRUCIAL(103L, "Business Crucial");

	private final Long id;

	private final String name;

	private AssetClass(Long domainId, String domainName) {
		this.id = domainId;
		this.name = domainName;
	}

	/**
	 * Returns Asset Class By ID
	 *
	 * @param id
	 * @return
	 */
	public static AssetClass of(Long id) {
		AssetClass result = null;

		if (id != null) {
			switch (id.intValue()) {
				case 101:
					return CROWN_JEWEL;
				case 102:
					return BUSINESS_CRITICAL;
				case 103:
					return BUSINESS_CRUCIAL;
			}
		}

		return result;
	}

}
