package com.cyberintech.vrisk.server.model.dto.quant_metrics;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.VariableFunction;
import com.cyberintech.vrisk.server.model.jpa.domains.VariableOperation;
import com.cyberintech.vrisk.server.model.jpa.entity.MetricVariables;
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
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
@Deprecated
public class MetricVariableViewDTO extends DTOBase<MetricVariables> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private Long ordinal;

	@Schema
	private Double factor;

	@Schema
	private Double value;

	@Schema
	private VariableOperation operationBefore;

	@Schema
	private VariableOperation operationAfter;

//	@Schema
//	private ValueSource valueSource;

	@Schema
	private VariableFunction variableFunction;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public MetricVariableViewDTO(MetricVariables entity) {
		super(entity);
	}

	@Override
	public void fromEntity(MetricVariables entity) {
		super.fromEntity(entity);
	}
}
