package com.cyberintech.vrisk.server.model.dto.assessments;


import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemControlTestResults;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * System Control Test Result DTO Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.01.31
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "system", "assessmentWeight"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class SystemControlTestResultViewDTO extends DTOBase<SystemControlTestResults> {

	@Schema
	private Long id;

	@Schema
	private SystemRefDTO system;

	@Schema
	private Double assessmentWeight;


	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SystemControlTestResultViewDTO(SystemControlTestResults entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 * @param system
	 */
	public SystemControlTestResultViewDTO(SystemControlTestResults entity, Systems system) {
		super(entity);

		if (entity == null) {
			if (system != null) {
				this.system = new SystemRefDTO(system);
			}
		}
	}

	@Override
	public void fromEntity(SystemControlTestResults entity) {
		id = entity.getId();
		assessmentWeight = entity.getAssessmentWeight();

		if (entity.getSystem() != null) {
			system = new SystemRefDTO(entity.getSystem());
		}
	}
}
