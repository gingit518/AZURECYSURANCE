package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * System Requirement Control Test Result Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.1
 * @since	 2020.01.28
 */
@Entity
@Table(name = "system_control_test_results")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "system", "assessmentWeight"})
@EqualsAndHashCode(of = {"id"})
public class SystemControlTestResults {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "system_id")
	private Systems system;

	@Column(name = "assessment_weight")
	private Double assessmentWeight;

	@OneToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinColumn(name = "system_control_test_result_id")
	private Set<SystemRequirementControlTestResults> systemRequirementControlTestResults;
}
