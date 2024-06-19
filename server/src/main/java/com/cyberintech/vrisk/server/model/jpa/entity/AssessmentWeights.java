package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Assessment Weights Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-04
 */
@Entity
@Table(name = "assessment_weights")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class AssessmentWeights {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "control_subcategory_id", nullable = false, insertable = false, updatable = false)
	private ControlSubcategories controlSubcategory;

	@Column(name = "name", nullable = false, length = 63)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "value")
	private Long value;

}
