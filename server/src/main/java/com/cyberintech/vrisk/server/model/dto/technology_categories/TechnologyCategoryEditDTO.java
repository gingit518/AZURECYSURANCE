package com.cyberintech.vrisk.server.model.dto.technology_categories;

import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Technology Category Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
public class TechnologyCategoryEditDTO extends TechnologyCategoryViewDTO {

//	@Schema
//	private Long organizationId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologyCategoryEditDTO(TechnologyCategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(TechnologyCategories entity) {
		super.fromEntity(entity);
	}
}
