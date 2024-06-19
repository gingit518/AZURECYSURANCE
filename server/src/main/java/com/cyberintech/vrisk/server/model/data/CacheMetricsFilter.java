package com.cyberintech.vrisk.server.model.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Implementation of Name Filtering Logic
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-04
 */
@NoArgsConstructor
@Setter
@Getter
public class CacheMetricsFilter extends NameFilter{

	@Schema
	private String systemName;

	@Schema
	private String organizationName;

	@Schema
	private String riskModelName;

	@Schema
	private String metricName;

	@Schema
	private String metricType;

	@Schema
	private String metricLevel;

}
