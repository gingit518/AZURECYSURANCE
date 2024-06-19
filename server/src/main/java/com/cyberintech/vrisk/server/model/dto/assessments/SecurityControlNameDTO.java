package com.cyberintech.vrisk.server.model.dto.assessments;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.entity.SecurityControlNames;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Security Control Name Entity Definition
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
public class SecurityControlNameDTO extends DTOBase<SecurityControlNames> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private SecurityControlFamilyDTO securityControlFamily;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SecurityControlNameDTO(SecurityControlNames entity) {
		super(entity);
	}

	@Override
	public void fromEntity(SecurityControlNames entity) {
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();

		if (entity.getSecurityControlFamily() != null) {
			securityControlFamily = new SecurityControlFamilyDTO(entity.getSecurityControlFamily());
		}
	}
}
