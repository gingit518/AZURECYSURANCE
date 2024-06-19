package com.cyberintech.vrisk.server.model.dto.technology_categories;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologySubcategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Technology Sub Category View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-11
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class TechnologySubcategoryRefDTO extends DTOBase<TechnologySubcategories> {

	@Schema
	private Long id;

	@Schema
	private String name;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologySubcategoryRefDTO(TechnologySubcategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(TechnologySubcategories entity) {
		id = entity.getId();
		name = entity.getName();
	}

}
