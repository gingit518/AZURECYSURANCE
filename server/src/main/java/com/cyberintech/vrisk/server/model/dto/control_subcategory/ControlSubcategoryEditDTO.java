package com.cyberintech.vrisk.server.model.dto.control_subcategory;

import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentWeightViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.ControlSubcategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Control Subcategory Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-29
 */
@Setter
@Getter
@NoArgsConstructor
public class ControlSubcategoryEditDTO extends ControlSubcategoryViewDTO {

//	@Schema
//	private Long organizationId;

	@Schema
	private List<AssessmentWeightViewDTO> assessmentWeights;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ControlSubcategoryEditDTO(ControlSubcategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ControlSubcategories entity) {
		super.fromEntity(entity);

		assessmentWeights = Optional.ofNullable(entity.getAssessmentWeights()).orElse(new HashSet<>()).stream().map(AssessmentWeightViewDTO::new).collect(Collectors.toList());
	}
}
