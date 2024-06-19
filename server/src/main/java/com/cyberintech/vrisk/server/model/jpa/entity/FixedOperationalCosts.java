package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Fixed Operational Costs Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Entity
@Table(name = "fixed_operational_costs")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "user", "businessUnit"})
@EqualsAndHashCode(of = {"id"})
public class FixedOperationalCosts {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Deprecated
	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "role_id")
	private Roles role;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "cyber_role_id")
	private CyberRoles cyberRole;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "user_id")
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "business_unit_id")
	private BusinessUnits businessUnit;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "task_id")
	private Tasks task;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "rate_type_id")
	private RateTypes rateType;

	@Column(name = "rate")
	private Double rate;

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

}
