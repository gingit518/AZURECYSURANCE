package com.cyberintech.vrisk.server.service.dashboards;

import com.cyberintech.vrisk.server.model.dto.dashboards.*;
import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentFindingLink;
import com.cyberintech.vrisk.server.model.jpa.domains.DashboardType;
import com.cyberintech.vrisk.server.model.jpa.domains.SLCT;
import com.cyberintech.vrisk.server.model.jpa.entity.AssessmentFindings;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.RiskModels;
import com.cyberintech.vrisk.server.repository.jpa.AssessmentFindingsRepository;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.RiskModelRepository;
import com.cyberintech.vrisk.server.util.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Assessment Finding Dashboard management service
 *
 * @author   Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version  0.1.1
 * @since    2020-09-18
 */
@Service
@Slf4j
public class AssessmentFindingDashboardService extends DashboardServiceBase {

	@Autowired
	private ClientMessage clientMessage;

	@Autowired
	private AssessmentFindingsRepository assessmentFindingsRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private RiskModelRepository riskModelRepository;

	public DashboardDTO getRiskRegisterDashboard(Long riskModelId) {

		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_RISK_REGISTER, clientMessage.getMessage(SLCT.DASHBOARDS$RISK_REGISTER$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$RISK_REGISTER$DESCRIPTION), DashboardType.None);

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		List<AssessmentFindings> assessmentFindings = assessmentFindingsRepository.getAllListByOrganization(organization.getId());
		// List<AssessmentFindings> assessmentFindings = assessmentFindingsRepository.getListByOrganizationAndIsGDPR(organization.getId(), true);

		buildAssessmentFindingsDashboard(dashboard, assessmentFindings);

