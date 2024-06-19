package com.cyberintech.vrisk.server.repository.results;

import com.cyberintech.vrisk.server.model.jpa.entity.MetricResultAnswers;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestionAnswers;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestions;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metric Result Answers Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-18
 */
@Getter
@Setter
@NoArgsConstructor
@Data
public class QualitativeQuestionAnswerResult {

	private MetricResultAnswers metricResultAnswers;

	private QualitativeQuestions question;

	private QualitativeQuestionAnswers answer;

	public QualitativeQuestionAnswerResult(MetricResultAnswers metricResultAnswers, QualitativeQuestions question, QualitativeQuestionAnswers answer) {
		this.metricResultAnswers = metricResultAnswers;
		this.question = question;
		this.answer = answer;
	}
}
