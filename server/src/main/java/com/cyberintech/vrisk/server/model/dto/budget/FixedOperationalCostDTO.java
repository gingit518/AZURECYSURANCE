package com.cyberintech.vrisk.server.model.dto.budget;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.role.RoleListDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskBudgetViewDTO;
import com.cyberintech.vrisk.server.model.dto.tasks.TaskViewDTO;
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
@ToString(of = {"id", "role", "user", "businessUnit", "rateType"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class FixedOperationalCostDTO extends DTOBase<FixedOperationalCosts> {

	@Schema
	private Long id;

	@Schema
	private RoleListDTO role;

	@Schema
	private CyberRoleDTO cyberRole;

	@Schema
	private UserRefDTO user;

	@Schema
	private BusinessUnitRefDTO businessUnit;

	@Schema
	private TaskBudgetViewDTO task;

	@Schema
	private Double rate;

	@Schema
	private Double percentOfTime;

	@Schema
	private Double percentOfBudget;

	@Schema
	private Double totalCosts;

	@Schema
	private String comments;

	@Schema
	private Date costDate;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public FixedOperationalCostDTO(FixedOperationalCosts entity) {
		super(entity);
	}

	@Override
	public void fromEntity(FixedOperationalCosts entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		rate = entity.getRate();
		percentOfTime = entity.getPercentOfTime();
		percentOfBudget = entity.getPercentOfBudget();
		totalCosts = entity.getTotalCosts();
		comments = entity.getComments();
		costDate = entity.getCostDate();

		if (entity.getCyberRole() != null) {
			cyberRole = new CyberRoleDTO(entity.getCyberRole());
		}

		if (entity.getUser() != null) {
			user = new UserRefDTO(entity.getUser());
		}

		if (entity.getBusinessUnit() != null) {
			businessUnit = new BusinessUnitRefDTO(entity.getBusinessUnit());
		}

		if (entity.getTask() != null) {
			task = new TaskBudgetViewDTO(entity.getTask());
		}

	}
}
