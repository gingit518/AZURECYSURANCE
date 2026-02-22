package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.jpa.entity.ApiKeys;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * API Key DTO Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@uventex.com>
 * @version  0.1.1
 * @since    2025-08-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIKeyDTO extends APIKeyViewDTO {

    @Schema
	private String apiKeyPublic;

    @Schema
	private String apiKeyPrivate;

	public APIKeyDTO(ApiKeys entity) {
		super(entity);
	}

	@Override
	public void fromEntity(ApiKeys entity) {
		super.fromEntity(entity);

		this.setApiKeyPublic(entity.getApiKeyPublic());
		this.setApiKeyPrivate(entity.getApiKeyPrivate());
	}
}
