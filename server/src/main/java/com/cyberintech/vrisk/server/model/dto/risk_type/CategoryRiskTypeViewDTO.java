package com.cyberintech.vrisk.server.model.dto.risk_type;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Risk Type View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class CategoryRiskTypeViewDTO extends DTOBase<RiskTypes> {

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
	public CategoryRiskTypeViewDTO(RiskTypes entity) {
		super(entity);
	}

	@Override
	public void fromEntity(RiskTypes entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
	}
}
