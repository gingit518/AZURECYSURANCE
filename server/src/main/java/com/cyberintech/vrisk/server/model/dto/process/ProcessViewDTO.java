package com.cyberintech.vrisk.server.model.dto.process;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Processes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Process View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class ProcessViewDTO extends DTOWithMetaData<Processes> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private UserRefDTO owner;

	@Schema
	private BusinessUnitRefDTO businessUnit;

	private Double revenueProcessed;

	private String notes;

	@Schema
	protected List<SystemRefDTO> systems;

	@Schema
	private List<BusinessUnitRefDTO> businessUnitUses;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ProcessViewDTO(Processes entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Processes entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
		this.revenueProcessed = entity.getRevenueProcessed();
		this.notes = entity.getNotes();

		if (entity.getOwner() != null) {
			owner = new UserRefDTO(entity.getOwner());
		}

		if (entity.getBusinessUnit() != null) {
			businessUnit = new BusinessUnitRefDTO(entity.getBusinessUnit());
		}

		systems = Optional.ofNullable(entity.getSystems()).orElse(new HashSet<>()).stream().map(SystemRefDTO::new).collect(Collectors.toList());
		businessUnitUses = Optional.ofNullable(entity.getBusinessUnitsUsed()).orElse(new HashSet<>()).stream().map(BusinessUnitRefDTO::new).collect(Collectors.toList());
	}
}
