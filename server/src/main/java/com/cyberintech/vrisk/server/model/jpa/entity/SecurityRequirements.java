package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Security Requirements Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-01-22
 */
@Entity
@Table(name = "security_requirements")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class SecurityRequirements {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "assessments_to_security_requirements",
		joinColumns = {@JoinColumn(name = "security_requirement_id")},
		inverseJoinColumns = {@JoinColumn(name = "assessment_id")}
	)
	private Set<Assessments> assessments = new HashSet<>();

	@OneToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "security_requirement_id")
	private Set<SecurityRequirementLevels> securityRequirementLevels = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "security_control_family_id")
	private SecurityControlFamilies securityControlFamily;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "security_control_name_id")
	private SecurityControlNames securityControlName;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "assessment_level_id")
	private AssessmentLevels assessmentLevel;

	@Column(name = "code", nullable = true, length = 255)
	private String code;

	@Column(name = "program_area")
	private String programArea;

	@Column(name = "description")
	private String description;

	@Column(name = "detailed_control_testing_procedure")
	private String detailedControlTestingProcedure;

	@Column(name = "risk_statement_examples")
	private String riskStatementExamples;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "security_requirements_frameworks_mapping",
		joinColumns = {@JoinColumn(name = "security_requirement_id")},
		inverseJoinColumns = {@JoinColumn(name = "control_subcategory_id")}
	)
	private Set<ControlSubcategories> controlSubcategories = new HashSet<>();

}
