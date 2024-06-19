package com.cyberintech.vrisk.server.model.dto.tasks;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.jpa.entity.TaskCategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Task Category View Entity Definition
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
public class TaskCategoryDTO extends DTOWithMetaData<TaskCategories> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private Boolean readOnly = false;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TaskCategoryDTO(TaskCategories entity) {
		super(entity);
	}

	@Override
	public void fromEntity(TaskCategories entity) {
		super.fromEntity(entity);

		readOnly = entity.getOrganizationId() == null;
	}
}
