package com.cyberintech.vrisk.server.model.dto.systems;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.jpa.domains.DeploymentType;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import com.cyberintech.vrisk.server.model.jpa.entity.Systems;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Systems View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-27
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class SystemRefDTO extends DTOBase<Systems> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private SystemStatus systemStatus;

	@Schema
	private SystemType systemType;

	@Schema
	private DeploymentType deploymentType;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public SystemRefDTO(Systems entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Systems entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		systemStatus = entity.getSystemStatus();
		systemType = entity.getSystemType();
		deploymentType = entity.getDeploymentType();
	}

	/**
	 * Static constructor
	 *
	 * @param id
	 * @param name
	 * @return
	 */
	public static SystemRefDTO of(Long id, String name) {
		SystemRefDTO result = new SystemRefDTO();
		result.setId(id);
		result.setName(name);

		return result;
	}
}
