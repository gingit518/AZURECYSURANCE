package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Organization Email Template Type
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-03-09
 */
public enum OrganizationEmailTemplateType {
	VENDOR_EMPLOYEE_INVITATION
	;

	/**
	 * Get proper Organization Email Template Type
	 *
	 * @param value
	 * @return OrganizationEmailTemplateType
	 */
	public static OrganizationEmailTemplateType of(String value) {
		OrganizationEmailTemplateType result = VENDOR_EMPLOYEE_INVITATION;

		OrganizationEmailTemplateType tmpValue = OrganizationEmailTemplateType.valueOf(value);
		if (tmpValue != null) {
			result = tmpValue;
		}

		return result;
	}
}
