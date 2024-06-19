package com.cyberintech.vrisk.server.model.dto.control_subcategory;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentTypeRefDTO;
import com.cyberintech.vrisk.server.model.dto.control_category.ControlCategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.control_function.ControlFunctionRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlSubcategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Control Subcategory Mapping View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-01-28
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class ControlSubcategoryMappingViewDTO extends DTOBase<ControlSubcategories> {

	@Schema
	private AssessmentTypeRefDTO assessmentType;

	@Schema
	private ControlFunctionRefDTO controlFunction;

	@Schema
	private ControlCategoryRefDTO controlCategory;

	@Schema
	private ControlSubcategoryRefDTO controlSubcategory;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ControlSubcategoryMappingViewDTO(ControlSubcategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ControlSubcategories entity) {

		controlSubcategory = new ControlSubcategoryRefDTO(entity);

		// Check Assessment Type
		if (entity.getAssessmentType() != null) {
			assessmentType = new AssessmentTypeRefDTO(entity.getAssessmentType());
		}

		if (entity.getControlCategory() != null) {
			controlCategory = new ControlCategoryRefDTO(entity.getControlCategory());

			if (entity.getControlCategory().getControlFunction() != null) {
				controlFunction = new ControlFunctionRefDTO(entity.getControlCategory().getControlFunction());
			}
		}

	}
}
