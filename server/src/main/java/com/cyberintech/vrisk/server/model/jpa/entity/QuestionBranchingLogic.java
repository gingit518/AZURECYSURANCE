package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Branching Logic for Qualification Question Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-07-26
 */
@Entity
@Table(name = "question_branching_logic")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "question", "answer"})
@EqualsAndHashCode(of = {"id"})
public class QuestionBranchingLogic {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "question_id")
	private QualitativeQuestions question;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "answer_id")
	private QualitativeQuestionAnswers answer;

	@Column(name = "ordinal")
	private Long ordinal;

	@Column(name = "operation")
	private Long operation;

}
