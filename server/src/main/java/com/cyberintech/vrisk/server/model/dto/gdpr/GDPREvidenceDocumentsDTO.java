package com.cyberintech.vrisk.server.model.dto.gdpr;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.GDPREvidenceDocuments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * GDPR Evidence Documents DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-11-11
 */
@Setter
@Getter
@NoArgsConstructor
public class GDPREvidenceDocumentsDTO extends DTOBase<GDPREvidenceDocuments> {

	@Schema
	private Long id;

	@Schema
	private String documentType;

	@Schema
	private String name;

	@Schema
	private String templateLink;

	@Schema
	private List<GDPRArticleItemDTO> articles;

	@Schema
	private List<DocumentDTO> documents;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public GDPREvidenceDocumentsDTO(GDPREvidenceDocuments entity) {
		super(entity);
	}

	@Override
	public void fromEntity(GDPREvidenceDocuments entity) {
//		super.fromEntity(entity);

		this.id = entity.getId();
		this.name = entity.getName();
		this.documentType = entity.getDocumentType();
		this.templateLink = entity.getTemplateLink();

		articles = Optional.ofNullable(entity.getArticles()).orElse(new HashSet<>()).stream().map(GDPRArticleItemDTO::new).collect(Collectors.toList());
		documents = Optional.ofNullable(entity.getDocuments()).orElse(new HashSet<>()).stream().map(DocumentDTO::new).collect(Collectors.toList());
	}
}
