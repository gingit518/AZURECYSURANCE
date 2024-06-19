package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleParagraph;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GDPR Article Paragraph DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-24
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPRArticleParagraphDTO extends DTOBase<GDPRArticleParagraph> {

	@Schema
	private Long id;

	@Schema
	private String referenceNumber;

	@Schema
	private Long paragraphNumber;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private String question;

	@Schema
	private String bestPractice;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRArticleParagraphDTO(GDPRArticleParagraph entity) {
		super(entity);
	}

	@Override
	public void fromEntity(GDPRArticleParagraph entity) {
//		super.fromEntity(entity);

		id = entity.getId();
//		referenceNumber = entity.getgetReferenceNumber();
		paragraphNumber = entity.getParagraphNumber();
		name = entity.getName();
		description = entity.getDescription();
		question = entity.getQuestion();
		bestPractice = entity.getBestPractice();
	}
}
