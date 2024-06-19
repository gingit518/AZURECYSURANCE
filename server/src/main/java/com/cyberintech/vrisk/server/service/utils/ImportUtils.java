package com.cyberintech.vrisk.server.service.utils;

import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.country.CountryViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.datadomains.DataDomainsDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology.EnvironmentTypesDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryEditDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.OrganizationType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.service.BusinessUnitService;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Andrii Iakovenko
 * @since  2022-07-22
 */
public class ImportUtils {

	public static List<SimpleDateFormat> DATE_FORMATS = Arrays.asList(
		new SimpleDateFormat("yyyy-MM-dd"),
		new SimpleDateFormat("dd-MM-yyyy"),
		new SimpleDateFormat("dd.MM.yyyy"),
		new SimpleDateFormat("yyyy.MM.dd"),
		new SimpleDateFormat("dd/MM/yyyy"),
		new SimpleDateFormat("yyyy/MM/dd")
	);

	public static Pair<BusinessUnitRefDTO, List<String>> loadBusinessUnit(CSVRecord csvRecord, String header,
		String scopeName, Long organizationId, BusinessUnitService businessUnitService) {
		BusinessUnitRefDTO businessUnitDto = null;
		List<String> messages = new LinkedList<>();
		String businessUnitPath = CSVUtils.getAsString(csvRecord, header);
		if (StringUtils.isNotEmpty(businessUnitPath)) {
			BusinessUnits businessUnit = businessUnitService.getParentByPath(businessUnitPath, organizationId,
				false, CSVUtils.PATH_SEPARATOR);
			if (businessUnit != null) {
				businessUnitDto = new BusinessUnitRefDTO(businessUnit);
			} else {
				messages.add(MessageFormat.format("WARNING: {0}. Failed to find Business Unit Owned: {1}, SKIPPING",
					scopeName, businessUnitPath));
			}
		}
		return ImmutablePair.of(businessUnitDto, messages);
	}

	public static Pair<CountryViewDTO, List<String>> loadCountry(CSVRecord csvRecord, String header, String scopeName,
		CountryRepository countryRepository) {
		CountryViewDTO countryDto = null;
		List<String> messages = new LinkedList<>();
		String countryName = CSVUtils.getAsString(csvRecord, header);
		if (StringUtils.isNotEmpty(countryName)) {
			Optional<Country> details = countryRepository.findFirstByName(countryName);
			if (details.isPresent()) {
				countryDto = new CountryViewDTO(details.get());
			} else {
				messages.add(MessageFormat.format("WARNING: {0}. Failed to find Country: {1}, SKIPPING",
					scopeName, details));
			}
		}
		return ImmutablePair.of(countryDto, messages);
	}

	public static Pair<DataAssetClassificationRefDTO, List<String>> loadDataAssetClassification(
		CSVRecord csvRecord, String header, String scopeName, Long organizationId,
		DataAssetClassificationRepository dataAssetClassificationRepository, Map<String, DataAssetClassification> dataAssetClassificationCache) {
		DataAssetClassificationRefDTO classification = null;
		List<String> messages = new LinkedList<>();
		String classificationName = CSVUtils.getAsString(csvRecord, header);
		if (StringUtils.isNotEmpty(classificationName)) {
			if (dataAssetClassificationCache.containsKey(classificationName)) {
				classification = new DataAssetClassificationRefDTO(dataAssetClassificationCache.get(classificationName));
			} else {
				Optional<DataAssetClassification> details = dataAssetClassificationRepository.getFirstByNameForOrganization(classificationName, organizationId);
				if (details.isPresent()) {
					classification = new DataAssetClassificationRefDTO(details.get());
					dataAssetClassificationCache.put(classificationName, details.get());
				} else {
					messages.add(MessageFormat.format("WARNING: {0}. Failed to find Digital Asset: {1}, SKIPPING",
						scopeName, classificationName));
				}
			}

		}
		return ImmutablePair.of(classification, messages);
	}

	public static Pair<List<DataAssetClassificationRefDTO>, List<String>> loadDataAssetClassifications(
		CSVRecord csvRecord, String header, String scopeName, Long organizationId,
		DataAssetClassificationRepository dataAssetClassificationRepository) {
		List<DataAssetClassificationRefDTO> classifications = new LinkedList<>();
		List<String> messages = new LinkedList<>();
		String[] classificationNames = CSVUtils.getAsStrings(csvRecord, header);
		for (String classificationName : classificationNames) {
			if (StringUtils.isNotEmpty(classificationName)) {
				Optional<DataAssetClassification> details = dataAssetClassificationRepository
					.getFirstByNameForOrganization(classificationName, organizationId);
				if (details.isPresent()) {
					classifications.add(new DataAssetClassificationRefDTO(details.get()));
				} else {
					messages.add(MessageFormat.format("WARNING: {0}. Failed to find Digital Asset: {1}, SKIPPING",
						scopeName, classificationName));
				}
			}
		}
		return ImmutablePair.of(classifications, messages);
	}

