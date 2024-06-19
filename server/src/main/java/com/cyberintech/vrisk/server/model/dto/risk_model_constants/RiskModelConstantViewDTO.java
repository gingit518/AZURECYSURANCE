package com.cyberintech.vrisk.server.model.dto.risk_model_constants;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModelConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Risk Model Constant View Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-10-27
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name", "value"})
@EqualsAndHashCode(of = {"id"})
public class RiskModelConstantViewDTO extends DTOWithMetaData<RiskModelConstants> {

	@Schema
	private Long id;

	@Schema
	private Double value;

	@Schema
	private String name;

	@Schema
	private String description;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RiskModelConstantViewDTO(RiskModelConstants entity) {
		super(entity);
	}

	@Override
	public void fromEntity(RiskModelConstants entity) {
		setMetadataFromEntity(entity);

		id = entity.getId();
		value = entity.getValue();
		name = entity.getName();
		description = entity.getDescription();
	}

}
