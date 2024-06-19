package com.cyberintech.vrisk.server.model.dto.technology_categories;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyClassTypes;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologySubcategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Technology Class Type View Entity Definition
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
public class TechnologyClassTypeRefDTO extends DTOBase<TechnologyClassTypes> {

	@Schema
	private Long id;

	@Schema
	private String name;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TechnologyClassTypeRefDTO(TechnologyClassTypes entity) {
		super(entity);
	}

	@Override
	public void fromEntity(TechnologyClassTypes entity) {
		id = entity.getId();
		name = entity.getName();
	}

}
