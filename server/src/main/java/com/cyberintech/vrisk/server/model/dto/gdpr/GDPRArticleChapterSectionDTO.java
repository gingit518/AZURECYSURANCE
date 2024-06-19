package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPRArticleChapterSection;
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
 * GDPR Article Chapter Section DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-24
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPRArticleChapterSectionDTO extends DTOBase<GDPRArticleChapterSection> {

	@Schema
	private Long id;

	@Schema
	private String referenceNumber;

	@Schema
	private Long sectionNumber;

	@Schema
	private String name;

	@Schema
	private String description;

	// @Schema
	// private List<GDPRArticleItemDTO> items = new ArrayList<>();

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPRArticleChapterSectionDTO(GDPRArticleChapterSection entity) {
		super(entity);
	}

	@Override
	public void fromEntity(GDPRArticleChapterSection entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		referenceNumber = entity.getReferenceNumber();
		sectionNumber = entity.getSectionNumber();
		name = entity.getName();
		description = entity.getDescription();

		// items = Optional.ofNullable(entity.getItems()).orElse(new HashSet<>()).stream().map(GDPRArticleItemDTO::new).collect(Collectors.toList());
	}
}
