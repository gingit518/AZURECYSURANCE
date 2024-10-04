package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.service.*;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-02-05
 */
@Service
@Slf4j
public class DashboardService extends DashboardServiceBase {

	public static final String CISO_DASHBOARD_GROUP = "CISO Dashboard";
	public static final String CFO_DASHBOARD_GROUP = "DASHBOARDS$CFO_DASHBOARD";

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Autowired
	private AnalyticDashboardsService analyticDashboardsService;

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private CrownJewelAssetDashboardService crownJewelAssetDashboardService;

	@Autowired
	private CyberSecurityToolROIDashboardService cyberSecurityToolROIDashboardService;

	@Autowired
	private DataTypeClassificationRepository dataTypeClassificationRepository;

	@Autowired
	private ExposureMetricsDashboardService exposureMetricsDashboardService;

	@Autowired
	private ExternalAnalyticsRepository externalAnalyticsRepository;

	@Autowired
	private FixedCapitalCostRepository fixedCapitalCostRepository;

	@Autowired
	private FixedOperationalCostRepository fixedOperationalCostRepository;

	@Autowired
	private VariableCostRepository variableCostRepository;

	@Autowired
	private OrganizationDashboardService organizationDashboardService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private VendorService vendorService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private PrivacyRiskDashboardService privacyRiskDashboardService;

	@Autowired
	private ProcessRepository processRepository;

	@Autowired
	private QuestionAnswersForVendorRepository questionAnswersForVendorRepository;

	@Autowired
	private QuestionAnswersForSystemRepository questionAnswersForSystemRepository;

	@Autowired
	private QuestionStatusDashboardService questionStatusDashboardService;

	@Autowired
	private QualitativeQuestionRepository qualitativeQuestionRepository;

	@Autowired
	private QuantMetricsRepository quantMetricsRepository;

	@Autowired
	private QuantMetricsService quantMetricsService;

	@Autowired
	private ResidualRiskDashboardService residualRiskDashboardService;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private ScoringQuestionsDashboardService scoringQuestionsDashboardService;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private UserAssignedSystemRepository userAssignedSystemRepository;

	@Autowired
	private UserAssignedVendorRepository userAssignedVendorRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private VendorsDashboardService vendorsDashboardService;

	@Autowired
	private PHIDashboardService phiDashboardService;

	@Autowired
	private AssessmentDashboardService assessmentDashboardService;

	@Autowired
	private TaskDashboardService taskDashboardService;

	@Autowired
	private BudgetDashboardService budgetDashboardService;

	@Autowired
	private AssessmentFindingDashboardService assessmentFindingDashboardService;

	/**
	 * Get Dashboards List
	 *
	 * @return Dashboards List
	 */
	public List<DashboardRefDTO> getList() {
		List<DashboardRefDTO> items = new ArrayList<>();

		Set<String> permissionNames = permissionService.getUserPermissionNames();

		// Analytics Items
		// items.add(new DashboardRefDTO(150001L, "Test qLick Dashboard", "Test qLick Dashboard", DashboardType.None, "fa fa-user", "Analytics"));
		analyticDashboardsService.buildAnalyticDashboardItemsMenu(items);

		Long organizationId = organizationService.getCurrentOrganizationId();
		List<ExternalAnalytics> analyticDashbardList = externalAnalyticsRepository.getListByOrganizationIdAndType(organizationId, ExternalAnalyticsType.DASHBOARD);
		Map<Long, String> dashboardMenuNames = new HashMap<>();
		for (ExternalAnalytics externalAnalytic : analyticDashbardList) {
			Optional<ExternalAnalyticsParameters> dashboardIdOpt = externalAnalytic.getExternalAnalyticsParameters().stream().filter(externalAnalyticsParameter -> ExternalAnalyticsQlikParameters.DASHBOARD_REPORT_ID.name().equalsIgnoreCase(externalAnalyticsParameter.getName())).findFirst();
			Optional<ExternalAnalyticsParameters> dashboardCaption = externalAnalytic.getExternalAnalyticsParameters().stream().filter(externalAnalyticsParameter -> ExternalAnalyticsQlikParameters.DASHBOARD_SECTION_NAME.name().equalsIgnoreCase(externalAnalyticsParameter.getName())).findFirst();
			if (dashboardIdOpt.isPresent() && dashboardCaption.isPresent()) {
				try {
					Long dashboardId = Long.parseLong(dashboardIdOpt.get().getValue().trim());
					String folderName = dashboardCaption.get().getValue().trim();
					if (folderName.equalsIgnoreCase("") || folderName.equalsIgnoreCase("none") || folderName.equalsIgnoreCase("no") || folderName.equalsIgnoreCase("-")) {
						folderName = null;
					}
					dashboardMenuNames.put(dashboardId, folderName);
				} catch (NumberFormatException exception) {
					;
				}
			}
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_MY_ASSIGNMENTS)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_MY_ASSIGNMENTS, "DASHBOARDS$MY_ASSIGNMENTS$NAME", "Shows system, vendor and cloud scoring that is completed or needs to be completed by the user", DashboardType.None, "fa fa-user", "DASHBOARD_TABS$SET_UP$NAME", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_VENDOR)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_VENDOR, "DASHBOARDS$VENDOR$NAME", "Displays the vendor exposures and scores", DashboardType.Vendor, "fa fa-th-list", "DASHBOARDS$EXECUTIVE_DASHBOARDS", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_VENDOR_STATUS)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_VENDOR_STATUS, "DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$ITEM_NAME", "Displays all the vendors by type and owner", DashboardType.Vendor, "fa fa-sitemap", "DASHBOARD_TABS$VENDOR_CYBER_RISK_MANAGER$NAME", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_VENDOR_SELF_ASSESSMENT)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_VENDOR_SELF_ASSESSMENT, "DASHBOARDS$VENDOR_SELF_ASSESSMENT$NAME", "Displays the vendors questionnaire results", DashboardType.Vendor, "fa fa-object-ungroup", "DASHBOARD_TABS$VENDOR_CYBER_RISK_MANAGER$NAME", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_VENDOR_AND_SYSTEM)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_VENDOR_AND_SYSTEM, SLCT.DASHBOARDS$VENDORS_AND_SYSTEMS$NAME, "Displays the vendors and systems details", DashboardType.Vendor, "fa fa-cc-diners-club", "DASHBOARDS$EXECUTIVE_DASHBOARDS", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ORGANIZATION)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_CYBER_INSURANCE, SLCT.DASHBOARDS$CYBER_INSURANCE$NAME, "Displays aggregate limits and sublimits. Identifies extraordinary exposures", DashboardType.Organization, "fa fa-table", "DASHBOARDS$EXECUTIVE_DASHBOARDS", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CISO_DIGITAL_ASSET)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_ORGANIZATION, "DASHBOARDS$CISO$DIGITAL_ASSET", "Displays cyber exposures and scores", DashboardType.Organization, "fa fa-table", "DASHBOARD_TABS$CISO$NAME", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_RESIDUAL_CYBER_RISK)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_RESIDUAL_CYBER_RISK, SLCT.DASHBOARDS$RESIDUAL_CYBER_RISK$NAME, "Displays the cyber risk post assessment and with cyber tool integration", DashboardType.None, "fa fa-truck", "DASHBOARD_TABS$CISO$NAME", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_PRIVACY)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_PRIVACY, "DASHBOARDS$PRIVACY$NAME", "Displays the vendor's privacy risk", DashboardType.None, "fa fa-user-secret", "DASHBOARD_TABS$VENDOR_CYBER_RISK_MANAGER$NAME", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CLOUD)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_CLOUD, "DASHBOARDS$CLOUD$NAME", "Displays cloud scores", DashboardType.Vendor, "fa fa-cloud", "DASHBOARD_TABS$CISO$NAME", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_M_AND_A)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_M_AND_A, "DASHBOARDS$M_AND_A$NAME", "Displays the target exposures", DashboardType.None, "fa fa-info-circle", "DASHBOARDS$EXECUTIVE_DASHBOARDS", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_PHI_DATA)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_PHI_DATA, "DASHBOARDS$PHI_DATA$NAME", "Displays PHI Exposures", DashboardType.None, "fa fa-medkit", "DASHBOARD_TABS$DPO$NAME", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ETL_SYSTEMS_EXPOSURES)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_ETL_SYSTEMS_EXPOSURES, "DASHBOARDS$ETL_SYSTEMS_EXPOSURES$NAME", "Displays the system exposures from files that are sent to vendors", DashboardType.None, "fa fa-database", "DASHBOARD_TABS$VENDOR_CYBER_RISK_MANAGER$NAME", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_IOT_EXPOSURES)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_IOT_EXPOSURES, "DASHBOARDS$IOT_EXPOSURES$NAME", "Displays exposures of systems using IoT Technology", DashboardType.None, "fa fa-plug", "DASHBOARD_TABS$CISO$NAME", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_UNINSURABLE_EXPOSURES)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_UNINSURABLE_EXPOSURES, "DASHBOARDS$UNINSURABLE_EXPOSURES$NAME", "Displays all systems with exposures over 750m", DashboardType.None, "fa fa-exclamation-circle", "DASHBOARDS$EXECUTIVE_DASHBOARDS", dashboardMenuNames));
		}

		// Jefferies Presentation Dashboards
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CROWN_JEWEL_ASSET)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_CROWN_JEWEL_ASSET, SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$NAME, "Displays the exposures and inherent risk scores of the crown jewel assets", DashboardType.None, "fa fa-diamond", "DASHBOARD_TABS$CISO$NAME", dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_PRIVACY_RISK)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_PRIVACY_RISK, "DASHBOARDS$PRIVACY_RISK$NAME", "Displays privacy exposures and scores", DashboardType.None, "fa fa-id-card", "DASHBOARD_TABS$DPO$NAME", dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CYBERSECURITY_TOOL_ROI)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_CYBERSECURITY_TOOL_ROI, SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$NAME, "Displays ROI for cyber tools", DashboardType.None, "fa fa-gavel", "DASHBOARD_TABS$CISO$NAME", dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_BUDGETING)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_BUDGETING, "DASHBOARDS$BUDGETING$NAME", "Displays fixed and variable operational and captial costs", DashboardType.None, "fa fa-money", "DASHBOARD_TABS$CISO$NAME", dashboardMenuNames));
		}

		// TODO 	2020-05-21 	Owner asked to HIDE IT FOR NOW (In Table with new names for dashboards was a short note to hide it)
//		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_BUDGET_SCENARIO_ANALYSIS)) {
//			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_BUDGET_SCENARIO_ANALYSIS, "Budget Scenario Analysis", "Budget Scenario Analysis", DashboardType.None, "fa fa-usd", dashboardMenuNames));
//		}

		// Only CEO or CRO can see
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ADMIN)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_ADMIN, "DASHBOARDS$ADMIN$NAME", "Displays the exposures and scores by BU", DashboardType.Admin, "fa fa-columns", null, null));
		}
