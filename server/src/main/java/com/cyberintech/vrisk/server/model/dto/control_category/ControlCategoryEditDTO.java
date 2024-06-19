package com.cyberintech.vrisk.server.model.dto.control_category;

import com.cyberintech.vrisk.server.model.jpa.entity.ControlCategories;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Control Category Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-29
 */
@Setter
@Getter
@NoArgsConstructor
public class ControlCategoryEditDTO extends ControlCategoryViewDTO{

//	@Schema
//	private Long organizationId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ControlCategoryEditDTO(ControlCategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ControlCategories entity) {
		super.fromEntity(entity);
	}
}
