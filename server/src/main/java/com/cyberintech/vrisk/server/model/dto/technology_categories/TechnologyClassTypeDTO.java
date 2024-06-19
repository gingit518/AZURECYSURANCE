package com.cyberintech.vrisk.server.model.dto.technology_categories;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyClassTypes;
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
public class TechnologyClassTypeDTO extends DTOBase<TechnologyClassTypes> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private TechnologyCategoryRefDTO technologyCategory;

	@Schema
	private TechnologySubcategoryDTO technologySubcategory;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologyClassTypeDTO(TechnologyClassTypes entity) {
		super(entity);
	}

	@Override
	public void fromEntity(TechnologyClassTypes entity) {
		id = entity.getId();
		name = entity.getName();

		if (entity.getCategory() != null) technologyCategory = new TechnologyCategoryRefDTO(entity.getCategory());
		if (entity.getSubcategory() != null) technologySubcategory = new TechnologySubcategoryDTO(entity.getSubcategory());
	}

}
