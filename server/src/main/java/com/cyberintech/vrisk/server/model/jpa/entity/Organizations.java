package com.cyberintech.vrisk.server.model.jpa.entity;

import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentFrameworkLevel;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.TwoFactorType;
import com.cyberintech.vrisk.server.model.jpa.domains.elastio.PlatformAssetType;
import com.cyberintech.vrisk.server.model.jpa.domains.elastio.PlatformType;
import com.cyberintech.vrisk.server.model.jpa.entity.common.IMetadataAware;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Organization Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version 0.1.1
 * @since 2018-11-08
 */
@Entity
@Table(name = "organizations")
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"id", "name", "organizationType"})
@EqualsAndHashCode(of = {"id", "name"})
public class Organizations implements IMetadataAware<OrganizationsMetadata> {

	public static final double INSURANCE_LIMIT = 500000000d;
	public static final double DEFAULT_RECORD_PRICE = 196d;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "uid")
	private String uid;

	@Column(name = "name", unique = true, nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at")
	private Date updatedAt;

	@Column(name = "tax_id")
	private String taxId;

	@Column(name = "vat_id")
	private String vatId;

	@Enumerated(EnumType.STRING)
	@Column(name = "org_type")
	private OrganizationType organizationType;

	@Column(name = "street_address_1")
	private String streetAddress1;

	@Column(name = "street_address_2")
	private String streetAddress2;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "country_id")
	private Country country;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "state_id")
	private State state;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "city_id")
	private City city;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "currency_id")
	private Currency currency;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "language_id")
	private Language language;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "status_id")
	private Status status;

	@Column(name = "zip", length = 16)
	private String zip;

	@Column(name = "phone", length = 20)
	private String phone;

	@Column(name = "fax", length = 20)
	private String fax;

	@Column(name = "site", length = 255)
	private String site;

	@Column(name = "logo", length = 255)
	private String logo;

	@Column(name = "notes")
	private String notes;

	@Column(name = "logout_after_inactivity_time")
	private Long logoutAfterInactivityTime;

	@Column(name = "average_revenue")
	private Double averageRevenue;

	@Column(name = "employee_count")
	private Long employeeCount;

	@Column(name = "qual_threshold")
	private Double qualThreshold;

	@Column(name = "market_capitalization_number")
	private Double marketCapitalizationNumber;

	@Column(name = "revenue")
	private Double revenue;

	@Column(name = "ebitda")
	private Double ebitda;

	@Column(name = "debt")
	private Double debt;

	@Column(name = "pension_debt")
	private Double pensionDebt;

	@Column(name = "credit_rating")
	private String creditRating;

	@Column(name = "gross_risk_bearing_capacity")
	private Double grossRiskBearingCapacity;

	@Column(name = "business_risk_bearing_capacity")
	private Double businessRiskBearingCapacity;

	@Column(name = "cyber_risk_bearing_capacity")
	private Double cyberRiskBearingCapacity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Organizations parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "root_parent_id")
	private Organizations rootParent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private Users owner;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "industry_id")
	private Industries industry;

	@Column(name = "is_cloud_vendor")
	private Boolean isCloudVendor;

	@Column(name = "zoom_info_id")
	private Long zoomInfoId;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "vendor_to_technology",
		joinColumns = {@JoinColumn(name = "vendor_id")},
		inverseJoinColumns = {@JoinColumn(name = "technology_id")}
	)
	private Set<Technologies> technologies = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "organization_to_language",
		joinColumns = {@JoinColumn(name = "organization_id")},
		inverseJoinColumns = {@JoinColumn(name = "language_id")}
	)
	private Set<SupportedLanguages> supportedLanguages = new HashSet<>();

	@Column(name = "is_technology_vendor")
	private Boolean isTechnologyVendor;

	@Column(name = "is_system_vendor")
	private Boolean isSystemVendor;

	@Column(name = "is_service_vendor")
	private Boolean isServiceVendor;

	@Column(name = "is_public_company")
	private Boolean isPublicCompany;

	@Column(name = "is_multi_language")
	private Boolean isMultiLanguage;

	@Column(name = "use_multi_factor_auth")
	private Boolean useMultiFactorAuth;

	@Column(name = "two_factor_type")
	private TwoFactorType twoFactorType;

	@Column(name = "insurance_limit")
	private Double insuranceLimit;

	@Column(name = "insurance_deductible")
	private Double insuranceDeductible;

	@Column(name = "record_price_limit")
	private Double recordPriceLimit;

	@Column(name = "maximum_written_premium")
	private Double maximumWrittenPremium;

	@Column(name = "maximum_carrier_limit")
	private Double maximumCarrierLimit;

	@Column(name = "ransomware_sublimit")
	private Double ransomwareSublimit;

	@Column(name = "privacy_gdpr_sublimit")
	private Double privacyGdprSublimit;

	@Column(name = "privacy_ccpa_sublimit")
	private Double privacyCcpaSublimit;

	@Column(name = "ddos_sublimit")
	private Double ddosSublimit;

	@Column(name = "total_head_count")
	private Long totalHeadCount;

	@Column(name = "company_it_budget")
	private Double companyITBudget;

	@Column(name = "it_head_count")
	private Long itHeadCount;

	@Column(name = "security_head_count")
	private Long securityHeadCount;

	@Column(name = "it_security_budget")
	private Double itSecurityBudget;

	@Column(name = "number_of_employees")
	private Long numberOfEmployees;

	@Column(name = "number_of_independent_contractors")
	private Long numberOfIndependentContractors;

	@Column(name = "revenue_in_2021")
	private Double revenueIn2021;

	@Column(name = "revenue_in_2022")
	private Double revenueIn2022;

	@Column(name = "revenue_in_2023")
	private Double revenueIn2023;

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "organization_business_critical_applications",
		joinColumns = {@JoinColumn(name = "organization_id")},
		inverseJoinColumns = {@JoinColumn(name = "system_id")}
	)
	private Set<Systems> businessCriticalApplications = new HashSet<>();

	@ManyToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.LAZY)
	@JoinTable(
		name = "organization_outsourced_critical_providers",
		joinColumns = {@JoinColumn(name = "organization_id")},
		inverseJoinColumns = {@JoinColumn(name = "vendor_id")}
	)
	private Set<Organizations> outsourcedCriticalProviders = new HashSet<>();

	@Column(name = "inhouse_data_centers_count")
	private Long inhouseDataCentersCount;

	@Column(name = "external_data_centers_count")
	private Long externalDataCentersCount;

	@Column(name = "personally_identifiable_information")
	private Long personallyIdentifiableInformation;

	@Column(name = "payment_card_industry")
	private Double paymentCardIndustry;

	@Column(name = "protected_health_information")
	private Double protectedHealthInformation;

	@Column(name = "powerbi_capacity_expiration_date")
	private Date powerbiCapacityExpirationDate;

	@Column(name = "powerbi_capacity_status")
	private String powerbiCapacityStatus;

	@Column(name = "powerbi_capacity_name")
	private String powerbiCapacityName;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "assessment_framework_level")
	private AssessmentFrameworkLevel assessmentFrameworkLevel;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "logo_document_id")
	private Documents logoDocument;

	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<OrganizationsMetadata> metadata = new HashSet<>();

	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<OrganizationAssetInfo> assetInfoList = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "package_plan_id")
	private PackagePlans packagePlan;

	@Column(name = "is_in_ofac_list")
	private Boolean isInOfacList;

	@Column(name = "past_security_incidents")
	private String pastSecurityIncidents;

	@Column(name = "platform_type")
	@Enumerated(EnumType.STRING)
	private PlatformType platformType;

	@Column(name = "asset_type")
	@Enumerated(EnumType.STRING)
	private PlatformAssetType assetType;

	@Column(name = "amount_of_data_in_terabytes")
	private Double amountOfDataInTerabytes;

	@Column(name = "replication_factor")
	private Double replicationFactor;

}
