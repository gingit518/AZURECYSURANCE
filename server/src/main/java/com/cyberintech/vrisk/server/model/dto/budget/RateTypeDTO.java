package com.cyberintech.vrisk.server.model.dto.budget;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.RateTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Rate Type View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class RateTypeDTO extends DTOBase<RateTypes> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RateTypeDTO(RateTypes entity) {
		super(entity);
	}

	@Override
	public void fromEntity(RateTypes entity) {
		super.fromEntity(entity);
	}
}
