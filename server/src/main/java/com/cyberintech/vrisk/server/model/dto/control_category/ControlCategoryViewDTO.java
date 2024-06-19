package com.cyberintech.vrisk.server.model.dto.control_category;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.control_function.ControlFunctionRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlCategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Control Category View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-29
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class ControlCategoryViewDTO extends DTOWithMetaData<ControlCategories> {

	@Schema
	private Long id;

	@Schema
	private String code;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private ControlFunctionRefDTO controlFunction;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ControlCategoryViewDTO(ControlCategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ControlCategories entity) {
		id = entity.getId();
		code = entity.getCode();
		name = entity.getName();
		description = entity.getDescription();

		if (entity.getControlFunction() != null) {
			controlFunction = new ControlFunctionRefDTO(entity.getControlFunction());
		}
	}
}
