package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * GDPR Article Chapter Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-08-21
 */
@Entity
@Table(name = "gdpr_article_chapter_section")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class GDPRArticleChapterSection {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "chapter_id")
	private GDPRArticleChapter chapter;

	@Column(name = "section_number")
	private Long sectionNumber;

	@Column(name = "reference_number")
	private String referenceNumber;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "section_id")
	private Set<GDPRArticleItem> items = new HashSet<>();

}
