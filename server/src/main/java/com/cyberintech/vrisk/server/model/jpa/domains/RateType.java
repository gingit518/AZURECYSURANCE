package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

/**
 * Predefined Rate Types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-05-31
 */
@Getter
@ToString(of = {"id", "name"})
public enum RateType {

	SALARY(1L, "Salary"),
	HOURLY(2L, "Hourly"),
	DAILY(3L, "Daily"),
	WEEKLY(4L, "Weekly"),
	MONTHLY(5L, "Monthly"),
	ANNUALLY(6L, "Annually");

	private final Long id;

	private final String name;

	private RateType(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Returns Type Entity By ID
	 *
	 * @param id
	 * @return
	 */
	public static RateType of(Long id) {

		if (id != null) {
			switch (id.intValue()) {
				case 1:
					return SALARY;
				case 2:
					return HOURLY;
				case 3:
					return DAILY;
				case 4:
					return WEEKLY;
				case 5:
					return MONTHLY;
				case 6:
					return ANNUALLY;
			}
		}

		return null;
	}

}
