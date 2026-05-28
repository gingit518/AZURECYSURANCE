package com.cyberintech.vrisk.server.service.dashboards;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
public class DashboardDataEvaluator {

	Map<Long, ExposureMetricResult> exposureMetrics = new HashMap<>();

	public String evaluate(String valueRef) {
		String result = null;

		if (StringUtils.isNoneEmpty(valueRef)) {
			String[] valueRefPath = StringUtils.split(valueRef, ":");
			if (valueRefPath.length >= 3) {
				if (valueRefPath[0].equalsIgnoreCase("quant")) {
					try {
						Long quantId = Long.valueOf(valueRefPath[1]);
						if (exposureMetrics.containsKey(quantId)) {
							ExposureMetricResult exposureMetric = exposureMetrics.get(quantId);
							String refType = valueRefPath[2];
							if ("formula".equalsIgnoreCase(refType)) {
								result = exposureMetric.getFormulaBuilder().getFormulaString();
							} else if ("name".equalsIgnoreCase(refType)) {
								result = exposureMetric.getFormulaBuilder().getName();
							} else {
								result = exposureMetric.getResult() != null ? String.valueOf(exposureMetric.getResult()) : null;
							}
						}
					} catch (NumberFormatException nfe) {
						// Number Format Exception
					}
				}
			}
		}

		return result;
	}
}
