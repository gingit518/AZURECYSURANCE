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
public class QuestionSystemFilter extends BaseFilter<Long> {

	@Schema
	private Long systemId;

	@Schema
	private String metricDomain;
}
