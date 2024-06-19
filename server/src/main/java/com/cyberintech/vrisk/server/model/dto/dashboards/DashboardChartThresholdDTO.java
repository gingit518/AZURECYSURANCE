package com.cyberintech.vrisk.server.model.dto.dashboards;

import lombok.Getter;
import lombok.Setter;

/**
 * Dashboard Chart Threshold Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-03-19
 */
@Setter
@Getter
public class DashboardChartThresholdDTO {

	private Double x;
	private Double y;
	private String label;

	public DashboardChartThresholdDTO() {
	}

	/**
	 * Static class constructor
	 *
	 * @param x
	 * @param y
	 * @param label
	 * @return
	 */
	public static DashboardChartThresholdDTO of(Double x, Double y, String label) {
		DashboardChartThresholdDTO result = new DashboardChartThresholdDTO();
		result.setX(x);
		result.setY(y);
		result.setLabel(label);

		return result;
	}

	/**
	 * Static class constructor
	 *
	 * @param y
	 * @param label
	 * @return
	 */
	public static DashboardChartThresholdDTO of(Double y, String label) {
		return of(null, y, label);
	}
}
