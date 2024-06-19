package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Assessment Type View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-05
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class AssessmentTypeViewDTO extends DTOBase<AssessmentTypes> {

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
	public AssessmentTypeViewDTO(AssessmentTypes entity) {
		super(entity);
	}

	@Override
	public void fromEntity(AssessmentTypes entity) {
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
	}
}
