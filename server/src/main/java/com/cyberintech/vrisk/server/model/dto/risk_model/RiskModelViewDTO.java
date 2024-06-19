package com.cyberintech.vrisk.server.model.dto.risk_model;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Risk Model View Entity Definition
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
public class RiskModelViewDTO extends DTOBase<RiskModels> {

	private Long id;

	@Schema(example = "DEFAULT")
	private String name;
	private String description;
	private Long ordinal;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RiskModelViewDTO(RiskModels entity) {
		super(entity);
	}
}
