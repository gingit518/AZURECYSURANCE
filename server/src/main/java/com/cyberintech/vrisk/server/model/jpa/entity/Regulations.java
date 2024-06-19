package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.converters.MapOfObjectsConverter;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Description
 *
 * @author Daniel A. Kolesnik <dkolesnik@dfusiontech.com>
 * @version 0.1.1
 * @since 2020-10-12
 */
@Entity
@Table(name = "regulations")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"})
public class Regulations implements IEntityWithMetadata {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "is_data_type")
	private Boolean isDataType;

	@Column(name = "is_industry")
	private Boolean isIndustry;

	@Column(name = "is_technology")
	private Boolean isTechnology;

	@Column(name = "is_geography")
	private Boolean isGeography;

	@Enumerated(EnumType.STRING)
	@Column(name = "jurisdiction_type")
	private RegulatoryJurisdictionType jurisdictionType;

	@Column(name = "acronym")
	private String acronym;

	@Column(name = "description")
	private String description;

	@Column(name = "body")
	private String body;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "document_id")
	private Documents document;

	@Column(name = "regulated_party")
	private String regulatedParty;

	@Column(name = "protected_party")
	private String protectedParty;

	@Column(name = "protected_information")
	private String protectedInformation;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "regulations_to_data_type_classifications",
		joinColumns = {@JoinColumn(name = "regulation_id")},
		inverseJoinColumns = {@JoinColumn(name = "data_type_classification_id")}
	)
	private Set<DataTypeClassification> dataTypes = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "regulations_to_industries",
		joinColumns = {@JoinColumn(name = "regulation_id")},
		inverseJoinColumns = {@JoinColumn(name = "industry_id")}
	)
	private Set<Industries> industries = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "regulations_to_technology_categories",
		joinColumns = {@JoinColumn(name = "regulation_id")},
		inverseJoinColumns = {@JoinColumn(name = "technology_category_id")}
	)
	private Set<TechnologyCategories> technologyCategories = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "regulations_to_countries",
		joinColumns = {@JoinColumn(name = "regulation_id")},
		inverseJoinColumns = {@JoinColumn(name = "country_id")}
	)
	private Set<Country> countries = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "regulations_to_states",
		joinColumns = {@JoinColumn(name = "regulation_id")},
		inverseJoinColumns = {@JoinColumn(name = "state_id")}
	)
	private Set<State> states = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "created_by_id")
	private Users createdBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "updated_by_id")
	private Users updatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

	@Column(name = "notification_requirements")
	private Long notificationRequirements;

	@Column(name = "url_for_notification")
	private String urlForNotification;

	@Column(name = "date_enacted")
	private Date dateEnacted;

	@Column(name = "date_updated")
	private Date dateUpdated;

	@Column(name = "current_version")
	private String currentVersion;

	@Column(name = "regulatory_type")
	private RegulatoryType regulatoryType;

	@Column(name = "criminal_penalties")
	private Boolean criminalPenalties;

	@Column(name = "criminal_penalties_comment")
	private String criminalPenaltiesComment;

	@Column(name = "civil_penalties")
	private Boolean civilPenalties;

	@Column(name = "civil_penalties_comment")
	private String civilPenaltiesComment;

	@Column(name = "exemption_for_employee")
	private Boolean exemptionForEmployee;

	@Column(name = "non_profits_covered")
	private Boolean nonProfitsCovered;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "regulations_to_carveout",
		joinColumns = {@JoinColumn(name = "regulation_id")},
		inverseJoinColumns = {@JoinColumn(name = "industry_id")}
	)
	private Set<Industries> sectoralLawsCarveout = new HashSet<>();

	@Column(name = "state_level_preemption")
	private String stateLevelPreemption;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "regulations_to_state_level_preemptions",
		joinColumns = {@JoinColumn(name = "regulation_id")},
		inverseJoinColumns = {@JoinColumn(name = "state_id")}
	)
	private Set<State> stateLevelPreemptions = new HashSet<>();

	@Column(name = "data_breach_notification")
	private Boolean dataBreachNotification;

	@Column(name = "data_minimization")
	private Boolean dataMinimization;

	@Column(name = "data_protection_officer")
	private Boolean dataProtectionOfficer;

	@Column(name = "international_data_transfer_restrictions")
	private Boolean internationalDataTransferRestrictions;

	@Column(name = "legal_basis_of_processing")
	private Boolean legalBasisOfProcessing;

	@Column(name = "notice_transparency_requirements")
	private Boolean noticeTransparencyRequirements;

	@Column(name = "privacy_by_design")
	private Boolean privacyByDesign;

	@Column(name = "processor_service_provider_requirements")
	private Boolean processorServiceProviderRequirements;

	@Column(name = "prohibition_on_discrimination")
	private Boolean prohibitionOnDiscrimination;

	@Column(name = "purpose_limitation")
	private Boolean purposeLimitation;

	@Column(name = "records_of_processing")
	private Boolean recordsOfProcessing;

	@Column(name = "registration_with_authorities")
	private Boolean registrationWithAuthorities;

	@Column(name = "fining_authority")
	private Boolean finingAuthority;

	@Column(name = "private_right_of_action")
	private Boolean privateRightOfAction;

	@Column(name = "rule_making_authority")
	private Boolean ruleMakingAuthority;

	@Column(name = "data_security_requirements")
	private Boolean dataSecurityRequirements;

	@Column(name = "exercising_consumer_rights")
	private Boolean exercisingConsumerRights;

	@Column(name = "notices_of_changes_to_privacy_policy")
	private Boolean noticesOfChangesToPrivacyPolicy;

	@Column(name = "processing_agreements_required_btw_controllers_and_service_providers")
	private Boolean processingAgreementsRequiredBtwControllersAndServiceProviders;

	@Column(name = "protections_for_sensitive_pi")
	private Boolean protectionsForSensitivePI;

	@Column(name = "protection_assessments")
	private Boolean protectionAssessments;

	@Column(name = "transparency_and_purpose_requirements")
	private Boolean transparencyAndPurposeRequirements;

	@Column(name = "universal_opt_out_mechanism")
	private Boolean universalOptOutMechanism;

	@Column(name = "age_based_opt_in_right")
	private Boolean ageBasedOptInRight;

	@OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinColumn(name = "regulation_id")
	// @OneToMany(mappedBy = "regulation", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<RegulationAges> regulationAges = new HashSet<>();

	@Column(name = "right_of_not_to_be_subjected_to_fully_automated_decision_making")
	private Boolean rightOfNotToBeSubjectedToFullyAutomatedDecisionMaking;

	@Column(name = "right_to_access")
	private Boolean rightToAccess;

	@Column(name = "right_to_appeal")
	private Boolean rightToAppeal;

	@Column(name = "right_to_consent")
	private Boolean rightToConsent;

	@Column(name = "right_to_correct")
	private Boolean rightToCorrect;

	@Column(name = "right_to_data_portability")
	private Boolean rightToDataPortability;

	@Column(name = "right_to_delete")
	private Boolean rightToDelete;

	@Column(name = "right_to_not_be_discriminated_against")
	private Boolean rightToNotBeDiscriminatedAgainst;

	@Column(name = "right_to_opt_in_of_all_sensitive_data_processing")
	private Boolean rightToOptInOfAllSensitiveDataProcessing;

	@Column(name = "right_to_opt_down_of_all_specific_processing")
	private Boolean rightToOptDownOfAllSpecificProcessing;

	@Column(name = "right_to_portability")
	private Boolean rightToPortability;

	@Column(name = "right_to_private_action")
	private Boolean rightToPrivateAction;

	@Column(name = "right_to_refresh_consent")
	private Boolean rightToRefreshConsent;

	@Column(name = "right_to_re_seek_consent")
	private Boolean rightToReSeekConsent;

	@Column(name = "right_to_restrict_of_processing")
	private Boolean rightToRestrictOfProcessing;

	@Column(name = "right_to_revoke_consent")
	private Boolean rightToRevokeConsent;

	@Column(name = "right_to_object")
	private Boolean rightToObject;

	@Column(name = "responding_to_requests")
	private Boolean respondingToRequests;

	@Column(name = "type_of_disclosure")
	private String typeOfDisclosure;

	@Column(name = "disclosure_specifications")
	private String disclosureSpecifications;

	@Column(name = "disclosure_time_requirements")
	private Long disclosureTimeRequirements;

	@Column(name = "disclosure_time_unit")
	private TimeUnitType disclosureTimeUnit;

	@Column(name = "disclosure_time_requirements_days")
	private Boolean disclosureTimeRequirementsDays;

	@Column(name = "disclosure_type_requirements")
	private String disclosureTypeRequirements;

	@Column(name = "aggregated_or_deidentified_data_definition")
	private String aggregatedOrDeidetifiedDataDefinition;

	@Column(name = "aggregated_or_deidentified_data_use")
	private String aggregatedOrDeidetifiedDataUse;

	@Column(name = "classification")
	private String classification;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "regulations_to_classifications",
		joinColumns = {@JoinColumn(name = "regulation_id")},
		inverseJoinColumns = {@JoinColumn(name = "data_type_classification_id")}
	)
	private Set<DataTypeClassification> classifications = new HashSet<>();

	@Column(name = "data_retention")
	private Long dataRetention;

	@Column(name = "data_retention_time_unit")
	private TimeUnitType dataRetentionTimeUnit;

	@Column(name = "purpose_limitations")
	private String purposeLimitations;

	@Column(name = "access_control")
	private String accessControl;

	@Column(name = "encryption_requirements")
	private String encryptionRequirements;

	@Column(name = "notification_requirements_text")
	private String notificationRequirementsText;

	@Enumerated(EnumType.STRING)
	@Column(name = "pen_test_cadence")
	private CadenceType penTestCadence;

	@Enumerated(EnumType.STRING)
	@Column(name = "vulnerability_scan_cadence")
	private CadenceType vulnerabilityScanCadence;

	@Enumerated(EnumType.STRING)
	@Column(name = "policies_and_procedures_review")
	private CadenceType policiesAndProceduresReview;

	@Enumerated(EnumType.STRING)
	@Column(name = "risk_impact_assessments")
	private CadenceType riskImpactAssessments;

	@Column(name = "details_of_vendor_program")
	private String detailsOfVendorProgram;

	@Column(name = "vendor_due_diligence_requirements")
	private String vendorDueDiligenceRequirements;

	@Column(name = "mfa")
	private Boolean mfa;

	@Column(name = "revenue_requirement")
	private Double revenueRequirement;

	@Column(name = "personal_data_processed_number")
	private Long personalDataProcessedNumber;

	@Column(name = "personal_data_gross_revenue_percent")
	private Long personalDataGrossRevenuePercent;

	@Column(name = "personal_data_processed_number_for_gross_revenue")
	private Long personalDataProcessedNumberForGrossRevenue;

	@Column(name = "cure_period")
	private Long curePeriod;

	@Column(name = "response_to_consumer_requests")
	private Long responseToConsumerRequests;

	@Column(name = "independent_enforcement_authority")
	private Boolean independentEnforcementAuthority;

	@Enumerated(EnumType.STRING)
	@Column(name = "entry_level_exemptions")
	private EntryLevelExemptionsType entryLevelExemptions;

	@Enumerated(EnumType.STRING)
	@Column(name = "data_specific_exemptions")
	private DataSpecificExemptionsType dataSpecificExemptions;


	@SuppressWarnings("JpaAttributeTypeInspection")
	@Column(name = "additional_properties")
	@Convert(converter = MapOfObjectsConverter.class)
	private Map<String, Object> additionalProperties;

}
