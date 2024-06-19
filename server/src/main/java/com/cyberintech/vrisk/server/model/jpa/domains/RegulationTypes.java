package com.cyberintech.vrisk.server.model.jpa.domains;

import lombok.Getter;
import lombok.ToString;

/**
 * Regulation types
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-04-20
 */
@Getter
@ToString(of = {"id", "code"})
public enum RegulationTypes {

	/**
	 * The Health Insurance Portability and Accountability Act of 1996 is a United States federal statute signed into law on August 21, 1996. It was created primarily to modernize the flow of healthcare information, stipulate how personally identifiable information maintained by the healthcare and healthcare insurance industries should be protected from fraud and theft, and address limitations on healthcare insurance coverage.
	 */
	HIPAA(1L, "HIPAA")

	/**
	 * The California Consumer Privacy Act is a state statute intended to enhance privacy rights and consumer protection for residents of California
	 */
	, CCPA(9L, "CCPA")

	/**
	 * New York DFS regulations
	 */
	, NEW_YORK_DFS_500(12L, "NYS DFS Part 500")

	/**
	 * Insurance Data Security Act - Model Law
	 */
	, NIAC_MDL_668(15L, "NIAC MDL-668")

	;

	private final Long id;

	private final String name;

	/**
	 * Private constructor
	 *
	 * @param id
	 * @param code
	 */
	private RegulationTypes(Long id, String code) {
		this.id = id;
		this.name = code;
	}

	/**
	 * Returns Regulation By ID
	 *
	 * @param id
	 * @return
	 */
	public static RegulationTypes of(Long id) {
		RegulationTypes result = null;

		return result;
	}

}