	public static Pair<List<DataDomainsDTO>, List<String>> loadDataDomains(CSVRecord csvRecord, String header,
		String scopeName, Long organizationId, DataDomainsRepository dataDomainsRepository) {
		List<DataDomainsDTO> dataDomains = new LinkedList<>();
		List<String> messages = new LinkedList<>();
		String[] dataDomainNames = CSVUtils.getAsStrings(csvRecord, header);
		for (String dataDomain : dataDomainNames) {
			if (StringUtils.isNotEmpty(dataDomain)) {
				Optional<DataDomains> details = dataDomainsRepository.findFirstByNameAndOrganizationId(dataDomain,
					organizationId);
				if (details.isPresent()) {
					dataDomains.add(new DataDomainsDTO(details.get()));
				} else {
					messages.add(MessageFormat.format("WARNING: {0}. Failed to find Data Domain: {1}, SKIPPING",
						scopeName, dataDomain));
				}
			}
		}
		return ImmutablePair.of(dataDomains, messages);
	}

	public static Pair<List<DataTypeClassificationRefDTO>, List<String>> loadDataTypeClassifications(
		CSVRecord csvRecord, String header, String scopeName, Long organizationId,
		DataTypeClassificationRepository dataTypeClassificationRepository, @NotNull Map<String, DataTypeClassification> dataTypeClassificationCache) {
		List<DataTypeClassificationRefDTO> classifications = new LinkedList<>();
		List<String> messages = new LinkedList<>();
		String[] classificationNames = CSVUtils.getAsStrings(csvRecord, header);
		for (String classificationName : classificationNames) {
			if (StringUtils.isNotEmpty(classificationName)) {
				if (dataTypeClassificationCache.containsKey(classificationName)) {
					classifications.add(new DataTypeClassificationRefDTO(dataTypeClassificationCache.get(classificationName)));
				} else {
					Optional<DataTypeClassification> details = dataTypeClassificationRepository.getFirstByNameForOrganization(classificationName, organizationId);
					if (details.isEmpty()) {
						details = dataTypeClassificationRepository.findByNameAndOrganizationIdIsNull(classificationName);
					}
					if (details.isPresent()) {
						classifications.add(new DataTypeClassificationRefDTO(details.get()));
						dataTypeClassificationCache.put(classificationName, details.get());
					} else {
						messages.add(MessageFormat.format("WARNING: {0}. Failed to find Data Classification: {1}, SKIPPING", scopeName, classificationName));
					}
				}
			}
		}
		return ImmutablePair.of(classifications, messages);
	}

	public static Pair<EnvironmentTypesDTO, List<String>> loadEnvironmentTypes(CSVRecord csvRecord, String header,
		String scopeName, Long organizationId, EnvironmentTypesRepository environmentTypesRepository) {
		EnvironmentTypesDTO environmentTypeDto = null;
		List<String> messages = new LinkedList<>();
		String environmentTypeName = CSVUtils.getAsString(csvRecord, header);
		if (StringUtils.isNotEmpty(environmentTypeName)) {
			Optional<EnvironmentTypes> details = environmentTypesRepository.findFirstByName(environmentTypeName);
			if (details.isPresent()) {
				environmentTypeDto = new EnvironmentTypesDTO(details.get());
			} else {
				messages.add(MessageFormat.format("WARNING: {0}. Failed to find Environment Type: {1}, SKIPPING",
					scopeName, environmentTypeName));
			}
		}
		return ImmutablePair.of(environmentTypeDto, messages);
	}

	public static Pair<OrganizationRefDTO, List<String>> loadOrganization(CSVRecord csvRecord, String header,
		String scopeName, Long organizationId, OrganizationRepository organizationRepository) {
		OrganizationRefDTO environmentTypeDto = null;
		List<String> messages = new LinkedList<>();
		String organizationName = CSVUtils.getAsString(csvRecord, header);
		if (StringUtils.isNotEmpty(organizationName)) {
			Optional<Organizations> details = organizationRepository
				.getByNameAndNoParentForRootOrganization(organizationName, organizationId);
			if (details.isPresent()) {
				environmentTypeDto = new OrganizationRefDTO(details.get());
			} else {
				messages.add(MessageFormat.format("WARNING: {0}. Failed to find Organization: {1}, SKIPPING",
					scopeName, organizationName));
			}
		}
		return ImmutablePair.of(environmentTypeDto, messages);
	}

