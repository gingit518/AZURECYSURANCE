package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.SecurityControlFamilies;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Security Control Family Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-01-23
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class SecurityControlFamilyDTO extends DTOBase<SecurityControlFamilies> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SecurityControlFamilyDTO(SecurityControlFamilies entity) {
		super(entity);
	}

	@Override
	public void fromEntity(SecurityControlFamilies entity) {
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
	}
}
