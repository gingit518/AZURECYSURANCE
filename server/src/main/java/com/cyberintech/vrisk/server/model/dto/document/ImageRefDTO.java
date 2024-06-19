package com.cyberintech.vrisk.server.model.dto.document;

import com.cyberintech.vrisk.server.model.jpa.entity.Documents;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Image Reference DTO Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-11-30
 */
@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageRefDTO {

	@Schema
	private Long id;

	@Schema
	private String url;

	@Schema
	private String documentUid;

	@Schema
	private String remotePath;

	/**
	 * Default constructor
	 */
	public ImageRefDTO() {
	}

	/**
	 * Parametrized constructor
	 */
	public ImageRefDTO(String url) {
		this.url = url;
	}

	/**
	 * Parametrized constructor
	 */
	public ImageRefDTO(String url, String remotePath, String documentUid) {
		this.url = url;
		this.remotePath = remotePath;
		this.documentUid = documentUid;
	}

	/**
	 * Static constructor for Image object
	 *
	 * @param document
	 * @return
	 */
	public static ImageRefDTO of(Documents document) {
		if (document == null) return null;

		ImageRefDTO result = new ImageRefDTO(null, document.getRemotePath(), document.getDocumentUid());
		result.setId(document.getId());

		return result;
	}

}
