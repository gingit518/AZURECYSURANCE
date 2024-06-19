package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Risk Model Domains Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-09
 */
@Entity
@Table(name = "risk_model_domains")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "riskModelId", "riskDomain"})
@EqualsAndHashCode(of = {"id", "riskModelId", "riskDomain"})
public class RiskModelDomains {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "risk_model_id")
	private Long riskModelId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "risk_domain_id")
	private RiskDomains riskDomain;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "risk_management_owner_id")
	private Users riskManagementOwner;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by_id")
	private Users createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

}
