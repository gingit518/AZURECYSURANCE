package com.cyberintech.vrisk.server.model.dto.quant_metrics;

import com.cyberintech.vrisk.server.model.dto.regulations.RegulationRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Quants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Quantification Category Edit Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-17
 */
@Setter
@Getter
@NoArgsConstructor
public class QuantsEditDTO extends QuantsViewDTO {

	@Schema
	private Long riskModelId;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public QuantsEditDTO(Quants entity) {
		super(entity);
	}

}
