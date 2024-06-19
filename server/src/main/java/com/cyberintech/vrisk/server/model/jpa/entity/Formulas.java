package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Formula Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-02-12
 */
@Entity
@Table(name = "formulas")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name", "formula"})
@EqualsAndHashCode(of = {"id"})
public class Formulas {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "formula")
	private String formula;

	@Column(name = "organization_id")
	private Long organizationId;

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name = "formula_id")
	private Set<FormulaItems> formulaItems = new HashSet<>();

}
