package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentLevel;
import com.cyberintech.vrisk.server.model.jpa.domains.DashboardType;
import com.cyberintech.vrisk.server.model.jpa.domains.RelationToRequirementType;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.entity.Assessments;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.repository.jpa.AssessmentsRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelRepository;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class AssessmentDashboardService extends DashboardServiceBase {

	private DateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY");

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private AssessmentsRepository assessmentsRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private TaskDashboardService taskDashboardService;

	public DashboardDTO getAssessmentAuditDashboardDetails(Long riskModelId) {

		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_ASSESSMENTS_AUDIT, "Audit", "Audit", DashboardType.None);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.AUDIT_DASHBOARD(clientMessage);

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		List<Assessments> assessments = assessmentsRepository.getListByOrOrganizationId(organization.getId());

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(12400L, clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$ITEM_DESCRIPTION));
		dashboard.getSections().add(section1);
		buildAssessmentInfoDashboardItem(assessments, section1);

		// Create breadcrumbs
		section1.setBreadcrumbs(breadcrumbsTop.extend("AUDIT_DASHBOARD", SLCT.AUDIT$AUDIT, "").getBreadcrumbs());

		return dashboard;
	}

	public DashboardDTO getAssessment1DashboardDetails(Long riskModelId) {

		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_ASSESSMENTS, clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$DESCRIPTION), DashboardType.None);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_CISO(clientMessage).add("DASHBOARDS_ASSESSMENTS",SLCT.DASHBOARDS$ASSESSMENT$NAME,"/private/dashboards/124");

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		List<Assessments> assessments = assessmentsRepository.getListByOrOrganizationId(organization.getId());
//		List<SecurityRequirements> requirements = securityRequirementRepository.getListByOrganizationId(organization.getId());

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(12400L, clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$ITEM_DESCRIPTION));
		dashboard.getSections().add(section1);
		buildAssessmentInfoDashboardItem(assessments, section1);

		DashboardSectionDTO section2 = new DashboardSectionDTO(12401L, clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$ITEM_DESCRIPTION));
		dashboard.getSections().add(section2);

		// Create breadcrumbs
		section1.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARDS_ASSESSMENTS_1", SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$ITEM_NAME, "").getBreadcrumbs());
		section2.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARDS_ASSESSMENTS_2", SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$ITEM_NAME, "").getBreadcrumbs());

		// Initialize Assessments and Requirements Summary Scores
		DashboardDataGridItemDTO dashboardItem2 = new DashboardDataGridItemDTO(12420L, "");
		dashboardItem2.getGridHeaders().add(
			Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$ASSESSMENT_NAME_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$SECURITY_FRAMEWORK_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$REQUIREMENT_NUMBER_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$REQUIREMENT_DESCRIPTION_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$CONTROL_IMPL_STATUS_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$TASK_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$PERCENT_IN_PLACE_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$COMMENTS_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$CONTROL_ORIGINATION_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_REQUIREMENTS$TESTER_NAME_HEADER), null)
			)
		);
		section2.getDashboardItems().add(dashboardItem2);

		// TODO 05.05.2020	fill with proper data
		for (Assessments assessment: assessments) {

			// all security requirements selected
			if (assessment.getRelationToRequirementType() != null && assessment.getRelationToRequirementType().equals(RelationToRequirementType.ALL_REQUIREMENTS)) {
				List<DashboardDataItemDTO> itemsList = new ArrayList<>();

				String assessmentName = StringUtils.isNotEmpty(assessment.getName()) ? assessment.getName() : "";
				DashboardDataItemDTO assessmentNameCell = sI(assessmentName).applyDrilldown(DashboardDataItemDrilldownDTO.of(assessment));

				String frameworkName = "";
				DashboardDataItemDTO frameworkNameCell = sI(frameworkName);

				String requirementCode = clientMessage.getMessage(SLCT.DASHBOARD_VALUES$ALL);
				DashboardDataItemDTO requirementCodeCell = sI(requirementCode);

				String requirementDescription = clientMessage.getMessage(SLCT.DASHBOARD_VALUES$ALL_REQUIREMENTS_SELECTED);
				DashboardDataItemDTO requirementDescriptionCell = sI(requirementDescription);

				itemsList.addAll(Arrays.asList(assessmentNameCell, frameworkNameCell, requirementCodeCell, requirementDescriptionCell, sI(""), sI(""), sI(""), sI(""), sI(""), sI("")));
				dashboardItem2.getGridItems().add(itemsList);

			// any security requirements selected
			} else if (assessment.getRelationToRequirementType() != null && assessment.getRelationToRequirementType().equals(RelationToRequirementType.REQUIREMENTS) && assessment.getSecurityRequirements() != null) {
				AtomicInteger securityRequirementNumber = new AtomicInteger(0);

				assessment.getSecurityRequirements().stream().forEach(securityRequirement -> {
					List<DashboardDataItemDTO> itemsList = new ArrayList<>();

					String assessmentName = StringUtils.isNotEmpty(assessment.getName()) ? assessment.getName() : "";
					DashboardDataItemDTO assessmentNameCell = sI(assessmentName).applyDrilldown(DashboardDataItemDrilldownDTO.of(assessment));

					String requirementCode = StringUtils.isNotEmpty(securityRequirement.getCode()) ? securityRequirement.getCode() : "";
					DashboardDataItemDTO requirementCodeCell = sI(requirementCode);

					String requirementDescription = StringUtils.isNotEmpty(securityRequirement.getDescription()) ? securityRequirement.getDescription() : "";
					DashboardDataItemDTO requirementDescriptionCell = sI(requirementDescription);

					if (securityRequirementNumber.getAndIncrement() == 0) {
						Long securityRequirementsCount = (long) assessment.getSecurityRequirements().size();
						assessmentNameCell.applyRowspan(securityRequirementsCount);
						itemsList.addAll(Arrays.asList(
							assessmentNameCell,
							sI(""),
							requirementCodeCell,
							requirementDescriptionCell,
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI("")
						));
					} else {
						itemsList.addAll(Arrays.asList(
							sI(""),
							requirementCodeCell,
							requirementDescriptionCell,
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI("")
						));
					}
					dashboardItem2.getGridItems().add(itemsList);
				});

			// any security frameworks (assessment types) selected
			} else if (assessment.getRelationToRequirementType() != null && assessment.getRelationToRequirementType().equals(RelationToRequirementType.FRAMEWORKS) && assessment.getAssessmentTypes() != null) {
				AtomicInteger securityFrameworkNumber = new AtomicInteger(0);

				assessment.getAssessmentTypes().stream().forEach(assessmentType -> {
					List<DashboardDataItemDTO> itemsList = new ArrayList<>();

					String assessmentName = StringUtils.isNotEmpty(assessment.getName()) ? assessment.getName() : "";
					DashboardDataItemDTO assessmentNameCell = sI(assessmentName).applyDrilldown(DashboardDataItemDrilldownDTO.of(assessment));

					String frameworkName = StringUtils.isNotEmpty(assessmentType.getName()) ? assessmentType.getName() : "";
					DashboardDataItemDTO frameworkNameCell = sI(frameworkName);

					if (securityFrameworkNumber.getAndIncrement() == 0) {
						Long securityFrameworkCount = (long) assessment.getAssessmentTypes().size();
						assessmentNameCell.applyRowspan(securityFrameworkCount);
						itemsList.addAll(Arrays.asList(
							assessmentNameCell,
							frameworkNameCell,
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI("")
						));
					} else {
						itemsList.addAll(Arrays.asList(
							frameworkNameCell,
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI(""),
							sI("")
						));
					}
					dashboardItem2.getGridItems().add(itemsList);
				});

			// assessment.getRelationToRequirementType() somehow appeared to be null
			} else {
				List<DashboardDataItemDTO> itemsList = new ArrayList<>();

				String assessmentName = StringUtils.isNotEmpty(assessment.getName()) ? assessment.getName() : "";
				DashboardDataItemDTO assessmentNameCell = sI(assessmentName).applyDrilldown(DashboardDataItemDrilldownDTO.of(assessment));

				itemsList.addAll(Arrays.asList(
					assessmentNameCell,
					sI(""),
					sI(""),
					sI(""),
					sI(""),
					sI(""),
					sI(""),
					sI(""),
					sI(""),
					sI("")
				));
				dashboardItem2.getGridItems().add(itemsList);
			}
		}

		return dashboard;
	}

	private void buildAssessmentInfoDashboardItem(List<Assessments> assessments, DashboardSectionDTO section1) {
		// Initialize Assessment and Tasks Summary Scores
		DashboardDataGridItemDTO dashboardItem1 = new DashboardDataGridItemDTO(12410L, "");
		dashboardItem1.getGridHeaders().add(
			Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$SUMMARY_SCORES$ASSESSMENT_NAME_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$SUMMARY_SCORES$TYPE_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$SUMMARY_SCORES$FRAMEWORK_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$SUMMARY_SCORES$TASK_NAME_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$SUMMARY_SCORES$STATUS_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$SUMMARY_SCORES$ASSIGNEE_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$SUMMARY_SCORES$PLANNED_START_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$SUMMARY_SCORES$PLANNED_END_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$SUMMARY_SCORES$ACTUAL_START_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT$ASSESSMENTS_TASKS$SUMMARY_SCORES$ACTUAL_END_HEADER), null),
				DashboardDataItemHeaderDTO.of("", null)
			)
		);
		section1.getDashboardItems().add(dashboardItem1);

		for (Assessments assessment: assessments) {

			if (assessment.getTasks() != null && assessment.getTasks().size() > 0) {
				AtomicInteger taskNumber = new AtomicInteger(0);

				assessment.getTasks().stream().forEach(task -> {
					List<DashboardDataItemDTO> itemsList = new ArrayList<>();

					String assessmentName = StringUtils.isNotEmpty(assessment.getName()) ? assessment.getName() : "";
					DashboardDataItemDTO nameCell = sI(assessmentName).applyDrilldown(DashboardDataItemDrilldownDTO.of(assessment));

					String assessmentType = assessment.getAssessmentLevel() != null && StringUtils.isNotEmpty(assessment.getAssessmentLevel().getName()) ? assessment.getAssessmentLevel().getName() : "";
					DashboardDataItemDTO typeCell = sI(assessmentType);

					String assessmentFramework = assessment.getAssessmentType() != null && StringUtils.isNotEmpty(assessment.getAssessmentType().getName()) ? assessment.getAssessmentType().getName() : "";
					DashboardDataItemDTO frameworkCell = sI(assessmentFramework);

					String taskName = StringUtils.isNotEmpty(task.getName()) ? task.getName() : "";
					DashboardDataItemDTO taskNameCell = sI(taskName).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

					String status = task.getStatus() != null ? task.getStatus().toString() + "%" : "";
					DashboardDataItemDTO statusCell = sI(status).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

					String assignee = task.getTaskAssignee() != null ? task.getTaskAssignee().getFullName() : "";
					DashboardDataItemDTO assigneeCell = sI(assignee).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

					String plannedStartDate = task.getEstimatedStartDate() != null ? dateFormat.format(task.getEstimatedStartDate()) : "";
					DashboardDataItemDTO plannedStartDateCell = sI(plannedStartDate).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

					String plannedEndDate = task.getEstimatedEndDate() != null ? dateFormat.format(task.getEstimatedEndDate()) : "";
					DashboardDataItemDTO plannedEndDateCell = sI(plannedEndDate).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

					String actualStartDate = task.getActualStartDate() != null ? dateFormat.format(task.getActualStartDate()) : "";
					DashboardDataItemDTO actualStartDateCell = sI(actualStartDate).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

					String actualEndDate = task.getActualEndDate() != null ? dateFormat.format(task.getActualEndDate()) : "";
					DashboardDataItemDTO actualEndDateCell = sI(actualEndDate).applyDrilldown(DashboardDataItemDrilldownDTO.of(task));

					// DashboardDataItemDTO auditLinkCell = sI(clientMessage.getMessage(SLCT.AUDIT$AUDIT)).applyDrilldown(DashboardDataItemDrilldownDTO.of(assessment).param("mode", "audit"));

					if (taskNumber.getAndIncrement() == 0) {
						Long tasksCount = (long) assessment.getTasks().size();
						nameCell.setRowSpan(tasksCount);
						typeCell.setRowSpan(tasksCount);
						frameworkCell.setRowSpan(tasksCount);
						itemsList.addAll(Arrays.asList(nameCell, typeCell, frameworkCell, taskNameCell, statusCell, assigneeCell, plannedStartDateCell, plannedEndDateCell, actualStartDateCell, actualEndDateCell));
					} else {
						itemsList.addAll(Arrays.asList(taskNameCell, statusCell, assigneeCell, plannedStartDateCell, plannedEndDateCell, actualStartDateCell, actualEndDateCell));
					}
					dashboardItem1.getGridItems().add(itemsList);
				});
			} else {
				List<DashboardDataItemDTO> itemsList = new ArrayList<>();

				String assessmentName = StringUtils.isNotEmpty(assessment.getName()) ? assessment.getName() : "";
				DashboardDataItemDTO nameCell = sI(assessmentName).applyDrilldown(DashboardDataItemDrilldownDTO.of(assessment));

				String assessmentType = assessment.getAssessmentLevel() != null && StringUtils.isNotEmpty(assessment.getAssessmentLevel().getName()) ? assessment.getAssessmentLevel().getName() : "";
				DashboardDataItemDTO typeCell = sI(assessmentType);

				String assessmentFramework = assessment.getAssessmentType() != null && StringUtils.isNotEmpty(assessment.getAssessmentType().getName()) ? assessment.getAssessmentType().getName() : "";
				DashboardDataItemDTO frameworkCell = sI(assessmentFramework);

				String taskName = "";
				DashboardDataItemDTO taskNameCell = sI(taskName);

				String status = "";
				DashboardDataItemDTO statusCell = sI(status);

				String assignee = "";
				DashboardDataItemDTO assigneeCell = sI(assignee);

				String plannedStartDate = "";
				DashboardDataItemDTO plannedStartDateCell = sI(plannedStartDate);

				String plannedEndDate = "";
				DashboardDataItemDTO plannedEndDateCell = sI(plannedEndDate);

				String actualStartDate = "";
				DashboardDataItemDTO actualStartDateCell = sI(actualStartDate);

				String actualEndDate = "";
				DashboardDataItemDTO actualEndDateCell = sI(actualEndDate);

				itemsList.addAll(Arrays.asList(nameCell, typeCell, frameworkCell, taskNameCell, statusCell, assigneeCell, plannedStartDateCell, plannedEndDateCell, actualStartDateCell, actualEndDateCell));
				dashboardItem1.getGridItems().add(itemsList);
			}
		}
	}

	/**
	 * Build Assessment drilldown
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	public void buildAssessmentDrilldown (DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		// Prepare assessment data
		Long assessmentId = Long.valueOf(drilldown.getParams().get("assessment"));
		Assessments assessment = assessmentsRepository.findById(assessmentId).get();

		// Org, System, Process, Technology
		String assessmentLevelName = assessment.getAssessmentLevel() != null ? assessment.getAssessmentLevel().getName() : "";
		if (assessment.getAssessmentLevel() != null && assessment.getAssessmentLevel().getId().equals(AssessmentLevel.ORG.getId())) {
			assessmentLevelName = clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$ORG_ASSESSMENT_LEVEL);
		}
		// HIPAA, ISO 27001, NIST CSF, ... ,  regular assessment
		String assessmentTypeName = assessment.getAssessmentType() != null ? assessment.getAssessmentType().getName() : "";
		String assessmentName = StringUtils.isNotEmpty(assessment.getName()) ? assessment.getName() : "";
		String assessmentDescription = StringUtils.isNotEmpty(assessment.getDescription()) ? assessment.getDescription() : "";
		String legalOrganizationName = assessment.getLegalOrganization() != null && StringUtils.isNotEmpty(assessment.getLegalOrganization().getName()) ? assessment.getLegalOrganization().getName() : "";

		// Provide drilldown with actual data
		String dashboardName = MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$ITEM_NAME), assessmentName);
		if (assessmentLevelName.length() > 0) dashboardName = MessageFormat.format("{0} {1}", assessmentLevelName, dashboardName);
//		if (assessmentTypeName.length() > 0) {
//			dashboardName = MessageFormat.format("{0} ({1}) Drilldown", dashboardName, assessmentTypeName);
//		} else {
			dashboardName = MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$ALTERNATIVE_ITEM_NAME), dashboardName);
//		}
		dashboard.setName(dashboardName);

		// Initialize Assessment Summary
		DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(1001L, clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$ASSESSMENT_SUMMARY$ITEM_NAME));
		List<List<DashboardDataItemDTO>> summaryPoints = new ArrayList<List<DashboardDataItemDTO>>(Arrays.asList(
			Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$ASSESSMENT_SUMMARY$ASSESSMENT_NAME_HEADER)).applyHeader(true), sI(assessmentName)),
			Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$ASSESSMENT_SUMMARY$LEVEL_HEADER)).applyHeader(true), sI(assessmentLevelName)),
			Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$ASSESSMENT_SUMMARY$TYPE_HEADER)).applyHeader(true), sI(assessmentTypeName)),
			Arrays.asList(sI(clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$ASSESSMENT_SUMMARY$LEGAL_ORG_HEADER)).applyHeader(true), sI(legalOrganizationName))
		));

		dashboardItem1.getGridItems().addAll(summaryPoints);
		section1.getDashboardItems().add(dashboardItem1);

		if (assessment.getAssessmentLevel() != null) {
			if (assessment.getAssessmentLevel().getId().equals(AssessmentLevel.ORG.getId())) {
				// TODO - Show some Org-level-specific information in drilldown
			} else if (assessment.getAssessmentLevel().getId().equals(AssessmentLevel.SYSTEM.getId())) {
				// System-level-specific dashboard data item
				DashboardTableItemDTO systemsSummaryTable = new DashboardTableItemDTO(1002L, clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_SYSTEMS$ITEM_NAME));
				systemsSummaryTable.addGridHeaders(Arrays.asList(
					clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_SYSTEMS$SYSTEM_NAME_HEADER),
					"",
					clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_SYSTEMS$STATUS_HEADER),
					clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_SYSTEMS$OWNER_HEADER),
					clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_SYSTEMS$BUSINESS_UNIT_HEADER)
				));

				Optional.ofNullable(assessment.getSystems()).orElse(new HashSet<>()).stream().forEach(system -> {
					List<DashboardDataItemDTO> itemsList = new ArrayList<>();

					String systemName = StringUtils.isNotEmpty(system.getName()) ? system.getName() : "";
					String systemStatusName = system.getSystemStatus() != null && StringUtils.isNotEmpty(system.getSystemStatus().name()) ? system.getSystemStatus().name() : "";
					String systemOwnerName = system.getOwner() != null && StringUtils.isNotEmpty(system.getOwner().getFullName()) ? system.getOwner().getFullName() : "";
					String systemBusinessUnitName = system.getBusinessUnit() != null && StringUtils.isNotEmpty(system.getBusinessUnit().getName()) ? system.getBusinessUnit().getName() : "";

					// sI(clientMessage.getMessage(SLCT.AUDIT$AUDIT)).applyDrilldown(DashboardDataItemDrilldownDTO.of(assessment).param("mode", "audit"))
					itemsList.addAll(
						Arrays.asList(
							sI(systemName),
							sI("[" + clientMessage.getMessage(SLCT.AUDIT$AUDIT) + "]").applyLink(
								DashboardLinkDTO.of(MessageFormat.format("/private/system-control-test-results/drilldown/{0,number,#}/{1,number,#}", system.getId(), assessment.getId()))
							).applyTextAlign("center"),
							sI(systemStatusName),
							sI(systemOwnerName),
							sI(systemBusinessUnitName)
						)
					);
					systemsSummaryTable.getGridItems().add(itemsList);
				});
				section1.getDashboardItems().add(systemsSummaryTable);

			} else if (assessment.getAssessmentLevel().getId().equals(AssessmentLevel.PROCESS.getId())) {
				// Process-level-specific dashboard data item
				DashboardTableItemDTO processesSummaryTable = new DashboardTableItemDTO(1002L, clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_PROCESSES$ITEM_NAME));
				processesSummaryTable.addGridHeaders(Arrays.asList(
					clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_PROCESSES$PROCESS_NAME_HEADER),
					clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_PROCESSES$REVENUE_PROCESSED_HEADER),
					clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_PROCESSES$OWNER_HEADER),
					clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_PROCESSES$BUSINESS_UNIT_HEADER)
				));

				Optional.ofNullable(assessment.getProcesses()).orElse(new HashSet<>()).stream().forEach(process -> {
					List<DashboardDataItemDTO> itemsList = new ArrayList<>();

					String processName = StringUtils.isNotEmpty(process.getName()) ? process.getName() : "";
					String processRevenueProcessed = process.getRevenueProcessed() != null ? process.getRevenueProcessed().toString() : "";
					String processOwnerName = process.getOwner() != null && StringUtils.isNotEmpty(process.getOwner().getFullName()) ? process.getOwner().getFullName() : "";
					String processBusinessUnitName = process.getBusinessUnit() != null && StringUtils.isNotEmpty(process.getBusinessUnit().getName()) ? process.getBusinessUnit().getName() : "";

					itemsList.addAll(
						Arrays.asList(
							sI(processName),
							sI(processRevenueProcessed),
							sI(processOwnerName),
							sI(processBusinessUnitName)
						)
					);
					processesSummaryTable.getGridItems().add(itemsList);
				});
				section1.getDashboardItems().add(processesSummaryTable);

			} else if (assessment.getAssessmentLevel().getId().equals(AssessmentLevel.TECHNOLOGY.getId())) {
				// Technology-level-specific dashboard data item
				DashboardTableItemDTO technologiesSummaryTable = new DashboardTableItemDTO(1002L, clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_TECH_CATEGORIES$ITEM_NAME));
				technologiesSummaryTable.addGridHeaders(Arrays.asList(clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$RELATED_TECH_CATEGORIES$TECH_CATEGORY_NAME_HEADER)));

				Optional.ofNullable(assessment.getTechnologyCategories()).orElse(new HashSet<>()).stream().forEach(technologyCategory -> {
					technologiesSummaryTable.getGridItems().add(
						Arrays.asList(
							sI(StringUtils.isNotEmpty(technologyCategory.getName()) ? technologyCategory.getName() : "")
						)
					);
				});
				section1.getDashboardItems().add(technologiesSummaryTable);
			}
		}

		// Assessment Security requirements dashboard data item
		DashboardTableItemDTO securityRequirementsTable = new DashboardTableItemDTO(1003L, clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$SECURITY_REQUIREMENTS$ITEM_NAME));
		securityRequirementsTable.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$SECURITY_REQUIREMENTS$CODE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$SECURITY_REQUIREMENTS$SECURITY_CONTROL_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$SECURITY_REQUIREMENTS$SECURITY_CONTROL_FAMILY_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$SECURITY_REQUIREMENTS$PROGRAM_AREA_HEADER)
		));

		assessment.getSecurityRequirements().stream().forEach(securityRequirement -> {
			String requirementCode = StringUtils.isNotEmpty(securityRequirement.getCode()) ? securityRequirement.getCode() : "";
			String requirementControlName = securityRequirement.getSecurityControlName() != null && StringUtils.isNotEmpty(securityRequirement.getSecurityControlName().getName()) ? securityRequirement.getSecurityControlName().getName() : "";
			String requirementControlFamily = securityRequirement.getSecurityControlFamily() != null && StringUtils.isNotEmpty(securityRequirement.getSecurityControlFamily().getName()) ? securityRequirement.getSecurityControlFamily().getName() : "";
			String requirementProgramArea = StringUtils.isNotEmpty(securityRequirement.getProgramArea()) ? securityRequirement.getProgramArea() : "";

			securityRequirementsTable.getGridItems().add(
				Arrays.asList(
					sI(requirementCode),
					sI(requirementControlName),
					sI(requirementControlFamily),
					sI(requirementProgramArea)
				)
			);
		});

		section1.getDashboardItems().add(securityRequirementsTable);

		// Assessment Tasks dashboard data item
		DashboardTableItemDTO tasksTable = taskDashboardService.buildTasksSummary(assessment.getTasks(), clientMessage.getMessage(SLCT.DRILLDOWNS$ASSESSMENT$TASK_SUMMARY$ITEM_NAME));

		section1.getDashboardItems().add(tasksTable);

	}
}
