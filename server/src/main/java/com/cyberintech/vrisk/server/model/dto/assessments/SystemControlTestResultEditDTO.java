package com.cyberintech.vrisk.server.model.dto.assessments;


import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemControlTestResults;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemRequirementControlTestResults;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
public class SystemControlTestResultEditDTO extends SystemControlTestResultViewDTO {

	@Schema
	private List<SystemRequirementControlTestResultDTO> systemRequirementControlTestResults;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SystemControlTestResultEditDTO(SystemControlTestResults entity) {
		super(entity);
	}

	@Override
	public void fromEntity(SystemControlTestResults entity) {
		super.fromEntity(entity);

		systemRequirementControlTestResults = Optional.ofNullable(entity.getSystemRequirementControlTestResults()).orElse(new HashSet<>()).stream()
			.map(SystemRequirementControlTestResultDTO::new).collect(Collectors.toList());
	}
}
