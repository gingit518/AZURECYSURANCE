package com.cyberintech.vrisk.server.service.csv;

import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.business_unit.BusinessUnitRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_asset_classification.DataAssetClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.data_type_classification.DataTypeClassificationRefDTO;
import com.cyberintech.vrisk.server.model.dto.process.ProcessEditDTO;
import com.cyberintech.vrisk.server.model.dto.systems.SystemRefDTO;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.DataAssetClassificationRepository;
import com.cyberintech.vrisk.server.repository.jpa.DataTypeClassificationRepository;
import com.cyberintech.vrisk.server.repository.jpa.ProcessRepository;
import com.cyberintech.vrisk.server.repository.jpa.SystemRepository;
import com.cyberintech.vrisk.server.service.BusinessUnitService;
import com.cyberintech.vrisk.server.service.ProcessService;
import com.cyberintech.vrisk.server.service.UserService;
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
import java.util.*;

/**
 * @author Andrii Iakovenko
 * @since 2022-07-19
 */
@Slf4j
@RequiredArgsConstructor
public class ProcessCSVImporter implements CSVImporter {

	public static final String PROCESS_NAME_HEADER = "Process Name";
	public static final String PROCESS_DESCRIPTION_HEADER = "Description";
	public static final String PROCESS_OWNER_NAME_HEADER = "Contact Name";
	public static final String PROCESS_OWNER_EMAIL_HEADER = "Contact Email";
	public static final String PROCESS_REVENUE_PROCESSED_HEADER = "Revenue Processed";
	public static final String PROCESS_BUSINESS_UNIT_OWNS_PATH_HEADER = "Business Unit Owns Path";
	public static final String PROCESS_NOTES_HEADER = "Notes";
	public static final String PROCESS_BUSINESS_UNITS_USED_PATH_HEADER = "Uses Business Units Path";
	public static final String PROCESS_DATA_TYPE_CLASSIFICATIONS_HEADER = "Data Classification";
	public static final String PROCESS_DATA_ASSET_CLASSIFICATIONS_HEADER = "Asset Class";
	public static final String PROCESS_SYSTEMS_HEADER = "Systems";

	private final BusinessUnitService businessUnitService;
	private final DataTypeClassificationRepository dataTypeClassificationRepository;
	private final DataAssetClassificationRepository dataAssetClassificationRepository;
	private final ProcessRepository processRepository;
	private final ProcessService processService;
	private final SystemRepository systemRepository;
	private final UserService userService;

	private final Organizations organization;

