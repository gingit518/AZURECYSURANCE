package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.control_category.ControlCategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.control_function.ControlFunctionRefDTO;
import com.cyberintech.vrisk.server.model.dto.control_subcategory.ControlSubcategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlTests;
import com.cyberintech.vrisk.server.repository.results.ControlTestResult;
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
public class ControlTestViewDTO extends DTOWithMetaData<ControlTests> {

	@Schema
	private Long id;

	@Schema
	private AssessmentTypeRefDTO assessmentType;

	@Schema
	private ControlFunctionRefDTO controlFunction;

	@Schema
	private ControlCategoryRefDTO controlCategory;

	@Schema
	private ControlSubcategoryRefDTO controlSubcategory;

	@Schema
	private AssessmentWeightRefDTO assessmentWeight;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ControlTestViewDTO(ControlTests entity) {
		super(entity);
	}

	public ControlTestViewDTO(ControlTestResult controlTestResult) {
		if (controlTestResult.getControlTest() != null) {
			fromEntity(controlTestResult.getControlTest());
		} else {
			if (controlTestResult.getControlSubcategory() != null) {
				controlSubcategory = new ControlSubcategoryRefDTO(controlTestResult.getControlSubcategory());
				controlCategory = new ControlCategoryRefDTO(controlTestResult.getControlSubcategory().getControlCategory());
				controlFunction = new ControlFunctionRefDTO(controlTestResult.getControlSubcategory().getControlCategory().getControlFunction());
			}

			if (controlTestResult.getAssessmentWeight() != null) {
				assessmentWeight = new AssessmentWeightRefDTO(controlTestResult.getAssessmentWeight());
			}
		}
	}

	@Override
	public void fromEntity(ControlTests entity) {
		this.setId(entity.getId());

		if (entity.getAssessmentType() != null) {
			assessmentType = new AssessmentTypeRefDTO(entity.getAssessmentType());
		}

		if (entity.getControlFunction() != null) {
			controlFunction = new ControlFunctionRefDTO(entity.getControlFunction());
		}

		if (entity.getControlCategory() != null) {
			controlCategory = new ControlCategoryRefDTO(entity.getControlCategory());
		}

		if (entity.getControlSubcategory() != null) {
			controlSubcategory = new ControlSubcategoryRefDTO(entity.getControlSubcategory());
		}

		if (entity.getAssessmentWeight() != null) {
			assessmentWeight = new AssessmentWeightRefDTO(entity.getAssessmentWeight());
		}
	}
}
