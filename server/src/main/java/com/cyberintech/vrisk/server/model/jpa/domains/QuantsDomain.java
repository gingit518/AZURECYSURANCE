package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

/**
 * Predefined Quant Metric Domains
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-08
 */
@Getter
@ToString(of = {"id", "name"})
public enum QuantsDomain {

	/**
	 * The unauthorized copying, transfer or retrieval of data from a computer or server.
	 */
	DATA_EXFILTRATION(101L, "Data Exfiltration"),

	/**
	 * When a process that generates revenue is interrupted.  Usually due to a denial of service (DOS) attack.
	 */
	BUSINESS_INTERRUPTION(102L, "Business Interruption"),

	/**
	 * Penalties levied against organizations for noncompliance with data security measures.
	 */
	REGULATORY_LOSS(103L, "Regulatory Loss"),

	/**
	 * Cyber exposures due to 3rd parties
	 */
	VENDOR_EXPOSURE(104L, "Vendor Exposure"),

	/**
	 * Amount of insurance needed to transfer risk against a cybergeddon scenario
	 */
	CYBER_INSURANCE_NEEDS(105L, "Cyber Insurance Needs"),

	/**
	 * Cyber exposures due to 3rd parties
	 */
	MA_EXPOSURE(106L, "M&A Exposure"),

	/**
	 * Pricing for cyber insurance based on digital asset values
	 */
	ACTUARIAL_ANALYSIS(107L, "Actuarial Analysis"),

	/**
	 * Insurance discounts based on security assessments
	 */
	CYBER_STEWARD_DISCOUNT(108L, "Cyber Steward Discount"),

	/**
	 * Portfolio of risk exposures based on scenario analysis
	 */
	RISK_ACCUMULATION_EXPOSURE(109L, "Risk Accumulation Exposure"),

	/**
	 * Ransomware is a type of malware from cryptovirology that threatens to publish the victim''s data or perpetually block access to it unless a ransom is paid.
	 */
	RANSOMWARE(111L, "Ransomware"),

	/**
	 * Summary of all exposure types
	 */
	TOTAL_EXPOSURE(110L, "Total Exposure"),

	/**
	 * Penalties levied against organizations for noncompliance with data security measures.
	 */
	GDPR_REGULATORY_EXPOSURE(112L, "GDPR Regulatory Sublimit"),

	/**
	 *
	 */
	BUSINESS_INTERRUPTION_SUBLIMIT(113L, "Business Interruption Sublimit"),

	/**
	 *
	 */
	RANSOMWARE_SUBLIMIT(114L, "Ransomware Sublimit"),

	/**
	 * Privacy Exposure Model
	 */
	PRIVACY_EXPOSURE(115L, "Privacy Exposure");

	private final Long id;

	private final String name;

	private QuantsDomain(Long domainId, String domainName) {
		this.id = domainId;
		this.name = domainName;
	}

	/**
	 * Returns Qual By ID
	 *
	 * @param id
	 * @return
	 */
	public static QuantsDomain of(Long id) {
		QuantsDomain result = null;

		if (id != null) {
			switch (id.intValue()) {
				case 101:
					return DATA_EXFILTRATION;
				case 102:
					return BUSINESS_INTERRUPTION;
				case 103:
					return REGULATORY_LOSS;
				case 104:
					return VENDOR_EXPOSURE;
				case 105:
					return CYBER_INSURANCE_NEEDS;
				case 106:
					return MA_EXPOSURE;
				case 107:
					return ACTUARIAL_ANALYSIS;
				case 108:
					return CYBER_STEWARD_DISCOUNT;
				case 109:
					return RISK_ACCUMULATION_EXPOSURE;
				case 110:
					return TOTAL_EXPOSURE;
				case 111:
					return RANSOMWARE;
				case 112:
					return GDPR_REGULATORY_EXPOSURE;
				case 113:
					return BUSINESS_INTERRUPTION_SUBLIMIT;
				case 114:
					return RANSOMWARE_SUBLIMIT;
				case 115:
					return PRIVACY_EXPOSURE;
			}
		}

		return result;
	}

}
