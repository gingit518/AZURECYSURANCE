package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

/**
 * Predefined Cost Types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-05-31
 */
@Getter
@ToString(of = {"id", "name"})
public enum CostType {

	VULNERABILITY(1L, "Vulnerability"),
	SECURITY_FINDING(2L, "Security Finding"),
	INCIDENT(3L, "Incident");

	private final Long id;

	private final String name;

	private CostType(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Returns Type Entity By ID
	 *
	 * @param id
	 * @return
	 */
	public static CostType of(Long id) {

		if (id != null) {
			switch (id.intValue()) {
				case 1:
					return VULNERABILITY;
				case 2:
					return SECURITY_FINDING;
				case 3:
					return INCIDENT;
			}
		}

		return null;
	}

}
