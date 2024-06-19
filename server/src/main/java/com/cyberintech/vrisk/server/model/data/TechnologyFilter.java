package com.cyberintech.vrisk.server.model.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Implementation of Technology Filtering Logic
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-03-13
 */
@NoArgsConstructor
@Setter
@Getter
public class TechnologyFilter extends NameFilter {

	private Long technologyCategoryId;
	private Long technologySubcategoryId;
	private Long technologyClassTypeId;

}