		return dashboard;
	}

	public DashboardDTO getAssessmentFindingsDashboard(Long riskModelId) {

		DashboardDTO dashboard = new DashboardDTO(DashboardsConfig.DASHBOARD_ASSESSMENT_FINDING, clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT_FINDING$NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT_FINDING$DESCRIPTION), DashboardType.None);

		// Load Initial Data
		RiskModels riskModel = riskModelRepository.findById(riskModelId).get();
		Organizations organization = organizationRepository.findById(riskModel.getOrganizationId()).get();
		List<AssessmentFindings> assessmentFindings = assessmentFindingsRepository.getAllListByOrganization(organization.getId());

		buildAssessmentFindingsDashboard(dashboard, assessmentFindings);

		return dashboard;
	}

	public void buildAssessmentFindingsDashboard(DashboardDTO dashboard, List<AssessmentFindings> assessmentFindings) {

		// Create Initial Sections
		DashboardSectionDTO section = new DashboardSectionDTO(12800L, clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT_FINDING$MAIN_SECTION$ITEM_NAME), clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT_FINDING$MAIN_SECTION$ITEM_DESCRIPTION));
		dashboard.getSections().add(section);

		DashboardBreadcrumbsHelper breadCrumbsTop;
		if (DashboardsConfig.DASHBOARD_ASSESSMENT_FINDING.equals(dashboard.getId())) {
			breadCrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD_CISO(clientMessage)
				.extend("DASHBOARD_ASSESSMENT_FINDING", SLCT.DASHBOARDS$ASSESSMENT_FINDING$NAME, "");
		} else {
			// DashboardsConfig.DASHBOARD_RISK_REGISTER
			breadCrumbsTop = DashboardBreadcrumbsHelper.DASHBOARD(clientMessage)
				.extend("DASHBOARDS_RISK_REGISTER", "DASHBOARDS$RISK_REGISTER$NAME", "");
		}
		section.setBreadcrumbs(breadCrumbsTop.getBreadcrumbs());

		// Initialize Assessment Findings Summary Scores
		// DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(128001L, clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT_FINDING$MAIN_SECTION$ASSESSMENT_FINDINGS_TABLE$ITEM_NAME));
		DashboardDataGridItemDTO dashboardItem = new DashboardDataGridItemDTO(128001L, "");
		dashboardItem.getGridHeaders().add(Arrays.asList(
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT_FINDING$MAIN_SECTION$ASSESSMENT_FINDINGS_TABLE$NAME_HEADER), null),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT_FINDING$MAIN_SECTION$ASSESSMENT_FINDINGS_TABLE$MATURITY_HEADER), null),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT_FINDING$MAIN_SECTION$ASSESSMENT_FINDINGS_TABLE$TECH_CATEGORY_HEADER), null),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT_FINDING$MAIN_SECTION$ASSESSMENT_FINDINGS_TABLE$TECHNOLOGY_HEADER), null),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT_FINDING$MAIN_SECTION$ASSESSMENT_FINDINGS_TABLE$LINK_TYPE_HEADER), null),
			DashboardDataItemHeaderDTO.of(clientMessage.getMessage(SLCT.DASHBOARDS$ASSESSMENT_FINDING$MAIN_SECTION$ASSESSMENT_FINDINGS_TABLE$LINKED_ENTITIES_HEADER), null)
		));
		section.getDashboardItems().add(dashboardItem);

		for (AssessmentFindings assessmentFinding: assessmentFindings) {
			String findingName = StringUtils.isNotEmpty(assessmentFinding.getName()) ? assessmentFinding.getName() : "";
			String findingMaturityName = assessmentFinding.getControlMaturity() != null && StringUtils.isNotEmpty(assessmentFinding.getControlMaturity().getName()) ? assessmentFinding.getControlMaturity().getName() : "";
			String findingMaturityWeight = assessmentFinding.getControlMaturity() != null && assessmentFinding.getControlMaturity().getWeight() != null ? Double.toString(assessmentFinding.getControlMaturity().getWeight()) : "";
			String findingMaturity = (StringUtils.isNotEmpty(findingMaturityName) ? findingMaturityName : "") + (StringUtils.isNotEmpty(findingMaturityWeight) ? " - " + findingMaturityWeight + "%" : "");
			String findingTechCategory = assessmentFinding.getTechnologyCategory() != null && StringUtils.isNotEmpty(assessmentFinding.getTechnologyCategory().getName()) ? assessmentFinding.getTechnologyCategory().getName() : "";
			String findingTechnology = assessmentFinding.getTechnology() != null && StringUtils.isNotEmpty(assessmentFinding.getTechnology().getName()) ? assessmentFinding.getTechnology().getName() : "";
			String findingLinkType = assessmentFinding.getLinkType() != null ? assessmentFinding.getLinkType().name().substring(0,1) + assessmentFinding.getLinkType().name().substring(1).toLowerCase() : "";

			String findingLinkedEntities = "";

			if (AssessmentFindingLink.ASSESSMENT.equals(assessmentFinding.getLinkType())) {
				// joining names of linked assessments into single string ignoring those not having name
				findingLinkedEntities = Optional.ofNullable(assessmentFinding.getAssessments()).orElseGet(HashSet::new).stream().map(assessment -> {
					if (assessment != null && StringUtils.isNotEmpty(assessment.getName())) {
						return assessment.getName();
					} else {
						return null;
					}
				}).filter(Objects::nonNull).collect(Collectors.joining(", "));
			} else if (AssessmentFindingLink.REQUIREMENT.equals(assessmentFinding.getLinkType())) {
				// joining codes of linked requirements into single string ignoring those not having code
				findingLinkedEntities = Optional.ofNullable(assessmentFinding.getSecurityRequirements()).orElseGet(HashSet::new).stream().map(requirement -> {
					if (requirement != null && requirement.getSecurityControlName() != null && StringUtils.isNotEmpty(requirement.getSecurityControlName().getName())) {
						return requirement.getSecurityControlName().getName();
					} else if (requirement != null && StringUtils.isNotEmpty(requirement.getCode())) {
						return requirement.getCode();
					} else {
						return null;
					}

				}).filter(Objects::nonNull).collect(Collectors.joining(", "));

			}

			dashboardItem.getGridItems().add(Arrays.asList(
				sI(findingName),
				sI(findingMaturity),
				sI(findingTechCategory),
				sI(findingTechnology),
				sI(findingLinkType),
				sI(findingLinkedEntities)
			));
		}

	}
























}
