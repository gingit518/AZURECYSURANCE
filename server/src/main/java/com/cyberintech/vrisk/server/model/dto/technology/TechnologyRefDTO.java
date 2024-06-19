package com.cyberintech.vrisk.server.model.dto.technology;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Technologies;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Technologies Ref Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-22
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class TechnologyRefDTO extends DTOWithMetaData<Technologies> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private TechnologyCategoryRefDTO technologyCategory;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologyRefDTO(Technologies entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologyRefDTO(Technologies entity, Boolean loadCategory) {
		super(entity);

		// Load category
		if (Boolean.TRUE.equals(loadCategory)) {
			TechnologyCategories category = entity.getTechnologyCategory();
			if (category != null) {
				technologyCategory = new TechnologyCategoryRefDTO(category);
			}
		}
	}

	@Override
	public void fromEntity(Technologies entity) {
		// super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
	}

	/**
	 * Static constructor
	 *
	 * @param entity
	 * @return
	 */
	public static TechnologyRefDTO of(Technologies entity) {
		return new TechnologyRefDTO(entity, true);
	}
}
