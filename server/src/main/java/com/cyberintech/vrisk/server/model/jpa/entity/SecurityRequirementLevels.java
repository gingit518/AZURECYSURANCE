package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentFrameworkLevel;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Security Requirements Levels
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-06-06
 */
@Entity
@Table(name = "security_requirement_levels")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class SecurityRequirementLevels {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "assessment_framework_level")
	private AssessmentFrameworkLevel assessmentFrameworkLevel;

}
