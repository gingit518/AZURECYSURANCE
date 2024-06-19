package com.cyberintech.vrisk.server.model.dto.policy;


import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.assessments.AssessmentTypeViewDTO;
import com.cyberintech.vrisk.server.model.dto.assessments.SecurityRequirementDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.gdpr.GDPRArticleItemDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.entity.Policies;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Policy Reference Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.0
 * @since    2022-10-13
 */
@SuppressWarnings("serial")
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class PolicyRefDTO extends DTOBase<Policies> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String version;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public PolicyRefDTO(Policies entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Policies entity) {
		//	super.fromEntity(entity);
		id = entity.getId();
		name = entity.getName();
		version = entity.getVersion();
	}
}
