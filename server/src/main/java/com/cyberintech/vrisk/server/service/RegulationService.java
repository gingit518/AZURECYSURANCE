package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.data.NameFilter;
import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.regulations.RegulationAgeDTO;
import com.cyberintech.vrisk.server.model.dto.regulations.RegulationDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.TimeUnitType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.Documents;
import com.cyberintech.vrisk.server.model.jpa.entity.RegulationAges;
import com.cyberintech.vrisk.server.model.jpa.entity.Regulations;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.ConflictException;
import com.cyberintech.vrisk.server.rest.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;

/**
 * Description
 *
 * @author Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version 0.1.1
 * @since 2020-10-12
 */
@Service
public class RegulationService {

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private DataTypeClassificationRepository dataTypeClassificationRepository;

	@Autowired
	private IndustryRepository industryRepository;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private RegulationRepository regulationRepository;

	@Autowired
	private TechnologyRepository technologyRepository;

	@Autowired
	private TechnologyCategoryRepository technologyCategoryRepository;

	@Autowired
	private UserService userService;
	@Autowired
	private EnvironmentTypesRepository environmentTypesRepository;

	@Autowired
	private RegulationAgesRepository regulationAgesRepository;
	@Autowired
	private DocumentsRepository documentsRepository;

	/**
	 * Get Regulations List
	 *
	 * @return Regulations List
	 */
	public List<RegulationDTO> getList() {
		List<Regulations> items = regulationRepository.findAll();

		List<RegulationDTO> itemDTOs = DTOBase.fromEntitiesList(items, RegulationDTO.class);

		return itemDTOs;
	}

	/**
	 * Get Data Type Classifications List
	 *
	 * @return Users List
	 */
	public FilteredResponse<NameFilter, RegulationDTO> getListFiltered(FilteredRequest<NameFilter> filteredRequest) {
		FilteredResponse<NameFilter, RegulationDTO> filteredResponse = new FilteredResponse<NameFilter, RegulationDTO>(filteredRequest);

		String namePattern = "";
		if (filteredRequest.getFilter() != null && filteredRequest.getFilter().getName() != null) {
			namePattern = filteredRequest.getFilter().getName();
		}

		List<Regulations> items = regulationRepository.getListByName(namePattern, filteredRequest.toPageRequest());
		Long count = regulationRepository.getCountByName(namePattern);

		List<RegulationDTO> itemsDTOList = RegulationDTO.fromEntitiesList(items, RegulationDTO.class);

		filteredResponse.setItems(itemsDTOList);
		filteredResponse.setTotal(count.intValue());

		return filteredResponse;
	}

	/**
	 * Get Regulation details
	 *
	 * @param itemId
	 * @return Regulation Details
	 */
	public RegulationDTO getDetails(Long itemId) {
		Regulations itemDetails = getItem(itemId);

		RegulationDTO result = new RegulationDTO(itemDetails);
		if (itemDetails.getDocument() != null) {
			result.setDocument(new DocumentDTO(itemDetails.getDocument(), true));
		}

		return result;
	}

	/**
	 * Get Regulation details
	 *
	 * @param itemId
	 * @return Regulation Details
	 */
	public Regulations getItem(Long itemId) {
		Regulations itemDetails;

		try {
			itemDetails = regulationRepository.findById(itemId).get();

		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Regulation not found in the database [{0}]", itemId));
		}

		return itemDetails;
	}

	/**
	 * Create new Regulation
	 *
	 * @param newItemDTO
	 * @return New Regulation Details
	 */
	@Transactional
	public RegulationDTO create(RegulationDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		Regulations newItem = new Regulations();
		newItem.setCreatedBy(userService.getCurrentUserEntity());
		newItem.setCreatedAt(new Date());

		applyEntityChanges(newItemDTO, newItem);

		// Save to the database
		Regulations savedResult = regulationRepository.save(newItem);
		// RegulationAges newRegulationAge = new RegulationAges();
		// newItemDTO.setId(savedResult.getId());
		// applyRegulationAgesChanges(newItemDTO, newRegulationAge);
		// RegulationAges savedRegulationAge = regulationAgesRepository.save(newRegulationAge);

		RegulationDTO result = new RegulationDTO(savedResult);
		// List<RegulationAgeDTO> savedRegulationAges = new ArrayList<>();
		// savedRegulationAges.add(new RegulationAgeDTO(savedRegulationAge));
		// result.setRegulationAges(savedRegulationAges);
		// Save Audit Log CREATE event
		auditLogService.create(
			VItemType.REGULATION,
			savedResult.getId(),
			result
		);

		return result;
	}

