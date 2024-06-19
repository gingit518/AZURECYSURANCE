package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.PolicyReviewType;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Policy Review Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2023-09-24
 */
@Entity
@Table(name = "policy_reviews")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"})
public class PolicyReviews {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "policy_id")
	private Long policyId;

	@Column(name = "notes")
	private String notes;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "review_type")
	private PolicyReviewType reviewType;

	@Column(name = "reviewer_id")
	private Long reviewerId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewer_id", nullable = false, insertable = false, updatable = false)
	private Users reviewer;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "review_date")
	private Date reviewDate;

	@Column(name = "review_year")
	private Long reviewYear;

	@Column(name = "review_month")
	private Long reviewMonth;

	@Column(name = "review_day")
	private Long reviewDay;

}
