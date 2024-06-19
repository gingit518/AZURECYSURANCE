package com.cyberintech.vrisk.server.model.jpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * GDPR Article to Question Relation Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-11-08
 */
@Entity
@Table(name = "gdpr_article_to_question")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "article", "paragraph", "question"})
@EqualsAndHashCode(of = {"id"})
public class GDPRArticleToQuestion {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "organization_id")
	private Long organizationId;

	@Column(name = "article_id")
	private Long articleId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "article_id", insertable = false, updatable = false)
	private GDPRArticleItem article;

	@Column(name = "paragraph_id")
	private Long paragraphId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "paragraph_id", insertable = false, updatable = false)
	private GDPRArticleParagraph paragraph;

	@Column(name = "question_id")
	private Long questionId;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "question_id", insertable = false, updatable = false)
	private QualitativeQuestions question;

}
