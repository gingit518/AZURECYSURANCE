package com.cyberintech.vrisk.server.service.integrations.elastio;

import com.cyberintech.vrisk.server.model.dao.OrganizationModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.ElastioOrganizationFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.OrganizationFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.organization.ElastioOrganizationEvaluationResultDTO;
import com.cyberintech.vrisk.server.model.dto.organization.ElastioOrganizationViewDTO;
import com.cyberintech.vrisk.server.model.dto.organization.PackagePlansDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.PackagePlans;
import com.cyberintech.vrisk.server.repository.jpa.OrganizationRepository;
import com.cyberintech.vrisk.server.repository.jpa.PackagePlansRepository;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import com.cyberintech.vrisk.server.service.AuditLogService;
import com.cyberintech.vrisk.server.service.admin.AdminOrganizationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Organization management Service. Implements basic Organization logic.
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-12-20
 */
@Service
@Slf4j
public class ElastioOrganizationService extends AdminOrganizationService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private OrganizationModelDAO organizationModelDAO;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private PackagePlansRepository packagePlansRepository;

	/**
	 * Get List of Organizations by Type and Filter
	 *
	 * @return Users List
	 */
	public FilteredResponse<ElastioOrganizationFilter, ElastioOrganizationViewDTO> getElastioListFiltered(FilteredRequest<ElastioOrganizationFilter> filteredRequest) {

		FilteredResponse<ElastioOrganizationFilter, ElastioOrganizationViewDTO> filteredResponse = new FilteredResponse<ElastioOrganizationFilter, ElastioOrganizationViewDTO>(filteredRequest);

		if (filteredRequest.getFilter() == null) {
			filteredRequest.setFilter(new ElastioOrganizationFilter());
		}

		OrganizationFilter filter = new OrganizationFilter();
		filter.setName(filteredRequest.getFilter().getName());
		filter.setPackagePlanIds(List.of(PackagePlans.PACKAGE_PLAN_ELASTIO));

		PagedResult<Organizations> pagedResult = organizationModelDAO.getItemsPageable(filter, filteredRequest.toPageRequest(), filteredRequest.getSort());

		List<ElastioOrganizationViewDTO> itemsDTOList = DTOBase.fromEntitiesList(pagedResult.getItems(), ElastioOrganizationViewDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(pagedResult.getCount().intValue());

		return filteredResponse;
	}

	/**
	 * Get organization Details to edit
	 *
	 * @param id
	 * @return
	 */
	public ElastioOrganizationViewDTO getElastioDetails(Long id) {
		Organizations organization = getOrganization(id);

		ElastioOrganizationViewDTO result = new ElastioOrganizationViewDTO(organization);

		return result;
	}

	/**
	 * Create new Organization
	 *
	 * @return New Organization
	 */
	public ElastioOrganizationViewDTO createElastio(ElastioOrganizationViewDTO elastioOrganizationDTO) {

		PackagePlans elastioPackagePlan = packagePlansRepository.findById(PackagePlans.PACKAGE_PLAN_ELASTIO).get();

		// Throw Exception if ID is set in create mode
		Optional<Organizations> existingItemOpt = getElastioOrganization(elastioOrganizationDTO, elastioPackagePlan);

		PackagePlansDTO packagePlan = new PackagePlansDTO();
		packagePlan.setId(PackagePlans.PACKAGE_PLAN_ELASTIO);

		Organizations newItem = existingItemOpt.orElse(null);
		if (newItem == null) {
			newItem = new Organizations();
		}
		newItem.setOrganizationType(OrganizationType.Organization);
		newItem.setName(elastioOrganizationDTO.getName());
		newItem.setDescription(elastioOrganizationDTO.getDescription());
		newItem.setUid(elastioOrganizationDTO.getUid());
		newItem.setPlatformType(elastioOrganizationDTO.getPlatformType());
		newItem.setAssetType(elastioOrganizationDTO.getAssetType());
		newItem.setAverageRevenue(elastioOrganizationDTO.getAnnualRevenue());
		newItem.setAmountOfDataInTerabytes(elastioOrganizationDTO.getAmountOfDataInTerabytes());
		newItem.setReplicationFactor(elastioOrganizationDTO.getReplicationFactor());
		if (newItem.getUid() == null) newItem.setUid(UUID.randomUUID().toString());
		if (newItem.getCreatedAt() == null) newItem.setCreatedAt(new Date());
		newItem.setUpdatedAt(new Date());
		newItem.setPackagePlan(elastioPackagePlan);

		// TODO Apply Contact Email/Name

		// newItemDTO.setPackagePlan(packagePlan);
		// applyEntityChanges(newItemDTO, newItem);

		Organizations saveResult = organizationRepository.save(newItem);

		// Verify Elastio Package Plan setup
		verifyElastioPackagePlanSetup(newItem);

		ElastioOrganizationViewDTO result = new ElastioOrganizationViewDTO(saveResult);

		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.ORGANIZATION,
			saveResult.getId(),
			result,
			collectAuditLogItems(null, newItem.getRootParent() != null ? newItem.getRootParent().getId() : newItem.getId())
		);

		return result;
	}


	/**
	 * Create new Organization
	 *
	 * @return New Organization
	 */
	public ElastioOrganizationViewDTO evaluateElastio(ElastioOrganizationViewDTO elastioOrganizationDTO) {

		PackagePlans elastioPackagePlan = packagePlansRepository.findById(PackagePlans.PACKAGE_PLAN_ELASTIO).get();

		// Throw Exception if ID is set in create mode
		Organizations elastioOrganization = getElastioOrganization(elastioOrganizationDTO, elastioPackagePlan).orElseThrow(() -> new ItemNotFoundException("Requested Organization is not found."));

		ElastioOrganizationViewDTO result = new ElastioOrganizationViewDTO(elastioOrganization);

		// TODO Apply evaluation
		ElastioOrganizationEvaluationResultDTO evaluationResult = new ElastioOrganizationEvaluationResultDTO();
		// evaluationResult.initDemoValues();

		// Get Elastio Variables
		// TODO Handle Annual Revenue
		double annualRevenue = elastioOrganization.getAverageRevenue() != null ? elastioOrganization.getAverageRevenue() : 500000000d;
		Double amountOfDataInTerabytes = elastioOrganization.getAmountOfDataInTerabytes();

		Double revenuePerHour = annualRevenue / 8760; // Annual Revenue / 8,760 hours per year
		double businessInteractionRiskInHours = 168d; // 1 week = 168 hrs — given assumption
		double maxRansomwareDemand = 5000000d; // Given ransomware event value
		double probabilityOfRansomwareAttackInPercent = 35d; // Industry: ~1-in-3 for mid-market
		double ebitdaMarginForInterruptionCalcInPercent = 18d; // Typical for $500M revenue co.

		double ransomPayment = 5000000d; // Elastio clean recovery eliminates need to pay
		double businessInterruption = revenuePerHour * businessInteractionRiskInHours; // 168 hrs × hourly revenue; Elastio cuts downtime 90%
		double lostEBITDADuringDowntime = businessInterruption * ebitdaMarginForInterruptionCalcInPercent / 100; // Interruption loss × EBITDA margin
		double forensicsAndIncidentResponse = 850000d; // Cysurance warranty covers IR costs; Elastio accelerates
		double legalAndRegulatoryFines = 1200000d; // RiskQ quantifies & prioritizes compliance gaps pre-breach
		double dataRecoveryAndReconstruction = elastioOrganization.getAmountOfDataInTerabytes() * 50000; // Elastio proven clean restore vs. full rebuild
		double reputationalCustomerChurCost = 3000000; // Faster recovery = lower churn; est. 0.5% rev impact
		double cyberInsuranceDeductible = 500000; // Cysurance warranty absorbs deductible first
		double prAndCrisisCommunications = 350000; // Reduced with fast, provable recovery narrative
		double reInfectionRisk = 1500000; // 80% of payers hit again; Elastio breaks reinfection loop

		double ransomPaymentWithSolution = 0d; // Elastio clean recovery eliminates need to pay
		double businessInterruptionWithSolution = businessInterruption * 0.1; // 168 hrs × hourly revenue; Elastio cuts downtime 90%
		double lostEBITDADuringDowntimeWithSolution = lostEBITDADuringDowntime * 0.1; // Interruption loss × EBITDA margin with Solution
		double forensicsAndIncidentResponseWithSolution = 850000d; // Cysurance warranty covers IR costs; Elastio accelerates
		double legalAndRegulatoryFinesWithSolution = 1200000d; // RiskQ quantifies & prioritizes compliance gaps pre-breach
		double dataRecoveryAndReconstructionWithSolution = elastioOrganization.getAmountOfDataInTerabytes() * 3000; // Elastio proven clean restore vs. full rebuild
		double reputationalCustomerChurCostWithSolution = 600000; // Faster recovery = lower churn; est. 0.5% rev impact
		double cyberInsuranceDeductibleWithSolution = 500000; // Cysurance warranty absorbs deductible first
		double prAndCrisisCommunicationsWithSolution = 75000; // Reduced with fast, provable recovery narrative
		double reInfectionRiskWithSolution = 0; // 80% of payers hit again; Elastio breaks reinfection loop

		double downtimeSavings = lostEBITDADuringDowntime - lostEBITDADuringDowntimeWithSolution; // Downtime savings
		double businessInterruptionSavings = businessInterruption - businessInterruptionWithSolution; // Business Interruption savings

		double cysuranceWarrantyYear1 = 0; // First-loss layer: covers IR, forensics, legal up to $500K; no underwriting
		double riskQValuRisPlatformACV = 0; // Financial risk quantification, board reporting, insurance justification, compliance gap prioritization
		// TODO calculate proper Elastio Price based on the Storage in TB
		double elastioRecoveryAssurance = calculateElastioStoragePrice(amountOfDataInTerabytes); // Continuous backup scanning, clean restore validation, 90% downtime reduction, NYDFS/DORA/PCI proof
		double totalAnnualInsuranceCost = cysuranceWarrantyYear1 + riskQValuRisPlatformACV + elastioRecoveryAssurance; // FULL Assurance

		double totalCoastOfRansomwareEvent = ransomPayment + businessInterruption + lostEBITDADuringDowntime + forensicsAndIncidentResponse
			+ legalAndRegulatoryFines + dataRecoveryAndReconstruction + reputationalCustomerChurCost + cyberInsuranceDeductible + prAndCrisisCommunications + reInfectionRisk;
		double totalCoastOfRansomwareEventWithSolution = ransomPaymentWithSolution + businessInterruptionWithSolution + lostEBITDADuringDowntimeWithSolution + forensicsAndIncidentResponseWithSolution
			+ legalAndRegulatoryFinesWithSolution + dataRecoveryAndReconstructionWithSolution + reputationalCustomerChurCostWithSolution + cyberInsuranceDeductibleWithSolution
			+ prAndCrisisCommunicationsWithSolution;

		// double expectedAnnualSavings = totalCoastOfRansomwareEvent - totalCoastOfRansomwareEventWithSolution;

		double totalCoastOfAttack = totalCoastOfRansomwareEvent; // Sum of all ransomware event costs
		double totalCoastOfAttackWithSolution = totalCoastOfRansomwareEventWithSolution; // Residual costs after solution mitigation
		double grossRiskReduction = totalCoastOfAttack - totalCoastOfAttackWithSolution; // Direct cost avoidance per event
		double annualSolutionInvestment = totalAnnualInsuranceCost; // Cysurance + RiskQ + Elastio combined
		double expectedAnnualSavings = grossRiskReduction * probabilityOfRansomwareAttackInPercent / 100; // Risk reduction × 35% annual attack probability
		double netAnnualBenefit = expectedAnnualSavings - annualSolutionInvestment; // Expected savings less solution cost
		double roiOnSolutionInvestment = 100 * netAnnualBenefit / annualSolutionInvestment; // Net benefit / annual cost
		double riskAdjustedROIMultiple = expectedAnnualSavings / annualSolutionInvestment; // Net benefit / annual cost
		double paybackPeriodMonths = 12 * annualSolutionInvestment / expectedAnnualSavings; // Net benefit / annual cost

		evaluationResult.setBaselineRansomwareExposure(totalCoastOfRansomwareEvent);
		evaluationResult.setBaselineDowntimeLoss(businessInterruption);
		evaluationResult.setDowntimeLossPostElastio(businessInterruptionWithSolution);
		evaluationResult.setDowntimeSavings(businessInterruption - businessInterruptionWithSolution);
		evaluationResult.setRoiRansomware(roiOnSolutionInvestment);
		evaluationResult.setPaybackPeriodRansomware(paybackPeriodMonths);
		evaluationResult.setTotalAnnualSavings(expectedAnnualSavings);

		result.setEvaluationResult(evaluationResult);

		return result;
	}

	private Optional<Organizations> getElastioOrganization(ElastioOrganizationViewDTO elastioOrganizationDTO, PackagePlans elastioPackagePlan) {
		Optional<Organizations> existingItemOpt = Optional.empty();
		if (elastioOrganizationDTO.getId() != null) {
			existingItemOpt = organizationRepository.findByIdAndPackagePlan(elastioOrganizationDTO.getId(), elastioPackagePlan);
		}
		if (existingItemOpt.isEmpty()) {
			existingItemOpt = organizationRepository.findByUidAndPackagePlan(elastioOrganizationDTO.getName(), elastioPackagePlan);
		}
		if (existingItemOpt.isEmpty()) {
			existingItemOpt = organizationRepository.findFirstByNameAndOrganizationTypeAndPackagePlan(elastioOrganizationDTO.getName(), OrganizationType.Organization, elastioPackagePlan);
		}
		return existingItemOpt;
	}

	private double calculateElastioStoragePrice(double storage) {
		double result = 0;

		List<ElastioPricingOption> pricingPlan = getPricingPlan();
		double calculatedStorage = 0;
		for (int i = 0; i < pricingPlan.size(); i++) {
			ElastioPricingOption option = pricingPlan.get(i);
			double storageToPay = 0;

			if (storage < option.getStorage()) {
				result += (storage - calculatedStorage) * option.getPrice() * 1024 * 12;
				break;
			} else {
				if (i == pricingPlan.size() - 1) {
					result += (storage - calculatedStorage) * option.getPrice() * 1024 * 12;
				} else {
					result += (option.getStorage() - calculatedStorage) * option.getPrice() * 1024 * 12;
				}
			}
		}

		return result;
	}

	private List<ElastioPricingOption> getPricingPlan() {
		/*
		100 TB	$0.0490
		250 TB	$0.0250
		500 TB	$0.0150
		1 PB	$0.0080
		5 PB	$0.0065
		10 PB	$0.0060
		20 PB	$0.0055
		 */
		return List.of(
				new ElastioPricingOption(100D, 0.049),
				new ElastioPricingOption(250D, 0.025),
				new ElastioPricingOption(500D, 0.015),
				new ElastioPricingOption(1000D, 0.008),
				new ElastioPricingOption(5000D, 0.0065),
				new ElastioPricingOption(10000D, 0.006),
				new ElastioPricingOption(20000D, 0.0055)
			);
	}

	@Data
	public static class ElastioPricingOption {
		private Double storage;
		private Double price;

		public ElastioPricingOption(Double storage, Double price) {
			this.storage = storage;
			this.price = price;
		}
	}

}
