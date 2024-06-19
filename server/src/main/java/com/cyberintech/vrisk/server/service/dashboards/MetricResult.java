package com.cyberintech.vrisk.server.service.dashboards;

import lombok.Data;

import java.util.List;

/**
 * Metric result manager
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-02-28
 */
@Data
public class MetricResult<QUESTION_ANSWER_ENTITY> implements Cloneable {

	public static final String DEFAULT_FORMULA = "SUM(Answer Score * Question Weight) / SUM(MAX(Answer Score) * Question Weight)";

	private String metricName;

	private Double result;

	private Double maxQuestionsAnswersWeight = 1d;

	private Double resultNormalized;

	private String formulaString = DEFAULT_FORMULA;

	private MetricStatistics metricStatistic;

	private List<QUESTION_ANSWER_ENTITY> questionAnswers;

	public MetricResult() {
		result = 0D;
	}

	public MetricResult(String metricName, Double result) {
		this();
		this.metricName = metricName;
		this.setResult(result);
	}

	/**
	 * Returns result normalized by Answers Weight
	 *
	 * @return
	 */
	public Double buildNormalizedResult() {
		resultNormalized = this.getResult();

		if (maxQuestionsAnswersWeight > 0) {
			resultNormalized = this.getResult() / maxQuestionsAnswersWeight;
		}

		return resultNormalized;
	}

}
