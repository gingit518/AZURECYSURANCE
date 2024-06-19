package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleChapter;
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
 * GDPR Article Chapter DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-24
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPRArticleChapterDTO extends DTOBase<GDPRArticleChapter> {

	@Schema
	private Long id;

	@Schema
	private Long chapterNumber;

	@Schema
	private String referenceNumber;

	@Schema
	private String name;

	@Schema
	private String description;

//	@Schema
//	private List<GDPRArticleChapterSectionDTO> sections = new ArrayList<>();

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRArticleChapterDTO(GDPRArticleChapter entity) {
		super(entity);
	}

	@Override
	public void fromEntity(GDPRArticleChapter entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		chapterNumber = entity.getChapterNumber();
		referenceNumber = entity.getReferenceNumber();
		name = entity.getName();
		description = entity.getDescription();

//		sections = Optional.ofNullable(entity.getSections()).orElse(new HashSet<>()).stream().map(GDPRArticleChapterSectionDTO::new).collect(Collectors.toList());
	}
}
