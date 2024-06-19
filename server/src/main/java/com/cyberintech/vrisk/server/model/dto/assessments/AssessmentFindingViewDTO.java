package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.control_category.ControlCategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.control_function.ControlFunctionRefDTO;
import com.cyberintech.vrisk.server.model.dto.control_subcategory.ControlSubcategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentFindingLink;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentFindings;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Assessment View Entity Definition
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
public class AssessmentFindingViewDTO extends DTOWithMetaData<AssessmentFindings> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private Double percentage;

	@Schema
	private Double value;

	@Schema
	private TechnologyCategoryRefDTO technologyCategory;

	@Schema
	private ControlSubcategoryRefDTO controlSubcategory;

	@Schema
	private ControlCategoryRefDTO controlCategory;

	@Schema
	private ControlFunctionRefDTO controlFunction;

	@Schema
	private AssessmentFindingLink linkType;

	@Schema
	private List<AssessmentViewDTO> assessments;

	@Schema
	private List<SecurityRequirementDTO> securityRequirements;

	@Schema
	private TechnologyRefDTO technology;

	@Schema
	private List<TaskViewDTO> tasks;

	@Schema
	private ControlMaturityViewDTO controlMaturity;

	@Schema
	private Boolean isGDPR;

	@Schema
	private Long subjectiveRiskLevel;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public AssessmentFindingViewDTO(AssessmentFindings entity) {
		super(entity);
	}

	@Override
	public void fromEntity(AssessmentFindings entity) {
		this.id = entity.getId();
		this.name = entity.getName();
		this.percentage = entity.getPercentage();
		this.value = entity.getValue();
		this.linkType = entity.getLinkType();
		this.isGDPR = entity.getIsGDPR();
		this.subjectiveRiskLevel = entity.getSubjectiveRiskLevel();

		if (entity.getTechnologyCategory() != null) {
			technologyCategory = new TechnologyCategoryRefDTO(entity.getTechnologyCategory());
		}

		if (entity.getTechnology() != null) {
			technology = new TechnologyRefDTO(entity.getTechnology());
		}

		if (entity.getControlMaturity() != null) {
			controlMaturity = new ControlMaturityViewDTO(entity.getControlMaturity());
		}

		if (entity.getControlSubcategory() != null) {
			controlSubcategory = new ControlSubcategoryRefDTO(entity.getControlSubcategory());

			if (entity.getControlSubcategory().getControlCategory() != null) {
				controlCategory = new ControlCategoryRefDTO(entity.getControlSubcategory().getControlCategory());

				if (entity.getControlSubcategory().getControlCategory().getControlFunction() != null) {
					controlFunction = new ControlFunctionRefDTO(entity.getControlSubcategory().getControlCategory().getControlFunction());
				}
			}
		}

		assessments = Optional.ofNullable(entity.getAssessments()).orElse(new HashSet<>()).stream()
			.map(AssessmentViewDTO::new).collect(Collectors.toList());

		securityRequirements = Optional.ofNullable(entity.getSecurityRequirements()).orElse(new HashSet<>()).stream()
			.map(SecurityRequirementDTO::new).collect(Collectors.toList());

		tasks = Optional.ofNullable(entity.getTasks()).orElse(new HashSet<>()).stream()
			.map(TaskViewDTO::new).collect(Collectors.toList());

	}
}
