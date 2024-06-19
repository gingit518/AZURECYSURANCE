package com.cyberintech.vrisk.server.model.dto.technology;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.DataFields;
import com.cyberintech.vrisk.server.model.jpa.entity.EnvironmentTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Environment Types Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-21
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class EnvironmentTypesDTO extends DTOBase<EnvironmentTypes> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public EnvironmentTypesDTO(EnvironmentTypes entity) {
		super(entity);
	}

	@Override
	public void fromEntity(EnvironmentTypes entity) {
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
	}
}
