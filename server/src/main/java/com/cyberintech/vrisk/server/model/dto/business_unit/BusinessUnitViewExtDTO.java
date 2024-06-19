package com.cyberintech.vrisk.server.model.dto.business_unit;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.process.ProcessRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Extended Business Unit View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-04
 */
@Setter
@Getter
@NoArgsConstructor
public class BusinessUnitViewExtDTO extends BusinessUnitViewDTO {

	@Schema
	private List<ProcessRefDTO> ownedProcesses;

	@Schema
	private List<ProcessRefDTO> usedProcesses;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public BusinessUnitViewExtDTO(BusinessUnits entity) {
		super(entity);
	}

	@Override
	public void fromEntity(BusinessUnits entity) {
		super.fromEntity(entity);

		ownedProcesses = Optional.ofNullable(entity.getOwnedProcesses()).orElse(new HashSet<>()).stream().map(ProcessRefDTO::new).collect(Collectors.toList());
		usedProcesses = Optional.ofNullable(entity.getUsedProcesses()).orElse(new HashSet<>()).stream().map(ProcessRefDTO::new).collect(Collectors.toList());
	}
}
