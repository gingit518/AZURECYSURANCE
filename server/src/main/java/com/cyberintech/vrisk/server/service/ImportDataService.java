package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.country.CountryViewDTO;
import com.cyberintech.vrisk.server.model.dto.currency.CurrencyViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.IndustryRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationEditDTO;
import com.cyberintech.vrisk.server.model.dto.organization.VendorEditDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessRefDTO;
import com.cyberintech.vrisk.server.model.dto.state.StateViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemEditDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemGeoParametersDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserUpdateDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.*;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.model.jpa.entity.Currency;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.rest.exception.ApplicationExceptionCodes;
import com.cyberintech.vrisk.server.rest.exception.BadRequestException;
import com.cyberintech.vrisk.server.rest.exception.ForbiddenException;
import com.cyberintech.vrisk.server.rest.exception.InternalServerErrorException;
import com.cyberintech.vrisk.server.service.admin.AdminUserService;
import com.cyberintech.vrisk.server.service.communication.EmailService;
import com.cyberintech.vrisk.server.service.csv.ProcessCSVImporter;
import com.cyberintech.vrisk.server.service.csv.TechnologyCSVImporter;
import com.cyberintech.vrisk.server.service.csv.TechnologyMappingCSVImporter;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import com.cyberintech.vrisk.server.service.utils.ExportUtils;
import com.cyberintech.vrisk.server.service.utils.ImportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Data Import management Service
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-04-22
 */
@Service
@Slf4j
public class ImportDataService {

	public static final String USER_NAME_HEADER = "Name";
	public static final String USER_BU_HEADER = "BU";
	public static final String USER_ROLE_HEADER = "Role";
	public static final String USER_TITLE_HEADER = "Title";
	public static final String USER_EMAIL_HEADER = "Email";
	public static final String USER_PHONE_HEADER = "Corporate Phone";

	public static final String USER_MOBILE_PHONE_HEADER = "Mobile Phone";

	public static final String USER_ENABLED_HEADER = "Enabled";

	public static final String ASSET_NAME_HEADER = "Asset Name";
	public static final String ASSET_DESCRIPTION_HEADER = "Asset Description";
	public static final String DEVICE_ID_HEADER = "Device ID";

	public static final String SYSTEM_NAME_HEADER = "System Name";
	public static final String SYSTEM_DESCRIPTION_HEADER = "System Description";
	public static final String SYSTEM_ASSOCIATE_VENDORS_HEADER = "Associate Vendors";
	public static final String SYSTEM_BU_DIVISION_HEADER = "BU Division";
	public static final String SYSTEM_BU_LOCATION_HEADER = "BU Location";
	public static final String SYSTEM_BU_PATH_HEADER = "Business Unit Path";
	public static final String SYSTEM_COST_TO_RESTORE_HEADER = "Cost to restore";
	public static final String SYSTEM_DATA_CLASSIFICATION_HEADER = "Data Classification";
	public static final String SYSTEM_DIGITAL_ASSET_CLASS_HEADER = "Asset Class";
	public static final String SYSTEM_GEO_RECORDS_PROCESSED_HEADER = "Geo Records Processed";
	public static final String SYSTEM_INFOSEC_PERSON_EMAIL_HEADER = "Infosec Focal Person Email";
	public static final String SYSTEM_INFOSEC_PERSON_NAME_HEADER = "Infosec Focal Person";
	public static final String SYSTEM_MA_ASSET_HEADER = "M&A asset";
	public static final String SYSTEM_ON_DEPLOYMENT_TYPE_HEADER = "Deployment Type";
	public static final String SYSTEM_OWNER_EMAIL_HEADER = "System Owner Email";
	public static final String SYSTEM_OWNER_NAME_HEADER = "System Owner Name";
	public static final String SYSTEM_PROCESSES_HEADER = "Processes";
	public static final String SYSTEM_RECORDS_PROCESSED_HEADER = "Records Processed";
	public static final String SYSTEM_RPO_HEADER = "RPO";
	public static final String SYSTEM_RTO_HEADER = "RTO";
	public static final String SYSTEM_STATUS_HEADER = "Status";
	public static final String SYSTEM_SYSTEM_TYPE_HEADER = "System Type";
	public static final String SYSTEM_TECHNOLOGIES_HEADER = "Technologies";
	public static final String SYSTEM_VERSION_NUMBER_HEADER = "Version Number";
	public static final String SYSTEM_EOL_DATE_HEADER = "EOL Date";
	public static final String SYSTEM_IP_ADDRESS_HEADER = "IP Address";
	public static final String SYSTEM_TECHNOLOGY_CATEGORY_HEADER = "Technology Category";
	public static final String SYSTEM_SERIAL_NUMBER_HEADER = "Serial Number";
	public static final String SYSTEM_ASSET_DOMAIN_FUNCTION_HEADER = "Asset Domain Function";
	public static final String SYSTEM_OS_NAME_HEADER = "Operating System";
	public static final String SYSTEM_LOCATION_HEADER = "Location";
	public static final String SYSTEM_HARDWARE_STATUS_HEADER = "Hardware Substatus";
	public static final String SYSTEM_WARRANTY_EXPIRATION_HEADER = "Warranty Expiration";
	public static final String SYSTEM_ASSET_NAME_HEADER = "Asset Name";
	public static final String SYSTEM_DISCOVERY_SOURCE_HEADER = "Discovery Source";
	public static final String SYSTEM_UPDATED_DATE_HEADER = "Updated Date";
	public static final String SYSTEM_UPDATED_BY_HEADER = "Updated By";
	public static final String SYSTEM_CREATED_DATE_HEADER = "Created Date";
	public static final String SYSTEM_CREATED_BY_HEADER = "Created By";
	public static final String SYSTEM_COMPUTER_ID_HEADER = "Computer ID";
	public static final String SYSTEM_OWNER_TYPE_HEADER = "Owner Type";


