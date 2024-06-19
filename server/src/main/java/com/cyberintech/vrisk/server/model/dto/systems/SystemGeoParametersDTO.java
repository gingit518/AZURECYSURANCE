package com.cyberintech.vrisk.server.model.dto.systems;

import com.cyberintech.vrisk.server.model.converters.CustomDoubleDeserializer;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.country.CountryViewDTO;
import com.cyberintech.vrisk.server.model.dto.state.StateViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.SystemGeoParameters;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Systems Geo Parameters Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-08-10
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "numberOfRecProcessed", "country", "state"})
@EqualsAndHashCode(of = {"id", "country", "state"}, callSuper = false)
public class SystemGeoParametersDTO extends DTOBase<SystemGeoParameters> {

	@Schema
	private Long id;

	@Schema
	private CountryViewDTO country;

	@Schema
	private StateViewDTO state;

	@Schema
	@JsonDeserialize(using = CustomDoubleDeserializer.class)
	private Double numberOfRecProcessed;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SystemGeoParametersDTO(SystemGeoParameters entity) {
		super(entity);
	}

	@Override
	public void fromEntity(SystemGeoParameters entity) {
		this.id = entity.getId();
		if (entity.getCountry() != null) this.country = new CountryViewDTO(entity.getCountry());
		if (entity.getState() != null) this.state = new StateViewDTO(entity.getState());
		this.numberOfRecProcessed = entity.getNumberOfRecProcessed();
	}
}
