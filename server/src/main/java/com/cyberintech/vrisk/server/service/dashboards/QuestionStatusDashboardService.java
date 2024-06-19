package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.ApplicationProperties;
import com.cyberintech.vrisk.server.service.*;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Question Status Dashboard Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-09-02
 */
@Service
@Slf4j
public class QuestionStatusDashboardService extends DashboardServiceBase {

	public static final Long SYSTEM_STATUSES = 35102L;
	public static final Long VENDOR_STATUSES = 35202L;
	public static final Long SYSTEM_QUESTION_ANSWERS = 35700L;
	public static final Long VENDOR_QUESTION_ANSWERS = 35800L;

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private DataTypeClassificationRepository dataTypeClassificationRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private QuestionAnswersForVendorRepository questionAnswersForVendorRepository;

	@Autowired
	private QuestionAnswersForSystemRepository questionAnswersForSystemRepository;

	@Autowired
	private QualitativeQuestionRepository qualitativeQuestionRepository;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RiskModelItemCommentsRepository riskModelItemCommentsRepository;

	@Autowired
	private RiskModelRepository riskModelRepository;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;


	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getQuestionStatusDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {

		// Check Active Filters
		DashboardItemFilterDTO dashboardItemFilter;
		if ((dashboardState != null) && (dashboardItemFilter = dashboardState.getFilterByItemById(SYSTEM_STATUSES)) != null && dashboardItemFilter.hasValues()) {
			return buildSystemQuestionStatusSearchDashboard(riskModelId, dashboardState);
		} else if ((dashboardState != null) && (dashboardItemFilter = dashboardState.getFilterByItemById(VENDOR_STATUSES)) != null && dashboardItemFilter.hasValues()) {
			return buildVendorQuestionStatusSearchDashboard(riskModelId, dashboardState);
		}

		return buildQuestionStatusDashboardDetails(riskModelId, dashboardState);
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getVendorQuestionStatusDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {
		// Check Active Filters
		DashboardItemFilterDTO dashboardItemFilter;
		if ((dashboardState != null) && (dashboardItemFilter = dashboardState.getFilterByItemById(VENDOR_STATUSES)) != null && dashboardItemFilter.hasValues()) {
			return buildVendorQuestionStatusSearchDashboard(riskModelId, dashboardState);
		}

		return buildVendorQuestionStatusDashboardDetails(riskModelId, dashboardState);
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getFFIECCATCyberMaturityDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {
		// Check Active Filters
		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.FFIEC_CAT_CYBER_MATURITY, clientMessage.getMessage("FFIEC CAT Cyber Maturity"), "", DashboardType.Admin);
		DashboardSectionDTO section = new DashboardSectionDTO(35200L, clientMessage.getMessage("FFIEC CAT Cyber Maturity"), "");
		dashboard.getSections().add(section);

		DashboardItemDTO dashboardItem = new DashboardItemDTO(16538001L, "FFIEC CAT Cyber Maturity", "", DashboardItemType.FFIECCatCyberMaturity);
		section.getDashboardItems().add(dashboardItem);

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO getFFIECCATInherentRiskDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {
		// Check Active Filters
		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.FFIEC_CAT_INHERENT_RISK, clientMessage.getMessage("FFIEC CAT Inherent Risk"), "", DashboardType.Admin);
		DashboardSectionDTO section = new DashboardSectionDTO(35200L, clientMessage.getMessage("FFIEC CAT Inherent Risk"), "");
		dashboard.getSections().add(section);

		DashboardItemDTO dashboardItem = new DashboardItemDTO(16537001L, "FFIEC CAT Inherent Risk", "", DashboardItemType.FFIECCatInherentRisk);
		section.getDashboardItems().add(dashboardItem);

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO buildQuestionStatusDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {

		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_QUESTION_STATUS, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$DESCRIPTION), DashboardType.Admin);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.SET_UP_DASHBOARD(clientMessage).add("CYBER_RISK_SCORING_STATUS", "DASHBOARDS$QUESTION_STATUS$NAME", "/private/dashboards/35");

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<String> roles = Arrays.asList(RoleType.SYSTEM_OWNER.role(), RoleType.VENDOR_OWNER.role());
		List<Users> ownersList = userRepository.filterUsersByOrganizationAndNameAndRoles(riskModel.getOrganizationId(), "", roles, Arrays.asList(0L), PageRequest.of(0, 1000000));
		List<Systems> systemList = systemRepository.getAllByOrganization(riskModel.getOrganizationId());
		List<Organizations> vendorList = organizationRepository.getListForRootOrganization(riskModel.getOrganizationId(), OrganizationType.Vendor);
		// List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		Set<Systems> systemsSet = new LinkedHashSet<>(systemList);
		Set<Organizations> vendorsSet = new LinkedHashSet<>(vendorList);
		Set<Organizations> cloudVendorsSet = vendorList.stream().filter(organizations -> Boolean.TRUE.equals(organizations.getIsCloudVendor())).collect(Collectors.toCollection(LinkedHashSet::new));
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

		// Initialize Organization Summary Scores
		DashboardItemDTO dashboardItem1DowloadLink = null;
		// if (permissionService.checkCurrentUserPermission(PermissionType.DASHBOARD_QUESTION_STATUS_REPORT)) {
			dashboardItem1DowloadLink = new DashboardItemDTO(35101L, clientMessage.getMessage(SLCT.DASHBOARD_COMPONENTS$BUTTON$DOWNLOAD$NAME), "", DashboardItemType.Link);
			String authorization = request.getHeader("Authorization");
			String apiUrl = applicationProperties.getApiUrl();
			String tokenId = authorization;
			if (authorization != null && authorization.toLowerCase().contains("bearer")) {
				tokenId = authorization.substring("bearer".length() + 1);
			}
			String downloadUrl = apiUrl + "/api/dashboards/download/questions-status?token=" + tokenId.trim() + "&riskModelId=" + riskModelId.toString();
			dashboardItem1DowloadLink.addParameter("href", downloadUrl);
			dashboardItem1DowloadLink.addParameter("isExternal", "true");
			dashboardItem1DowloadLink.addParameter("textAlign", "right");
		// }

		// Build Dashboard Data
		List<VendorType> systemTypes = Arrays.asList(VendorType.Both, VendorType.System);
		List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor);
		List<VendorType> cloudTypes = Arrays.asList(VendorType.Both, VendorType.Cloud);

