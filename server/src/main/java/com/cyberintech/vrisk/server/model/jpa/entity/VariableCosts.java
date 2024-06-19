package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Variable Costs Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Entity
@Table(name = "variable_costs")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class VariableCosts {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "system_id")
	private Systems system;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "cost_type_id")
	private CostTypes costType;

	@Column(name = "equipment_cost")
	private Double equipmentCost;

	@Column(name = "personnel_cost")
	private Double personnelCost;

	@Column(name = "percent_of_time")
	private Double percentOfTime;

	@Column(name = "percent_of_budget")
	private Double percentOfBudget;

	@Column(name = "total_costs")
	private Double totalCosts;

	@Column(name = "comments")
	private String comments;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "cost_date")
	private Date costDate;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "variable_costs_to_fixed_capital_costs",
		joinColumns = {@JoinColumn(name = "variable_costs_id")},
		inverseJoinColumns = {@JoinColumn(name = "fixed_capital_costs_id")}
	)
	private Set<FixedCapitalCosts> fixedCapitalCosts = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "variable_costs_to_fixed_operational_costs",
		joinColumns = {@JoinColumn(name = "variable_costs_id")},
		inverseJoinColumns = {@JoinColumn(name = "fixed_operational_costs_id")}
	)
	private Set<FixedOperationalCosts> fixedOperationalCosts = new HashSet<>();

}
