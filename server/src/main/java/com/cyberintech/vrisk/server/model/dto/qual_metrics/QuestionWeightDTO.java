package com.cyberintech.vrisk.server.model.dto.qual_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.QuestionWeights;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Qualification Metrics Questions View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-03
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class QuestionWeightDTO extends DTOBase<QuestionWeights> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Long value;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QuestionWeightDTO(QuestionWeights entity) {
		super(entity);
	}

	@Override
	public void fromEntity(QuestionWeights entity) {
		super.fromEntity(entity);
	}
}
