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
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2019-06-03
 */
@Entity
@Table(name = "fixed_capital_costs")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "vendor", "businessUnit"})
@EqualsAndHashCode(of = {"id"})
public class FixedCapitalCosts {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "vendor_id")
	private Organizations vendor;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "business_unit_id")
	private BusinessUnits businessUnit;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "license_type_id")
	private LicenseTypes licenseType;

	@Column(name = "license_users")
	private Double licenseUsers;

	@Column(name = "license_cpus")
	private Double licenseCpus;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_date")
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "license_cost")
	private Double licenseCost;

	@Column(name = "percent_of_budget")
	private Double percentOfBudget;

	@Column(name = "total_costs")
	private Double totalCosts;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "cost_date")
	private Date costDate;

	/*
	@Deprecated
	@Column(name = "security_tool_name")
	private String securityToolName;
*/
	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "cybersecurity_tool_id")
	private CybersecurityTools cybersecurityTool;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "technology_id")
	private Technologies technology;

}
