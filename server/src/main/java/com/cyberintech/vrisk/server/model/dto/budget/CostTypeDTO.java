package com.cyberintech.vrisk.server.model.dto.budget;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.CostTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Cost Type View Entity Definition
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
public class CostTypeDTO extends DTOBase<CostTypes> {

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
	public CostTypeDTO(CostTypes entity) {
		super(entity);
	}

	@Override
	public void fromEntity(CostTypes entity) {
		super.fromEntity(entity);
	}
}
