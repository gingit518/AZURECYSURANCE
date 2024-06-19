package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.dao.ControlSubcategoryModelDAO;
import com.cyberintech.vrisk.server.model.dao.PagedResult;
import com.cyberintech.vrisk.server.model.data.ByFrameworkFilter;
import com.cyberintech.vrisk.server.model.data.FilteredRequest;
import com.cyberintech.vrisk.server.model.data.FilteredResponse;
import com.cyberintech.vrisk.server.model.dto.ImportResultDTO;
import com.cyberintech.vrisk.server.model.dto.ItemViewDTO;
import com.cyberintech.vrisk.server.model.dto.control_subcategory.ControlSubcategoryEditDTO;
import com.cyberintech.vrisk.server.model.dto.control_subcategory.ControlSubcategoryViewDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.RoleType;
import com.cyberintech.vrisk.server.model.jpa.entity.*;
import com.cyberintech.vrisk.server.repository.jpa.AssessmentWeightsRepository;
import com.cyberintech.vrisk.server.repository.jpa.ControlCategoriesRepository;
import com.cyberintech.vrisk.server.repository.jpa.ControlFunctionsRepository;
import com.cyberintech.vrisk.server.repository.jpa.ControlSubcategoriesRepository;
import com.cyberintech.vrisk.server.rest.exception.*;
import com.cyberintech.vrisk.server.service.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;

/**
 * Control Subcategories management Service. Implements basic user CRUD.
 * [RENAMED to Control Guidelines]
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-01-08
 */
@Service
@Slf4j
public class ControlSubcategoryService {

	public static final String CONTROL_FUNCTION_CODE_HEADER = "Function Code";
	public static final String CONTROL_FUNCTION_NAME_HEADER = "Function Name";
	public static final String CONTROL_FUNCTION_DESCRIPTION_HEADER = "Function Description";
	public static final String CONTROL_CATEGORY_CODE_HEADER = "Control Test Code";
	public static final String CONTROL_CATEGORY_NAME_HEADER = "Control Test Name";
	public static final String CONTROL_CATEGORY_DESCRIPTION_HEADER = "Control Test Description";
	public static final String CONTROL_SUBCATEGORY_CODE_HEADER = "Guideline Code";
	public static final String CONTROL_SUBCATEGORY_NAME_HEADER = "Guideline Name";
	public static final String CONTROL_SUBCATEGORY_DESCRIPTION_HEADER = "Guideline Description";


	@Autowired
	private ControlSubcategoriesRepository controlSubcategoriesRepository;

	@Autowired
	private ControlCategoriesRepository controlCategoriesRepository;

	@Autowired
	private ControlFunctionsRepository controlFunctionsRepository;

	@Autowired
	private AssessmentWeightsRepository assessmentWeightsRepository;

	@Autowired
	private AssessmentTypeService assessmentTypeService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private ControlSubcategoryModelDAO controlSubcategoryModelDAO;

	/**
	 * Get Control Subcategories List
	 *
	 * @return Control Subcategories List
	 */
	public FilteredResponse<ByFrameworkFilter, ControlSubcategoryViewDTO> getListFiltered(FilteredRequest<ByFrameworkFilter> filteredRequest) {

		PagedResult<ControlSubcategoryViewDTO> pagedResult = controlSubcategoryModelDAO.getItemsPageable(filteredRequest.getFilter(), filteredRequest.toPageRequest(), filteredRequest.getSort());
		FilteredResponse<ByFrameworkFilter, ControlSubcategoryViewDTO> filteredResponse = new FilteredResponse<>(filteredRequest, pagedResult);

		return filteredResponse;
	}

