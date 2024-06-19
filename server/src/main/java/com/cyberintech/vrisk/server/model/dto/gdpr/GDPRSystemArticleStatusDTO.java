package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualificationQuestionViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRSystemArticleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * GDPR System Article Status DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-03
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPRSystemArticleStatusDTO extends DTOBase<GDPRSystemArticleStatus> {

	@Schema
	private Long id;

	@Schema
	private SystemRefDTO system; //

	@Schema
	private GDPRArticleChapterDTO chapter; //

	@Schema
	private GDPRArticleChapterSectionDTO section; //

	@Schema
	private GDPRArticleItemDTO article; //

	@Schema
	private GDPRArticleParagraphDTO paragraph; //

	@Schema
	private QualificationQuestionViewDTO question; //

	@Schema
	private UserRefDTO owner; //

	@Schema
	private DocumentDTO document; //

	@Schema
	private Double compliance;

	@Schema
	private Double complianceMetric;

	@Schema
	private Date dueDate;

	@Schema
	private String comments;

	@Schema
	private String documentLink;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRSystemArticleStatusDTO(GDPRSystemArticleStatus entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRSystemArticleStatusDTO(GDPRSystemArticleStatus entity, GDPRArticleItem article, GDPRArticleToQuestion articleToQuestion) {
		super(entity);

		// Set Entity
		if (entity == null) {
			if (article != null) {
				this.article = new GDPRArticleItemDTO(article);
				this.chapter = new GDPRArticleChapterDTO(article.getChapter());

				if (article.getSection() != null) {
					this.setSection(new GDPRArticleChapterSectionDTO(article.getSection()));
				}
			}

		}

		if (articleToQuestion != null && articleToQuestion.getQuestion() != null) {
			this.question = new QualificationQuestionViewDTO(articleToQuestion.getQuestion());
		}
	}

	@Override
	public void fromEntity(GDPRSystemArticleStatus entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		compliance = entity.getCompliance();
		complianceMetric = entity.getComplianceMetric();
		dueDate = entity.getDueDate();
		comments = entity.getComments();
		documentLink = entity.getDocumentLink();

		if (entity.getSystem() != null) system = new SystemRefDTO(entity.getSystem());
		if (entity.getArticle() != null) {
			article = new GDPRArticleItemDTO(entity.getArticle());
			chapter = new GDPRArticleChapterDTO(entity.getArticle().getChapter());
			if (entity.getArticle().getSection() != null) {
				this.setSection(new GDPRArticleChapterSectionDTO(entity.getArticle().getSection()));
			}
		}
		if (entity.getParagraph() != null) paragraph = new GDPRArticleParagraphDTO(entity.getParagraph());
		if (entity.getOwner() != null) owner = new UserRefDTO(entity.getOwner());
		if (entity.getDocument() != null) document = new DocumentDTO(entity.getDocument());
	}
}
