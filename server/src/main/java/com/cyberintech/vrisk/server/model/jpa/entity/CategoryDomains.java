package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Category Domain Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-13
 */
@Entity
@Table(name = "category_domains")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"})
public class CategoryDomains implements IEntityWithDates {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name", unique = true, nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "risk_model_domain_id")
	private RiskModelDomains riskModelDomain;

	@Column(name = "risk_model_id")
	private Long riskModelId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

	@OneToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinColumn(name = "category_domain_id")
	private Set<RiskTypes> riskTypes = new HashSet<>();

	/**
	 * Default addRiskType() method.
	 *
	 * @param riskType
	 */
	public void addRiskType(RiskTypes riskType) {
		riskTypes.add(riskType);
		riskType.setCategoryDomain(this);
	}

	/**
	 * Detach Risk Type from the common entity
	 *
	 * @param riskType
	 */
	public void removeRiskType(RiskTypes riskType) {
		riskTypes.remove(riskType);
		riskType.setCategoryDomain(null);
	}

}
