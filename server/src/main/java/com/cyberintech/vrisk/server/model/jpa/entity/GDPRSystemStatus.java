package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * GDPR System Status Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-30
 */
@Entity
@Table(name = "gdpr_system_status")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "system"})
@EqualsAndHashCode(of = {"id"})
public class GDPRSystemStatus {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "system_id")
	private Systems system;

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
