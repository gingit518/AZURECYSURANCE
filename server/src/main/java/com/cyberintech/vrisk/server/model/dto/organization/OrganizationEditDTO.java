package com.cyberintech.vrisk.server.model.dto.organization;

import com.cyberintech.vrisk.server.model.dto.DTOBase;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.currency.CurrencyViewDTO;
import com.cyberintech.vrisk.server.model.dto.document.DocumentDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.AssessmentFrameworkLevel;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Organization View Entity Definition
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2018-11-08
 */
@Setter
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id", "name"}, callSuper = false)
public class OrganizationEditDTO extends DTOBase<Organizations> {

	@Schema
	private Long id;

	@Schema
	private String name;

	@Schema
	private String description;

	@Schema
	private OrganizationType organizationType;

	@Schema
	private String taxId;

	@Schema
	private String vatId;

	@Schema
	private String streetAddress1;

	@Schema
	private String streetAddress2;

	@Schema
	private ItemViewDTO<Country> country;

	@Schema
	private ItemViewDTO<State> state;

	@Schema
	private ItemViewDTO<City> city;

	@Schema
	private CurrencyViewDTO currency;

	@Schema
	private ItemViewDTO<Language> language;

	@Schema
	private ItemViewDTO<Status> status;

	@Schema
	private OrganizationRefDTO parent;

	@Schema
	private IndustryRefDTO industry;

	@Schema
	private UserRefDTO owner;

	@Schema
	private String zip;

	@Schema
	private String phone;

	@Schema
	private String site;

	@Schema
	private String logo;

	@Schema
	private String notes;

	@Schema
	private Double averageRevenue;

	@Schema
	private Double qualThreshold;

	@Schema
	private Boolean isCloudVendor;

	@Schema
	private Double marketCapitalizationNumber;

	@Schema
	private Double revenue;

	@Schema
	private Double ebitda;

	@Schema
	private Double debt;

	@Schema
	private Double pensionDebt;

	@Schema
	private String creditRating;

	@Schema
	private Double grossRiskBearingCapacity;

	@Schema
	private Double businessRiskBearingCapacity;

	@Schema
	private Double cyberRiskBearingCapacity;

	@Schema
	private Boolean isPublicCompany;

	@Schema
	private List<SupportedLanguageEditDTO> supportedLanguages;

	@Schema
	private Boolean isMultiLanguage;

	@Schema
	private Boolean useMultiFactorAuth;

	@Schema
	private Boolean isServiceVendor;

	@Schema
	private Boolean isTechnologyVendor;

	@Schema
	private Boolean isSystemVendor;

	@Schema
	private OrganizationRefDTO rootParent;

	@Schema
	private Set<TechnologyRefDTO> technologies;

	@Schema
	private Double insuranceLimit;

	@Schema
	private Double insuranceDeductible;

	@Schema
	private Double recordPriceLimit;

	@Schema
	private Double maximumWrittenPremium;

	@Schema
	private Double maximumCarrierLimit;

	@Schema
	private Double ransomwareSublimit;

	@Schema
	private Double privacyGdprSublimit;

	@Schema
	private Double privacyCcpaSublimit;

	@Schema
	private Double ddosSublimit;

	@Schema
	private Long totalHeadCount;

	@Schema
	private Double companyITBudget;

	@Schema
	private Long itHeadCount;

	@Schema
	private Long securityHeadCount;

	@Schema
	private Double itSecurityBudget;

	@Schema
	private Long numberOfEmployees;

	@Schema
	private Long numberOfIndependentContractors;

	@Schema
	private Double revenueIn2021;

	@Schema
	private Double revenueIn2022;

	@Schema
	private Double revenueIn2023;

	@Schema
	private Set<SystemRefDTO> businessCriticalApplications;

	@Schema
	private Set<OrganizationRefDTO> outsourcedCriticalProviders;

	@Schema
	private Long inhouseDataCentersCount;

	@Schema
	private Long externalDataCentersCount;

	@Schema
	private Long personallyIdentifiableInformation;

	@Schema
	private Double paymentCardIndustry;

