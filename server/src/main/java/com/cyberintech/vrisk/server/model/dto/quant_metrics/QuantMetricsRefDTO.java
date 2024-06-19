package com.cyberintech.vrisk.server.model.dto.quant_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.jpa.domains.QuantMetricLevel;
import com.cyberintech.vrisk.server.model.jpa.entity.QuantMetrics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Quantification Metrics Ref Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-19
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class QuantMetricsRefDTO extends DTOBase<QuantMetrics> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private QuantsRefDTO quant;

	@Schema
	private QuantMetricLevel quantMetricLevel;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QuantMetricsRefDTO(QuantMetrics entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QuantMetrics entity) {
		// super.fromEntity(entity);

		this.id = entity.getId();
		this.name = entity.getName();
		this.quantMetricLevel = entity.getQuantMetricLevel();

		if (entity.getQuant() != null) {
			quant = new QuantsRefDTO(entity.getQuant());
		}
	}
}
