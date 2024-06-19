package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * GDPR Article Items DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-24
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPRArticleItemDTO extends DTOBase<GDPRArticleItem> {

	@Schema
	private Long id;

	@Schema
	private String referenceNumber;

	@Schema
	private Long articleNumber;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private String question;

	@Schema
	private String bestPractice;

	@Schema
	private List<GDPRArticleParagraphDTO> paragraphs = new ArrayList<>();

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRArticleItemDTO(GDPRArticleItem entity) {
		super(entity);
	}

	@Override
	public void fromEntity(GDPRArticleItem entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		referenceNumber = entity.getReferenceNumber();
		articleNumber = entity.getArticleNumber();
		name = entity.getName();
		description = entity.getDescription();
		question = entity.getQuestion();
		bestPractice = entity.getBestPractice();

		paragraphs = Optional.ofNullable(entity.getParagraphs()).orElse(new HashSet<>()).stream().map(GDPRArticleParagraphDTO::new).collect(Collectors.toList());
	}
}
