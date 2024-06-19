package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.ExternalAnalyticsType;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * External Analytics Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2021-10-13
 */
@Entity
@Table(name = "external_analytics")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class ExternalAnalytics {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", insertable = false, updatable = false)
	private Organizations organization;

	@Enumerated(EnumType.STRING)
	@Column(name = "external_analytics_type")
	private ExternalAnalyticsType externalAnalyticsType;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description")
	private String description;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "external_analytics_id", updatable = true, insertable = true)
	private Set<ExternalAnalyticsParameters> externalAnalyticsParameters;

	@Column(name = "is_public")
	private Boolean isPublic;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "external_analytics_to_roles",
		joinColumns = {@JoinColumn(name = "external_analytic_id")},
		inverseJoinColumns = {@JoinColumn(name = "role_id")}
	)
	private Set<Roles> roles = new HashSet<>();

	@Column(name = "logo", length = 255)
	private String logo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "logo_document_id")
	private Documents logoDocument;

}
