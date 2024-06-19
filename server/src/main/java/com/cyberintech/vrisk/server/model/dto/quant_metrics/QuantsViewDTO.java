package com.cyberintech.vrisk.server.model.dto.quant_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.regulations.RegulationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Quants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Quantification Category View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-17
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class QuantsViewDTO extends DTOWithMetaData<Quants> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Long ordinal;

	@Schema
	private List<RegulationRefDTO> regulations;

	private Boolean isReadOnly;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QuantsViewDTO(Quants entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Quants entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
		this.ordinal = entity.getOrdinal();
		this.regulations = Optional.ofNullable(entity.getRegulations()).orElse(new HashSet<>()).stream().map(RegulationRefDTO::new).collect(Collectors.toList());

		this.isReadOnly = entity.getId() < 1024;
	}
}
