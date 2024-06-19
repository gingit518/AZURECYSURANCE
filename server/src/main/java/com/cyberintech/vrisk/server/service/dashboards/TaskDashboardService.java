package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.DashboardType;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.domains.TaskPriorityType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Projects;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.model.jpa.entity.Tasks;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.ProjectRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelRepository;
import com.cyberintech.vrisk.server.repository.jpa.TaskRepository;
import com.cyberintech.vrisk.server.service.TaskService;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class TaskDashboardService extends DashboardServiceBase {

	public static final Long TASKS_STATUSES = 12510L;

	private static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TaskService taskService;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private ProjectRepository projectRepository;

	/**
	 * Get Task Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getTaskDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {

		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_TASK, clientMessage.getMessage(SLCT.DASHBOARDS$TASK$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$TASK$DESCRIPTION), DashboardType.None);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_CISO(clientMessage);

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<Tasks> tasks = taskRepository.getListByOrganization(organization.getId());

		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO(12500L, clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$ITEM_DESCRIPTION));
		dashboard.getSections().add(section);

		// Create breadcrumbs
		section.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_TASK_STATUS", "DASHBOARDS$TASK$TASK_SUMMARY$ITEM_NAME", "").getBreadcrumbs());

		// Initialize Tasks Summary Scores
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(TASKS_STATUSES, clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$ITEM_NAME));
		dashboardItem.getGridHeaders().add(
			Arrays.asList(
//				DashboardDataItemHeaderDTO.of("Task Number", 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$TASK_NAME_HEADER), 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$TASK_CATEGORY_HEADER), 1L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$PROJECT_HEADER), 2L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$TASK_MANAGER_HEADER), 3L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$TASK_ASSIGNEE_HEADER), 4L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$STATUS_HEADER), 5L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$PRIORITY_HEADER), 6L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$EST_HOURS_HEADER), 7L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$EST_START_HEADER), 8L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$EST_END_HEADER), 9L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$ACTUAL_START_HEADER), 10L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$ACTUAL_END_HEADER), 11L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$BUDGET_HEADER), 12L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$TASK$TASK_SUMMARY$TASK_STATUSES$COMMENTS_HEADER), 13L)
			)
		);
		section.getDashboardItems().add(dashboardItem);

		// Fill Dashboard with Actual Data
		for (Tasks task: tasks) {
			List<DashboardDataItemDTO> itemsList = new ArrayList<>();

			String taskName = StringUtils.isNotEmpty(task.getName()) ? task.getName() : "";
			DashboardDataItemDTO taskNameCell = sI(taskName).applyLink(DashboardLinkDTO.of("/private/tasks/edit/" + task.getId()));

			String taskCategory = task.getTaskCategory() != null && StringUtils.isNotEmpty(task.getTaskCategory().getName()) ? task.getTaskCategory().getName() : "";
			DashboardDataItemDTO taskCategoryCell = sI(taskCategory);

			String project = task.getProject() != null && StringUtils.isNotEmpty(task.getProject().getName()) ? task.getProject().getName() : "";
			DashboardDataItemDTO projectCell = sI(project);

			String taskManager = task.getTaskManager() != null && StringUtils.isNotEmpty(task.getTaskManager().getFullName()) ? task.getTaskManager().getFullName() : "";
			DashboardDataItemDTO taskManagerCell = sI(taskManager);

			String taskAssignee = task.getTaskAssignee() != null && StringUtils.isNotEmpty(task.getTaskAssignee().getFullName()) ? task.getTaskAssignee().getFullName() : "";
			DashboardDataItemDTO taskAssigneeCell = sI(taskAssignee);

			String status = task.getStatus() != null ? task.getStatus().toString() + "%" : "";
			DashboardDataItemDTO statusCell = sI(status);

			String priority = task.getPriority() != null ? task.getPriority().toString() : "";
			DashboardDataItemDTO priorityCell = sI(priority).applyColor(getPriorityColor(task.getPriority()));

			String estHours = task.getEstimatedHours() != null ? task.getEstimatedHours().toString() : "";
			DashboardDataItemDTO estHoursCell = sI(estHours);

			String estStartDate = task.getEstimatedStartDate() != null ? dateFormat.format(task.getEstimatedStartDate()) : "";
			DashboardDataItemDTO estStartDateCell = sI(estStartDate);

			String estEndDate = task.getEstimatedEndDate() != null ? dateFormat.format(task.getEstimatedEndDate()) : "";
			DashboardDataItemDTO estEndDateCell = sI(estEndDate);

			String actualStartDate = task.getActualStartDate() != null ? dateFormat.format(task.getActualStartDate()) : "";
			DashboardDataItemDTO actualStartDateCell = sI(actualStartDate);

			String actualEndDate = task.getActualEndDate() != null ? dateFormat.format(task.getActualEndDate()) : "";
			DashboardDataItemDTO actualEndDateCell = sI(actualEndDate);

			// TODO 29.04.2020	[HARDCODED]	provide Task Dashboard with correct Budget value/link
			String budget = clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE);
			DashboardDataItemDTO budgetCell = sI(budget);

			String comments = StringUtils.isNotEmpty(task.getTaskNotes()) ? task.getTaskNotes() : "";
			DashboardDataItemDTO commentsCell = sI(comments);

			itemsList.addAll(
				Arrays.asList(
					taskNameCell,
					taskCategoryCell,
					projectCell,
					taskManagerCell,
					taskAssigneeCell,
					statusCell,
					priorityCell,
					estHoursCell,
					estStartDateCell,
					estEndDateCell,
					actualStartDateCell,
					actualEndDateCell,
					budgetCell,
					commentsCell
				)
			);

			dashboardItem.getGridItems().add(itemsList);
		}

		return dashboard;
	}

	/**
	 * Get Resources Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getResourcesDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {

		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_RESOURCES, clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$DESCRIPTION), DashboardType.None);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_CISO(clientMessage);

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<Tasks> tasks = taskRepository.getListByOrganization(organization.getId());

		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO();
		dashboard.getSections().add(section);

		// Create breadcrumbs
		section.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARDS_RESOURCES", "DASHBOARDS$RESOURCES$NAME", "").getBreadcrumbs());

		// Initialize Resources Scores
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(126000L, "");
		dashboardItem.getGridHeaders().add(
			Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$RESOURCES_SCORES$EMPLOYEE_NAME_HEADER), 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$RESOURCES_SCORES$PROJECT_NAME_HEADER), 1L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$RESOURCES_SCORES$TASK_NAME_HEADER), 2L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$RESOURCES_SCORES$EST_HOURS_HEADER), 3L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$RESOURCES_SCORES$ACTUAL_HOURS_HEADER), 4L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$RESOURCES_SCORES$STATUS_HEADER), 5L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$RESOURCES_SCORES$APPROVAL_HEADER), 6L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$RESOURCES_SCORES$START_DATE_HEADER), 7L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$RESOURCES_SCORES$END_DATE_HEADER), 8L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$RESOURCES$RESOURCES_SCORES$COMMENTS_HEADER), 9L)
			)
		);
		section.getDashboardItems().add(dashboardItem);

		for (Tasks task: tasks) {
			List<DashboardDataItemDTO> itemsList = new ArrayList<>();

			String employeeName = task.getTaskAssignee() != null && StringUtils.isNotEmpty(task.getTaskAssignee().getFullName()) ? task.getTaskAssignee().getFullName() : "";
			DashboardDataItemDTO employeeNameCell = sI(employeeName).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

			String projectName = task.getProject() != null && StringUtils.isNotEmpty(task.getProject().getName()) ? task.getProject().getName() : "";
			DashboardDataItemDTO projectNameCell = sI(projectName);
			if (task.getProject() != null) {
				projectNameCell.applyDrilldown(DashboardDataItemDrilldownDTO.of(task.getProject()));
			}

			String taskName = StringUtils.isNotEmpty(task.getName()) ? task.getName() : "";
			DashboardDataItemDTO taskNameCell = sI(taskName).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

			String estHours = task.getEstimatedHours() != null ? task.getEstimatedHours().toString() : "";
			DashboardDataItemDTO estHoursCell = sI(estHours).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

			String actualHours = task.getActualHours() != null ? task.getEstimatedHours().toString() : "";
			DashboardDataItemDTO actualHoursCell = sI(actualHours).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

			String status = task.getStatus() != null ? task.getStatus().toString() + "%" : "";
			DashboardDataItemDTO statusCell = sI(status).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

			String approval = task.getTaskManager() != null && StringUtils.isNotEmpty(task.getTaskManager().getFullName()) ? task.getTaskManager().getFullName() : "";
			DashboardDataItemDTO approvalCell = sI(approval).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

			String startDate = task.getActualStartDate() != null ? dateFormat.format(task.getActualStartDate()) : "";
			DashboardDataItemDTO startDateCell = sI(startDate).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

			String endDate = task.getActualEndDate() != null ? dateFormat.format(task.getActualEndDate()) : "";
			DashboardDataItemDTO endDateCell = sI(endDate).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

			String comments = StringUtils.isNotEmpty(task.getTaskNotes()) ? task.getTaskNotes() : "";
			DashboardDataItemDTO commentsCell = sI(comments);

			itemsList.addAll(
				Arrays.asList(
					employeeNameCell,
					projectNameCell,
					taskNameCell,
					estHoursCell,
					actualHoursCell,
					statusCell,
					approvalCell,
					startDateCell,
					endDateCell,
					commentsCell
				)
			);

			dashboardItem.getGridItems().add(itemsList);
		}

		return dashboard;
	}

	/**
	 * Build Task drilldown
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	public void buildTaskDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		// Prepare task data
		Long taskId = Long.valueOf(drilldown.getParams().get("task"));
		Tasks task = taskRepository.findById(taskId).get();

		String taskName = StringUtils.isNotEmpty(task.getName()) ? task.getName() : "";
		String taskPriority = task.getPriority() != null ? task.getPriority().toString() : "";
		String taskBusinessUnitName = task.getBusinessUnit() != null && StringUtils.isNotEmpty(task.getBusinessUnit().getName()) ? task.getBusinessUnit().getName() : "";
		String taskProjectName = task.getProject() != null && StringUtils.isNotEmpty(task.getProject().getName()) ? task.getProject().getName() : "";
		String taskCategotyName = task.getTaskCategory() != null && StringUtils.isNotEmpty(task.getTaskCategory().getName()) ? task.getTaskCategory().getName() : "";
		String taskManagerName = task.getTaskManager() != null && StringUtils.isNotEmpty(task.getTaskManager().getFullName()) ? task.getTaskManager().getFullName() : "";
		String taskAssigneeName = task.getTaskAssignee() != null && StringUtils.isNotEmpty(task.getTaskAssignee().getFullName()) ? task.getTaskAssignee().getFullName() : "";
		String taskNotes = StringUtils.isNotEmpty(task.getTaskNotes()) ? task.getTaskNotes() : "";
		String taskStatus = task.getStatus() != null ? task.getStatus().toString() + "%" : "0.0%";
		String taskEstimatedHours = task.getEstimatedHours() != null ? task.getEstimatedHours().toString() + "h" : "";
		String taskActualHours = task.getActualHours() != null ? task.getActualHours().toString() + "h" : "";
		String taskEstimatedStartDate = task.getEstimatedStartDate() != null ? dateFormat.format(task.getEstimatedStartDate()) : "";
		String taskActualStartDate = task.getActualStartDate() != null ? dateFormat.format(task.getActualStartDate()) : "";
		String taskEstimatedEndDate = task.getEstimatedEndDate() != null ? dateFormat.format(task.getEstimatedEndDate()) : "";
		String taskActualEndDate = task.getActualEndDate() != null ? dateFormat.format(task.getActualEndDate()) : "";
		String linkageType = task.getLinkageType() != null ? task.getLinkageType().getName() : "";

		// Provide drilldown with actual data
		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$ITEM_NAME), taskName));

		// Initialize Task Summary
		DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(1001L, clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$ITEM_NAME));
		dashboardItem1.getGridItems().addAll(
			Arrays.asList(
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$TASK_NAME_HEADER)).applyHeader(true),	 	sI(taskName)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$BUSINESS_UNIT_HEADER)).applyHeader(true),	sI(taskBusinessUnitName)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$PROJECT_HEADER)).applyHeader(true),		sI(taskProjectName)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$CATEGORY_HEADER)).applyHeader(true),		sI(taskCategotyName)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$LINKAGE_TYPE_HEADER)).applyHeader(true),	sI(linkageType)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$STATUS_HEADER)).applyHeader(true),			sI(taskStatus)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$PRIORITY_HEADER)).applyHeader(true),		sI(taskPriority).applyColor(getPriorityColor(task.getPriority()))),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$TASK_MANAGER_HEADER)).applyHeader(true),	sI(taskManagerName)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$TASK_ASSIGNEE_HEADER)).applyHeader(true),	sI(taskAssigneeName)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$EST_HOURS_HEADER)).applyHeader(true),		sI(taskEstimatedHours)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$ACTUAL_HOURS_HEADER)).applyHeader(true),	sI(taskActualHours)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$EST_START_HEADER)).applyHeader(true),		sI(taskEstimatedStartDate)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$ACTUAL_START_HEADER)).applyHeader(true),	sI(taskActualStartDate)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$EST_END_HEADER)).applyHeader(true),		sI(taskEstimatedEndDate)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$ACTUAL_END_HEADER)).applyHeader(true),		sI(taskActualEndDate)),
				Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$TASK$TASK_SUMMARY$NOTES_HEADER)).applyHeader(true),			sI(taskNotes))
			)
		);

		section1.getDashboardItems().add(dashboardItem1);
	}

	/**
	 * Build Project drilldown
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	public void buildProjectDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		// Prepare project data
		Long projectId = Long.valueOf(drilldown.getParams().get("project"));
		Projects project = projectRepository.findById(projectId).get();
		List<Tasks> projectTasks = taskRepository.getListByOrganizationAndProject(project.getOrganizationId(), projectId);

		String projectName = StringUtils.isNotEmpty(project.getName()) ? project.getName() : "";

		// Provide drilldown with actual data
		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$PROJECT$ITEM_NAME), projectName));

		if (projectTasks.size() > 0) {
			// Initialize Project Tasks Summary
			DashboardTableItemDTO dashboardItem1 = buildTasksSummary(projectTasks, clientMessage.getMessage(SLCT.DRILLDOWNS$PROJECT$TASKS_SUMMARY$ITEM_NAME));

			section1.getDashboardItems().add(dashboardItem1);
		}
	}

	public DashboardTableItemDTO buildTasksSummary(Collection<Tasks> tasks, String name) {
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(1001L, name);
		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$PROJECT$TASKS_SUMMARY$TASK_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$PROJECT$TASKS_SUMMARY$CATEGORY_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$PROJECT$TASKS_SUMMARY$STATUS_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$PROJECT$TASKS_SUMMARY$PRIORITY_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$PROJECT$TASKS_SUMMARY$MANAGER_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$PROJECT$TASKS_SUMMARY$ASSIGNEE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$PROJECT$TASKS_SUMMARY$EST_START_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$PROJECT$TASKS_SUMMARY$EST_END_HEADER)
		));
		tasks.stream().forEach(task -> {
			List<DashboardDataItemDTO> itemsList = new ArrayList<>();

			String taskName = StringUtils.isNotEmpty(task.getName()) ? task.getName() : "";
			String taskCategotyName = task.getTaskCategory() != null && StringUtils.isNotEmpty(task.getTaskCategory().getName()) ? task.getTaskCategory().getName() : "";
			String taskStatus = task.getStatus() != null ? task.getStatus().toString() + "%" : "0.0%";
			String taskPriority = task.getPriority() != null ? task.getPriority().toString() : "";
			String taskManagerName = task.getTaskManager() != null && StringUtils.isNotEmpty(task.getTaskManager().getFullName()) ? task.getTaskManager().getFullName() : "";
			String taskAssigneeName = task.getTaskAssignee() != null && StringUtils.isNotEmpty(task.getTaskAssignee().getFullName()) ? task.getTaskAssignee().getFullName() : "";
			String taskEstimatedStartDate = task.getEstimatedStartDate() != null ? dateFormat.format(task.getEstimatedStartDate()) : "";
			String taskEstimatedEndDate = task.getEstimatedEndDate() != null ? dateFormat.format(task.getEstimatedEndDate()) : "";

			itemsList.addAll(Arrays.asList(
				sI(taskName).applyDrilldown(DashboardDataItemDrilldownDTO.of(task)),
				sI(taskCategotyName).applyDrilldown(DashboardDataItemDrilldownDTO.of(task)),
				sI(taskStatus).applyDrilldown(DashboardDataItemDrilldownDTO.of(task)),
				sI(taskPriority).applyColor(getPriorityColor(task.getPriority())),
				sI(taskManagerName).applyDrilldown(DashboardDataItemDrilldownDTO.of(task)),
				sI(taskAssigneeName).applyDrilldown(DashboardDataItemDrilldownDTO.of(task)),
				sI(taskEstimatedStartDate).applyDrilldown(DashboardDataItemDrilldownDTO.of(task)),
				sI(taskEstimatedEndDate).applyDrilldown(DashboardDataItemDrilldownDTO.of(task))
			));

			dashboardItem.getGridItems().add(itemsList);
		});

		return dashboardItem;
	}

	/**
	 * Helper method to get
	 * @param priority
	 * @return
	 */
	private String getPriorityColor(TaskPriorityType priority) {

		if (priority != null) {
			switch (priority) {
				case LOW:
					return "#00ff00";
				case MEDIUM:
					return "#ffd400";
				case HIGH:
					return "#ffae40";
				case URGENT:
					return "#ff0000";
			}
		}

		return null;
	}

}
