package com.cyberintech.vrisk.server.model.dto.risk_metrics;


import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.formulas.FormulaViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskMetrics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Risk Metrics View Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-02-14
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class RiskMetricsViewDTO extends DTOWithMetaData<RiskMetrics> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Long riskModelId;

	@Schema
	private FormulaViewDTO formula;

	@Schema
	private Boolean isResidual;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RiskMetricsViewDTO(RiskMetrics entity) {
		super(entity);
	}

	@Override
	public void fromEntity(RiskMetrics entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		riskModelId = entity.getRiskModelId();
		isResidual = entity.getIsResidual();

		if (entity.getFormula() != null) {
			formula = new FormulaViewDTO(entity.getFormula());
		}
	}
}
