package com.cyberintech.vrisk.server.model.dto.control_subcategory;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.control_category.ControlCategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlSubcategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Control Subcategory View Entity Definition
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
public class ControlSubcategoryViewDTO extends DTOWithMetaData<ControlSubcategories> {

	@Schema
	private Long id;

	@Schema
	private String code;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private ControlCategoryRefDTO controlCategory;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ControlSubcategoryViewDTO(ControlSubcategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ControlSubcategories entity) {
		id = entity.getId();
		code = entity.getCode();
		name = entity.getName();
		description = entity.getDescription();

		if (entity.getControlCategory() != null) {
			controlCategory = new ControlCategoryRefDTO(entity.getControlCategory());
		}
	}
}
