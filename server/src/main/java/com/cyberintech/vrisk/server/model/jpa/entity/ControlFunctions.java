package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Control Functions Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-04
 */
@Entity
@Table(name = "control_functions")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class ControlFunctions {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "security_framework_id", nullable = true)
	private AssessmentTypes assessmentType;

	@Column(name = "code", nullable = true, length = 127)
	private String code;

	@Column(name = "name", nullable = true, length = 255)
	private String name;

	@Column(name = "description")
	private String description;

}
