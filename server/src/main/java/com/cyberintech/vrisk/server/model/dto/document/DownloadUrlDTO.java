package com.cyberintech.vrisk.server.model.dto.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Document DTO Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-05-06
 */
@Setter
@Getter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DownloadUrlDTO {

	@Schema
	private Long id;

	@Schema
	private String url;

	@Schema
	private String token;

	@Schema
	private String documentUid;

	@Schema
	private String remotePath;

	/**
	 * Default constructor
	 */
	public DownloadUrlDTO() {
	}

	/**
	 * Parametrized constructor
	 */
	public DownloadUrlDTO(String url, String token, String remotePath, String documentUid) {
		this.url = url;
		this.token = token;
		this.remotePath = remotePath;
		this.documentUid = documentUid;
	}

}
