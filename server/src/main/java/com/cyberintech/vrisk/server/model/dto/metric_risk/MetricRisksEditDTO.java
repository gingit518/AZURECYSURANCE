package com.cyberintech.vrisk.server.model.dto.metric_risk;

import com.cyberintech.vrisk.server.model.jpa.entity.MetricRisks;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metric Risk Domain Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-18
 */
@Setter
@Getter
@NoArgsConstructor
public class MetricRisksEditDTO extends MetricRisksViewDTO {

	@Schema
	private Long riskModelId;

	@Schema(hidden = true)
	private Long metricDomainId;

	@Schema
	private String metricDomainCode;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public MetricRisksEditDTO(MetricRisks entity) {
		super(entity);
	}

}
