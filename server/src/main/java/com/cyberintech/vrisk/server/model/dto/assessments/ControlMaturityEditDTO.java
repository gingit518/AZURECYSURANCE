package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.jpa.entity.ControlMaturities;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Control Maturity View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-14
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class ControlMaturityEditDTO extends ControlMaturityViewDTO {

	@Schema
	private Long riskModelId;

    /**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ControlMaturityEditDTO(ControlMaturities entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ControlMaturities entity) {
		super.fromEntity(entity);
		this.riskModelId = entity.getRiskModelId();
	}
}
