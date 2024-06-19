package com.cyberintech.vrisk.server.model.dto.document;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Documents;
import com.cyberintech.vrisk.server.service.DocumentService;
import com.cyberintech.vrisk.server.util.BeanUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * Document DTO Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-10-03
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "fileName", "fileType"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Slf4j
public class DocumentDTO extends DTOBase<Documents> {

	@Schema
	private Long id;

	@Schema
	private String fileType;

	@Schema
	private String fileName;

	private String documentUid;

	private String remotePath;

	private String url;

	@Schema
	private Double fileSize;

	@Schema
	private Date createdAt;

	@Schema
	private String downloadUrl;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public DocumentDTO(Documents entity) {
		super(entity);
	}

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public DocumentDTO(Documents entity, Boolean buildDownloadLink) {
		super(entity);

		// Build image link
		if (Boolean.TRUE.equals(buildDownloadLink)) {
			try {
				url = BeanUtil.getBean(DocumentService.class).buildStorageUrl(this);
				downloadUrl = url;
			} catch (Exception exception) {
				log.warn("## Failed to get URL for the user logo URL. " + exception.getMessage());
			}
		}
	}

	@Override
	public void fromEntity(Documents entity) {
		super.fromEntity(entity);
	}

	@JsonIgnore
	@Hidden
	public String getDocumentUid() {
		return documentUid;
	}

	@JsonIgnore
	@Hidden
	public String getRemotePath() {
		return remotePath;
	}
}
