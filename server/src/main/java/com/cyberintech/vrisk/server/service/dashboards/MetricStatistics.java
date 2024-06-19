package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestionAnswers;
import com.cyberintech.vrisk.server.model.jpa.entity.QualitativeQuestions;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Scoring Metric Statistics
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-02-28
 */
@Getter
@Setter
public class MetricStatistics {

	private Double answersNumber;

	private Double questionsNumber;

	private Double maxAnswersWeight;

	private Double maxQuestionAnswersWeight;

	private List<QualitativeQuestions> qualitativeQuestionList;

	public MetricStatistics() {
		answersNumber = 0d;
		questionsNumber = 0d;
		maxAnswersWeight = 0d;
		maxQuestionAnswersWeight = 0d;
		qualitativeQuestionList = new ArrayList<>();
	}

	public static MetricStatistics of(List<QualitativeQuestions> qualitativeQuestionList) {
		MetricStatistics result = new MetricStatistics();

		// Return Empty object
		if (qualitativeQuestionList == null) return result;

		Double answersNumber = 0d;
		Double questionsNumber = Double.valueOf(qualitativeQuestionList.size());
		Double maxAnswersWeight = 0d;
		Double maxQuestionAnswersWeight = 0d;
		for (QualitativeQuestions question : qualitativeQuestionList) {
			if (question.getAnswers().size() > 0) {
				answersNumber += question.getAnswers().size();

				double maxQuestionAnswerWeight = 0d;
				for (QualitativeQuestionAnswers answer : question.getAnswers()) {
					if (answer.getAnswerWeight() != null && maxQuestionAnswerWeight < answer.getAnswerWeight().getValue()) {
						maxQuestionAnswerWeight = answer.getAnswerWeight().getValue().doubleValue();
					}
				}
				maxAnswersWeight += maxQuestionAnswerWeight;
				double questionWeight = question.getQuestionWeight() != null && question.getQuestionWeight().getValue() != null ? question.getQuestionWeight().getValue() : 0;
				maxQuestionAnswersWeight += maxQuestionAnswerWeight * questionWeight;
			}
		}

		result.setAnswersNumber(answersNumber);
		result.setQuestionsNumber(questionsNumber);
		result.setMaxAnswersWeight(maxAnswersWeight);
		result.setMaxQuestionAnswersWeight(maxQuestionAnswersWeight);
		result.setQualitativeQuestionList(qualitativeQuestionList);

		return result;
	}

}
