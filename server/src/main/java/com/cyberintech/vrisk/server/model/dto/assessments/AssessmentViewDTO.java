package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.RelationToRequirementType;
import com.cyberintech.vrisk.server.model.jpa.entity.Assessments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Assessment View Entity Definition
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
public class AssessmentViewDTO extends DTOWithMetaData<Assessments> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private AssessmentLevelRefDTO assessmentLevel;

	@Schema
	private AssessmentTypeRefDTO assessmentType;

	@Schema
	private List<TechnologyCategoryRefDTO> technologyCategories;

	@Schema
	private List<SystemRefDTO> systems;

	@Schema
	private List<ProcessRefDTO> processes;

	@Schema
	private OrganizationRefDTO legalOrganization;

	@Schema
	private Boolean isAllSelected;

	@Schema
	private RelationToRequirementType relationToRequirementType;

	@Schema
	private List<SecurityRequirementDTO> securityRequirements;

	@Schema
	private List<AssessmentTypeRefDTO> assessmentTypes;

	@Schema
	private List<TaskViewDTO> tasks;

	private Date estimatedStartDate;

	private Date actualStartDate;

	private Date estimatedEndDate;

	private Date actualEndDate;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AssessmentViewDTO(Assessments entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Assessments entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		isAllSelected = entity.getIsAllSelected();
		relationToRequirementType = entity.getRelationToRequirementType();
		estimatedStartDate = entity.getEstimatedStartDate();
		estimatedEndDate = entity.getEstimatedEndDate();
		actualStartDate = entity.getActualStartDate();
		actualEndDate = entity.getActualEndDate();

		if (entity.getAssessmentLevel() != null) {
			assessmentLevel = new AssessmentLevelRefDTO(entity.getAssessmentLevel());
		}

		if (entity.getAssessmentType() != null) {
			assessmentType = new AssessmentTypeRefDTO(entity.getAssessmentType());
		}

		if (entity.getLegalOrganization() != null) {
			legalOrganization = new OrganizationRefDTO(entity.getLegalOrganization());
		}

		technologyCategories = Optional.ofNullable(entity.getTechnologyCategories()).orElse(new HashSet<>()).stream()
			.map(TechnologyCategoryRefDTO::new).collect(Collectors.toList());

		systems = Optional.ofNullable(entity.getSystems()).orElse(new HashSet<>()).stream()
			.map(SystemRefDTO::new).collect(Collectors.toList());

		processes = Optional.ofNullable(entity.getProcesses()).orElse(new HashSet<>()).stream()
			.map(ProcessRefDTO::new).collect(Collectors.toList());

		securityRequirements = Optional.ofNullable(entity.getSecurityRequirements()).orElse(new HashSet<>()).stream()
			.map(SecurityRequirementDTO::new).collect(Collectors.toList());

		assessmentTypes = Optional.ofNullable(entity.getAssessmentTypes()).orElse(new HashSet<>()).stream()
			.map(AssessmentTypeRefDTO::new).collect(Collectors.toList());

		tasks = Optional.ofNullable(entity.getTasks()).orElse(new HashSet<>()).stream()
			.map(TaskViewDTO::new).collect(Collectors.toList());
	}
}
