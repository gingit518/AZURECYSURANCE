package com.cyberintech.vrisk.server.model.dto.policy;

import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentTypeViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityRequirementDTO;
import com.cyberintech.vrisk.server.model.dto.budget.CyberRoleDTO;
import com.cyberintech.vrisk.server.model.dto.budget.CybersecurityToolDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleItemDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Policies;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.Policy;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Policy Edit Entity Definition
 *
 * @author	 Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.1
 * @since 	 2020-01-09
 */
@Setter
@Getter
@NoArgsConstructor
public class PolicyEditDTO extends PolicyViewDTO {

	@Schema
	private List<PolicyStatementViewDTO> statements;

	@Schema
	private List<CyberRoleDTO> rolesAndResponsibilities;


	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public PolicyEditDTO(Policies entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Policies entity) {
		super.fromEntity(entity);

		statements = Optional.ofNullable(entity.getStatements()).orElse(new HashSet<>()).stream().map(PolicyStatementViewDTO::new).collect(Collectors.toList());
		rolesAndResponsibilities = Optional.ofNullable(entity.getRolesAndResponsibilities()).orElse(new HashSet<>()).stream().map(CyberRoleDTO::new).collect(Collectors.toList());
	}
}
