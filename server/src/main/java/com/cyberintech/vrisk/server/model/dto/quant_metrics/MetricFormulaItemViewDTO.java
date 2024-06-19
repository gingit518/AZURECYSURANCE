package com.cyberintech.vrisk.server.model.dto.quant_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.risk_model_constants.RiskModelConstantViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.VariableOperation;
import com.cyberintech.vrisk.server.model.jpa.entity.MetricFormulaItems;
import com.cyberintech.vrisk.server.model.jpa.entity.VariableTypes;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Metric Variables View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-16
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricFormulaItemViewDTO extends DTOBase<MetricFormulaItems> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Long ordinal;

	@Schema
	private ItemViewDTO<VariableTypes> variableType;

	@Schema
	private Double value;

	@Schema
	private Boolean isOperation;

	@Schema
	private VariableOperation operation;

	@Schema
	private RiskModelConstantViewDTO riskModelConstantRef;

	@Schema
	private QuantMetricsRefDTO quantMetricRef;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public MetricFormulaItemViewDTO(MetricFormulaItems entity) {
		super(entity);
	}

	@Override
	public void fromEntity(MetricFormulaItems entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		ordinal = entity.getOrdinal();
		value = entity.getValue();
		isOperation = entity.getIsOperation();
		operation = entity.getOperation();

		if (entity.getVariableType() != null) {
			variableType = new ItemViewDTO<VariableTypes>(entity.getVariableType());
		}

		if (entity.getRiskModelConstantRef() != null) {
			riskModelConstantRef = new RiskModelConstantViewDTO(entity.getRiskModelConstantRef());
		}

		if (entity.getQuantMetricRef() != null) {
			quantMetricRef = new QuantMetricsRefDTO(entity.getQuantMetricRef());
		}
	}

	@Override
	public MetricFormulaItems toEntity(MetricFormulaItems origEntity) {

		MetricFormulaItems result = origEntity != null ? origEntity : new MetricFormulaItems();
		result.setName(this.getName());
		result.setDescription(this.getDescription());
		result.setOrdinal(this.getOrdinal());
		result.setValue(this.getValue());
		result.setIsOperation(this.getIsOperation());
		result.setOperation(this.getOperation());

		if (this.getVariableType() != null) {
			result.setVariableTypeId(this.getVariableType().getId());
		}

		if (this.getRiskModelConstantRef() != null) {
			result.setQuantMetricRefId(this.getRiskModelConstantRef().getId());
		}

		if (this.getQuantMetricRef() != null) {
			result.setQuantMetricRefId(this.getQuantMetricRef().getId());
		}

		return result;
	}
}
