package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Package Plans Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-07-22
 */
@Entity
@Table(name = "package_plans")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class PackagePlans {

	public static final Long PACKAGE_PLAN_CCPA = 1L;
	public static final Long PACKAGE_PLAN_ELASTIO = 2L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description")
	private String description;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "package_plan_to_role",
		joinColumns = {@JoinColumn(name = "package_plan_id")},
		inverseJoinColumns = {@JoinColumn(name = "role_id")}
	)
	private Set<Roles> roles = new HashSet<>();

}