	@SuppressWarnings("rawtypes")
	@Override
	public ImportResultDTO doImport(InputStream inputStream) {
		ImportResultDTO result = new ImportResultDTO();

		try {
			CSVParser csvParser = CSVUtils.createCSVParser(inputStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();
			Map<String, DataTypeClassification> dataTypeClassificationCache = new HashMap<>();
			for (CSVRecord csvRecord : csvRecordList) {
				// Accessing values by Header names
				String processName = Optional.ofNullable(csvRecord.get(PROCESS_NAME_HEADER)).orElse("").trim();
				String processDescription = (csvRecord.isMapped(PROCESS_DESCRIPTION_HEADER))
					? Optional.ofNullable(csvRecord.get(PROCESS_DESCRIPTION_HEADER)).orElse("").trim()
					: "";
				String processOwnerEmail = (csvRecord.isMapped(PROCESS_OWNER_EMAIL_HEADER))
					? Optional.ofNullable(csvRecord.get(PROCESS_OWNER_EMAIL_HEADER)).orElse("").trim()
					: "";
				String processOwnerName = (csvRecord.isMapped(PROCESS_OWNER_NAME_HEADER))
					? Optional.ofNullable(csvRecord.get(PROCESS_OWNER_EMAIL_HEADER)).orElse("").trim()
					: "";
				Double processRevenueProcessed = CSVUtils.getAsDouble(csvRecord, PROCESS_REVENUE_PROCESSED_HEADER);
				String processNotes = (csvRecord.isMapped(PROCESS_NOTES_HEADER))
					? Optional.ofNullable(csvRecord.get(PROCESS_NOTES_HEADER)).orElse("").trim()
					: "";

				if (StringUtils.isNotEmpty(processName)) {
					String scopeName = MessageFormat.format("Technology [{0}]", processName);

					Optional<Processes> processDetails = processRepository.findFirstByNameAndOrganizationId(processName,
						organization.getId());

					ProcessEditDTO process;
					if (processDetails.isEmpty()) {
						process = new ProcessEditDTO();
						process.setName(processName);
					} else {
						process = new ProcessEditDTO(processDetails.get());
					}

					process.setDescription(processDescription);
					process.setNotes(processNotes);
					process.setRevenueProcessed(processRevenueProcessed);

					// Set Contact Name
					UserRefDTO processOwner = userService.findByEmailOrFullName(processOwnerEmail, processOwnerName);
					if (processOwner != null) {
						process.setOwner(processOwner);
					} else if (StringUtils.isNoneBlank(processOwnerEmail)) {
						result.getMessages()
							.add(MessageFormat.format(
								"WARNING: Process [{0}]. Failed to find Owner: {1}, SKIPPING", processName,
								processOwnerEmail));
					}

					// Set Business unit owns
					if (csvRecord.isMapped(PROCESS_BUSINESS_UNIT_OWNS_PATH_HEADER)) {
						String businessUnitPath = StringUtils
							.trim(csvRecord.get(PROCESS_BUSINESS_UNIT_OWNS_PATH_HEADER));
						if (StringUtils.isNotEmpty(businessUnitPath)) {
							BusinessUnits businessUnit = businessUnitService.getParentByPath(businessUnitPath,
								organization.getId(), false, CSVUtils.PATH_SEPARATOR);
							if (businessUnit != null) {
								process.setBusinessUnit(new BusinessUnitRefDTO(businessUnit));
							} else {
								result.getMessages()
									.add(MessageFormat.format(
										"WARNING: Process [{0}]. Failed to find Business Unit Owned: {1}, SKIPPING",
										processName,
										businessUnitPath));
							}
						}
					}

					// Set Uses business units
					if (csvRecord.isMapped(PROCESS_BUSINESS_UNITS_USED_PATH_HEADER)) {
						String[] businessUnitPathes = StringUtils
							.split(csvRecord.get(PROCESS_BUSINESS_UNITS_USED_PATH_HEADER), CSVUtils.LIST_SEPARATOR);
						List<BusinessUnitRefDTO> businessUnits = new ArrayList<>(businessUnitPathes.length);
						for (String businessUnitPath : businessUnitPathes) {
							businessUnitPath = StringUtils.trim(businessUnitPath);
							if (StringUtils.isNotEmpty(businessUnitPath)) {
								BusinessUnits businessUnit = businessUnitService.getParentByPath(businessUnitPath,
									organization.getId(), false, CSVUtils.PATH_SEPARATOR);
								if (businessUnit != null) {
									businessUnits.add(new BusinessUnitRefDTO(businessUnit));
								} else {
									result.getMessages()
										.add(MessageFormat.format(
											"WARNING: Process [{0}]. Failed to find Business Unit Used: {1}, SKIPPING",
											processName,
											businessUnitPath));
								}
							}
						}
						process.setBusinessUnits(businessUnits);
					}

					// Set Asset Class
					if (csvRecord.isMapped(PROCESS_DATA_ASSET_CLASSIFICATIONS_HEADER)) {
						String[] classificationNames = StringUtils
							.split(csvRecord.get(PROCESS_DATA_ASSET_CLASSIFICATIONS_HEADER), CSVUtils.LIST_SEPARATOR);
						List<DataAssetClassificationRefDTO> classifications = new ArrayList<>(
							classificationNames.length);
						for (String classificationName : classificationNames) {
							if (StringUtils.isNotBlank(classificationName)) {
								Optional<DataAssetClassification> dataAssetClassificationDetails = dataAssetClassificationRepository
									.getFirstByNameForOrganization(classificationName.trim(),
										organization.getId());
								if (dataAssetClassificationDetails.isPresent()) {
									classifications
										.add(new DataAssetClassificationRefDTO(dataAssetClassificationDetails.get()));
								} else {
									result.getMessages()
										.add(MessageFormat.format(
											"WARNING: Process [{0}]. Failed to find Digital Asset: {1}, SKIPPING",
											processName,
											classificationName));
								}
							}
						}
						process.setDataAssetClassifications(classifications);
					}

					// Set Data Classification
					Pair<List<DataTypeClassificationRefDTO>, List<String>> dataTypeClassifications = ImportUtils
						.loadDataTypeClassifications(csvRecord, PROCESS_DATA_TYPE_CLASSIFICATIONS_HEADER, scopeName,
							organization.getId(), dataTypeClassificationRepository, dataTypeClassificationCache);
					process.setDataTypeClassifications(dataTypeClassifications.getLeft());
					result.getMessages().addAll(dataTypeClassifications.getRight());

					// Set Systems
					Pair<List<SystemRefDTO>, List<String>> systems = ImportUtils.loadSystems(csvRecord,
						PROCESS_SYSTEMS_HEADER, scopeName, organization.getId(), systemRepository);
					process.setSystems(systems.getLeft());
					result.getMessages().addAll(systems.getRight());

					ProcessEditDTO processResult = null;
					if (process.getId() == null) {
						processResult = processService.create(process);
						result.getCreated().add(new ItemViewDTO(processResult.getId(),
							MessageFormat.format("{0}", processResult.getName())));
					} else {
						processResult = processService.update(process);
						result.getUpdated().add(new ItemViewDTO(processResult.getId(),
							MessageFormat.format("{0}", processResult.getName())));
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to import Processes", e);
		}

		return result;
	}

}
