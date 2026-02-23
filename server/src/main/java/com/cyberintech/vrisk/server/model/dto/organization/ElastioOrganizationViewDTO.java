package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * Organization View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-02-22
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElastioOrganizationViewDTO extends DTOBase<Organizations> {

	private Long id;
	private String name;
	private String description;
	private Double averageRevenue;
	private Double qualThreshold;

	@Schema(type = "string", pattern = "yyyy-MM-dd HH:mm:ss", example = "2025-12-31 01:00:00")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createdAt;

	@Schema(type = "string", pattern = "yyyy-MM-dd HH:mm:ss", example = "2025-12-31 01:00:00")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date updatedAt;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ElastioOrganizationViewDTO(Organizations entity) {
		super(entity);
	}

	/**
	 * Converts from entity to DTO
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(Organizations entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		averageRevenue = entity.getAverageRevenue();
		qualThreshold = entity.getQualThreshold();

		createdAt = entity.getCreatedAt();
		updatedAt = entity.getUpdatedAt();
	}
}