	public static Pair<OrganizationRefDTO, List<String>> loadOrganization(CSVRecord csvRecord, String header,
		OrganizationType organizationType, String scopeName, Organizations rootOrganization,
		OrganizationRepository organizationRepository) {
		OrganizationRefDTO environmentTypeDto = null;
		List<String> messages = new LinkedList<>();
		String organizationName = CSVUtils.getAsString(csvRecord, header);
		if (StringUtils.isNotEmpty(organizationName)) {
			Optional<Organizations> details = organizationRepository
				.findFirstByNameAndOrganizationTypeAndRootParent(organizationName, organizationType,
					rootOrganization);
			if (details.isPresent()) {
				environmentTypeDto = new OrganizationRefDTO(details.get());
			} else {
				messages.add(MessageFormat.format("WARNING: {0}. Failed to find Organization: {1}, SKIPPING",
					scopeName, organizationName));
			}
		}
		return ImmutablePair.of(environmentTypeDto, messages);
	}

	public static Pair<List<ProcessRefDTO>, List<String>> loadProcesses(CSVRecord csvRecord, String header,
		String scopeName, Long organizationId, ProcessRepository processRepository) {
		List<ProcessRefDTO> processes = new LinkedList<>();
		List<String> messages = new LinkedList<>();
		String[] processNames = CSVUtils.getAsStrings(csvRecord, header);
		for (String processName : processNames) {
			if (StringUtils.isNotEmpty(processName)) {
				Optional<Processes> processDetails = processRepository.findFirstByNameAndOrganizationId(processName,
					organizationId);
				if (processDetails.isPresent()) {
					processes.add(new ProcessRefDTO(processDetails.get()));
				} else {
					messages.add(MessageFormat.format("WARNING: {0}. Failed to find Process: {1}, SKIPPING",
						scopeName, processName));
				}
			}
		}
		return ImmutablePair.of(processes, messages);
	}

	public static Pair<List<SystemRefDTO>, List<String>> loadSystems(CSVRecord csvRecord, String header,
		String scopeName, Long organizationId, SystemRepository systemRepository) {
		List<SystemRefDTO> systems = new LinkedList<>();
		List<String> messages = new LinkedList<>();
		String[] systemNames = CSVUtils.getAsStrings(csvRecord, header);
		for (String systemName : systemNames) {
			systemName = StringUtils.trim(systemName);
			if (StringUtils.isNotEmpty(systemName)) {
				Optional<Systems> systemDetails = systemRepository.getFirstByNameForOrganization(systemName,
					organizationId);
				if (systemDetails.isPresent()) {
					systems.add(new SystemRefDTO(systemDetails.get()));
				} else {
					messages.add(MessageFormat.format("WARNING: {0}. Failed to find System: {1}, SKIPPING",
						scopeName, systemName));
				}
			}
		}
		return ImmutablePair.of(systems, messages);
	}

	public static Pair<List<TechnologyRefDTO>, List<String>> loadTechnologies(CSVRecord csvRecord, String header,
		String scopeName, Long organizationId, TechnologyRepository technologyRepository, String categoryHeaderName, TechnologyCategoryRepository technologyCategoryRepository
		, Map<String, Technologies> technologiesCache, Map<String, TechnologyCategories> technologyCategoriesCache) {

		List<TechnologyRefDTO> technologies = new LinkedList<>();
		List<String> messages = new LinkedList<>();
		String[] technologyNames = CSVUtils.getAsStrings(csvRecord, header);
		for (String technologyName : technologyNames) {
			if (StringUtils.isNotEmpty(technologyName)) {

				if (technologiesCache.containsKey(technologyName)) {
					technologies.add(new TechnologyRefDTO(technologiesCache.get(technologyName)));
				} else {
					Optional<Technologies> technologyDetails = technologyRepository.getFirstByNameAndOrganization(technologyName, organizationId);
					if (technologyDetails.isPresent()) {
						technologies.add(new TechnologyRefDTO(technologyDetails.get()));
					} else {

						// TODO Technology Category and Technology creation
						String technologyCategoryName = CSVUtils.getAsString(csvRecord, categoryHeaderName);
						if (StringUtils.isNotEmpty(technologyCategoryName)) {
							Optional<TechnologyCategories> technologyCategoryDetails = technologyCategoryRepository.getFirstByNameAndOrganization(technologyCategoryName, organizationId);
							if (technologyCategoryDetails.isEmpty()) {
								TechnologyCategories technologyCategory = new TechnologyCategories();
								technologyCategory.setName(technologyCategoryName);
								technologyCategory.setOrganizationId(organizationId);
								technologyCategory.setCreatedAt(new Date());
								// technologyCategory.setCreatedBy(Sy);
								technologyCategory.setUpdatedAt(new Date());
								// technologyCategory.setUpdatedBy(new Date());
								technologyCategory = technologyCategoryRepository.save(technologyCategory);

								technologyCategoryDetails = Optional.of(technologyCategory);
							}

							Technologies newTechnology = new Technologies();
							newTechnology.setOrganizationId(organizationId);
							newTechnology.setTechnologyCategory(technologyCategoryDetails.get());
							newTechnology.setName(technologyName);
							newTechnology.setCreatedAt(new Date());
							newTechnology.setUpdatedAt(new Date());
							newTechnology = technologyRepository.save(newTechnology);

							technologies.add(new TechnologyRefDTO(newTechnology));
						}

						messages.add(MessageFormat.format("WARNING: {0}. Failed to find Technology: {1}, SKIPPING", scopeName, technologyName));
					}
				}

			}
		}
		return ImmutablePair.of(technologies, messages);
	}

