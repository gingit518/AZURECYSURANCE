package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Organizations Agreements Entity Definition
 *
 * @author 	 Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since	 2020-01-15
 */
@Entity
@Table(name = "organizations_agreements")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class OrganizationsAgreements implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

//	@Column(name = "organization_id")
//	private Long organizationId;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "organizations_to_agreements",
		joinColumns = {@JoinColumn(name = "agreement_id")},
		inverseJoinColumns = {@JoinColumn(name = "organization_id")}
	)
	private Set<Organizations> organizations = new HashSet<>();

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "organization_id", insertable = false, updatable = false)
//	private Organizations organization;

	@Column(name = "name")
	private String name;

	@Column(name = "content")
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_id")
	private Users createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by_id")
	private Users updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;
}
