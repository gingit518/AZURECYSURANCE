package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Process Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Entity
@Table(name = "process")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class Processes implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "owner_id")
	private Users owner;

	@Column(name = "revenue_processed")
	private Double revenueProcessed;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "business_unit_id")
	private BusinessUnits businessUnit;

	@Column(name = "notes")
	private String notes;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "process_to_business_unit_used",
		joinColumns = {@JoinColumn(name = "process_id")},
		inverseJoinColumns = {@JoinColumn(name = "business_unit_id")}
	)
	private Set<BusinessUnits> businessUnitsUsed = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "process_to_data_type",
		joinColumns = {@JoinColumn(name = "process_id")},
		inverseJoinColumns = {@JoinColumn(name = "data_type_id")}
	)
	private Set<DataTypeClassification> dataTypeClassifications = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "process_to_data_asset",
		joinColumns = {@JoinColumn(name = "process_id")},
		inverseJoinColumns = {@JoinColumn(name = "data_asset_id")}
	)
	private Set<DataAssetClassification> dataAssetClassifications = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "process_to_system",
		joinColumns = {@JoinColumn(name = "process_id")},
		inverseJoinColumns = {@JoinColumn(name = "system_id")}
	)
	private Set<Systems> systems = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "created_by_id")
	private Users createdBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "updated_by_id")
	private Users updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

}