	public static final String ORGANIZATION_NAME_HEADER = "Organization Name";
	public static final String ORGANIZATION_DESCRIPTION_HEADER = "Description";
	public static final String ORGANIZATION_PARENT_NAME_HEADER = "Parent Organization";
	public static final String ORGANIZATION_STATUS_HEADER = "Status";
	public static final String ORGANIZATION_OWNER_NAME_HEADER = "Contact Name";
	public static final String ORGANIZATION_OWNER_EMAIL_HEADER = "Contact Email";
	public static final String ORGANIZATION_INDUSTRY_HEADER = "Industry";
	public static final String ORGANIZATION_TECHNOLOGIES_HEADER = "Technologies";
	public static final String ORGANIZATION_SITE_HEADER = "Website URL";
	public static final String ORGANIZATION_PHONE_HEADER = "Phone Number";
	public static final String ORGANIZATION_COUNTRY_HEADER = "Country";
	public static final String ORGANIZATION_STATE_HEADER = "State";
	public static final String ORGANIZATION_CITY_HEADER = "City";
	public static final String ORGANIZATION_ADDRESS1_HEADER = "Address 1";
	public static final String ORGANIZATION_ADDRESS2_HEADER = "Address 2";
	public static final String ORGANIZATION_CURRENCY_HEADER = "Currency";
	public static final String ORGANIZATION_LANGUAGE_HEADER = "Language";
	public static final String ORGANIZATION_ZIP_HEADER = "ZIP or Postal Code";
	public static final String ORGANIZATION_TAX_HEADER = "TAX ID";
	public static final String ORGANIZATION_VAT_HEADER = "VAT Number";
	public static final String ORGANIZATION_NOTES_HEADER = "Notes";
	public static final String ORGANIZATION_LINKS_TO_CONTRACTS_HEADER = "Links to Contacts in Policy Management";
	public static final String ORGANIZATION_REVENUE_HEADER = "Annual Revenue";

	public static final String ORGANIZATION_CLOUD_HEADER = "Cloud Vendor";
	public static final String VENDOR_IS_TECHNOLOGY_HEADER = "Technology Vendor";
	public static final String VENDOR_IS_SYSTEM_HEADER = "System Vendor";
	public static final String VENDOR_IS_SERVICE_HEADER = "Service Vendor";

	@Autowired
	private AdminUserService adminUserService;

	@Autowired
	private AssociateVendorRepository associateVendorRepository;

	@Autowired
	private AssociateVendorService associateVendorService;

	@Autowired
	private BusinessUnitRepository businessUnitRepository;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private DataAssetClassificationRepository dataAssetClassificationRepository;

	@Autowired
	private DataDomainsRepository dataDomainsRepository;

	@Autowired
	private DataTypeClassificationRepository dataTypeClassificationRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private EnvironmentTypesRepository environmentTypesRepository;

	@Autowired
	private GDPRArticleChapterRepository gdprArticleChapterRepository;

	@Autowired
	private GDPRArticleItemRepository gdprArticleItemRepository;

	@Autowired
	private GDPRArticleParagraphRepository gdprArticleParagraphRepository;

	@Autowired
	private GDPRArticleChapterSectionRepository gdprArticleChapterSectionRepository;

	@Autowired
	private IndustryRepository industryRepository;

	@Autowired
	private ImportDataRecordsService importDataRecordsService;

	@Autowired
	private LanguageRepository languageRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private ProcessRepository processRepository;

	@Autowired
	private ProcessService processService;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private TechnologyRepository technologyRepository;

	@Autowired
	private TechnologyCategoryImportMappingRepository technologyCategoryImportMappingRepository;

	@Autowired
	private TechnologyService technologyService;

	@Autowired
	private TechnologyCategoryRepository technologyCategoryRepository;

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private VendorService vendorService;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Create CSV Printer to build Users
	 *
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */
	private CSVPrinter createUserCsvPrinter(ByteArrayOutputStream outputStream) throws IOException {
		Writer writer = new OutputStreamWriter(outputStream);
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
			USER_NAME_HEADER,
			USER_BU_HEADER,
			USER_ROLE_HEADER,
			USER_TITLE_HEADER,
			USER_EMAIL_HEADER,
			USER_MOBILE_PHONE_HEADER,
			USER_PHONE_HEADER,
			USER_ENABLED_HEADER
		);
		return new CSVPrinter(writer, csvFormat);
	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getUserDownloadData() {

		// String templateContent = "Business Unit Name,Business Unit Description,Parent Business Unit";
		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = createUserCsvPrinter(outputStream);
			if (userService.isAuthorized() && userService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
				List<Users> items = userRepository.getListActiveByOrganization(userService.getCurrentUser().getOrganizationId());
				for (Users user : items) {
					csvPrinter.printRecord(
						user.getFullName(),
						businessUnitService.getBusinessUnitPath(user.getBusinessUnit(), true, ";"),
						StringUtils.join(Optional.ofNullable(user.getRoles()).orElse(new HashSet<>()).stream().map(Roles::getName).collect(Collectors.toList()), ", "),
						user.getTitle(),
						user.getEmail(),
						user.getMobilePhone(),
						user.getCorporatePhone(),
						(Boolean.TRUE.equals(user.getEnabled()) ? "YES" : "NO")
					);
				}
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.error("Failed to generate CSV Template file for Users", e);
			throw new InternalServerErrorException("Failed to generate CSV Template file for Users");
		}

		return byteArrayInputStream;
	}

	/**
	 * Insert data from CSV file
	 */
	@Transactional
	public ImportResultDTO importUsersFromCSVFile(InputStream fileContentStream) {
		ImportResultDTO result = new ImportResultDTO();

		try {
			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			if (csvParser.getHeaderMap().containsKey(USER_NAME_HEADER) && csvParser.getHeaderMap().containsKey(USER_EMAIL_HEADER) && csvParser.getHeaderMap().containsKey(USER_ROLE_HEADER)) {
				result = importUsersFromCSVItems(csvRecordList);
			} else {
				throw new BadRequestException("User Name/Role/Email header not found. Import Failed.");
			}

		} catch (IOException e) {
			log.error("Failed to import users", e);
		}

		return result;
	}

	/**
	 * Import {@link Processes} from CSV file
	 */
	@Transactional
	public ImportResultDTO importProcessesFromCSVFile(InputStream inputStream) {
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		ProcessCSVImporter csvImporter = new ProcessCSVImporter(businessUnitService,
			dataTypeClassificationRepository, dataAssetClassificationRepository, processRepository, processService,
			systemRepository, userService, organization);
		return csvImporter.doImport(inputStream);
	}

