package com.cyberintech.vrisk.server.model.dto.budget;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.role.RoleListDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskBudgetViewDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.FixedOperationalCosts;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * Fixed Operational Costs View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-06-03
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "task"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class FixedOperationalCostRefDTO extends DTOBase<FixedOperationalCosts> {

	@Schema
	private Long id;

	@Schema
	private CyberRoleDTO cyberRole;

	@Schema
	private TaskBudgetViewDTO task;

	@Schema
	private Double totalCosts;

	@Schema
	private String comments;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public FixedOperationalCostRefDTO(FixedOperationalCosts entity) {
		super(entity);
	}

	@Override
	public void fromEntity(FixedOperationalCosts entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		totalCosts = entity.getTotalCosts();
		comments = entity.getComments();

		if (entity.getCyberRole() != null) {
			cyberRole = new CyberRoleDTO(entity.getCyberRole());
		}

		if (entity.getTask() != null) {
			task = new TaskBudgetViewDTO(entity.getTask());
		}

	}
}
