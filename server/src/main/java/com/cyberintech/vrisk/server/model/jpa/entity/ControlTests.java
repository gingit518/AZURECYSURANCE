package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Assessment Weights Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-04
 */
@Entity
@Table(name = "control_tests")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class ControlTests implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assessment_id", nullable = true)
	private Assessments assessment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assessment_type_id", nullable = true)
	private AssessmentTypes assessmentType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "control_function_id", nullable = true)
	private ControlFunctions controlFunction;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "control_category_id", nullable = true)
	private ControlCategories controlCategory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "control_subcategory_id", nullable = true)
	private ControlSubcategories controlSubcategory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assessment_weight_id", nullable = true)
	private AssessmentWeights assessmentWeight;

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
