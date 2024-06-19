package com.cyberintech.vrisk.server.model.dto.qual_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.AnswerWeight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Qualification Metrics Answer View Entity Definition
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
public class AnswerWeightDTO extends DTOBase<AnswerWeight> {

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
	public AnswerWeightDTO(AnswerWeight entity) {
		super(entity);
	}

	@Override
	public void fromEntity(AnswerWeight entity) {
		super.fromEntity(entity);
	}
}
