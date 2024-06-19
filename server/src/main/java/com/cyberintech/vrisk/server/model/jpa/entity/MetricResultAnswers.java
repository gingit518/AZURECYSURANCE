package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Metric Result Answers Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-23
 */
@Entity
@Table(name = "metric_result_answers")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class MetricResultAnswers implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "risk_model_id")
	private Long riskModelId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false)
	private QualitativeQuestions question;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "answer_id", nullable = false)
	private QualitativeQuestionAnswers answer;

	@Column(name = "multiplier")
	private Double multiplier;

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
