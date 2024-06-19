package com.cyberintech.vrisk.server.model.dto.policy;


import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityRequirementDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.PolicyStatements;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Policy Statement View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.0
 * @since    2022-10-13
 */
@SuppressWarnings("serial")
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "statement"})
@EqualsAndHashCode(of = {"id", "statement"}, callSuper = false)
public class PolicyStatementViewDTO extends DTOBase<PolicyStatements> {

	@Schema
	private Long id;

	@Schema
	private String statement;

	@Schema
	private List<SecurityRequirementDTO> securityRequirements;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public PolicyStatementViewDTO(PolicyStatements entity) {
		super(entity);
	}

	@Override
	public void fromEntity(PolicyStatements entity) {
	//	super.fromEntity(entity);

		id = entity.getId();
		statement = entity.getStatement();

		securityRequirements = Optional.ofNullable(entity.getSecurityRequirements()).orElse(new HashSet<>()).stream().map(SecurityRequirementDTO::new).collect(Collectors.toList());
	}
}