	/**
	 * Insert data from CSV file
	 */
	@Transactional
	public ImportResultDTO importSystemsFromCSVFile(InputStream fileContentStream) {

		ImportResultDTO result = new ImportResultDTO();

		try {
			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			if (csvParser.getHeaderMap().containsKey(SYSTEM_NAME_HEADER)) {
				result = importSystemsFromCSVItems(csvRecordList);
			} else {
				throw new BadRequestException("System Name Header not found. Import Failed.");
			}

		} catch (IOException e) {
			log.error("Failed to import systems", e);
		}

		return result;
	}

	/**
	 * Import {@link Technologies} from CSV file
	 */
	@Transactional
	public ImportResultDTO importTechnologiesFromCSVFile(InputStream inputStream) {
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		TechnologyCSVImporter csvImporter = new TechnologyCSVImporter(countryRepository,
			dataAssetClassificationRepository, dataDomainsRepository, environmentTypesRepository,
			organizationRepository, systemRepository, technologyRepository, technologyCategoryRepository,
			technologyService, organization);
		return csvImporter.doImport(inputStream);
	}

	/**
	 * Import {@link TechnologyCategoryImportMappings} from CSV file
	 */
	@Transactional
	public ImportResultDTO importTechnologyAssetsFromCSVFile(InputStream inputStream) {
		ImportResultDTO result = new ImportResultDTO();

		try {
			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(inputStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			if (csvParser.getHeaderMap().containsKey(SYSTEM_NAME_HEADER) || csvParser.getHeaderMap().containsKey(ASSET_NAME_HEADER)) {
				result = importTechnologyAssetsFromCSVItems(csvRecordList);
			} else {
				throw new BadRequestException("Asset Name Header not found. Import Failed.");
			}

		} catch (IOException e) {
			log.error("Failed to import systems", e);
		}

		return result;
	}

	/**
	 * Import {@link TechnologyCategoryImportMappings} from CSV file
	 */
	@Transactional
	public ImportResultDTO importTechnologyCategoryMappingFromCSVFile(InputStream inputStream) {
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		TechnologyMappingCSVImporter csvImporter = new TechnologyMappingCSVImporter(technologyCategoryImportMappingRepository, organization, platformTransactionManager);
		return csvImporter.doImport(inputStream);
	}

	/**
	 * Insert some data from CSV file
	 */
	@Transactional
	public ImportResultDTO importUsersFromCSVItems(List<CSVRecord> csvRecordList) {

		if (!adminUserService.hasRole(RoleType.ADMIN) && !adminUserService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			throw new ForbiddenException("You are not allowed to import users!", ApplicationExceptionCodes.USER_MANAGEMENT_FORBIDDEN);
		}

		ImportResultDTO result = new ImportResultDTO();
		Organizations organization = organizationService.getCurrentOrganizationEntity();

		// Proceed with user management
		for (CSVRecord csvRecord : csvRecordList) {
			// Accessing values by Header names
			String name = Optional.ofNullable(csvRecord.get(USER_NAME_HEADER)).orElse("").trim();
			String businessUnitName = Optional.ofNullable(csvRecord.get(USER_BU_HEADER)).orElse("").trim();
			String rolesString = Optional.ofNullable(csvRecord.get(USER_ROLE_HEADER)).orElse("").trim();
			String title = Optional.ofNullable(csvRecord.get(USER_TITLE_HEADER)).orElse("").trim();
			String email = Optional.ofNullable(csvRecord.get(USER_EMAIL_HEADER)).orElse("").trim();
			String corporatePhone = csvRecord.isMapped(USER_PHONE_HEADER) ? Optional.ofNullable(csvRecord.get(USER_PHONE_HEADER)).orElse("").trim() : "";
			String mobilePhone = csvRecord.isMapped(USER_MOBILE_PHONE_HEADER) ? Optional.ofNullable(csvRecord.get(USER_MOBILE_PHONE_HEADER)).orElse("").trim() : "";
			String enabledString = csvRecord.isMapped(USER_ENABLED_HEADER) ? Optional.ofNullable(csvRecord.get(USER_ENABLED_HEADER)).orElse("").trim() : "";

			if (StringUtils.isNotEmpty(name)) {
				Optional<Users> userDetails = userRepository.findFirstByEmailIgnoreCaseAndOrganization(email, organization);
				Optional<Users> anotherUserDetails = userRepository.findFirstByEmailIgnoreCase(email);
				if (anotherUserDetails.isPresent() && !anotherUserDetails.equals(userDetails)) {
					result.getIgnored().add(new ItemViewDTO(anotherUserDetails.get().getId(), anotherUserDetails.get().getFullName()));

					continue;
				}

				UserUpdateDTO userDTO;
				if (userDetails.isEmpty()) {
					userDTO = new UserUpdateDTO();
					userDTO.setEmail(email);
					// userDTO.setPasswordPlain("password");
					userDTO.setEnabled(true);
					userDTO.setExpired(false);
					userDTO.setCredentialsExpired(true);
				} else {
					UserUpdateDTO userUpdateDTO = new UserUpdateDTO(userDetails.get());
					if (StringUtils.isNotEmpty(rolesString)) {
						userUpdateDTO.setRoles(null);
					}
					userDTO = userUpdateDTO;
				}

				String[] peopleName = StringUtils.split(name, ",", 2);
				if (peopleName.length < 2) {
					peopleName = StringUtils.split(name, " ", 2);
					if (peopleName.length > 0) userDTO.setFirstName(peopleName[0].trim());
					if (peopleName.length > 1) userDTO.setLastName(peopleName[1].trim());
				} else {
					if (peopleName.length > 0) userDTO.setLastName(peopleName[0].trim());
					if (peopleName.length > 1) userDTO.setFirstName(peopleName[1].trim());
				}
				userDTO.setTitle(title);
				if (StringUtils.isNotEmpty(corporatePhone)) userDTO.setCorporatePhone(corporatePhone);
				if (StringUtils.isNotEmpty(mobilePhone)) userDTO.setMobilePhone(mobilePhone);
				userDTO.setEnabled(!("no".equalsIgnoreCase(enabledString)));

				// Try to find Business Unit for the User
				if (StringUtils.isNotEmpty(businessUnitName)) {
					BusinessUnits businessUnit = businessUnitService.getParentByPath(businessUnitName, organization.getId(), true);
					if (businessUnit == null) {
						businessUnit = businessUnitService.getParentByPath(businessUnitName, organization.getId(), false);
					}
					if (businessUnit != null) {
						userDTO.setBusinessUnit(new BusinessUnitRefDTO(businessUnit));
					}
				}

				if (StringUtils.isNotEmpty(rolesString)) {
					List<String> rolesList = Arrays.stream(rolesString.split(",")).filter(roleName -> !RoleType.ADMIN.role().equalsIgnoreCase(roleName.trim())).map(roleName -> roleName.trim()).collect(Collectors.toList());
					userDTO.setRoleNames(rolesList);
				}

				UserDTO userResult;
				if (userDetails.isEmpty()) {
					userResult = userService.create(userDTO);
					result.getCreated().add(new ItemViewDTO(userResult.getId(), MessageFormat.format("{0}, {1} <{2}>", userResult.getFirstName(), userResult.getLastName(), userResult.getEmail())));
				} else {
					userResult = userService.update((UserUpdateDTO) userDTO);
					result.getUpdated().add(new ItemViewDTO(userResult.getId(), MessageFormat.format("{0}, {1} <{2}>", userResult.getFirstName(), userResult.getLastName(), userResult.getEmail())));
				}
			}
		}

		return result;
	}

	/**
	 * Insert some data from CSV file
	 */
	public ImportResultDTO importSystemsFromCSVItems(List<CSVRecord> csvRecordList) {

		final ImportResultDTO result = new ImportResultDTO();

		int currentItem = 0;
		int totalItem = 0;
		long currentTime = System.currentTimeMillis();
		List<CSVRecord> recordBatch = new ArrayList<>();
		// Proceed with user management
		for (CSVRecord csvRecord : csvRecordList) {

			recordBatch.add(csvRecord);

			totalItem++;
			if (++currentItem >= 100 || totalItem == csvRecordList.size()) {
				final List<CSVRecord> runnableBatch = new ArrayList<>(recordBatch);
				ImportResultDTO batchResult = new TransactionTemplate(platformTransactionManager).execute(status -> {
					return importDataRecordsService.importSystemsFromCSVItems(runnableBatch);
				});;
				if (batchResult != null) result.load(batchResult);

				recordBatch = new ArrayList<>();
				long processingTime = (System.currentTimeMillis() - currentTime) / 1000;
				log.info("Processed import records: {} in {} seconds", totalItem, processingTime);

				currentItem = 0;
			}
		}

		return result;
	}


	/**
	 * Insert some data from CSV file
	 */
	public ImportResultDTO importTechnologyAssetsFromCSVItems(List<CSVRecord> csvRecordList) {

		final ImportResultDTO result = new ImportResultDTO();

		int currentItem = 0;
		int totalItem = 0;
		long currentTime = System.currentTimeMillis();
		List<CSVRecord> recordBatch = new ArrayList<>();
		// Proceed with user management
		for (CSVRecord csvRecord : csvRecordList) {

			recordBatch.add(csvRecord);

			totalItem++;
			if (++currentItem >= 100 || totalItem == csvRecordList.size()) {
				final List<CSVRecord> runnableBatch = new ArrayList<>(recordBatch);
				ImportResultDTO batchResult = new TransactionTemplate(platformTransactionManager).execute(status -> {
					return importDataRecordsService.importTechnologyAssetsFromCSVItems(runnableBatch);
				});;
				if (batchResult != null) result.load(batchResult);

				recordBatch = new ArrayList<>();
				long processingTime = (System.currentTimeMillis() - currentTime) / 1000;
				log.info("Processed import records: {} in {} seconds", totalItem, processingTime);

				currentItem = 0;
			}
		}

		return result;
	}

	public Optional<Users> getUserFromNames(String systemOwnerEmail, Organizations organization, @NotNull String systemOwnerName, Map<String, Users> usersCache) {

		Optional<Users> systemOwnerDetails = Optional.empty();

		if (usersCache.containsKey(systemOwnerEmail)) {
			systemOwnerDetails = Optional.ofNullable(usersCache.get(systemOwnerEmail));
		} else if (usersCache.containsKey(systemOwnerName)) {
			systemOwnerDetails = Optional.ofNullable(usersCache.get(systemOwnerName));
		}

		if (systemOwnerDetails.isEmpty()) {
			systemOwnerDetails = userRepository.findFirstByFullNameAndOrganization(systemOwnerEmail, organization);
            systemOwnerDetails.ifPresent(users -> usersCache.put(systemOwnerEmail, users));
		}
		if (systemOwnerDetails.isEmpty()) {
			systemOwnerDetails = userRepository.findFirstByEmailIgnoreCaseAndOrganization(systemOwnerEmail, organization);
			systemOwnerDetails.ifPresent(users -> usersCache.put(systemOwnerEmail, users));
		}
		if (systemOwnerDetails.isEmpty()) {
			systemOwnerDetails = userRepository.findFirstByFullNameAndOrganization(systemOwnerName, organization);
			systemOwnerDetails.ifPresent(users -> usersCache.put(systemOwnerName, users));
		}

		return systemOwnerDetails;
	}

	/**
	 * Get Template content for Download
	 */
	public ByteArrayInputStream getSystemsDownloadData() {

		// String templateContent = "Business Unit Name,Business Unit Description,Parent Business Unit";
		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			Writer writer = new OutputStreamWriter(outputStream);
			CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
				SYSTEM_NAME_HEADER,
				SYSTEM_DESCRIPTION_HEADER,
				SYSTEM_VERSION_NUMBER_HEADER,
				SYSTEM_OWNER_NAME_HEADER,
				SYSTEM_OWNER_EMAIL_HEADER,
				SYSTEM_INFOSEC_PERSON_NAME_HEADER,
				SYSTEM_INFOSEC_PERSON_EMAIL_HEADER,
				SYSTEM_STATUS_HEADER,
				SYSTEM_BU_PATH_HEADER,
				SYSTEM_DIGITAL_ASSET_CLASS_HEADER,
				SYSTEM_DATA_CLASSIFICATION_HEADER,
				SYSTEM_PROCESSES_HEADER,
				SYSTEM_TECHNOLOGIES_HEADER,
				SYSTEM_ASSOCIATE_VENDORS_HEADER,
				SYSTEM_COST_TO_RESTORE_HEADER,
				SYSTEM_ON_DEPLOYMENT_TYPE_HEADER,
				SYSTEM_MA_ASSET_HEADER,
				SYSTEM_SYSTEM_TYPE_HEADER,
				SYSTEM_RTO_HEADER,
				SYSTEM_RPO_HEADER,
				SYSTEM_RECORDS_PROCESSED_HEADER,
				SYSTEM_GEO_RECORDS_PROCESSED_HEADER
			);
			CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

			Long organizationId = organizationService.getCurrentOrganizationId();

			// Build Associate Vendors Map
			List<AssociateVendors> associateVendorList = associateVendorRepository.getListForOrganization(organizationId);
			Map<Systems, Set<Organizations>> systemVendorsMap = new HashMap<>();
			for (AssociateVendors associateVendor : associateVendorList) {
				if (CollectionUtils.isNotEmpty(associateVendor.getSystems()) && associateVendor.getVendor() != null) {
					for (Systems system : associateVendor.getSystems()) {
						if (!systemVendorsMap.containsKey(system)) {
							systemVendorsMap.put(system, new HashSet<>());
						}

						systemVendorsMap.get(system).add(associateVendor.getVendor());
					}
				}
			}

			List<Systems> systemsList = systemRepository.getAllByOrganizationOrderedByName(organizationId);
			for (Systems system : systemsList) {
				String geoRecordsLine = system.getSystemGeoParameters() != null ? StringUtils.join(system.getSystemGeoParameters().stream().map(systemGeoParameters -> {
					String geoLine = (systemGeoParameters.getCountry() != null ? systemGeoParameters.getCountry().getName() : "") + ","
						+ (systemGeoParameters.getState() != null ? systemGeoParameters.getState().getName() : "") + ","
						+ (systemGeoParameters.getNumberOfRecProcessed() != null ? systemGeoParameters.getNumberOfRecProcessed().longValue() : "");

					return geoLine;
				}).collect(Collectors.toList()), "\n") : "";

				String associateVendors = "";
				if (systemVendorsMap.containsKey(system)) {
					associateVendors = systemVendorsMap.get(system).stream().map(Organizations::getName).collect(Collectors.joining(";"));
				}

				csvPrinter.printRecord(
					system.getName(),
					system.getDescription(),
					system.getVersionNumber(),
					ExportUtils.userFullNameAsString(system.getOwner()),
					ExportUtils.userEmailAsString(system.getOwner()),
					ExportUtils.userFullNameAsString(system.getInfosecFocalPerson()),
					ExportUtils.userEmailAsString(system.getInfosecFocalPerson()),
					system.getSystemStatus() != null ? system.getSystemStatus().name() : "",
					ExportUtils.asString(system.getBusinessUnit(), businessUnitService),
					ExportUtils.asString(system.getDataAssetClassification()),
					ExportUtils.dataTypeClassificationAsString(system.getDataTypeClassifications()),
					ExportUtils.processesAsString(system.getProcesses()),
					ExportUtils.technologiesAsString(system.getTechnologies()),
					associateVendors,
					system.getCostToRestore(),
					system.getDeploymentType(),
					Boolean.TRUE.equals(system.getIsMAAsset()) ? "YES" : "NO",
					system.getSystemType(),
					system.getRto(),
					system.getRpo(),
					system.getNumberOfRecProcessed(),
					geoRecordsLine
				);
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.error("Failed to generate CSV Template file for Systems", e);
			throw new InternalServerErrorException("Failed to generate CSV Template file for Systems");
		}

		return byteArrayInputStream;
	}

