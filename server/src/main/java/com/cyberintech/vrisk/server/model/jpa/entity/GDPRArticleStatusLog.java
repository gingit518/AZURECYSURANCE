package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * GDPR Article Status Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-20
 */
@Entity
@Table(name = "gdpr_article_status_log")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "article", "compliance"})
@EqualsAndHashCode(of = {"id"})
public class GDPRArticleStatusLog {

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

	@Column(name = "document_file_type")
	private String documentFileType;

	@Column(name = "document_file_name")
	private String documentFileName;

	@Column(name = "document_file_size")
	private Double documentFileSize;

	@Column(name = "document_uid")
	private String documentUid;

	@Column(name = "remote_path")
	private String remotePath;

}
