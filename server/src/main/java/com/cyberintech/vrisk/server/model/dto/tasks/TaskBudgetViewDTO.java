package com.cyberintech.vrisk.server.model.dto.tasks;

import com.cyberintech.vrisk.server.model.dto.user.UserHRDataDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.Tasks;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Task View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-12-09
 */
@Setter
@Getter
@NoArgsConstructor
public class TaskBudgetViewDTO extends TaskViewDTO {

	@Schema
	private UserHRDataDTO taskAssigneeDetails;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TaskBudgetViewDTO(Tasks entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Tasks entity) {
		super.fromEntity(entity);

		if (entity.getTaskAssignee() != null) {
			taskAssigneeDetails = new UserHRDataDTO(entity.getTaskAssignee());
		}
	}
}
