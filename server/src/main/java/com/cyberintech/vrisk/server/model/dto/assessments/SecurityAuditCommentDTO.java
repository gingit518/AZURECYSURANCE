package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskViewDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Security Audit Comments DTO Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.0
 * @since    2020-09-27
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "comment", "createdAt"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class SecurityAuditCommentDTO extends DTOBase<SecurityAuditComments> {

	@Schema
	private Long id;

	@Schema
	private String comment;

	@Schema
	private Date createdAt;

	@Schema
	private Date updatedAt;

	@Schema
	private UserRefDTO createdBy;

	@Schema
	private UserRefDTO updatedBy;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SecurityAuditCommentDTO(SecurityAuditComments entity) {
		super(entity);
	}

	@Override
	public void fromEntity(SecurityAuditComments entity) {
		id = entity.getId();
		comment = entity.getComment();

		setCreatedAt(entity.getCreatedAt());
		setUpdatedAt(entity.getUpdatedAt());
		Users createdBy = entity.getCreatedBy();
		if (createdBy != null) {
			setCreatedBy(new UserRefDTO(createdBy));
		}
		Users updatedBy = entity.getUpdatedBy();
		if (updatedBy != null) {
			setUpdatedBy(new UserRefDTO(updatedBy));
		}
	}
}
