package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Answer for Qualification Question Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-29
 */
@Entity
@Table(name = "qualitative_question_answers")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "answer"})
@EqualsAndHashCode(of = {"id", "answer"})
public class QualitativeQuestionAnswers {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "question_id")
	private QualitativeQuestions qualitativeQuestion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "weight_id")
	private AnswerWeight answerWeight;

	@Column(name = "answer")
	private String answer;

	@Column(name = "ordinal")
	private Long ordinal;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

}
