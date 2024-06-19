package com.cyberintech.vrisk.server.model.dto.tasks;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.TaskPriorityType;
import com.cyberintech.vrisk.server.model.jpa.entity.Tasks;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

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
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class TaskViewDTO extends DTOWithMetaData<Tasks> {

	@Schema
	private Long id;

	@Schema
	private TaskPriorityType priority;

	private BusinessUnitRefDTO businessUnit;

	@Schema
	private String name;

	@Schema
	private String taskNotes;

	@Schema
	private Double status;

	@Schema
	private Double estimatedHours;

	@Schema
	private Double actualHours;

	@Schema
	private Date estimatedStartDate;

	@Schema
	private Date actualStartDate;

	@Schema
	private Date estimatedEndDate;

	@Schema
	private Date actualEndDate;

	@Schema
	private UserRefDTO taskManager;

	@Schema
	private UserRefDTO taskAssignee;

	@Schema
	private ProjectDTO project;

	@Schema
	private TaskCategoryDTO taskCategory;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public TaskViewDTO(Tasks entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Tasks entity) {
		// super.fromEntity(entity);
		id = entity.getId();
		name = entity.getName();
		priority = entity.getPriority();
		taskNotes = entity.getTaskNotes();
		status = entity.getStatus();
		estimatedHours = entity.getEstimatedHours();
		actualHours = entity.getActualHours();
		estimatedStartDate = entity.getEstimatedStartDate();
		actualStartDate = entity.getActualStartDate();
		estimatedEndDate = entity.getEstimatedEndDate();
		actualEndDate = entity.getActualEndDate();

		if (entity.getBusinessUnit() != null) {
			businessUnit = new BusinessUnitRefDTO(entity.getBusinessUnit());
		}

		if (entity.getTaskManager() != null) {
			taskManager = new UserRefDTO(entity.getTaskManager());
		}

		if (entity.getTaskAssignee() != null) {
			taskAssignee = new UserRefDTO(entity.getTaskAssignee());
		}

		if (entity.getProject() != null) {
			project = new ProjectDTO(entity.getProject());
		}

		if (entity.getTaskCategory() != null) {
			taskCategory = new TaskCategoryDTO(entity.getTaskCategory());
		}
	}
}
