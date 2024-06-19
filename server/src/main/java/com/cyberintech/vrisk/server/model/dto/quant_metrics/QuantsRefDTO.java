package com.cyberintech.vrisk.server.model.dto.quant_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Quants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Quantification Category Ref Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-17
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class QuantsRefDTO extends DTOBase<Quants> {

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
	public QuantsRefDTO(Quants entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Quants entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
	}

}
