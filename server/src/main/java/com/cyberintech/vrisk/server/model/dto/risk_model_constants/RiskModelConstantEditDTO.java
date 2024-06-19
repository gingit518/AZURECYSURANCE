package com.cyberintech.vrisk.server.model.dto.risk_model_constants;

import com.cyberintech.vrisk.server.model.jpa.entity.RiskModelConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Risk Model Constant Edit Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-10-27
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"riskModelId"}, callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RiskModelConstantEditDTO extends RiskModelConstantViewDTO {

	@Schema
	private Long riskModelId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RiskModelConstantEditDTO(RiskModelConstants entity) {
		super(entity);
	}

	@Override
	public void fromEntity(RiskModelConstants entity) {
		super.fromEntity(entity);

		riskModelId = entity.getRiskModelId();
	}
}
