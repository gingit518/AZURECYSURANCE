package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.VulnerabilityStatus;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Vulnerability Entity Definition
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-03-11
 */
@Entity
@Table(name = "vulnerabilities")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name", "status"})
@EqualsAndHashCode(of = {"id"})
public class Vulnerabilities {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "name")
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private VulnerabilityStatus status;

	@Column(name = "percentage")
	private Double percentage;

	@Column(name = "score")
	private Double score;

	@Column(name = "version")
	private String version;

	@Column(name = "code")
	private String code;

	@Column(name = "description")
	private String description;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "vulnerability_to_technology_category",
		joinColumns = {@JoinColumn(name = "vulnerability_id")},
		inverseJoinColumns = {@JoinColumn(name = "technology_category_id")}
	)
	private Set<TechnologyCategories> technologyCategories = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "vulnerability_to_technology",
		joinColumns = {@JoinColumn(name = "vulnerability_id")},
		inverseJoinColumns = {@JoinColumn(name = "technology_id")}
	)
	private Set<Technologies> technologies = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "vulnerabilities_to_tasks",
		joinColumns = {@JoinColumn(name = "vulnerability_id")},
		inverseJoinColumns = {@JoinColumn(name = "task_id")}
	)
	private Set<Tasks> tasks = new HashSet<>();

}
