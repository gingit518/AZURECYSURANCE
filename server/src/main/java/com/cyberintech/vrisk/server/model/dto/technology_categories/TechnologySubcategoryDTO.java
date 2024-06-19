package com.cyberintech.vrisk.server.model.dto.technology_categories;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologySubcategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Technology Subcategory View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-01-03
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class TechnologySubcategoryDTO extends DTOBase<TechnologySubcategories> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private TechnologyCategoryRefDTO technologyCategory;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologySubcategoryDTO(TechnologySubcategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(TechnologySubcategories entity) {
		id = entity.getId();
		name = entity.getName();
	}

}