	@Schema
	private Double protectedHealthInformation;

	@Schema
	private Long zoomInfoId;

	@Schema
	private DocumentDTO logoDocument;

	@Schema
	private Boolean removeLogo;

	@Schema
	private AssessmentFrameworkLevel assessmentFrameworkLevel;

	@Schema
	private PackagePlansDTO packagePlan;

	@Schema
	private Long logoutAfterInactivityTime;

	@Schema
	private Long logoutAfterInactivityTimeInMinutes;

	@Schema
	private Boolean isInOfacList;

	@Schema
	private String pastSecurityIncidents;

	@Schema
	private Map<String, String> integrationProperties;

	/**
	 * Entity based constructor
	 *
	 * @param entity
	 */
	public OrganizationEditDTO(Organizations entity) {
		super(entity);
	}

	/**
	 * Converts from entity to DTO
	 *
	 * @param entity
	 */
	@Override
	public void fromEntity(Organizations entity) {
//		super.fromEntity(entity);

		id = entity.getId();
		name = entity.getName();
		description = entity.getDescription();
		organizationType = entity.getOrganizationType();
		taxId = entity.getTaxId();
		vatId = entity.getVatId();
		streetAddress1 = entity.getStreetAddress1();
		streetAddress2 = entity.getStreetAddress2();
		zip = entity.getZip();
		phone = entity.getPhone();
		site = entity.getSite();
		logo = entity.getLogo();
		notes = entity.getNotes();
		marketCapitalizationNumber = entity.getMarketCapitalizationNumber();

		revenue = entity.getRevenue();
		ebitda = entity.getEbitda();
		debt = entity.getDebt();
		pensionDebt = entity.getPensionDebt();
		creditRating = entity.getCreditRating();
		grossRiskBearingCapacity = entity.getGrossRiskBearingCapacity();
		businessRiskBearingCapacity = entity.getBusinessRiskBearingCapacity();
		cyberRiskBearingCapacity = entity.getCyberRiskBearingCapacity();

		averageRevenue = entity.getAverageRevenue();
		qualThreshold = entity.getQualThreshold();
		isCloudVendor = entity.getIsCloudVendor();
		isServiceVendor = entity.getIsServiceVendor();
		isTechnologyVendor = entity.getIsSystemVendor();
		isSystemVendor = entity.getIsTechnologyVendor();
		isPublicCompany = entity.getIsPublicCompany();
		isMultiLanguage = entity.getIsMultiLanguage();
		useMultiFactorAuth = entity.getUseMultiFactorAuth();
		insuranceLimit = entity.getInsuranceLimit();
		insuranceDeductible = entity.getInsuranceDeductible();
		recordPriceLimit = entity.getRecordPriceLimit();
		maximumWrittenPremium = entity.getMaximumWrittenPremium();
		maximumCarrierLimit = entity.getMaximumCarrierLimit();
		ransomwareSublimit = entity.getRansomwareSublimit();
		privacyGdprSublimit = entity.getPrivacyGdprSublimit();
		privacyCcpaSublimit = entity.getPrivacyCcpaSublimit();
		ddosSublimit = entity.getDdosSublimit();

		zoomInfoId = entity.getZoomInfoId();
		assessmentFrameworkLevel = entity.getAssessmentFrameworkLevel();

		if (entity.getLogoutAfterInactivityTime() != null) {
			logoutAfterInactivityTime = entity.getLogoutAfterInactivityTime();
			logoutAfterInactivityTimeInMinutes = entity.getLogoutAfterInactivityTime() / 60000;
		}

		totalHeadCount = entity.getTotalHeadCount();
		companyITBudget = entity.getCompanyITBudget();
		itHeadCount = entity.getItHeadCount();
		securityHeadCount = entity.getSecurityHeadCount();
		itSecurityBudget = entity.getItSecurityBudget();
		numberOfEmployees = entity.getNumberOfEmployees();
		numberOfIndependentContractors = entity.getNumberOfIndependentContractors();
		revenueIn2021 = entity.getRevenueIn2021();
		revenueIn2022 = entity.getRevenueIn2022();
		revenueIn2023 = entity.getRevenueIn2023();
		inhouseDataCentersCount = entity.getInhouseDataCentersCount();
		externalDataCentersCount = entity.getExternalDataCentersCount();
		personallyIdentifiableInformation = entity.getPersonallyIdentifiableInformation();
		paymentCardIndustry = entity.getPaymentCardIndustry();
		protectedHealthInformation = entity.getProtectedHealthInformation();

		isInOfacList = entity.getIsInOfacList();
		pastSecurityIncidents = entity.getPastSecurityIncidents();
		integrationProperties = entity.getIntegrationProperties();

		if (entity.getCountry() != null) setCountry(new ItemViewDTO<Country>(entity.getCountry()));
		if (entity.getCity() != null) setCity(new ItemViewDTO<City>(entity.getCity()));
		if (entity.getState() != null) setState(new ItemViewDTO<State>(entity.getState()));
		if (entity.getLanguage() != null) setLanguage(new ItemViewDTO<Language>(entity.getLanguage()));
		if (entity.getCurrency() != null) setCurrency(new CurrencyViewDTO(entity.getCurrency()));
		if (entity.getStatus() != null) setStatus(new ItemViewDTO<Status>(entity.getStatus()));
		if (entity.getParent() != null) setParent(new OrganizationRefDTO(entity.getParent()));
		if (entity.getRootParent() != null) setRootParent(new OrganizationRefDTO(entity.getRootParent()));
		if (entity.getOwner() != null) setOwner(new UserRefDTO(entity.getOwner()));
		if (entity.getIndustry() != null) setIndustry(new IndustryRefDTO(entity.getIndustry()));
		if (entity.getLogoDocument() != null) setLogoDocument(new DocumentDTO(entity.getLogoDocument()));
		if (entity.getPackagePlan() != null) setPackagePlan(new PackagePlansDTO(entity.getPackagePlan()));

		supportedLanguages = Optional.ofNullable(entity.getSupportedLanguages()).orElse(new HashSet<>()).stream().map(SupportedLanguageEditDTO::new).collect(Collectors.toList());
		technologies = entity.getTechnologies().stream().map(TechnologyRefDTO::new).collect(Collectors.toSet());

		businessCriticalApplications = entity.getBusinessCriticalApplications().stream().map(SystemRefDTO::new).collect(Collectors.toSet());
		outsourcedCriticalProviders = entity.getOutsourcedCriticalProviders().stream().map(OrganizationRefDTO::new).collect(Collectors.toSet());
	}

