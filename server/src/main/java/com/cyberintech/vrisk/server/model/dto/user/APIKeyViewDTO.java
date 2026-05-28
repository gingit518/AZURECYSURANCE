package com.cyberintech.vrisk.server.model.dto.user;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.ApiKeys;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
public class APIKeyViewDTO extends DTOBase<ApiKeys> {

    @Schema
    private Long id;

    @Schema
    private UserRefDTO user;

    @Schema
    private Long organizationId;

    @Schema(type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2025-12-31T01:00:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date createdAt;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
	private Date expiredAt;


    public APIKeyViewDTO() {
        super();
    }

    public APIKeyViewDTO(ApiKeys entity) {
        super(entity);
    }

	@Override
	public void fromEntity(ApiKeys entity) {
		this.setId(entity.getId());
		this.setUser(new UserRefDTO(entity.getUser()));
		this.setOrganizationId(entity.getOrganizationId());
		this.setCreatedAt(entity.getCreatedAt());
		this.setExpiredAt(entity.getExpiredAt());
	}
}
