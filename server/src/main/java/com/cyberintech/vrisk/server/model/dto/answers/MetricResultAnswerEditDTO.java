package com.cyberintech.vrisk.server.model.dto.answers;

import com.cyberintech.vrisk.server.model.jpa.entity.MetricResultAnswers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metric Risk Domain Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Setter
@Getter
@NoArgsConstructor
public class MetricResultAnswerEditDTO extends MetricResultAnswerViewDTO {

	@Schema
	private Long riskModelId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public MetricResultAnswerEditDTO(MetricResultAnswers entity) {
		super(entity);
	}

}
