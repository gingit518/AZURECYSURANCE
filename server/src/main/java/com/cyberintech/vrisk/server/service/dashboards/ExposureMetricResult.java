package com.cyberintech.vrisk.server.service.dashboards;

import lombok.Getter;
import lombok.Setter;

/**
 * Metric result manager
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-02-28
 */
@Getter
@Setter
public class ExposureMetricResult implements Cloneable {

	private String metricName;

	private Double result;

	private Double resultNormalized;

	private FormulaBuilder formulaBuilder;

	private ExposureMetricStatistics metricStatistic;

	public ExposureMetricResult() {
		result = 0D;
	}

	public ExposureMetricResult(String metricName, Double result) {
		this();
		this.metricName = metricName;
		this.setResult(result);
	}

	public Double getNormalizedResult() {
		Double result = this.getResult();

		return result;
	}

	public static ExposureMetricResult of(String metricName, Double value, FormulaBuilder formulaBuilder) {
		ExposureMetricResult result = of(metricName, value);
		result.setFormulaBuilder(formulaBuilder);

		return result;
	}

	public static ExposureMetricResult of(String metricName, Double value) {
		ExposureMetricResult result = new ExposureMetricResult(metricName, value);

		return result;
	}

}
