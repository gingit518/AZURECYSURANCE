package com.cyberintech.vrisk.server.model.jpa.domains;

/**
 * Dashboard types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-05
 */
public enum DashboardType {
	Organization, Vendor, Admin, Dynamic, Drilldown, Analytics, Qlik, None;

	/**
	 * Get proper DashboardType from String
	 *
	 * @param dashboardTypeName
	 * @return
	 */
	public static DashboardType of(String dashboardTypeName) {
		DashboardType result = Vendor;

		DashboardType tmpVendorType = DashboardType.valueOf(dashboardTypeName);
		if (tmpVendorType != null) {
			result = tmpVendorType;
		}

		return result;
	}
}
