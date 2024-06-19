package com.cyberintech.vrisk.server.model.dto.qual_metrics;

import com.cyberintech.vrisk.server.model.dto.qualitative_question.QualitativeQuestionViewDTO;
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
public class MetricStatisticsDTO {

	private Double answersNumber;

	private Double questionsNumber;

	private Double maxAnswersWeight;

	private Double maxQuestionAnswersWeight;

	private List<QualitativeQuestionViewDTO> qualitativeQuestionList;

	public MetricStatisticsDTO() {
		answersNumber = 0d;
		questionsNumber = 0d;
		maxAnswersWeight = 0d;
		maxQuestionAnswersWeight = 0d;
		qualitativeQuestionList = new ArrayList<>();
	}
}
