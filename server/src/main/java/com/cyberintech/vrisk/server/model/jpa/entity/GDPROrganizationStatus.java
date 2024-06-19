package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * GDPR Organization Status Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-11-12
 */
@Entity
@Table(name = "gdpr_organization_status")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "organizationId"})
@EqualsAndHashCode(of = {"id"})
public class GDPROrganizationStatus {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "compliance")
	private Double compliance;

	@Column(name = "compliance_metric")
	private Double complianceMetric;

	@Column(name = "files_number")
	private Double filesNumber;

	@Column(name = "articles_processed")
	private Double articlesProcessed;

	@Column(name = "articles_number")
	private Double articlesNumber;

	@Column(name = "comments")
	private String comments;

}
