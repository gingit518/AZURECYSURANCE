package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Qualification Metrics Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-19
 */
@Entity
@Table(name = "qual_metrics")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"})
public class QualMetrics implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name", unique = true, nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "risk_model_id")
	private Long riskModelId;

	@Column(name = "ordinal")
	private Long ordinal;

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "metric_domain_id", updatable = false, insertable = false)
	private MetricDomains metricDomain;

	@Column(name = "metric_domain_id")
	private Long metricDomainId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by_id")
	private Users createdBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "updated_by_id")
	private Users updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

}
