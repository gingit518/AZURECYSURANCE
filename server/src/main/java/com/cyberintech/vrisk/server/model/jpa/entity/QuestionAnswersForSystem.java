package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Answer for Qualification Question Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-04-19
 */
@Entity
@Table(name = "question_answers_to_systems")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "system", "answer"})
@EqualsAndHashCode(of = {"id"})
public class QuestionAnswersForSystem {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id")
	private QualitativeQuestions question;

	@ManyToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.MERGE)
	@JoinColumn(name = "answer_id")
	private QualitativeQuestionAnswers answer;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "system_id")
	private Systems system;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "document_id")
	private Documents document;

	@Column(name = "answer_text")
	private String answerText;

	@Column(name = "answer_comment")
	private String answerComment;

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
