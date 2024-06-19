package com.cyberintech.vrisk.server.model.dto.qual_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.jpa.entity.QualMetrics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Qualification Metrics View Entity Definition
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
public class QualMetricsRefDTO extends DTOBase<QualMetrics> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private MetricDomainViewDTO metricDomain;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QualMetricsRefDTO(QualMetrics entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QualMetrics entity) {
		super.fromEntity(entity);

		if (entity.getMetricDomain() != null) {
			metricDomain = new MetricDomainViewDTO(entity.getMetricDomain());
		}
	}
}
