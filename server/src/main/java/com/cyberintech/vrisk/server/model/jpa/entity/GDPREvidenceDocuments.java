package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * GDPR Evidence Documents
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-11-11
 */
@Entity
@Table(name = "gdpr_evidence_document_definition")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "documentType", "name"})
@EqualsAndHashCode(of = {"id"})
public class GDPREvidenceDocuments {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "document_type")
	private String documentType;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "template_link", nullable = true)
	private String templateLink;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "gdpr_document_articles",
		joinColumns = {@JoinColumn(name = "document_definition_id")},
		inverseJoinColumns = {@JoinColumn(name = "article_id")}
	)
	private Set<GDPRArticleItem> articles = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "gdpr_evidence_documents",
		joinColumns = {@JoinColumn(name = "document_definition_id")},
		inverseJoinColumns = {@JoinColumn(name = "document_id")}
	)
	private Set<Documents> documents = new HashSet<>();

}
