package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.control_category.ControlCategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.control_function.ControlFunctionRefDTO;
import com.cyberintech.vrisk.server.model.dto.control_subcategory.ControlSubcategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlTests;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Control Tests View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-08
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class ControlTestEditDTO extends ControlTestViewDTO {

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ControlTestEditDTO(ControlTests entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ControlTests entity) {
		super.fromEntity(entity);
	}
}
