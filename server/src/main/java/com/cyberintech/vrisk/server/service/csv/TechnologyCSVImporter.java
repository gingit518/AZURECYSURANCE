package com.cyberintech.vrisk.server.service.csv;

import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.country.CountryViewDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.datadomains.DataDomainsDTO;
import com.cyberintech.vrisk.server.model.dto.organization.OrganizationRefDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.technology.EnvironmentTypesDTO;
import com.cyberintech.vrisk.server.model.dto.technology.TechnologyEditDTO;
import com.cyberintech.vrisk.server.model.dto.technology_categories.TechnologyCategoryRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.DataAssetClassification;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Technologies;
import com.cyberintech.vrisk.server.repository.jpa.*;
import com.cyberintech.vrisk.server.service.TechnologyService;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import com.cyberintech.vrisk.server.service.utils.ImportUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Andrii Iakovenko
 * @since 2022-07-19
 */
@Slf4j
@RequiredArgsConstructor
public class TechnologyCSVImporter implements CSVImporter {

	public static final String TECHNOLOGY_NAME_HEADER = "Technology Name";
	public static final String TECHNOLOGY_VERSION_HEADER = "Version";
	public static final String TECHNOLOGY_DESCRIPTION_HEADER = "Description";
	public static final String TECHNOLOGY_CATEGORY_HEADER = "Technology Category";
	public static final String TECHNOLOGY_ASSET_CLASS_HEADER = "Asset Class";
	public static final String TECHNOLOGY_DATA_DOMAINS_HEADER = "Data Domains";
	public static final String TECHNOLOGY_ENVIRONMENT_TYPE_HEADER = "Environment Type";
	public static final String TECHNOLOGY_COUNTRY_HEADER = "Country";
	public static final String TECHNOLOGY_VENDOR_HEADER = "Vendor";
	public static final String TECHNOLOGY_SYSTEMS_HEADER = "Systems";
	public static final String TECHNOLOGY_RISK_REDUCTION_HEADER = "Risk reduction";
	public static final String TECHNOLOGY_RISK_REDUCTION_PERCENT_HEADER = "Risk reduction, %";
	public static final String TECHNOLOGY_LICENSE_COST_HEADER = "License cost";
	public static final String TECHNOLOGY_NOTES_HEADER = "Notes";

	private final CountryRepository countryRepository;
	private final DataAssetClassificationRepository dataAssetClassificationRepository;
	private final DataDomainsRepository dataDomainsRepository;
	private final EnvironmentTypesRepository environmentTypesRepository;
	private final OrganizationRepository organizationRepository;
	private final SystemRepository systemRepository;
	private final TechnologyRepository technologyRepository;
	private final TechnologyCategoryRepository technologyCategoryRepository;
	private final TechnologyService technologyService;

	private final Organizations organization;

	@SuppressWarnings("rawtypes")
	@Override
	public ImportResultDTO doImport(InputStream inputStream) {
		ImportResultDTO result = new ImportResultDTO();

		try {
			CSVParser csvParser = CSVUtils.createCSVParser(inputStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();

			Map<String, DataAssetClassification> dataAssetClassificationCache = new HashMap<>();

			for (CSVRecord csvRecord : csvRecordList) {
				// Accessing values by Header names
				String name = CSVUtils.getAsString(csvRecord, TECHNOLOGY_NAME_HEADER);
				String scopeName = MessageFormat.format("Technology [{0}]", name);

				if (StringUtils.isNotEmpty(name)) {
					Optional<Technologies> technologyDetails = technologyRepository.getFirstByNameAndOrganization(name,
						organization.getId());

					TechnologyEditDTO technology;
					if (technologyDetails.isEmpty()) {
						technology = new TechnologyEditDTO();
						technology.setName(name);
					} else {
						technology = new TechnologyEditDTO(technologyDetails.get());
					}

					Pair<DataAssetClassificationRefDTO, List<String>> assetClassification = ImportUtils
						.loadDataAssetClassification(csvRecord, TECHNOLOGY_ASSET_CLASS_HEADER, scopeName,
							organization.getId(), dataAssetClassificationRepository, dataAssetClassificationCache);
					technology.setAssetClassification(assetClassification.getLeft());
					result.getMessages().addAll(assetClassification.getRight());

					Pair<CountryViewDTO, List<String>> country = ImportUtils.loadCountry(csvRecord,
						TECHNOLOGY_COUNTRY_HEADER, scopeName, countryRepository);
					technology.setCountry(country.getLeft());
					result.getMessages().addAll(country.getRight());

					Pair<List<DataDomainsDTO>, List<String>> dataDomains = ImportUtils.loadDataDomains(csvRecord,
						TECHNOLOGY_DATA_DOMAINS_HEADER, scopeName, organization.getId(), dataDomainsRepository);
					technology.setDataDomains(dataDomains.getLeft());
					result.getMessages().addAll(dataDomains.getRight());

					Pair<EnvironmentTypesDTO, List<String>> environmentType = ImportUtils.loadEnvironmentTypes(
						csvRecord,
						TECHNOLOGY_ENVIRONMENT_TYPE_HEADER, scopeName, organization.getId(),
						environmentTypesRepository);
					technology.setEnvironmentType(environmentType.getLeft());
					result.getMessages().addAll(environmentType.getRight());

					technology.setDescription(CSVUtils.getAsString(csvRecord, TECHNOLOGY_DESCRIPTION_HEADER));
					technology.setNotes(CSVUtils.getAsString(csvRecord, TECHNOLOGY_NOTES_HEADER));
					technology.setRiskReduction(CSVUtils.getAsDouble(csvRecord, TECHNOLOGY_RISK_REDUCTION_HEADER));
					technology.setRiskReductionPercent(
						CSVUtils.getAsDouble(csvRecord, TECHNOLOGY_RISK_REDUCTION_PERCENT_HEADER));

					Pair<List<SystemRefDTO>, List<String>> systems = ImportUtils.loadSystems(csvRecord,
						TECHNOLOGY_SYSTEMS_HEADER, scopeName, organization.getId(), systemRepository);
					technology.setSystems(systems.getLeft());
					result.getMessages().addAll(systems.getRight());

					Pair<TechnologyCategoryRefDTO, List<String>> technologyCategory = ImportUtils
						.loadTechnologyCategory(
							csvRecord, TECHNOLOGY_CATEGORY_HEADER, scopeName, organization.getId(),
							technologyCategoryRepository);
					technology.setTechnologyCategory(technologyCategory.getLeft());
					result.getMessages().addAll(technologyCategory.getRight());

					technology.setToolPrice(CSVUtils.getAsDouble(csvRecord, TECHNOLOGY_LICENSE_COST_HEADER));

					Pair<OrganizationRefDTO, List<String>> vendor = ImportUtils.loadOrganization(csvRecord,
						TECHNOLOGY_VENDOR_HEADER, scopeName, organization.getId(), organizationRepository);
					technology.setVendor(vendor.getLeft());
					result.getMessages().addAll(vendor.getRight());

					technology.setVersion(CSVUtils.getAsString(csvRecord, TECHNOLOGY_VERSION_HEADER));

					TechnologyEditDTO technologyResult = null;
					if (technology.getId() == null) {
						technologyResult = technologyService.create(technology);
						result.getCreated().add(new ItemViewDTO(technologyResult.getId(),
							MessageFormat.format("{0}", technologyResult.getName())));
					} else {
						technologyResult = technologyService.update(technology);
						result.getUpdated().add(new ItemViewDTO(technologyResult.getId(),
							MessageFormat.format("{0}", technologyResult.getName())));
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to import Technologies", e);

		}

		return result;
	}

}
