package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO to search GDPR Organization Article Status by Its Data
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-20
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPRArticleStatusSearchDTO {

	@Schema
	private Long id;

	@Schema
	private ItemViewDTO<GDPRArticleItemDTO> article;

	@Schema
	private ItemViewDTO<GDPRArticleParagraphDTO> paragraph;

	public static GDPRArticleStatusSearchDTO of(GDPRArticleStatusDTO  status) {
		GDPRArticleStatusSearchDTO result = new GDPRArticleStatusSearchDTO();
		result.setArticle(new ItemViewDTO<>(status.getArticle()));
		if (status.getParagraph() != null && status.getParagraph().getId() != null) result.setParagraph(new ItemViewDTO<>(status.getParagraph()));
		if (status.getId() != null) result.setId(status.getId());

		return result;
	}

}
