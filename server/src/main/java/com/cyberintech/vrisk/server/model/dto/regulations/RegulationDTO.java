package com.cyberintech.vrisk.server.model.dto.regulations;

import com.cyberintech.vrisk.server.model.dto.DTOWithMetaData;
import com.cyberintech.vrisk.server.model.dto.country.CountryViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.organization.IndustryRefDTO;
import com.cyberintech.vrisk.server.model.dto.state.StateViewDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.Regulations;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @author Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version 0.1.1
 * @since 2020-10-12
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class RegulationDTO extends DTOWithMetaData<Regulations> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private List<DataTypeClassificationRefDTO> dataTypes;

	@Schema
	private List<IndustryRefDTO> industries;

	@Schema
	private List<TechnologyCategoryRefDTO> technologyCategories;

	@Schema
	private List<CountryViewDTO> countries;

	@Schema
	private List<StateViewDTO> states;

	@Schema
	private List<StateViewDTO> stateLevelPreemptions;

	@Schema
	private Boolean isDataType;

	@Schema
	private Boolean isIndustry;

	@Schema
	private Boolean isTechnology;

	@Schema
	private Boolean isGeography;

	@Schema
	private RegulatoryJurisdictionType jurisdictionType;

	@Schema
	private String acronym;

	@Schema
	private String description;

	@Schema
	private String body;

	@Schema
	private DocumentDTO document;

	@Schema
	private String regulatedParty;

	@Schema
	private String protectedParty;

	@Schema
	private String protectedInformation;

	private Long notificationRequirements;

	private String urlForNotification;

	private Date dateEnacted;

	private Date dateUpdated;

	private String currentVersion;

	private RegulatoryType regulatoryType;

	private Boolean criminalPenalties;

	private String criminalPenaltiesComment;

	private Boolean civilPenalties;

	private String civilPenaltiesComment;

	private Boolean exemptionForEmployee;

	private Boolean nonProfitsCovered;

	private List<IndustryRefDTO> sectoralLawsCarveout;

	private String stateLevelPreemption;

	private Boolean dataBreachNotification;

	private Boolean dataMinimization;

	private Boolean dataProtectionOfficer;

	private Boolean internationalDataTransferRestrictions;

	private Boolean legalBasisOfProcessing;

	private Boolean noticeTransparencyRequirements;

	private Boolean privacyByDesign;

	private Boolean processorServiceProviderRequirements;

	private Boolean prohibitionOnDiscrimination;

	private Boolean purposeLimitation;

	private Boolean recordsOfProcessing;

	private Boolean registrationWithAuthorities;

	private Boolean finingAuthority;

	private Boolean privateRightOfAction;

	private Boolean ruleMakingAuthority;

	private Boolean dataSecurityRequirements;

	private Boolean exercisingConsumerRights;

	private Boolean noticesOfChangesToPrivacyPolicy;

	private Boolean processingAgreementsRequiredBtwControllersAndServiceProviders;

	private Boolean protectionsForSensitivePI;

	private Boolean protectionAssessments;

	private Boolean transparencyAndPurposeRequirements;

	private Boolean universalOptOutMechanism;

	private Boolean ageBasedOptInRight;

	private List<RegulationAgeDTO> regulationAges;


	private Boolean rightOfNotToBeSubjectedToFullyAutomatedDecisionMaking;
	private Boolean rightToAccess;
	private Boolean rightToAppeal;
	private Boolean rightToConsent;
	private Boolean rightToCorrect;
	private Boolean rightToDataPortability;
	private Boolean rightToDelete;
	private Boolean rightToNotBeDiscriminatedAgainst;
	private Boolean rightToOptInOfAllSensitiveDataProcessing;
	private Boolean rightToOptDownOfAllSpecificProcessing;
	private Boolean rightToPortability;
	private Boolean rightToPrivateAction;
	private Boolean rightToRefreshConsent;
	private Boolean rightToReSeekConsent;
	private Boolean rightToRestrictOfProcessing;
	private Boolean rightToRevokeConsent;
	private Boolean rightToObject;


	private Boolean respondingToRequests;

	private String typeOfDisclosure;

	private String disclosureSpecifications;

	private Long disclosureTimeRequirements;

	private String disclosureTimeUnit;

	private Boolean disclosureTimeRequirementsDays;

	private String disclosureTypeRequirements;

	private String aggregatedOrDeidetifiedDataDefinition;

	private String aggregatedOrDeidetifiedDataUse;

	private String classification;

	@Schema
	private List<DataTypeClassificationRefDTO> classifications;

	private Long dataRetention;

	private String dataRetentionTimeUnit;

	private String purposeLimitations;

	private String accessControl;

	private String encryptionRequirements;

	private String notificationRequirementsText;

	private CadenceType penTestCadence;

	private CadenceType vulnerabilityScanCadence;

	private CadenceType policiesAndProceduresReview;

	private CadenceType riskImpactAssessments;

	private String detailsOfVendorProgram;

	private String vendorDueDiligenceRequirements;

	private Boolean mfa;

	// New Fields
	private Boolean independentEnforcementAuthority;
	private Long responseToConsumerRequests;
	private Long curePeriod;
	private Boolean dataSecurity;
	private Boolean transparencyAndPurposeSpecification;
	private Date dateEnforced;
	private Boolean consent;
	private Boolean revokeConsent;
	private Boolean appeal;
	private Boolean rightOfPrivateAction;
	private Boolean optOutOfSale;


	private Double revenueRequirement;
	private Long personalDataProcessedNumber;
	private Long personalDataGrossRevenuePercent;
	private Long personalDataProcessedNumberForGrossRevenue;
	private EntryLevelExemptionsType entryLevelExemptions;
	private DataSpecificExemptionsType dataSpecificExemptions;

	private Map<String, Object> additionalProperties;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public RegulationDTO(Regulations entity) {
		super(entity);
	}

	@Override
	public void fromEntity(Regulations entity) {
		setMetadataFromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		isDataType = entity.getIsDataType();
		isIndustry = entity.getIsIndustry();
		isTechnology = entity.getIsTechnology();
		isGeography = entity.getIsGeography();
		jurisdictionType = entity.getJurisdictionType();
		acronym = entity.getAcronym();
		description = entity.getDescription();
		body = entity.getBody();
		regulatedParty = entity.getRegulatedParty();
		protectedParty = entity.getProtectedParty();
		protectedInformation = entity.getProtectedInformation();

		notificationRequirements = entity.getNotificationRequirements();
		urlForNotification = entity.getUrlForNotification();
		dateEnacted = entity.getDateEnacted();
		dateUpdated = entity.getDateUpdated();
		currentVersion = entity.getCurrentVersion();

		dataTypes = Optional.ofNullable(entity.getDataTypes()).orElse(new HashSet<>()).stream().map(DataTypeClassificationRefDTO::new).collect(Collectors.toList());
		classifications = Optional.ofNullable(entity.getClassifications()).orElse(new HashSet<>()).stream().map(DataTypeClassificationRefDTO::new).collect(Collectors.toList());
		industries = Optional.ofNullable(entity.getIndustries()).orElse(new HashSet<>()).stream().map(IndustryRefDTO::new).collect(Collectors.toList());
		sectoralLawsCarveout = Optional.ofNullable(entity.getSectoralLawsCarveout()).orElse(new HashSet<>()).stream().map(IndustryRefDTO::new).collect(Collectors.toList());
		technologyCategories = Optional.ofNullable(entity.getTechnologyCategories()).orElse(new HashSet<>()).stream().map(TechnologyCategoryRefDTO::new).collect(Collectors.toList());
		countries = Optional.ofNullable(entity.getCountries()).orElse(new HashSet<>()).stream().map(CountryViewDTO::new).collect(Collectors.toList());
		states = Optional.ofNullable(entity.getStates()).orElse(new HashSet<>()).stream().map(StateViewDTO::new).collect(Collectors.toList());
		stateLevelPreemptions = Optional.ofNullable(entity.getStateLevelPreemptions()).orElse(new HashSet<>()).stream().map(StateViewDTO::new).collect(Collectors.toList());

		regulatoryType = entity.getRegulatoryType();
		criminalPenalties = entity.getCriminalPenalties();
		criminalPenaltiesComment = entity.getCriminalPenaltiesComment();
		civilPenalties = entity.getCivilPenalties();
		civilPenaltiesComment = entity.getCivilPenaltiesComment();
		exemptionForEmployee = entity.getExemptionForEmployee();
		nonProfitsCovered = entity.getNonProfitsCovered();
		// sectoralLawsCarveout = entity.getSectoralLawsCarveout();
		stateLevelPreemption = entity.getStateLevelPreemption();
		dataBreachNotification = entity.getDataBreachNotification();
		dataMinimization = entity.getDataMinimization();
		dataProtectionOfficer = entity.getDataProtectionOfficer();
		internationalDataTransferRestrictions = entity.getInternationalDataTransferRestrictions();
		legalBasisOfProcessing = entity.getLegalBasisOfProcessing();
		noticeTransparencyRequirements = entity.getNoticeTransparencyRequirements();
		privacyByDesign = entity.getPrivacyByDesign();
		processorServiceProviderRequirements = entity.getProcessorServiceProviderRequirements();
		prohibitionOnDiscrimination = entity.getProhibitionOnDiscrimination();
		purposeLimitation = entity.getPurposeLimitation();
		recordsOfProcessing = entity.getRecordsOfProcessing();
		registrationWithAuthorities = entity.getRegistrationWithAuthorities();
		finingAuthority = entity.getFiningAuthority();
		privateRightOfAction = entity.getPrivateRightOfAction();
		ruleMakingAuthority = entity.getRuleMakingAuthority();

		dataSecurityRequirements = entity.getDataSecurityRequirements();
		exercisingConsumerRights = entity.getExercisingConsumerRights();
		noticesOfChangesToPrivacyPolicy = entity.getNoticesOfChangesToPrivacyPolicy();
		processingAgreementsRequiredBtwControllersAndServiceProviders = entity.getProcessingAgreementsRequiredBtwControllersAndServiceProviders();
		protectionsForSensitivePI = entity.getProtectionsForSensitivePI();
		protectionAssessments = entity.getProtectionAssessments();
		transparencyAndPurposeRequirements = entity.getTransparencyAndPurposeRequirements();
		universalOptOutMechanism = entity.getUniversalOptOutMechanism();

		ageBasedOptInRight = entity.getAgeBasedOptInRight();
		regulationAges = Optional.ofNullable(entity.getRegulationAges()).orElse(new HashSet<>()).stream().map(RegulationAgeDTO::new).collect(Collectors.toList());

		rightOfNotToBeSubjectedToFullyAutomatedDecisionMaking = entity.getRightOfNotToBeSubjectedToFullyAutomatedDecisionMaking();
		rightToAccess = entity.getRightToAccess();
		rightToAppeal = entity.getRightToAppeal();
		rightToConsent = entity.getRightToConsent();
		rightToCorrect = entity.getRightToCorrect();
		rightToDataPortability = entity.getRightToDataPortability();
		rightToDelete = entity.getRightToDelete();
		rightToNotBeDiscriminatedAgainst = entity.getRightToNotBeDiscriminatedAgainst();
		rightToOptInOfAllSensitiveDataProcessing = entity.getRightToOptInOfAllSensitiveDataProcessing();
		rightToOptDownOfAllSpecificProcessing = entity.getRightToOptDownOfAllSpecificProcessing();
		rightToPortability = entity.getRightToPortability();
		rightToPrivateAction = entity.getRightToPrivateAction();
		rightToRefreshConsent = entity.getRightToRefreshConsent();
		rightToReSeekConsent = entity.getRightToReSeekConsent();
		rightToRestrictOfProcessing = entity.getRightToRestrictOfProcessing();
		rightToRevokeConsent = entity.getRightToRevokeConsent();
		rightToObject = entity.getRightToObject();

		respondingToRequests = entity.getRespondingToRequests();
		typeOfDisclosure = entity.getTypeOfDisclosure();
		disclosureSpecifications = entity.getDisclosureSpecifications();
		disclosureTimeRequirements = entity.getDisclosureTimeRequirements();
		if (entity.getDisclosureTimeUnit() != null) disclosureTimeUnit = entity.getDisclosureTimeUnit().name();
		disclosureTimeRequirementsDays = entity.getDisclosureTimeRequirementsDays();
		disclosureTypeRequirements = entity.getDisclosureTypeRequirements();
		aggregatedOrDeidetifiedDataDefinition = entity.getAggregatedOrDeidetifiedDataDefinition();
		aggregatedOrDeidetifiedDataUse = entity.getAggregatedOrDeidetifiedDataUse();
		classification = entity.getClassification();
		dataRetention = entity.getDataRetention();
		if (entity.getDataRetentionTimeUnit() != null) dataRetentionTimeUnit = entity.getDataRetentionTimeUnit().name();
		purposeLimitations = entity.getPurposeLimitations();
		accessControl = entity.getAccessControl();
		encryptionRequirements = entity.getEncryptionRequirements();
		notificationRequirementsText = entity.getNotificationRequirementsText();
		penTestCadence = entity.getPenTestCadence();
		vulnerabilityScanCadence = entity.getVulnerabilityScanCadence();
		policiesAndProceduresReview = entity.getPoliciesAndProceduresReview();
		riskImpactAssessments = entity.getRiskImpactAssessments();
		detailsOfVendorProgram = entity.getDetailsOfVendorProgram();
		vendorDueDiligenceRequirements = entity.getVendorDueDiligenceRequirements();
		mfa = entity.getMfa();

		revenueRequirement = entity.getRevenueRequirement();
		personalDataProcessedNumber = entity.getPersonalDataProcessedNumber();
		personalDataGrossRevenuePercent = entity.getPersonalDataGrossRevenuePercent();
		personalDataProcessedNumberForGrossRevenue = entity.getPersonalDataProcessedNumberForGrossRevenue();
		entryLevelExemptions = entity.getEntryLevelExemptions();
		dataSpecificExemptions = entity.getDataSpecificExemptions();

		if (entity.getDocument() != null) {
			document = new DocumentDTO(entity.getDocument());
		}

		additionalProperties = entity.getAdditionalProperties();
		independentEnforcementAuthority = entity.getIndependentEnforcementAuthority();
		responseToConsumerRequests = entity.getResponseToConsumerRequests();
		curePeriod = entity.getCurePeriod();

	}
}
