package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Assessments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Assessment Reference Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-05
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class AssessmentRefDTO extends DTOBase<Assessments> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AssessmentRefDTO(Assessments entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Assessments entity) {
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
	}
}