	/**
	 * Get Data content for Download
	 */
	public ByteArrayInputStream getOrganizationsDownloadData(OrganizationType organizationType) {

		// String templateContent = "Business Unit Name,Business Unit Description,Parent Business Unit";
		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Writer writer = new OutputStreamWriter(outputStream);
			CSVFormat csvFormat;
			CSVPrinter csvPrinter;

			Long organizationId = organizationService.getCurrentOrganizationId();
			List<Organizations> organizationList = organizationRepository.getListForRootOrganization(organizationId, organizationType);

			if (OrganizationType.Vendor.equals(organizationType)) {

				csvFormat = CSVFormat.DEFAULT.withHeader(
					ORGANIZATION_NAME_HEADER,
					ORGANIZATION_DESCRIPTION_HEADER,
					ORGANIZATION_PARENT_NAME_HEADER,
					ORGANIZATION_STATUS_HEADER,
					ORGANIZATION_OWNER_NAME_HEADER,
					ORGANIZATION_OWNER_EMAIL_HEADER,
					ORGANIZATION_INDUSTRY_HEADER,
					ORGANIZATION_TECHNOLOGIES_HEADER,
					ORGANIZATION_SITE_HEADER,
					ORGANIZATION_PHONE_HEADER,
					ORGANIZATION_COUNTRY_HEADER,
					ORGANIZATION_STATE_HEADER,
					ORGANIZATION_CITY_HEADER,
					ORGANIZATION_ADDRESS1_HEADER,
					ORGANIZATION_ADDRESS2_HEADER,
					ORGANIZATION_CURRENCY_HEADER,
					ORGANIZATION_LANGUAGE_HEADER,
					ORGANIZATION_ZIP_HEADER,
					ORGANIZATION_TAX_HEADER,
					ORGANIZATION_VAT_HEADER,
					ORGANIZATION_NOTES_HEADER,
					ORGANIZATION_CLOUD_HEADER,
					VENDOR_IS_TECHNOLOGY_HEADER,
					VENDOR_IS_SYSTEM_HEADER,
					VENDOR_IS_SERVICE_HEADER
				);

				csvPrinter = new CSVPrinter(writer, csvFormat);

				for (Organizations organization : organizationList) {
					List<String> technologieNames = organization.getTechnologies().stream().map(item -> item.getName()).collect(Collectors.toList());
					String technologiesString = StringUtils.join(technologieNames, ";");

					csvPrinter.printRecord(
						organization.getName(),
						organization.getDescription(),
						getRootParentsString(organization),
						organization.getStatus() != null ? organization.getStatus().getName() : "",
						organization.getOwner() != null ? organization.getOwner().getFullName() : "",
						organization.getOwner() != null ? organization.getOwner().getEmail() : "",
						organization.getIndustry() != null ? organization.getIndustry().getName() : null,
						technologiesString,
						organization.getSite(),
						organization.getPhone(),
						organization.getCountry() != null ? organization.getCountry().getName() : null,
						organization.getState() != null ? organization.getState().getName() : null,
						organization.getCity() != null ? organization.getCity().getName() : null,
						organization.getStreetAddress1(),
						organization.getStreetAddress2(),
						organization.getCurrency() != null ? organization.getCurrency().getName() : null,
						organization.getLanguage() != null ? organization.getLanguage().getName() : null,
						organization.getZip(),
						organization.getTaxId(),
						organization.getVatId(),
						organization.getNotes(),
						Boolean.TRUE.equals(organization.getIsCloudVendor()) ? "YES" : "NO",
						Boolean.TRUE.equals(organization.getIsTechnologyVendor()) ? "YES" : "NO",
						Boolean.TRUE.equals(organization.getIsSystemVendor()) ? "YES" : "NO",
						Boolean.TRUE.equals(organization.getIsServiceVendor()) ? "YES" : "NO"
					);
				}


			} else {

				csvFormat = CSVFormat.DEFAULT.withHeader(
					ORGANIZATION_NAME_HEADER,
					ORGANIZATION_DESCRIPTION_HEADER,
					ORGANIZATION_PARENT_NAME_HEADER,
					ORGANIZATION_STATUS_HEADER,
					ORGANIZATION_OWNER_NAME_HEADER,
					ORGANIZATION_OWNER_EMAIL_HEADER,
					ORGANIZATION_INDUSTRY_HEADER,
					ORGANIZATION_TECHNOLOGIES_HEADER,
					ORGANIZATION_SITE_HEADER,
					ORGANIZATION_PHONE_HEADER,
					ORGANIZATION_COUNTRY_HEADER,
					ORGANIZATION_STATE_HEADER,
					ORGANIZATION_CITY_HEADER,
					ORGANIZATION_ADDRESS1_HEADER,
					ORGANIZATION_ADDRESS2_HEADER,
					ORGANIZATION_CURRENCY_HEADER,
					ORGANIZATION_LANGUAGE_HEADER,
					ORGANIZATION_ZIP_HEADER,
					ORGANIZATION_TAX_HEADER,
					ORGANIZATION_VAT_HEADER,
					ORGANIZATION_NOTES_HEADER,
					ORGANIZATION_REVENUE_HEADER,
					ORGANIZATION_LINKS_TO_CONTRACTS_HEADER
				);

				csvPrinter = new CSVPrinter(writer, csvFormat);

				for (Organizations organization : organizationList) {
					List<String> technologieNames = organization.getTechnologies().stream().map(item -> item.getName()).collect(Collectors.toList());
					String technologiesString = StringUtils.join(technologieNames, ";");

					csvPrinter.printRecord(
						organization.getName(),
						organization.getDescription(),
						getRootParentsString(organization),
						organization.getStatus() != null ? organization.getStatus().getName() : "",
						organization.getOwner() != null ? organization.getOwner().getFullName() : "",
						organization.getOwner() != null ? organization.getOwner().getEmail() : "",
						organization.getIndustry() != null ? organization.getIndustry().getName() : null,
						technologiesString,
						organization.getSite(),
						organization.getPhone(),
						organization.getCountry() != null ? organization.getCountry().getName() : null,
						organization.getState() != null ? organization.getState().getName() : null,
						organization.getCity() != null ? organization.getCity().getName() : null,
						organization.getStreetAddress1(),
						organization.getStreetAddress2(),
						organization.getCurrency() != null ? organization.getCurrency().getName() : null,
						organization.getLanguage() != null ? organization.getLanguage().getName() : null,
						organization.getZip(),
						organization.getTaxId(),
						organization.getVatId(),
						organization.getNotes(),
						organization.getAverageRevenue(),
						"" // TODO Add ORGANIZATION_LINKS_TO_CONTRACTS_HEADER
					);
				}
			}

			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			log.error("Failed to generate CSV Template file for Organizations", e);
			throw new InternalServerErrorException("Failed to generate CSV Template file for Organizations");
		}

