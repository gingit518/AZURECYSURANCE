package com.cyberintech.vrisk.server.model.dto.systems;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Systems View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-15
 */
@SuppressWarnings("serial")
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class SystemDataExfiltrationDTO extends DTOWithMetaData<Systems> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Double numberOfRecProcessed;

	@Schema
	private Double rto;

	@Schema
	private Double rpo;

	@Schema
	private List<SystemGeoParametersDTO> geoRecordsProcessed;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SystemDataExfiltrationDTO(Systems entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Systems entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
		this.numberOfRecProcessed = entity.getNumberOfRecProcessed();
		this.rto = entity.getRto();
		this.rpo = entity.getRpo();
		geoRecordsProcessed = Optional.ofNullable(entity.getSystemGeoParameters()).orElse(new HashSet<>()).stream().map(SystemGeoParametersDTO::new).collect(Collectors.toList());
	}
}
