package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Likelihood Metrics Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-29
 */
@Entity
@Table(name = "likelihood_metrics")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "createdAt"})
@EqualsAndHashCode(of = {"id", "createdAt"})
public class LikelihoodMetrics implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "risk_model_id")
	private Long riskModelId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id")
	private QualitativeQuestions question;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_weight_id")
	private QuestionWeights questionWeight;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "risk_type_id")
	private RiskTypes riskType;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vendor_id")
	private Organizations vendor;

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
