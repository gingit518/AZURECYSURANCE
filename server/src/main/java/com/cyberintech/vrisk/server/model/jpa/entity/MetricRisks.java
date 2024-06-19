package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Metric Risks Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-18
 */
@Entity
@Table(name = "metric_risks")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "createdAt"})
@EqualsAndHashCode(of = {"id", "createdAt"})
public class MetricRisks implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "risk_model_id")
	private Long riskModelId;

	@Column(name = "metric_domain_id")
	private Long metricDomainId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "metric_domain_id", insertable = false, updatable = false)
	private MetricDomains metricDomain;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id")
	private QualitativeQuestions question;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_weight_id")
	private QuestionWeights questionWeight;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "risk_type_id")
	private RiskTypes riskType;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "vendor_id")
	private Organizations vendor;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "created_by_id")
	private Users createdBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "updated_by_id")
	private Users updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

}
