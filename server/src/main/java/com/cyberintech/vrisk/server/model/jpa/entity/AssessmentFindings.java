package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentFindingLink;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Assessment Findings
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-14
 */
@Entity
@Table(name = "findings")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class AssessmentFindings implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "technology_category_id", nullable = true)
	private TechnologyCategories technologyCategory;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "technology_id")
	private Technologies technology;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "control_subcategory_id", nullable = true)
	private ControlSubcategories controlSubcategory;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "is_gdpr")
	private Boolean isGDPR;

	@Column(name = "percentage")
	private Double percentage;

	@Column(name = "value")
	private Double value;

	@Enumerated(EnumType.STRING)
	@Column(name = "link_type")
	private AssessmentFindingLink linkType;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "finding_to_assessment",
		joinColumns = {@JoinColumn(name = "finding_id")},
		inverseJoinColumns = {@JoinColumn(name = "assessment_id")}
	)
	private Set<Assessments> assessments = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "finding_to_security_requirement",
		joinColumns = {@JoinColumn(name = "finding_id")},
		inverseJoinColumns = {@JoinColumn(name = "security_requirement_id")}
	)
	private Set<SecurityRequirements> securityRequirements = new HashSet<>();

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

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "findings_to_tasks",
		joinColumns = {@JoinColumn(name = "finding_id")},
		inverseJoinColumns = {@JoinColumn(name = "task_id")}
	)
	private Set<Tasks> tasks = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "control_maturity_id")
	private ControlMaturities controlMaturity;

	@Column(name = "subjective_risk_level")
	private Long subjectiveRiskLevel;

}
