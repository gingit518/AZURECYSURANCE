package com.cyberintech.vrisk.server.model.jpa.domains;

import org.apache.commons.lang3.StringUtils;

/**
 * Question types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-29
 */
public enum VendorType {
	Organization,
	System,
	Vendor,
	Cloud,
	Both,
	GDPRSystem,
	GDPROrganization,
	VendorInternal,
	CloudInternal;

	/**
	 * Get proper VendorType from String
	 *
	 * @param questionTypeName
	 * @return
	 */
	public static VendorType of(String questionTypeName) {
		return of(questionTypeName, Both);
	}

	/**
	 * Get proper VendorType from String
	 *
	 * @param questionTypeName
	 * @return
	 */
	public static VendorType of(String questionTypeName, VendorType defaultVendorType) {
		VendorType result = defaultVendorType;

		if (StringUtils.isNotEmpty(questionTypeName)) {
			VendorType tmpVendorType = VendorType.valueOf(questionTypeName);
			if (tmpVendorType != null) {
				result = tmpVendorType;
			}
		}

		return result;
	}

	/**
	 * Scoring Question type label
	 *
	 * @param vendorType
	 * @return
	 */
	public static String buildLabel(VendorType vendorType) {
		String result = vendorType != null ? vendorType.name() : null;

		switch (vendorType) {
			case Vendor:
				result = "Vendor Level 1";
				break;
			case VendorInternal:
				result = "Vendor Level 2";
				break;
			case Cloud:
				result = "Cloud Level 1";
				break;
			case CloudInternal:
				result = "Cloud Level 2";
				break;
			case GDPRSystem:
				result = "GDPR System";
				break;
			case GDPROrganization:
				result = "GDPR Organization";
				break;
		}

		return result;
	}
}