		return byteArrayInputStream;
	}

	/**
	 * Return String of parents for Organization
	 * @param organization
	 * @return
	 */
	private String getRootParentsString(Organizations organization) {
		String result = "";

		int i = 0;
		Organizations parent = organization.getParent();
		while (parent != null) {
			result += (StringUtils.isEmpty(result) ? "" : ";") + parent.getName();

			parent = parent.getParent();

			if (++i > 64) break;
		}

		return result;
	}

	/**
	 * Insert business unit data from CSV file
	 */
	@Transactional
	public ImportResultDTO importOrganizationsFromCSVFile(InputStream fileContentStream, OrganizationType organizationType) {
		ImportResultDTO result = new ImportResultDTO();
		Organizations organization = organizationService.getCurrentOrganizationEntity();

		try {

			log.info(String.format("#### Applying DEMO data for %s: %s, %s", organizationType, organization.getId(), organization.getName()));
			Map<String, Organizations> organizationsHashMap = new HashMap<>();
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			log.info(String.format("#### Loaded CSV records: %s", csvRecordList.size()));
			for (CSVRecord csvRecord : csvRecordList) {
				// Accessing values by Header names
				String name = Optional.ofNullable(csvRecord.get(ORGANIZATION_NAME_HEADER)).orElse("").trim();
				String description = csvRecord.get(ORGANIZATION_DESCRIPTION_HEADER);
				String ownerEmail = "";
				if (csvRecord.isMapped(ORGANIZATION_OWNER_EMAIL_HEADER)) {
					ownerEmail = csvRecord.get(ORGANIZATION_OWNER_EMAIL_HEADER);
				}
				String ownerName = "";
				if (csvRecord.isMapped(ORGANIZATION_OWNER_NAME_HEADER)) {
					ownerName = csvRecord.get(ORGANIZATION_OWNER_NAME_HEADER);
				}
				String parentName = "";
				if (csvRecord.isMapped(ORGANIZATION_PARENT_NAME_HEADER)) {
					parentName = csvRecord.get(ORGANIZATION_PARENT_NAME_HEADER);
				}
				String statusName = "";
				if (csvRecord.isMapped(ORGANIZATION_STATUS_HEADER)) {
					statusName = StringUtils.trim(csvRecord.get(ORGANIZATION_STATUS_HEADER));
				}

				Organizations organizationParent = organizationService.getParentByPath(parentName, organization.getId());
				Optional<Organizations> organizationItem = Optional.empty();
				if (organizationParent != null) {
					organizationItem = organizationRepository.getByParentNameForRootOrganizationAndType(name, organizationParent.getId(), organization.getId(), organizationType);
				} else {
					organizationItem = organizationRepository.getByNameAndNoParentForRootOrganizationAndType(name, organization.getId(), organizationType);
				}
				String isCloudOrgString = "";
				if (csvRecord.isMapped(ORGANIZATION_CLOUD_HEADER)) {
					isCloudOrgString = csvRecord.get(ORGANIZATION_CLOUD_HEADER);
				}

				VendorEditDTO itemDTO;
				if (organizationItem.isEmpty()) {
					itemDTO = new VendorEditDTO();
					itemDTO.setName(name);
					itemDTO.setOrganizationType(organizationType);
				} else {
					itemDTO = new VendorEditDTO(organizationItem.get());
				}

				// Get Business Unit Data
				Optional<Users> owner = (StringUtils.isNotEmpty(ownerEmail)) ? userRepository.findFirstByEmailIgnoreCaseAndOrganization(ownerEmail, organization) : Optional.empty();
				if (owner.isEmpty() && StringUtils.isNotEmpty(ownerName)) owner = userRepository.findFirstByFullNameAndOrganization(ownerName, organization);

				// Set status for the Organization
				if (StringUtils.isNotEmpty(statusName)) {
					Optional<Status> statusOptional = statusRepository.findFirstByName(statusName);
					if (statusOptional.isPresent()) {
						itemDTO.setStatus(new ItemViewDTO<>(statusOptional.get()));
					}
				}

				// Fill item info
				if (StringUtils.isNotEmpty(description)) itemDTO.setDescription(description);
				if (owner.isPresent()) itemDTO.setOwner(new UserRefDTO(owner.get()));
				if ("yes".equalsIgnoreCase(isCloudOrgString)) itemDTO.setIsCloudVendor(true);
				if ("no".equalsIgnoreCase(isCloudOrgString)) itemDTO.setIsCloudVendor(false);

				// Set Industry header
				if (csvRecord.isMapped(ORGANIZATION_INDUSTRY_HEADER)) {
					Optional<Industries> industryItem = industryRepository.findFirstByName(csvRecord.get(ORGANIZATION_INDUSTRY_HEADER));
					if (industryItem.isPresent()) {
						itemDTO.setIndustry(new IndustryRefDTO(industryItem.get()));
					}
				}

				// Set Industry header
				if (csvRecord.isMapped(ORGANIZATION_TECHNOLOGIES_HEADER)) {
					String[] technologyItemNames = csvRecord.get(ORGANIZATION_TECHNOLOGIES_HEADER).split(CSVUtils.LIST_SEPARATOR);
					if (technologyItemNames != null && technologyItemNames.length > 0) {
						List<String> technologyNameList = Arrays.stream(technologyItemNames).map(itemName -> itemName.trim()).filter(itemName -> StringUtils.isNotEmpty(itemName)).collect(Collectors.toList());
						if (technologyNameList != null && technologyNameList.size() > 0) {
							Set<TechnologyRefDTO> technologyRefList = technologyRepository.getItemsByOrganizationAndNames(organization.getId(), technologyNameList).stream().map(item -> new TechnologyRefDTO(item)).collect(Collectors.toSet());
							if (technologyRefList.size() > 0) {
								itemDTO.setTechnologies(technologyRefList);
							}
						}
					}
				}

				if (csvRecord.isMapped(VENDOR_IS_TECHNOLOGY_HEADER) && "yes".equalsIgnoreCase(csvRecord.get(VENDOR_IS_TECHNOLOGY_HEADER).trim())) {
					itemDTO.setIsTechnologyVendor(true);
				} else {
					itemDTO.setIsTechnologyVendor(false);
				}

				if (csvRecord.isMapped(VENDOR_IS_SYSTEM_HEADER) && "yes".equalsIgnoreCase(csvRecord.get(VENDOR_IS_SYSTEM_HEADER).trim())) {
					itemDTO.setIsSystemVendor(true);
				} else {
					itemDTO.setIsSystemVendor(false);
				}

				if (csvRecord.isMapped(VENDOR_IS_SERVICE_HEADER) && "yes".equalsIgnoreCase(csvRecord.get(VENDOR_IS_SERVICE_HEADER).trim())) {
					itemDTO.setIsServiceVendor(true);
				} else {
					itemDTO.setIsServiceVendor(false);
				}

				if (csvRecord.isMapped(ORGANIZATION_SITE_HEADER)) {
					itemDTO.setSite(csvRecord.get(ORGANIZATION_SITE_HEADER).trim());
				}

				if (csvRecord.isMapped(ORGANIZATION_PHONE_HEADER)) {
					itemDTO.setPhone(csvRecord.get(ORGANIZATION_PHONE_HEADER).trim());
				}

				// Country, City, State
				if (csvRecord.isMapped(ORGANIZATION_COUNTRY_HEADER)) {
					String countryName = csvRecord.get(ORGANIZATION_COUNTRY_HEADER).trim();
					Optional<Country> countryOpt = countryRepository.findFirstByName(countryName);
					if (countryOpt.isPresent()) {
						itemDTO.setCountry(new ItemViewDTO<>(countryOpt.get()));

						if (csvRecord.isMapped(ORGANIZATION_STATE_HEADER)) {
							String stateName = csvRecord.get(ORGANIZATION_STATE_HEADER).trim();
							Optional<State> stateOpt = stateRepository.findFirstByNameAndCountry(stateName, countryOpt.get());
							if (stateOpt.isPresent()) {
								itemDTO.setState(new ItemViewDTO<>(stateOpt.get()));
							}
						}

						if (csvRecord.isMapped(ORGANIZATION_CITY_HEADER)) {
							String cityName = csvRecord.get(ORGANIZATION_CITY_HEADER).trim();
							Optional<City> cityOpt = cityRepository.findFirstByNameAndCountry(cityName, countryOpt.get());
							if (cityOpt.isPresent()) {
								itemDTO.setCity(new ItemViewDTO<>(cityOpt.get()));
							}
						}
					}
				}

				if (csvRecord.isMapped(ORGANIZATION_ADDRESS1_HEADER)) {
					itemDTO.setStreetAddress1(csvRecord.get(ORGANIZATION_ADDRESS1_HEADER).trim());
				}

				if (csvRecord.isMapped(ORGANIZATION_ADDRESS2_HEADER)) {
					itemDTO.setStreetAddress2(csvRecord.get(ORGANIZATION_ADDRESS2_HEADER).trim());
				}

				// Currency, Language
				if (csvRecord.isMapped(ORGANIZATION_CURRENCY_HEADER)) {
					String currencyName = csvRecord.get(ORGANIZATION_CURRENCY_HEADER).trim();
					Optional<Currency> currencyOpt = currencyRepository.findFirstByName(currencyName);
					if (currencyOpt.isPresent()) {
						itemDTO.setCurrency(new CurrencyViewDTO(currencyOpt.get()));
					}
				}

				if (csvRecord.isMapped(ORGANIZATION_LANGUAGE_HEADER)) {
					String languageName = csvRecord.get(ORGANIZATION_LANGUAGE_HEADER).trim();
					Optional<Language> languageOpt = languageRepository.findFirstByName(languageName);
					if (languageOpt.isPresent()) {
						itemDTO.setLanguage(new ItemViewDTO<>(languageOpt.get()));
					}
				}

				if (csvRecord.isMapped(ORGANIZATION_TAX_HEADER)) {
					itemDTO.setTaxId(csvRecord.get(ORGANIZATION_TAX_HEADER).trim());
				}

				if (csvRecord.isMapped(ORGANIZATION_VAT_HEADER)) {
					itemDTO.setVatId(csvRecord.get(ORGANIZATION_VAT_HEADER).trim());
				}

				if (OrganizationType.Vendor.equals(organizationType) && csvRecord.isMapped(ORGANIZATION_REVENUE_HEADER)) {
					try {
						itemDTO.setAverageRevenue(Double.parseDouble(csvRecord.get(ORGANIZATION_REVENUE_HEADER).trim()));
					} catch (NumberFormatException e) {}
				}

				if (csvRecord.isMapped(ORGANIZATION_ZIP_HEADER)) {
					itemDTO.setZip(csvRecord.get(ORGANIZATION_ZIP_HEADER).trim());
				}

				if (csvRecord.isMapped(ORGANIZATION_NOTES_HEADER)) {
					itemDTO.setNotes(csvRecord.get(ORGANIZATION_NOTES_HEADER).trim());
				}


				// Create Item
				OrganizationEditDTO organizationItemDTO = null;
				if (OrganizationType.Vendor.equals(organizationType)) {
					organizationItemDTO = organizationItem.isEmpty() ? vendorService.createVendor(itemDTO) : vendorService.updateVendor(itemDTO);
				} else if (OrganizationType.Subsidiary.equals(organizationType)) {
					organizationItemDTO = organizationItem.isEmpty() ? organizationService.createSubsidiary(itemDTO) : organizationService.updateSubsidiary(itemDTO);
				}
				Organizations organizationItemEntity = organizationRepository.findById(organizationItemDTO.getId()).get();

				// Put item to the map
				if (StringUtils.isNotEmpty(parentName)) organizationsHashMap.put(name + "_" + parentName, organizationItemEntity);

				if (organizationItem.isEmpty()) {
					result.getCreated().add(new ItemViewDTO(organizationItemEntity));
					result.getMessages().add(MessageFormat.format("New Organization [{0}] created successfully.", name));
				} else {
					result.getMessages().add(MessageFormat.format("Organization [{0}] already exists in the organization. Updating.", name));
					result.getUpdated().add(new ItemViewDTO(organizationItemEntity));
				}
			}

			// Proceed with Parent units
			for (CSVRecord csvRecord : csvRecordList) {
				// Accessing values by Header names
				String name = Optional.ofNullable(csvRecord.get(ORGANIZATION_NAME_HEADER)).orElse("").trim();
				String parentName = "";
				if (csvRecord.isMapped(ORGANIZATION_PARENT_NAME_HEADER)) {
					parentName = csvRecord.get(ORGANIZATION_PARENT_NAME_HEADER);
				}

				if (StringUtils.isNotEmpty(parentName)) {
					Organizations organizations = organizationsHashMap.get(name + "_" + parentName);

					Organizations organizationParentItem = organizationService.getParentByPath(parentName, organization.getId());
					if (organizationParentItem != null) {
						organizations.setParent(organizationParentItem);
						organizationRepository.save(organizations);
					} else {
						result.getMessages().add(MessageFormat.format("Parent [{0}] for organization [{1}] not found. Skipping.", parentName, name));
					}
				}
			}

		} catch (IOException e) {
			log.error("Failed to import Organizations from CSV", e);
		}

		return result;
	}

	/**
	 * Insert GDPR data from CSV file
	 */
	@Transactional
	public ImportResultDTO importGDPRFromCSVFile(MultipartFile file) {

		ImportResultDTO result = new ImportResultDTO();
		Organizations organization = organizationService.getCurrentOrganizationEntity();

		try {

			CSVParser csvParser = CSVUtils.createCSVParser(file.getInputStream());
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			for (CSVRecord csvRecord : csvRecordList) {
				// Accessing values by Header names
				String name = Optional.ofNullable(csvRecord.get(ORGANIZATION_NAME_HEADER)).orElse("").trim();
				String description = csvRecord.get(ORGANIZATION_DESCRIPTION_HEADER);
			}

		} catch (IOException e) {
			log.error("Failed to load GDPR's from CSV file", e);
		}

		return result;
	}

}
