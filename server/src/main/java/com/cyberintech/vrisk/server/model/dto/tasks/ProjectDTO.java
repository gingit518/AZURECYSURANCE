package com.cyberintech.vrisk.server.model.dto.tasks;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.jpa.entity.Projects;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Project View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-09
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class ProjectDTO extends DTOWithMetaData<Projects> {

	@Schema
	private Long id;

	@Schema
	private String name;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public ProjectDTO(Projects entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Projects entity) {
		super.fromEntity(entity);
	}
}
