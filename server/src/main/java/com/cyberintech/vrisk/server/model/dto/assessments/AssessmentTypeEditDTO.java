package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Assessment Type Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-05
 */
@Setter
@Getter
@NoArgsConstructor
public class AssessmentTypeEditDTO extends AssessmentTypeViewDTO {

	@Schema
	private Long organizationId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AssessmentTypeEditDTO(AssessmentTypes entity) {
		super(entity);
	}

	@Override
	public void fromEntity(AssessmentTypes entity) {
		super.fromEntity(entity);
	}
}
