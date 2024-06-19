package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.country.CountryViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessRefDTO;
import com.cyberintech.vrisk.server.model.dto.state.StateViewDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemEditDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemGeoParametersDTO;
import com.cyberintech.vrisk.server.model.dto.systems.TechnologyAssetEditDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyClassTypeRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologySubcategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.DeploymentType;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemStatus;
import com.cyberintech.vrisk.server.model.jpa.domains.SystemType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import com.cyberintech.vrisk.server.service.utils.ImportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.util.*;

@Service
@Slf4j
public class ImportDataRecordsService {
	@Autowired
	private TechnologyCategoryImportMappingRepository technologyCategoryImportMappingRepository;

	@Autowired
	private AssociateVendorService associateVendorService;

	@Autowired
	private BusinessUnitService businessUnitService;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private DataAssetClassificationRepository dataAssetClassificationRepository;

	@Autowired
	private DataTypeClassificationRepository dataTypeClassificationRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private ProcessRepository processRepository;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private SystemRepository systemRepository;

	@Autowired
	private SystemsService systemsService;

	@Autowired
	private TechnologyRepository technologyRepository;

	@Autowired
	private TechnologyAssetRepository technologyAssetRepository;

	@Autowired
	private TechnologyAssetsService technologyAssetsService;

	@Autowired
	private TechnologyCategoryRepository technologyCategoryRepository;

	@Autowired
	private TechnologyCategoryService technologyCategoryService;

	@Autowired
	private TechnologyClassTypeService technologyClassTypeService;

	@Autowired
	private TechnologySubcategoryService technologySubcategoryService;

	@Autowired
	private TechnologyService technologyService;

	@Autowired
	private UserRepository userRepository;


