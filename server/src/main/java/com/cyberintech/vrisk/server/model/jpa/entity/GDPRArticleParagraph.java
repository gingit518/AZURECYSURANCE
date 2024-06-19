package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * GDPR Article Item Paragraph Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-08-21
 */
@Entity
@Table(name = "gdpr_article_paragraph")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class GDPRArticleParagraph {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "article_id")
	private GDPRArticleItem article;

	@Column(name = "paragraph_number")
	private Long paragraphNumber;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "question")
	private String question;

	@Column(name = "best_practice")
	private String bestPractice;

	@Column(name = "is_system_level")
	private Boolean isSystemLevel;

	@Column(name = "is_organization_level")
	private Boolean isOrganizationLevel;

}
