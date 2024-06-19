package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRSystemArticleStatusLog;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * GDPR System Article Status Log DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-07
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPRSystemArticleStatusLogDTO extends DTOBase<GDPRSystemArticleStatusLog> {

	@Schema
	private Long id;

	@Schema
	private SystemRefDTO system;

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
	public GDPRSystemArticleStatusLogDTO(GDPRSystemArticleStatusLog entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRSystemArticleStatusLogDTO(GDPRSystemArticleStatusLog entity, Systems system) {
		super(entity);

		// Set Entity
		if (entity == null) {
			if (system != null) {
				this.system = new SystemRefDTO(system);
			}
		}
	}

	@Override
	public void fromEntity(GDPRSystemArticleStatusLog entity) {
//		super.fromEntity(entity);

		id = entity.getId();
//		document = entity.getDocument();
		compliance = entity.getCompliance();
		complianceMetric = entity.getComplianceMetric();
		dueDate = entity.getDueDate();
		createdAt = entity.getCreatedAt();
		comments = entity.getComments();
		documentLink = entity.getDocumentLink();
		documentFileType = entity.getDocumentFileType();
		documentFileName = entity.getDocumentFileName();
		documentFileSize = entity.getDocumentFileSize() != null ? entity.getDocumentFileSize().toString() : "";

		if (entity.getSystem() != null) system = new SystemRefDTO(entity.getSystem());
		if (entity.getArticle() != null) {
			article = new GDPRArticleItemDTO(entity.getArticle());
			if (entity.getArticle().getChapter() != null) chapter = new GDPRArticleChapterDTO(entity.getArticle().getChapter());
		}
		if (entity.getParagraph() != null) paragraph = new GDPRArticleParagraphDTO(entity.getParagraph());
		if (entity.getOwner() != null) owner = new UserRefDTO(entity.getOwner());
	}
}
