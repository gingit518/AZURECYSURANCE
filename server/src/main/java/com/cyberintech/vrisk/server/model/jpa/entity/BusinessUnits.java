package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Business Units Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-26
 */
@Entity
@Table(name = "business_units")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class BusinessUnits implements IEntityWithMetadata {

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

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "infosec_focal_person_id")
	private Users infosecFocalPerson;

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

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "business_unit_id")
	private Set<Systems> ownedSystems = new HashSet<>();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "business_unit_id")
	private Set<Processes> ownedProcesses = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "process_to_business_unit_used",
		joinColumns = {@JoinColumn(name = "business_unit_id", updatable = false, insertable = false)},
		inverseJoinColumns = {@JoinColumn(name = "process_id", updatable = false, insertable = false)}
	)
	private Set<Processes> usedProcesses = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private BusinessUnits parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subsidiary_organization_id")
	private Organizations subsidiaryOrganization;
}