	public static Pair<TechnologyCategoryRefDTO, List<String>> loadTechnologyCategory(CSVRecord csvRecord,
		String header, String scopeName, Long organizationId,
		TechnologyCategoryRepository technologyCategoryRepository) {
		TechnologyCategoryRefDTO technologyCategoryDto = null;
		List<String> messages = new LinkedList<>();
		String technologyCategoryName = CSVUtils.getAsString(csvRecord, header);
		if (StringUtils.isNotEmpty(technologyCategoryName)) {
			Optional<TechnologyCategories> details = technologyCategoryRepository
				.getFirstByNameAndOrganization(technologyCategoryName, organizationId);
			if (details.isPresent()) {
				technologyCategoryDto = new TechnologyCategoryRefDTO(details.get());
			} else {
				TechnologyCategories technologyCategory = new TechnologyCategories();
				technologyCategory.setName(technologyCategoryName);
				technologyCategory.setOrganizationId(organizationId);
				technologyCategory.setCreatedAt(new Date());
				// technologyCategory.setCreatedBy(Sy);
				technologyCategory.setUpdatedAt(new Date());
				// technologyCategory.setUpdatedBy(new Date());
				technologyCategory = technologyCategoryRepository.save(technologyCategory);

				technologyCategoryDto = new TechnologyCategoryRefDTO(technologyCategory);
				messages.add(MessageFormat.format("WARNING: {0}. Technology Category not Found: {1}. CREATING.", scopeName, technologyCategoryName));
			}
		}
		return ImmutablePair.of(technologyCategoryDto, messages);
	}

	public static Pair<UserRefDTO, List<String>> loadUser(CSVRecord csvRecord, String emailHeader,
		String fullnameHeader, String scopeName, Organizations organization, UserRepository userRepository) {
		UserRefDTO userDto = null;
		List<String> messages = new LinkedList<>();

		String userEmail = CSVUtils.getAsString(csvRecord, emailHeader);
		if (StringUtils.isNotEmpty(userEmail)) {
			Optional<Users> details = userRepository.findFirstByEmailIgnoreCaseAndOrganization(userEmail, organization);
			if (details.isPresent()) {
				userDto = new UserRefDTO(details.get());
			} else {
				String userName = CSVUtils.getAsString(csvRecord, fullnameHeader);
				if (StringUtils.isNotEmpty(userName)) {
					details = userRepository.findFirstByFullNameAndOrganization(userName, organization);
					if (details.isPresent()) {
						userDto = new UserRefDTO(details.get());
						messages.add(MessageFormat.format(
							"WARNING: {0}. Failed to find User by email, but found by full name: {1}, OK",
							scopeName, details));
					} else {
						messages.add(MessageFormat.format(
							"WARNING: {0}. Failed to find User by email or full name: {1}, SKIPPING",
							scopeName, details));
					}
				}
			}
		}
		return ImmutablePair.of(userDto, messages);
	}

	public static Date loadDate(String dateString) {
		Date result = null;

		if (StringUtils.isNotEmpty(dateString)) {
			for (SimpleDateFormat sdf : DATE_FORMATS) {
				try {
					result = sdf.parse(dateString);
					break;
				} catch (ParseException ignored) {
				}
			}
		}

		return result;
	}

}
