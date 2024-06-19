package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Organization Requirement Control Test Result Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since 	 2020-01-28
 */
@Entity
@Table(name = "organization_requirement_control_test_results")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "securityRequirement", "assessmentWeight"})
@EqualsAndHashCode(of = {"id"})
public class OrganizationRequirementControlTestResults {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "security_requirement_id")
	private SecurityRequirements securityRequirement;

	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinTable(
		name = "organization_requirement_control_test_result_to_task",
		joinColumns = {@JoinColumn(name = "organization_control_test_result_id")},
		inverseJoinColumns = {@JoinColumn(name = "task_id")}
	)
	private Set<Tasks> tasks = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinTable(
		name = "security_audit_comment_to_organization_requirement_control_test",
		joinColumns = {@JoinColumn(name = "organization_requirement_control_test_result_id")},
		inverseJoinColumns = {@JoinColumn(name = "security_audit_comment_id")}
	)
	private Set<SecurityAuditComments> securityAuditComments = new HashSet<>();

	@Column(name = "comments")
	private String comments;

	@Column(name = "evidence_eligible")
	private Boolean evidenceEligible;

	@Column(name = "assessment_weight")
	private Double assessmentWeight;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "document_id")
	private Documents document;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "control_maturity_id")
	private ControlMaturities controlMaturity;

}
