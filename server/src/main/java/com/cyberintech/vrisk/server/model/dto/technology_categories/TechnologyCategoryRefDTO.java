package com.cyberintech.vrisk.server.model.dto.technology_categories;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Technology Category View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class TechnologyCategoryRefDTO extends DTOBase<TechnologyCategories> {

	@Schema
	private Long id;

	@Schema
	private String name;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologyCategoryRefDTO(TechnologyCategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(TechnologyCategories entity) {
		id = entity.getId();
		name = entity.getName();
	}

}
