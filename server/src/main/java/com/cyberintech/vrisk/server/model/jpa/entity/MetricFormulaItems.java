package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.VariableOperation;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Metric Formula Items Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-31
 */
@Entity
@Table(name = "metric_formula_items")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class MetricFormulaItems implements IFormulaItem {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "quant_metric_id")
	private Long quantMetricId;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "ordinal")
	private Long ordinal;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "variable_type_id", updatable = false, insertable = false)
	private VariableTypes variableType;

	@Column(name = "variable_type_id")
	private Long variableTypeId;

	@Column(name = "value")
	private Double value;

	@Column(name = "is_operation")
	private Boolean isOperation;

	@Enumerated(EnumType.STRING)
	@Column(name = "operation")
	private VariableOperation operation;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "risk_model_constant_ref_id", updatable = false, insertable = false)
	private RiskModelConstants riskModelConstantRef;

	@Column(name = "risk_model_constant_ref_id")
	private Long riskModelConstantRefId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "quant_metric_ref_id", updatable = false, insertable = false)
	private QuantMetrics quantMetricRef;

	@Column(name = "quant_metric_ref_id")
	private Long quantMetricRefId;

}
