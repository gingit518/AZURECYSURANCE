package com.cyberintech.vrisk.server.model.dto.business_unit;

import com.cyberintech.vrisk.server.model.dto.process.ProcessRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Business Unit Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
public class BusinessUnitEditDTO extends BusinessUnitViewExtDTO {

//	@Schema
//	private Long organizationId;

	@Schema
	private List<SystemRefDTO> ownedSystems;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public BusinessUnitEditDTO(BusinessUnits entity) {
		super(entity);
	}

	@Override
	public void fromEntity(BusinessUnits entity) {
		super.fromEntity(entity);

		ownedSystems = Optional.ofNullable(entity.getOwnedSystems()).orElse(new HashSet<>()).stream().map(SystemRefDTO::new).collect(Collectors.toList());
	}
}
