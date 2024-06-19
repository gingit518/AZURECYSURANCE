package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.qual_metrics.QualificationQuestionViewDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskEditDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleItem;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GDPR Organization Article Status DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-03
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPRArticleStatusDTO extends DTOBase<GDPRArticleStatus> {

	@Schema
	private Long id;

	@Schema
	private GDPRArticleChapterDTO chapter;

	@Schema
	private GDPRArticleChapterSectionDTO section;

	@Schema
	private GDPRArticleItemDTO article;

	@Schema
	private GDPRArticleParagraphDTO paragraph;

	@Schema
	private QualificationQuestionViewDTO question;

	@Schema
	private UserRefDTO owner;

	@Schema
	private DocumentDTO document;

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

	@Schema
	private List<TaskEditDTO> tasks;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRArticleStatusDTO(GDPRArticleStatus entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRArticleStatusDTO(GDPRArticleStatus entity, GDPRArticleItem article) {
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
	}

	@Override
	public void fromEntity(GDPRArticleStatus entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
//		this.question = entity.getQuestion();
		this.compliance = entity.getCompliance();
		this.complianceMetric = entity.getComplianceMetric();
		this.dueDate = entity.getDueDate();
		this.comments = entity.getComments();
		this.documentLink = entity.getDocumentLink();

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

		// Loading tasks
		// a NullPointerException is thrown after usage getEntity() in loadTasks() without super.fromEntity(entity), since "entity" isn't initialized in the parent class this way.
//		loadTasks();
		this.tasks = entity.getTasks().stream().map(TaskEditDTO::new).collect(Collectors.toList());
	}

	/**
	 * Load tasks list
	 */
	public void loadTasks() {
		tasks = getEntity().getTasks().stream().map(TaskEditDTO::new).collect(Collectors.toList());
	}
}
