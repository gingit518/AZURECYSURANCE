package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.jpa.entity.Assessments;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Assessment Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-05
 */
@Setter
@Getter
@NoArgsConstructor
public class AssessmentEditDTO extends AssessmentViewDTO {

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AssessmentEditDTO(Assessments entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Assessments entity) {
		super.fromEntity(entity);
	}
}
