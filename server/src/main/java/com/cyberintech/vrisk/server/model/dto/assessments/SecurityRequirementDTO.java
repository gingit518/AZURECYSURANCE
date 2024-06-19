package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentFrameworkLevel;
import com.cyberintech.vrisk.server.model.jpa.entity.SecurityRequirementLevels;
import com.cyberintech.vrisk.server.model.jpa.entity.SecurityRequirements;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Security Requirement Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-01-23
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class SecurityRequirementDTO extends DTOBase<SecurityRequirements> {

	@Schema
	private Long id;

	@Schema
	private SecurityControlFamilyDTO securityControlFamily;

	@Schema
	private SecurityControlNameDTO securityControlName;

	@Schema
	private AssessmentLevelRefDTO assessmentLevel;

	@Schema
	private String code;

	@Schema
	private String programArea;

	@Schema
	private String description;

	@Schema
	private String detailedControlTestingProcedure;

	@Schema
	private String riskStatementExamples;

//	@Schema
//	private AssessmentViewDTO assessment;

	private List<AssessmentFrameworkLevel> securityRequirementLevels;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SecurityRequirementDTO(SecurityRequirements entity) {
		super(entity);
	}

	@Override
	public void fromEntity(SecurityRequirements entity) {
		id = entity.getId();
		code = entity.getCode();
		programArea = entity.getProgramArea();
		description = entity.getDescription();
		detailedControlTestingProcedure = entity.getDetailedControlTestingProcedure();
		riskStatementExamples = entity.getRiskStatementExamples();

		securityRequirementLevels = Optional.ofNullable(entity.getSecurityRequirementLevels()).orElse(new HashSet<>()).stream().map(SecurityRequirementLevels::getAssessmentFrameworkLevel).collect(Collectors.toList());

		if (entity.getAssessmentLevel() != null) {
			assessmentLevel = new AssessmentLevelRefDTO(entity.getAssessmentLevel());
		}

		if (entity.getSecurityControlFamily() != null) {
			securityControlFamily = new SecurityControlFamilyDTO(entity.getSecurityControlFamily());
		}

		if (entity.getSecurityControlName() != null) {
			securityControlName = new SecurityControlNameDTO(entity.getSecurityControlName());
		}

//		if (entity.getAssessment() != null) {
//			assessment = new AssessmentViewDTO(entity.getAssessment());
//		}
	}
}
