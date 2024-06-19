package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Associate Vendors
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-15
 */
@Entity
@Table(name = "associate_vendors")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "organization", "vendor"})
@EqualsAndHashCode(of = {"id"})
public class AssociateVendors implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", insertable = false, updatable = false)
	private Organizations organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id", nullable = true)
	private Organizations vendor;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "associate_systems",
		joinColumns = {@JoinColumn(name = "associate_vendor_id")},
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
