package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentLevels;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Assessment Level Reference Entity Definition
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
public class AssessmentLevelRefDTO extends DTOBase<AssessmentLevels> {

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
	public AssessmentLevelRefDTO(AssessmentLevels entity) {
		super(entity);
	}

	@Override
	public void fromEntity(AssessmentLevels entity) {
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
	}
}
