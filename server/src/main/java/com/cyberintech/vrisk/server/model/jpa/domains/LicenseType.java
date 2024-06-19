package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

/**
 * Predefined License Types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-05-31
 */
@Getter
@ToString(of = {"id", "name"})
public enum LicenseType {

	NUMBER_OF_USERS(1L, "Number of Users"),
	CPU(2L, "CPU"),
	ANNUAL(3L, "Annual");

	private final Long id;

	private final String name;

	private LicenseType(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Returns Type Entity By ID
	 *
	 * @param id
	 * @return
	 */
	public static LicenseType of(Long id) {

		if (id != null) {
			switch (id.intValue()) {
				case 1:
					return NUMBER_OF_USERS;
				case 2:
					return CPU;
				case 3:
					return ANNUAL;
			}
		}

		return null;
	}

}
