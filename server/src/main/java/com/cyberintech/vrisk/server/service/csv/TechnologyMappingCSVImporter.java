package com.cyberintech.vrisk.server.service.csv;

import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.jpa.entity.DataAssetClassification;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.TechnologyCategoryImportMappings;
import com.cyberintech.vrisk.server.repository.jpa.TechnologyCategoryImportMappingRepository;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class TechnologyMappingCSVImporter implements CSVImporter {

	public static final String TECHNOLOGY_CATEGORY_HEADER = "Technology Category";
	public static final String TECHNOLOGY_HEADER = "Technology";
	public static final String ASSOCIATE_VENDOR_HEADER = "Associate Vendor";
	public static final String TARGET_TECHNOLOGY_CATEGORY_HEADER = "Target Category";
	public static final String TARGET_TECHNOLOGY_SUBCATEGORY_HEADER = "Target Subcategory";
	public static final String TARGET_TECHNOLOGY_CLASS_HEADER = "Target Class";
	public static final String TARGET_TECHNOLOGY_HEADER = "Target Technology";

	private final TechnologyCategoryImportMappingRepository technologyCategoryImportMappingRepository;

	private final Organizations organization;

	private final PlatformTransactionManager platformTransactionManager;


	@SuppressWarnings("rawtypes")
	@Override
	public ImportResultDTO doImport(InputStream inputStream) {
		final ImportResultDTO result = new ImportResultDTO();

		try {
			CSVParser csvParser = CSVUtils.createCSVParser(inputStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();

			long timeStart = System.currentTimeMillis();
			for (final CSVRecord csvRecord : csvRecordList) {
				// Accessing values by Header names
				TechnologyCategoryImportMappings item = new TransactionTemplate(platformTransactionManager).execute(status -> {
					return importOneRecord(csvRecord, result);
				});
			}
			log.info(String.format("## Successfully Imported Mappings: %s, Processing Time: %s ms", result.getSummaryInfo(), (System.currentTimeMillis() - timeStart)));
		} catch (Exception e) {
			log.error("Failed to import Technologies Mapping", e);

		}

		return result;
	}

	private TechnologyCategoryImportMappings importOneRecord(final CSVRecord csvRecord, ImportResultDTO importResult) {
		String technologyCategory = CSVUtils.getAsString(csvRecord, TECHNOLOGY_CATEGORY_HEADER);
		String technology = CSVUtils.getAsString(csvRecord, TECHNOLOGY_HEADER);
		String associateVendor = CSVUtils.getAsString(csvRecord, ASSOCIATE_VENDOR_HEADER);
		String targetTechnologyCategory = CSVUtils.getAsString(csvRecord, TARGET_TECHNOLOGY_CATEGORY_HEADER);
		String targetTechnologySubcategory = CSVUtils.getAsString(csvRecord, TARGET_TECHNOLOGY_SUBCATEGORY_HEADER);
		String targetTechnologyClass = CSVUtils.getAsString(csvRecord, TARGET_TECHNOLOGY_CLASS_HEADER);
		String targetTechnology = CSVUtils.getAsString(csvRecord, TARGET_TECHNOLOGY_HEADER);

		Optional<TechnologyCategoryImportMappings> itemOptional = technologyCategoryImportMappingRepository.getFirstByNameAndTechnologyAndOrganization(organization.getId(), technologyCategory, technology);
		TechnologyCategoryImportMappings mappingItem = itemOptional.orElse(new TechnologyCategoryImportMappings());
		mappingItem.setOrganizationId(organization.getId());
		mappingItem.setTechnologyCategory(technologyCategory);
		mappingItem.setTechnologyName(technology);
		mappingItem.setTechnologyVendor(associateVendor);
		mappingItem.setTargetTechnologyCategory(targetTechnologyCategory);
		mappingItem.setTargetTechnologySubcategory(targetTechnologySubcategory);
		mappingItem.setTargetTechnologyClass(targetTechnologyClass);
		mappingItem.setTargetTechnologyName(targetTechnology);

		boolean isNew = mappingItem.getId() == null;

		TechnologyCategoryImportMappings result = technologyCategoryImportMappingRepository.save(mappingItem);

		if (isNew) {
			importResult.getCreated().add(new ItemViewDTO(result.getId(), result.getTechnologyName()));
		} else {
			importResult.getUpdated().add(new ItemViewDTO(result.getId(), result.getTechnologyName()));
		}

		return result;
	}

}
