package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentWeights;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Assessment Weight Reference Entity Definition
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
public class AssessmentWeightViewDTO extends DTOBase<AssessmentWeights> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	private Long value;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AssessmentWeightViewDTO(AssessmentWeights entity) {
		super(entity);
	}

	@Override
	public void fromEntity(AssessmentWeights entity) {
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		value = entity.getValue();
	}
}
