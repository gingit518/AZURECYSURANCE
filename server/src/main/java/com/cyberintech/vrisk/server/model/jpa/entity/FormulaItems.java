package com.cyberintech.vrisk.server.model.jpa.entity;


import com.cyberintech.vrisk.server.model.jpa.domains.VariableOperation;
import lombok.*;

import javax.persistence.*;

/**
 * Formula Item Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-02-12
 */
@Entity
@Table(name = "formula_items")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class FormulaItems implements IFormulaItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "formula_id", nullable = false, updatable = false, insertable = false)
	private Formulas formula;

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

	public static FormulaItems ofOperation(VariableOperation operation) {
		FormulaItems result = new FormulaItems();
		result.setOperation(operation);

		return result;
	}

}