	/**
	 * Insert some data from CSV file
	 */
	@Transactional
	public ImportResultDTO importTechnologyAssetsFromCSVItems(List<CSVRecord> csvRecordList) {

		/*
		if (!adminUserService.hasRole(RoleType.ADMIN) && !adminUserService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			throw new ForbiddenException("You are not allowed to import Systems!", ApplicationExceptionCodes.USER_MANAGEMENT_FORBIDDEN);
		}
		*/

		ImportResultDTO result = new ImportResultDTO();
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		Map<String, TechnologyAssets> systemsCache = new HashMap<>();
		Map<String, Organizations> vendorsCache = new HashMap<>();
		Map<String, Technologies> technologiesCache = new HashMap<>();
		Map<String, TechnologyCategories> technologyCategoriesCache = new HashMap<>();
		Map<String, Users> usersCache = new HashMap<>();
		Map<String, DataAssetClassification> dataAssetClassificationCache = new HashMap<>();
		Map<String, DataTypeClassification> dataTypeClassificationCache = new HashMap<>();

		int currentItem = 0;
		long currentTime = System.currentTimeMillis();
		// Proceed with user management
		for (CSVRecord csvRecord : csvRecordList) {

			// Accessing values by Header names
			String systemName = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_NAME_HEADER);
			String systemDesc = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_DESCRIPTION_HEADER);
			String systemVersionNumber = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_VERSION_NUMBER_HEADER);
			String systemOwnerName = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_OWNER_NAME_HEADER);
			String systemOwnerEmail = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_OWNER_EMAIL_HEADER);
			String infosecPersonName = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_INFOSEC_PERSON_NAME_HEADER);
			String infosecPersonEmail = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_INFOSEC_PERSON_EMAIL_HEADER);
			String systemStatus = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_STATUS_HEADER);
			String businessUnitLocation = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_BU_LOCATION_HEADER);
			String businessUnitDivision = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_BU_DIVISION_HEADER);
			String businessUnitPath = (csvRecord.isMapped(ImportDataService.SYSTEM_BU_PATH_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_BU_PATH_HEADER)).orElse("").trim() : "";
			String associateVendorsString = (csvRecord.isMapped(ImportDataService.SYSTEM_ASSOCIATE_VENDORS_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_ASSOCIATE_VENDORS_HEADER)).orElse("").trim() : "";
			String costToRestoreString = (csvRecord.isMapped(ImportDataService.SYSTEM_COST_TO_RESTORE_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_COST_TO_RESTORE_HEADER)).orElse("").trim() : "";
			String deploymentTypeString = (csvRecord.isMapped(ImportDataService.SYSTEM_ON_DEPLOYMENT_TYPE_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_ON_DEPLOYMENT_TYPE_HEADER)).orElse("").trim() : "";
			String maAssetString = (csvRecord.isMapped(ImportDataService.SYSTEM_MA_ASSET_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_MA_ASSET_HEADER)).orElse("").trim() : "";
			String systemTypeString = (csvRecord.isMapped(ImportDataService.SYSTEM_SYSTEM_TYPE_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_SYSTEM_TYPE_HEADER)).orElse("").trim() : "";
			String rtoString = (csvRecord.isMapped(ImportDataService.SYSTEM_RTO_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_RTO_HEADER)).orElse("").trim() : "";
			String rpoString = (csvRecord.isMapped(ImportDataService.SYSTEM_RPO_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_RPO_HEADER)).orElse("").trim() : "";
			String recordsProcessedString = (csvRecord.isMapped(ImportDataService.SYSTEM_RECORDS_PROCESSED_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_RECORDS_PROCESSED_HEADER)).orElse("").trim() : "";
			String geoRecordsProcessedString = (csvRecord.isMapped(ImportDataService.SYSTEM_GEO_RECORDS_PROCESSED_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_GEO_RECORDS_PROCESSED_HEADER)).orElse("").trim() : "";
			// String etl = (csvRecord.isMapped(ImportDataService.SYSTEM_ETL_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_ETL_HEADER)).orElse("").trim() : "";

			if (StringUtils.isNotEmpty(systemName)) {
				String scopeName = MessageFormat.format("System [{0}]", systemName);

				TechnologyAssets technologyAssetDetails = null;
				boolean isNew = true;
				if (systemsCache.containsKey(systemName)) {
					technologyAssetDetails = systemsCache.get(systemName);
					isNew = false;
				} else {
					Optional<TechnologyAssets> systemDetailsOpt = technologyAssetRepository.getFirstByNameForOrganization(systemName, organization.getId());
					if (systemDetailsOpt.isPresent()) {
						technologyAssetDetails = systemDetailsOpt.get();
						systemsCache.put(systemName, technologyAssetDetails);
						isNew = false;
					}
				}

				TechnologyAssetEditDTO technologyAssetEditDTO;
				if (technologyAssetDetails == null) {
					technologyAssetEditDTO = new TechnologyAssetEditDTO();
					technologyAssetEditDTO.setName(systemName);
					technologyAssetEditDTO.setSystemStatus(SystemStatus.ACTIVE);
				} else {
					technologyAssetEditDTO = new TechnologyAssetEditDTO(technologyAssetDetails);
				}

				// if ("yes".equalsIgnoreCase(etl)) systemEditDTO.setIsEtl(true);
				technologyAssetEditDTO.setDescription(systemDesc);

				technologyAssetEditDTO.setVersionNumber(systemVersionNumber);

				/*
				public static final String ImportDataService.SYSTEM_UPDATED_DATE_HEADER = "Updated Date";
				public static final String ImportDataService.SYSTEM_UPDATED_BY_HEADER = "Updated By";
				public static final String ImportDataService.SYSTEM_CREATED_DATE_HEADER = "Created Date";
				public static final String ImportDataService.SYSTEM_CREATED_BY_HEADER = "Created By";
				 */

				// Set Digital Asset
				Optional<Users> systemOwnerDetails = getUserFromNames(systemOwnerEmail, organization, systemOwnerName, usersCache);
				if (systemOwnerDetails.isPresent()) {
					technologyAssetEditDTO.setOwner(new UserRefDTO(systemOwnerDetails.get()));
				} else if (StringUtils.isNotEmpty(systemOwnerEmail)) {
					result.getMessages().add(MessageFormat.format("WARNING: System [{0}]. Failed to find System Owner User: {1}, SKIPPING", systemName, systemOwnerEmail));
				}

				Optional<Users> systemInfoSecDetails = getUserFromNames(infosecPersonEmail, organization, infosecPersonName, usersCache);
				if (systemInfoSecDetails.isPresent()) {
					technologyAssetEditDTO.setInfosecFocalPerson(new UserRefDTO(systemInfoSecDetails.get()));
				} else if (StringUtils.isNotEmpty(infosecPersonEmail)) {
					result.getMessages().add(MessageFormat.format("WARNING: System [{0}]. Failed to find InfoSEC User: {1}, SKIPPING", systemName, infosecPersonEmail));
				}

				String technologyCategory = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_TECHNOLOGY_CATEGORY_HEADER);
				String technologyName = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_TECHNOLOGIES_HEADER);
				Optional<TechnologyCategoryImportMappings> technologyImportMappingsOpt = technologyCategoryImportMappingRepository.getFirstByNameAndTechnologyAndOrganization(organization.getId(), technologyCategory, technologyName);
				TechnologyCategoryImportMappings technologyImportMappings = technologyImportMappingsOpt.orElse(TechnologyCategoryImportMappings.builder().targetTechnologyCategory(technologyCategory).targetTechnologyName(technologyName).build());

				if (StringUtils.isNotEmpty(technologyImportMappings.getTargetTechnologySubcategory()) && StringUtils.isNotEmpty(technologyImportMappings.getTargetTechnologyClass())) {
					TechnologyCategories technologyCategoryDetails = technologyCategoryService.getOrCreate(organization.getId(), technologyImportMappings.getTargetTechnologyCategory());
					TechnologySubcategories technologySubcategoryDetails = technologySubcategoryService.getOrCreate(organization.getId(), technologyCategoryDetails.getId(), technologyImportMappings.getTargetTechnologySubcategory());
					TechnologyClassTypes technologyClassTypeDetails = technologyClassTypeService.getOrCreate(organization.getId(), technologyCategoryDetails.getId(), technologySubcategoryDetails.getId(), technologyImportMappings.getTargetTechnologyClass());
					Technologies technologyDetails = technologyService.getOrCreate(organization.getId(), technologyCategoryDetails, technologySubcategoryDetails.getId(), technologyClassTypeDetails.getId(), technologyImportMappings.getTargetTechnologyName());

					technologyAssetEditDTO.setTechnologyCategory(new TechnologyCategoryRefDTO(technologyCategoryDetails));
					technologyAssetEditDTO.setTechnologySubcategory(new TechnologySubcategoryRefDTO(technologySubcategoryDetails));
					technologyAssetEditDTO.setTechnologyClassType(new TechnologyClassTypeRefDTO(technologyClassTypeDetails));
					technologyAssetEditDTO.setTechnology(new TechnologyRefDTO(technologyDetails));
				} else {
					// Technologies
					Pair<List<TechnologyRefDTO>, List<String>> technologies = ImportUtils.loadTechnologies(csvRecord, ImportDataService.SYSTEM_TECHNOLOGIES_HEADER, scopeName,
						organization.getId(), technologyRepository, ImportDataService.SYSTEM_TECHNOLOGY_CATEGORY_HEADER, technologyCategoryRepository,
						technologiesCache, technologyCategoriesCache);
					if (CollectionUtils.isNotEmpty(technologies.getLeft())) {
						technologyAssetEditDTO.setTechnology(technologies.getLeft().get(0));
						result.getMessages().addAll(technologies.getRight());
					}

					if (!technologyCategoriesCache.containsKey(technologyCategory)) {
						technologyCategoriesCache.put(technologyCategory, technologyCategoryService.getOrCreate(organization.getId(), technologyCategory));
					}

					if (technologyCategoriesCache.containsKey(technologyCategory)) technologyAssetEditDTO.setTechnologyCategory(new TechnologyCategoryRefDTO(technologyCategoriesCache.get(technologyCategory)));
				}

				if (csvRecord.isMapped(ImportDataService.SYSTEM_EOL_DATE_HEADER)) {
					technologyAssetEditDTO.setEolDate(ImportUtils.loadDate(csvRecord.get(ImportDataService.SYSTEM_EOL_DATE_HEADER)));
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_WARRANTY_EXPIRATION_HEADER)) {
					technologyAssetEditDTO.setWarrantyExpiration(ImportUtils.loadDate(csvRecord.get(ImportDataService.SYSTEM_WARRANTY_EXPIRATION_HEADER)));
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_ASSET_NAME_HEADER)) {
					technologyAssetEditDTO.setAssetName(csvRecord.get(ImportDataService.SYSTEM_ASSET_NAME_HEADER));
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_IP_ADDRESS_HEADER)) {
					technologyAssetEditDTO.setIpAddress(csvRecord.get(ImportDataService.SYSTEM_IP_ADDRESS_HEADER));
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_SERIAL_NUMBER_HEADER)) {
					technologyAssetEditDTO.setSerialNumber(csvRecord.get(ImportDataService.SYSTEM_SERIAL_NUMBER_HEADER));
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_ASSET_DOMAIN_FUNCTION_HEADER)) {
					technologyAssetEditDTO.setAssetDomainFunction(csvRecord.get(ImportDataService.SYSTEM_ASSET_DOMAIN_FUNCTION_HEADER));
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_OS_NAME_HEADER)) {
					technologyAssetEditDTO.setOsName(csvRecord.get(ImportDataService.SYSTEM_OS_NAME_HEADER));
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_LOCATION_HEADER)) {
					technologyAssetEditDTO.setLocation(csvRecord.get(ImportDataService.SYSTEM_LOCATION_HEADER));
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_HARDWARE_STATUS_HEADER)) {
					technologyAssetEditDTO.setHardwareSubstatus(csvRecord.get(ImportDataService.SYSTEM_HARDWARE_STATUS_HEADER));
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_DISCOVERY_SOURCE_HEADER)) {
					technologyAssetEditDTO.setDiscoverySource(csvRecord.get(ImportDataService.SYSTEM_DISCOVERY_SOURCE_HEADER));
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_COMPUTER_ID_HEADER)) {
					technologyAssetEditDTO.setDeviceId(csvRecord.get(ImportDataService.SYSTEM_COMPUTER_ID_HEADER));
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_OWNER_TYPE_HEADER)) {
					technologyAssetEditDTO.setOwnerType(csvRecord.get(ImportDataService.SYSTEM_OWNER_TYPE_HEADER));
				}

				SystemStatus status = SystemStatus.ofString(systemStatus);
				if (status != null) {
					technologyAssetEditDTO.setSystemStatus(status);
				}

				if (StringUtils.isNotEmpty(costToRestoreString)) {
					try {
						double costToRestore = Double.parseDouble(costToRestoreString);
						technologyAssetEditDTO.setCostToRestore(costToRestore);
					} catch (NumberFormatException exception) {
						log.info(String.format("## Failed to parse costToRestore Double for system %s: %s", systemName, costToRestoreString));
					}
				}
				if (StringUtils.isNotEmpty(rtoString)) {
					try {
						double rto = Double.parseDouble(rtoString);
						technologyAssetEditDTO.setRto(rto);
					} catch (NumberFormatException exception) {
						log.info(String.format("## Failed to parse RTO Double for system %s: %s", systemName, rtoString));
					}
				}
				if (StringUtils.isNotEmpty(rpoString)) {
					try {
						double rpo = Double.parseDouble(rpoString);
						technologyAssetEditDTO.setRpo(rpo);
					} catch (NumberFormatException exception) {
						log.info(String.format("## Failed to parse RPO Double for system %s: %s", systemName, rpoString));
					}
				}
				if (StringUtils.isNotEmpty(recordsProcessedString)) {
					try {
						double recordsProcessed = Double.parseDouble(recordsProcessedString);
						technologyAssetEditDTO.setNumberOfRecProcessed(recordsProcessed);
					} catch (NumberFormatException exception) {
						log.info(String.format("## Failed to parse numberOfRecProcessed Double for system %s: %s", systemName, recordsProcessedString));
					}
				}
				if (StringUtils.isNotEmpty(maAssetString)) {
					technologyAssetEditDTO.setIsMAAsset(maAssetString.equalsIgnoreCase("yes"));
				}
				if (StringUtils.isNotBlank(deploymentTypeString)) {
					deploymentTypeString = deploymentTypeString.toUpperCase().replace(" ", "_");
					technologyAssetEditDTO.setDeploymentType(DeploymentType.of(deploymentTypeString));
				}
				if (StringUtils.isNotBlank(systemTypeString)) {
					systemTypeString = systemTypeString.toUpperCase().replace(" ", "_");
					technologyAssetEditDTO.setSystemType(SystemType.of(systemTypeString, null));
				}

				// Set Business Unit Path
				boolean isBusinessUnitPathReverse = false;
				if (StringUtils.isNotEmpty(businessUnitLocation) && StringUtils.isNotEmpty(businessUnitDivision)) {
					businessUnitPath = businessUnitDivision + CSVUtils.PATH_SEPARATOR + businessUnitLocation;
					isBusinessUnitPathReverse = true;
				} else if (StringUtils.isNotEmpty(businessUnitDivision)) {
					businessUnitPath = businessUnitDivision;
					isBusinessUnitPathReverse = true;
				}

				if (StringUtils.isNotEmpty(businessUnitPath)) {
					BusinessUnits businessUnit = businessUnitService.getParentByPath(businessUnitPath, organization.getId(), isBusinessUnitPathReverse, CSVUtils.PATH_SEPARATOR);
					if (businessUnit != null) {
						technologyAssetEditDTO.setBusinessUnit(new BusinessUnitRefDTO(businessUnit));
					} else if (StringUtils.isNotEmpty(businessUnitPath)) {
						result.getMessages().add(MessageFormat.format("WARNING: System [{0}]. Failed to find Business Unit: {1}, SKIPPING", systemName, businessUnitPath));
					}
				}

				// Create associate Vendors
				if (StringUtils.isNotEmpty(associateVendorsString)) {

					String vendorName = associateVendorsString;
					Optional<Organizations> vendorOptional = Optional.empty();
					if (vendorsCache.containsKey(vendorName)) {
						vendorOptional = Optional.ofNullable(vendorsCache.get(vendorName));
					} else {
						Organizations vendorDetails = organizationService.getOrCreateVendor(organization, vendorName);
						vendorsCache.put(vendorName, vendorDetails);
						vendorOptional = Optional.ofNullable(vendorDetails);
					}

					if (vendorOptional.isPresent()) {
						technologyAssetEditDTO.setManufacturer(new OrganizationRefDTO(vendorOptional.get()));
						result.getMessages().add(MessageFormat.format("#### Added Manufacturer [{0}] to Technology Asset [{1}]", vendorName, systemName));
					}
				}

				TechnologyAssetEditDTO systemResult;
				if (technologyAssetEditDTO.getId() == null) {
					systemResult = technologyAssetsService.create(technologyAssetEditDTO);
					result.getCreated().add(new ItemViewDTO(systemResult.getId(), systemResult.getName()));
				} else {
					systemResult = technologyAssetsService.update(technologyAssetEditDTO);
					result.getUpdated().add(new ItemViewDTO(systemResult.getId(), systemResult.getName()));
				}

				currentItem++;
				if (currentItem % 100 == 0) {
					// long processingTime = (System.currentTimeMillis() - currentTime) / 1000;
					// log.info("Processed import records: {} in {} seconds", currentItem, processingTime);

					// currentTime = System.currentTimeMillis();
				}
			}
		}

		return result;
	}

	/**
	 * Insert some data from CSV file
	 */
	@Transactional
	public ImportResultDTO importSystemsFromCSVItems(List<CSVRecord> csvRecordList) {

		/*
		if (!adminUserService.hasRole(RoleType.ADMIN) && !adminUserService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			throw new ForbiddenException("You are not allowed to import Systems!", ApplicationExceptionCodes.USER_MANAGEMENT_FORBIDDEN);
		}
		*/

		ImportResultDTO result = new ImportResultDTO();
		Organizations organization = organizationService.getCurrentOrganizationEntity();
		Map<String, Systems> systemsCache = new HashMap<>();
		Map<String, Organizations> vendorsCache = new HashMap<>();
		Map<String, Technologies> technologiesCache = new HashMap<>();
		Map<String, TechnologyCategories> technologyCategoriesCache = new HashMap<>();
		Map<String, Users> usersCache = new HashMap<>();
		Map<String, DataAssetClassification> dataAssetClassificationCache = new HashMap<>();
		Map<String, DataTypeClassification> dataTypeClassificationCache = new HashMap<>();

		int currentItem = 0;
		long currentTime = System.currentTimeMillis();
		// Proceed with user management
		for (CSVRecord csvRecord : csvRecordList) {

			// Accessing values by Header names
			String systemName = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_NAME_HEADER);
			String systemDesc = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_DESCRIPTION_HEADER);
			String systemVersionNumber = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_VERSION_NUMBER_HEADER);
			String systemOwnerName = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_OWNER_NAME_HEADER);
			String systemOwnerEmail = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_OWNER_EMAIL_HEADER);
			String infosecPersonName = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_INFOSEC_PERSON_NAME_HEADER);
			String infosecPersonEmail = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_INFOSEC_PERSON_EMAIL_HEADER);
			String systemStatus = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_STATUS_HEADER);
			String businessUnitLocation = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_BU_LOCATION_HEADER);
			String businessUnitDivision = CSVUtils.getAsString(csvRecord, ImportDataService.SYSTEM_BU_DIVISION_HEADER);
			String businessUnitPath = (csvRecord.isMapped(ImportDataService.SYSTEM_BU_PATH_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_BU_PATH_HEADER)).orElse("").trim() : "";
			String associateVendorsString = (csvRecord.isMapped(ImportDataService.SYSTEM_ASSOCIATE_VENDORS_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_ASSOCIATE_VENDORS_HEADER)).orElse("").trim() : "";
			String costToRestoreString = (csvRecord.isMapped(ImportDataService.SYSTEM_COST_TO_RESTORE_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_COST_TO_RESTORE_HEADER)).orElse("").trim() : "";
			String deploymentTypeString = (csvRecord.isMapped(ImportDataService.SYSTEM_ON_DEPLOYMENT_TYPE_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_ON_DEPLOYMENT_TYPE_HEADER)).orElse("").trim() : "";
			String maAssetString = (csvRecord.isMapped(ImportDataService.SYSTEM_MA_ASSET_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_MA_ASSET_HEADER)).orElse("").trim() : "";
			String systemTypeString = (csvRecord.isMapped(ImportDataService.SYSTEM_SYSTEM_TYPE_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_SYSTEM_TYPE_HEADER)).orElse("").trim() : "";
			String rtoString = (csvRecord.isMapped(ImportDataService.SYSTEM_RTO_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_RTO_HEADER)).orElse("").trim() : "";
			String rpoString = (csvRecord.isMapped(ImportDataService.SYSTEM_RPO_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_RPO_HEADER)).orElse("").trim() : "";
			String recordsProcessedString = (csvRecord.isMapped(ImportDataService.SYSTEM_RECORDS_PROCESSED_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_RECORDS_PROCESSED_HEADER)).orElse("").trim() : "";
			String geoRecordsProcessedString = (csvRecord.isMapped(ImportDataService.SYSTEM_GEO_RECORDS_PROCESSED_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_GEO_RECORDS_PROCESSED_HEADER)).orElse("").trim() : "";
			// String etl = (csvRecord.isMapped(ImportDataService.SYSTEM_ETL_HEADER)) ? Optional.ofNullable(csvRecord.get(ImportDataService.SYSTEM_ETL_HEADER)).orElse("").trim() : "";

			if (StringUtils.isNotEmpty(systemName)) {
				String scopeName = MessageFormat.format("System [{0}]", systemName);

				Systems systemDetails = null;
				boolean isNew = true;
				if (systemsCache.containsKey(systemName)) {
					systemDetails = systemsCache.get(systemName);
					isNew = false;
				} else {
					Optional<Systems> systemDetailsOpt = systemRepository.getFirstByNameForOrganization(systemName, organization.getId());
					if (systemDetailsOpt.isPresent()) {
						systemDetails = systemDetailsOpt.get();
						systemsCache.put(systemName, systemDetails);
						isNew = false;
					}
				}

				SystemEditDTO systemEditDTO;
				if (systemDetails == null) {
					systemEditDTO = new SystemEditDTO();
					systemEditDTO.setName(systemName);
					systemEditDTO.setSystemStatus(SystemStatus.ACTIVE);
				} else {
					systemEditDTO = new SystemEditDTO(systemDetails);
				}

				// if ("yes".equalsIgnoreCase(etl)) systemEditDTO.setIsEtl(true);
				systemEditDTO.setDescription(systemDesc);

				systemEditDTO.setVersionNumber(systemVersionNumber);

				/*
				public static final String ImportDataService.SYSTEM_UPDATED_DATE_HEADER = "Updated Date";
				public static final String ImportDataService.SYSTEM_UPDATED_BY_HEADER = "Updated By";
				public static final String ImportDataService.SYSTEM_CREATED_DATE_HEADER = "Created Date";
				public static final String ImportDataService.SYSTEM_CREATED_BY_HEADER = "Created By";
				 */

				// Set Digital Asset
				Pair<DataAssetClassificationRefDTO, List<String>> dataAssetClassification = ImportUtils
					.loadDataAssetClassification(csvRecord, ImportDataService.SYSTEM_DIGITAL_ASSET_CLASS_HEADER, scopeName, organization.getId(), dataAssetClassificationRepository, dataAssetClassificationCache);
				systemEditDTO.setDataAssetClassification(dataAssetClassification.getLeft());
				result.getMessages().addAll(dataAssetClassification.getRight());

				Optional<Users> systemOwnerDetails = getUserFromNames(systemOwnerEmail, organization, systemOwnerName, usersCache);
				if (systemOwnerDetails.isPresent()) {
					systemEditDTO.setOwner(new UserRefDTO(systemOwnerDetails.get()));
				} else if (StringUtils.isNotEmpty(systemOwnerEmail)) {
					result.getMessages().add(MessageFormat.format("WARNING: System [{0}]. Failed to find System Owner User: {1}, SKIPPING", systemName, systemOwnerEmail));
				}

				Optional<Users> systemInfoSecDetails = getUserFromNames(infosecPersonEmail, organization, infosecPersonName, usersCache);
				if (systemInfoSecDetails.isPresent()) {
					systemEditDTO.setInfosecFocalPerson(new UserRefDTO(systemInfoSecDetails.get()));
				} else if (StringUtils.isNotEmpty(infosecPersonEmail)) {
					result.getMessages().add(MessageFormat.format("WARNING: System [{0}]. Failed to find InfoSEC User: {1}, SKIPPING", systemName, infosecPersonEmail));
				}

				// Data Classification
				Pair<List<DataTypeClassificationRefDTO>, List<String>> dataTypeClassifications = ImportUtils
					.loadDataTypeClassifications(csvRecord,
						ImportDataService.SYSTEM_DATA_CLASSIFICATION_HEADER, scopeName, organization.getId(),
						dataTypeClassificationRepository, dataTypeClassificationCache);
				systemEditDTO.setDataTypeClassifications(dataTypeClassifications.getLeft());
				result.getMessages().addAll(dataTypeClassifications.getRight());

				// Processes
				// TODO Cache Processes
				Pair<List<ProcessRefDTO>, List<String>> processes = ImportUtils.loadProcesses(csvRecord,
					ImportDataService.SYSTEM_PROCESSES_HEADER, scopeName, organization.getId(), processRepository);
				systemEditDTO.setProcesses(processes.getLeft());
				result.getMessages().addAll(processes.getRight());

				// Technologies
				Pair<List<TechnologyRefDTO>, List<String>> technologies = ImportUtils.loadTechnologies(csvRecord, ImportDataService.SYSTEM_TECHNOLOGIES_HEADER, scopeName,
					organization.getId(), technologyRepository, ImportDataService.SYSTEM_TECHNOLOGY_CATEGORY_HEADER, technologyCategoryRepository,
					technologiesCache, technologyCategoriesCache);
				systemEditDTO.setTechnologies(technologies.getLeft());
				result.getMessages().addAll(technologies.getRight());

				// Update first Technology with Enhanced fields
				Technologies technology = null;
				boolean isTechnologyChanged = false;
				if (systemEditDTO.getTechnologies() != null && systemEditDTO.getTechnologies().size() == 1) {
					Optional<Technologies> technologyOptional = technologyRepository.findById(systemEditDTO.getTechnologies().get(0).getId());
					if (technologyOptional.isPresent()) technology = technologyOptional.get();
				}

				if (csvRecord.isMapped(ImportDataService.SYSTEM_EOL_DATE_HEADER)) {
					systemEditDTO.setEolDate(ImportUtils.loadDate(csvRecord.get(ImportDataService.SYSTEM_EOL_DATE_HEADER)));
					if (technology != null && !Objects.equals(technology.getEolDate(), ImportUtils.loadDate(csvRecord.get(ImportDataService.SYSTEM_EOL_DATE_HEADER)))) {
						technology.setEolDate(ImportUtils.loadDate(csvRecord.get(ImportDataService.SYSTEM_EOL_DATE_HEADER)));
						isTechnologyChanged = true;
					}
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_WARRANTY_EXPIRATION_HEADER)) {
					systemEditDTO.setWarrantyExpiration(ImportUtils.loadDate(csvRecord.get(ImportDataService.SYSTEM_WARRANTY_EXPIRATION_HEADER)));
					if (technology != null && !Objects.equals(technology.getWarrantyExpiration(), ImportUtils.loadDate(csvRecord.get(ImportDataService.SYSTEM_WARRANTY_EXPIRATION_HEADER)))) {
						technology.setWarrantyExpiration(ImportUtils.loadDate(csvRecord.get(ImportDataService.SYSTEM_WARRANTY_EXPIRATION_HEADER)));
						isTechnologyChanged = true;
					}
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_IP_ADDRESS_HEADER)) {
					systemEditDTO.setIpAddress(csvRecord.get(ImportDataService.SYSTEM_IP_ADDRESS_HEADER));
					if (technology != null && !Objects.equals(technology.getIpAddress(), csvRecord.get(ImportDataService.SYSTEM_IP_ADDRESS_HEADER))) {
						technology.setIpAddress(csvRecord.get(ImportDataService.SYSTEM_IP_ADDRESS_HEADER));
						isTechnologyChanged = true;
					}
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_SERIAL_NUMBER_HEADER)) {
					systemEditDTO.setSerialNumber(csvRecord.get(ImportDataService.SYSTEM_SERIAL_NUMBER_HEADER));
					if (technology != null && !Objects.equals(technology.getSerialNumber(), csvRecord.get(ImportDataService.SYSTEM_SERIAL_NUMBER_HEADER))) {
						technology.setSerialNumber(csvRecord.get(ImportDataService.SYSTEM_SERIAL_NUMBER_HEADER));
						isTechnologyChanged = true;
					}
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_ASSET_DOMAIN_FUNCTION_HEADER)) {
					systemEditDTO.setAssetDomainFunction(csvRecord.get(ImportDataService.SYSTEM_ASSET_DOMAIN_FUNCTION_HEADER));
					if (technology != null && !Objects.equals(technology.getAssetDomainFunction(), csvRecord.get(ImportDataService.SYSTEM_ASSET_DOMAIN_FUNCTION_HEADER))) {
						technology.setAssetDomainFunction(csvRecord.get(ImportDataService.SYSTEM_ASSET_DOMAIN_FUNCTION_HEADER));
						isTechnologyChanged = true;
					}
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_OS_NAME_HEADER)) {
					systemEditDTO.setOsName(csvRecord.get(ImportDataService.SYSTEM_OS_NAME_HEADER));
					if (technology != null && !Objects.equals(technology.getOsName(), csvRecord.get(ImportDataService.SYSTEM_OS_NAME_HEADER))) {
						technology.setOsName(csvRecord.get(ImportDataService.SYSTEM_OS_NAME_HEADER));
						isTechnologyChanged = true;
					}
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_LOCATION_HEADER)) {
					systemEditDTO.setLocation(csvRecord.get(ImportDataService.SYSTEM_LOCATION_HEADER));
					if (technology != null && !Objects.equals(technology.getLocation(), csvRecord.get(ImportDataService.SYSTEM_LOCATION_HEADER))) {
						technology.setLocation(csvRecord.get(ImportDataService.SYSTEM_LOCATION_HEADER));
						isTechnologyChanged = true;
					}
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_HARDWARE_STATUS_HEADER)) {
					systemEditDTO.setHardwareSubstatus(csvRecord.get(ImportDataService.SYSTEM_HARDWARE_STATUS_HEADER));
					if (technology != null && !Objects.equals(technology.getHardwareSubstatus(), csvRecord.get(ImportDataService.SYSTEM_HARDWARE_STATUS_HEADER))) {
						technology.setHardwareSubstatus(csvRecord.get(ImportDataService.SYSTEM_HARDWARE_STATUS_HEADER));
						isTechnologyChanged = true;
					}
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_DISCOVERY_SOURCE_HEADER)) {
					systemEditDTO.setDiscoverySource(csvRecord.get(ImportDataService.SYSTEM_DISCOVERY_SOURCE_HEADER));
					if (technology != null && !Objects.equals(technology.getDiscoverySource(), csvRecord.get(ImportDataService.SYSTEM_DISCOVERY_SOURCE_HEADER))) {
						technology.setDiscoverySource(csvRecord.get(ImportDataService.SYSTEM_DISCOVERY_SOURCE_HEADER));
						isTechnologyChanged = true;
					}
				}
				if (csvRecord.isMapped(ImportDataService.SYSTEM_COMPUTER_ID_HEADER)) {
					systemEditDTO.setDeviceId(csvRecord.get(ImportDataService.SYSTEM_COMPUTER_ID_HEADER));
					if (technology != null && !Objects.equals(technology.getDeviceId(), csvRecord.get(ImportDataService.SYSTEM_COMPUTER_ID_HEADER))) {
						technology.setDeviceId(csvRecord.get(ImportDataService.SYSTEM_COMPUTER_ID_HEADER));
						isTechnologyChanged = true;
					}
				}

				if (technology != null && isTechnologyChanged) {
					technologyRepository.save(technology);
				}

				SystemStatus status = SystemStatus.ofString(systemStatus);
				if (status != null) {
					systemEditDTO.setSystemStatus(status);
				}

				if (StringUtils.isNotEmpty(costToRestoreString)) {
					try {
						double costToRestore = Double.parseDouble(costToRestoreString);
						systemEditDTO.setCostToRestore(costToRestore);
					} catch (NumberFormatException exception) {
						log.info(String.format("## Failed to parse costToRestore Double for system %s: %s", systemName, costToRestoreString));
					}
				}
				if (StringUtils.isNotEmpty(rtoString)) {
					try {
						double rto = Double.parseDouble(rtoString);
						systemEditDTO.setRto(rto);
					} catch (NumberFormatException exception) {
						log.info(String.format("## Failed to parse RTO Double for system %s: %s", systemName, rtoString));
					}
				}
				if (StringUtils.isNotEmpty(rpoString)) {
					try {
						double rpo = Double.parseDouble(rpoString);
						systemEditDTO.setRpo(rpo);
					} catch (NumberFormatException exception) {
						log.info(String.format("## Failed to parse RPO Double for system %s: %s", systemName, rpoString));
					}
				}
				if (StringUtils.isNotEmpty(recordsProcessedString)) {
					try {
						double recordsProcessed = Double.parseDouble(recordsProcessedString);
						systemEditDTO.setNumberOfRecProcessed(recordsProcessed);
					} catch (NumberFormatException exception) {
						log.info(String.format("## Failed to parse numberOfRecProcessed Double for system %s: %s", systemName, recordsProcessedString));
					}
				}
				if (StringUtils.isNotEmpty(maAssetString)) {
					systemEditDTO.setIsMAAsset(maAssetString.equalsIgnoreCase("yes"));
				}
				if (StringUtils.isNotBlank(deploymentTypeString)) {
					deploymentTypeString = deploymentTypeString.toUpperCase().replace(" ", "_");
					systemEditDTO.setDeploymentType(DeploymentType.of(deploymentTypeString));
				}
				if (StringUtils.isNotBlank(systemTypeString)) {
					systemTypeString = systemTypeString.toUpperCase().replace(" ", "_");
					systemEditDTO.setSystemType(SystemType.of(systemTypeString, null));
				}
				if (StringUtils.isNotEmpty(geoRecordsProcessedString)) {
					List<SystemGeoParametersDTO> geoRecordsList = new ArrayList<>();
					String[] geoLines = StringUtils.split(StringUtils.trim(geoRecordsProcessedString), '\n');
					for (int i = 0; i < geoLines.length; i++) {
						String geoLine = StringUtils.strip(geoLines[i]);
						String[] geo = StringUtils.split(geoLine, ",");
						if (geo.length >= 2) {
							SystemGeoParametersDTO systemGeoParameterDTO = new SystemGeoParametersDTO();
							Optional<Country> country = Optional.empty();
							Optional<State> state = Optional.empty();
							Double geoRecProcessed = 0d;
							String geoCountryName = StringUtils.trim(geo[0]);
							String geoStateName = StringUtils.trim(geo.length == 3 ? geo[1] : "");
							String geoRecString = StringUtils.trim(geo.length == 3 ? geo[2] : geo[1]);
							if (StringUtils.isNotEmpty(geoCountryName)) {
								country = countryRepository.findFirstByName(geoCountryName);
								if (country.isPresent()) {
									state = stateRepository.findFirstByNameAndCountry(geoStateName, country.get());
								}

								if (country.isPresent()) {
									systemGeoParameterDTO.setCountry(new CountryViewDTO(country.get()));
									if (state.isPresent()) {
										systemGeoParameterDTO.setState(new StateViewDTO(state.get()));
									}

									try {
										geoRecProcessed = Double.parseDouble(geoRecString);
									} catch (NumberFormatException exception) {
										log.info(String.format("## Failed to parse geoRecProcessed Double for system %s: %s", systemName, geoRecString));
									}

									systemGeoParameterDTO.setNumberOfRecProcessed(geoRecProcessed);
									geoRecordsList.add(systemGeoParameterDTO);
								}
							}

							// Set Geo Records list
							if (geoRecordsList.size() > 0) {
								systemEditDTO.setGeoRecordsProcessed(geoRecordsList);
							}
						}
					}
				}

				// Set Business Unit Path
				boolean isBusinessUnitPathReverse = false;
				if (StringUtils.isNotEmpty(businessUnitLocation) && StringUtils.isNotEmpty(businessUnitDivision)) {
					businessUnitPath = businessUnitDivision + CSVUtils.PATH_SEPARATOR + businessUnitLocation;
					isBusinessUnitPathReverse = true;
				} else if (StringUtils.isNotEmpty(businessUnitDivision)) {
					businessUnitPath = businessUnitDivision;
					isBusinessUnitPathReverse = true;
				}

				if (StringUtils.isNotEmpty(businessUnitPath)) {
					BusinessUnits businessUnit = businessUnitService.getParentByPath(businessUnitPath, organization.getId(), isBusinessUnitPathReverse, CSVUtils.PATH_SEPARATOR);
					if (businessUnit != null) {
						systemEditDTO.setBusinessUnit(new BusinessUnitRefDTO(businessUnit));
					} else if (StringUtils.isNotEmpty(businessUnitPath)) {
						result.getMessages().add(MessageFormat.format("WARNING: System [{0}]. Failed to find Business Unit: {1}, SKIPPING", systemName, businessUnitPath));
					}
				}

				SystemEditDTO systemResult;
				if (systemEditDTO.getId() == null) {
					systemResult = systemsService.create(systemEditDTO);
					result.getCreated().add(new ItemViewDTO(systemResult.getId(), systemResult.getName()));
				} else {
					systemResult = systemsService.update(systemEditDTO);
					result.getUpdated().add(new ItemViewDTO(systemResult.getId(), systemResult.getName()));
				}

				currentItem++;
				if (currentItem % 100 == 0) {
					// long processingTime = (System.currentTimeMillis() - currentTime) / 1000;
					// log.info("Processed import records: {} in {} seconds", currentItem, processingTime);

					// currentTime = System.currentTimeMillis();
				}

				// Create associate Vendors
				if (StringUtils.isNotEmpty(associateVendorsString)) {
					String[] vendorNames = StringUtils.split(associateVendorsString, ";");
					for (int i = 0; i < vendorNames.length; i++) {
						String vendorName = StringUtils.trim(vendorNames[i]);

						Optional<Organizations> vendorOptional = Optional.empty();
						if (vendorsCache.containsKey(vendorName)) {
							vendorOptional = Optional.ofNullable(vendorsCache.get(vendorName));
						} else {
							vendorOptional = organizationRepository.findFirstByNameAndOrganizationTypeAndRootParent(vendorName, OrganizationType.Vendor, organization);
							vendorOptional.ifPresent(organizations -> vendorsCache.put(vendorName, organizations));
						}

						if (vendorOptional.isPresent()) {
							associateVendorService.addSystemToVendor(vendorOptional.get().getId(), systemResult.getId());
							result.getMessages().add(MessageFormat.format("Added System [{0}] to Associate Vendor [{1}]", systemName, vendorName));
						}
					}
				}

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

}
