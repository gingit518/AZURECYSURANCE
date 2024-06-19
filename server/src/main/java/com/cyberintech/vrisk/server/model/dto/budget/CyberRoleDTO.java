package com.cyberintech.vrisk.server.model.dto.budget;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.jpa.entity.CyberRoles;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Cyber Role View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-10
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class CyberRoleDTO extends DTOWithMetaData<CyberRoles> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private boolean readOnly;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public CyberRoleDTO(CyberRoles entity) {
		super(entity);
	}

	@Override
	public void fromEntity(CyberRoles entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();

		readOnly = entity.getOrganizationId() == null;
	}
}