	/**
	 * Get Control Subcategory details
	 *
	 * @return Control Subcategory Details
	 */
	public ControlSubcategories getControlSubcategoryForCurrentOrganization(Long itemId) {
		ControlSubcategories itemDetails;

		try {
			itemDetails = controlSubcategoriesRepository.findById(itemId).get();
		} catch (NoSuchElementException exception) {
			throw new ItemNotFoundException(MessageFormat.format("Control Subcategory not found in the database [{0}]", itemId));
		}

		// Verify Control Subcategory and Organization
		if (!organizationService.getCurrentOrganizationId().equals(itemDetails.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Control Subcategory [{0}] doesn't match your organization [{1}]", itemDetails.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		return itemDetails;
	}

	/**
	 * Get Control Subcategory DTO details
	 *
	 * @return Control Subcategory Details
	 */
	public ControlSubcategoryEditDTO getDetails(Long itemId) {

		ControlSubcategories itemDetails = getControlSubcategoryForCurrentOrganization(itemId);

		ControlSubcategoryEditDTO result = new ControlSubcategoryEditDTO(itemDetails);

		return result;
	}

	/**
	 * Create new Control Subcategory
	 *
	 * @return New Control Subcategory
	 */
	public ControlSubcategoryEditDTO create(ControlSubcategoryEditDTO newItemDTO) {
		// Throw Exception if ID is set in create mode
		if (newItemDTO.getId() != null) {
			throw new ConflictException(MessageFormat.format("Conflict while create item. ID is not allowed on create [{0}]", newItemDTO.getId()));
		}

		ControlSubcategories newItem = new ControlSubcategories();
		newItem.setOrganizationId(organizationService.getCurrentOrganizationId());
		// newItem.setCreatedBy(userService.getCurrentUserEntity());
		// newItem.setCreatedAt(new Date());
		applyEntityChanges(newItemDTO, newItem);
		ControlSubcategories saveResult = controlSubcategoriesRepository.save(newItem);

		ControlSubcategoryEditDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Update Control Subcategory
	 *
	 * @return Updated Control Subcategory
	 */
	public ControlSubcategoryEditDTO update(ControlSubcategoryEditDTO itemDTO) {

		// Get Existing item from the database
		ControlSubcategories existingItem = getControlSubcategoryForCurrentOrganization(itemDTO.getId());

		// Verify Control Subcategory and Organization
		if (!organizationService.getCurrentOrganizationId().equals(existingItem.getOrganizationId())) {
			throw new ForbiddenException(MessageFormat.format("Organization for Control Subcategory [{0}] doesn't match your organization [{1}]", existingItem.getOrganizationId(), organizationService.getCurrentOrganizationId()));
		}

		// Update item details
		existingItem.setName(itemDTO.getName());
		existingItem.setDescription(itemDTO.getDescription());
		applyEntityChanges(itemDTO, existingItem);

		// Save to the database
		ControlSubcategories saveResult = controlSubcategoriesRepository.save(existingItem);

		ControlSubcategoryEditDTO result = getDetails(saveResult.getId());

		return result;
	}

	/**
	 * Apply entity changes and linkages
	 *
	 * @param itemDTO
	 * @param entity
	 */
	private void applyEntityChanges(ControlSubcategoryEditDTO itemDTO, ControlSubcategories entity) {

		// Apply new Values
		entity.setCode(itemDTO.getCode());
		entity.setName(itemDTO.getName());
		entity.setDescription(itemDTO.getDescription());

		if (itemDTO.getControlCategory() != null && itemDTO.getControlCategory().getId() != null) {
			ControlCategories controlCategory = controlCategoriesRepository.findById(itemDTO.getControlCategory().getId()).get();
			entity.setControlCategory(controlCategory);

			// Set Assessment Type cache
			entity.setAssessmentType(controlCategory.getAssessmentType());
		}

		Optional.ofNullable(itemDTO.getAssessmentWeights()).ifPresent(assessmentWeightViewDTOS -> {
			entity.setAssessmentWeights(new HashSet<>());
			assessmentWeightViewDTOS.stream().forEach(assessmentWeightViewDTO -> {
				if (assessmentWeightViewDTO.getId() != null) {
					AssessmentWeights assessmentWeights = assessmentWeightsRepository.findById(assessmentWeightViewDTO.getId()).get();
//					assessmentWeightViewDTO.toEntity(assessmentWeights);
					assessmentWeights.setName(assessmentWeightViewDTO.getName());
					assessmentWeights.setDescription(assessmentWeightViewDTO.getDescription());
					assessmentWeights.setValue(assessmentWeightViewDTO.getValue());
					entity.getAssessmentWeights().add(assessmentWeights);
				} else {
//					AssessmentWeights assessmentWeights = assessmentWeightViewDTO.toEntity();
					AssessmentWeights assessmentWeights = new AssessmentWeights();
					assessmentWeights.setOrganizationId(entity.getOrganizationId());
					assessmentWeights.setName(assessmentWeightViewDTO.getName());
					assessmentWeights.setDescription(assessmentWeightViewDTO.getDescription());
					assessmentWeights.setValue(assessmentWeightViewDTO.getValue());
					assessmentWeightsRepository.save(assessmentWeights);
					entity.getAssessmentWeights().add(assessmentWeights);
				}
			});
		});

		// entity.setUpdatedBy(userService.getCurrentUserEntity());
		// entity.setUpdatedAt(new Date());
	}

	/**
	 * Deletes Control Subcategory
	 *
	 * @return ID of removed item
	 */
	@Transactional
	public Long delete(Long itemId) {

		ControlSubcategories existingItem = getControlSubcategoryForCurrentOrganization(itemId);
		controlSubcategoriesRepository.delete(existingItem);
		controlSubcategoriesRepository.flush();

		return itemId;
	}

	/**
	 * Create CSV Printer to build Assessment Framework Vocabulary
	 *
	 * @param outputStream
	 * @return CSVPrinter
	 * @throws IOException
	 */
	private CSVPrinter createCsvPrinter(ByteArrayOutputStream outputStream) throws IOException {
		Writer writer = new OutputStreamWriter(outputStream);
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
			CONTROL_FUNCTION_CODE_HEADER,
			CONTROL_FUNCTION_NAME_HEADER,
			CONTROL_FUNCTION_DESCRIPTION_HEADER,
			CONTROL_CATEGORY_CODE_HEADER,
			CONTROL_CATEGORY_NAME_HEADER,
			CONTROL_CATEGORY_DESCRIPTION_HEADER,
			CONTROL_SUBCATEGORY_CODE_HEADER,
			CONTROL_SUBCATEGORY_NAME_HEADER,
			CONTROL_SUBCATEGORY_DESCRIPTION_HEADER
		);
		return new CSVPrinter(writer, csvFormat);
	}

	/**
	 * Get content for download
	 *
	 * @param assessmentType
	 * @return
	 */
	public ByteArrayInputStream getDownloadData(AssessmentTypes assessmentType) {
		ByteArrayInputStream byteArrayInputStream = null;

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = createCsvPrinter(outputStream);

			// Find items By Assessment Type
			Set<ControlSubcategories> items = new HashSet<>();
			if (assessmentType != null) {
				items = controlSubcategoriesRepository.findAllByOrganizationIdAndAssessmentType(organizationService.getCurrentOrganizationId(), assessmentType.getId());
			}

			Long lastFunctionId = 0L;
			Long lastCategoryId = 0L;
			for (ControlSubcategories item: items) {
				ControlCategories category = Optional.ofNullable(item.getControlCategory()).orElse(new ControlCategories());
				ControlFunctions function = Optional.ofNullable(category.getControlFunction()).orElse(new ControlFunctions());

				boolean isCategoryChanged = true;
				boolean isFunctionChanged = true;
				if (lastFunctionId.equals(function.getId())) {
					isFunctionChanged = false;
				}
				if (lastCategoryId.equals(category.getId())) {
					isCategoryChanged = false;
				}
				csvPrinter.printRecord(
					isFunctionChanged ? function.getCode(): "",
					isFunctionChanged ? function.getName(): "",
					isFunctionChanged ? function.getDescription(): "",
					isCategoryChanged ? category.getCode(): "",
					isCategoryChanged ? category.getName(): "",
					isCategoryChanged ? category.getDescription(): "",
					item.getCode() != null ? item.getCode(): "",
					item.getName() != null ? item.getName(): "",
					item.getDescription() != null ? item.getDescription(): ""
				);

				lastFunctionId = function.getId() != null ? function.getId(): 0L;
				lastCategoryId = category.getId() != null ? category.getId(): 0L;
			}
			csvPrinter.flush();

			byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException exception) {
			log.warn(exception.getMessage(), exception);
			throw new InternalServerErrorException(MessageFormat.format("Failed to generate CSV Template file for Assessment Framework - {0}", assessmentType != null ? assessmentType.getName() : AssessmentTypeService.FRAMEWORK_NAME_NIST_CSF));
		}

		return byteArrayInputStream;
	}

	/**
	 * Insert Assessment Framework data from CSV file
	 */
	@Transactional
	public ImportResultDTO importFromCSVFile(InputStream fileContentStream, Long assessmentTypeId) {

		// Check Permissions to Import Assessment Framework Data
		if (!userService.hasRole(RoleType.ADMIN) && !userService.hasRole(RoleType.ORGANIZATION_ADMIN)) {
			throw new ForbiddenException("You are not allowed to import GDPR data!", ApplicationExceptionCodes.ASSESSMENT_IMPORT_FORBIDDEN);
		}

		Organizations organization = organizationService.getCurrentOrganizationEntity();
		log.info(MessageFormat.format("## Importing Assessment Framework Data For Organization: {0}", organization.getName()));

		AssessmentTypes assessmentType = assessmentTypeService.getAssessmentTypeForCurrentOrganization(assessmentTypeId);

		// Verify Assessment Type (Assessment Framework) and Organization
		ImportResultDTO result = new ImportResultDTO();
		try {
			// Parse CSV file
			CSVParser csvParser = CSVUtils.createCSVParser(fileContentStream);
			List<CSVRecord> csvRecordList = csvParser.getRecords();

			result = this.importFromCSVItems(csvRecordList, organization, assessmentType);
		} catch (IOException exception) {
			log.warn(exception.getMessage(), exception);
		}

		return result;
	}

	/**
	 * Insert some data from CSV file
	 */
	@Transactional
	protected ImportResultDTO importFromCSVItems(List<CSVRecord> csvRecordList, Organizations organization, AssessmentTypes assessmentType) {

		ImportResultDTO result = new ImportResultDTO();

		Map<String, ControlSubcategories> controlSubcategoriesMap = new HashMap<>();
		Map<String, ControlCategories> controlCategoriesMap = new HashMap<>();
		Map<String, ControlFunctions> controlFunctionsMap = new HashMap<>();

		// Proceed with assessment framework management
		String lastControlFunctionCode = "";
		String lastControlFunctionName = "";
		String lastControlFunctionDescription = "";
		String lastControlCategoryCode = "";
		String lastControlCategoryName = "";
		String lastControlCategoryDescription = "";
		for (CSVRecord csvRecord: csvRecordList) {

			// Accessing values by Header names
			String controlFunctionCode = Optional.ofNullable(csvRecord.get(CONTROL_FUNCTION_CODE_HEADER)).orElse("").trim();
			String controlFunctionName = Optional.ofNullable(csvRecord.get(CONTROL_FUNCTION_NAME_HEADER)).orElse("").trim();
			String controlFunctionDescription = Optional.ofNullable(csvRecord.get(CONTROL_FUNCTION_DESCRIPTION_HEADER)).orElse("").trim();
			String controlCategoryCode = Optional.ofNullable(csvRecord.get(CONTROL_CATEGORY_CODE_HEADER)).orElse("").trim();
			String controlCategoryName = Optional.ofNullable(csvRecord.get(CONTROL_CATEGORY_NAME_HEADER)).orElse("").trim();
			String controlCategoryDescription = Optional.ofNullable(csvRecord.get(CONTROL_CATEGORY_DESCRIPTION_HEADER)).orElse("").trim();
			String controlSubcategoryCode = Optional.ofNullable(csvRecord.get(CONTROL_SUBCATEGORY_CODE_HEADER)).orElse("").trim();
			String controlSubcategoryName = Optional.ofNullable(csvRecord.get(CONTROL_SUBCATEGORY_NAME_HEADER)).orElse("").trim();
			String controlSubcategoryDescription = Optional.ofNullable(csvRecord.get(CONTROL_SUBCATEGORY_DESCRIPTION_HEADER)).orElse("").trim();

			if (StringUtils.isNotEmpty(controlFunctionName)) {
				lastControlFunctionCode = controlFunctionCode;
				lastControlFunctionName = controlFunctionName;
				lastControlFunctionDescription = controlFunctionDescription;
			}

			if (StringUtils.isNotEmpty(lastControlFunctionName)) {
				// Process Control Function
				ControlFunctions controlFunction = controlFunctionsMap.get(StringUtils.isEmpty(lastControlFunctionCode) ? lastControlFunctionName : lastControlFunctionCode);

				if (controlFunction == null) {

					// Detect last control function
					Optional<ControlFunctions> controlFunctionOptional = controlFunctionsRepository.findFirstByNameIgnoreCaseAndAssessmentType(lastControlFunctionName, assessmentType);
					if (controlFunctionOptional.isEmpty() && StringUtils.isNotEmpty(lastControlFunctionCode)) {
						controlFunctionOptional = controlFunctionsRepository.findFirstByCodeIgnoreCaseAndAssessmentType(lastControlFunctionCode, assessmentType);
					}

					if(!controlFunctionOptional.isPresent()) {
						controlFunction = new ControlFunctions();
						controlFunction.setOrganizationId(organization.getId());
						controlFunction.setAssessmentType(assessmentType);
					} else {
						controlFunction = controlFunctionOptional.get();
					}
					controlFunction.setCode(lastControlFunctionCode);
					controlFunction.setName(lastControlFunctionName);
					controlFunction.setDescription(controlFunctionDescription);

					controlFunction = controlFunctionsRepository.save(controlFunction);

					boolean isNew = !controlFunctionOptional.isPresent();

					if (isNew) {
						log.info(MessageFormat.format("## Create new Control Function [{0}, {1}]", lastControlFunctionCode, lastControlFunctionName));
						result.getCreated().add(new ItemViewDTO(controlFunction.getId(), controlFunction.getName()));
						result.getMessages().add(MessageFormat.format("Create new Control Function: {0}, {1}", lastControlFunctionCode, lastControlFunctionName));
					} else {
						log.info(MessageFormat.format("## Update Control Function [{0}, {1}]", lastControlFunctionCode, lastControlFunctionName));
						result.getUpdated().add(new ItemViewDTO(controlFunction.getId(), controlFunction.getName()));
						result.getMessages().add(MessageFormat.format("Update Control Function: {0}, {1}", lastControlFunctionCode, lastControlFunctionName));
					}

					// Put Control Function item to the Map
					controlFunctionsMap.put(StringUtils.isEmpty(lastControlFunctionCode) ? lastControlFunctionName : lastControlFunctionCode, controlFunction);
				}

				// Process Control Category (Control Test)
				ControlCategories controlCategory = null;
				if (StringUtils.isNotEmpty(controlCategoryName)) {
					lastControlCategoryCode = controlCategoryCode;
					lastControlCategoryName = controlCategoryName;
					lastControlCategoryDescription = controlCategoryDescription;
				}
				if (StringUtils.isNotEmpty(lastControlCategoryName)) {

					controlCategory = controlCategoriesMap.get(StringUtils.isEmpty(lastControlCategoryCode) ? lastControlCategoryName : lastControlCategoryCode);

					if (controlCategory == null) {
						Optional<ControlCategories> controlCategoryOptional = controlCategoriesRepository.findFirstByNameIgnoreCaseAndControlFunction(lastControlCategoryName, controlFunction);
						if (controlCategoryOptional.isEmpty()) controlCategoryOptional = controlCategoriesRepository.findFirstByCodeIgnoreCaseAndControlFunction(lastControlCategoryCode, controlFunction);
						if (controlCategoryOptional.isEmpty()) controlCategoryOptional = controlCategoriesRepository.findFirstByNameIgnoreCaseAndAssessmentType(lastControlCategoryName, assessmentType);
						if (controlCategoryOptional.isEmpty()) controlCategoryOptional = controlCategoriesRepository.findFirstByCodeIgnoreCaseAndAssessmentType(lastControlCategoryCode, assessmentType);

						if (!controlCategoryOptional.isPresent()) {
							controlCategory = new ControlCategories();
							controlCategory.setOrganizationId(organization.getId());
							controlCategory.setAssessmentType(assessmentType);
							controlCategory.setControlFunction(controlFunction);
						} else {
							controlCategory = controlCategoryOptional.get();
						}
						controlCategory.setCode(lastControlCategoryCode);
						controlCategory.setName(lastControlCategoryName);
						controlCategory.setDescription(controlCategoryDescription);

						controlCategory = controlCategoriesRepository.save(controlCategory);

						boolean isNew = !controlCategoryOptional.isPresent();

						if (isNew) {
							log.info(MessageFormat.format("## Create new Control Test [{0}, {1}]", lastControlCategoryCode, lastControlCategoryName));
							result.getCreated().add(new ItemViewDTO(controlCategory.getId(), controlCategory.getName()));
							result.getMessages().add(MessageFormat.format("Create new Control Test: {0}, {1}", lastControlCategoryCode, lastControlCategoryName));
						} else {
							log.info(MessageFormat.format("## Update Control Test [{0}, {1}]", lastControlCategoryCode, lastControlCategoryName));
							result.getUpdated().add(new ItemViewDTO(controlCategory.getId(), controlCategory.getName()));
							result.getMessages().add(MessageFormat.format("Update Control Test: {0}, {1}", lastControlCategoryCode, lastControlCategoryName));
						}

						// Put Control Category item to the Map
						controlCategoriesMap.put(StringUtils.isEmpty(lastControlCategoryCode) ? lastControlCategoryName : lastControlCategoryCode, controlCategory);
					}
				}

				// Process Control Subcategory (Control Guideline)
				ControlSubcategories controlSubcategory = null;
				if (StringUtils.isNotEmpty(controlSubcategoryName)) {
					controlSubcategory = controlSubcategoriesMap.get(StringUtils.isNotEmpty(controlSubcategoryCode) ? controlSubcategoryName : controlSubcategoryCode);
					if (controlSubcategory == null) {
						Optional<ControlSubcategories> controlSubcategoryOptional = controlSubcategoriesRepository.findFirstByNameIgnoreCaseAndAssessmentType(controlSubcategoryName, assessmentType);
						if (controlSubcategoryOptional.isEmpty() && StringUtils.isNotEmpty(controlSubcategoryCode)) {
							controlSubcategoryOptional = controlSubcategoriesRepository.findFirstByCodeIgnoreCaseAndAssessmentType(controlSubcategoryCode, assessmentType);
						}

						if(!controlSubcategoryOptional.isPresent()) {
							controlSubcategory = new ControlSubcategories();
							controlSubcategory.setOrganizationId(organization.getId());
							controlSubcategory.setAssessmentType(assessmentType);
							controlSubcategory.setControlCategory(controlCategory);
						} else {
							log.info(MessageFormat.format("## Already Existed Control Guideline [{0}, {1}]", controlSubcategoryCode, controlSubcategoryName));
							controlSubcategory = controlSubcategoryOptional.get();
						}
						controlSubcategory.setCode(controlSubcategoryCode);
						controlSubcategory.setName(controlSubcategoryName);
						controlSubcategory.setDescription(controlSubcategoryDescription);

						controlSubcategory = controlSubcategoriesRepository.save(controlSubcategory);

						boolean isNew = !controlSubcategoryOptional.isPresent();

						if (isNew) {
							log.info(MessageFormat.format("## Create new Control Guideline [{0}, {1}]", controlSubcategoryCode, controlSubcategoryName));
							result.getCreated().add(new ItemViewDTO(controlSubcategory.getId(), controlSubcategory.getName()));
							result.getMessages().add(MessageFormat.format("Create new Control Guideline: {0}, {1}", controlSubcategoryCode, controlSubcategoryName));
						} else {
							log.info(MessageFormat.format("## Update Control Guideline [{0}, {1}]", controlSubcategoryCode, controlSubcategoryName));
							result.getUpdated().add(new ItemViewDTO(controlSubcategory.getId(), controlSubcategory.getName()));
							result.getMessages().add(MessageFormat.format("Update Control Guideline: {0}, {1}", controlSubcategoryCode, controlSubcategoryName));
						}

						// Put Control Subcategory item to the Map
						controlSubcategoriesMap.put(controlSubcategoryCode.isEmpty() ? controlSubcategoryName : controlSubcategoryCode, controlSubcategory);
					}
				}

			}
		}

		return result;
	}

}





















