package com.cyberintech.vrisk.server.model.dto.qual_metrics;

import com.cyberintech.vrisk.server.service.dashboards.MetricResult;
import com.cyberintech.vrisk.server.service.dashboards.MetricStatistics;
import lombok.Data;

import java.text.MessageFormat;
import java.util.List;

/**
 * Metric result manager
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-02-28
 */
@Data
public class MetricResultDTO<QUESTION_ANSWER_ENTITY> implements Cloneable {

	public static final String DEFAULT_FORMULA = "SUM(Answer Score * Question Weight) / SUM(MAX(Answer Score) * Question Weight)";

	private String metricName;

	private Double result;

	private Double maxQuestionsAnswersWeight = 1d;

	private Double resultNormalized;

	private String resultNormalizedString;

	private String formulaString = DEFAULT_FORMULA;

	private MetricStatisticsDTO metricStatistic;

	private List<QualificationQuestionViewDTO> questionAnswers;

	private Boolean hideCalculation = false;

	public MetricResultDTO() {
		result = 0D;
	}

	public MetricResultDTO(String metricName, Double result) {
		this();
		this.metricName = metricName;
		this.setResult(result);
	}

	public static MetricResultDTO of(MetricResult metricResult) {

		metricResult.buildNormalizedResult();

		MetricResultDTO result = new MetricResultDTO();
		result.setResult(metricResult.getResult());
		result.setResultNormalized(metricResult.getResultNormalized());
		if (metricResult.getResultNormalized() != null) result.setResultNormalizedString(String.format("%,.2f", metricResult.getResultNormalized()));
		result.setMetricName(metricResult.getMetricName());
		result.setMaxQuestionsAnswersWeight(metricResult.getMaxQuestionsAnswersWeight());

		return result;
	}

}
