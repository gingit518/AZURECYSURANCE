package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentFindings;
import lombok.*;

/**
 * Assessment Findings Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-14
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class AssessmentFindingEditDTO extends AssessmentFindingViewDTO {

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AssessmentFindingEditDTO(AssessmentFindings entity) {
		super(entity);
	}

	@Override
	public void fromEntity(AssessmentFindings entity) {
		super.fromEntity(entity);
	}
}
