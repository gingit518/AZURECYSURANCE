package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.VariableFunction;
import com.cyberintech.vrisk.server.model.jpa.domains.VariableOperation;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Metric Variables Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-16
 */
@Entity
@Table(name = "metric_variables")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
@Deprecated
public class MetricVariables {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "ordinal")
	private Long ordinal;

	@Column(name = "factor")
	private Double factor;

	@Column(name = "value")
	private Double value;

	@Enumerated(EnumType.STRING)
	@Column(name = "operation_before")
	private VariableOperation operationBefore;

	@Enumerated(EnumType.STRING)
	@Column(name = "operation_after")
	private VariableOperation operationAfter;

	@Enumerated(EnumType.STRING)
	@Column(name = "variable_function")
	private VariableFunction variableFunction;

}
