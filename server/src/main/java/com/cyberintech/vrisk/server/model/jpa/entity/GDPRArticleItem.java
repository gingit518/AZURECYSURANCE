package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * GDPR Article Item Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-08-21
 */
@Entity
@Table(name = "gdpr_article")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class GDPRArticleItem {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "chapter_id")
	private GDPRArticleChapter chapter;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "section_id")
	private GDPRArticleChapterSection section;

	@Column(name = "article_number")
	private Long articleNumber;

	@Column(name = "reference_number")
	private String referenceNumber;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "question")
	private String question;

	@Column(name = "best_practice")
	private String bestPractice;

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "article_id")
	private Set<GDPRArticleParagraph> paragraphs = new HashSet<>();

	@Column(name = "is_mandatory")
	private Boolean isMandatory;

	@Column(name = "is_system_level")
	private Boolean isSystemLevel;

	@Column(name = "is_organization_level")
	private Boolean isOrganizationLevel;

}
