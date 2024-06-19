package com.cyberintech.vrisk.server.model.dto.budget;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.quant_metrics.QuantMetricsRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.CybersecurityTools;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Cyber Security Tool View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-10
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class CybersecurityToolDTO extends DTOWithMetaData<CybersecurityTools> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private boolean readOnly;

	@Schema
	private Double riskReduction;

	@Schema
	private Double riskReductionPercent;

	@Schema
	private Double toolPrice;

	@Schema
	private List<QuantMetricsRefDTO> quantMetrics;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public CybersecurityToolDTO(CybersecurityTools entity) {
		super(entity);
	}

	@Override
	public void fromEntity(CybersecurityTools entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		riskReduction = entity.getRiskReduction();
		riskReductionPercent = entity.getRiskReductionPercent();
		toolPrice = entity.getToolPrice();

		readOnly = entity.getOrganizationId() == null;
		quantMetrics = Optional.ofNullable(entity.getQuantMetrics()).orElse(new HashSet<>()).stream().map(QuantMetricsRefDTO::new).collect(Collectors.toList());
	}
}