		List<QualitativeQuestions> allSystemQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndType(riskModelId, systemTypes);
		long totalSystemQuestions = calculateQuestionsCount(allSystemQuestions);
		List<QuestionAnswersForSystem> allSystemQuestionsAnswersList = questionAnswersForSystemRepository.getListByRiskModelAndScoringTypes(riskModelId, systemTypes);
		Map<Systems, List<QuestionAnswersForSystem>> allSystemQuestionsAnswersMap = allSystemQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForSystem::getSystem));

		List<QualitativeQuestions> allVendorQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndType(riskModelId, vendorTypes);
		long totalVendorQuestions = calculateQuestionsCount(allVendorQuestions);
		List<QuestionAnswersForVendor> allVendorQuestionsAnswersList = questionAnswersForVendorRepository.getListByRiskModelAndVendorTypes(riskModelId, Arrays.asList(VendorType.Vendor, VendorType.Both)).stream().filter(item -> item.getAnswer() != null).toList();
		Map<Organizations, List<QuestionAnswersForVendor>> allVendorQuestionsAnswersMap = allVendorQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForVendor::getVendor));

		List<QualitativeQuestions> allCloudQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndType(riskModelId, cloudTypes);
		long totalCloudQuestions = calculateQuestionsCount(allCloudQuestions);
		List<QuestionAnswersForVendor> allCloudQuestionsAnswersList = questionAnswersForVendorRepository.getListByRiskModelAndVendorTypes(riskModelId, Arrays.asList(VendorType.Cloud)).stream().filter(item -> item.getAnswer() != null).toList();
		Map<Organizations, List<QuestionAnswersForVendor>> allCloudQuestionsAnswersMap = allCloudQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForVendor::getVendor));


		DataTypeClassification piiDataType = dataTypeClassificationRepository.findById(DataTypeDomain.PII.getId()).orElse(null);


		/*
		// ========== ========== ========== ========== ========== 4TEST ========== ========== ========== ========== ========== //
		DashboardItemDTO dashboardItemTest = new DashboardItemDTO(35202L, "System Question: Test");
		dashboardItemTest.addGridHeaders(Arrays.asList(
			"Person",
			"System Name",
			"Business Unit",
			"Answer",
			"Answer Weight"
		));
		section1.getDashboardItems().add(dashboardItemTest);
		dashboardItemTest.setDashboardItemFilter(new DashboardItemFilterDTO(35011L, "Vendor Questions Filter", null, DashboardFilterType.VENDOR_QUESTION));
		dashboardItemTest.getGridItems().add(Arrays.asList(sI("a"), sI("b"), sI("c"), sI("d"), sI("e")));
		// ========== ========== ========== ========== ========== END 4TEST ========== ========== ========== ========== ========== //
		*/

		// DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(SYSTEM_STATUSES, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$SYSTEM_STATUS$ITEM_NAME));
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(SYSTEM_STATUSES);
		dashboardItem.getGridHeaders().add(
			Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$SYSTEM_STATUS$PERSON_HEADER), 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$SYSTEM_STATUS$SYSTEM_NAME_HEADER), 1L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$SYSTEM_STATUS$BUSINESS_UNIT_HEADER), 2L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$SYSTEM_STATUS$SYSTEM_QUESTIONNAIRE_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$SYSTEM_STATUS$DATA_EXFILTRATION_HEADER), 5L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$SYSTEM_STATUS$BUSINESS_INTERRUPTION_HEADER), 6L)
			)
		);

		dashboardItem.setDashboardItemFilter(new DashboardItemFilterDTO(3501L, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$SYSTEM_STATUS$FILTER$ITEM_NAME), null, DashboardFilterType.SYSTEM_QUESTION));

		// Create Initial Sections
		if (totalSystemQuestions > 0) {
			DashboardSectionDTO section1 = new DashboardSectionDTO(35100L, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$ITEM_DESCRIPTION));
			dashboard.getSections().add(section1);
			if (dashboardItem1DowloadLink != null) {
				section1.getDashboardItems().add(dashboardItem1DowloadLink);
			}
			section1.setBreadcrumbs(breadcrumbsTop.extend("CYBER_RISK_SCORING_STATUS_1", SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$ITEM_NAME, "").getBreadcrumbs());
			section1.getDashboardItems().add(dashboardItem);

			int i = 0;
			for (Systems system : systemsSet) {
				List<DashboardDataItemDTO>  itemsList = new ArrayList<>();

				String ownerName = system.getOwner() != null ? system.getOwner().getFullName() : "";
				DashboardDataItemDTO ownerCell = sI(ownerName);
				if (system.getOwner() != null) {
					ownerCell.applyDrilldown(DashboardDataItemDrilldownDTO.of(system.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null));
				}
				itemsList.add(ownerCell);
				itemsList.add(sI(system.getName()));
				if (system.getOwner() != null && system.getOwner().getBusinessUnit() != null) {
					itemsList.add(sI(businessUnitService.getBusinessUnitPath(system.getOwner().getBusinessUnit(), true)).applyDrilldown(DashboardDataItemDrilldownDTO.of(system.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null)));
				} else {
					itemsList.add(sI(""));
				}

				if (Boolean.TRUE.equals(system.getIsEtl())) {
					itemsList.add(sI("N/A").applyTextAlign("center"));
					itemsList.add(sI("N/A").applyTextAlign("center"));
				} else {
					long answersCount = allSystemQuestionsAnswersMap.containsKey(system) ? allSystemQuestionsAnswersMap.get(system).size() : 0;
					// System Questionnaire Status
					if (answersCount == 0) {
						itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED)).applyColor("#ff0000")
							.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "all"))));
					} else if (answersCount >= totalSystemQuestions) {
						itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE)).applyColor("#00ff00")
							.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "all"))));
					} else {
						itemsList.add(sI(MessageFormat.format("{0}/{1}", answersCount, totalSystemQuestions)).applyTextAlign("right")
							.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), "all"))));
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
					itemsList.add(sI("N/A"));
				}

				// Business Interruption Question
				itemsList.add(sI("N/A"));

				dashboardItem.getGridItems().add(itemsList);
			}
		}

		// Initialize Organization Summary Scores
		// DashboardDataGridItemDTO dashboardItem2 = new DashboardDataGridItemDTO(VENDOR_STATUSES, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$ITEM_NAME));
		if (totalVendorQuestions > 0 || totalCloudQuestions > 0) {
			DashboardDataGridItemDTO dashboardItem2 = new DashboardDataGridItemDTO(VENDOR_STATUSES);
			List<DashboardDataItemDTO> headersList = new ArrayList<>(Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$PERSON_HEADER), 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_NAME_HEADER), 1L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$BUSINESS_UNIT_HEADER), 2L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_QUESTIONNAIRE_HEADER), null)
			));
			if (totalCloudQuestions > 0) {
				headersList.add(DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$CLOUD_QUESTIONNAIRE_HEADER), null));
			}

			dashboardItem2.getGridHeaders().add(headersList);
			dashboardItem2.setDashboardItemFilter(new DashboardItemFilterDTO(3501L, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$FILTER$ITEM_NAME), null, DashboardFilterType.VENDOR_QUESTION));

			DashboardSectionDTO section2 = new DashboardSectionDTO(35200L, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$ITEM_DESCRIPTION));
			dashboard.getSections().add(section2);
			if (dashboardItem1DowloadLink != null) {
				section2.getDashboardItems().add(dashboardItem1DowloadLink);
			}
			section2.setBreadcrumbs(breadcrumbsTop.extend("CYBER_RISK_SCORING_STATUS_2", SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$ITEM_NAME, "").getBreadcrumbs());
			section2.getDashboardItems().add(dashboardItem2);

			int i = 0;
			for (Organizations vendor : vendorsSet) {
				List<DashboardDataItemDTO>  itemsList = new ArrayList<>();

				String ownerName = vendor.getOwner() != null ? vendor.getOwner().getFullName() : "";
				DashboardDataItemDTO ownerCell = sI(ownerName);
				if (vendor.getOwner() != null) {
					ownerCell.applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null));
				}
				itemsList.add(ownerCell);
				itemsList.add(sI(vendor.getName()));
				if (vendor.getOwner() != null && vendor.getOwner().getBusinessUnit() != null) {
					itemsList.add(sI(businessUnitService.getBusinessUnitPath(vendor.getOwner().getBusinessUnit(), true)).applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null)));
				} else {
					itemsList.add(sI(""));
				}

				// Vendor Questionnaire Status
				long answersCount = allVendorQuestionsAnswersMap.containsKey(vendor) ? allVendorQuestionsAnswersMap.get(vendor).size() : 0;
				// System Questionnaire Status
				if (answersCount == 0) {
					itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED)).applyColor("#ff0000")
						.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildVendorRiskQualQuestionsUrl(vendor.getId(), "all"))));
				} else if (answersCount >= totalVendorQuestions) {
					itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE)).applyColor("#00ff00")
						.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildVendorRiskQualQuestionsUrl(vendor.getId(), "all"))));
				} else {
					itemsList.add(sI(MessageFormat.format("{0}/{1}", answersCount, totalVendorQuestions)).applyTextAlign("right")
						.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildVendorRiskQualQuestionsUrl(vendor.getId(), "all"))));
				}

				// Cloud Questionnaire Status
				if (totalCloudQuestions > 0) {
					if (Boolean.TRUE.equals(vendor.getIsCloudVendor())) {
						long cloudAnswersCount = allCloudQuestionsAnswersMap.containsKey(vendor) ? allCloudQuestionsAnswersMap.get(vendor).size() : 0;
						// System Questionnaire Status
						if (cloudAnswersCount == 0) {
							itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED)).applyColor("#ff0000")
								.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildCloudRiskQualQuestionsUrl(vendor.getId(), "impact"))));
						} else if (cloudAnswersCount >= totalCloudQuestions) {
							itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE)).applyColor("#00ff00")
								.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildCloudRiskQualQuestionsUrl(vendor.getId(), "impact"))));
						} else {
							itemsList.add(sI(MessageFormat.format("{0}/{1}", cloudAnswersCount, totalCloudQuestions)).applyTextAlign("right")
								.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildCloudRiskQualQuestionsUrl(vendor.getId(), "impact"))));
						}
					} else {
						itemsList.add(sI("N/A").applyColspan(2L).applyTextAlign("center"));
					}
				}

				i++;
				dashboardItem2.getGridItems().add(itemsList);
			}
		}


		DashboardSectionDTO section3 = new DashboardSectionDTO(3303L, clientMessage.getMessage(SLCT.DASHBOARDS$SET_UP_DASHBOARD$CYBER_RISK_SCORING_STATUS_BUSINESS_UNITS), "Business Units");

		section3.setBreadcrumbs(breadcrumbsTop.extend("CYBER_RISK_SCORING_STATUS_3", "DASHBOARDS$SET_UP_DASHBOARD$CYBER_RISK_SCORING_STATUS_BUSINESS_UNITS", "").getBreadcrumbs());
		if (permissionService.checkCurrentUserPermission(PermissionType.DASHBOARD_BUSINESS_UNIT_QUESTION_STATUS)) {
			dashboard.getSections().add(section3);
		}
		// DashboardDataGridItemDTO dashboardItem3 = new DashboardDataGridItemDTO(3303L, clientMessage.getMessage(SLCT.DASHBOARDS$SET_UP_DASHBOARD$CYBER_RISK_SCORING_STATUS_BUSINESS_UNITS));
		DashboardDataGridItemDTO dashboardItem3 = new DashboardDataGridItemDTO(3303L);
		dashboardItem3.getGridHeaders().add(
			Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_STATUSES$BUSINESS_UNIT_HEADER), 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$TOTAL_STATUS), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_STATUS$SYSTEM_STATUS$SYSTEM_QUESTIONNAIRE_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_QUESTIONNAIRE_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$CLOUD_QUESTIONNAIRE_HEADER), null)
			)
		);
		section3.getDashboardItems().add(dashboardItem3);

		Map<BusinessUnits, List<QuestionAnswersForSystem>> impactQuestionsAnswersMap4BU = getMapOfSystemAnswersByBusinessUnitAndSystem(allSystemQuestionsAnswersList);
		Map<BusinessUnits, List<QuestionAnswersForVendor>> impactVendorQuestionsAnswersMap4BU = getMapOfVendorAnswersByBusinessUnit(allVendorQuestionsAnswersList);
		Map<BusinessUnits, List<QuestionAnswersForVendor>> impactCloudQuestionsAnswersMap4BU = getMapOfVendorAnswersByBusinessUnit(allCloudQuestionsAnswersList);

		Map<BusinessUnits, List<Systems>> businessUnitSystemsMap = systemsSet.stream().filter(o -> o.getBusinessUnit() != null).collect(Collectors.groupingBy(o -> o.getBusinessUnit()));
		Map<BusinessUnits, List<Organizations>> businessUnitVendorsMap = vendorsSet.stream().filter(o -> o.getOwner() != null && o.getOwner().getBusinessUnit() != null).collect(Collectors.groupingBy(o -> o.getOwner().getBusinessUnit()));
		Map<BusinessUnits, List<Organizations>> businessUnitCloudVendorsMap = cloudVendorsSet.stream().filter(o -> o.getOwner() != null && o.getOwner().getBusinessUnit() != null).collect(Collectors.groupingBy(o -> o.getOwner().getBusinessUnit()));

		Set<BusinessUnits> allInvolvedBusinessUnits = new HashSet<>();
		allInvolvedBusinessUnits.addAll(businessUnitSystemsMap.keySet());
		allInvolvedBusinessUnits.addAll(businessUnitVendorsMap.keySet());

		for (BusinessUnits businessUnit : allInvolvedBusinessUnits) {
			List<DashboardDataItemDTO>  itemsList = new ArrayList<>();
			itemsList.add(sI(businessUnitService.getBusinessUnitPath(businessUnit, true))); // .applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null)));

			itemsList.add(sI("-").applyTextAlign("center"));
			itemsList.add(sI("-").applyTextAlign("center"));

			double totalBUImpactQuestionsCount = 0d;
			double totalBULikelihoodQuestionsCount = 0d;
			double answeredBUImpactQuestionsCount = 0d;
			double answeredBULikelihoodQuestionsCount = 0d;

			if (businessUnitSystemsMap.containsKey(businessUnit)) {
				int businessUnitSystemsCount = businessUnitSystemsMap.get(businessUnit).size();
				long totalBUImpactQuestions = (businessUnitSystemsCount > 0 && totalSystemQuestions > 0) ? businessUnitSystemsCount * totalSystemQuestions : 1;
				int buImpactQuestionAnswers = (impactQuestionsAnswersMap4BU.containsKey(businessUnit)) ? impactQuestionsAnswersMap4BU.get(businessUnit).size() : 0;
				Double buImpactQuestionsAnsweredPercent = Double.valueOf(buImpactQuestionAnswers) / Double.valueOf(totalBUImpactQuestions) * 100d;
				if (buImpactQuestionsAnsweredPercent >= 99.99d) {
					itemsList.add($I(100d, "%").round(2).applyTextAlign("right").applyColor("#00ff00"));
				} else {
					itemsList.add($I(buImpactQuestionsAnsweredPercent, "%").round(2).applyTextAlign("right"));
				}

				totalBUImpactQuestionsCount += totalBUImpactQuestions;
				answeredBUImpactQuestionsCount += buImpactQuestionAnswers;
			} else {
				itemsList.add(sI("N/A").applyColspan(2L).applyTextAlign("center"));
			}

			if (businessUnitVendorsMap.containsKey(businessUnit)) {
				int businessUnitVendorsCount = businessUnitVendorsMap.get(businessUnit).size();
				long totalBUVendorImpactQuestions = (businessUnitVendorsCount > 0 && totalVendorQuestions > 0) ? businessUnitVendorsCount * totalVendorQuestions : 1;
				int buImpactVendorQuestionAnswers = (impactVendorQuestionsAnswersMap4BU.containsKey(businessUnit)) ? impactVendorQuestionsAnswersMap4BU.get(businessUnit).size() : 0;
				Double buImpactVendorQuestionsAnsweredPercent = Double.valueOf(buImpactVendorQuestionAnswers) / Double.valueOf(totalBUVendorImpactQuestions) * 100d;
				if (buImpactVendorQuestionsAnsweredPercent >= 99.99d) {
					itemsList.add($I(100d, "%").round(2).applyTextAlign("right").applyColor("#00ff00"));
				} else {
					itemsList.add($I(buImpactVendorQuestionsAnsweredPercent, "%").round(2).applyTextAlign("right"));
				}

				totalBUImpactQuestionsCount += totalBUVendorImpactQuestions;
				answeredBUImpactQuestionsCount += buImpactVendorQuestionAnswers;
			} else {
				itemsList.add(sI("N/A").applyColspan(2L).applyTextAlign("center"));
			}

			if (businessUnitCloudVendorsMap.containsKey(businessUnit)) {
				int businessUnitCloudsCount = businessUnitCloudVendorsMap.get(businessUnit).size();
				long totalBUCloudImpactQuestions = (businessUnitCloudsCount > 0 && totalCloudQuestions > 0) ? businessUnitCloudsCount * totalCloudQuestions : 1;
				int buImpactCloudQuestionAnswers = (impactCloudQuestionsAnswersMap4BU.containsKey(businessUnit)) ? impactCloudQuestionsAnswersMap4BU.get(businessUnit).size() : 0;
				Double buImpactCloudQuestionsAnsweredPercent = Double.valueOf(buImpactCloudQuestionAnswers) / Double.valueOf(totalBUCloudImpactQuestions) * 100d;
				if (buImpactCloudQuestionsAnsweredPercent >= 99.99d) {
					itemsList.add($I(100d, "%").round(2).applyTextAlign("right").applyColor("#00ff00"));
				} else {
					itemsList.add($I(buImpactCloudQuestionsAnsweredPercent, "%").round(2).applyTextAlign("right"));
				}

				totalBUImpactQuestionsCount += totalBUCloudImpactQuestions;
				answeredBUImpactQuestionsCount += buImpactCloudQuestionAnswers;
			} else {
				itemsList.add(sI("N/A").applyColspan(2L).applyTextAlign("center"));
			}

			// Set Total Values
			Double buTotalImpactQuestionsAnsweredPercent = answeredBUImpactQuestionsCount / totalBUImpactQuestionsCount * 100d;
			Double buTotalLikelihoodQuestionsAnsweredPercent = Double.valueOf(answeredBULikelihoodQuestionsCount) / Double.valueOf(totalBULikelihoodQuestionsCount) * 100d;
			if (buTotalImpactQuestionsAnsweredPercent >= 99.99d) {
				itemsList.set(1, $I(100d, "%").round(2).applyTextAlign("right").applyColor("#00ff00"));
			} else {
				itemsList.set(1, $I(buTotalImpactQuestionsAnsweredPercent, "%").round(2).applyTextAlign("right"));
			}
			if (buTotalLikelihoodQuestionsAnsweredPercent >= 99.99d) {
				itemsList.set(2, $I(100d, "%").round(2).applyTextAlign("left").applyColor("#00ff00"));
			} else {
				itemsList.set(2, $I(buTotalLikelihoodQuestionsAnsweredPercent, "%").round(2).applyTextAlign("right"));
			}

			dashboardItem3.getGridItems().add(itemsList);
		}

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO buildVendorQuestionStatusDashboardDetails(Long riskModelId, DashboardStateDTO dashboardState) {

		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_QUESTION_STATUS, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$DESCRIPTION), DashboardType.Admin);

		// Create breadcrumbs
		DashboardBreadcrumbsHelper breadcrumbsTop = DashboardBreadcrumbsHelper.SET_UP_DASHBOARD(clientMessage).add("CYBER_RISK_SCORING_STATUS", "DASHBOARDS$QUESTION_STATUS$NAME", "/private/dashboards/35");

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();

		List<String> roles = Arrays.asList(RoleType.SYSTEM_OWNER.role(), RoleType.VENDOR_OWNER.role());
		List<Organizations> vendorList = organizationRepository.getListForRootOrganization(riskModel.getOrganizationId(), OrganizationType.Vendor);
		// List<AssociateVendors> associateVendors = associateVendorRepository.getListForOrganization(riskModel.getOrganizationId());
		Set<Organizations> vendorsSet = vendorList.stream().collect(Collectors.toSet());
		// Set<Organizations> cloudVendorsSet = vendorList.stream().filter(organizations -> Boolean.TRUE.equals(organizations.getIsCloudVendor())).collect(Collectors.toSet());
		// Map<Systems, Set<Organizations>> associateVendorsMap = new HashMap<>();
		Map<Users, Set<Organizations>> userToVendorsSetMap = new HashMap<>();
		for (Organizations vendor : vendorList) {
			if (vendor.getOwner() != null) {
				if (!userToVendorsSetMap.containsKey(vendor.getOwner())) {
					userToVendorsSetMap.put(vendor.getOwner(), new HashSet<>());
				}
				userToVendorsSetMap.get(vendor.getOwner()).add(vendor);
			}
		}

		List<RiskModelItemComments> itemComments = riskModelItemCommentsRepository.findAllByRiskModelIdAndItemTypeName(riskModelId, "VENDOR_SCORING_QUESTION_COMMENT");
		Map<Long, RiskModelItemComments> vendorItemCommentsMap = itemComments.stream().collect(Collectors.toMap(riskModelItemComments -> riskModelItemComments.getExternalId(), riskModelItemComments -> riskModelItemComments, (o, o2) -> o));

		// Create Initial Sections
		DashboardSectionDTO section2 = new DashboardSectionDTO(35200L, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$ITEM_DESCRIPTION));
		dashboard.getSections().add(section2);

		section2.setBreadcrumbs(breadcrumbsTop.extend("CYBER_RISK_SCORING_STATUS_2", SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$ITEM_NAME, "").getBreadcrumbs());

		// Build Dashboard Data
		// List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor, VendorType.Cloud);
		List<VendorType> vendorTypes = Arrays.asList(VendorType.Both, VendorType.Vendor);
		List<QualitativeQuestions> vendorQuestions = qualitativeQuestionRepository.getListByRiskModelIdAndType(riskModelId, vendorTypes);
		long totalVendorQuestions = calculateQuestionsCount(vendorQuestions);
		List<QuestionAnswersForVendor> vendorQuestionsAnswersList = questionAnswersForVendorRepository.getListByRiskModelAndVendorTypes(riskModelId, vendorTypes).stream().filter(item -> item.getAnswer() != null).collect(Collectors.toList());;
		Map<Organizations, List<QuestionAnswersForVendor>> vendorQuestionsAnswersMap = vendorQuestionsAnswersList.stream().collect(Collectors.groupingBy(QuestionAnswersForVendor::getVendor));

		DataTypeClassification piiDataType = dataTypeClassificationRepository.findById(DataTypeDomain.PII.getId()).orElse(null);

		// Initialize Organization Summary Scores
		/*
		if (permissionService.checkCurrentUserPermission(PermissionType.DASHBOARD_QUESTION_STATUS_REPORT)) {
			DashboardItemDTO dashboardItem1DowloadLink = new DashboardItemDTO(35101L, clientMessage.getMessage(SLCT.DASHBOARD_COMPONENTS$BUTTON$DOWNLOAD$NAME), "", DashboardItemType.Link);
			String authorization = request.getHeader("Authorization");
			String apiUrl = applicationProperties.getApiUrl();
			String tokenId = authorization;
			if (authorization != null && authorization.toLowerCase().contains("bearer")) {
				tokenId = authorization.substring("bearer".length() + 1);
			}
			String downloadUrl = apiUrl + "/api/dashboards/download/questions-status?token=" + tokenId.trim() + "&riskModelId=" + riskModelId.toString();
			dashboardItem1DowloadLink.addParameter("href", downloadUrl);
			dashboardItem1DowloadLink.addParameter("isExternal", "true");
			dashboardItem1DowloadLink.addParameter("textAlign", "right");
			section2.getDashboardItems().add(dashboardItem1DowloadLink);
		}
		*/

		// Initialize Organization Summary Scores
		// DashboardDataGridItemDTO dashboardItem2 = new DashboardDataGridItemDTO(VENDOR_STATUSES, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$ITEM_NAME));
		DashboardDataGridItemDTO dashboardItem2 = new DashboardDataGridItemDTO(VENDOR_STATUSES);
		dashboardItem2.getGridHeaders().add(
			Arrays.asList(
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$PERSON_HEADER), 0L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_NAME_HEADER), 1L),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$VENDOR_QUESTIONNAIRE_HEADER), null),
				DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.GLOBALS$COMMENTS), null).applyParam("width", "40%")
			)
		);
		/*
		dashboardItem2.getGridHeaders().get(0).get(0).setRowSpan(2l);
		dashboardItem2.getGridHeaders().get(0).get(1).setRowSpan(2l);
		dashboardItem2.getGridHeaders().get(0).get(2).setRowSpan(2l);
		dashboardItem2.getGridHeaders().get(0).get(3).setColSpan(2l);
		dashboardItem2.getGridHeaders().get(0).get(4).setColSpan(2l);
		dashboardItem2.getGridHeaders().add(Arrays.asList(
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$IMPACT_HEADER), 3L),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$LIKELIHOOD_HEADER), 4L),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$IMPACT_HEADER), 5L),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$LIKELIHOOD_HEADER), 6L)
		));
		*/
		dashboardItem2.setDashboardItemFilter(new DashboardItemFilterDTO(3501L, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_STATUS$VENDOR_STATUS$FILTER$ITEM_NAME), null, DashboardFilterType.VENDOR_QUESTION));
		section2.getDashboardItems().add(dashboardItem2);
		for (Organizations vendor : vendorsSet) {
			List<DashboardDataItemDTO>  itemsList = new ArrayList<>();

			String ownerName = vendor.getOwner() != null ? vendor.getOwner().getFullName() : "";
			DashboardDataItemDTO ownerCell = sI(ownerName);
			if (vendor.getOwner() != null) {
				ownerCell.applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null));
			}
			itemsList.add(ownerCell);
			itemsList.add(sI(vendor.getName()));

			/*
			if (vendor.getOwner() != null && vendor.getOwner().getBusinessUnit() != null) {
				itemsList.add(sI(businessUnitService.getBusinessUnitPath(vendor.getOwner().getBusinessUnit(), true)).applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null)));
			} else {
				itemsList.add(sI(""));
			}
			*/

			// Vendor Questionnaire Status
			long answersCount = vendorQuestionsAnswersMap.containsKey(vendor) ? vendorQuestionsAnswersMap.get(vendor).size() : 0;
			// String urlLink = DashboardsConfig.buildVendorRiskQualQuestionsUrl(vendor.getId(), "all") + "&back=/private/dashboards/35002";
			String urlLink = DashboardsConfig.buildVendorRiskQualQuestionsUrl(vendor.getId(), "all") + "&";
			// System Questionnaire Status
			if (answersCount == 0) {
				itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$NOT_STARTED)).applyColor("#ff0000")
					.applyLink(DashboardLinkDTO.of(urlLink)));
			} else if (answersCount >= totalVendorQuestions) {

				itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARD_VALUES$COMPLETE)).applyColor("#00ff00")
					.applyLink(DashboardLinkDTO.of(urlLink)));
			} else {
				itemsList.add(sI(MessageFormat.format("{0}/{1}", answersCount, totalVendorQuestions)).applyTextAlign("right")
					.applyLink(DashboardLinkDTO.of(urlLink)));
			}

			String vendorComment = vendorItemCommentsMap.containsKey(vendor.getId()) ? vendorItemCommentsMap.get(vendor.getId()).getComment() : "";
			itemsList.add(sI(vendorComment).applyType("TEXTAREA")
				.applyParam("vendorId", vendor.getId().toString())
				.applyParam("method", "VENDOR_SCORING_QUESTION_COMMENT")
				.applyParam("riskModelId", riskModelId.toString())
			);

			dashboardItem2.getGridItems().add(itemsList);
		}

		return dashboard;
	}

	public static long calculateQuestionsCount(List<QualitativeQuestions> impactQuestions) {
		return impactQuestions.stream().filter(
			qualitativeQuestions -> {
				return (
					(qualitativeQuestions.getBranchingLogic() == null || qualitativeQuestions.getBranchingLogic().size() < 1)
						&& Boolean.TRUE.equals(qualitativeQuestions.getAllVendorsSelected())
				);
			}
		).count();
	}

	/**
	 * Get Question Answers For System
	 *
	 * @return Systems Map
	 */
	public Map<Systems, QuestionAnswersForSystem> getSystemAnswersMapForSystems(Long riskModelId, Long questionId) {
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		List<QuestionAnswersForSystem> systemQuestionAnswers = questionAnswersForSystemRepository.getListByRiskModelAndQuestions(riskModelId, Arrays.asList(questionId));
		List<Systems> systems = systemRepository.getAllByOrganization(riskModel.getOrganizationId());
		Map<Systems, QuestionAnswersForSystem> result = new HashMap<>();
		for (Systems system : systems) result.put(system, null);

		for (QuestionAnswersForSystem systemQuestionAnswer : systemQuestionAnswers) {
			result.put(systemQuestionAnswer.getSystem(), systemQuestionAnswer);
		}

		return result;
	}

	/**
	 * Get Question Answers For Vendor
	 *
	 * @return Vendors Map
	 */
	public Map<Organizations, QuestionAnswersForVendor> getVendorAnswersMapForVendors(Long riskModelId, Long questionId) {
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		QualitativeQuestions questionDetails = qualitativeQuestionRepository.findById(questionId).get();
		List<Organizations> vendors;
		List<QuestionAnswersForVendor> vendorQuestionAnswers = questionAnswersForVendorRepository.getListByRiskModelAndQuestions(riskModelId, Arrays.asList(questionId));
		if (VendorType.Cloud.equals(questionDetails.getVendorType())) {
			vendors = organizationRepository.getCloudListForRootOrganization(riskModel.getOrganizationId(), OrganizationType.Vendor);
		} else {
			vendors = organizationRepository.getListForRootOrganization(riskModel.getOrganizationId(), OrganizationType.Vendor);
		}
		Map<Organizations, QuestionAnswersForVendor> result = new HashMap<>();
		for (Organizations vendor : vendors) result.put(vendor, null);

		for (QuestionAnswersForVendor vendorQuestionAnswer : vendorQuestionAnswers) {
			if (result.containsKey(vendorQuestionAnswer.getVendor())) result.put(vendorQuestionAnswer.getVendor(), vendorQuestionAnswer);
		}

		return result;
	}

	/**
	 * Get System search Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO buildSystemQuestionStatusSearchDashboard(Long riskModelId, DashboardStateDTO dashboardState) {

		String dashboardRefUUID = (dashboardState != null && dashboardState.getReferenceUUID() != null) ? dashboardState.getReferenceUUID() : UUID.randomUUID().toString();
		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_QUESTION_STATUS, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$DESCRIPTION), DashboardType.Admin);
		dashboard.setReferenceUUID(dashboardRefUUID);

		DashboardItemFilterDTO dashboardItemFilter = dashboardState.getFilterByItemById(SYSTEM_STATUSES);
		Long questionId = dashboardItemFilter.getValueLong("question", "id");

		Map<Systems, QuestionAnswersForSystem> systemQuestionAnswerMap = getSystemAnswersMapForSystems(riskModelId, questionId);
		QualitativeQuestions questionDetails = qualitativeQuestionRepository.findById(questionId).get();
		String metricDomainName = questionDetails.getQualitativeMetric().getMetricDomain().getName().toLowerCase();

		DashboardSectionDTO section = new DashboardSectionDTO(SYSTEM_QUESTION_ANSWERS, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_ANSWERS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_ANSWERS$ITEM_DESCRIPTION));
		dashboard.getSections().add(section);

		if (permissionService.checkCurrentUserPermission(PermissionType.DASHBOARD_QUESTION_STATUS_REPORT)) {
			DashboardItemDTO downloadButton = buildDownloadButtonDashboardItemDTO(riskModelId, dashboardRefUUID, 35701L);
			section.getDashboardItems().add(downloadButton);
		}
		// Initialize System Question table
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(35702L, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_ANSWERS$SYSTEM_QUESTION$ITEM_NAME) + questionDetails.getQuestion());
		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_ANSWERS$SYSTEM_QUESTION$PERSON_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_ANSWERS$SYSTEM_QUESTION$SYSTEM_NAME_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_ANSWERS$SYSTEM_QUESTION$BUSINESS_UNIT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_ANSWERS$SYSTEM_QUESTION$ANSWER_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_ANSWERS$SYSTEM_QUESTION$ANSWER_WEIGHT_HEADER)
		));
		section.getDashboardItems().add(dashboardItem);

		for (Map.Entry<Systems, QuestionAnswersForSystem> systemQuestionAnswerEntry : systemQuestionAnswerMap.entrySet()) {

			QuestionAnswersForSystem systemQuestionAnswer = systemQuestionAnswerEntry.getValue();
			Systems system = systemQuestionAnswerEntry.getKey();

			List<DashboardDataItemDTO>  itemsList = new ArrayList<>();

			String ownerName = system.getOwner() != null ? system.getOwner().getFullName() : "";
			DashboardDataItemDTO ownerCell = sI(ownerName);
			if (system.getOwner() != null) {
				ownerCell.applyDrilldown(DashboardDataItemDrilldownDTO.of(system.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null));
			}
			itemsList.add(ownerCell);

			itemsList.add(sI(system.getName()).applyColor("#ff0000")
				.applyLink(DashboardLinkDTO.of(DashboardsConfig.buildSystemRiskQualQuestionsUrl(system.getId(), metricDomainName))));

			if (system.getOwner() != null && system.getOwner().getBusinessUnit() != null) {
				itemsList.add(sI(businessUnitService.getBusinessUnitPath(system.getOwner().getBusinessUnit(), true)).applyDrilldown(DashboardDataItemDrilldownDTO.of(system.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_SYSOWN, null)));
			} else {
				itemsList.add(sI(""));
			}

			if (Boolean.TRUE.equals(system.getIsEtl())) {
				itemsList.add(sI(clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$SYSTEM_ANSWERS$SYSTEM_QUESTION$ETL_ANSWER))); // N/A - ETL
				itemsList.add(sI(""));
			} else {
				if (systemQuestionAnswer != null) {
					itemsList.add(sI(systemQuestionAnswer.getAnswer().getAnswer()));
					itemsList.add(dI(systemQuestionAnswer.getAnswer().getAnswerWeight().getValue().doubleValue()));
				} else {
					itemsList.add(sI(""));
					itemsList.add(sI(""));
				}
			}


			dashboardItem.getGridItems().add(itemsList);
		}

		dashboardState.setReferenceUUID(dashboardRefUUID);
		cacheService.saveSearchConfig(dashboardRefUUID, dashboardState);

		return dashboard;
	}

	/**
	 * Get Vendor search Dashboard definition
	 *
	 * @return Dashboard
	 */
	public DashboardDTO buildVendorQuestionStatusSearchDashboard(Long riskModelId, DashboardStateDTO dashboardState) {

		String dashboardRefUUID = (dashboardState != null && dashboardState.getReferenceUUID() != null) ? dashboardState.getReferenceUUID() : UUID.randomUUID().toString();
		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_QUESTION_STATUS, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$DESCRIPTION), DashboardType.Admin);
		dashboard.setReferenceUUID(dashboardRefUUID);

		DashboardItemFilterDTO dashboardItemFilter = dashboardState.getFilterByItemById(VENDOR_STATUSES);
		Long questionId = dashboardItemFilter.getValueLong("question", "id");

		// List<QuestionAnswersForVendor> vendorQuestionAnswers = questionAnswersForVendorRepository.getListByRiskModelAndQuestions(riskModelId, Arrays.asList(questionId));
		Map<Organizations, QuestionAnswersForVendor> vendorQuestionAnswerMap = getVendorAnswersMapForVendors(riskModelId, questionId);
		QualitativeQuestions questionDetails = qualitativeQuestionRepository.findById(questionId).get();
		String metricDomainName = questionDetails.getQualitativeMetric().getMetricDomain().getName().toLowerCase();
		String questionType = VendorType.Cloud.equals(questionDetails.getVendorType()) ? "cloud-scoring" : "vendors";

		DashboardSectionDTO section = new DashboardSectionDTO(VENDOR_QUESTION_ANSWERS, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_ANSWERS$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_ANSWERS$ITEM_DESCRIPTION));
		dashboard.getSections().add(section);

		if (permissionService.checkCurrentUserPermission(PermissionType.DASHBOARD_QUESTION_STATUS_REPORT)) {
			DashboardItemDTO downloadButton = buildDownloadButtonDashboardItemDTO(riskModelId, dashboardRefUUID, 35801L);
			section.getDashboardItems().add(downloadButton);
		}
		// Initialize Vendor Question table
		DashboardTableItemDTO dashboardItem = new DashboardTableItemDTO(35802L, clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_ANSWERS$VENDOR_QUESTION$ITEM_NAME) + questionDetails.getQuestion());
		dashboardItem.addGridHeaders(Arrays.asList(
			clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_ANSWERS$VENDOR_QUESTION$PERSON_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_ANSWERS$VENDOR_QUESTION$VENDOR_NAME_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_ANSWERS$VENDOR_QUESTION$BUSINESS_UNIT_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_ANSWERS$VENDOR_QUESTION$ANSWER_HEADER),
			clientMessage.getMessage(SLCT.DASHBOARDS$QUESTION_STATUS$VENDOR_ANSWERS$VENDOR_QUESTION$ANSWER_WEIGHT_HEADER)
		));
		section.getDashboardItems().add(dashboardItem);

		for (Map.Entry<Organizations, QuestionAnswersForVendor> vendorQuestionAnswerEntry : vendorQuestionAnswerMap.entrySet()) {

			QuestionAnswersForVendor vendorQuestionAnswer = vendorQuestionAnswerEntry.getValue();
			Organizations vendor = vendorQuestionAnswerEntry.getKey();

			List<DashboardDataItemDTO>  itemsList = new ArrayList<>();

			String ownerName = vendor.getOwner() != null ? vendor.getOwner().getFullName() : "";
			DashboardDataItemDTO ownerCell = sI(ownerName);
			if (vendor.getOwner() != null) {
				ownerCell.applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_VNDOWN_QUAL, null));
			}
			itemsList.add(ownerCell);
			itemsList.add(sI(vendor.getName()).applyColor("#ff0000")
				.applyLink(DashboardLinkDTO.of(MessageFormat.format("/private/{0}/{2,number,#}?metric={1}", questionType, metricDomainName, vendor.getId()))));
			if (vendor.getOwner() != null && vendor.getOwner().getBusinessUnit() != null) {
				itemsList.add(sI(businessUnitService.getBusinessUnitPath(vendor.getOwner().getBusinessUnit(), true)).applyDrilldown(DashboardDataItemDrilldownDTO.of(vendor.getOwner(), DashboardDataItemDrilldownDTO.ADMIN_VNDOWN_QUAL, null)));
			} else {
				itemsList.add(sI(""));
			}

			if (vendorQuestionAnswer != null) {
				itemsList.add(sI(vendorQuestionAnswer.getAnswer().getAnswer()));
				itemsList.add(dI(vendorQuestionAnswer.getAnswer().getAnswerWeight().getValue().doubleValue()));
			} else {
				itemsList.add(sI(""));
				itemsList.add(sI(""));
			}

			dashboardItem.getGridItems().add(itemsList);
		}

		dashboardState.setReferenceUUID(dashboardRefUUID);
		cacheService.saveSearchConfig(dashboardRefUUID, dashboardState);

		return dashboard;
	}

	/**
	 * Get Dashboard definition
	 *
	 * @return Dashboard
	 */
	public ByteArrayOutputStream buildQuestionStatusSearchReport(Long riskModelId, DashboardStateDTO searchConfig) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		DashboardItemFilterDTO dashboardItemFilter;
		if ((dashboardItemFilter = searchConfig.getFilterByItemById(SYSTEM_STATUSES)) != null && dashboardItemFilter.hasValues()) {
			Long questionId = dashboardItemFilter.getValueLong("question", "id");

			// List<QuestionAnswersForSystem> systemQuestionAnswers = questionAnswersForSystemRepository.getListByRiskModelAndQuestions(riskModelId, Arrays.asList(questionId));
			Map<Systems, QuestionAnswersForSystem> systemQuestionAnswerMap = getSystemAnswersMapForSystems(riskModelId, questionId);
			QualitativeQuestions questionDetails = qualitativeQuestionRepository.findById(questionId).get();

			try {
				CSVPrinter csvPrinter = getSystemCsvPrinter(result);
				for (Map.Entry<Systems, QuestionAnswersForSystem> systemQuestionAnswerEntry : systemQuestionAnswerMap.entrySet()) {

					QuestionAnswersForSystem systemQuestionAnswer = systemQuestionAnswerEntry.getValue();
					Systems system = systemQuestionAnswerEntry.getKey();

					String businessUnitPath = businessUnitService.getBusinessUnitPath(system.getBusinessUnit(), true, "/");
					csvPrinter.printRecord(
						system.getOwner() != null ? system.getOwner().getFullName() : "",
						system.getOwner() != null ? system.getOwner().getEmail() : "",
						system.getName(),
						businessUnitPath,
						(Boolean.TRUE.equals(system.getIsEtl())) ? "N/A - ETL" : (systemQuestionAnswer != null ? systemQuestionAnswer.getAnswer().getAnswer() : ""),
						(Boolean.TRUE.equals(system.getIsEtl())) ? "" : (systemQuestionAnswer != null ? systemQuestionAnswer.getAnswer().getAnswerWeight().getValue() : "")
					);
				}
				csvPrinter.flush();
			} catch (IOException e) {
				log.warn(e.getMessage(), e);
			}
		} else if ((dashboardItemFilter = searchConfig.getFilterByItemById(VENDOR_STATUSES)) != null && dashboardItemFilter.hasValues()) {
			Long questionId = dashboardItemFilter.getValueLong("question", "id");

			// List<QuestionAnswersForVendor> vendorQuestionAnswers = questionAnswersForVendorRepository.getListByRiskModelAndQuestions(riskModelId, Arrays.asList(questionId));
			Map<Organizations, QuestionAnswersForVendor> vendorQuestionAnswerMap = getVendorAnswersMapForVendors(riskModelId, questionId);
			QualitativeQuestions questionDetails = qualitativeQuestionRepository.findById(questionId).get();

			try {
				CSVPrinter csvPrinter = getVendorCsvPrinter(result);
				for (Map.Entry<Organizations, QuestionAnswersForVendor> vendorQuestionAnswerEntry : vendorQuestionAnswerMap.entrySet()) {

					QuestionAnswersForVendor vendorQuestionAnswer = vendorQuestionAnswerEntry.getValue();
					Organizations vendor = vendorQuestionAnswerEntry.getKey();

					String businessUnitPath = vendor.getOwner() != null ? businessUnitService.getBusinessUnitPath(vendor.getOwner().getBusinessUnit(), true, "/") : "";
					csvPrinter.printRecord(
						vendor.getOwner() != null ? vendor.getOwner().getFullName() : "",
						vendor.getOwner() != null ? vendor.getOwner().getEmail() : "",
						vendor.getName(),
						businessUnitPath,
						vendorQuestionAnswer != null ? vendorQuestionAnswer.getAnswer().getAnswer() : "",
						vendorQuestionAnswer != null ? vendorQuestionAnswer.getAnswer().getAnswerWeight().getValue() : ""
					);
				}
				csvPrinter.flush();
			} catch (IOException e) {
				log.warn(e.getMessage(), e);
			}
		}

		return result;
	}

	private CSVPrinter getSystemCsvPrinter(ByteArrayOutputStream result) throws IOException {
		Writer writer = new OutputStreamWriter(result);
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
			"Person",
			"Email",
			"System Name",
			"Business Unit",
			"Answer",
			"Answer Weight"
		);

		return new CSVPrinter(writer, csvFormat);
	}

	private CSVPrinter getVendorCsvPrinter(ByteArrayOutputStream result) throws IOException {
		Writer writer = new OutputStreamWriter(result);
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
			"Person",
			"Email",
			"Vendor Name",
			"Business Unit",
			"Answer",
			"Answer Weight"
		);

		return new CSVPrinter(writer, csvFormat);
	}

}
