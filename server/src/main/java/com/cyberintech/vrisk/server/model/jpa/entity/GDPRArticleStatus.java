package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * GDPR Organization Article Status Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-20
 */
@Entity
@Table(name = "gdpr_article_status")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "article", "compliance"})
@EqualsAndHashCode(of = {"id"})
public class GDPRArticleStatus {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "article_id")
	private GDPRArticleItem article;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "paragraph_id")
	private GDPRArticleParagraph paragraph;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "owner_id")
	private Users owner;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "document_id")
	private Documents document;

	@ManyToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "gdpr_organization_article_tasks",
		joinColumns = {@JoinColumn(name = "article_status_id")},
		inverseJoinColumns = {@JoinColumn(name = "task_id")}
	)
	private Set<Tasks> tasks = new HashSet<>();

	@Column(name = "compliance")
	private Double compliance;

	@Column(name = "compliance_metric")
	private Double complianceMetric;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "due_date")
	private Date dueDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "comments")
	private String comments;

	@Column(name = "document_link")
	private String documentLink;

}
