package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleStatusLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * GDPR Organization Article Status Log DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-20
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPRArticleStatusLogDTO extends DTOBase<GDPRArticleStatusLog> {

	@Schema
	private Long id;

	@Schema
	private GDPRArticleChapterDTO chapter;

	@Schema
	private GDPRArticleItemDTO article;

	@Schema
	private GDPRArticleParagraphDTO paragraph;

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
	private Date createdAt;

	@Schema
	private String comments;

	@Schema
	private String documentLink;

	@Schema
	private String documentFileType;

	@Schema
	private String documentFileName;

	@Schema
	private String documentFileSize;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRArticleStatusLogDTO(GDPRArticleStatusLog entity) {
		super(entity);
	}

	@Override
	public void fromEntity(GDPRArticleStatusLog entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
//		this.document = entity.getDocument();
		this.compliance = entity.getCompliance();
		this.complianceMetric = entity.getComplianceMetric();
		this.dueDate = entity.getDueDate();
		this.createdAt = entity.getCreatedAt();
		this.comments = entity.getComments();
		this.documentLink = entity.getDocumentLink();
		this.documentFileType = entity.getDocumentFileType();
		this.documentFileName = entity.getDocumentFileName();
		if(entity.getDocumentFileSize() != null) {
			this.documentFileSize = entity.getDocumentFileSize().toString();
		}

		if (entity.getArticle() != null) {
			article = new GDPRArticleItemDTO(entity.getArticle());
			if (entity.getArticle().getChapter() != null) chapter = new GDPRArticleChapterDTO(entity.getArticle().getChapter());
		}
		if (entity.getParagraph() != null) paragraph = new GDPRArticleParagraphDTO(entity.getParagraph());
		if (entity.getOwner() != null) owner = new UserRefDTO(entity.getOwner());
	}
}