	/**
	 * Update Regulation
	 *
	 * @param itemDTO
	 * @return Updated Regulation Details
	 */
	@Transactional
	public RegulationDTO update(RegulationDTO itemDTO) {

		// Get Existing item from the database
		Regulations existingItem = getItem(itemDTO.getId());
		RegulationDTO existingItemDTO = new RegulationDTO(existingItem);

		// Update item details
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		Regulations savedResult = regulationRepository.save(existingItem);
		// RegulationAges updatedRegulationAge = new RegulationAges();
		// applyRegulationAgesChanges(itemDTO, updatedRegulationAge);
		// regulationAgesRepository.save(updatedRegulationAge);
		// List<RegulationAgeDTO> updatedRegulationAges = new ArrayList<>(itemDTO.getRegulationAges());

		RegulationDTO result = new RegulationDTO(savedResult);
		// result.setRegulationAges(updatedRegulationAges);

		// Save Audit Log UPDATE event
		auditLogService.update(
			VItemType.REGULATION,
			savedResult.getId(),
			existingItemDTO,
			result
		);

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(RegulationDTO itemDTO, Regulations entity) {
		entity.setName(itemDTO.getName());

		entity.setJurisdictionType(itemDTO.getJurisdictionType());
		entity.setAcronym(itemDTO.getAcronym());
		entity.setDescription(itemDTO.getDescription());
		entity.setBody(itemDTO.getBody());
		entity.setRegulatedParty(itemDTO.getRegulatedParty());
		entity.setProtectedParty(itemDTO.getProtectedParty());
		entity.setProtectedInformation(itemDTO.getProtectedInformation());

		entity.setNotificationRequirements(itemDTO.getNotificationRequirements());
		entity.setUrlForNotification(itemDTO.getUrlForNotification());
		entity.setDateEnacted(itemDTO.getDateEnacted());
		entity.setDateUpdated(itemDTO.getDateUpdated());
		entity.setCurrentVersion(itemDTO.getCurrentVersion());
		entity.setRegulatoryType(itemDTO.getRegulatoryType());

		// Update Data Types
		Optional.ofNullable(itemDTO.getDataTypes()).ifPresent(itemsList -> {
			entity.setDataTypes(new HashSet<>());
			itemsList.stream().forEach(itemRef -> {
				entity.getDataTypes().add(dataTypeClassificationRepository.findById(itemRef.getId()).get());
			});
			entity.setIsDataType(entity.getDataTypes().size() > 0);
		});

		// Update Data Types
		Optional.ofNullable(itemDTO.getClassifications()).ifPresent(itemsList -> {
			entity.setClassifications(new HashSet<>());
			itemsList.stream().forEach(itemRef -> {
				entity.getClassifications().add(dataTypeClassificationRepository.findById(itemRef.getId()).get());
			});
		});

		// Update Industries
		Optional.ofNullable(itemDTO.getIndustries()).ifPresent(itemsList -> {
			entity.setIndustries(new HashSet<>());
			itemsList.stream().forEach(itemRef -> {
				entity.getIndustries().add(industryRepository.findById(itemRef.getId()).get());
			});
			entity.setIsIndustry(entity.getIndustries().size() > 0);
		});

		// Update Industries
		Optional.ofNullable(itemDTO.getSectoralLawsCarveout()).ifPresent(itemsList -> {
			entity.setSectoralLawsCarveout(new HashSet<>());
			itemsList.stream().forEach(itemRef -> {
				entity.getSectoralLawsCarveout().add(industryRepository.findById(itemRef.getId()).get());
			});
		});

		// Update Technology Categories
		Optional.ofNullable(itemDTO.getTechnologyCategories()).ifPresent(itemsList -> {
			entity.setTechnologyCategories(new HashSet<>());
			itemsList.stream().forEach(itemRef -> {
				entity.getTechnologyCategories().add(technologyCategoryRepository.findById(itemRef.getId()).get());
			});
			entity.setIsTechnology(entity.getTechnologyCategories().size() > 0);
		});

		// Update Geography
		Optional.ofNullable(itemDTO.getCountries()).ifPresent(itemsList -> {
			entity.setCountries(new HashSet<>());
			itemsList.stream().forEach(itemRef -> {
				entity.getCountries().add(countryRepository.findById(itemRef.getId()).get());
			});
		});
		Optional.ofNullable(itemDTO.getStates()).ifPresent(itemsList -> {
			entity.setStates(new HashSet<>());
			itemsList.stream().forEach(itemRef -> {
				entity.getStates().add(stateRepository.findById(itemRef.getId()).get());
			});
		});
		entity.setIsGeography(entity.getCountries().size() > 0 || entity.getStates().size() > 0);

		// Set State Level pre-emption
		Optional.ofNullable(itemDTO.getStateLevelPreemptions()).ifPresent(itemsList -> {
			entity.setStateLevelPreemptions(new HashSet<>());
			itemsList.stream().forEach(itemRef -> {
				entity.getStateLevelPreemptions().add(stateRepository.findById(itemRef.getId()).get());
			});
		});

		entity.setUpdatedBy(userService.getCurrentUserEntity());
		entity.setUpdatedAt(new Date());


		entity.setCriminalPenalties(itemDTO.getCriminalPenalties());
		entity.setCriminalPenaltiesComment(itemDTO.getCriminalPenaltiesComment());
		entity.setCivilPenalties(itemDTO.getCivilPenalties());
		entity.setCivilPenaltiesComment(itemDTO.getCivilPenaltiesComment());
		entity.setExemptionForEmployee(itemDTO.getExemptionForEmployee());
		entity.setNonProfitsCovered(itemDTO.getNonProfitsCovered());
		// entity.setSectoralLawsCarveout(itemDTO.getSectoralLawsCarveout());
		entity.setStateLevelPreemption(itemDTO.getStateLevelPreemption());
		entity.setDataBreachNotification(itemDTO.getDataBreachNotification());
		entity.setDataMinimization(itemDTO.getDataMinimization());
		entity.setDataProtectionOfficer(itemDTO.getDataProtectionOfficer());
		entity.setInternationalDataTransferRestrictions(itemDTO.getInternationalDataTransferRestrictions());
		entity.setLegalBasisOfProcessing(itemDTO.getLegalBasisOfProcessing());
		entity.setNoticeTransparencyRequirements(itemDTO.getNoticeTransparencyRequirements());
		entity.setPrivacyByDesign(itemDTO.getPrivacyByDesign());
		entity.setProcessorServiceProviderRequirements(itemDTO.getProcessorServiceProviderRequirements());
		entity.setProhibitionOnDiscrimination(itemDTO.getProhibitionOnDiscrimination());
		entity.setPurposeLimitation(itemDTO.getPurposeLimitation());
		entity.setRecordsOfProcessing(itemDTO.getRecordsOfProcessing());
		entity.setRegistrationWithAuthorities(itemDTO.getRegistrationWithAuthorities());
		entity.setFiningAuthority(itemDTO.getFiningAuthority());
		entity.setPrivateRightOfAction(itemDTO.getPrivateRightOfAction());
		entity.setRuleMakingAuthority(itemDTO.getRuleMakingAuthority());
		entity.setAgeBasedOptInRight(itemDTO.getAgeBasedOptInRight());

		// Update Regulation Ages

		Optional.ofNullable(itemDTO.getRegulationAges()).ifPresent(itemsList -> {
			entity.setRegulationAges(new HashSet<>());
			itemsList.stream().filter(regulationAgeDTO -> regulationAgeDTO.getAge() != null && regulationAgeDTO.getAge() > 0).forEach(itemRef -> {
				if (itemRef.getId() != null) {
					entity.getRegulationAges().add(regulationAgesRepository.findById(itemRef.getId()).get());
				} else {
					RegulationAges regulationAge = new RegulationAges();
					regulationAge.setAge(itemRef.getAge());
					regulationAge.setComments(itemRef.getComments());
					regulationAge.setRegulation(entity);
					// regulationAge = regulationAgesRepository.save(regulationAge);
					entity.getRegulationAges().add(regulationAge);
				}
			});
		});
		// entity.setAgeBasedOptInRight(Optional.ofNullable(itemDTO.getRegulationAges()).isPresent());

		entity.setDataSecurityRequirements(itemDTO.getDataSecurityRequirements());
		entity.setExercisingConsumerRights(itemDTO.getExercisingConsumerRights());
		entity.setNoticesOfChangesToPrivacyPolicy(itemDTO.getNoticesOfChangesToPrivacyPolicy());
		entity.setProcessingAgreementsRequiredBtwControllersAndServiceProviders(itemDTO.getProcessingAgreementsRequiredBtwControllersAndServiceProviders());
		entity.setProtectionsForSensitivePI(itemDTO.getProtectionsForSensitivePI());
		entity.setProtectionAssessments(itemDTO.getProtectionAssessments());
		entity.setTransparencyAndPurposeRequirements(itemDTO.getTransparencyAndPurposeRequirements());
		entity.setUniversalOptOutMechanism(itemDTO.getUniversalOptOutMechanism());

		/*

		rightToRefreshConsent = entity.getRightToRefreshConsent();
		rightToReSeekConsent = entity.getRightToReSeekConsent();
		rightToRestrictOfProcessing = entity.getRightToRestrictOfProcessing();
		rightToRevokeConsent = entity.getRightToRevokeConsent();
		rightToObject = entity.getRightToObject();
		 */

		entity.setRightOfNotToBeSubjectedToFullyAutomatedDecisionMaking(itemDTO.getRightOfNotToBeSubjectedToFullyAutomatedDecisionMaking());
		entity.setRightToAccess(itemDTO.getRightToAccess());
		entity.setRightToAppeal(itemDTO.getRightToAppeal());
		entity.setRightToConsent(itemDTO.getRightToConsent());
		entity.setRightToCorrect(itemDTO.getRightToCorrect());
		entity.setRightToDataPortability(itemDTO.getRightToDataPortability());
		entity.setRightToDelete(itemDTO.getRightToDelete());
		entity.setRightToNotBeDiscriminatedAgainst(itemDTO.getRightToNotBeDiscriminatedAgainst());
		entity.setRightToOptInOfAllSensitiveDataProcessing(itemDTO.getRightToOptInOfAllSensitiveDataProcessing());
		entity.setRightToOptDownOfAllSpecificProcessing(itemDTO.getRightToOptDownOfAllSpecificProcessing());
		entity.setRightToPortability(itemDTO.getRightToPortability());
		entity.setRightToPrivateAction(itemDTO.getRightToPrivateAction());
		entity.setRightToRefreshConsent(itemDTO.getRightToRefreshConsent());
		entity.setRightToReSeekConsent(itemDTO.getRightToReSeekConsent());
		entity.setRightToRestrictOfProcessing(itemDTO.getRightToRestrictOfProcessing());
		entity.setRightToRevokeConsent(itemDTO.getRightToRevokeConsent());
		entity.setRightToObject(itemDTO.getRightToObject());

		entity.setRespondingToRequests(itemDTO.getRespondingToRequests());
		entity.setTypeOfDisclosure(itemDTO.getTypeOfDisclosure());
		entity.setDisclosureSpecifications(itemDTO.getDisclosureSpecifications());
		entity.setDisclosureTimeRequirements(itemDTO.getDisclosureTimeRequirements());
		entity.setDisclosureTimeRequirementsDays(itemDTO.getDisclosureTimeRequirementsDays());
		if (itemDTO.getDisclosureTimeUnit() != null) {
			if (TimeUnitType.Days.name().equalsIgnoreCase(itemDTO.getDisclosureTimeUnit())) {
				entity.setDisclosureTimeUnit(TimeUnitType.Days);
			} else if (TimeUnitType.Hours.name().equalsIgnoreCase(itemDTO.getDisclosureTimeUnit())) {
				entity.setDisclosureTimeUnit(TimeUnitType.Hours);
			}
		}
		entity.setDisclosureTypeRequirements(itemDTO.getDisclosureTypeRequirements());
		entity.setAggregatedOrDeidetifiedDataDefinition(itemDTO.getAggregatedOrDeidetifiedDataDefinition());
		entity.setAggregatedOrDeidetifiedDataUse(itemDTO.getAggregatedOrDeidetifiedDataUse());
		entity.setClassification(itemDTO.getClassification());
		entity.setDataRetention(itemDTO.getDataRetention());
		if (itemDTO.getDataRetentionTimeUnit() != null) {
			if (TimeUnitType.Months.name().equalsIgnoreCase(itemDTO.getDataRetentionTimeUnit())) {
				entity.setDataRetentionTimeUnit(TimeUnitType.Months);
			} else if (TimeUnitType.Years.name().equalsIgnoreCase(itemDTO.getDataRetentionTimeUnit())) {
				entity.setDataRetentionTimeUnit(TimeUnitType.Years);
			}
		}
		entity.setPurposeLimitations(itemDTO.getPurposeLimitations());
		entity.setAccessControl(itemDTO.getAccessControl());
		entity.setEncryptionRequirements(itemDTO.getEncryptionRequirements());
		entity.setNotificationRequirementsText(itemDTO.getNotificationRequirementsText());
		entity.setPenTestCadence(itemDTO.getPenTestCadence());
		entity.setVulnerabilityScanCadence(itemDTO.getVulnerabilityScanCadence());
		entity.setPoliciesAndProceduresReview(itemDTO.getPoliciesAndProceduresReview());
		entity.setRiskImpactAssessments(itemDTO.getRiskImpactAssessments());
		entity.setDetailsOfVendorProgram(itemDTO.getDetailsOfVendorProgram());
		entity.setVendorDueDiligenceRequirements(itemDTO.getVendorDueDiligenceRequirements());
		entity.setMfa(itemDTO.getMfa());

		entity.setRevenueRequirement(itemDTO.getRevenueRequirement());
		entity.setPersonalDataProcessedNumber(itemDTO.getPersonalDataProcessedNumber());
		entity.setPersonalDataGrossRevenuePercent(itemDTO.getPersonalDataGrossRevenuePercent());
		entity.setPersonalDataProcessedNumberForGrossRevenue(itemDTO.getPersonalDataProcessedNumberForGrossRevenue());
		entity.setEntryLevelExemptions(itemDTO.getEntryLevelExemptions());
		entity.setDataSpecificExemptions(itemDTO.getDataSpecificExemptions());

		entity.setAdditionalProperties(itemDTO.getAdditionalProperties());
		entity.setIndependentEnforcementAuthority(itemDTO.getIndependentEnforcementAuthority());
		entity.setResponseToConsumerRequests(itemDTO.getResponseToConsumerRequests());
		entity.setCurePeriod(itemDTO.getCurePeriod());

		if (itemDTO.getDocument() != null) {
			documentsRepository.findById(itemDTO.getDocument().getId()).ifPresent(entity::setDocument);
		}
	}

	/*
	private void applyRegulationAgesChanges(RegulationDTO itemDTO, RegulationAges regulationAges) {

		Optional.ofNullable(itemDTO.getRegulationAges()).ifPresent(itemsList -> {
			regulationAges.setRegulation(regulationRepository.findById(itemDTO.getId()).get());
			itemsList.stream().forEach(itemRef ->  {
				regulationAges.setId(itemRef.getId());
				regulationAges.setAge(itemRef.getAge());
				regulationAges.setComments(itemRef.getComments());
			});
		});
		}
	 */

	/**
	 * Delete Regulation
	 *
	 * @param itemId
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		Regulations existingItem = getItem(itemId);
		RegulationDTO existingItemDTO = new RegulationDTO(existingItem);

		regulationRepository.delete(existingItem);
		regulationRepository.flush();

		// Save Audit Log DELETE event
		auditLogService.delete(
			VItemType.REGULATION,
			existingItemDTO.getId(),
			existingItemDTO
		);

		return itemId;
	}
}