//		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_TEST_ITEM)) {
//			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_TEST_ITEM, "Test Dashboard", "Test Dashboard", DashboardType.Admin, "fa fa-columns", dashboardMenuNames));
//		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_QUESTION_STATUS)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_QUESTION_STATUS, "DASHBOARDS$QUESTION_STATUS$NAME", "Displays the status of the cyber risk scoring questions", DashboardType.Admin, "fa fa-question-circle", "DASHBOARD_TABS$SET_UP$NAME", dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_VENDOR_QUESTION_STATUS)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_VENDOR_QUESTION_STATUS, "DASHBOARDS$VENDOR_QUESTION_STATUS$NAME", "Displays the status of the cyber risk scoring questions", DashboardType.Admin, "fa fa-question-circle", "DASHBOARD_TABS$SET_UP$NAME", dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ASSIGNMENT_STATUS)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_ASSIGNMENTS_STATUS, "DASHBOARDS$ASSIGNMENTS_STATUS$NAME", "Displays the status of the system scoring assigments", DashboardType.Admin, "fa fa-users", "DASHBOARD_TABS$SET_UP$NAME", dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_TASK)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_TASK, "DASHBOARDS$TASK$NAME", "Displays the status of tasks", DashboardType.None, "fa fa-tasks", "DASHBOARD_TABS$CISO$NAME", dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ASSESSMENT)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_ASSESSMENTS, "DASHBOARDS$VENDOR_SELF_ASSESSMENT$NAME", "Displays the status of cybersecurity assessments", DashboardType.None, "fa fa-object-ungroup", "DASHBOARD_TABS$CISO$NAME", dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ASSESSMENT_FINDING)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_ASSESSMENT_FINDING, SLCT.DASHBOARDS$ASSESSMENT_FINDING$NAME, "Assessment Findings", DashboardType.None, "fa fa-bar-chart", "DASHBOARD_TABS$CISO$NAME", dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_RESOURCES)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_RESOURCES, "DASHBOARDS$RESOURCES$NAME", "Displays the resource utilization and planning", DashboardType.None, "fa fa-users", "DASHBOARD_TABS$CISO$NAME", dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ORGANIZATION_SYSTEM_GDPR)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_ORGANIZATION_SYSTEM_GDPR, "DASHBOARDS$ORGANIZATION_SYSTEM_GDPR$NAME", "GDPR Dashboard", DashboardType.None, "fa fa-lock", null, dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_RISK_REGISTER)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_RISK_REGISTER, "DASHBOARDS$RISK_REGISTER$NAME", "Risk Register", DashboardType.None, "fa fa-bar-chart", null, dashboardMenuNames));
		}

		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_AUDIT_ASSESSMENT_AUDIT)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_ASSESSMENTS_AUDIT, "DASHBOARDS$AUDIT$AUDIT", "Audits the status of cybersecurity assessments", DashboardType.None, "fa fa-balance-scale", "DASHBOARDS$AUDIT_DASHBOARD", dashboardMenuNames));
		}

		//
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_DATA_EXFILTRATION)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_DATA_EXFILTRATION, SLCT.DASHBOARD$DATA_EXFILTRATION$NAME, "Financial exposures of systems", DashboardType.None, "fa fa-columns", null, dashboardMenuNames));
		}

		// CFO Dashboards
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CFO_CROWN_JEWEL_ASSET)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_CFO_CROWN_JEWEL_ASSET, SLCT.DASHBOARDS$CROWN_JEWEL_ASSET$NAME, "Displays the exposures and inherent risk scores of the crown jewel assets", DashboardType.None, "fa fa-diamond", CFO_DASHBOARD_GROUP, dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CFO_ORGANIZATION)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_CFO_CYBER_INSURANCE, SLCT.DASHBOARDS$CYBER_INSURANCE$NAME, "Displays aggregate limits and sublimits. Identifies extraordinary exposures", DashboardType.Organization, "fa fa-table", CFO_DASHBOARD_GROUP, dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CFO_CYBERSECURITY_TOOL_ROI)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_CFO_CYBERSECURITY_TOOL_ROI, SLCT.DASHBOARDS$CYBERSECURITY_TOOL_ROI$NAME, "Displays ROI for cyber tools", DashboardType.None, "fa fa-gavel", CFO_DASHBOARD_GROUP, dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CFO_M_AND_A)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_CFO_M_AND_A, "DASHBOARDS$M_AND_A$NAME", "Displays the target exposures", DashboardType.None, "fa fa-info-circle", CFO_DASHBOARD_GROUP, dashboardMenuNames));
		}
		if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CFO_VENDOR)) {
			items.add(new DashboardRefDTO(DashboardsConfig.DASHBOARD_CFO_VENDOR, "DASHBOARDS$VENDOR$NAME", "Displays the vendor exposures and scores", DashboardType.Vendor, "fa fa-th-list", CFO_DASHBOARD_GROUP, dashboardMenuNames));
		}

		// items.add(new DashboardRefDTO(DashboardsConfig.FFIEC_CAT_CYBER_MATURITY, "FFIEC CAT Cyber Maturity", "Displays the FFIEC CAT Cyber Maturity", DashboardType.Vendor, "fa fa-question-circle", CFO_DASHBOARD_GROUP, dashboardMenuNames));
		// items.add(new DashboardRefDTO(DashboardsConfig.FFIEC_CAT_INHERENT_RISK, "FFIEC CAT Inherent Risk", "Displays the FFIEC CAT Inherent Risk", DashboardType.Organization, "fa fa-question-circle", CFO_DASHBOARD_GROUP, dashboardMenuNames));

		return items;
	}

	public String getDashboardGroupNameForUser(String dashboard, RoleType roleType, String defaultGroupName) {
		String result = defaultGroupName;

		if (userService.hasRole(roleType)) {
			result = dashboard;
		}

		return result;
	}

	private boolean checkDashboardPermissions(Set<String> permissionNames, PermissionType permissionType) {
		return checkDashboardPermissions(permissionNames, permissionType, false);
	}

	private boolean checkDashboardPermissions(Set<String> permissionNames, PermissionType permissionType, boolean throwExceptionOnFalse) {
		boolean result = false;

		if (permissionNames != null && permissionType != null && permissionNames.contains(permissionType.getPermission())) {
			result = true;
		} else if (throwExceptionOnFalse) {
			throw new ForbiddenException("You are not allowed to load this dashboard!");
		}

		return result;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getDashboardDetails(Long dashboardId, Long riskModelId, DashboardStateDTO dashboardState) {
		DashboardDTO dashboard = new DashboardDTO();

		// Try to get proper state of the filters/searches
		DashboardStateDTO currentDashboardState = dashboardState;
		if (dashboardState != null && dashboardState.getReferenceUUID() != null) {
			DashboardStateDTO tmpDashboardState = cacheService.getSearchConfig(dashboardState.getReferenceUUID());
			if (tmpDashboardState != null) {
				currentDashboardState = tmpDashboardState;
			}
		}

		Set<String> permissionNames = permissionService.getUserPermissionNames();

		// Analytics Items Detection
		DashboardDTO analyticsDashboard = analyticDashboardsService.checkIsAnalyticsDashboard(riskModelId, dashboardId, dashboardState);
		if (analyticsDashboard != null) {
			return analyticsDashboard;
		}

		if (DashboardsConfig.DASHBOARD_MY_ASSIGNMENTS.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_MY_ASSIGNMENTS, true)) dashboard = getMyAssignmentsDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_VENDOR.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_VENDOR, true)) dashboard = getVendorDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_CFO_VENDOR.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_VENDOR, true)) dashboard = getVendorDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_VENDOR_STATUS.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_VENDOR_STATUS, true)) dashboard = vendorsDashboardService.buildVendorStatusDashboardDetails(riskModelId, dashboardState);
		} else if (DashboardsConfig.DASHBOARD_VENDOR_SELF_ASSESSMENT.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_VENDOR_SELF_ASSESSMENT, true)) dashboard = vendorsDashboardService.buildVendorSelfAssessmentDashboardDetails(riskModelId, dashboardState);
		} else if (DashboardsConfig.DASHBOARD_VENDOR_AND_SYSTEM.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_VENDOR_AND_SYSTEM, true)) dashboard = vendorsDashboardService.buildVendorAndSystemsDashboardDetails(riskModelId, dashboardState);
		} else if (DashboardsConfig.DASHBOARD_CYBER_INSURANCE.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ORGANIZATION, true)) dashboard = organizationDashboardService.getCyberInsuranceDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_CFO_CYBER_INSURANCE.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CFO_ORGANIZATION, true)) dashboard = organizationDashboardService.getCyberInsuranceDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_ORGANIZATION.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CISO_DIGITAL_ASSET, true)) dashboard = organizationDashboardService.getOrganizationDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_ADMIN.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ADMIN, true)) dashboard = getAdminDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_QUESTION_STATUS.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_QUESTION_STATUS, true)) dashboard = questionStatusDashboardService.getQuestionStatusDashboardDetails(riskModelId, currentDashboardState);
		} else if (DashboardsConfig.DASHBOARD_VENDOR_QUESTION_STATUS.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_VENDOR_QUESTION_STATUS, true)) dashboard = questionStatusDashboardService.getVendorQuestionStatusDashboardDetails(riskModelId, currentDashboardState);
		} else if (DashboardsConfig.DASHBOARD_ASSIGNMENTS_STATUS.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ASSIGNMENT_STATUS, true)) dashboard = getAssignmentStatusDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_RESIDUAL_CYBER_RISK.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_RESIDUAL_CYBER_RISK, true)) dashboard = residualRiskDashboardService.getResidualScoreDashboardDetails(riskModelId, currentDashboardState);
		} else if (DashboardsConfig.DASHBOARD_PRIVACY.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_PRIVACY, true)) dashboard = getPrivacyDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_CLOUD.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CLOUD, true)) dashboard = getCloudScoringDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_M_AND_A.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_M_AND_A, true)) dashboard = getMATargetRiskDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_CFO_M_AND_A.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CFO_M_AND_A, true)) dashboard = getMATargetRiskDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_CROWN_JEWEL_ASSET.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CROWN_JEWEL_ASSET, true)) dashboard = crownJewelAssetDashboardService.getCrownJewelsDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_CFO_CROWN_JEWEL_ASSET.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CFO_CROWN_JEWEL_ASSET, true)) dashboard = crownJewelAssetDashboardService.getCrownJewelsDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_PRIVACY_RISK.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_PRIVACY_RISK, true)) dashboard = privacyRiskDashboardService.getPrivacyRiskDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_PRIVACY_IMPACT_ASSESSMENT.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_PRIVACY_IMPACT_ASSESSMENT, true)) dashboard = privacyRiskDashboardService.getPrivacyRiskDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_CYBERSECURITY_TOOL_ROI.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CYBERSECURITY_TOOL_ROI, true)) dashboard = cyberSecurityToolROIDashboardService.getDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_CFO_CYBERSECURITY_TOOL_ROI.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_CFO_CYBERSECURITY_TOOL_ROI, true)) dashboard = cyberSecurityToolROIDashboardService.getDashboardDetails(riskModelId, dashboardId);
		} else if (DashboardsConfig.DASHBOARD_BUDGETING.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_BUDGETING, true)) dashboard = budgetDashboardService.getBudgetingDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_BUDGET_SCENARIO_ANALYSIS.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_BUDGET_SCENARIO_ANALYSIS, true)) dashboard = budgetDashboardService.getCyberSecurityBudgetScenarioAnalysisDashBoard(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_PHI_DATA.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_PHI_DATA, true)) dashboard = phiDashboardService.getDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_ETL_SYSTEMS_EXPOSURES.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ETL_SYSTEMS_EXPOSURES, true)) dashboard = exposureMetricsDashboardService.getETLSystemsDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_IOT_EXPOSURES.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_IOT_EXPOSURES, true)) dashboard = exposureMetricsDashboardService.getIOTExposuresDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_UNINSURABLE_EXPOSURES.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_UNINSURABLE_EXPOSURES, true)) dashboard = exposureMetricsDashboardService.getUninsurableExposuresDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_DATA_EXFILTRATION.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_DATA_EXFILTRATION, true)) dashboard = exposureMetricsDashboardService.getExposureTypeDashboardDetails(riskModelId, QuantsDomain.DATA_EXFILTRATION, "Data Exfiltrations");
		} else if (DashboardsConfig.DASHBOARD_ASSESSMENTS.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ASSESSMENT, true)) dashboard = assessmentDashboardService.getAssessment1DashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_ASSESSMENTS_AUDIT.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_AUDIT_ASSESSMENT_AUDIT, true)) dashboard = assessmentDashboardService.getAssessmentAuditDashboardDetails(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_TASK.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_TASK, true)) dashboard = taskDashboardService.getTaskDashboardDetails(riskModelId, dashboardState);
		} else if (DashboardsConfig.DASHBOARD_RESOURCES.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_RESOURCES, true)) dashboard = taskDashboardService.getResourcesDashboardDetails(riskModelId, dashboardState);
		} else if (DashboardsConfig.DASHBOARD_ORGANIZATION_SYSTEM_GDPR.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ORGANIZATION_SYSTEM_GDPR, true)) dashboard = organizationDashboardService.getOrganizationSystemGDPRDashboard(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_RISK_REGISTER.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_RISK_REGISTER, true)) dashboard = assessmentFindingDashboardService.getRiskRegisterDashboard(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_ASSESSMENT_FINDING.equals(dashboardId)) {
			if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ASSESSMENT_FINDING, true)) dashboard = assessmentFindingDashboardService.getAssessmentFindingsDashboard(riskModelId);
		} else if (DashboardsConfig.FFIEC_CAT_CYBER_MATURITY.equals(dashboardId)) {
			//if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ASSESSMENT_FINDING, true))
			dashboard = questionStatusDashboardService.getFFIECCATCyberMaturityDashboardDetails(riskModelId, dashboardState);
		} else if (DashboardsConfig.FFIEC_CAT_INHERENT_RISK.equals(dashboardId)) {
			// if (checkDashboardPermissions(permissionNames, PermissionType.DASHBOARD_ASSESSMENT_FINDING, true))
			dashboard = questionStatusDashboardService.getFFIECCATInherentRiskDashboardDetails(riskModelId, dashboardState);
		}

		/*
		items.add(new DashboardRefDTO(DashboardsConfig.FFIEC_CAT_CYBER_MATURITY, "FFIEC CAT Cyber Maturity", "Displays the FFIEC CAT Cyber Maturity", DashboardType.Vendor, "fa fa-question-circle", CFO_DASHBOARD_GROUP, dashboardMenuNames));
		items.add(new DashboardRefDTO(DashboardsConfig.FFIEC_CAT_INHERENT_RISK, "FFIEC CAT Inherent Risk", "Displays the FFIEC CAT Inherent Risk", DashboardType.Organization, "fa fa-question-circle", CFO_DASHBOARD_GROUP, dashboardMenuNames));
		 */

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public ByteArrayOutputStream getReportContent(Long riskModelId, String dashboardRefUUID) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		Long dashboardId = null;
		if (DashboardsConfig.CROWN_JEWEL_REPORT.equalsIgnoreCase(dashboardRefUUID)) {
			result = crownJewelAssetDashboardService.buildReport(riskModelId);
		} else if (DashboardsConfig.PRIVACY_RISK_REPORT.equalsIgnoreCase(dashboardRefUUID)) {
			result = privacyRiskDashboardService.buildReport(riskModelId, Arrays.asList(DataTypeDomain.PRIVACY));
		} else if (DashboardsConfig.DASHBOARD_VENDOR_STATUS_REPORT.equalsIgnoreCase(dashboardRefUUID)) {
			result = vendorsDashboardService.buildReport(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_VENDOR_AND_SYSTEM_REPORT.equalsIgnoreCase(dashboardRefUUID)) {
			result = vendorsDashboardService.buildVendorAndSystemReport(riskModelId);
		} else if (DashboardsConfig.DASHBOARD_EXPOSURE_RISK_REPORT.equalsIgnoreCase(dashboardRefUUID)) {
			result = exposureMetricsDashboardService.buildReport(riskModelId, QuantsDomain.DATA_EXFILTRATION);
		} else {
			DashboardStateDTO searchConfig = cacheService.getSearchConfig(dashboardRefUUID);

			if (searchConfig != null) {
				if (dashboardId == null) dashboardId = searchConfig.getId();
				if (DashboardsConfig.DASHBOARD_QUESTION_STATUS.equals(dashboardId)) {
					result = questionStatusDashboardService.buildQuestionStatusSearchReport(riskModelId, searchConfig);
				} else if (DashboardsConfig.DASHBOARD_VENDOR_QUESTION_STATUS.equals(dashboardId)) {
					result = questionStatusDashboardService.buildQuestionStatusSearchReport(riskModelId, searchConfig);
				}
			}
		}

		return result;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getVendorDashboardDetails(Long riskModelId, Long dashboardId) {
		DashboardDTO dashboard = new DashboardDTO(1L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$DESCRIPTION), DashboardType.Vendor);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop;
		if (DashboardsConfig.DASHBOARD_VENDOR.equals(dashboardId)) {
			breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_EXECUTIVE(clientMessage)
				.add("DASHBOARD_VENDOR_DASHBOARD", SLCT.DASHBOARDS$VENDOR$NAME, "/private/dashboards/1");
		} else {
			breadcrumbsTop = DashboardBreadcrumbsHelper.CFO_DASHBOARD(clientMessage)
				.add("DASHBOARD_VENDOR_DASHBOARD", SLCT.DASHBOARDS$VENDOR$NAME, "/private/dashboards/2005");
		}

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(1L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_RISK$ITEM_NAME), "");
		dashboard.getSections().add(section1);
		/*
		DashboardSectionDTO section2 = new DashboardSectionDTO(2L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$ITEM_NAME), "");
		DashboardSectionDTO section3 = new DashboardSectionDTO(3L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MAP$ITEM_NAME), "");
		DashboardSectionDTO section32 = new DashboardSectionDTO(32L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$ITEM_NAME), "");
		DashboardSectionDTO section4 = new DashboardSectionDTO(4L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$IMPACT$ITEM_NAME), "");
		DashboardSectionDTO section5 = new DashboardSectionDTO(5L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$LIKELIHOOD$ITEM_NAME), "");
		dashboard.getSections().add(section2);
		dashboard.getSections().add(section3);
		dashboard.getSections().add(section32);
		dashboard.getSections().add(section4);
		dashboard.getSections().add(section5);
		*/

		// Create breadcrumbs
		section1.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_VENDOR_DASHBOARD_1", SLCT.DASHBOARDS$VENDOR$CYBER_RISK$ITEM_NAME, "/private/dashboards/1").getBreadcrumbs());
		/*
		section2.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_VENDOR_DASHBOARD_2", SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$ITEM_NAME, "/private/dashboards/1").getBreadcrumbs());
		section3.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_VENDOR_DASHBOARD_3", SLCT.DASHBOARDS$VENDOR$HEAT_MAP$ITEM_NAME, "/private/dashboards/1").getBreadcrumbs());
		section32.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_VENDOR_DASHBOARD_32", SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$ITEM_NAME, "/private/dashboards/1").getBreadcrumbs());
		section4.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_VENDOR_DASHBOARD_4", SLCT.DASHBOARDS$VENDOR$IMPACT$ITEM_NAME, "/private/dashboards/1").getBreadcrumbs());
		section5.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARD_VENDOR_DASHBOARD_5", SLCT.DASHBOARDS$VENDOR$LIKELIHOOD$ITEM_NAME, "/private/dashboards/1").getBreadcrumbs());

		List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor);
		List<VendorDataSeries> impactData = getQualMetricData(riskModelId, MetricDomain.IMPACT, null, vendorTypes);
		List<VendorDataSeries> likelihoodData = getQualMetricData(riskModelId, MetricDomain.LIKELIHOOD, null, vendorTypes);
		Map<Long, VendorDataSeries> summaryQualData = getLongVendorDataSeriesMap(impactData, likelihoodData);
		*/

		// List<VendorDataSeries> quantVendorScores = buildQuantMetricData(riskModelId);

		// Initialize Section 1
		DashboardChartItemDTO dashboardItem11 = new DashboardChartItemDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_RISK$CYBER_RISK_SUMMARY$ITEM_NAME), "", DashboardItemType.BarChart);
		// dashboardItem11.setGridHeaders(Arrays.asList("Vendor", "Score"));
		dashboardItem11.setXAxis(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_RISK$CYBER_RISK_SUMMARY$X_AXIS));
		dashboardItem11.setYAxis(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_RISK$CYBER_RISK_SUMMARY$Y_AXIS));
		if (organization.getQualThreshold() != null) dashboardItem11.setThreshold(DashboardChartThresholdDTO.of(organization.getQualThreshold(), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_RISK$CYBER_RISK_SUMMARY$THRESHOLD)));
		section1.getDashboardItems().add(dashboardItem11);

		// Total Qual Vendor Scores
		// DashboardTableItemDTO dashboardItem12 = new DashboardTableItemDTO(1l, "Vendor Cyber Risk Scores");
		DashboardDataGridItemDTO dashboardItem12 = new DashboardDataGridItemDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_RISK$CYBER_RISK_SCORES$ITEM_NAME));
		// dashboardItem12.addGridHeaders(Arrays.asList("Vendor", "Score", "Impact Weight", "Impact Total", "Likelihood Weight", "Likelihood Total"));
		dashboardItem12.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_RISK$CYBER_RISK_SCORES$VENDOR_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_RISK$CYBER_RISK_SCORES$SCORE_HEADER),
			clientMessage.getMessage("Level 1"),
			clientMessage.getMessage("Level 2"),
			clientMessage.getMessage("Cloud Level 1"),
			clientMessage.getMessage("Cloud Level 2")
		), true);
		dashboardItem12.addGridHeaders(Arrays.asList(
			"Score", "Total",
			"Score", "Total",
			"Score", "Total",
			"Score", "Total",
			"Score", "Total"
		), true);
		dashboardItem12.getGridHeaders().get(0).get(0).setRowSpan(2l);
		dashboardItem12.getGridHeaders().get(0).get(1).setColSpan(2l);
		dashboardItem12.getGridHeaders().get(0).get(2).setColSpan(2l);
		dashboardItem12.getGridHeaders().get(0).get(3).setColSpan(2l);
		dashboardItem12.getGridHeaders().get(0).get(4).setColSpan(2l);
		dashboardItem12.getGridHeaders().get(0).get(5).setColSpan(2l);
		section1.getDashboardItems().add(dashboardItem12);

		Map<Organizations, MetricResult> vendorLevel1QualScoringDataMap = scoringQuestionsDashboardService.getVendorsScoringAggregatedData(riskModel.getId(), Arrays.asList(VendorType.Vendor), null);
		Map<Organizations, MetricResult> vendorLevel2QualScoringDataMap = scoringQuestionsDashboardService.getVendorsScoringAggregatedData(riskModel.getId(), Arrays.asList(VendorType.VendorInternal), null);
		Map<Organizations, MetricResult> cloudLevel1QualScoringDataMap = scoringQuestionsDashboardService.getVendorsScoringAggregatedData(riskModel.getId(), Arrays.asList(VendorType.Cloud), null);
		Map<Organizations, MetricResult> cloudLevel2QualScoringDataMap = scoringQuestionsDashboardService.getVendorsScoringAggregatedData(riskModel.getId(), Arrays.asList(VendorType.CloudInternal), null);
		for (Map.Entry<Organizations, MetricResult> entry : vendorLevel1QualScoringDataMap.entrySet()) {
			Organizations vendor = entry.getKey();
			MetricResult vendorLevel1 = vendorLevel1QualScoringDataMap.computeIfAbsent(vendor, organizations -> new MetricResult(organizations.getName(), 0D));
			MetricResult vendorLevel2 = vendorLevel2QualScoringDataMap.computeIfAbsent(vendor, organizations -> new MetricResult(organizations.getName(), 0D));
			MetricResult cloudLevel1 = cloudLevel1QualScoringDataMap.computeIfAbsent(vendor, organizations -> new MetricResult(organizations.getName(), 0D));
			MetricResult cloudLevel2 = cloudLevel2QualScoringDataMap.computeIfAbsent(vendor, organizations -> new MetricResult(organizations.getName(), 0D));

			MetricResult<QuestionAnswersForVendor> totalScore = new MetricResult(vendor.getName(), 0d);
			totalScore.setResult(vendorLevel1.getResult() + vendorLevel2.getResult() + cloudLevel1.getResult() + cloudLevel2.getResult());
			totalScore.setMaxQuestionsAnswersWeight(vendorLevel1.getMaxQuestionsAnswersWeight() + vendorLevel2.getMaxQuestionsAnswersWeight() + cloudLevel1.getMaxQuestionsAnswersWeight() + cloudLevel2.getMaxQuestionsAnswersWeight());

			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(vendor.getName()));
			rowItems.add(sI(totalScore.getResultScore()).applyTextAlign("center"));
			rowItems.add(sI(totalScore.buildNormalizedResult()).round(2));
			rowItems.add(sI(vendorLevel1.getResultScore()).applyTextAlign("center"));
			rowItems.add(sI(vendorLevel1.buildNormalizedResult()).round(2));
			rowItems.add(sI(vendorLevel2.getResultScore()).applyTextAlign("center"));
			rowItems.add(sI(vendorLevel2.buildNormalizedResult()).round(2));
			rowItems.add(sI(cloudLevel1.getResultScore()).applyTextAlign("center"));
			rowItems.add(sI(cloudLevel1.buildNormalizedResult()).round(2));
			rowItems.add(sI(cloudLevel2.getResultScore()).applyTextAlign("center"));
			rowItems.add(sI(cloudLevel2.buildNormalizedResult()).round(2));
			dashboardItem12.getGridItems().add(rowItems);
			// applyVendorDashboardQualsDrilldown(rowItems, dataSeries.getVendor());

			List<DashboardDataItemDTO> chartItems = new ArrayList<>();
			chartItems.add(sI(vendor.getName()));
			chartItems.add(sI(totalScore.buildNormalizedResult()).round(2));
			// if (chartItems.size() > 0) chartItems.get(0).setDrilldown(DashboardDataItemDrilldownDTO.of(dataSeries.getVendor()));
			dashboardItem11.getGridItems().add(chartItems);
		}

		/*
		for (Map.Entry<Long, VendorDataSeries> entry : summaryQualData.entrySet()) {
			VendorDataSeries dataSeries = entry.getValue();

			List<DashboardDataItemDTO> rowItems = createRowItems(dataSeries);
			dashboardItem12.getGridItems().add(rowItems);
			applyVendorDashboardQualsDrilldown(rowItems, dataSeries.getVendor());

			List<DashboardDataItemDTO> chartItems = createChartItems(dataSeries);
			if (chartItems.size() > 0) chartItems.get(0).setDrilldown(DashboardDataItemDrilldownDTO.of(dataSeries.getVendor()));
			dashboardItem11.getGridItems().add(chartItems);
		}
		*/

		/*
		// Init Heat Matrix and Heat Chart data
		Map<Long, DashboardDataItemDTO> heatChartItemsMap = new HashMap<>();
		List<DashboardDataItemDTO> heatChartItems = new ArrayList<>();
		List<List<DashboardDataItemDTO>> itemsMatrix = new ArrayList<>();
		buildHeatChartItems(summaryQualData, heatChartItemsMap, heatChartItems, itemsMatrix);

		// Total Quantification Scores
//		DashboardItemDTO dashboardItem14 = vendorsDashboardService.createVendorQuantScoresDashboard(riskModelId, quantVendorScores, heatChartItemsMap);
		// Vendor Exposures grid
		DashboardItemDTO dashboardItem14 = vendorsDashboardService.createVendorQuantScoresDashboard(riskModel, null, heatChartItemsMap);
		section2.getDashboardItems().add(dashboardItem14);

		// Create Heat Chart
		DashboardChartItemDTO dashboardItem130 = createHeatChartDashboardItem(heatChartItems);
		section3.getDashboardItems().add(dashboardItem130);

		// Create Heat Matrix Table
		DashboardItemDTO dashboardItem13 = createHeatMatrixTableDashboardItem(itemsMatrix);
		section32.getDashboardItems().add(dashboardItem13);

		// Initialize Section 2
		DashboardChartItemDTO dashboardItem21 = new DashboardChartItemDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$IMPACT$IMPACT_CHART$ITEM_NAME), "", DashboardItemType.BarChart);
		// dashboardItem21.setGridHeaders(Arrays.asList("Vendor", "Score"));
		dashboardItem21.setXAxis(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$IMPACT$IMPACT_CHART$X_AXIS));
		dashboardItem21.setYAxis(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$IMPACT$IMPACT_CHART$Y_AXIS));
		if (organization.getQualThreshold() != null) dashboardItem21.setThreshold(DashboardChartThresholdDTO.of(organization.getQualThreshold(), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$IMPACT$IMPACT_CHART$THRESHOLD)));
		section4.getDashboardItems().add(dashboardItem21);

		DashboardDataGridItemDTO dashboardItem22 = new DashboardDataGridItemDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$IMPACT$IMPACT_GRID$ITEM_NAME));
		dashboardItem22.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$IMPACT$IMPACT_GRID$VENDOR_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$IMPACT$IMPACT_GRID$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$IMPACT$IMPACT_GRID$WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$IMPACT$IMPACT_GRID$TOTAL_WEIGHT_HEADER)
		), true);
		section4.getDashboardItems().add(dashboardItem22);

		for (VendorDataSeries dataSeries : impactData) {
			List<DashboardDataItemDTO> rowItems = createRowItems(dataSeries);
			dashboardItem22.getGridItems().add(rowItems);
			applyVendorDashboardQualMetricDrilldown(rowItems, dataSeries.getVendor(), MetricDomain.IMPACT);

			List<DashboardDataItemDTO> chartItems = createChartItems(dataSeries);
			if (chartItems.size() > 0) chartItems.get(0).setDrilldown(DashboardDataItemDrilldownDTO.of(dataSeries.getVendor(), MetricDomain.IMPACT));
			dashboardItem21.getGridItems().add(chartItems);
		}

		// Initialize Section 3
		DashboardChartItemDTO dashboardItem31 = new DashboardChartItemDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$LIKELIHOOD$LIKELIHOOD_CHART$ITEM_NAME), "", DashboardItemType.BarChart);
		// dashboardItem31.setGridHeaders(Arrays.asList("Vendor", "Score"));
		dashboardItem31.setXAxis(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$LIKELIHOOD$LIKELIHOOD_CHART$X_AXIS));
		dashboardItem31.setYAxis(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$LIKELIHOOD$LIKELIHOOD_CHART$Y_AXIS));
		if (organization.getQualThreshold() != null) dashboardItem31.setThreshold(DashboardChartThresholdDTO.of(organization.getQualThreshold(), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$LIKELIHOOD$LIKELIHOOD_CHART$THRESHOLD)));
		section5.getDashboardItems().add(dashboardItem31);

		DashboardDataGridItemDTO dashboardItem32 = new DashboardDataGridItemDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$LIKELIHOOD$LIKELIHOOD_GRID$ITEM_NAME));
		dashboardItem32.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$LIKELIHOOD$LIKELIHOOD_GRID$VENDOR_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$LIKELIHOOD$LIKELIHOOD_GRID$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$LIKELIHOOD$LIKELIHOOD_GRID$WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$LIKELIHOOD$LIKELIHOOD_GRID$TOTAL_WEIGHT_HEADER)
		), true);
		section5.getDashboardItems().add(dashboardItem32);

		for (VendorDataSeries dataSeries : likelihoodData) {
			List<DashboardDataItemDTO> rowItems = createRowItems(dataSeries);
			dashboardItem32.getGridItems().add(rowItems);
			applyVendorDashboardQualMetricDrilldown(rowItems, dataSeries.getVendor(), MetricDomain.LIKELIHOOD);

			List<DashboardDataItemDTO> chartItems = createChartItems(dataSeries);
			if (chartItems.size() > 0) chartItems.get(0).setDrilldown(DashboardDataItemDrilldownDTO.of(dataSeries.getVendor(), MetricDomain.LIKELIHOOD));
			dashboardItem31.getGridItems().add(chartItems);
		}
		*/

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getAdminDashboardDetails(Long riskModelId) {

		DashboardDTO dashboard = new DashboardDTO(3L, clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$DESCRIPTION), DashboardType.Admin);

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<String> roles = Arrays.asList(RoleType.SYSTEM_OWNER.role(), RoleType.VENDOR_OWNER.role());
		List<Users> ownersList = userRepository.filterUsersByOrganizationAndNameAndRoles(riskModel.getOrganizationId(), "", roles, Arrays.asList(0L), PageRequest.of(0, 1000000));
		List<Systems> systemList = systemRepository.getAllByOrganization(riskModel.getOrganizationId());
		List<Organizations> vendorList = organizationRepository.getListForRootOrganization(riskModel.getOrganizationId(), OrganizationType.Vendor);
		// List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		Set<Systems> systemsSet = systemList.stream().collect(Collectors.toSet());
		// Map<Systems, Set<Organizations>> associateVendorsMap = new HashMap<>();
		Map<Users, Set<Systems>> userToSystemsSetMap = new HashMap<>();
		Map<Users, Set<Organizations>> userToVendorsSetMap = new HashMap<>();
		for (Systems system : systemList) {
			if (system.getOwner() != null) {
				if (!userToSystemsSetMap.containsKey(system.getOwner())) {
					userToSystemsSetMap.put(system.getOwner(), new HashSet<>());
				}
				userToSystemsSetMap.get(system.getOwner()).add(system);
			}
		}
		for (Organizations vendor : vendorList) {
			if (vendor.getOwner() != null) {
				if (!userToVendorsSetMap.containsKey(vendor.getOwner())) {
					userToVendorsSetMap.put(vendor.getOwner(), new HashSet<>());
				}
				userToVendorsSetMap.get(vendor.getOwner()).add(vendor);
			}
		}

		/*
		// Create Associate Vendors Map
		for (AssociateVendors associateVendor : associateVendors) {
			for (Systems system : associateVendor.getSystems()) {
				if (!associateVendorsMap.containsKey(system)) {
					associateVendorsMap.put(system, new HashSet<>());
				}
				associateVendorsMap.get(system).add(associateVendor.getVendor());
			}
		}
		*/

		List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor);
		Map<Systems, SystemDataSeries> dataExfiltrationMap = getOrganizationQuantMetricData(riskModelId, QuantsDomain.DATA_EXFILTRATION, null, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> impactDataSystemMap = getQualMetricDataForSystems(riskModelId, MetricDomain.IMPACT, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> likelihoodDataSystemMap = getQualMetricDataForSystems(riskModelId, MetricDomain.LIKELIHOOD, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Organizations, VendorDataSeries> impactDataVendorMap = getQualMetricData(riskModelId, MetricDomain.IMPACT, null, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));
		Map<Organizations, VendorDataSeries> likelihoodDataVendorMap = getQualMetricData(riskModelId, MetricDomain.LIKELIHOOD, null, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));
		Map<Organizations, VendorDataSeries> impactDataCloudMap = getQualMetricData(riskModelId, MetricDomain.IMPACT, null, Arrays.asList(VendorType.Cloud)).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));
		Map<Organizations, VendorDataSeries> likelihoodDataCloudMap = getQualMetricData(riskModelId, MetricDomain.LIKELIHOOD, null, Arrays.asList(VendorType.Cloud)).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));
		// Map<Long, SystemDataSeries> summaryQualData = getLongSystemDataSeriesMap(impactData, likelihoodData);

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD(clientMessage);
		section1.setBreadcrumbs(breadcrumbsTop.extend("DASHBOARDS_ADMIN_1", "DASHBOARDS$ADMIN$NAME", "").getBreadcrumbs());


		// Initialize Organization Summary Scores
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(31l, clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$ITEM_NAME));

		// Add Top Header
		dashboardItem.addGridHeaders(Arrays.asList(
			"",
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$SYSTEM_VENDOR_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$SERVICE_VENDOR_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$TECHNOLOGY_VENDOR_HEADER)
		));

		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$PERSON_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$BUSINESS_UNIT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$SYSTEM_DATA_EXFILTRATION_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$IMPACT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$LIKELIHOOD_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$IMPACT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$LIKELIHOOD_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$IMPACT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ADMIN$SYSTEM_OWNER$LIKELIHOOD_HEADER)
		));
		dashboardItem.getGridHeaders().get(0).get(0).setColSpan(3l);
		dashboardItem.getGridHeaders().get(0).get(1).setColSpan(2l);
		dashboardItem.getGridHeaders().get(0).get(2).setColSpan(2l);
		dashboardItem.getGridHeaders().get(0).get(3).setColSpan(2l);

		// for (Map.Entry<Users, Set<Systems>> entry : userToSystemsSetMap.entrySet()) {
		for (Users systemOwner : ownersList) {
			// Users systemOwner = entry.getKey();
			Set<Systems> ownerSystemsSet = userToSystemsSetMap.get(systemOwner);
			Set<Organizations> ownerVendorsSet = userToVendorsSetMap.get(systemOwner);

			List<Double> itemsData = Arrays.asList(0D, 0D, 0D, 0D, 0D, 0D, 0D);
			List<Double> itemMetricData = Arrays.asList(0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D);
			if (ownerSystemsSet != null) {
				for (Systems system : ownerSystemsSet) {
					if (dataExfiltrationMap.containsKey(system)) {
						itemsData.set(0, itemsData.get(0) + dataExfiltrationMap.get(system).getItems().get(0));
					}
					/*
					if (impactDataSystemMap.containsKey(system)) {
						itemMetricData.set(1, itemMetricData.get(1) + impactDataSystemMap.get(system).getItems().get(1));
						itemMetricData.set(2, itemMetricData.get(2) + impactDataSystemMap.get(system).getItems().get(2));
					}
					if (likelihoodDataSystemMap.containsKey(system)) {
						itemMetricData.set(3, itemMetricData.get(3) + likelihoodDataSystemMap.get(system).getItems().get(1));
						itemMetricData.set(4, itemMetricData.get(4) + likelihoodDataSystemMap.get(system).getItems().get(2));
					}
					*/
				}
			}

			if (ownerVendorsSet != null) {
				for (Organizations vendor : ownerVendorsSet) {
					if (impactDataVendorMap.containsKey(vendor) && Boolean.TRUE.equals(vendor.getIsSystemVendor())) {
						itemMetricData.set(1, itemMetricData.get(1) + impactDataVendorMap.get(vendor).getItems().get(1));
						itemMetricData.set(2, itemMetricData.get(2) + impactDataVendorMap.get(vendor).getItems().get(2));
					}
					if (likelihoodDataVendorMap.containsKey(vendor) && Boolean.TRUE.equals(vendor.getIsSystemVendor())) {
						itemMetricData.set(3, itemMetricData.get(3) + likelihoodDataVendorMap.get(vendor).getItems().get(1));
						itemMetricData.set(4, itemMetricData.get(4) + likelihoodDataVendorMap.get(vendor).getItems().get(2));
					}
					if (impactDataVendorMap.containsKey(vendor) && Boolean.TRUE.equals(vendor.getIsServiceVendor())) {
						itemMetricData.set(5, itemMetricData.get(5) + impactDataVendorMap.get(vendor).getItems().get(1));
						itemMetricData.set(6, itemMetricData.get(6) + impactDataVendorMap.get(vendor).getItems().get(2));
					}
					if (likelihoodDataVendorMap.containsKey(vendor) && Boolean.TRUE.equals(vendor.getIsServiceVendor())) {
						itemMetricData.set(7, itemMetricData.get(7) + likelihoodDataVendorMap.get(vendor).getItems().get(1));
						itemMetricData.set(8, itemMetricData.get(8) + likelihoodDataVendorMap.get(vendor).getItems().get(2));
					}
					if (impactDataVendorMap.containsKey(vendor) && Boolean.TRUE.equals(vendor.getIsTechnologyVendor())) {
						itemMetricData.set(9, itemMetricData.get(9) + impactDataVendorMap.get(vendor).getItems().get(1));
						itemMetricData.set(10, itemMetricData.get(10) + impactDataVendorMap.get(vendor).getItems().get(2));
					}
					if (likelihoodDataVendorMap.containsKey(vendor) && Boolean.TRUE.equals(vendor.getIsTechnologyVendor())) {
						itemMetricData.set(11, itemMetricData.get(11) + likelihoodDataVendorMap.get(vendor).getItems().get(1));
						itemMetricData.set(12, itemMetricData.get(12) + likelihoodDataVendorMap.get(vendor).getItems().get(2));
					}
					/*
					if (impactDataCloudMap.containsKey(vendor) && Boolean.TRUE.equals(vendor.getIsTechnologyVendor())) {
						itemMetricData.set(9, itemMetricData.get(9) + impactDataCloudMap.get(vendor).getItems().get(1));
						itemMetricData.set(10, itemMetricData.get(10) + impactDataCloudMap.get(vendor).getItems().get(2));
					}
					if (likelihoodDataCloudMap.containsKey(vendor)) {
						itemMetricData.set(11, itemMetricData.get(11) + likelihoodDataCloudMap.get(vendor).getItems().get(1));
						itemMetricData.set(12, itemMetricData.get(12) + likelihoodDataCloudMap.get(vendor).getItems().get(2));
					}
					*/
				}
			}

			for (int i = 0; i < 6; i++) {
				// Build Target Metrics
				if (itemMetricData.get((i + 1) * 2) > 0) {
					itemsData.set(i + 1, itemMetricData.get(i * 2 + 1) / itemMetricData.get((i + 1) * 2));
				}
			}

			List<DashboardDataItemDTO>  itemsList = new ArrayList<>();
			itemsList.add(sI(systemOwner.getFullName()).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemOwner, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null)));
			if (systemOwner.getBusinessUnit() != null) {
				itemsList.add(sI(businessUnitService.getBusinessUnitPath(systemOwner.getBusinessUnit(), true)).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemOwner, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null)));
			} else {
				itemsList.add(sI(""));
			}
			itemsList.add($I(itemsData.get(0)).round(0).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemOwner, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUANT, null).param(DashboardDataItemDrilldownDTO.PARAM_CATEGORY, QuantsDomain.DATA_EXFILTRATION.getId().toString())));
			itemsList.add(sI(itemsData.get(1)).round(2));
			itemsList.add(sI(itemsData.get(2)).round(2));
			itemsList.add(sI(itemsData.get(3)).round(2));
			itemsList.add(sI(itemsData.get(4)).round(2));
			itemsList.add(sI(itemsData.get(5)).round(2));
			itemsList.add(sI(itemsData.get(6)).round(2));

			/*
			itemsList.add(sI(itemsData.get(1)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemOwner, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL, MetricDomain.IMPACT)));
			itemsList.add(sI(itemsData.get(2)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemOwner, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL, MetricDomain.LIKELIHOOD)));
			itemsList.add(sI(itemsData.get(3)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemOwner, DashboardDataItemDrilldownDTO.ADMIN_VNDOWN_QUAL, MetricDomain.IMPACT)));
			itemsList.add(sI(itemsData.get(4)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemOwner, DashboardDataItemDrilldownDTO.ADMIN_VNDOWN_QUAL, MetricDomain.LIKELIHOOD)));
			itemsList.add(sI(itemsData.get(5)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemOwner, DashboardDataItemDrilldownDTO.ADMIN_VNDOWN_QUAL_CLOUD, MetricDomain.IMPACT)));
			itemsList.add(sI(itemsData.get(6)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemOwner, DashboardDataItemDrilldownDTO.ADMIN_VNDOWN_QUAL_CLOUD, MetricDomain.LIKELIHOOD)));
			*/

			dashboardItem.getGridItems().add(itemsList);
		}
		section1.getDashboardItems().add(dashboardItem);

		return dashboard;
	}


	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public ByteArrayOutputStream getQuestionStatusReport(Long riskModelId) {

		ByteArrayOutputStream result = new ByteArrayOutputStream();

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<String> roles = Arrays.asList(RoleType.SYSTEM_OWNER.role(), RoleType.VENDOR_OWNER.role());
		List<Users> ownersList = userRepository.filterUsersByOrganizationAndNameAndRoles(riskModel.getOrganizationId(), "", roles, Arrays.asList(0L), PageRequest.of(0, 1000000));
		List<Systems> systemList = systemRepository.getAllByOrganization(riskModel.getOrganizationId());
		List<Organizations> vendorList = organizationRepository.getListForRootOrganization(riskModel.getOrganizationId(), OrganizationType.Vendor);
		// List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		Set<Systems> systemsSet = systemList.stream().collect(Collectors.toSet());
		Set<Organizations> vendorsSet = vendorList.stream().collect(Collectors.toSet());
		Set<Organizations> cloudVendorsSet = vendorList.stream().filter(organizations -> Boolean.TRUE.equals(organizations.getIsCloudVendor())).collect(Collectors.toSet());
		// Map<Systems, Set<Organizations>> associateVendorsMap = new HashMap<>();
		Map<Users, Set<Systems>> userToSystemsSetMap = new HashMap<>();
		Map<Users, Set<Organizations>> userToVendorsSetMap = new HashMap<>();
		for (Systems system : systemList) {
			if (system.getOwner() != null) {
				if (!userToSystemsSetMap.containsKey(system.getOwner())) {
					userToSystemsSetMap.put(system.getOwner(), new HashSet<>());
				}
				userToSystemsSetMap.get(system.getOwner()).add(system);
			}
		}
		for (Organizations vendor : vendorList) {
			if (vendor.getOwner() != null) {
				if (!userToVendorsSetMap.containsKey(vendor.getOwner())) {
					userToVendorsSetMap.put(vendor.getOwner(), new HashSet<>());
				}
				userToVendorsSetMap.get(vendor.getOwner()).add(vendor);
			}
		}

		// Build Dashboard Data
		List<VendorType> systemTypes = Arrays.asList(VendorType.Both, VendorType.System);
		List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor);
		List<VendorType> cloudTypes = Arrays.asList(VendorType.Both, VendorType.Cloud);

		List<QualitativeQuestions> allSystemQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndType(riskModelId, systemTypes);
		long totalSystemQuestions = QuestionStatusDashboardService.calculateQuestionsCount(allSystemQuestions);
		List<QuestionAnswersForSystem> allSystemQuestionsAnswersList = questionAnswersForSystemRepository.getListByRiskModelAndScoringTypes(riskModelId, systemTypes);
		Map<Systems, List<QuestionAnswersForSystem>> allSystemQuestionsAnswersMap = allSystemQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForSystem::getSystem));

		List<QualitativeQuestions> allVendorQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndType(riskModelId, vendorTypes);
		long totalVendorQuestions = QuestionStatusDashboardService.calculateQuestionsCount(allVendorQuestions);
		List<QuestionAnswersForVendor> allVendorQuestionsAnswersList = questionAnswersForVendorRepository.getListByRiskModelAndVendorTypes(riskModelId, Arrays.asList(VendorType.Vendor, VendorType.Both)).stream().filter(item -> item.getAnswer() != null).toList();
		Map<Organizations, List<QuestionAnswersForVendor>> allVendorQuestionsAnswersMap = allVendorQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForVendor::getVendor));

		List<QualitativeQuestions> allCloudQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndType(riskModelId, cloudTypes);
		long totalCloudQuestions = QuestionStatusDashboardService.calculateQuestionsCount(allCloudQuestions);
		List<QuestionAnswersForVendor> allCloudQuestionsAnswersList = questionAnswersForVendorRepository.getListByRiskModelAndVendorTypes(riskModelId, Arrays.asList(VendorType.Cloud)).stream().filter(item -> item.getAnswer() != null).toList();
		Map<Organizations, List<QuestionAnswersForVendor>> allCloudQuestionsAnswersMap = allCloudQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForVendor::getVendor));

		DataTypeClassification piiDataType = dataTypeClassificationRepository.findById(DataTypeDomain.PII.getId()).orElse(null);


		Cell cell;

		// Create a Workbook
		Workbook workbook = new XSSFWorkbook();

		/* CreationHelper helps us create instances of various things like DataFormat,
           Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
        CreationHelper createHelper = workbook.getCreationHelper();

		// Sheet sheet3 = workbook.createSheet("Business Units");

		// XSSFDataFormat numberFormat = (XSSFDataFormat) workbook.createDataFormat();

		// Create a Font for styling header cells
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontName("Calibri");
		headerFont.setFontHeightInPoints((short) 13);
		headerFont.setColor(IndexedColors.BLACK1.getIndex());
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
		headerCellStyle.setWrapText(true);
		// headerCellStyle.setIndention((short) 2);

		// Create Usual Cell Font
		Font cellFont = workbook.createFont();
		cellFont.setBold(false);
		cellFont.setFontName("Calibri");
		cellFont.setFontHeightInPoints((short) 11);
		cellFont.setColor(IndexedColors.BLACK1.getIndex());
		CellStyle bodyCellStyle = workbook.createCellStyle();
		bodyCellStyle.setFont(cellFont);

		// Create Centered Text
		Font cellFontCenter = workbook.createFont();
		cellFontCenter.setBold(false);
		cellFontCenter.setFontName("Calibri");
		cellFontCenter.setFontHeightInPoints((short) 11);
		cellFontCenter.setColor(IndexedColors.BLACK.getIndex());
		CellStyle bodyCellStyleCenter = workbook.createCellStyle();
		bodyCellStyleCenter.setFont(cellFontCenter);
		bodyCellStyleCenter.setAlignment(HorizontalAlignment.CENTER);
		bodyCellStyleCenter.setWrapText(true);

		// Create RIGHT Text format
		Font cellFontRight = workbook.createFont();
		cellFontRight.setBold(false);
		cellFontRight.setFontName("Calibri");
		cellFontRight.setFontHeightInPoints((short) 11);
		cellFontRight.setColor(IndexedColors.BLACK.getIndex());
		CellStyle bodyCellStyleRight = workbook.createCellStyle();
		bodyCellStyleRight.setAlignment(HorizontalAlignment.RIGHT);
		bodyCellStyleRight.setFont(cellFontRight);

		Font cellFontRed = workbook.createFont();
		cellFontRed.setBold(false);
		cellFontRed.setFontName("Calibri");
		cellFontRed.setFontHeightInPoints((short) 11);
		cellFontRed.setColor(IndexedColors.RED.getIndex());
		CellStyle bodyCellStyleRed = workbook.createCellStyle();
		bodyCellStyleRed.setFont(cellFontRed);

		Font cellFontGreen = workbook.createFont();
		cellFontGreen.setBold(false);
		cellFontGreen.setFontName("Calibri");
		cellFontGreen.setFontHeightInPoints((short) 11);
		cellFontGreen.setColor(IndexedColors.BRIGHT_GREEN.getIndex());
		CellStyle bodyCellStyleGreen = workbook.createCellStyle();
		bodyCellStyleGreen.setFont(cellFontGreen);

		// Create Cell Style for formatting Date
		CellStyle dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

		// ========== ========== ========== ========== ========== SHEET 1. Systems Status ========== ========== ========== ========== ========== //
		if (totalSystemQuestions > 0) {
			// Create a Sheet
			Sheet sheet1 = workbook.createSheet("Systems Status");

			// Set Column Width
			sheet1.setColumnWidth(0, 40 * 256);
			sheet1.setColumnWidth(1, 40 * 256);
			sheet1.setColumnWidth(2, 40 * 256);
			sheet1.setColumnWidth(3, 40 * 256);
			sheet1.setColumnWidth(4, 20 * 256);
			sheet1.setColumnWidth(5, 20 * 256);
			sheet1.setColumnWidth(6, 20 * 256);

			// Create a Row
			Row headerRow01 = sheet1.createRow(0);
			Row headerRow02 = sheet1.createRow(1);

			// Create Header for First Sheet
			cell = headerRow01.createCell(0);
			cell.setCellValue("Person");
			cell.setCellStyle(headerCellStyle);
			// sheet1.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));

			cell = headerRow01.createCell(1);
			cell.setCellValue("System Name");
			cell.setCellStyle(headerCellStyle);
			// sheet1.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));

			cell = headerRow01.createCell(2);
			cell.setCellValue("Business Unit");
			cell.setCellStyle(headerCellStyle);
			// sheet1.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));

			cell = headerRow01.createCell(3);
			cell.setCellValue("System Questionnaire Status");
			cell.setCellStyle(headerCellStyle);
			// sheet1.addMergedRegion(new CellRangeAddress(0, 0, 3, 4));

			/*
			cell = headerRow02.createCell(3);
			cell.setCellValue("Impact");
			cell.setCellStyle(headerCellStyle);

			cell = headerRow02.createCell(4);
			cell.setCellValue("Likelihood");
			cell.setCellStyle(headerCellStyle);

			cell = headerRow01.createCell(5);
			cell.setCellValue("Data Exfiltration Question");
			cell.setCellStyle(headerCellStyle);
			sheet1.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));

			cell = headerRow01.createCell(6);
			cell.setCellValue("Business Interruption Question");
			cell.setCellStyle(headerCellStyle);
			sheet1.addMergedRegion(new CellRangeAddress(0, 1, 6, 6));
			*/

			int i = 1;
			for (Systems system : systemsSet) {

				Row bodyRow = sheet1.createRow(i);

				String ownerName = system.getOwner() != null ? system.getOwner().getFullName() : "";

				cell = bodyRow.createCell(0);
				cell.setCellValue(ownerName);
				cell.setCellStyle(bodyCellStyle);

				cell = bodyRow.createCell(1);
				cell.setCellValue(system.getName());
				cell.setCellStyle(bodyCellStyle);

				String businessUnit = (system.getOwner() != null && system.getOwner().getBusinessUnit() != null) ? businessUnitService.getBusinessUnitPath(system.getOwner().getBusinessUnit(), true) : "";
				cell = bodyRow.createCell(2);
				cell.setCellValue(businessUnit);
				cell.setCellStyle(bodyCellStyle);


				if (Boolean.TRUE.equals(system.getIsEtl())) {
					cell = bodyRow.createCell(3);
					cell.setCellStyle(bodyCellStyleCenter);
					cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE));
					cell.setCellType(CellType.STRING);
				} else {
					int systemAnswersCount = allSystemQuestionsAnswersMap.containsKey(system) ? allSystemQuestionsAnswersMap.get(system).size() : 0;
					cell = bodyRow.createCell(3);
					if (systemAnswersCount == 0) {
						cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED));
						cell.setCellStyle(bodyCellStyleRed);
					} else if (systemAnswersCount >= totalSystemQuestions) {
						cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE));
						cell.setCellStyle(bodyCellStyleGreen);
					} else {
						cell.setCellValue(MessageFormat.format("{0}/{1}", systemAnswersCount, totalSystemQuestions));
						cell.setCellStyle(bodyCellStyleRight);
					}
				}

				boolean isPIISystem = system.getDataTypeClassifications().contains(piiDataType);

				/*
				cell = bodyRow.createCell(5);
				// Data Exfiltration Question
				if (isPIISystem) {
					if (system.getNumberOfRecProcessed() != null) {
						cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE));
						cell.setCellStyle(bodyCellStyleGreen);
					} else {
						cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_COMPLETE));
						cell.setCellStyle(bodyCellStyleRed);
					}
				} else {
					cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE));
					cell.setCellStyle(bodyCellStyleCenter);
				}

				// Business Interruption Question
				cell = bodyRow.createCell(6);
				cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE));
				cell.setCellStyle(bodyCellStyleCenter);
				*/

				i++;
			}

		}

		// ========== ========== ========== ========== ========== SHEET 1. Systems Status END ========== ========== ========== ========== ========== //


		// ========== ========== ========== ========== ========== SHEET 2. Vendors Status ========== ========== ========== ========== ========== //

		if (totalVendorQuestions > 0 || totalCloudQuestions > 0) {
			Sheet sheet2 = workbook.createSheet("Vendors Status");

			// Set Column Width
			sheet2.setColumnWidth(0, 40 * 256);
			sheet2.setColumnWidth(1, 40 * 256);
			sheet2.setColumnWidth(2, 40 * 256);
			sheet2.setColumnWidth(3, 40 * 256);
			sheet2.setColumnWidth(4, 40 * 256);
			sheet2.setColumnWidth(5, 20 * 256);
			sheet2.setColumnWidth(6, 20 * 256);

			// Create a Row
			Row headerRow11 = sheet2.createRow(0);
			// Row headerRow12 = sheet2.createRow(1);

			// Create Header for First Sheet
			cell = headerRow11.createCell(0);
			cell.setCellValue("Person");
			cell.setCellStyle(headerCellStyle);
			// sheet2.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));

			cell = headerRow11.createCell(1);
			cell.setCellValue("Vendor Name");
			cell.setCellStyle(headerCellStyle);
			// sheet2.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));

			cell = headerRow11.createCell(2);
			cell.setCellValue("Business Unit");
			cell.setCellStyle(headerCellStyle);
			// sheet2.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));

			cell = headerRow11.createCell(3);
			cell.setCellValue("Vendor Questionnaire Status");
			cell.setCellStyle(headerCellStyle);
			// sheet2.addMergedRegion(new CellRangeAddress(0, 0, 3, 4));

			/*
			cell = headerRow12.createCell(3);
			cell.setCellValue("Impact");
			cell.setCellStyle(headerCellStyle);

			cell = headerRow12.createCell(4);
			cell.setCellValue("Likelihood");
			cell.setCellStyle(headerCellStyle);
			*/

			if (totalCloudQuestions > 0) {
				cell = headerRow11.createCell(4);
				cell.setCellValue("Cloud Questionnaire Status");
				cell.setCellStyle(headerCellStyle);
				// sheet2.addMergedRegion(new CellRangeAddress(0, 0, 5, 6));
			}

			/*
			cell = headerRow12.createCell(5);
			cell.setCellValue("Impact");
			cell.setCellStyle(headerCellStyle);

			cell = headerRow12.createCell(6);
			cell.setCellValue("Likelihood");
			cell.setCellStyle(headerCellStyle);
			*/

			int i = 1;
			for (Organizations vendor : vendorsSet) {

				Row bodyRow = sheet2.createRow(i);

				String ownerName = vendor.getOwner() != null ? vendor.getOwner().getFullName() : "";

				cell = bodyRow.createCell(0);
				cell.setCellValue(ownerName);
				cell.setCellStyle(bodyCellStyle);

				cell = bodyRow.createCell(1);
				cell.setCellValue(vendor.getName());
				cell.setCellStyle(bodyCellStyle);

				String businessUnit = (vendor.getOwner() != null && vendor.getOwner().getBusinessUnit() != null) ? businessUnitService.getBusinessUnitPath(vendor.getOwner().getBusinessUnit(), true) : "";
				cell = bodyRow.createCell(2);
				cell.setCellValue(businessUnit);
				cell.setCellStyle(bodyCellStyle);

				// Vendor Questionnaire Status
				int vendorAnswersCount = allVendorQuestionsAnswersMap.containsKey(vendor) ? allVendorQuestionsAnswersMap.get(vendor).size() : 0;

				cell = bodyRow.createCell(3);
				if (vendorAnswersCount == 0) {
					cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED));
					cell.setCellStyle(bodyCellStyleRed);
				} else if (vendorAnswersCount >= totalVendorQuestions) {
					cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE));
					cell.setCellStyle(bodyCellStyleGreen);
				} else {
					cell.setCellValue(MessageFormat.format("{0}/{1}", vendorAnswersCount, totalVendorQuestions));
					cell.setCellStyle(bodyCellStyleRight);
				}

				// Cloud Questionnaire Status
				if (totalCloudQuestions > 0) {
					if (Boolean.TRUE.equals(vendor.getIsCloudVendor())) {
						int cloudAnswersCount = allCloudQuestionsAnswersMap.containsKey(vendor) ? allCloudQuestionsAnswersMap.get(vendor).size() : 0;
						// System Questionnaire Status
						cell = bodyRow.createCell(4);
						if (cloudAnswersCount == 0) {
							cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED));
							cell.setCellStyle(bodyCellStyleRed);
						} else if (cloudAnswersCount >= totalCloudQuestions) {
							cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE));
							cell.setCellStyle(bodyCellStyleGreen);
						} else {
							cell.setCellValue(MessageFormat.format("{0}/{1}", cloudAnswersCount, totalCloudQuestions));
							cell.setCellStyle(bodyCellStyleRight);
						}

						/*
						cell = bodyRow.createCell(6);
						if (likelihoodCloudAnswersCount == 0) {
							cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED));
							cell.setCellStyle(bodyCellStyleRed);
						} else if (likelihoodCloudAnswersCount >= totalLikelihoodCloudQuestions) {
							cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE));
							cell.setCellStyle(bodyCellStyleGreen);
						} else {
							cell.setCellValue(MessageFormat.format("{0}/{1}", likelihoodCloudAnswersCount, totalLikelihoodCloudQuestions));
							cell.setCellStyle(bodyCellStyleRight);
						}
						*/
					} else {
						cell = bodyRow.createCell(4);
						cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE));
						cell.setCellStyle(bodyCellStyleCenter);
						sheet2.addMergedRegion(new CellRangeAddress(i, i, 5, 6));
					}
				}

				i++;
			}

		}

		// ========== ========== ========== ========== ========== SHEET 2. Vendors Status END ========== ========== ========== ========== ========== //

		// ========== ========== ========== ========== ========== SHEET 3. Business Units ========== ========== ========== ========== ========== //

		// Set Column Width
		/*
		sheet3.setColumnWidth(0, 50 * 256);
		sheet3.setColumnWidth(1, 15 * 256);
		sheet3.setColumnWidth(2, 15 * 256);
		sheet3.setColumnWidth(3, 15 * 256);
		sheet3.setColumnWidth(4, 15 * 256);
		sheet3.setColumnWidth(5, 15 * 256);
		sheet3.setColumnWidth(6, 15 * 256);
		sheet3.setColumnWidth(7, 15 * 256);
		sheet3.setColumnWidth(8, 15 * 256);

		// Create a Row
		Row headerRow21 = sheet3.createRow(0);
		Row headerRow22 = sheet3.createRow(1);

		// Create Header for First Sheet
		cell = headerRow21.createCell(0);
		cell.setCellValue("Business Unit");
		cell.setCellStyle(headerCellStyle);
		sheet3.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));

		cell = headerRow21.createCell(1);
		cell.setCellValue("Total Status");
		cell.setCellStyle(headerCellStyle);
		sheet3.addMergedRegion(new CellRangeAddress(0, 0, 1, 2));

		cell = headerRow22.createCell(1);
		cell.setCellValue("Impact");
		cell.setCellStyle(headerCellStyle);

		cell = headerRow22.createCell(2);
		cell.setCellValue("Likelihood");
		cell.setCellStyle(headerCellStyle);

		cell = headerRow21.createCell(3);
		cell.setCellValue("System Questionnaire Status");
		cell.setCellStyle(headerCellStyle);
		sheet3.addMergedRegion(new CellRangeAddress(0, 0, 3, 4));

		cell = headerRow22.createCell(3);
		cell.setCellValue("Impact");
		cell.setCellStyle(headerCellStyle);

		cell = headerRow22.createCell(4);
		cell.setCellValue("Likelihood");
		cell.setCellStyle(headerCellStyle);

		cell = headerRow21.createCell(5);
		cell.setCellValue("Vendor Questionnaire Status");
		cell.setCellStyle(headerCellStyle);
		sheet3.addMergedRegion(new CellRangeAddress(0, 0, 5, 6));

		cell = headerRow22.createCell(5);
		cell.setCellValue("Impact");
		cell.setCellStyle(headerCellStyle);

		cell = headerRow22.createCell(6);
		cell.setCellValue("Likelihood");
		cell.setCellStyle(headerCellStyle);

		cell = headerRow21.createCell(7);
		cell.setCellValue("Cloud Questionnaire Status");
		cell.setCellStyle(headerCellStyle);
		sheet3.addMergedRegion(new CellRangeAddress(0, 0, 7, 8));

		cell = headerRow22.createCell(7);
		cell.setCellValue("Impact");
		cell.setCellStyle(headerCellStyle);

		cell = headerRow22.createCell(8);
		cell.setCellValue("Likelihood");
		cell.setCellStyle(headerCellStyle);

		Map<BusinessUnits, List<QuestionAnswersForSystem>> impactQuestionsAnswersMap4BU = getMapOfSystemAnswersByBusinessUnitAndSystem(impactQuestionsAnswersList);
		Map<BusinessUnits, List<QuestionAnswersForSystem>> likelihoodQuestionsAnswersMap4BU = getMapOfSystemAnswersByBusinessUnitAndSystem(likelihoodQuestionsAnswersList);
		Map<BusinessUnits, List<QuestionAnswersForVendor>> impactVendorQuestionsAnswersMap4BU = getMapOfVendorAnswersByBusinessUnit(impactVendorQuestionsAnswersList);
		Map<BusinessUnits, List<QuestionAnswersForVendor>> likelihoodVendorQuestionsAnswersMap4BU = getMapOfVendorAnswersByBusinessUnit(likelihoodVendorQuestionsAnswersList);
		Map<BusinessUnits, List<QuestionAnswersForVendor>> impactCloudQuestionsAnswersMap4BU = getMapOfVendorAnswersByBusinessUnit(impactCloudQuestionsAnswersList);
		Map<BusinessUnits, List<QuestionAnswersForVendor>> likelihoodCloudQuestionsAnswersMap4BU = getMapOfVendorAnswersByBusinessUnit(likelihoodCloudQuestionsAnswersList);

		Map<BusinessUnits, List<Systems>> businessUnitSystemsMap = systemsSet.stream().filter(o -> o.getBusinessUnit() != null).collect(Collectors.groupingBy(o -> o.getBusinessUnit()));
		Map<BusinessUnits, List<Organizations>> businessUnitVendorsMap = vendorsSet.stream().filter(o -> o.getOwner() != null && o.getOwner().getBusinessUnit() != null).collect(Collectors.groupingBy(o -> o.getOwner().getBusinessUnit()));
		Map<BusinessUnits, List<Organizations>> businessUnitCloudVendorsMap = cloudVendorsSet.stream().filter(o -> o.getOwner() != null && o.getOwner().getBusinessUnit() != null).collect(Collectors.groupingBy(o -> o.getOwner().getBusinessUnit()));

		Set<BusinessUnits> allInvolvedBusinessUnits = new HashSet<>();
		allInvolvedBusinessUnits.addAll(businessUnitSystemsMap.keySet());
		allInvolvedBusinessUnits.addAll(businessUnitVendorsMap.keySet());

		CellStyle stylePercentage = workbook.createCellStyle();
		stylePercentage.setFont(cellFont);
		stylePercentage.setDataFormat(workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat( 10 )));

		CellStyle stylePercentageGreen = workbook.createCellStyle();
		stylePercentageGreen.setFont(cellFontGreen);
		stylePercentageGreen.setDataFormat(workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat( 10 )));

		i = 2;
		for (BusinessUnits businessUnit : allInvolvedBusinessUnits) {

			String businessUnitPath = businessUnitService.getBusinessUnitPath(businessUnit, true);

			Row bodyRow = sheet3.createRow(i);

			cell = bodyRow.createCell(0);
			cell.setCellValue(businessUnitPath);
			cell.setCellStyle(bodyCellStyle);

			Cell totalImpactCell = bodyRow.createCell(1);
			totalImpactCell.setCellType(CellType.NUMERIC);
			totalImpactCell.setCellStyle(stylePercentage);
			Cell totalLikelihoodCell = bodyRow.createCell(2);
			totalLikelihoodCell.setCellType(CellType.NUMERIC);
			totalLikelihoodCell.setCellStyle(stylePercentage);

			double totalBUImpactQuestionsCount = 0d;
			double totalBULikelihoodQuestionsCount = 0d;
			double answeredBUImpactQuestionsCount = 0d;
			double answeredBULikelihoodQuestionsCount = 0d;

			if (businessUnitSystemsMap.containsKey(businessUnit)) {
				int businessUnitSystemsCount = businessUnitSystemsMap.get(businessUnit).size();
				int totalBUImpactQuestions = (businessUnitSystemsCount > 0 && totalImpactQuestions > 0) ? businessUnitSystemsCount * totalImpactQuestions : 1;
				int totalBULikelihoodQuestions = (businessUnitSystemsCount > 0 && totalLikelihoodQuestions > 0) ? businessUnitSystemsCount * totalLikelihoodQuestions : 1;
				int buImpactQuestionAnswers = (impactQuestionsAnswersMap4BU.containsKey(businessUnit)) ? impactQuestionsAnswersMap4BU.get(businessUnit).size() : 0;
				int buLikelihoodQuestionAnswers = (likelihoodQuestionsAnswersMap4BU.containsKey(businessUnit)) ? likelihoodQuestionsAnswersMap4BU.get(businessUnit).size() : 0;
				Double buImpactQuestionsAnsweredPercent = Double.valueOf(buImpactQuestionAnswers) / Double.valueOf(totalBUImpactQuestions);
				Double buLikelihoodQuestionsAnsweredPercent = Double.valueOf(buLikelihoodQuestionAnswers) / Double.valueOf(totalBULikelihoodQuestions);
				cell = bodyRow.createCell(3);
				cell.setCellType(CellType.NUMERIC);
				if (buImpactQuestionsAnsweredPercent >= 0.99d) {
					cell.setCellValue(1d);
					cell.setCellStyle(stylePercentageGreen);
				} else {
					cell.setCellValue(buImpactQuestionsAnsweredPercent);
					cell.setCellStyle(stylePercentage);
				}

				cell = bodyRow.createCell(4);
				cell.setCellType(CellType.NUMERIC);
				if (buLikelihoodQuestionsAnsweredPercent >= 0.99d) {
					cell.setCellValue(1d);
					cell.setCellStyle(stylePercentageGreen);
				} else {
					cell.setCellValue(buLikelihoodQuestionsAnsweredPercent);
					cell.setCellStyle(stylePercentage);
				}

				totalBUImpactQuestionsCount += totalBUImpactQuestions;
				totalBULikelihoodQuestionsCount += totalBULikelihoodQuestions;
				answeredBUImpactQuestionsCount += buImpactQuestionAnswers;
				answeredBULikelihoodQuestionsCount += buLikelihoodQuestionAnswers;
			} else {
				cell = bodyRow.createCell(3);
				cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE));
				sheet3.addMergedRegion(new CellRangeAddress(i, i, 3, 4));
				cell.setCellStyle(bodyCellStyleCenter);
			}

			if (businessUnitVendorsMap.containsKey(businessUnit)) {
				int businessUnitVendorsCount = businessUnitVendorsMap.get(businessUnit).size();
				int totalBUVendorImpactQuestions = (businessUnitVendorsCount > 0 && totalImpactVendorQuestions > 0) ? businessUnitVendorsCount * totalImpactVendorQuestions : 1;
				int totalBUVendorLikelihoodQuestions = (businessUnitVendorsCount > 0 && totalLikelihoodVendorQuestions > 0) ? businessUnitVendorsCount * totalLikelihoodVendorQuestions : 1;
				int buImpactVendorQuestionAnswers = (impactVendorQuestionsAnswersMap4BU.containsKey(businessUnit)) ? impactVendorQuestionsAnswersMap4BU.get(businessUnit).size() : 0;
				int buLikelihoodVendorQuestionAnswers = (likelihoodVendorQuestionsAnswersMap4BU.containsKey(businessUnit)) ? likelihoodVendorQuestionsAnswersMap4BU.get(businessUnit).size() : 0;
				Double buImpactVendorQuestionsAnsweredPercent = Double.valueOf(buImpactVendorQuestionAnswers) / Double.valueOf(totalBUVendorImpactQuestions);
				Double buLikelihoodVendorQuestionsAnsweredPercent = Double.valueOf(buLikelihoodVendorQuestionAnswers) / Double.valueOf(totalBUVendorLikelihoodQuestions);

				cell = bodyRow.createCell(5);
				cell.setCellType(CellType.NUMERIC);
				cell.setCellStyle(stylePercentage);
				if (buImpactVendorQuestionsAnsweredPercent >= 0.99d) {
					cell.setCellValue(1d);
					cell.setCellStyle(stylePercentageGreen);
				} else {
					cell.setCellValue(buImpactVendorQuestionsAnsweredPercent);
					cell.setCellStyle(stylePercentage);
				}

				cell = bodyRow.createCell(6);
				cell.setCellType(CellType.NUMERIC);
				cell.setCellStyle(stylePercentage);
				if (buLikelihoodVendorQuestionsAnsweredPercent >= 0.99d) {
					cell.setCellValue(1d);
					cell.setCellStyle(stylePercentageGreen);
				} else {
					cell.setCellValue(buLikelihoodVendorQuestionsAnsweredPercent);
					cell.setCellStyle(stylePercentage);
				}

				totalBUImpactQuestionsCount += totalBUVendorImpactQuestions;
				totalBULikelihoodQuestionsCount += totalBUVendorLikelihoodQuestions;
				answeredBUImpactQuestionsCount += buImpactVendorQuestionAnswers;
				answeredBULikelihoodQuestionsCount += buLikelihoodVendorQuestionAnswers;
			} else {
				cell = bodyRow.createCell(5);
				cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE));
				sheet3.addMergedRegion(new CellRangeAddress(i, i, 5, 6));
				cell.setCellStyle(bodyCellStyleCenter);
			}

			if (businessUnitCloudVendorsMap.containsKey(businessUnit)) {
				int businessUnitCloudsCount = businessUnitCloudVendorsMap.get(businessUnit).size();
				int totalBUCloudImpactQuestions = (businessUnitCloudsCount > 0 && totalImpactCloudQuestions > 0) ? businessUnitCloudsCount * totalImpactCloudQuestions : 1;
				int totalBUCloudLikelihoodQuestions = (businessUnitCloudsCount > 0 && totalLikelihoodCloudQuestions > 0) ? businessUnitCloudsCount * totalLikelihoodCloudQuestions : 1;
				int buImpactCloudQuestionAnswers = (impactCloudQuestionsAnswersMap4BU.containsKey(businessUnit)) ? impactCloudQuestionsAnswersMap4BU.get(businessUnit).size() : 0;
				int buLikelihoodCloudQuestionAnswers = (likelihoodCloudQuestionsAnswersMap4BU.containsKey(businessUnit)) ? likelihoodCloudQuestionsAnswersMap4BU.get(businessUnit).size() : 0;
				Double buImpactCloudQuestionsAnsweredPercent = Double.valueOf(buImpactCloudQuestionAnswers) / Double.valueOf(totalBUCloudImpactQuestions);
				Double buLikelihoodCloudQuestionsAnsweredPercent = Double.valueOf(buLikelihoodCloudQuestionAnswers) / Double.valueOf(totalBUCloudLikelihoodQuestions);

				cell = bodyRow.createCell(7);
				cell.setCellType(CellType.NUMERIC);
				cell.setCellStyle(stylePercentage);
				if (buImpactCloudQuestionsAnsweredPercent >= 0.99d) {
					cell.setCellValue(1d);
					cell.setCellStyle(stylePercentageGreen);
				} else {
					cell.setCellValue(buImpactCloudQuestionsAnsweredPercent);
					cell.setCellStyle(stylePercentage);
				}

				cell = bodyRow.createCell(8);
				cell.setCellType(CellType.NUMERIC);
				cell.setCellStyle(stylePercentage);
				if (buLikelihoodCloudQuestionsAnsweredPercent >= 0.99d) {
					cell.setCellValue(1d);
					cell.setCellStyle(stylePercentageGreen);
				} else {
					cell.setCellValue(buLikelihoodCloudQuestionsAnsweredPercent);
					cell.setCellStyle(stylePercentage);
				}

				totalBUImpactQuestionsCount += totalBUCloudImpactQuestions;
				totalBULikelihoodQuestionsCount += totalBUCloudLikelihoodQuestions;
				answeredBUImpactQuestionsCount += buImpactCloudQuestionAnswers;
				answeredBULikelihoodQuestionsCount += buLikelihoodCloudQuestionAnswers;
			} else {
				cell = bodyRow.createCell(7);
				cell.setCellValue(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE));
				sheet3.addMergedRegion(new CellRangeAddress(i, i, 7, 8));
				cell.setCellStyle(bodyCellStyleCenter);
			}

			// Set Total Values
			Double buTotalImpactQuestionsAnsweredPercent = answeredBUImpactQuestionsCount / totalBUImpactQuestionsCount;
			Double buTotalLikelihoodQuestionsAnsweredPercent = Double.valueOf(answeredBULikelihoodQuestionsCount) / Double.valueOf(totalBULikelihoodQuestionsCount);
			if (buTotalImpactQuestionsAnsweredPercent >= 0.99d) {
				totalImpactCell.setCellValue(1d);
				totalImpactCell.setCellStyle(stylePercentageGreen);
			} else {
				totalImpactCell.setCellValue(buTotalImpactQuestionsAnsweredPercent);
				totalImpactCell.setCellStyle(stylePercentage);
			}
			if (buTotalLikelihoodQuestionsAnsweredPercent >= 0.99d) {
				totalLikelihoodCell.setCellValue(1d);
				totalLikelihoodCell.setCellStyle(stylePercentageGreen);
			} else {
				totalLikelihoodCell.setCellValue(buTotalLikelihoodQuestionsAnsweredPercent);
				totalLikelihoodCell.setCellStyle(stylePercentage);
			}

			i++;
		}
		*/

		// ========== ========== ========== ========== ========== SHEET 3. Business Units END ========== ========== ========== ========== ========== //

		try {
			workbook.write(result);

			// Closing the workbook
			workbook.close();

		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Get Assignment Status Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getAssignmentStatusDashboardDetails(Long riskModelId) {

		DashboardDTO dashboard = new DashboardDTO(3L, clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$DESCRIPTION), DashboardType.Admin);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.SET_UP_DASHBOARD(clientMessage).add("SET_UP_ASSIGNMENT_STATUS", SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$NAME, "/private/dashboards/36");

		DashboardSectionDTO section1 = new DashboardSectionDTO(3601L, clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$ITEM_DESCRIPTION));
		dashboard.getSections().add(section1);
		DashboardTableItemDTO dashboardItem1 = new DashboardTableItemDTO(3601L, clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$SYSTEM_ASSIGNMENTS$ITEM_NAME), true);
		section1.getDashboardItems().add(dashboardItem1);

		DashboardSectionDTO section2 = new DashboardSectionDTO(3602L, clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$ITEM_DESCRIPTION));
		dashboard.getSections().add(section2);
		DashboardTableItemDTO dashboardItem2 = new DashboardTableItemDTO(3602L, clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$PEOPLE_ASSIGNMENTS$ITEM_NAME), true);
		section2.getDashboardItems().add(dashboardItem2);

		// Create breadcrumbs
		section1.setBreadcrumbs(breadcrumbsTop.extend("SET_UP_ASSIGNMENT_STATUS_1", SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$ITEM_NAME, "").getBreadcrumbs());
		section2.setBreadcrumbs(breadcrumbsTop.extend("SET_UP_ASSIGNMENT_STATUS_2", SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$ITEM_NAME, "").getBreadcrumbs());

		// ========== ========== ========== ========== ========== System Assignments ========== ========== ========== ========== ========== //


		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<UserAssignedSystem> userSystemAssignments = userAssignedSystemRepository.getSystemAssignmentsForOrganization(organization.getId());
		Map<Systems, Set<Users>> systemAssignmentsMap = userSystemAssignments.stream().filter(userAssignedSystem -> userAssignedSystem.getSystem() != null && userAssignedSystem.getUser() != null).collect(Collectors.groupingBy(UserAssignedSystem::getSystem, Collectors.mapping(UserAssignedSystem::getUser, Collectors.toSet())));
		Map<Users, Set<Systems>> userAssignmentsMap = userSystemAssignments.stream().filter(userAssignedSystem -> userAssignedSystem.getSystem() != null && userAssignedSystem.getUser() != null).collect(Collectors.groupingBy(UserAssignedSystem::getUser, Collectors.mapping(UserAssignedSystem::getSystem, Collectors.toSet())));

		List<String> roles = Arrays.asList(RoleType.SYSTEM_OWNER.role(), RoleType.VENDOR_OWNER.role());
		List<Users> ownersList = userRepository.filterUsersByOrganizationAndNameAndRoles(riskModel.getOrganizationId(), "", roles, Arrays.asList(0L), PageRequest.of(0, 1000000));
		List<Systems> systemList = systemRepository.getAllByOrganization(riskModel.getOrganizationId());
		List<Organizations> vendorList = organizationRepository.getListForRootOrganization(riskModel.getOrganizationId(), OrganizationType.Vendor);
		// List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		Set<Systems> systemsSet = systemList.stream().collect(Collectors.toSet());
		Set<Organizations> vendorsSet = vendorList.stream().collect(Collectors.toSet());
		Set<Organizations> cloudVendorsSet = vendorList.stream().filter(organizations -> Boolean.TRUE.equals(organizations.getIsCloudVendor())).collect(Collectors.toSet());
		// Map<Systems, Set<Organizations>> associateVendorsMap = new HashMap<>();
		Map<Users, Set<Systems>> userToSystemsSetMap = userAssignmentsMap;
		for (Systems system : systemList) {
			if (system.getOwner() != null) {
				if (!userToSystemsSetMap.containsKey(system.getOwner())) {
					userToSystemsSetMap.put(system.getOwner(), new HashSet<>());
				}
				userToSystemsSetMap.get(system.getOwner()).add(system);
			}
		}

		Map<Users, Set<Organizations>> userToVendorsSetMap = new HashMap<>();
		for (Organizations vendor : vendorList) {
			if (vendor.getOwner() != null) {
				if (!userToVendorsSetMap.containsKey(vendor.getOwner())) {
					userToVendorsSetMap.put(vendor.getOwner(), new HashSet<>());
				}
				userToVendorsSetMap.get(vendor.getOwner()).add(vendor);
			}
		}

		// Build Dashboard Data
		List<VendorType> systemTypes = Arrays.asList(VendorType.Both, VendorType.System);
		List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor);
		List<VendorType> cloudTypes = Arrays.asList(VendorType.Both, VendorType.Cloud);
		List<QualitativeQuestions> impactQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndMetricDomainAndType(riskModelId, MetricDomain.IMPACT.getId(), systemTypes);
		List<QualitativeQuestions> likelihoodQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndMetricDomainAndType(riskModelId, MetricDomain.LIKELIHOOD.getId(), systemTypes);
		List<QualitativeQuestions> impactVendorQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndMetricDomainAndType(riskModelId, MetricDomain.IMPACT.getId(), vendorTypes);
		List<QualitativeQuestions> likelihoodVendorQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndMetricDomainAndType(riskModelId, MetricDomain.LIKELIHOOD.getId(), vendorTypes);
		List<QualitativeQuestions> impactCloudQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndMetricDomainAndType(riskModelId, MetricDomain.IMPACT.getId(), cloudTypes);
		List<QualitativeQuestions> likelihoodCloudQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndMetricDomainAndType(riskModelId, MetricDomain.LIKELIHOOD.getId(), cloudTypes);

		long totalImpactQuestions = QuestionStatusDashboardService.calculateQuestionsCount(impactQuestions);
		long totalLikelihoodQuestions = QuestionStatusDashboardService.calculateQuestionsCount(likelihoodQuestions);
		long totalImpactVendorQuestions = QuestionStatusDashboardService.calculateQuestionsCount(impactVendorQuestions);
		long totalLikelihoodVendorQuestions = QuestionStatusDashboardService.calculateQuestionsCount(likelihoodVendorQuestions);
		long totalImpactCloudQuestions = QuestionStatusDashboardService.calculateQuestionsCount(impactCloudQuestions);
		long totalLikelihoodCloudQuestions =QuestionStatusDashboardService.calculateQuestionsCount( likelihoodCloudQuestions);

		List<QuestionAnswersForSystem> impactQuestionsAnswersList = questionAnswersForSystemRepository.getListByRiskModelAndMetricDomainId(riskModelId, MetricDomain.IMPACT.getId()).stream().filter(item -> item.getAnswer() != null).collect(Collectors.toList());
		List<QuestionAnswersForSystem> likelihoodQuestionsAnswersList = questionAnswersForSystemRepository.getListByRiskModelAndMetricDomainId(riskModelId, MetricDomain.LIKELIHOOD.getId()).stream().filter(item -> item.getAnswer() != null).collect(Collectors.toList());;
		List<QuestionAnswersForVendor> impactVendorQuestionsAnswersList = questionAnswersForVendorRepository.getListByRiskModelAndMetricDomainId(riskModelId, MetricDomain.IMPACT.getId(), Arrays.asList(VendorType.Vendor, VendorType.Both)).stream().filter(item -> item.getAnswer() != null).collect(Collectors.toList());;
		List<QuestionAnswersForVendor> likelihoodVendorQuestionsAnswersList = questionAnswersForVendorRepository.getListByRiskModelAndMetricDomainId(riskModelId, MetricDomain.LIKELIHOOD.getId(), Arrays.asList(VendorType.Vendor, VendorType.Both)).stream().filter(item -> item.getAnswer() != null).collect(Collectors.toList());;
		List<QuestionAnswersForVendor> impactCloudQuestionsAnswersList = questionAnswersForVendorRepository.getListByRiskModelAndMetricDomainId(riskModelId, MetricDomain.IMPACT.getId(), Arrays.asList(VendorType.Cloud, VendorType.Both)).stream().filter(item -> item.getAnswer() != null).collect(Collectors.toList());;
		List<QuestionAnswersForVendor> likelihoodCloudQuestionsAnswersList = questionAnswersForVendorRepository.getListByRiskModelAndMetricDomainId(riskModelId, MetricDomain.LIKELIHOOD.getId(), Arrays.asList(VendorType.Cloud, VendorType.Both)).stream().filter(item -> item.getAnswer() != null).collect(Collectors.toList());;

		Map<Systems, List<QuestionAnswersForSystem>> impactQuestionsAnswersMap = impactQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForSystem::getSystem));
		Map<Systems, List<QuestionAnswersForSystem>> likelihoodQuestionsAnswersMap = likelihoodQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForSystem::getSystem));
		Map<Organizations, List<QuestionAnswersForVendor>> impactVendorQuestionsAnswersMap = impactVendorQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForVendor::getVendor));
		Map<Organizations, List<QuestionAnswersForVendor>> likelihoodVendorQuestionsAnswersMap = likelihoodVendorQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForVendor::getVendor));
		Map<Organizations, List<QuestionAnswersForVendor>> impactCloudQuestionsAnswersMap = impactCloudQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForVendor::getVendor));
		Map<Organizations, List<QuestionAnswersForVendor>> likelihoodCloudQuestionsAnswersMap = likelihoodCloudQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForVendor::getVendor));

		DataTypeClassification piiDataType = dataTypeClassificationRepository.findById(DataTypeDomain.PII.getId()).orElse(null);

		dashboardItem1.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$SYSTEM_ASSIGNMENTS$SYSTEM_NAME_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$SYSTEM_ASSIGNMENTS$ASSIGNMENTS_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$SYSTEM_ASSIGNMENTS$TYPE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$SYSTEM_ASSIGNMENTS$BUSINESS_UNIT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$SYSTEM_ASSIGNMENTS$SYSTEM_QUESTIONNAIRE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$SYSTEM_ASSIGNMENTS$DATA_EXFILTRATION_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$SYSTEM_ASSIGNMENTS$BUSINESS_INTERRUPTION_HEADER)
		));

		dashboardItem1.getGridItems().get(0).get(0).setRowSpan(2l);
		dashboardItem1.getGridItems().get(0).get(1).setRowSpan(2l);
		dashboardItem1.getGridItems().get(0).get(2).setRowSpan(2l);
		dashboardItem1.getGridItems().get(0).get(3).setRowSpan(2l);
		dashboardItem1.getGridItems().get(0).get(4).setColSpan(2l);
		dashboardItem1.getGridItems().get(0).get(5).setRowSpan(2l);
		dashboardItem1.getGridItems().get(0).get(6).setRowSpan(2l);

		dashboardItem1.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$SYSTEM_ASSIGNMENTS$IMPACT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$SYSTEM_ASSIGNMENTS$SYSTEM_ASSIGNMENTS$LIKELIHOOD_HEADER)
		));

		int i = 0;
		for (Systems system : systemsSet) {

			Users owner = system.getOwner();
			Set<Users> assignmets = systemAssignmentsMap.containsKey(system) ? systemAssignmentsMap.get(system) : new HashSet<>();
			assignmets.add(owner);

			int ownersNumber = assignmets.size();
			int j = 0;
			for (Users assignment : assignmets) {
				List<DashboardDataItemDTO>  itemsList = new ArrayList<>();

				Long rowSpan = (ownersNumber > 1) ? Long.valueOf(ownersNumber) : null;
				if (j == 0) {
					DashboardDataItemDTO systemCell = sI(system.getName());
					systemCell.setRowSpan(rowSpan);
					itemsList.add(systemCell);
				}

				String assignmentName = assignment != null ? assignment.getFullName() : "";
				DashboardDataItemDTO ownerCell = sI(assignmentName);
				if (assignment != null) {
					ownerCell.applyDrilldown(DashboardDataItemDrilldownDTO.of(assignment, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null));
				}
				itemsList.add(ownerCell);

				if (assignment != null && assignment.equals(system.getOwner())) {
					itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$OWNER)));
				} else if (assignment != null) {
					itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$ASSIGNMENT)));
				} else {
					itemsList.add(sI(""));
				}

				if (assignment != null && assignment.getBusinessUnit() != null) {
					itemsList.add(sI(businessUnitService.getBusinessUnitPath(assignment.getBusinessUnit(), true))
						.applyDrilldown(DashboardDataItemDrilldownDTO.of(assignment, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null)));
				} else {
					itemsList.add(sI(""));
				}

				if (j == 0) {


					if (Boolean.TRUE.equals(system.getIsEtl())) {
						itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE)).applyTextAlign("center").applyRowspan(rowSpan));
						itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE)).applyTextAlign("center").applyRowspan(rowSpan));
					} else {
						int impactAnswersCount = impactQuestionsAnswersMap.containsKey(system) ? impactQuestionsAnswersMap.get(system).size() : 0;
						int likelihoodAnswersCount = likelihoodQuestionsAnswersMap.containsKey(system) ? likelihoodQuestionsAnswersMap.get(system).size() : 0;
						// System Questionnaire Status
						if (impactAnswersCount == 0) {
							itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED)).applyColor("#ff0000").applyRowspan(rowSpan)
								.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "impact"))));
						} else if (impactAnswersCount >= totalImpactQuestions) {
							itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE)).applyColor("#00ff00").applyRowspan(rowSpan));
						} else {
							itemsList.add(sI(MessageFormat.format("{0}/{1}", impactAnswersCount, totalImpactQuestions)).applyTextAlign("right").applyRowspan(rowSpan)
								.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "impact"))));
						}
						if (likelihoodAnswersCount == 0) {
							itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED)).applyColor("#ff0000").applyRowspan(rowSpan)
								// .applyLink(DashboardLinkDTO.of(MessageFormat.format("/private/cyber-risk-scoring/questions/{0,number,#}?metric=likelihood", system.getId()))));
								.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "likelihood"))));
						} else if (likelihoodAnswersCount >= totalLikelihoodQuestions) {
							itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE)).applyColor("#00ff00").applyRowspan(rowSpan));
						} else {
							itemsList.add(sI(MessageFormat.format("{0}/{1}", likelihoodAnswersCount, totalLikelihoodQuestions)).applyTextAlign("right").applyRowspan(rowSpan)
								.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "likelihood"))));
						}
					}

					boolean isPIISystem = system.getDataTypeClassifications().contains(piiDataType);

					// Data Exfiltration Question
					if (isPIISystem) {
						if (system.getNumberOfRecProcessed() != null) {
							itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE)).applyColor("#00ff00").applyRowspan(rowSpan));
						} else {
							itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_COMPLETE)).applyColor("#ff0000").applyRowspan(rowSpan));
						}
					} else {
						itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE)).applyRowspan(rowSpan));
					}

					// Business Interruption Question
					itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE)).applyRowspan(rowSpan));
				}

				dashboardItem1.getGridItems().add(itemsList);
				j++;

			}
		}


		dashboardItem2.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$PEOPLE_ASSIGNMENTS$PERSON_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$PEOPLE_ASSIGNMENTS$BUSINESS_UNIT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$PEOPLE_ASSIGNMENTS$SYSTEM_NAME_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$PEOPLE_ASSIGNMENTS$ASSIGNMENT_TYPE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$PEOPLE_ASSIGNMENTS$SYSTEM_QUESTIONNAIRE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$PEOPLE_ASSIGNMENTS$DATA_EXFILTRATION_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$PEOPLE_ASSIGNMENTS$BUSINESS_INTERRUPTION_HEADER)
		));
		dashboardItem2.getGridItems().get(0).get(0).setRowSpan(2l);
		dashboardItem2.getGridItems().get(0).get(1).setRowSpan(2l);
		dashboardItem2.getGridItems().get(0).get(2).setRowSpan(2l);
		dashboardItem2.getGridItems().get(0).get(3).setRowSpan(2l);
		dashboardItem2.getGridItems().get(0).get(4).setColSpan(2l);
		dashboardItem2.getGridItems().get(0).get(5).setRowSpan(2l);
		dashboardItem2.getGridItems().get(0).get(6).setRowSpan(2l);
		dashboardItem2.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$PEOPLE_ASSIGNMENTS$IMPACT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$ASSIGNMENTS_STATUS$PEOPLE_ASSIGNMENTS$PEOPLE_ASSIGNMENTS$LIKELIHOOD_HEADER)
		));

		i = 0;
		for (Users assignment : userAssignmentsMap.keySet()) {

			Set<Systems> assignedSystemsSet = userAssignmentsMap.containsKey(assignment) ? userAssignmentsMap.get(assignment) : new HashSet<>();

			int systemsNumber = assignedSystemsSet.size();
			int j = 0;
			for (Systems system : assignedSystemsSet) {
				List<DashboardDataItemDTO>  itemsList = new ArrayList<>();

				Long rowSpan = (systemsNumber > 1) ? Long.valueOf(systemsNumber) : null;
				if (j == 0) {
					itemsList.add(sI(assignment.getFullName()).applyRowspan(rowSpan));

					if (assignment != null && assignment.getBusinessUnit() != null) {
						itemsList.add(sI(businessUnitService.getBusinessUnitPath(assignment.getBusinessUnit(), true)).applyRowspan(rowSpan)
							.applyDrilldown(DashboardDataItemDrilldownDTO.of(assignment, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null)));
					} else {
						itemsList.add(sI("").applyRowspan(rowSpan));
					}
				}

				String systemName = system != null ? system.getName() : "";
				DashboardDataItemDTO systemCell = sI(systemName);
				if (system != null) {
					systemCell.applyDrilldown(DashboardDataItemDrilldownDTO.of(system));
				}
				itemsList.add(systemCell);

				if (assignment != null && assignment.equals(system.getOwner())) {
					itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$OWNER)));
				} else if (assignment != null) {
					itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$ASSIGNMENT)));
				} else {
					itemsList.add(sI(""));
				}

				if (Boolean.TRUE.equals(system.getIsEtl())) {
					itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE)).applyTextAlign("center"));
					itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE)).applyTextAlign("center"));
				} else {
					int impactAnswersCount = impactQuestionsAnswersMap.containsKey(system) ? impactQuestionsAnswersMap.get(system).size() : 0;
					int likelihoodAnswersCount = likelihoodQuestionsAnswersMap.containsKey(system) ? likelihoodQuestionsAnswersMap.get(system).size() : 0;
					// System Questionnaire Status
					if (impactAnswersCount == 0) {
						itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED)).applyColor("#ff0000")
							.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "impact"))));
					} else if (impactAnswersCount >= totalImpactQuestions) {
						itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE)).applyColor("#00ff00"));
					} else {
						itemsList.add(sI(MessageFormat.format("{0}/{1}", impactAnswersCount, totalImpactQuestions)).applyTextAlign("right")
							.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "impact"))));
					}
					if (likelihoodAnswersCount == 0) {
						itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED)).applyColor("#ff0000")
							.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "likelihood"))));
					} else if (likelihoodAnswersCount >= totalLikelihoodQuestions) {
						itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE)).applyColor("#00ff00"));
					} else {
						itemsList.add(sI(MessageFormat.format("{0}/{1}", likelihoodAnswersCount, totalLikelihoodQuestions)).applyTextAlign("right")
							.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "likelihood"))));
					}
				}

				boolean isPIISystem = system.getDataTypeClassifications().contains(piiDataType);

				// Data Exfiltration Question
				if (isPIISystem) {
					if (system.getNumberOfRecProcessed() != null) {
						itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE)).applyColor("#00ff00"));
					} else {
						itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_COMPLETE)).applyColor("#ff0000"));
					}
				} else {
					itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE)));
				}

				// Business Interruption Question
				itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_AVAILABLE)));

				dashboardItem2.getGridItems().add(itemsList);

				j++;
			}
		}

		// ========== ========== ========== ========== ========== System Assignments END ========== ========== ========== ========== ========== //

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getMyAssignmentsDashboardDetails(Long riskModelId) {

		// TODO Remove this
		// exposureMetricsDashboardService.getSystemsScoringData(riskModelId, null);

		if (!userService.hasRole(RoleType.VENDOR_OWNER) && !userService.hasRole(RoleType.SYSTEM_OWNER)) {
			// throw new ForbiddenException("You are not allowed to load this dashboard!");
		}

		DashboardDTO dashboard = new DashboardDTO(41L, clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$DESCRIPTION), DashboardType.None);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.SET_UP_DASHBOARD(clientMessage).add("SET_UP_ASSIGNMENT_STATUS", SLCT.DASHBOARDS$MY_ASSIGNMENTS$NAME, "/private/dashboards/41");



		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		Users currentUser = userService.getCurrentUserEntity();
		List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor);
		Double itemMetric;

		// Get Assigned Systems Data
		Set<Systems> mySystems = new HashSet<>();
		Set<Systems> systemsForOwner = systemRepository.getAllBySystemOwnerAndOrganization(currentUser.getId(), organization.getId()).stream().collect(Collectors.toSet());
		Set<Systems> systemsForAssignments = userAssignedSystemRepository.getSystemsForUser(currentUser.getId(), organization.getId()).stream().collect(Collectors.toSet());
		mySystems.addAll(systemsForOwner);
		mySystems.addAll(systemsForAssignments);

		if (mySystems.size() > 0) {
			Map<Systems, SystemDataSeries> impactDataSystemMap = getQualMetricDataForSystems(riskModelId, MetricDomain.IMPACT, mySystems).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
			Map<Systems, SystemDataSeries> likelihoodDataSystemMap = getQualMetricDataForSystems(riskModelId, MetricDomain.LIKELIHOOD, mySystems).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
			DashboardSectionDTO section1 = new DashboardSectionDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$SYSTEMS$ITEM_NAME), null);
			dashboard.getSections().add(section1);
			DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(2031l, clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$SYSTEMS$SYSTEM_OWNER$ITEM_NAME));
			section1.getDashboardItems().add(dashboardItem);

			// Create breadcrumbs
			section1.setBreadcrumbs(breadcrumbsTop.extend("VENDORS", SLCT.DASHBOARDS$MY_ASSIGNMENTS$SYSTEMS$ITEM_NAME, "").getBreadcrumbs());

			dashboardItem.addGridHeaders(Arrays.asList(
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$SYSTEMS$SYSTEM_OWNER$SYSTEM_HEADER),
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$SYSTEMS$SYSTEM_OWNER$OWNERSHIP_HEADER),
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$SYSTEMS$SYSTEM_OWNER$IMPACT_HEADER),
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$SYSTEMS$SYSTEM_OWNER$LIKELIHOOD_HEADER)
			));
			for (Systems system : mySystems) {
				List<DashboardDataItemDTO>  itemsList = new ArrayList<>();
				itemsList.add(sI(system.getName()).applyLink(DashboardLinkDTO.of(MessageFormat.format("/private/systems/edit/{0,number,#}", system.getId()))));
				String ownershipType = clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$SYSTEMS$SYSTEM_OWNER$ASSIGNMENT_OWNERSHIP);
				if (systemsForOwner.contains(system)) {
					ownershipType = clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$SYSTEMS$SYSTEM_OWNER$OWNER_OWNERSHIP);
				}
				itemsList.add(sI(ownershipType));

				itemMetric = 0D;
				if (impactDataSystemMap.containsKey(system) && impactDataSystemMap.get(system).getItems().get(2) > 0) {
					itemMetric = impactDataSystemMap.get(system).getItems().get(1) / impactDataSystemMap.get(system).getItems().get(2);
				}
				itemsList.add(sI(itemMetric).round(2).applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "impact"))));

				itemMetric = 0D;
				if (likelihoodDataSystemMap.containsKey(system) && likelihoodDataSystemMap.get(system).getItems().get(2) > 0) {
					itemMetric = likelihoodDataSystemMap.get(system).getItems().get(1) / likelihoodDataSystemMap.get(system).getItems().get(2);
				}
				itemsList.add(sI(itemMetric).round(2).applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "likelihood"))));

				dashboardItem.getGridItems().add(itemsList);
			}
		}

		// Vendors Data
		Set<Organizations> myVendors = new HashSet<>();
		Set<Organizations> vendorsForOwner = organizationRepository.getAllByOwnerAndOrganization(currentUser.getId(), organization.getId(), OrganizationType.Vendor).stream().collect(Collectors.toSet());
		Set<Organizations> vendorsForAssignments = userAssignedVendorRepository.getVendorsForUser(currentUser.getId(), organization.getId()).stream().collect(Collectors.toSet());
		myVendors.addAll(vendorsForOwner);
		myVendors.addAll(vendorsForAssignments);
		List<Organizations> myVendorsList = myVendors.stream().collect(Collectors.toList());
		List<Organizations> myCloudVendorsList = myVendors.stream().filter(vendor -> Boolean.TRUE.equals(vendor.getIsCloudVendor())).collect(Collectors.toList());

		if (myVendorsList.size() > 0) {
			Map<Organizations, VendorDataSeries> impactDataVendorMap = getQualMetricData(riskModelId, MetricDomain.IMPACT, myVendorsList, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));
			Map<Organizations, VendorDataSeries> likelihoodDataVendorMap = getQualMetricData(riskModelId, MetricDomain.LIKELIHOOD, myVendorsList, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));

			DashboardSectionDTO section2 = new DashboardSectionDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$VENDORS$ITEM_NAME), null);
			dashboard.getSections().add(section2);
			DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(2031l, clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$VENDORS$VENDOR_OWNER$ITEM_NAME));
			section2.getDashboardItems().add(dashboardItem);

			// Create breadcrumbs
			section2.setBreadcrumbs(breadcrumbsTop.extend("VENDORS", SLCT.DASHBOARDS$MY_ASSIGNMENTS$VENDORS$ITEM_NAME, "").getBreadcrumbs());

			dashboardItem.addGridHeaders(Arrays.asList(
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$VENDORS$VENDOR_OWNER$VENDOR_HEADER),
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$VENDORS$VENDOR_OWNER$OWNERSHIP_HEADER),
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$VENDORS$VENDOR_OWNER$IMPACT_HEADER),
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$VENDORS$VENDOR_OWNER$LIKELIHOOD_HEADER)
			));
			for (Organizations vendor : myVendors) {
				List<DashboardDataItemDTO>  itemsList = new ArrayList<>();
				itemsList.add(sI(vendor.getName()).applyLink(DashboardLinkDTO.of(MessageFormat.format("/private/vendors/edit/{0,number,#}", vendor.getId()))));
				String ownershipType = clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$VENDORS$VENDOR_OWNER$ASSIGNMENT_OWNERSHIP);
				if (vendorsForOwner.contains(vendor)) {
					ownershipType = clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$VENDORS$VENDOR_OWNER$OWNER_OWNERSHIP);
				}
				itemsList.add(sI(ownershipType));

				itemMetric = 0D;
				if (impactDataVendorMap.containsKey(vendor) && impactDataVendorMap.get(vendor).getItems().get(2) > 0) {
					itemMetric = impactDataVendorMap.get(vendor).getItems().get(1) / impactDataVendorMap.get(vendor).getItems().get(2);
				}
				itemsList.add(sI(itemMetric).round(2).applyLink(DashboardLinkDTO.of(DashboardsConfig.buildVendorRiskQualQuestionsUrl(vendor.getId(), "impact"))));

				itemMetric = 0D;
				if (likelihoodDataVendorMap.containsKey(vendor) && likelihoodDataVendorMap.get(vendor).getItems().get(2) > 0) {
					itemMetric = likelihoodDataVendorMap.get(vendor).getItems().get(1) / likelihoodDataVendorMap.get(vendor).getItems().get(2);
				}
				itemsList.add(sI(itemMetric).round(2).applyLink(DashboardLinkDTO.of(DashboardsConfig.buildVendorRiskQualQuestionsUrl(vendor.getId(), "likelihood"))));

				dashboardItem.getGridItems().add(itemsList);
			}
		}

		if (myCloudVendorsList.size() > 0) {
			Map<Organizations, VendorDataSeries> impactDataCloudMap = getQualMetricData(riskModelId, MetricDomain.IMPACT, myCloudVendorsList, Arrays.asList(VendorType.Cloud)).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));
			Map<Organizations, VendorDataSeries> likelihoodDataCloudMap = getQualMetricData(riskModelId, MetricDomain.LIKELIHOOD, myCloudVendorsList, Arrays.asList(VendorType.Cloud)).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));

			DashboardSectionDTO section3 = new DashboardSectionDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$CLOUD$ITEM_NAME), null);
			dashboard.getSections().add(section3);
			DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(2031l, clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$CLOUD$CLOUD_OWNER$ITEM_NAME));
			section3.getDashboardItems().add(dashboardItem);

			// Create breadcrumbs
			section3.setBreadcrumbs(breadcrumbsTop.extend("CLOUD", SLCT.DASHBOARDS$MY_ASSIGNMENTS$CLOUD$ITEM_NAME, "").getBreadcrumbs());

			dashboardItem.addGridHeaders(Arrays.asList(
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$CLOUD$CLOUD_OWNER$VENDOR_HEADER),
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$CLOUD$CLOUD_OWNER$OWNERSHIP_HEADER),
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$CLOUD$CLOUD_OWNER$IMPACT_HEADER),
				clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$CLOUD$CLOUD_OWNER$LIKELIHOOD_HEADER)
			));
			for (Organizations vendor : myCloudVendorsList) {
				List<DashboardDataItemDTO>  itemsList = new ArrayList<>();
				itemsList.add(sI(vendor.getName()).applyLink(DashboardLinkDTO.of(MessageFormat.format("/private/vendors/edit/{0,number,#}", vendor.getId()))));
				String ownershipType = clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$CLOUD$CLOUD_OWNER$ASSIGNMENT_OWNERSHIP);
				if (vendorsForOwner.contains(vendor)) {
					ownershipType = clientMessage.getMessage(SLCT.DASHBOARDS$MY_ASSIGNMENTS$CLOUD$CLOUD_OWNER$OWNER_OWNERSHIP);
				}
				itemsList.add(sI(ownershipType));


				itemMetric = 0D;
				if (impactDataCloudMap.containsKey(vendor) && impactDataCloudMap.get(vendor).getItems().get(2) > 0) {
					itemMetric = impactDataCloudMap.get(vendor).getItems().get(1) / impactDataCloudMap.get(vendor).getItems().get(2);
				}
				itemsList.add(sI(itemMetric).round(2).applyLink(DashboardLinkDTO.of(DashboardsConfig.buildCloudRiskQualQuestionsUrl(vendor.getId(), "impact"))));

				itemMetric = 0D;
				if (likelihoodDataCloudMap.containsKey(vendor) && likelihoodDataCloudMap.get(vendor).getItems().get(2) > 0) {
					itemMetric = likelihoodDataCloudMap.get(vendor).getItems().get(1) / likelihoodDataCloudMap.get(vendor).getItems().get(2);
				}
				itemsList.add(sI(itemMetric).round(2).applyLink(DashboardLinkDTO.of(DashboardsConfig.buildCloudRiskQualQuestionsUrl(vendor.getId(), "likelihood"))));

				dashboardItem.getGridItems().add(itemsList);
			}

		}
	/*	DashboardSectionDTO section4 = new DashboardSectionDTO();
		dashboard.getSections().add(section4);

		// Create breadcrumbs
		section4.setBreadcrumbs(breadcrumbsTop.extend("MY_ASSIGNMENTS", SLCT.DASHBOARDS$MY_ASSIGNMENTS$NAME, "").getBreadcrumbs());*/

		return dashboard;
	}

	/**
	 * Get Privacy Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getPrivacyDashboardDetails(Long riskModelId) {
		DashboardDTO dashboard = new DashboardDTO(4L, clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$DESCRIPTION), DashboardType.None);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.VENDOR_CYBER_RISK_MANAGER(clientMessage)
			.add("VENDOR_PRIVACY_RISK", SLCT.DASHBOARDS$PRIVACY$NAME, "/private/dashboards/4");

		// Get Risk Model and Organization Details
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		// Build Data for Privacy Chart
		// List<Organizations> privacyVendors = organizationRepository.getVendorsListWithDataTypes(organization.getId(), Arrays.asList(DataTypeDomain.PRIVACY.getId()), OrganizationType.Vendor);
		List<Organizations> privacyVendors = organizationRepository.getVendorsListWithDataTypes(organization.getId(), Arrays.asList(DataTypeDomain.PII.getId(), DataTypeDomain.PRIVACY.getId()), OrganizationType.Vendor);
		List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor);
		List<Systems> privacySystems = systemRepository.getSystemsListWithDataTypes(organization.getId(), Arrays.asList(DataTypeDomain.PRIVACY.getId()));
		Set<Systems> privacySystemsSet = privacySystems.stream().collect(Collectors.toSet());
		List<VendorDataSeries> impactData = getQualMetricData(riskModelId, MetricDomain.IMPACT, privacyVendors, vendorTypes);
		List<VendorDataSeries> likelihoodData = getQualMetricData(riskModelId, MetricDomain.LIKELIHOOD, privacyVendors, vendorTypes);
		Map<Long, VendorDataSeries> summaryQualData = getLongVendorDataSeriesMap(impactData, likelihoodData);

		List<VendorDataSeries> quantVendorScores = buildQuantMetricData(riskModelId, privacyVendors, privacySystemsSet);

		List<SystemDataSeries> impactDataSystems = getQualMetricDataForSystems(riskModelId, MetricDomain.IMPACT, privacySystemsSet);
		List<SystemDataSeries> likelihoodDataSystems = getQualMetricDataForSystems(riskModelId, MetricDomain.LIKELIHOOD, privacySystemsSet);
		Map<Long, SystemDataSeries> summaryQualDataSystems = organizationDashboardService.getLongSystemDataSeriesMap(impactDataSystems, likelihoodDataSystems);

		// Create Vendor Heat Map section
		DashboardSectionDTO heatMapSection = new DashboardSectionDTO(40001L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MAP$HEAT_CHART$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MAP$HEAT_CHART$ITEM_DESCRIPTION));
		dashboard.getSections().add(heatMapSection);

		// Create breadcrumbs
		heatMapSection.setBreadcrumbs(breadcrumbsTop.extend("VENDOR_PRIVACY_RISK$HEAT_MAP", SLCT.DASHBOARDS$VENDOR$HEAT_MAP$HEAT_CHART$ITEM_NAME, "").getBreadcrumbs());

		// Init Heat Matrix and Heat Chart data
		Map<Long, DashboardDataItemDTO> heatChartItemsMap = new HashMap<>();
		List<DashboardDataItemDTO> heatChartItems = new ArrayList<>();
		List<List<DashboardDataItemDTO>> itemsMatrix = new ArrayList<>();
		buildHeatChartItems(summaryQualData, heatChartItemsMap, heatChartItems, itemsMatrix);

		// Create Heat Chart
		DashboardChartItemDTO dashboardItem1 = createHeatChartDashboardItem(heatChartItems);
		heatMapSection.getDashboardItems().add(dashboardItem1);

		// Total Quantification Scores
		DashboardSectionDTO quantScoresSection = new DashboardSectionDTO(40002L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$ITEM_DESCRIPTION));
		dashboard.getSections().add(quantScoresSection);
		quantScoresSection.setBreadcrumbs(breadcrumbsTop.extend("VENDOR_PRIVACY_RISK$VENDOR_EXPOSURES", SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$ITEM_NAME, "").getBreadcrumbs());

		DashboardItemDTO dashboardItem2 = vendorsDashboardService.createVendorQuantScoresDashboard(riskModel, Arrays.asList(DataTypeDomain.PII.getId(), DataTypeDomain.PRIVACY.getId()), heatChartItemsMap);
		quantScoresSection.getDashboardItems().add(dashboardItem2);

		// Initialize Organization Summary Scores
		DashboardSectionDTO organizationSummarySection = new DashboardSectionDTO(40003L, clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$ITEM_DESCRIPTION));
		dashboard.getSections().add(organizationSummarySection);
		organizationSummarySection.setBreadcrumbs(breadcrumbsTop.extend("VENDOR_PRIVACY_RISK$ORGANIZATION_SUMMARY_SCORES", SLCT.DASHBOARDS$PRIVACY$ORGANIZATION_SUMMARY_SCORES$ITEM_NAME, "").getBreadcrumbs());

		DashboardItemDTO dashboardItem3 = organizationDashboardService.createOrganizationSummaryScoresDashboardItem(summaryQualDataSystems);
		organizationSummarySection.getDashboardItems().add(dashboardItem3);

		return dashboard;
	}

	/**
	 * Get Cyber Risk Scoring Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getMATargetRiskDashboardDetails(Long riskModelId, Long dashboardId) {
		DashboardDTO dashboard = new DashboardDTO(dashboardId, clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$DESCRIPTION), DashboardType.None);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop;
		if (DashboardsConfig.DASHBOARD_M_AND_A.equals(dashboardId)) {
			breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_EXECUTIVE(clientMessage);
		} else {
			breadcrumbsTop = DashboardBreadcrumbsHelper.CFO_DASHBOARD(clientMessage);
		}

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		// Create breadcrumbs
		section1.setBreadcrumbs(breadcrumbsTop.extend("M_AND_A_DASHBOARD", SLCT.DASHBOARDS$M_AND_A$NAME, "").getBreadcrumbs());

		// Get Risk Model and Organization Details
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		// Build Data for Privacy Chart
		List<Systems> systemsList = systemRepository.getSystemsListByMA(true, organization.getId());

		// Initialize Organization Quant Scores
		Map<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoringData = exposureMetricsDashboardService.getSystemsScoringData(riskModelId, systemsList, Arrays.asList(QuantsDomain.BUSINESS_INTERRUPTION, QuantsDomain.DATA_EXFILTRATION, QuantsDomain.GDPR_REGULATORY_EXPOSURE));

		boolean isGDPRRegulatoryQuantDefined = quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE);

		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(12l, "");
		List<String> headers = new ArrayList<>();
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$ORGANIZATION_QUANT_SCORES$SYSTEM_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$ORGANIZATION_QUANT_SCORES$DATA_EXFILTRATION_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$ORGANIZATION_QUANT_SCORES$BUSINESS_INTERRUPTION_HEADER));
		if (isGDPRRegulatoryQuantDefined) headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$ORGANIZATION_QUANT_SCORES$GDPR_REGULATORY_EXPOSURE_HEADER));
		headers.add(clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$ORGANIZATION_QUANT_SCORES$TOTAL_EXPOSURE));

		dashboardItem.addGridHeaders(headers, true);
		for (Map.Entry<Systems, Map<QuantMetrics, ExposureMetricResult>> systemScoring : systemScoringData.entrySet()) {
			Systems system = systemScoring.getKey();
			Map<QuantMetrics, ExposureMetricResult> systemScoringMap = systemScoring.getValue();
			// QuantsDomain.BUSINESS_INTERRUPTION, QuantsDomain.DATA_EXFILTRATION, QuantsDomain.GDPR_REGULATORY_EXPOSURE
			Map<QuantsDomain, Double> scoringResultsMap = new HashMap<>();
			scoringResultsMap.put(QuantsDomain.BUSINESS_INTERRUPTION, 0D);
			scoringResultsMap.put(QuantsDomain.DATA_EXFILTRATION, 0D);
			scoringResultsMap.put(QuantsDomain.GDPR_REGULATORY_EXPOSURE, 0D);
			Double total = 0D;
			for (Map.Entry<QuantMetrics, ExposureMetricResult> scoringEntry : systemScoringMap.entrySet()) {
				QuantMetrics quantMetric = scoringEntry.getKey();
				ExposureMetricResult metricResult = scoringEntry.getValue();
				QuantsDomain quantDomain = QuantsDomain.of(quantMetric.getQuant().getId());
				if (scoringResultsMap.containsKey(quantDomain)) {
					scoringResultsMap.put(quantDomain, scoringResultsMap.get(quantDomain) + metricResult.getResult());
					total += metricResult.getResult();
				}
			}

			List<DashboardDataItemDTO> rowItems = new ArrayList<>();
			rowItems.add(sI(system.getName()));
			rowItems.add($I(scoringResultsMap.get(QuantsDomain.DATA_EXFILTRATION), "$").round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.DATA_EXFILTRATION)));
			rowItems.add($I(scoringResultsMap.get(QuantsDomain.BUSINESS_INTERRUPTION), "$").round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.BUSINESS_INTERRUPTION)));
			if (isGDPRRegulatoryQuantDefined)  rowItems.add($I(scoringResultsMap.get(QuantsDomain.GDPR_REGULATORY_EXPOSURE), "$").round(0).applyDrilldown(DashboardDataItemDrilldownDTO.ofQuant(system, QuantsDomain.GDPR_REGULATORY_EXPOSURE)));
			rowItems.add($I(total, "$").round(0));
			dashboardItem.getGridItems().add(rowItems);
		}

		section1.getDashboardItems().add(dashboardItem);

		dashboardItem.setName(clientMessage.getMessage(SLCT.DASHBOARDS$M_AND_A$M_AND_A_TARGET_RISK_QUANTIFICATION));

		return dashboard;
	}

	/**
	 * Get Cloud Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getCloudScoringDashboardDetails(Long riskModelId) {
		DashboardDTO dashboard = new DashboardDTO(5L, clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$DESCRIPTION), DashboardType.Vendor);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_CISO(clientMessage)
			.extend("CLOUD_CYBER_RISK_SCORES", SLCT.DASHBOARDS$CLOUD$NAME, "");

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(1L, clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$ITEM_NAME), "");
		DashboardSectionDTO section2 = new DashboardSectionDTO(2L, clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_TABLE$ITEM_NAME), "");
		DashboardSectionDTO section3 = new DashboardSectionDTO(3L, clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$HEAT_MAP_CHART$ITEM_NAME), "");
		DashboardSectionDTO section4 = new DashboardSectionDTO(4L, clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$HEAT_MAP_TABLE$ITEM_NAME), "");
		DashboardSectionDTO section5 = new DashboardSectionDTO(5L, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$ITEM_NAME), "");
		dashboard.getSections().add(section1);
		dashboard.getSections().add(section2);
		dashboard.getSections().add(section3);
		dashboard.getSections().add(section4);
		dashboard.getSections().add(section5);

		// Create breadcrumbs
		section1.setBreadcrumbs(breadcrumbsTop.extend("CLOUD_CYBER_RISK_SCORES", SLCT.DASHBOARDS$CLOUD$SUMMARY$ITEM_NAME, "").getBreadcrumbs());
		section2.setBreadcrumbs(breadcrumbsTop.extend("CLOUD_CYBER_RISK_SCORES_TAB2", SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_TABLE$ITEM_NAME, "").getBreadcrumbs());
		section3.setBreadcrumbs(breadcrumbsTop.extend("CLOUD_CYBER_RISK_SCORES_TAB3", SLCT.DASHBOARDS$CLOUD$SUMMARY$HEAT_MAP_CHART$ITEM_NAME, "").getBreadcrumbs());
		section4.setBreadcrumbs(breadcrumbsTop.extend("CLOUD_CYBER_RISK_SCORES_TAB4", SLCT.DASHBOARDS$CLOUD$SUMMARY$HEAT_MAP_TABLE$ITEM_NAME, "").getBreadcrumbs());
		section5.setBreadcrumbs(breadcrumbsTop.extend("CLOUD_CYBER_RISK_SCORES_TAB5", SLCT.DASHBOARDS$VENDOR$CYBER_EXPOSURES$VENDOR_EXPOSURES$ITEM_NAME, "").getBreadcrumbs());

		List<VendorType> vendorTypes = Arrays.asList(VendorType.Cloud);
		List<VendorDataSeries> impactData = getQualMetricData(riskModelId, MetricDomain.IMPACT, null, vendorTypes);
		List<VendorDataSeries> likelihoodData = getQualMetricData(riskModelId, MetricDomain.LIKELIHOOD, null, vendorTypes);
		Map<Long, VendorDataSeries> summaryQualData = getLongVendorDataSeriesMap(impactData, likelihoodData);

		List<VendorDataSeries> quantVendorScores = buildQuantMetricData(riskModelId);

		// Initialize Section 1
		// DashboardChartItemDTO dashboardItem11 = new DashboardChartItemDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_CHART$ITEM_NAME), "", DashboardItemType.BarChart);
		DashboardChartItemDTO dashboardItem11 = new DashboardChartItemDTO(1l, "", "", DashboardItemType.BarChart);
		// dashboardItem11.setGridHeaders(Arrays.asList("Vendor", "Score"));
		dashboardItem11.setXAxis(clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_CHART$X_AXIS));
		dashboardItem11.setYAxis(clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_CHART$Y_AXIS));
		if (organization.getQualThreshold() != null) dashboardItem11.setThreshold(DashboardChartThresholdDTO.of(organization.getQualThreshold(), clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_CHART$THRESHOLD)));
		section1.getDashboardItems().add(dashboardItem11);

		// Total Qual Vendor Scores
		// DashboardTableItemDTO dashboardItem12 = new DashboardTableItemDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_TABLE$ITEM_NAME));
		DashboardDataGridItemDTO dashboardItem12 = new DashboardDataGridItemDTO(1l, "");
		dashboardItem12.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_TABLE$VENDOR_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_TABLE$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_TABLE$IMPACT_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_TABLE$IMPACT_TOTAL_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_TABLE$LIKELIHOOD_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$CYBER_RISK_TABLE$LIKELIHOOD_TOTAL_HEADER)
		));
		section2.getDashboardItems().add(dashboardItem12);
		for (Map.Entry<Long, VendorDataSeries> entry : summaryQualData.entrySet()) {
			VendorDataSeries dataSeries = entry.getValue();

			List<DashboardDataItemDTO> rowItems = createRowItems(dataSeries);
			dashboardItem12.getGridItems().add(rowItems);
			applyVendorDashboardQualsDrilldown(rowItems, dataSeries.getVendor(), VendorType.Cloud);

			List<DashboardDataItemDTO> chartItems = createChartItems(dataSeries);
			if (chartItems.size() > 0) chartItems.get(0).setDrilldown(DashboardDataItemDrilldownDTO.of(dataSeries.getVendor()).applyDrillDownType(VendorType.Cloud));
			dashboardItem11.getGridItems().add(chartItems);
		}

		// Init Heat Matrix and Heat Chart data
		Map<Long, DashboardDataItemDTO> heatChartItemsMap = new HashMap<>();
		List<DashboardDataItemDTO> heatChartItems = new ArrayList<>();
		List<List<DashboardDataItemDTO>> itemsMatrix = new ArrayList<>();
		buildHeatChartItems(summaryQualData, heatChartItemsMap, heatChartItems, itemsMatrix);

		// Create Heat Chart
		DashboardChartItemDTO dashboardItem130 = createHeatChartDashboardItem(heatChartItems);
		// dashboardItem130.setName(clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$HEAT_MAP_CHART$ITEM_NAME));
		dashboardItem130.setName("");
		section3.getDashboardItems().add(dashboardItem130);

		// Create Heat Matrix Table
		DashboardItemDTO dashboardItem13 = createHeatMatrixTableDashboardItem(itemsMatrix);
		// dashboardItem13.setName(clientMessage.getMessage(SLCT.DASHBOARDS$CLOUD$SUMMARY$HEAT_MAP_TABLE$ITEM_NAME));
		dashboardItem13.setName("");
		section4.getDashboardItems().add(dashboardItem13);

		// Total Quantification Scores
		DashboardItemDTO dashboardItem14 = vendorsDashboardService.createVendorQuantScoresDashboard(riskModel, null, null);
		dashboardItem14.setName("");
		section5.getDashboardItems().add(dashboardItem14);

		return dashboard;
	}

	// ########## ########## ########## ########## Build Heat Chart and Heat Map Data ########## ########## ########## ########## //

	private void buildHeatChartItems(Map<Long, VendorDataSeries> summaryQualData, Map<Long, DashboardDataItemDTO> heatChartItemsMap, List<DashboardDataItemDTO> heatChartItems, List<List<DashboardDataItemDTO>> itemsMatrix) {

		for (int i = 0; i < 5; i++) {
			itemsMatrix.add(new ArrayList<>());
			for (int j = 0; j < 5; j++) {
				itemsMatrix.get(i).add(sI(""));
			}
		}

		for (Map.Entry<Long, VendorDataSeries> entry : summaryQualData.entrySet()) {
			Double impactScore = 0D;
			Double likelihoodScore = 0D;

			VendorDataSeries dataSeries = entry.getValue();
			if (dataSeries.getItems().get(2) > 0) {
				impactScore = dataSeries.getItems().get(1) / dataSeries.getItems().get(2);
			}
			if (dataSeries.getItems().get(4) > 0) {
				likelihoodScore = dataSeries.getItems().get(3) / dataSeries.getItems().get(4);
			}
			int colNum = (int) (likelihoodScore * 5 >= 4 ? 4 : (likelihoodScore * 5));
			int rowNum = (int) (impactScore * 5 >= 4 ? 4 : (impactScore * 5));

			DashboardDataItemDTO currentItem = itemsMatrix.get(rowNum).get(colNum);
			if (StringUtils.isNotEmpty(currentItem.getValue())) {
				currentItem.setValue(currentItem.getValue() + ", " + dataSeries.getVendor().getName());
			} else {
				currentItem.setValue(dataSeries.getVendor().getName());
			}
			String color = getQualCondBGColor((impactScore + likelihoodScore) / 2);
			currentItem.applyBackgroundColor(color);
			// applyVendorDashboardQualsDrilldown(rowItems, dataSeries.getVendor());

			// Apply Heat Chart Items
			DashboardDataItemDTO heatChartItem = sI(dataSeries.getVendor().getName());
			heatChartItem.getParams().put("x", ((Double) (10 * likelihoodScore)).toString());
			heatChartItem.getParams().put("y", ((Double) (10 * impactScore)).toString());
			heatChartItem.getParams().put("radius", "16");
			heatChartItem.getParams().put("color", color != null ? color : "#808080");
			heatChartItems.add(heatChartItem);
			heatChartItemsMap.put(dataSeries.getVendor().getId(), heatChartItem);
		}
	}

	private DashboardChartItemDTO createHeatChartDashboardItem(List<DashboardDataItemDTO> heatChartItems) {
		// Create Heat Chart
		DashboardChartItemDTO dashboardItem = new DashboardChartItemDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MAP$HEAT_CHART$ITEM_NAME), "", DashboardItemType.HeatChart);
		dashboardItem.setXAxis(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MAP$HEAT_CHART$X_AXIS));
		dashboardItem.setYAxis(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MAP$HEAT_CHART$Y_AXIS));
		dashboardItem.getGridItems().add(heatChartItems);
		return dashboardItem;
	}

	private DashboardItemDTO createHeatMatrixTableDashboardItem(List<List<DashboardDataItemDTO>> itemsMatrix) {
		// Create Heat Table
		List<String> likelihoodHeader1 = Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$LIKELIHOOD_NONE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$LIKELIHOOD_MINOR_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$LIKELIHOOD_CRUCIAL_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$LIKELIHOOD_CRITICAL_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$LIKELIHOOD_DISASTROUS_HEADER)
		);
		List<String> likelihoodHeader2 = Arrays.asList("0-2", "2-4", "4-6", "6-8", "8-10");
		List<String> impactHeader1 = Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$IMPACT_IMPROBABLE_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$IMPACT_SELDOM_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$IMPACT_OCCASIONAL_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$IMPACT_LIKELY_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$IMPACT_FREQUENT_HEADER)
		);
		List<String> impactHeader2 = Arrays.asList("0-2", "2-4", "4-6", "6-8", "8-10");
		DashboardTableItemDTO dashboardItem13 = new DashboardTableItemDTO(1l, clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$ITEM_NAME));
		dashboardItem13.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$VENDOR_CYBER_RISK_HEADER)).applyHeader(true).applyColspan(3l).applyRowspan(3l).applyTextAlign("center"),
			sI(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$LIKELIHOOD_HEADER)).applyHeader(true).applyColspan(5l).applyTextAlign("center")
		));
		dashboardItem13.getGridItems().add(likelihoodHeader1.stream().map(headerString -> sI(headerString).applyParam("width", "15%").applyHeader(true).applyTextAlign("center")).collect(Collectors.toList()));
		dashboardItem13.getGridItems().add(likelihoodHeader2.stream().map(headerString -> sI(headerString).applyHeader(true).applyTextAlign("center")).collect(Collectors.toList()));
		for (int i = 0; i < 5; i++) dashboardItem13.getGridItems().add(new ArrayList<>());
		dashboardItem13.getGridItems().get(3).add(sI(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR$HEAT_MATRIX$HEAT_MATRIX_TABLE$IMPACT_HEADER)).applyHeader(true).applyRowspan(5l).applyTextAlign("center"));
		for (int i = 0; i < 5; i++) {
			itemsMatrix.add(new ArrayList<>());
			dashboardItem13.getGridItems().get(3 + i).add(sI(impactHeader1.get(i)).applyHeader(true).applyTextAlign("center"));
			dashboardItem13.getGridItems().get(3 + i).add(sI(impactHeader2.get(i)).applyParam("width", "80px").applyHeader(true).applyTextAlign("center"));
			for (int j = 0; j < 5; j++) {
				dashboardItem13.getGridItems().get(3 + i).add(itemsMatrix.get(i).get(j));
			}
		}
		return dashboardItem13;
	}

	// ########## ########## ########## ########## END Build Heat Chart and Heat Map Data ########## ########## ########## ########## //


	/**
	 * Get Dashboard definition by Drilldown Details
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getDashboardDetails(DashboardDataItemDrilldownDTO drilldown, Long riskModelId) {
		DashboardDTO dashboard = new DashboardDTO(4L, "Data Drilldown", "Data Drilldown Dashboard", DashboardType.Drilldown);

		if (drilldown.getView() == null) {
			throw new BadRequestException("View is mandatory parameter for Drilldown.");
		}

		if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.VENDOR_QUALS_DOMAIN)) {
			buildVendorQualMetricDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.VENDOR_QUALS)) {
			buildVendorQualDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.SYSTEM_QUALS_DOMAIN)) {
			buildSystemQualMetricDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.SYSTEM_QUALS)) {
			scoringQuestionsDashboardService.buildSystemScoringDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.VENDOR_QUANTS)) {
			exposureMetricsDashboardService.buildVendorDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.VENDOR_QUANTS_DOMAIN)) {
			exposureMetricsDashboardService.buildVendorDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.SYSTEM_QUANTS)) {
			exposureMetricsDashboardService.buildSystemDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.SYSTEM_QUANTS_DOMAIN)) {
			exposureMetricsDashboardService.buildSystemDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.ORGANIZATION_QUANTS)) {
			exposureMetricsDashboardService.buildOrganizationDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.ADMIN_SYSOWN)) {
			buildAdminSystemOwnerDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUANT)) {
			buildAdminSystemOwnerQuantDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL)) {
			buildAdminSystemOwnerQualDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL_VENDOR) || drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL_CLOUD)) {
			buildAdminSystemOwnerVendorQualDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.ADMIN_VNDOWN_QUAL) || drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.ADMIN_VNDOWN_QUAL_CLOUD)) {
			buildAdminVendorOwnerQualDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.BUDGET)) {
			buildBudgetDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.VENDOR)) {
			vendorsDashboardService.buildVendorDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.RESIDUAL_RISK)) {
			residualRiskDashboardService.buildResidualScoringDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.TASK)) {
			taskDashboardService.buildTaskDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.PROJECT)) {
			taskDashboardService.buildProjectDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.ASSESSMENT)) {
			assessmentDashboardService.buildAssessmentDrilldown(drilldown, riskModelId, dashboard);
		} else if (drilldown.getView().equalsIgnoreCase(DashboardDataItemDrilldownDTO.SYSTEM_GDPR_STATUS)) {
			organizationDashboardService.buildSystemGDPRStatusDrilldown(drilldown, riskModelId, dashboard);
		}

		return dashboard;
	}

	/**
	 * Build concrete Qual drilldown for vendor and metric
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	private void buildVendorQualMetricDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		Long vendorId = Long.valueOf(drilldown.getParams().get("vendor"));
		Organizations vendor = vendorService.getVendor(vendorId);
		MetricDomain metricDomain = MetricDomain.valueOf(drilldown.getParams().get("metricDomain"));
		List<VendorType> vendorTypes = extractVendorTypes(drilldown);

		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUALS_DOMAIN$ITEM_NAME), vendor.getName(), metricDomain.getCode()));

		DashboardItemDTO dashboardItem = getVendorQualMetricDataDrilldown(riskModelId, vendor, metricDomain, vendorTypes);
		section1.getDashboardItems().add(dashboardItem);
	}

	/**
	 * Build summary Qual drilldown for vendor
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	private void buildVendorQualDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		Long vendorId = Long.valueOf(drilldown.getParams().get("vendor"));
		Organizations vendor = vendorService.getVendor(vendorId);
		List<VendorType> vendorTypes = extractVendorTypes(drilldown);

		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUAL$ITEM_NAME), vendor.getName()));

		DashboardTableItemDTO dashboardItem0 = new DashboardTableItemDTO(1005l, "");
		section1.getDashboardItems().add(dashboardItem0);

		DashboardItemDTO dashboardItem1 = getVendorQualMetricDataDrilldown(riskModelId, vendor, MetricDomain.LIKELIHOOD, vendorTypes);
		section1.getDashboardItems().add(dashboardItem1);

		DashboardItemDTO dashboardItem2 = getVendorQualMetricDataDrilldown(riskModelId, vendor, MetricDomain.IMPACT, vendorTypes);
		section1.getDashboardItems().add(dashboardItem2);

		// Get Target Items
		Double likelihoodMetricValue = (Double) dashboardItem1.getParameters().get("metricValue");
		Double likelihoodMaxMetricValue = (Double) dashboardItem1.getParameters().get("maxMetricValue");
		Double likelihoodTargetMetricValue = (Double) dashboardItem1.getParameters().get("targetMetricValue");
		Double impactMetricValue = (Double) dashboardItem2.getParameters().get("metricValue");
		Double impactMaxMetricValue = (Double) dashboardItem2.getParameters().get("maxMetricValue");
		Double impactTargetMetricValue = (Double) dashboardItem2.getParameters().get("targetMetricValue");
		Double targetMetricValue = (likelihoodTargetMetricValue + impactTargetMetricValue) / 2;
		dashboardItem0.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUAL$TOTAL_CYBERRISK_SCORE$ITEM_NAME), sRound(targetMetricValue)));
		dashboardItem0.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUAL$TOTAL_CYBERRISK_SCORE$METRIC_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUAL$TOTAL_CYBERRISK_SCORE$FORMULA_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUAL$TOTAL_CYBERRISK_SCORE$CALCULATIONS_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUAL$TOTAL_CYBERRISK_SCORE$TARGET_VALUE_HEADER)
		));
		dashboardItem0.getGridItems().add(
			Arrays.asList(
				sI(MetricDomain.LIKELIHOOD.getCode()),
				sI(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUAL$TOTAL_CYBERRISK_SCORE$LIKELIHOOD_FORMULA)),
				sI(sRound(likelihoodMetricValue) + " / " + sRound(likelihoodMaxMetricValue)).applyTextAlign("right"),
				qualCondBGColor(dI(likelihoodTargetMetricValue).round())
			)
		);
		dashboardItem0.getGridItems().add(
			Arrays.asList(
				sI(MetricDomain.IMPACT.getCode()),
				sI(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUAL$TOTAL_CYBERRISK_SCORE$IMPACT_FORMULA)),
				sI(sRound(impactMetricValue) + " / " + sRound(impactMaxMetricValue)).applyTextAlign("right"),
				qualCondBGColor(dI(impactTargetMetricValue).round())
			)
		);
		dashboardItem0.getGridItems().add(
			Arrays.asList(
				sI(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUAL$TOTAL_CYBERRISK_SCORE$TOTAL_COLUMN_VALUE)),
				sI(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUAL$TOTAL_CYBERRISK_SCORE$TOTAL_FORMULA)),
				sI(MessageFormat.format("({0} + {1}) / 2", sRound(likelihoodTargetMetricValue), sRound(impactTargetMetricValue))).applyTextAlign("right"),
				qualCondBGColor(dI(targetMetricValue).round())
			)
		);
	}

	private List<VendorType> extractVendorTypes(DashboardDataItemDrilldownDTO drilldown) {
		List<VendorType> result;

		if (VendorType.Cloud.name().equalsIgnoreCase(drilldown.getParams().get("vendorName"))) {
			result = Arrays.asList(VendorType.Cloud);
		} else {
			result = Arrays.asList(VendorType.Both, VendorType.Vendor);
		}

		return result;
	}

	/**
	 * Build concrete Qual drilldown for vendor and metric
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	private void buildSystemQualMetricDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		Long systemId = Long.valueOf(drilldown.getParams().get("system"));
		Systems system = systemsService.getSystemForCurrentOrganization(systemId);
		MetricDomain metricDomain = MetricDomain.valueOf(drilldown.getParams().get("metricDomain"));

		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$SYSTEM_QUALS_DOMAIN$ITEM_NAME), system.getName(), metricDomain.getCode()));

		DashboardItemDTO dashboardItem = scoringQuestionsDashboardService.createSystemScoringMetricDrilldownDashboardItems(riskModelId, system, Arrays.asList(VendorType.System), metricDomain);
		section1.getDashboardItems().add(dashboardItem);
	}

	/**
	 * Build concrete Qual drilldown for vendor and Quant metric
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	private void buildVendorQuantMetricDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		Long vendorId = Long.valueOf(drilldown.getParams().get("vendor"));
		Long metricDomainId = Long.valueOf(drilldown.getParams().get("metricDomain"));
		Organizations vendor = vendorService.getVendor(vendorId);
		QuantsDomain metricDomain = QuantsDomain.of(metricDomainId);

		dashboard.setName(MessageFormat.format("Vendor \"{0}\". {1} Drilldown.", vendor.getName(), metricDomain.getName()));

		QuantMetrics quantMetric = getOneQuantMetric(riskModelId, metricDomain.getId());

		DashboardItemDTO dashboardItem = new DashboardItemDTO();
		if (metricDomain.equals(QuantsDomain.DATA_EXFILTRATION)) {
			dashboardItem = getVendorQuantMetricDataDrilldown_DataExfiltration(riskModelId, vendor, quantMetric);
		} else if (metricDomain.equals(QuantsDomain.BUSINESS_INTERRUPTION)) {
			dashboardItem = getVendorQuantMetricDataDrilldown_BusinessInterruption(riskModelId, vendor, quantMetric);
		} else if (metricDomain.equals(QuantsDomain.GDPR_REGULATORY_EXPOSURE)) {
			dashboardItem = getVendorQuantMetricDataDrilldown_GDPRRegulatoryExposure(riskModelId, vendor, quantMetric);
		}
		section1.getDashboardItems().add(dashboardItem);
	}

	/**
	 * Build concrete Qual drilldown for vendor and Quant metric
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	private void buildVendorQuantDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		QuantMetrics quantMetric;
		Double targetMetricValue = 0D;
		Double metricValue;
		DashboardItemDTO dashboardItem;

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		Long vendorId = Long.valueOf(drilldown.getParams().get("vendor"));
		Organizations vendor = vendorService.getVendor(vendorId);

		dashboard.setName(MessageFormat.format("Vendor \"{0}\". Quants Drilldown.", vendor.getName()));

		DashboardTableItemDTO dashboardItem0 = new DashboardTableItemDTO(1105l, "");
		section1.getDashboardItems().add(dashboardItem0);
		dashboardItem0.addGridHeaders(Arrays.asList("Metric Name", "Formula", "Calculations"));

		// Get Data Exfiltration Details
		quantMetric = getOneQuantMetric(riskModelId, QuantsDomain.DATA_EXFILTRATION.getId());
		dashboardItem = getVendorQuantMetricDataDrilldown_DataExfiltration(riskModelId, vendor, quantMetric);
		section1.getDashboardItems().add(dashboardItem);
		metricValue = (Double) dashboardItem.getParameters().get("metricValue");
		targetMetricValue += metricValue;
		dashboardItem0.getGridItems().add(Arrays.asList(sI("[" + quantMetric.getQuant().getName() + "] " + quantMetric.getName()), sI(quantMetricsService.buildFormula(quantMetric)).applyTextAlign("right"), $I(metricValue).round(0)));

		// Get Business Interruption Details
		quantMetric = getOneQuantMetric(riskModelId, QuantsDomain.BUSINESS_INTERRUPTION.getId());
		dashboardItem = getVendorQuantMetricDataDrilldown_BusinessInterruption(riskModelId, vendor, quantMetric);
		section1.getDashboardItems().add(dashboardItem);
		metricValue = (Double) dashboardItem.getParameters().get("metricValue");
		targetMetricValue += metricValue;
		dashboardItem0.getGridItems().add(Arrays.asList(sI("[" + quantMetric.getQuant().getName() + "] " + quantMetric.getName()), sI(quantMetricsService.buildFormula(quantMetric)).applyTextAlign("right"), $I(metricValue).round(0)));

		// Get GDPR Regulatory Exposure Details
		quantMetric = getOneQuantMetric(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE.getId());
		if (quantMetric != null && quantMetric.getQuant() != null) {
			dashboardItem = getVendorQuantMetricDataDrilldown_GDPRRegulatoryExposure(riskModelId, vendor, quantMetric);
			section1.getDashboardItems().add(dashboardItem);
			metricValue = Optional.ofNullable((Double) dashboardItem.getParameters().get("metricValue")).orElse(0D);
			targetMetricValue += metricValue;
			dashboardItem0.getGridItems().add(Arrays.asList(sI("[" + quantMetric.getQuant().getName() + "] " + quantMetric.getName()), sI(quantMetricsService.buildFormula(quantMetric)).applyTextAlign("right"), $I(metricValue).round(0)));
		}

		// Get Target Items
		dashboardItem0.getGridItems().add(
			Arrays.asList(
				sI("TOTAL:").applyTextAlign("right").applyColspan(2l).applyHeader(true),
				$I(targetMetricValue).round(0).applyHeader(true)
			)
		);
		dashboardItem0.setName(MessageFormat.format("Total Vendor Exposure: ${0}", sRound(targetMetricValue)));
	}

	/**
	 * Find one quant metric for risk model and metric domain
	 *
	 * @param riskModelId
	 * @param metricDomainId
	 * @return
	 */
	private QuantMetrics getOneQuantMetric(Long riskModelId, Long metricDomainId) {
		List<QuantMetrics> quantsList = quantMetricsRepository.getListByRiskModelIdAndQuantId(riskModelId, metricDomainId);
		QuantMetrics quantMetric = null;
		if (quantsList.size() > 0) {
			quantMetric = quantsList.get(0);
		}
		return quantMetric;
	}

	/**
	 * Get Quant metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	@Deprecated
	public DashboardItemDTO getVendorQuantMetricDataDrilldown_DataExfiltration(Long riskModelId, Organizations vendor, QuantMetrics quantMetric) {

		Long vendorId = vendor.getId();

		DashboardTableItemDTO dashboard = new DashboardTableItemDTO(1001L, "");
		dashboard.addGridHeaders(Arrays.asList("System", "# of records", "Formula", "Value"));

		RiskModels riskModel = riskModelRepository.findById(quantMetric.getRiskModelId()).get();
		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		Double vendorResult = 0D;
		List<List<DashboardDataItemDTO>> dashboardItemsList = new ArrayList<>();
		List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganizationAndVendor(riskModel.getOrganizationId(), vendorId);
		for (AssociateVendors associateVendor : associateVendors) {
			// Only "Crown Jewel" assets should be included
			List<Systems> systemsList = associateVendorRepository.getSystemsListForAssociateVendorAndDataAssetClass(associateVendor.getId(), AssetClass.CROWN_JEWEL.getId());
			if (systemsList != null && systemsList.size() > 0) {
				for (Systems system : systemsList) {
					FormulaResult formulaResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), 0D, 0D);
					vendorResult += formulaResult.getResult();

					List<DashboardDataItemDTO> questionDetails = new ArrayList<>();
					questionDetails.add(sI(system.getName()));
					questionDetails.add(sI(system.getNumberOfRecProcessed()));
					questionDetails.add(sI(formulaResult.getFormula()).applyTextAlign("right"));
					questionDetails.add($I(formulaResult.getResult()).round(0));

					dashboardItemsList.add(questionDetails);
				}
			}
		}
		/*
		dashboardItemsList.sort((o1, o2) -> {
			int value1 = Integer.valueOf(o1.get(5).getValue());
			int value2 = Integer.valueOf(o2.get(5).getValue());

			return value2 - value1;
		});
		*/

		dashboard.getGridItems().addAll(dashboardItemsList);

		Double metricValue = vendorResult;
		List<DashboardDataItemDTO> totlaDetails = new ArrayList<>();
		DashboardDataItemDTO totalCell = new DashboardDataItemDTO("TOTAL:", "right", null);
		totalCell.setColSpan(3l);
		totalCell.setHeader(true);
		totlaDetails.add(totalCell);
		totlaDetails.add($I(metricValue).round(0).applyHeader(true));
		dashboard.getGridItems().add(totlaDetails);

		// dashboard.setName(MessageFormat.format("Drill down to {0} metrics score: ${1}", quantMetric.getName(), sRound(metricValue)));
		dashboard.setName(MessageFormat.format("Data Exfiltration Exposure: ${0}", sRound(metricValue)));
		dashboard.getParameters().put("metricValue", metricValue);

		return dashboard;
	}

	/**
	 * Get Quant metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	@Deprecated
	public DashboardItemDTO getVendorQuantMetricDataDrilldown_BusinessInterruption(Long riskModelId, Organizations vendor, QuantMetrics quantMetric) {

		Long vendorId = vendor.getId();

		DashboardTableItemDTO dashboard = new DashboardTableItemDTO(1001L, "");
		dashboard.addGridHeaders(Arrays.asList("Process", "Process revenue", "Formula", "Value"));

		RiskModels riskModel = riskModelRepository.findById(quantMetric.getRiskModelId()).get();
		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		Double vendorResult = 0D;
		List<List<DashboardDataItemDTO>> dashboardItemsList = new ArrayList<>();
		List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganizationAndVendor(riskModel.getOrganizationId(), vendorId);
		for (AssociateVendors associateVendor : associateVendors) {
			// Only "Crown Jewel" assets should be included
			List<Systems> systemsList = associateVendorRepository.getSystemsListForAssociateVendorAndDataAssetClass(associateVendor.getId(), AssetClass.CROWN_JEWEL.getId());
			if (systemsList != null && systemsList.size() > 0) {
				Map<Long, Processes> systemProcessMap = new HashMap<>();
				for (Systems system : systemsList) {
					processRepository.getListBySystem(system.getId()).stream().forEach(process -> {
						systemProcessMap.put(process.getId(), process);
					});
				}

				for (Map.Entry<Long, Processes> systemProcess : systemProcessMap.entrySet()) {
					Processes process = systemProcess.getValue();
					FormulaResult formulaResult = calculateFormula(formulaItems, 0D, process.getRevenueProcessed(), 0D);
					vendorResult += formulaResult.getResult();

					List<DashboardDataItemDTO> questionDetails = new ArrayList<>();
					questionDetails.add(sI(process.getName()));
					questionDetails.add($I(process.getRevenueProcessed()).round(0));
					questionDetails.add(sI(formulaResult.getFormula()).applyTextAlign("right"));
					questionDetails.add($I(formulaResult.getResult()).round(0));

					dashboardItemsList.add(questionDetails);
				}
			}
		}
		/*
		dashboardItemsList.sort((o1, o2) -> {
			int value1 = Integer.valueOf(o1.get(5).getValue());
			int value2 = Integer.valueOf(o2.get(5).getValue());

			return value2 - value1;
		});
		*/

		dashboard.getGridItems().addAll(dashboardItemsList);

		Double metricValue = vendorResult;
		List<DashboardDataItemDTO> totlaDetails = new ArrayList<>();
		DashboardDataItemDTO totalCell = new DashboardDataItemDTO("TOTAL:", "right", null);
		totalCell.setColSpan(3l);
		totalCell.setHeader(true);
		totlaDetails.add(totalCell);
		totlaDetails.add($I(metricValue).round(0).applyHeader(true));
		dashboard.getGridItems().add(totlaDetails);

		// dashboard.setName(MessageFormat.format("Drill down to {0} metrics score: ${1}", quantMetric.getName(), sRound(metricValue)));
		dashboard.setName(MessageFormat.format("Business Interruption Exposure: ${0}", sRound(metricValue)));
		dashboard.getParameters().put("metricValue", metricValue);

		return dashboard;
	}

	/**
	 * Get Quant metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	@Deprecated
	public DashboardItemDTO getVendorQuantMetricDataDrilldown_GDPRRegulatoryExposure(Long riskModelId, Organizations vendor, QuantMetrics quantMetric) {

		Long vendorId = vendor.getId();

		DashboardTableItemDTO dashboard = new DashboardTableItemDTO(1001L, "");
		dashboard.addGridHeaders(Arrays.asList("Process", "Org revenue", "Formula", "Value"));

		RiskModels riskModel = riskModelRepository.findById(quantMetric.getRiskModelId()).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		Double vendorResult = 0D;
		List<List<DashboardDataItemDTO>> dashboardItemsList = new ArrayList<>();
		List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganizationAndVendor(riskModel.getOrganizationId(), vendorId);
		for (AssociateVendors associateVendor : associateVendors) {
			boolean isPrivacyCalculated = false;
			boolean isCreditCardCalculated = false;

			// Only "Crown Jewel" assets should be included
			List<Systems> systemsList = associateVendorRepository.getSystemsListForAssociateVendorAndDataAssetClass(associateVendor.getId(), AssetClass.CROWN_JEWEL.getId());
			if (systemsList != null && systemsList.size() > 0) {
				Map<Long, Processes> systemProcessMap = new HashMap<>();
				for (Systems system : systemsList) {
					processRepository.getListBySystem(system.getId()).stream().forEach(process -> {
						systemProcessMap.put(process.getId(), process);
					});
				}

				for (Map.Entry<Long, Processes> systemProcess : systemProcessMap.entrySet()) {
					Processes process = systemProcess.getValue();

					FormulaResult formulaResult = new FormulaResult();

					for (DataTypeClassification dataTypeClassification : process.getDataTypeClassifications()) {
						if (dataTypeClassification.getName().toLowerCase().indexOf("privacy") != -1) {
							// Double privacy = 0.04 * organization.getAverageRevenue();
							formulaResult = calculateFormula(formulaItems, 0D, process.getRevenueProcessed(), organization.getAverageRevenue());
							Double currentResult = (formulaResult.getResult() > 25000000) ? formulaResult.getResult() : 25000000;
							formulaResult.setResult(currentResult);
							formulaResult.setFormula(formulaResult.getFormula());

							if (!isPrivacyCalculated) {
								vendorResult += (formulaResult.getResult() > 25000000) ? formulaResult.getResult() : 25000000;
								isPrivacyCalculated = true;
							}
						}

						if (dataTypeClassification.getName().toLowerCase().indexOf("credit card") != -1) {
							if (!isCreditCardCalculated) {
								vendorResult += 100000 * 12;
								isCreditCardCalculated = true;
							}
							formulaResult.setResult((double) 100000 * 12);
							formulaResult.setFormula("(Processing Credit Cards): 100000 * 12");
						}
					}

					List<DashboardDataItemDTO> questionDetails = new ArrayList<>();
					questionDetails.add(sI(process.getName()));
					questionDetails.add($I(organization.getAverageRevenue()).round(0));
					questionDetails.add(sI(formulaResult.getFormula()).applyTextAlign("right"));
					questionDetails.add($I(formulaResult.getResult()).round(0));

					dashboardItemsList.add(questionDetails);
				}
			}
		}
		/*
		dashboardItemsList.sort((o1, o2) -> {
			int value1 = Integer.valueOf(o1.get(5).getValue());
			int value2 = Integer.valueOf(o2.get(5).getValue());

			return value2 - value1;
		});
		*/

		dashboard.getGridItems().addAll(dashboardItemsList);

		Double metricValue = vendorResult;
		List<DashboardDataItemDTO> totlaDetails = new ArrayList<>();
		DashboardDataItemDTO totalCell = new DashboardDataItemDTO("TOTAL:", "right", null);
		totalCell.setColSpan(3l);
		totalCell.setHeader(true);
		totlaDetails.add(totalCell);
		totlaDetails.add($I(metricValue).round(0).applyHeader(true));
		dashboard.getGridItems().add(totlaDetails);

		// dashboard.setName(MessageFormat.format("Drill down to {0} metrics score: ${1}", quantMetric.getName(), sRound(metricValue)));
		dashboard.setName(MessageFormat.format("Regulatory Exposure: ${0}", sRound(metricValue)));
		dashboard.getParameters().put("metricValue", metricValue);

		return dashboard;
	}


	/**
	 * Build concrete Qual drilldown for vendor and Quant metric
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	private void buildSystemQuantMetricDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		Long systemId = Long.valueOf(drilldown.getParams().get("system"));
		Long metricDomainId = Long.valueOf(drilldown.getParams().get("metricDomain"));
		Systems system = systemsService.getSystemForCurrentOrganization(systemId);
		QuantsDomain metricDomain = QuantsDomain.of(metricDomainId);

		dashboard.setName(MessageFormat.format("System \"{0}\". {1} Drilldown.", system.getName(), metricDomain.getName()));

		List<QuantMetrics> quantsList = quantMetricsRepository.getListByRiskModelIdAndQuantId(riskModelId, metricDomainId);
		QuantMetrics quantMetric = quantsList != null && quantsList.size() > 0 ? quantsList.get(0) : null;

		DashboardItemDTO dashboardItem = new DashboardItemDTO();

		if (quantMetric != null) {
			if (metricDomain.equals(QuantsDomain.DATA_EXFILTRATION)) {
				dashboardItem = getSystemQuantMetricDataDrilldown_DataExfiltration(riskModelId, system, quantMetric);
				section1.getDashboardItems().add(dashboardItem);
			} else if (metricDomain.equals(QuantsDomain.BUSINESS_INTERRUPTION)) {
				for (QuantMetrics quantMetric1 : quantsList) {
					dashboardItem = getSystemQuantMetricDataDrilldown_BusinessInterruption(riskModelId, system, quantMetric1);
					section1.getDashboardItems().add(dashboardItem);
				}
			} else if (metricDomain.equals(QuantsDomain.GDPR_REGULATORY_EXPOSURE) || metricDomain.equals(QuantsDomain.REGULATORY_LOSS)) {
				dashboardItem = getSystemQuantMetricDataDrilldown_RegulatoryExposure(riskModelId, system, quantMetric);
				section1.getDashboardItems().add(dashboardItem);
			} else {
				dashboardItem = getSystemQuantMetricDataDrilldown_Quant_Default(riskModelId, system, quantMetric);
				section1.getDashboardItems().add(dashboardItem);
			}
		} else {
			throw new BadRequestException(MessageFormat.format("Quantification [{0}] is not defined in the risk model.", metricDomain.getName()));
		}
	}

	/**
	 * Build concrete Qual drilldown for vendor and Quant metric
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	@Deprecated
	private void buildSystemQuantDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		QuantMetrics quantMetric;
		List<QuantMetrics> quantsList;
		Double targetMetricValue = 0D;
		Double metricValue;
		DashboardItemDTO dashboardItem;

		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO(20305L, "System Drilldown", null);
		if (dashboard.getSections().size() == 0) {
			dashboard.getSections().add(section1);
		} else {
			section1 = dashboard.getSections().get(0);
		}

		Long systemId = Long.valueOf(drilldown.getParams().get("system"));
		Systems system = systemsService.getSystemForCurrentOrganization(systemId);

		dashboard.setName(MessageFormat.format("System \"{0}\". Quants Drilldown.", system.getName()));

		DashboardTableItemDTO dashboardItem0 = new DashboardTableItemDTO(1105l, "");
		section1.getDashboardItems().add(dashboardItem0);
		dashboardItem0.addGridHeaders(Arrays.asList("Metric Name", "Formula", "Calculations"));

		// Get Data Exfiltration Details
		quantMetric = getOneQuantMetric(riskModelId, QuantsDomain.DATA_EXFILTRATION.getId());
		if (quantMetric != null) {
			dashboardItem = getSystemQuantMetricDataDrilldown_DataExfiltration(riskModelId, system, quantMetric);
			section1.getDashboardItems().add(dashboardItem);
			metricValue = (Double) dashboardItem.getParameters().get("metricValue");
			targetMetricValue += metricValue;
			dashboardItem0.getGridItems().add(Arrays.asList(sI("[" + quantMetric.getQuant().getName() + "] " + quantMetric.getName()), sI(quantMetricsService.buildFormula(quantMetric)).applyTextAlign("right"), $I(metricValue).round(0)));
		}

		// Get Business Interruption Details
		// quantMetric = getOneQuantMetric(riskModelId, QuantsDomain.BUSINESS_INTERRUPTION.getId());
		quantsList = quantMetricsRepository.getListByRiskModelIdAndQuantId(riskModelId, QuantsDomain.BUSINESS_INTERRUPTION.getId());
		metricValue = 0D;
		for (QuantMetrics quantMetric1 : quantsList) {
			dashboardItem = getSystemQuantMetricDataDrilldown_BusinessInterruption(riskModelId, system, quantMetric1);
			section1.getDashboardItems().add(dashboardItem);
			metricValue += (Double) dashboardItem.getParameters().get("metricValue");
			targetMetricValue += metricValue;
			dashboardItem0.getGridItems().add(Arrays.asList(sI("[" + quantMetric1.getName() + "] "), sI(quantMetricsService.buildFormula(quantMetric1)).applyTextAlign("right"), $I(metricValue).round(0)));
		}

		// Get GDPR Regulatory Exposure Details
		quantMetric = getOneQuantMetric(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE.getId());
		if (quantMetric != null) {
			dashboardItem = getSystemQuantMetricDataDrilldown_RegulatoryExposure(riskModelId, system, quantMetric);
			section1.getDashboardItems().add(dashboardItem);
			metricValue = (Double) dashboardItem.getParameters().get("metricValue");
			targetMetricValue += Optional.ofNullable(metricValue).orElse(0d);
			dashboardItem0.getGridItems().add(Arrays.asList(sI("[" + quantMetric.getQuant().getName() + "] " + quantMetric.getName()), sI(quantMetricsService.buildFormula(quantMetric)).applyTextAlign("right"), $I(metricValue).round(0)));
		}

		// Get Target Items
		dashboardItem0.getGridItems().add(
			Arrays.asList(
				sI("TOTAL:").applyTextAlign("right").applyColspan(2l).applyHeader(true),
				$I(targetMetricValue).round(0).applyHeader(true)
			)
		);
		dashboardItem0.setName(MessageFormat.format("Total System Exposure: ${0}", sRound(targetMetricValue)));
	}

	/**
	 * Build Full Admin drilldown for System Owner
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	private void buildAdminSystemOwnerDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		// Load Initial Data
		Long userId = Long.valueOf(drilldown.getParams().get(DashboardDataItemDrilldownDTO.PARAM_ITEM));
		Users systemOwner = userService.getOrganizationUser(userId);
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Set<Systems> systemsSet = systemRepository.getAllBySystemOwnerAndOrganization(systemOwner.getId(), riskModel.getOrganizationId()).stream().collect(Collectors.toSet());
		Set<Organizations> vendorsSet = organizationRepository.getAllByOwnerAndOrganization(systemOwner.getId(), riskModel.getOrganizationId(), OrganizationType.Vendor).stream().collect(Collectors.toSet());
		Set<Organizations> cloudVendorsSet = vendorsSet.stream().filter(organizations -> Boolean.TRUE.equals(organizations.getIsCloudVendor())).collect(Collectors.toSet());

		List<AssociateVendors> associateVendors = associateVendorRepository.getAssociateVendorsListForSystemOwner(systemOwner.getId());
		Map<Systems, Set<Organizations>> associateVendorsMap = new HashMap<>();
		for (AssociateVendors associateVendor : associateVendors) {
			for (Systems system : associateVendor.getSystems()) {
				if (!associateVendorsMap.containsKey(system)) {
					associateVendorsMap.put(system, new HashSet<>());
				}
				associateVendorsMap.get(system).add(associateVendor.getVendor());
			}
		}

		Map<Systems, SystemDataSeries> impactDataSystemMap = getQualMetricDataForSystems(riskModelId, MetricDomain.IMPACT, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> likelihoodDataSystemMap = getQualMetricDataForSystems(riskModelId, MetricDomain.LIKELIHOOD, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));

		List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor);
		Map<Organizations, VendorDataSeries> impactDataVendorMap = getQualMetricData(riskModelId, MetricDomain.IMPACT, null, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));
		Map<Organizations, VendorDataSeries> likelihoodDataVendorMap = getQualMetricData(riskModelId, MetricDomain.LIKELIHOOD, null, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));

		vendorTypes = Arrays.asList(VendorType.Cloud);
		Map<Organizations, VendorDataSeries> impactDataCloudMap = getQualMetricData(riskModelId, MetricDomain.IMPACT, null, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));
		Map<Organizations, VendorDataSeries> likelihoodDataCloudMap = getQualMetricData(riskModelId, MetricDomain.LIKELIHOOD, null, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));

		DashboardItemDTO dashboardItem1 = getAdminSystemOwnerQuantDrilldownDashboardItem(riskModel, systemsSet);
		DashboardItemDTO dashboardItem2 = getAdminSystemOwnerQualDrilldownDashboardItem(systemsSet, impactDataSystemMap, likelihoodDataSystemMap);
		DashboardItemDTO dashboardItem3 = getAdminVendorOwnerQualDrilldownDashboardItem(vendorsSet, VendorType.Vendor, impactDataVendorMap, likelihoodDataVendorMap);
		DashboardItemDTO dashboardItem4 = getAdminVendorOwnerQualDrilldownDashboardItem(cloudVendorsSet, VendorType.Cloud, impactDataCloudMap, likelihoodDataCloudMap);
		// DashboardItemDTO dashboardItem5 = getAdminSystemOwnerVendorQualDrilldownDashboardItem(systemsSet, associateVendorsMap, VendorType.Vendor, impactDataVendorMap, likelihoodDataVendorMap);
		// DashboardItemDTO dashboardItem6 = getAdminSystemOwnerVendorQualDrilldownDashboardItem(systemsSet, associateVendorsMap, VendorType.Cloud, impactDataCloudMap, likelihoodDataCloudMap);

		section1.getDashboardItems().add(dashboardItem1);
		section1.getDashboardItems().add(dashboardItem2);
		section1.getDashboardItems().add(dashboardItem3);
		section1.getDashboardItems().add(dashboardItem4);
		// section1.getDashboardItems().add(dashboardItem5);
		// section1.getDashboardItems().add(dashboardItem6);

		String businessUnitNameLabel = systemOwner.getBusinessUnit() != null ? ", BU [" + businessUnitService.getBusinessUnitPath(systemOwner.getBusinessUnit(), true) + "]" : "";
		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$ITEM_NAME), systemOwner.getFullName(), businessUnitNameLabel));
	}

	/**
	 * Build Quant Admin drilldown for System Owner
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	private void buildAdminSystemOwnerQuantDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		// Load Initial Data
		Long userId = Long.valueOf(drilldown.getParams().get(DashboardDataItemDrilldownDTO.PARAM_ITEM));
		Users systemOwner = userService.getOrganizationUser(userId);
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Set<Systems> systemsSet = systemRepository.getAllBySystemOwnerAndOrganization(systemOwner.getId(), riskModel.getOrganizationId()).stream().collect(Collectors.toSet());

		DashboardItemDTO dashboardItem = getAdminSystemOwnerQuantDrilldownDashboardItem(riskModel, systemsSet);

		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_QUANT$ITEM_NAME), systemOwner.getFullName()));
		section1.getDashboardItems().add(dashboardItem);
	}

	/**
	 * Build Qual Admin drilldown for System Owner
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	private void buildAdminSystemOwnerQualDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		// Load Initial Data
		Long userId = Long.valueOf(drilldown.getParams().get(DashboardDataItemDrilldownDTO.PARAM_ITEM));
		Users systemOwner = userService.getOrganizationUser(userId);
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Set<Systems> systemsSet = systemRepository.getAllBySystemOwnerAndOrganization(systemOwner.getId(), riskModel.getOrganizationId()).stream().collect(Collectors.toSet());

		Map<Systems, SystemDataSeries> impactDataSystemMap = getQualMetricDataForSystems(riskModelId, MetricDomain.IMPACT, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));
		Map<Systems, SystemDataSeries> likelihoodDataSystemMap = getQualMetricDataForSystems(riskModelId, MetricDomain.LIKELIHOOD, systemsSet).stream().collect(Collectors.toMap(SystemDataSeries::getSystem, systemDataSeries -> systemDataSeries));

		DashboardItemDTO dashboardItem = getAdminSystemOwnerQualDrilldownDashboardItem(systemsSet, impactDataSystemMap, likelihoodDataSystemMap);

		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_QUAL$ITEM_NAME), systemOwner.getFullName()));
		section1.getDashboardItems().add(dashboardItem);
	}

	/**
	 * Build Qual Vendor/Cloud Admin drilldown for System Owner
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	private void buildAdminSystemOwnerVendorQualDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		// Load Initial Data
		Long userId = Long.valueOf(drilldown.getParams().get(DashboardDataItemDrilldownDTO.PARAM_ITEM));
		Users systemOwner = userService.getOrganizationUser(userId);
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Set<Systems> systemsSet = systemRepository.getAllBySystemOwnerAndOrganization(systemOwner.getId(), riskModel.getOrganizationId()).stream().collect(Collectors.toSet());

		List<AssociateVendors> associateVendors = associateVendorRepository.getAssociateVendorsListForSystemOwner(systemOwner.getId());
		Map<Systems, Set<Organizations>> associateVendorsMap = new HashMap<>();
		for (AssociateVendors associateVendor : associateVendors) {
			for (Systems system : associateVendor.getSystems()) {
				if (!associateVendorsMap.containsKey(system)) {
					associateVendorsMap.put(system, new HashSet<>());
				}
				associateVendorsMap.get(system).add(associateVendor.getVendor());
			}
		}

		List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor);
		VendorType currentType = VendorType.Vendor;
		if (DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL_CLOUD.equalsIgnoreCase(drilldown.getView()) || DashboardDataItemDrilldownDTO.ADMIN_VNDOWN_QUAL_CLOUD.equalsIgnoreCase(drilldown.getView())) {
			vendorTypes = Arrays.asList(VendorType.Cloud);
			currentType = VendorType.Cloud;
		}
		Map<Organizations, VendorDataSeries> impactDataVendorMap = getQualMetricData(riskModelId, MetricDomain.IMPACT, null, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));
		Map<Organizations, VendorDataSeries> likelihoodDataVendorMap = getQualMetricData(riskModelId, MetricDomain.LIKELIHOOD, null, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));

		// Build Dashboard Item
		DashboardItemDTO dashboardItem = getAdminSystemOwnerVendorQualDrilldownDashboardItem(systemsSet, associateVendorsMap, currentType, impactDataVendorMap, likelihoodDataVendorMap);

		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$ITEM_NAME), systemOwner.getFullName()));
		section1.getDashboardItems().add(dashboardItem);
	}

	/**
	 * Build Qual Vendor/Cloud Admin drilldown for System Owner
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	private void buildAdminVendorOwnerQualDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		// Load Initial Data
		Long userId = Long.valueOf(drilldown.getParams().get(DashboardDataItemDrilldownDTO.PARAM_ITEM));
		Users vendorOwner = userService.getOrganizationUser(userId);
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

		List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor);
		VendorType currentType = VendorType.Vendor;
		if (DashboardDataItemDrilldownDTO.ADMIN_VNDOWN_QUAL_CLOUD.equalsIgnoreCase(drilldown.getView())) {
			vendorTypes = Arrays.asList(VendorType.Cloud);
			currentType = VendorType.Cloud;
		}
		Set<Organizations> vendorsSet = organizationRepository.getAllByOwnerAndOrganization(vendorOwner.getId(), riskModel.getOrganizationId(), OrganizationType.Vendor).stream().collect(Collectors.toSet());
		Map<Organizations, VendorDataSeries> impactDataVendorMap = getQualMetricData(riskModelId, MetricDomain.IMPACT, null, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));
		Map<Organizations, VendorDataSeries> likelihoodDataVendorMap = getQualMetricData(riskModelId, MetricDomain.LIKELIHOOD, null, vendorTypes).stream().collect(Collectors.toMap(VendorDataSeries::getVendor, vendorDataSeries -> vendorDataSeries));

		// Build Dashboard Item
		DashboardItemDTO dashboardItem = getAdminVendorOwnerQualDrilldownDashboardItem(vendorsSet, currentType, impactDataVendorMap, likelihoodDataVendorMap);

		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_VENOWN_QUAL$ITEM_NAME), vendorOwner.getFullName()));
		section1.getDashboardItems().add(dashboardItem);
	}

	private DashboardItemDTO getAdminSystemOwnerQuantDrilldownDashboardItem(RiskModels riskModel, Set<Systems> systemsSet) {
		QuantMetrics quantMetric = getOneQuantMetric(riskModel.getId(), QuantsDomain.DATA_EXFILTRATION.getId());
		List<SystemDataSeries> dataExfiltrations = getOrganizationQuantMetricData(riskModel.getId(), QuantsDomain.DATA_EXFILTRATION, null, systemsSet);
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(3105l, clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUANT$ITEM_NAME) + QuantsDomain.DATA_EXFILTRATION.getName());
		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUANT$SYSTEM_NAME_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUANT$FORMULA_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUANT$NUMBER_OF_RECORDS_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUANT$DATA_EXFILTRATION_HEADER)
		));
		Double total = 0d;
		for (SystemDataSeries dataSeries : dataExfiltrations) {
			Double currentNumber = dataSeries.getItems().get(0);
			dashboardItem.getGridItems().add(Arrays.asList(
				sI(dataSeries.getSystem().getName()),
				sI(quantMetricsService.buildFormula(quantMetric)).applyTextAlign("right"),
				sI(Optional.ofNullable(dataSeries.getSystem().getNumberOfRecProcessed()).orElse(0D)).round(0),
				$I(currentNumber).round(0))
			);
			if (currentNumber != null) total += currentNumber;
		}
		dashboardItem.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$TOTAL)).applyColspan(3l).applyTextAlign("right"),
			$I(total).round(0))
		);
		return dashboardItem;
	}

	private DashboardItemDTO getAdminSystemOwnerQualDrilldownDashboardItem(Set<Systems> systemsSet, Map<Systems, SystemDataSeries> impactDataSystemMap, Map<Systems, SystemDataSeries> likelihoodDataSystemMap) {
		// Initialize Organization Summary Scores
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(31l, clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUAL$ITEM_NAME));

		// Add Top Header
		dashboardItem.addGridHeaders(Arrays.asList(
			"",
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUAL$IMPACT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUAL$LIKELIHOOD_HEADER)
		));
		dashboardItem.getGridItems().get(0).get(1).setColSpan(3l);
		dashboardItem.getGridItems().get(0).get(2).setColSpan(3l);
		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUAL$SYSTEM_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUAL$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUAL$ANSWERS_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUAL$TOTAL_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUAL$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUAL$ANSWERS_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$SYSTEM_QUAL$TOTAL_WEIGHT_HEADER)
		));

		for (Systems system : systemsSet) {

			List<Double> itemsData = Arrays.asList(0D, 0D, 0D, 0D, 0D, 0D);

			List<DashboardDataItemDTO>  itemsList = new ArrayList<>();
			itemsList.add(sI(system.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.of(system)));

			if (impactDataSystemMap.containsKey(system)) {
				SystemDataSeries impact = impactDataSystemMap.get(system);
				itemsData.set(0, impact.getItems().get(0));
				itemsData.set(1, impact.getItems().get(1));
				itemsData.set(2, impact.getItems().get(2));
			}

			if (likelihoodDataSystemMap.containsKey(system)) {
				SystemDataSeries likelihood = likelihoodDataSystemMap.get(system);
				itemsData.set(3, likelihood.getItems().get(0));
				itemsData.set(4, likelihood.getItems().get(1));
				itemsData.set(5, likelihood.getItems().get(2));
			}

			itemsList.add(sI(itemsData.get(0)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, MetricDomain.IMPACT)));
			itemsList.add(sI(itemsData.get(1)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, MetricDomain.IMPACT)));
			itemsList.add(sI(itemsData.get(2)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, MetricDomain.IMPACT)));
			itemsList.add(sI(itemsData.get(3)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, MetricDomain.LIKELIHOOD)));
			itemsList.add(sI(itemsData.get(4)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, MetricDomain.LIKELIHOOD)));
			itemsList.add(sI(itemsData.get(5)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, MetricDomain.LIKELIHOOD)));

			dashboardItem.getGridItems().add(itemsList);
		}
		return dashboardItem;
	}

	private DashboardItemDTO getAdminSystemOwnerVendorQualDrilldownDashboardItem(Set<Systems> systemsSet, Map<Systems, Set<Organizations>> associateVendorsMap, VendorType currentType, Map<Organizations, VendorDataSeries> impactDataVendorMap, Map<Organizations, VendorDataSeries> likelihoodDataVendorMap) {
		// Initialize Organization Summary Scores
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(31l, clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$VENDOR_QUAL_SCORES$ITEM_NAME));
		if (VendorType.Cloud.equals(currentType)) {
			dashboardItem.setName(clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$VENDOR_QUAL_SCORES$ALTERNATIVE_ITEM_NAME));
		}

		// Add Top Header
		dashboardItem.addGridHeaders(Arrays.asList(
			"",
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$VENDOR_QUAL_SCORES$IMPACT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$VENDOR_QUAL_SCORES$LIKELIHOOD_HEADER)
		));
		dashboardItem.getGridItems().get(0).get(0).setColSpan(1l);
		dashboardItem.getGridItems().get(0).get(1).setColSpan(3l);
		dashboardItem.getGridItems().get(0).get(2).setColSpan(3l);
		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$VENDOR_QUAL_SCORES$SYSTEM_VENDOR_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$VENDOR_QUAL_SCORES$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$VENDOR_QUAL_SCORES$ANSWER_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$VENDOR_QUAL_SCORES$TOTAL_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$VENDOR_QUAL_SCORES$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$VENDOR_QUAL_SCORES$ANSWER_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN_VENDOR_QUAL$VENDOR_QUAL_SCORES$TOTAL_WEIGHT_HEADER)
		));
		// dashboardItem.getGridItems().get(1).get(0).setColSpan(2l);

		for (Systems system : systemsSet) {

			// Apply system Items
			List<DashboardDataItemDTO>  systemItemsList = new ArrayList<>();
			List<Double> systemItemsData = Arrays.asList(0D, 0D, 0D, 0D, 0D, 0D);
			systemItemsList.add(sI(system.getName()).applyBackgroundColor("#ebebeb"));
			dashboardItem.getGridItems().add(systemItemsList);

			if (associateVendorsMap.containsKey(system)) {
				Set<Organizations> systemVendors = associateVendorsMap.get(system);
				int currentItem = 0;
				for (Organizations systemVendor : systemVendors) {

					List<Double> itemsData = Arrays.asList(0D, 0D, 0D, 0D, 0D, 0D);

					List<DashboardDataItemDTO>  itemsList = new ArrayList<>();
					// itemsList.add(sI(String.valueOf(++currentItem) + ".").applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor)));
					itemsList.add(sI("  " + String.valueOf(++currentItem) + ". " + systemVendor.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor)));

					if (impactDataVendorMap.containsKey(systemVendor)) {
						VendorDataSeries impact = impactDataVendorMap.get(systemVendor);
						itemsData.set(0, impact.getItems().get(0));
						itemsData.set(1, impact.getItems().get(1));
						itemsData.set(2, impact.getItems().get(2));
						systemItemsData.set(1, systemItemsData.get(1) + impact.getItems().get(1));
						systemItemsData.set(2, systemItemsData.get(2) + impact.getItems().get(2));
					}

					if (likelihoodDataVendorMap.containsKey(systemVendor)) {
						VendorDataSeries likelihood = likelihoodDataVendorMap.get(systemVendor);
						itemsData.set(3, likelihood.getItems().get(0));
						itemsData.set(4, likelihood.getItems().get(1));
						itemsData.set(5, likelihood.getItems().get(2));
						systemItemsData.set(4, systemItemsData.get(4) + likelihood.getItems().get(1));
						systemItemsData.set(5, systemItemsData.get(5) + likelihood.getItems().get(2));
					}

					itemsList.add(sI(itemsData.get(0)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.IMPACT)));
					itemsList.add(sI(itemsData.get(1)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.IMPACT)));
					itemsList.add(sI(itemsData.get(2)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.IMPACT)));
					itemsList.add(sI(itemsData.get(3)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.LIKELIHOOD)));
					itemsList.add(sI(itemsData.get(4)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.LIKELIHOOD)));
					itemsList.add(sI(itemsData.get(5)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.LIKELIHOOD)));

					// Apply Cloud Drilldown
					if (currentType.equals(VendorType.Cloud)) {
						for (DashboardDataItemDTO item : itemsList) {
							item.getDrilldown().applyDrillDownType(VendorType.Cloud);
						}
					}

					dashboardItem.getGridItems().add(itemsList);
				}
			}

			if (systemItemsData.get(2) > 0) {
				systemItemsData.set(0, systemItemsData.get(1) / systemItemsData.get(2));
			}

			if (systemItemsData.get(5) > 0) {
				systemItemsData.set(3, systemItemsData.get(4) / systemItemsData.get(5));
			}

			systemItemsList.add(sI(systemItemsData.get(0)).applyBackgroundColor("#ebebeb").round(2));
			systemItemsList.add(sI(systemItemsData.get(1)).applyBackgroundColor("#ebebeb").round(2));
			systemItemsList.add(sI(systemItemsData.get(2)).applyBackgroundColor("#ebebeb").round(2));
			systemItemsList.add(sI(systemItemsData.get(3)).applyBackgroundColor("#ebebeb").round(2));
			systemItemsList.add(sI(systemItemsData.get(4)).applyBackgroundColor("#ebebeb").round(2));
			systemItemsList.add(sI(systemItemsData.get(5)).applyBackgroundColor("#ebebeb").round(2));

			/*
			systemItemsList.add(sI(systemItemsData.get(0)).applyBackgroundColor("#ebebeb").applyColor("#00ffca").round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL_SYSTEM, MetricDomain.IMPACT)));
			systemItemsList.add(sI(systemItemsData.get(1)).applyBackgroundColor("#ebebeb").applyColor("#00ffca").round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL_SYSTEM, MetricDomain.IMPACT)));
			systemItemsList.add(sI(systemItemsData.get(2)).applyBackgroundColor("#ebebeb").applyColor("#00ffca").round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL_SYSTEM, MetricDomain.IMPACT)));
			systemItemsList.add(sI(systemItemsData.get(3)).applyBackgroundColor("#ebebeb").applyColor("#00ffca").round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL_SYSTEM, MetricDomain.LIKELIHOOD)));
			systemItemsList.add(sI(systemItemsData.get(4)).applyBackgroundColor("#ebebeb").applyColor("#00ffca").round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL_SYSTEM, MetricDomain.LIKELIHOOD)));
			systemItemsList.add(sI(systemItemsData.get(5)).applyBackgroundColor("#ebebeb").applyColor("#00ffca").round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(system, DashboardDataItemDrilldownDTO.ADMIN_SYSOWN_QUAL_SYSTEM, MetricDomain.LIKELIHOOD)));
			*/
		}

		return dashboardItem;
	}

	@Deprecated
	private DashboardItemDTO getAdminVendorOwnerQualDrilldownDashboardItem(Set<Organizations> vendorsSet, VendorType currentType, Map<Organizations, VendorDataSeries> impactDataVendorMap, Map<Organizations, VendorDataSeries> likelihoodDataVendorMap) {
		// Initialize Organization Summary Scores
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(31l, clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$VENDOR_QUAL$ITEM_NAME));
		if (VendorType.Cloud.equals(currentType)) {
			dashboardItem.setName(clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$VENDOR_QUAL$ALTERNATIVE_ITEM_NAME));
		}

		// Add Top Header
		dashboardItem.addGridHeaders(Arrays.asList(
			"",
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$VENDOR_QUAL$IMPACT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$VENDOR_QUAL$LIKELIHOOD_HEADER)
		));
		dashboardItem.getGridItems().get(0).get(0).setColSpan(1l);
		dashboardItem.getGridItems().get(0).get(1).setColSpan(3l);
		dashboardItem.getGridItems().get(0).get(2).setColSpan(3l);
		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$VENDOR_QUAL$SYSTEM_VENDOR_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$VENDOR_QUAL$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$VENDOR_QUAL$ANSWERS_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$VENDOR_QUAL$TOTAL_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$VENDOR_QUAL$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$VENDOR_QUAL$ANSWERS_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$ADMIN_SYSOWN$VENDOR_QUAL$TOTAL_WEIGHT_HEADER)
		));

		for (Organizations vendor : vendorsSet) {

			// Apply system Items
			List<DashboardDataItemDTO>  systemItemsList = new ArrayList<>();

			List<Double> itemsData = Arrays.asList(0D, 0D, 0D, 0D, 0D, 0D);

			List<DashboardDataItemDTO>  itemsList = new ArrayList<>();
			itemsList.add(sI(vendor.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor)));

			if (impactDataVendorMap.containsKey(vendor)) {
				VendorDataSeries impact = impactDataVendorMap.get(vendor);
				itemsData.set(0, impact.getItems().get(0));
				itemsData.set(1, impact.getItems().get(1));
				itemsData.set(2, impact.getItems().get(2));
			}

			if (likelihoodDataVendorMap.containsKey(vendor)) {
				VendorDataSeries likelihood = likelihoodDataVendorMap.get(vendor);
				itemsData.set(3, likelihood.getItems().get(0));
				itemsData.set(4, likelihood.getItems().get(1));
				itemsData.set(5, likelihood.getItems().get(2));
			}

			itemsList.add(sI(itemsData.get(0)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor, MetricDomain.IMPACT)));
			itemsList.add(sI(itemsData.get(1)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor, MetricDomain.IMPACT)));
			itemsList.add(sI(itemsData.get(2)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor, MetricDomain.IMPACT)));
			itemsList.add(sI(itemsData.get(3)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor, MetricDomain.LIKELIHOOD)));
			itemsList.add(sI(itemsData.get(4)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor, MetricDomain.LIKELIHOOD)));
			itemsList.add(sI(itemsData.get(5)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor, MetricDomain.LIKELIHOOD)));

			// Apply Cloud Drilldown
			if (currentType.equals(VendorType.Cloud)) {
				for (DashboardDataItemDTO item : itemsList) {
					item.getDrilldown().applyDrillDownType(VendorType.Cloud);
				}
			}

			dashboardItem.getGridItems().add(itemsList);
		}

		return dashboardItem;
	}

	/**
	 * Build Budget drilldown for System Owner
	 *
	 * @param drilldown
	 * @param riskModelId
	 * @param dashboard
	 */
	@Deprecated
	private void buildBudgetDrilldown(DashboardDataItemDrilldownDTO drilldown, Long riskModelId, DashboardDTO dashboard) {
		// Create Initial Sections
		DashboardSectionDTO section1 = new DashboardSectionDTO();
		dashboard.getSections().add(section1);

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();

		String category = drilldown.getParams().get(DashboardDataItemDrilldownDTO.PARAM_CATEGORY);
		DashboardItemDTO dashboardItem = new DashboardTableItemDTO(104003L, "");
		if (DashboardDataItemDrilldownDTO.CATEGORY_BUDGET_FIXED_CAPITAL_COSTS.equals(category)) {
			dashboardItem = buildFixedCapitalCostsDashboardItem(riskModel);
		} else if (DashboardDataItemDrilldownDTO.CATEGORY_BUDGET_FIXED_OPERATIONAL_COSTS.equals(category)) {
			dashboardItem = buildFixedOperationalCostsDashboardItem(riskModel);
		} else if (DashboardDataItemDrilldownDTO.CATEGORY_BUDGET_VARIABLE_OPERATIONAL_COSTS.equals(category)) {
			dashboardItem = buildVariableOperationalCostsDashboardItem(riskModel);
		}

		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$ITEM_NAME), category));
		section1.getDashboardItems().add(dashboardItem);
	}

	/**
	 * Build Fixed Capital Costs Dashboard Item
	 *
	 * @param riskModel
	 * @return
	 */
	@Deprecated
	private DashboardItemDTO buildFixedCapitalCostsDashboardItem(RiskModels riskModel) {
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(104003L, clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_CAPITAL_COSTS$ITEM_NAME));

		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_CAPITAL_COSTS$FIXED_CAPITAL_COSTS_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_CAPITAL_COSTS$BU_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_CAPITAL_COSTS$DATE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_CAPITAL_COSTS$LICENSE_COST_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_CAPITAL_COSTS$TOTAL_COST_HEADER)
		));
		List<FixedCapitalCosts> items = fixedCapitalCostRepository.findAllByOrganizationId(riskModel.getOrganizationId());
		Double totalCosts = fixedCapitalCostRepository.getTotalCosts(riskModel.getOrganizationId());
		for (FixedCapitalCosts item : items) {
		//	String positionName = item.getCybersecurityTool() != null ? item.getCybersecurityTool().getName() : "-";
			String positionName = item.getTechnology() != null ? item.getTechnology().getName() : "-";
			String costDateString = item.getCostDate() != null ? dateFormat.format(item.getCostDate()) : "";
			dashboardItem.getGridItems().add(Arrays.asList(
				sI(positionName).applyTextAlign("left"),
				sI(businessUnitService.getBusinessUnitPath(item.getBusinessUnit(), true)).applyTextAlign("left"),
				sI(costDateString).applyTextAlign("right"),
				$I(item.getLicenseCost()).round(0),
				$I(item.getTotalCosts()).round(0)
			));
		}

		dashboardItem.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_CAPITAL_COSTS$TOTAL_TOOL_COST_HEADER)).applyTextAlign("right").applyColspan(4l).applyHeader(true),
			$I(totalCosts).round(0)
		));

		return dashboardItem;
	}

	/**
	 * Build Fixed Operational Costs Dashboard Item
	 *
	 * @param riskModel
	 * @return
	 */
	@Deprecated
	private DashboardItemDTO buildFixedOperationalCostsDashboardItem(RiskModels riskModel) {
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(104004L, clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_OPERATIONAL_COSTS$ITEM_NAME));

		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_OPERATIONAL_COSTS$FIXED_OPERATIONAL_COSTS_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_OPERATIONAL_COSTS$BU_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_OPERATIONAL_COSTS$DATE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_OPERATIONAL_COSTS$COST_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_OPERATIONAL_COSTS$TOTAL_COST_HEADER)
		));
		List<FixedOperationalCosts> items = fixedOperationalCostRepository.findAllByOrganizationId(riskModel.getOrganizationId());
		Double totalCosts = fixedOperationalCostRepository.getTotalCosts(riskModel.getOrganizationId());
		for (FixedOperationalCosts item : items) {
			String positionName = item.getUser() != null ? item.getUser().getFullName() : "-";
			String costDateString = item.getCostDate() != null ? dateFormat.format(item.getCostDate()) : "";
			dashboardItem.getGridItems().add(Arrays.asList(
				sI(positionName).applyTextAlign("left"),
				sI(businessUnitService.getBusinessUnitPath(item.getBusinessUnit(), true)).applyTextAlign("left"),
				sI(costDateString).applyTextAlign("right"),
				$I(item.getRate()).round(0),
				$I(item.getTotalCosts()).round(0)
			));
		}

		dashboardItem.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_OPERATIONAL_COSTS$TOTAL_PERSONNEL_COST_HEADER)).applyTextAlign("right").applyColspan(4l).applyHeader(true),
			$I(totalCosts).round(0)
		));

		return dashboardItem;
	}

	/**
	 * Build Variable Operational Costs Dashboard Item
	 *
	 * @param riskModel
	 * @return
	 */
	private DashboardItemDTO buildVariableOperationalCostsDashboardItem(RiskModels riskModel) {
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(104005L, clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_VARIABLE_COSTS$ITEM_NAME));

		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_VARIABLE_COSTS$VARIABLE_COSTS_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_VARIABLE_COSTS$SYSTEM_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_VARIABLE_COSTS$BU_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_VARIABLE_COSTS$DATE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_VARIABLE_COSTS$PERSONNEL_COST_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_VARIABLE_COSTS$EQUIPMENT_COST_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_VARIABLE_COSTS$TOTAL_COST_HEADER)
		));
		List<VariableCosts> items = variableCostRepository.findAllByOrganizationId(riskModel.getOrganizationId());
		Double totalCosts = variableCostRepository.getTotalCosts(riskModel.getOrganizationId());
		for (VariableCosts item : items) {
			String positionName = item.getCostType() != null ? item.getCostType().getName() : "-";
			String systemName = item.getSystem() != null ? item.getSystem().getName() : "-";
			String businessUnitName = item.getSystem() != null ? businessUnitService.getBusinessUnitPath(item.getSystem().getBusinessUnit(), true) : "-";
			String costDateString = item.getCostDate() != null ? dateFormat.format(item.getCostDate()) : "";
			dashboardItem.getGridItems().add(Arrays.asList(
				sI(positionName).applyTextAlign("left"),
				sI(systemName).applyTextAlign("left"),
				sI(businessUnitName).applyTextAlign("left"),
				sI(costDateString).applyTextAlign("right"),
				$I(item.getPersonnelCost()).round(0),
				$I(item.getEquipmentCost()).round(0),
				$I(item.getTotalCosts()).round(0)
			));
		}

		dashboardItem.getGridItems().add(Arrays.asList(
			sI(clientMessage.getMessage(SLCT.DRILLDOWNS$BUDGET$FIXED_VARIABLE_COSTS$TOTAL_PERSONNEL_HEADER)).applyTextAlign("right").applyColspan(6l).applyHeader(true),
			$I(totalCosts).round(0)
		));

		return dashboardItem;
	}

	@Deprecated
	private DashboardItemDTO getAdminSystemOwnerVendorQualDrilldownDashboardItem_PREV(Set<Systems> systemsSet, Map<Systems, Set<Organizations>> associateVendorsMap, VendorType currentType, Map<Organizations, VendorDataSeries> impactDataVendorMap, Map<Organizations, VendorDataSeries> likelihoodDataVendorMap) {
		// Initialize Organization Summary Scores
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(31l, "[System Owner] Vendor Qualitative Scores");
		if (VendorType.Cloud.equals(currentType)) {
			dashboardItem.setName("[System Owner] Cloud Qualitative Scores");
		}

		// Add Top Header
		dashboardItem.addGridHeaders(Arrays.asList("", "Impact", "Likelihood"));
		dashboardItem.getGridItems().get(0).get(1).setColSpan(3l);
		dashboardItem.getGridItems().get(0).get(2).setColSpan(3l);
		dashboardItem.addGridHeaders(Arrays.asList("System", "Score", "Answers Weight", "Total Weight", "Score", "Answers Weight", "Total Weight"));

		for (Systems system : systemsSet) {

			if (associateVendorsMap.containsKey(system)) {
				Set<Organizations> systemVendors = associateVendorsMap.get(system);
				for (Organizations systemVendor : systemVendors) {

					List<Double> itemsData = Arrays.asList(0D, 0D, 0D, 0D, 0D, 0D);

					List<DashboardDataItemDTO>  itemsList = new ArrayList<>();
					itemsList.add(sI(systemVendor.getName()).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor)));

					if (impactDataVendorMap.containsKey(systemVendor)) {
						VendorDataSeries impact = impactDataVendorMap.get(systemVendor);
						itemsData.set(0, impact.getItems().get(0));
						itemsData.set(1, impact.getItems().get(1));
						itemsData.set(2, impact.getItems().get(2));
					}

					if (likelihoodDataVendorMap.containsKey(systemVendor)) {
						VendorDataSeries likelihood = likelihoodDataVendorMap.get(systemVendor);
						itemsData.set(3, likelihood.getItems().get(0));
						itemsData.set(4, likelihood.getItems().get(1));
						itemsData.set(5, likelihood.getItems().get(2));
					}

					itemsList.add(sI(itemsData.get(0)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.IMPACT)));
					itemsList.add(sI(itemsData.get(1)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.IMPACT)));
					itemsList.add(sI(itemsData.get(2)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.IMPACT)));
					itemsList.add(sI(itemsData.get(3)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.LIKELIHOOD)));
					itemsList.add(sI(itemsData.get(4)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.LIKELIHOOD)));
					itemsList.add(sI(itemsData.get(5)).round(2).applyDrilldown(DashboardDataItemDrilldownDTO.of(systemVendor, MetricDomain.LIKELIHOOD)));

					// Apply Cloud Drilldown
					if (currentType.equals(VendorType.Cloud)) {
						for (DashboardDataItemDTO item : itemsList) {
							item.getDrilldown().applyDrillDownType(VendorType.Cloud);
						}
					}

					dashboardItem.getGridItems().add(itemsList);
				}
			}
		}
		return dashboardItem;
	}

	/**
	 * Get Quant metrics System data
	 *
	 * @return Dashboard vendor data
	 */
	@Deprecated
	public DashboardItemDTO getSystemQuantMetricDataDrilldown_DataExfiltration(Long riskModelId, Systems drilldownSystem, QuantMetrics quantMetric) {

		DashboardTableItemDTO dashboard = new DashboardTableItemDTO(1001L, "");
		dashboard.addGridHeaders(Arrays.asList("System", "# of records", "Formula", "Value"));

		RiskModels riskModel = riskModelRepository.findById(quantMetric.getRiskModelId()).get();
		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		Double vendorResult = 0D;
		List<List<DashboardDataItemDTO>> dashboardItemsList = new ArrayList<>();
		// Only "Crown Jewel" assets should be included
		List<Systems> systemsList = Arrays.asList(drilldownSystem);
		if (systemsList != null && systemsList.size() > 0) {
			for (Systems system : systemsList) {
				FormulaResult formulaResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), 0D, 0D);
				vendorResult += formulaResult.getResult();

				List<DashboardDataItemDTO> questionDetails = new ArrayList<>();
				questionDetails.add(sI(system.getName()));
				questionDetails.add(sI(Optional.ofNullable(system.getNumberOfRecProcessed()).orElse(0D)));
				questionDetails.add(sI(formulaResult.getFormula()).applyTextAlign("right"));
				questionDetails.add($I(formulaResult.getResult()).round(0));

				dashboardItemsList.add(questionDetails);
			}
		}
		/*
		dashboardItemsList.sort((o1, o2) -> {
			int value1 = Integer.valueOf(o1.get(5).getValue());
			int value2 = Integer.valueOf(o2.get(5).getValue());

			return value2 - value1;
		});
		*/

		dashboard.getGridItems().addAll(dashboardItemsList);

		Double metricValue = vendorResult;
		List<DashboardDataItemDTO> totlaDetails = new ArrayList<>();
		DashboardDataItemDTO totalCell = new DashboardDataItemDTO("TOTAL:", "right", null);
		totalCell.setColSpan(3l);
		totalCell.setHeader(true);
		totlaDetails.add(totalCell);
		totlaDetails.add($I(metricValue).round(0).applyHeader(true));
		dashboard.getGridItems().add(totlaDetails);

		dashboard.setName(MessageFormat.format("Drill down to {0} metrics score: ${1}", quantMetric.getName(), sRound(metricValue)));
		dashboard.getParameters().put("metricValue", metricValue);

		return dashboard;
	}

	/**
	 * Get Quant metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	@Deprecated
	public DashboardItemDTO getSystemQuantMetricDataDrilldown_BusinessInterruption(Long riskModelId, Systems system, QuantMetrics quantMetric) {

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		Double vendorResult = 0D;
		Map<Long, Processes> systemProcessMap = new HashMap<>();
		processRepository.getListBySystem(system.getId()).stream().forEach(process -> {
			systemProcessMap.put(process.getId(), process);
		});

		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));
		FormulaInfo formulaInfo = new FormulaInfo(formulaItems);

		DashboardTableItemDTO dashboard = new DashboardTableItemDTO(1001L, "");

		if (formulaInfo.isProcessRevenueInvolved()) {
			dashboard.addGridHeaders(Arrays.asList("Formula", "Process", "Process revenue", "Calculation", "Value"));
			List<List<DashboardDataItemDTO>> dashboardItemsList = new ArrayList<>();
			for (Map.Entry<Long, Processes> systemProcess : systemProcessMap.entrySet()) {
				Processes process = systemProcess.getValue();
				FormulaResult formulaResult = calculateFormula(formulaItems, 0D, process.getRevenueProcessed(), 0D);
				vendorResult += formulaResult.getResult();

				List<DashboardDataItemDTO> questionDetails = new ArrayList<>();
				questionDetails.add(sI(quantMetricsService.buildFormula(quantMetric)).applyTextAlign("left"));
				questionDetails.add(sI(process.getName()));
				questionDetails.add($I(process.getRevenueProcessed()).round(0));
				questionDetails.add(sI(formulaResult.getFormula()).applyTextAlign("right"));
				questionDetails.add($I(formulaResult.getResult()).round(0));

				dashboardItemsList.add(questionDetails);
			}

			dashboard.getGridItems().addAll(dashboardItemsList);
		} else {
			dashboard.addGridHeaders(Arrays.asList("Formula", "Organization Revenue", "Systen Number of Records", "Calculation", "Value"));
			FormulaResult formulaResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), 0D, organization.getAverageRevenue());
			vendorResult = formulaResult.getResult();

			dashboard.getGridItems().add(Arrays.asList(
				sI(quantMetricsService.buildFormula(quantMetric)).applyTextAlign("left"),
				$I(organization.getAverageRevenue()).round(0).applyTextAlign("right"),
				dI(system.getNumberOfRecProcessed()).applyTextAlign("right"),
				sI(formulaResult.getFormula()).applyTextAlign("left"),
				$I(vendorResult).round(0).applyTextAlign("right")
			));
		}

		dashboard.getGridItems().add(Arrays.asList(
			sI("TOTAL:").applyTextAlign("right").applyColspan(4l).applyHeader(true),
			$I(vendorResult).round(0).applyHeader(true).applyHeader(true)
		));
		dashboard.setName(MessageFormat.format("Drill down to {0} metrics score: ${1}", quantMetric.getName(), sRound(vendorResult)));
		dashboard.getParameters().put("metricValue", vendorResult);

		return dashboard;
	}

	/**
	 * Get Quant metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	@Deprecated
	public DashboardItemDTO getSystemQuantMetricDataDrilldown_RegulatoryExposure(Long riskModelId, Systems system, QuantMetrics quantMetric) {

		DashboardTableItemDTO dashboard = new DashboardTableItemDTO(1001L, "");
		dashboard.addGridHeaders(Arrays.asList("Process", "Org revenue", "Formula", "Value"));

		if (quantMetric == null) {
			dashboard.addGridItems(Arrays.asList("Regulatory Exposure Metric is not Defined in this Risk Model."));
			dashboard.getGridItems().get(1).get(0).setColSpan(4l);

			return dashboard;
		}

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		Double totalResult = 0D;
		List<List<DashboardDataItemDTO>> dashboardItemsList = new ArrayList<>();
		boolean isPrivacyCalculated = false;
		boolean isCreditCardCalculated = false;

		// Only "Crown Jewel" assets should be included
		/*
		Map<Long, Processes> systemProcessMap = new HashMap<>();
		processRepository.getListBySystem(system.getId()).stream().forEach(process -> {
			systemProcessMap.put(process.getId(), process);
		});
		 */

		FormulaResult formulaResult = new FormulaResult();

		for (DataTypeClassification dataTypeClassification : system.getDataTypeClassifications()) {

			if (
				(
					QuantsDomain.REGULATORY_LOSS.getId().equals(quantMetric.getQuant().getId()) &&
					(DataTypeDomain.PII.getId().equals(dataTypeClassification.getId()) || DataTypeDomain.PRIVACY.getId().equals(dataTypeClassification.getId())) && !isPrivacyCalculated
				)
					||
				(QuantsDomain.GDPR_REGULATORY_EXPOSURE.getId().equals(quantMetric.getQuant().getId()) &&
					(DataTypeDomain.CREDIT_CARD.getId().equals(dataTypeClassification.getId())
						|| (dataTypeClassification.getName().toLowerCase().indexOf(DataTypeDomain.CREDIT_CARD.getName().toLowerCase()) >= 0))
					&& !isPrivacyCalculated))
			{

				formulaResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), 0d, organization.getAverageRevenue());
				formulaResult.setFormula(formulaResult.getFormula());
				totalResult = formulaResult.getResult();
				isPrivacyCalculated = true;
			}
		}

		List<DashboardDataItemDTO> questionDetails = new ArrayList<>();
		questionDetails.add(sI(system.getName()));
		questionDetails.add($I(organization.getAverageRevenue()).round(0));
		questionDetails.add(sI(formulaResult.getFormula()).applyTextAlign("right"));
		questionDetails.add($I(formulaResult.getResult()).round(0));

		dashboardItemsList.add(questionDetails);

		dashboard.getGridItems().addAll(dashboardItemsList);

		Double metricValue = totalResult;
		List<DashboardDataItemDTO> totlaDetails = new ArrayList<>();
		DashboardDataItemDTO totalCell = new DashboardDataItemDTO("TOTAL:", "right", null);
		totalCell.setColSpan(3l);
		totalCell.setHeader(true);
		totlaDetails.add(totalCell);
		totlaDetails.add($I(metricValue).round(0).applyHeader(true));
		dashboard.getGridItems().add(totlaDetails);

		dashboard.setName(MessageFormat.format("Drill down to {0} metrics score: ${1}", quantMetric.getName(), sRound(metricValue)));
		dashboard.getParameters().put("metricValue", metricValue);

		return dashboard;
	}

	/**
	 * Get Quant metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	@Deprecated
	public DashboardItemDTO getSystemQuantMetricDataDrilldown_Quant_Default(Long riskModelId, Systems system, QuantMetrics quantMetric) {

		DashboardTableItemDTO dashboard = new DashboardTableItemDTO(1001L, "");

		if (quantMetric == null) {
			dashboard.addGridItems(Arrays.asList("Regulatory Exposure Metric is not Defined in this Risk Model."));
			dashboard.getGridItems().get(1).get(0).setColSpan(4l);

			return dashboard;
		}

		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		Double totalResult = 0D;
		List<List<DashboardDataItemDTO>> dashboardItemsList = new ArrayList<>();

		// Only "Crown Jewel" assets should be included
		Map<Long, Processes> systemProcessMap = new HashMap<>();
		processRepository.getListBySystem(system.getId()).stream().forEach(process -> {
			systemProcessMap.put(process.getId(), process);
		});

		FormulaResult formulaResult = new FormulaResult();

		QuantMetricsService.FormulaAnalysisResult formulaAnalysis = quantMetricsService.analyzeFormula(quantMetric);

		// Only organization / constant data involved to the formula
		if ((formulaAnalysis.isConstantInvolved || formulaAnalysis.isOrganizationInvolved) && !formulaAnalysis.isSystemsInvolved && !formulaAnalysis.isProcessesInvolved) {
			formulaResult = calculateFormula(formulaItems, 0D, 0D, organization.getAverageRevenue());
			totalResult = formulaResult.getResult();
			dashboard.addGridHeaders(Arrays.asList("System", "Org revenue", "Formula", "Value"));
			dashboard.getGridItems().add(Arrays.asList(
				sI(system.getName()),
				$I(organization.getAverageRevenue()).round(0),
				sI(formulaResult.getFormula()).applyTextAlign("right"),
				$I(formulaResult.getResult()).round(0)
			));

		} else if (formulaAnalysis.isProcessesInvolved) {
			dashboard.addGridHeaders(Arrays.asList("Process", "Org revenue", "Formula", "Value"));
			for (Processes process : processRepository.getListBySystem(system.getId())) {
				formulaResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), process.getRevenueProcessed(), organization.getAverageRevenue());
				totalResult += formulaResult.getResult();
				dashboard.getGridItems().add(Arrays.asList(
					sI(process.getName()),
					$I(organization.getAverageRevenue()).round(0),
					sI(formulaResult.getFormula()).applyTextAlign("right"),
					$I(formulaResult.getResult()).round(0)
				));
			}
		} else if (formulaAnalysis.isSystemsInvolved) {
			dashboard.addGridHeaders(Arrays.asList("System", "Org revenue", "Formula", "Value"));
			formulaResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), 0d, organization.getAverageRevenue());
			totalResult = formulaResult.getResult();
			dashboard.getGridItems().add(Arrays.asList(
				sI(system.getName()),
				$I(organization.getAverageRevenue()).round(0),
				sI(formulaResult.getFormula()).applyTextAlign("right"),
				$I(formulaResult.getResult()).round(0)
			));
		}

		Double metricValue = totalResult;
		List<DashboardDataItemDTO> totlaDetails = new ArrayList<>();
		DashboardDataItemDTO totalCell = new DashboardDataItemDTO("TOTAL:", "right", null);
		totalCell.setColSpan(3l);
		totalCell.setHeader(true);
		totlaDetails.add(totalCell);
		totlaDetails.add($I(metricValue).round(0).applyHeader(true));
		dashboard.getGridItems().add(totlaDetails);

		dashboard.setName(MessageFormat.format("Drill down to {0} metrics score: ${1}", quantMetric.getName(), sRound(metricValue)));
		dashboard.getParameters().put("metricValue", metricValue);

		return dashboard;
	}

	/**
	 * Get Qual metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	@Deprecated
	public List<VendorDataSeries> getQualMetricData(Long riskModelId, MetricDomain metricDomain, List<Organizations> vendorList, List<VendorType> vendorTypes) {

		List<VendorDataSeries> result = new ArrayList<>();

		List<QuestionAnswersForVendor> questionAnswersForVendors;
		if (vendorList == null) {
			questionAnswersForVendors = questionAnswersForVendorRepository.getListByRiskModelAndMetricDomainId(riskModelId, metricDomain.getId(), vendorTypes);
		} else {
			List<Long> vendorIdList = vendorList.stream().mapToLong(Organizations::getId).boxed().collect(Collectors.toList());
			if (vendorIdList.size() == 0) vendorIdList.add(0l); // Add empty ID to prevent SQL exception
			questionAnswersForVendors = questionAnswersForVendorRepository.getListByRiskModelAndMetricDomainIdAndVendors(riskModelId, metricDomain.getId(), vendorIdList, vendorTypes);
		}

		Map<Long, List<QuestionAnswersForVendor>> vendorQuestionMap = new HashMap<>();
		questionAnswersForVendors.stream().forEach(questionAnswersForVendor -> {
			if (!vendorQuestionMap.containsKey(questionAnswersForVendor.getVendor().getId())) {
				vendorQuestionMap.put(questionAnswersForVendor.getVendor().getId(), new ArrayList<>());
			}
			vendorQuestionMap.get(questionAnswersForVendor.getVendor().getId()).add(questionAnswersForVendor);
		});

		for (Map.Entry<Long, List<QuestionAnswersForVendor>> entry : vendorQuestionMap.entrySet()) {

			Double metricValue = Double.valueOf(0);
			Double maxMetricValue = Double.valueOf(0);
			Organizations currentVendor = null;
			List<QuestionAnswersForVendor> questionAnswersForVendor =  entry.getValue();
			for (QuestionAnswersForVendor questionAnswerForVendor : questionAnswersForVendor) {
				if (currentVendor == null) currentVendor = questionAnswerForVendor.getVendor();

				double maxQuestionWeight = 1;
				for (QualitativeQuestionAnswers qualitativeQuestionAnswers : questionAnswerForVendor.getQuestion().getAnswers()) {
					if (qualitativeQuestionAnswers.getAnswerWeight() != null && maxQuestionWeight < qualitativeQuestionAnswers.getAnswerWeight().getValue()) {
						maxQuestionWeight = qualitativeQuestionAnswers.getAnswerWeight().getValue();
					}
				}

				double answerWeight = questionAnswerForVendor.getAnswer() != null && questionAnswerForVendor.getAnswer().getAnswerWeight() != null ? questionAnswerForVendor.getAnswer().getAnswerWeight().getValue() : 0;
				double questionWeight = questionAnswerForVendor.getQuestion() != null && questionAnswerForVendor.getQuestion().getQuestionWeight() != null ? questionAnswerForVendor.getQuestion().getQuestionWeight().getValue() : 0;

				metricValue += answerWeight * questionWeight;
				maxMetricValue += maxQuestionWeight * questionWeight;
			}

			if (currentVendor != null) {
				VendorDataSeries vendorDataSeries = new VendorDataSeries();
				vendorDataSeries.setVendor(currentVendor);
				vendorDataSeries.getItems().add(metricValue / (maxMetricValue != 0 ? maxMetricValue : 1));
				vendorDataSeries.getItems().add(metricValue);
				vendorDataSeries.getItems().add(maxMetricValue);

				result.add(vendorDataSeries);
			}
		}

		return result;
	}

	/**
	 * Get Qual metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	@Deprecated
	public DashboardItemDTO getVendorQualMetricDataDrilldown(Long riskModelId, Organizations vendor, MetricDomain metricDomain, List<VendorType> vendorTypes) {

		Long vendorId = vendor.getId();

		DashboardTableItemDTO dashboard = new DashboardTableItemDTO(1001L, "");
		dashboard.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUALS_DOMAIN$QUALS_DOMAIN_TABLE$QUESTION_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUALS_DOMAIN$QUALS_DOMAIN_TABLE$WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUALS_DOMAIN$QUALS_DOMAIN_TABLE$ANSWER_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUALS_DOMAIN$QUALS_DOMAIN_TABLE$ANSWER_WEIGHT_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUALS_DOMAIN$QUALS_DOMAIN_TABLE$SCORE_HEADER),
			clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUALS_DOMAIN$QUALS_DOMAIN_TABLE$MAX_SCORE_HEADER)
		));

		// List<List<DashboardDataItemDTO>> result = new ArrayList<>();

		List<QuestionAnswersForVendor> questionAnswersForVendors = questionAnswersForVendorRepository.getListByVendorAndRiskModelAndMetricDomainId(riskModelId, vendorId, metricDomain.getId(), vendorTypes);

		Double metricValue = Double.valueOf(0);
		Double maxMetricValue = Double.valueOf(0);
		List<List<DashboardDataItemDTO>> dashboardItemsList = new ArrayList<>();
		for (QuestionAnswersForVendor questionAnswerForVendor : questionAnswersForVendors) {

			double maxQuestionWeight = 1;
			for (QualitativeQuestionAnswers qualitativeQuestionAnswers : questionAnswerForVendor.getQuestion().getAnswers()) {
				if (qualitativeQuestionAnswers.getAnswerWeight() != null && maxQuestionWeight < qualitativeQuestionAnswers.getAnswerWeight().getValue()) {
					maxQuestionWeight = qualitativeQuestionAnswers.getAnswerWeight().getValue();
				}
			}


			double answerWeight = questionAnswerForVendor.getAnswer() != null && questionAnswerForVendor.getAnswer().getAnswerWeight() != null ? questionAnswerForVendor.getAnswer().getAnswerWeight().getValue() : 0;
			double questionWeight = questionAnswerForVendor.getQuestion() != null && questionAnswerForVendor.getQuestion().getQuestionWeight() != null ? questionAnswerForVendor.getQuestion().getQuestionWeight().getValue() : 0;

			Double currMetricValue = answerWeight * questionWeight;
			Double currMaxMetricValue = maxQuestionWeight * questionWeight;
			metricValue += currMetricValue;
			maxMetricValue += currMaxMetricValue;

			List<DashboardDataItemDTO> questionDetails = new ArrayList<>();
			questionDetails.add(sI(questionAnswerForVendor.getQuestion().getQuestion()));
			questionDetails.add(sI(questionWeight).applyTextAlign("right"));
			questionDetails.add(sI(questionAnswerForVendor.getAnswer().getAnswer()));
			questionDetails.add(sI(answerWeight).applyTextAlign("right"));
			questionDetails.add(sI(currMetricValue).round(0));
			questionDetails.add(sI(currMaxMetricValue).round(0));

			dashboardItemsList.add(questionDetails);
		}
		dashboardItemsList.sort((o1, o2) -> {
			int value1 = Integer.valueOf(o1.get(5).getValue());
			int value2 = Integer.valueOf(o2.get(5).getValue());

			return value2 - value1;
		});

		dashboard.getGridItems().addAll(dashboardItemsList);

		Double targetMetricValue = metricValue / (maxMetricValue != 0 ? maxMetricValue : 1);
		String statusColor = getQualCondBGColor(targetMetricValue);
		List<DashboardDataItemDTO> questionDetails = new ArrayList<>();
		DashboardDataItemDTO totalCell = new DashboardDataItemDTO(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$TOTAL), "right", null);
		totalCell.setColSpan(4l);
		totalCell.setHeader(true);
		questionDetails.add(totalCell);
		questionDetails.add(sI(metricValue).round(0).applyBackgroundColor(statusColor).applyHeader(true));
		questionDetails.add(sI(maxMetricValue).round(0).applyHeader(true));
		dashboard.getGridItems().add(questionDetails);

		dashboard.setName(MessageFormat.format(clientMessage.getMessage(SLCT.DRILLDOWNS$VENDOR_QUALS_DOMAIN$QUALS_DOMAIN_TABLE$ITEM_NAME), metricDomain.getCode(), sRound(targetMetricValue)));
		dashboard.getParameters().put("metricValue", metricValue);
		dashboard.getParameters().put("maxMetricValue", maxMetricValue);
		dashboard.getParameters().put("targetMetricValue", targetMetricValue);

		return dashboard;
	}

	/**
	 * Get Quant metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	public List<VendorDataSeries> buildQuantMetricData(Long riskModelId) {
		return buildQuantMetricData(riskModelId, null, null);
	}

	/**
	 * Get Quant metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	public List<VendorDataSeries> buildQuantMetricData(Long riskModelId, List<Organizations> vendorList, Set<Systems> privacySystemsSet) {

		Set<Organizations> vendorsSet = vendorList != null ? vendorList.stream().collect(Collectors.toSet()) : null;
		List<VendorDataSeries> dataExfiltration = getQuantMetricData(riskModelId, QuantsDomain.DATA_EXFILTRATION, vendorsSet, privacySystemsSet);
		List<VendorDataSeries> businessInterruption = getQuantMetricData(riskModelId, QuantsDomain.BUSINESS_INTERRUPTION, vendorsSet, privacySystemsSet);
		Map<Long, VendorDataSeries> dataExfiltrationMap = new HashMap<>();
		Map<Long, VendorDataSeries> businessInterruptionMap = new HashMap<>();
		Map<Long, VendorDataSeries> targetQuantsMap = new HashMap<>();
		verifyVendorDataSeries(dataExfiltration, dataExfiltrationMap, targetQuantsMap);
		verifyVendorDataSeries(businessInterruption, businessInterruptionMap, targetQuantsMap);
		synchronizeVendorDataSeries(dataExfiltrationMap, targetQuantsMap);
		synchronizeVendorDataSeries(businessInterruptionMap, targetQuantsMap);

		/**
		 * Check Regulatory Loss Map
		 */
		if (quantMetricsService.isQuanDefined(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE)) {
			Map<Long, VendorDataSeries> regulatoryLossMap = new HashMap<>();
			List<VendorDataSeries> regulatoryLoss = getQuantMetricData(riskModelId, QuantsDomain.GDPR_REGULATORY_EXPOSURE, vendorsSet, privacySystemsSet);
			verifyVendorDataSeries(regulatoryLoss, regulatoryLossMap, targetQuantsMap);
			synchronizeVendorDataSeries(regulatoryLossMap, targetQuantsMap);
		}

		List<VendorDataSeries> quantVendorScores = calculateTargetVendorDataSeries(targetQuantsMap);

		return quantVendorScores;
	}

	/**
	 * Get Quant metrics Vendor data
	 *
	 * @return Dashboard vendor data
	 */
	public List<VendorDataSeries> getQuantMetricData(Long riskModelId, QuantsDomain metricDomain, Set<Organizations> vendorSet, Set<Systems> systemSet) {
		List<VendorDataSeries> result = new ArrayList<>();

		List<QuantMetrics> quantsList = quantMetricsRepository.getListByRiskModelIdAndQuantId(riskModelId, metricDomain.getId());
		if (quantsList.size() > 0) {

			QuantMetrics quantMetric = quantsList.get(0);
			if (metricDomain.equals(QuantsDomain.DATA_EXFILTRATION)) {
				result = getQuant_DataExfiltration(quantMetric, vendorSet, systemSet);
			} else if (metricDomain.equals(QuantsDomain.BUSINESS_INTERRUPTION)) {
				result = getQuant_BusinessInterruption(quantMetric, vendorSet, systemSet);
			} else if (metricDomain.equals(QuantsDomain.REGULATORY_LOSS)) {
				result = getQuant_RegulatoryLoss(quantMetric, vendorSet, systemSet);
			} else if (metricDomain.equals(QuantsDomain.GDPR_REGULATORY_EXPOSURE)) {
				result = getQuant_RegulatoryLoss(quantMetric, vendorSet, systemSet);
			}
		}

		return result;
	}

	public void verifyVendorDataSeries(List<VendorDataSeries> series, Map<Long, VendorDataSeries> mapSeries, Map<Long, VendorDataSeries> targetSeries) {
		series.stream().forEach(vendorDataSeries -> {
			mapSeries.put(vendorDataSeries.getVendor().getId(), vendorDataSeries);
			if (!targetSeries.containsKey(vendorDataSeries.getVendor().getId())) {
				VendorDataSeries tmpVendorDataSeries = new VendorDataSeries();
				tmpVendorDataSeries.setVendor(vendorDataSeries.getVendor());
				tmpVendorDataSeries.setItems(new ArrayList<>());

				targetSeries.put(vendorDataSeries.getVendor().getId(), tmpVendorDataSeries);
			}
		});
	}

	public void synchronizeVendorDataSeries(Map<Long, VendorDataSeries> mapSeries, Map<Long, VendorDataSeries> targetSeries) {
		for (Map.Entry<Long, VendorDataSeries> syncSeries : targetSeries.entrySet()) {
			VendorDataSeries dataSeries = syncSeries.getValue();
			Long vendorId = syncSeries.getKey();
			if (mapSeries.containsKey(vendorId)) {
				dataSeries.getItems().add(mapSeries.get(vendorId).getItems().get(0));
			} else {
				dataSeries.getItems().add(0D);
			}
		}
	}

	public List<VendorDataSeries> calculateTargetVendorDataSeries(Map<Long, VendorDataSeries> targetSeries) {
		List<VendorDataSeries> result = new ArrayList<>();
		for (Map.Entry<Long, VendorDataSeries> syncSeries : targetSeries.entrySet()) {
			VendorDataSeries dataSeries = syncSeries.getValue();
			Double totalParameter = 0D;
			for (Double item : dataSeries.getItems()) {
				totalParameter += item;
			}
			dataSeries.getItems().add(totalParameter);

			result.add(dataSeries);
		}
		return result;
	}

	public List<VendorDataSeries> getQuant_DataExfiltration(QuantMetrics quantMetric, Set<Organizations> vendorSet, Set<Systems> systemSet) {
		List<VendorDataSeries> result = new ArrayList<>();

		RiskModels riskModel = riskModelRepository.findById(quantMetric.getRiskModelId()).get();
		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		for (AssociateVendors associateVendor : associateVendors) {
			Double vendorResult = 0D;

			// Block vendors not defined in vendors set
			if (vendorSet != null && !vendorSet.contains(associateVendor.getVendor())) {
				continue;
			}

			// Only "Crown Jewel" assets should be included
			List<Systems> systemsList = associateVendorRepository.getSystemsListForAssociateVendorAndDataAssetClass(associateVendor.getId(), AssetClass.CROWN_JEWEL.getId());
			if (systemsList != null && systemsList.size() > 0) {
				for (Systems system : systemsList) {
					Double formulaResult = calculateFormula(formulaItems, system.getNumberOfRecProcessed(), 0D, 0D).getResult();
					vendorResult += formulaResult;
				}

				VendorDataSeries vendorDataSeries = new VendorDataSeries();
				result.add(vendorDataSeries);
				vendorDataSeries.setVendor(associateVendor.getVendor());
				vendorDataSeries.setItems(Arrays.asList(vendorResult));
			}
		}

		log.info(quantMetric.getName());

		return result;
	}

	public List<VendorDataSeries> getQuant_BusinessInterruption(QuantMetrics quantMetric, Set<Organizations> vendorSet, Set<Systems> systemSet) {
		List<VendorDataSeries> result = new ArrayList<>();

		RiskModels riskModel = riskModelRepository.findById(quantMetric.getRiskModelId()).get();
		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		for (AssociateVendors associateVendor : associateVendors) {
			Double vendorResult = 0D;

			// Block vendors not defined in vendors set
			if (vendorSet != null && !vendorSet.contains(associateVendor.getVendor())) {
				continue;
			}

			// Get MAP of Processes for system
			// Only "Crown Jewel" assets should be included
			List<Systems> systemsList = associateVendorRepository.getSystemsListForAssociateVendorAndDataAssetClass(associateVendor.getId(), AssetClass.CROWN_JEWEL.getId());
			if (systemsList != null && systemsList.size() > 0) {
				Map<Long, Processes> systemProcessMap = new HashMap<>();
				for (Systems system : systemsList) {
					processRepository.getListBySystem(system.getId()).stream().forEach(process -> {
						systemProcessMap.put(process.getId(), process);
					});
				}

				for (Map.Entry<Long, Processes> systemProcess : systemProcessMap.entrySet()) {
					Processes process = systemProcess.getValue();
					Double formulaResult = calculateFormula(formulaItems, 0D, process.getRevenueProcessed(), 0D).getResult();
					vendorResult += formulaResult;
				}

				VendorDataSeries vendorDataSeries = new VendorDataSeries();
				result.add(vendorDataSeries);
				vendorDataSeries.setVendor(associateVendor.getVendor());
				vendorDataSeries.setItems(Arrays.asList(vendorResult));
			}
		}

		log.info(quantMetric.getName());

		return result;
	}

	public List<VendorDataSeries> getQuant_RegulatoryLoss(QuantMetrics quantMetric, Set<Organizations> vendorSet, Set<Systems> systemSet) {
		List<VendorDataSeries> result = new ArrayList<>();

		RiskModels riskModel = riskModelRepository.findById(quantMetric.getRiskModelId()).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		List<MetricFormulaItems> formulaItems = quantMetric.getMetricFormulaItems().stream().collect(Collectors.toList());
		formulaItems.sort((o1, o2) -> (o1.getOrdinal().intValue() - o2.getOrdinal().intValue()));

		List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		Map<Organizations, Set<Processes>> vendorProcessMap = new HashMap<>();
		for (AssociateVendors associateVendor : associateVendors) {

			// Block vendors not defined in vendors set
			if (vendorSet != null && !vendorSet.contains(associateVendor.getVendor())) {
				continue;
			}

			// Get MAP of Processes for system
			// Only "Crown Jewel" assets should be included
			List<Systems> systemsList = associateVendorRepository.getSystemsListForAssociateVendorAndDataAssetClass(associateVendor.getId(), AssetClass.CROWN_JEWEL.getId());
			if (systemsList != null && systemsList.size() > 0) {
				for (Systems system : systemsList) {
					processRepository.getListBySystem(system.getId()).stream().forEach(process -> {
						if (!vendorProcessMap.containsKey(associateVendor.getVendor())) {
							vendorProcessMap.put(associateVendor.getVendor(), new HashSet<>());
						}
						vendorProcessMap.get(associateVendor.getVendor()).add(process);
					});
				}
			}
		}

		for (Map.Entry<Organizations, Set<Processes>> vendorProcesses : vendorProcessMap.entrySet()) {
			Organizations vendor = vendorProcesses.getKey();
			Double vendorResult = 0D;

			boolean isPrivacyCalculated = false;
			boolean isCreditCardCalculated = false;
			for (Processes process : vendorProcesses.getValue()) {
				for (DataTypeClassification dataTypeClassification : process.getDataTypeClassifications()) {
					if (dataTypeClassification.getName().toLowerCase().indexOf("privacy") != -1 && !isPrivacyCalculated) {
						// Double privacy = 0.04 * organization.getAverageRevenue();
						Double privacy = calculateFormula(formulaItems, 0D, process.getRevenueProcessed(), organization.getAverageRevenue()).getResult();
						vendorResult += (privacy > 25000000) ? privacy : 25000000;
						isPrivacyCalculated = true;
					}

					if (dataTypeClassification.getName().toLowerCase().indexOf("credit card") != -1 && !isCreditCardCalculated) {
						vendorResult += 100000 * 12;
						isCreditCardCalculated = true;
					}
				}
			}

			VendorDataSeries vendorDataSeries = new VendorDataSeries();
			result.add(vendorDataSeries);
			vendorDataSeries.setVendor(vendor);
			vendorDataSeries.setItems(Arrays.asList(vendorResult));
		}

		log.info(quantMetric.getName());

		return result;
	}

}
