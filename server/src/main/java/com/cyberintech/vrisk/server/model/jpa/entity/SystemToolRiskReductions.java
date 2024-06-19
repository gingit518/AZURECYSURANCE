package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Associate Systems
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-15
 */
@Entity
@Table(name = "system_tool_risk_reduction")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "system", "riskReductionPercent"})
@EqualsAndHashCode(of = {"id"})
public class SystemToolRiskReductions {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "system_id")
	private Systems system;

	@Column(name = "risk_reduction_percent")
	private Double riskReductionPercent;

	@Column(name = "risk_reduction")
	private Double riskReduction;

	@Column(name = "tool_price")
	private Double toolPrice;

	@Column(name = "tool_name")
	private String toolName;

}
