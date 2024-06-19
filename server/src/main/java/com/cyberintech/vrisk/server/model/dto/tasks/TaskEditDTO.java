package com.cyberintech.vrisk.server.model.dto.tasks;

import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Tasks;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Task Edit Entity Definition
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
public class TaskEditDTO extends TaskViewDTO {

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TaskEditDTO(Tasks entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Tasks entity) {
		super.fromEntity(entity);
	}
}
