package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Data Fields Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-16
 */
@Entity
@Table(name = "policy_statements")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class PolicyStatements {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "statement", nullable = false, length = 4096)
	private String statement;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "policy_statements_to_security_requirements",
		joinColumns = {@JoinColumn(name = "policy_statement_id")},
		inverseJoinColumns = {@JoinColumn(name = "security_requirement_id")}
	)
	private Set<SecurityRequirements> securityRequirements;

}