	/**
	 * Convert User Details DTO to Entity
	 *
	 * @return
	 */
	/*
	@Override
	public Organizations toEntity(Organizations update) {
		Organizations result = super.toEntity(update);

		CountryRepository countryRepository = BeanUtil.getBean(CountryRepository.class);
		CityRepository cityRepository = BeanUtil.getBean(CityRepository.class);
		StateRepository stateRepository = BeanUtil.getBean(StateRepository.class);
		CurrencyRepository currencyRepository = BeanUtil.getBean(CurrencyRepository.class);
		LanguageRepository languageRepository = BeanUtil.getBean(LanguageRepository.class);
		StatusRepository statusRepository = BeanUtil.getBean(StatusRepository.class);

		if (getCountry() != null) {
			update.setCountry(countryRepository.findById(getCountry().getId()).orElse(null));
		}
		if (getState() != null) {
			update.setState(stateRepository.findById(getState().getId()).orElse(null));
		}
		if (getCity() != null) {
			update.setCity(cityRepository.findById(getCity().getId()).orElse(null));
		}
		if (getCurrency() != null) {
			update.setCurrency(currencyRepository.findById(getCurrency().getId()).orElse(null));
		}
		if (getLanguage() != null) {
			update.setLanguage(languageRepository.findById(getLanguage().getId()).orElse(null));
		}
		if (getStatus() != null) {
			update.setStatus(statusRepository.findById(getStatus().getId()).orElse(null));
		}

		return result;
	}
	*/
}
