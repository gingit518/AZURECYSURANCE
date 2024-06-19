package com.cyberintech.vrisk.server.model.dto.qual_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.QualMetrics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Qualification Metrics Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-19
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class QualMetricsEditDTO extends DTOBase<QualMetrics> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Long riskModelId;

	@Schema
	private Long ordinal;

	@Schema
	private MetricDomainViewDTO metricDomain;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QualMetricsEditDTO(QualMetrics entity) {
		super(entity);
	}

}
