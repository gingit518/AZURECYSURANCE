package com.cyberintech.vrisk.server.model.jpa.entity;


import static javax.persistence.GenerationType.IDENTITY;

import lombok.*;

import javax.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Policies Entity
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-01-08
 */
@Entity
@Table(name = "policies")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class Policies {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "name")
	private String name;

	@Column(name = "version")
	private String version;

	@Column(name = "overview")
	private String overview;

	@Column(name = "purpose")
	private String purpose;

	@Column(name = "scope")
	private String scope;

	@ManyToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "policies_to_cyber_roles",
		joinColumns = {@JoinColumn(name = "policy_id")},
		inverseJoinColumns = {@JoinColumn(name = "cyber_role_id")}
	)
	private Set<CyberRoles> rolesAndResponsibilities;

	@Column(name = "enforcement")
	private String enforcement;

	@Column(name = "exceptions")
	private String exceptions;

	@Column(name = "definitions")
	private String definitions;

	@ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "policies_to_related_policies",
		joinColumns = {@JoinColumn(name = "policy_id")},
		inverseJoinColumns = {@JoinColumn(name = "related_policy_id")}
	)
	private Set<Policies> relatedPolicies;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "policy_id")
	private Set<PolicyStatements> statements;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "created_by_id")
	private Users createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "approved_at")
	private Date approvedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "approved_by_id")
	private Users approvedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "annual_review_date")
	private Date annualReviewDate;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "assessment_types_to_policies",
		joinColumns = {@JoinColumn(name = "policy_id")},
		inverseJoinColumns = {@JoinColumn(name = "assessment_type_id")}
	)
	private Set<AssessmentTypes> assessmentTypes;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "gdpr_articles_to_policies",
		joinColumns = {@JoinColumn(name = "policy_id")},
		inverseJoinColumns = {@JoinColumn(name = "gdpr_article_id")}
	)
	private Set<GDPRArticleItem> gdprArticles;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "document_id")
	private Documents document;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "policies_to_security_requirements",
		joinColumns = {@JoinColumn(name = "policy_id")},
		inverseJoinColumns = {@JoinColumn(name = "security_requirement_id")}
	)
	private Set<SecurityRequirements> securityRequirements;
}
