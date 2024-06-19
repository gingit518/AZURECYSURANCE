package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.RelationToRequirementType;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Assessment Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-04
 */
@Entity
@Table(name = "assessments")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class Assessments implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assessment_level_id", nullable = true)
	private AssessmentLevels assessmentLevel;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assessment_type_id", nullable = true)
	private AssessmentTypes assessmentType;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description")
	private String description;

	// defines that all systems/organizations/processes/technologies selected
	// in case of appropriate assessment level (System/Org/Process/Technology respectively)
	@Column(name = "is_all_selected")
	private Boolean isAllSelected;

	@Enumerated(EnumType.STRING)
	@Column(name = "relation_to_requirements")
	private RelationToRequirementType relationToRequirementType;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "assessments_to_security_requirements",
		joinColumns = {@JoinColumn(name = "assessment_id")},
		inverseJoinColumns = {@JoinColumn(name = "security_requirement_id")}
	)
	private Set<SecurityRequirements> securityRequirements = new HashSet<>();

	// mapped Security Frameworks
	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "assessments_to_assessment_types",
		joinColumns = {@JoinColumn(name = "assessment_id")},
		inverseJoinColumns = {@JoinColumn(name = "assessment_type_id")}
	)
	private Set<AssessmentTypes> assessmentTypes = new HashSet<>();

	@ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
	@JoinTable(
		name = "assessments_to_tasks",
		joinColumns = {@JoinColumn(name = "assessment_id")},
		inverseJoinColumns = {@JoinColumn(name = "task_id")}
	)
	private Set<Tasks> tasks = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "assessment_to_technology_category",
		joinColumns = {@JoinColumn(name = "assessment_id")},
		inverseJoinColumns = {@JoinColumn(name = "technology_category_id")}
	)
	private Set<TechnologyCategories> technologyCategories = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "assessment_to_system",
		joinColumns = {@JoinColumn(name = "assessment_id")},
		inverseJoinColumns = {@JoinColumn(name = "system_id")}
	)
	private Set<Systems> systems = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "assessment_to_process",
		joinColumns = {@JoinColumn(name = "assessment_id")},
		inverseJoinColumns = {@JoinColumn(name = "process_id")}
	)
	private Set<Processes> processes = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "legal_organization_id", nullable = true)
	private Organizations legalOrganization;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "estimated_start_date")
	private Date estimatedStartDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "actual_start_date")
	private Date actualStartDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "estimated_end_date")
	private Date estimatedEndDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "actual_end_date")
	private Date actualEndDate;

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
