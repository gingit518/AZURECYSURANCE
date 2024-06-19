package com.cyberintech.vrisk.server.model.dto.control_subcategory;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlSubcategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Control Subcategory Ref Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-08
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class ControlSubcategoryRefDTO extends DTOBase<ControlSubcategories> {

	@Schema
	private Long id;

	@Schema
	private String code;

	@Schema
	private String name;

	@Schema
	private String description;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ControlSubcategoryRefDTO(ControlSubcategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ControlSubcategories entity) {
		id = entity.getId();
		code = entity.getCode();
		name = entity.getName();
		description = entity.getDescription();
	}
}
