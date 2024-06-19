package com.cyberintech.vrisk.server.model.dto.qual_metrics;

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
public class QualMetricsViewDTO extends DTOWithMetaData<QualMetrics> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Long ordinal;

	@Schema
	private MetricDomainViewDTO metricDomain;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QualMetricsViewDTO(QualMetrics entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QualMetrics entity) {
//		super.fromEntity(entity);
		// Loading item metadata
		loadMetadata(entity);

		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
		this.ordinal = entity.getOrdinal();

		if (entity.getMetricDomain() != null) {
			metricDomain = new MetricDomainViewDTO(entity.getMetricDomain());
		}
	}
}
