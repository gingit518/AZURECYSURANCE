package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.SecurityRequirements;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemRequirementControlTestResults;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * System Requirement Control Test Result DTO Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020.01.29
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "securityRequirement", "system", "assessmentWeight"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class SystemRequirementControlTestResultDTO extends DTOBase<SystemRequirementControlTestResults> {

	@Schema
	private Long id;

	@Schema
	private SecurityRequirementDTO securityRequirement;

	@Schema
	private String comments;

	@Schema
	private Boolean evidenceEligible;

	@Schema
	private Double assessmentWeight;

	@Schema
	private SystemRefDTO system;

	@Schema
	private List<TaskViewDTO> tasks;

	@Schema
	private DocumentDTO document;

	@Schema
	private ControlMaturityViewDTO controlMaturity;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SystemRequirementControlTestResultDTO(SystemRequirementControlTestResults entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 * @param requirement
	 */
	public SystemRequirementControlTestResultDTO(SystemRequirementControlTestResults entity, SecurityRequirements requirement) {
		super(entity);

		if (entity == null) {
			if (requirement != null) {
				this.securityRequirement = new SecurityRequirementDTO(requirement);
			}
		}
	}

	@Override
	public void fromEntity(SystemRequirementControlTestResults entity) {
		id = entity.getId();
		comments = entity.getComments();
		evidenceEligible = entity.getEvidenceEligible();
		assessmentWeight = entity.getAssessmentWeight();

		if (entity.getSecurityRequirement() != null) {
			securityRequirement = new SecurityRequirementDTO(entity.getSecurityRequirement());
		}

		if (entity.getSystem() != null) {
			system = new SystemRefDTO(entity.getSystem());
		}

		if (entity.getDocument() != null) {
			document = new DocumentDTO(entity.getDocument());
		}

		if (entity.getControlMaturity() != null) {
			controlMaturity = new ControlMaturityViewDTO(entity.getControlMaturity());
		}

		tasks = Optional.ofNullable(entity.getTasks()).orElse(new HashSet<>()).stream().map(TaskViewDTO::new).collect(Collectors.toList());
	}
}
