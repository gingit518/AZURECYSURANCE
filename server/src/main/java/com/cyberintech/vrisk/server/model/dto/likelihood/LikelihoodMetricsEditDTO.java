package com.cyberintech.vrisk.server.model.dto.likelihood;

import com.cyberintech.vrisk.server.model.jpa.entity.LikelihoodMetrics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Risk Model Domain View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-08
 */
@Setter
@Getter
@NoArgsConstructor
public class LikelihoodMetricsEditDTO extends LikelihoodMetricsViewDTO {

	@Schema
	private Long riskModelId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public LikelihoodMetricsEditDTO(LikelihoodMetrics entity) {
		super(entity);
	}

	@Override
	public void fromEntity(LikelihoodMetrics entity) {
		super.fromEntity(entity);
	}

}
